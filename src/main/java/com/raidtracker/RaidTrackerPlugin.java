package com.raidtracker;

import com.google.inject.Provides;
import com.google.inject.Inject;
import com.raidtracker.filereadwriter.FileReadWriter;
import com.raidtracker.ui.RaidTrackerPanel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.client.game.ItemManager;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Integer.parseInt;

@Slf4j
@PluginDescriptor(
	name = "Raid Data Tracker"
)
public class RaidTrackerPlugin extends Plugin
{
	private static final String LEVEL_COMPLETE_MESSAGE = "level complete!";
	private static final String RAID_COMPLETE_MESSAGE = "Congratulations - your raid is complete!";
	private static final String DUST_RECIPIENTS = "Dust recipients: ";
	private static final String TWISTED_KIT_RECIPIENTS = "Twisted Kit recipients: ";


	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private RaidTrackerConfig config;


	@Inject
	private ItemManager itemManager;

	@Inject
	private RaidTracker raidTracker;
	
	private static final WorldPoint TEMP_LOCATION = new WorldPoint(3360, 5152, 2);

	@Setter
	private RaidTrackerPanel panel;
	private NavigationButton navButton;

	@Setter
	private FileReadWriter fw = new FileReadWriter();

	@Provides
	RaidTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidTrackerConfig.class);
	}

	@Override
	protected void startUp() {
		panel = new RaidTrackerPanel(itemManager, fw, config);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel-icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Raid Data Tracker")
				.priority(6)
				.icon(icon)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);


		if (client.getGameState().equals(GameState.LOGGED_IN) || client.getGameState().equals(GameState.LOADING))
		{
			fw.updateUsername(client.getUsername());
			SwingUtilities.invokeLater(() -> panel.loadRTList());
		}
	}

	@Override
	protected void shutDown() {
		raidTracker.setInRaidChambers(false);
		clientToolbar.removeNavigation(navButton);
		reset();
	}


	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		boolean tempInRaid = client.getVar(Varbits.IN_RAID) == 1;
		boolean tempInTob = client.getVar(Varbits.THEATRE_OF_BLOOD) == 1;

		// if the player's raid state has changed
		if (tempInRaid ^ raidTracker.isInRaidChambers()) {
			// if the player is inside of a raid then check the raid
			if (tempInRaid && raidTracker.isLoggedIn()) {
				checkRaidPresence();
			}
			else if (raidTracker.isRaidComplete() && !raidTracker.isChestOpened()) {
				//player just exited a raid, if the chest is not looted write the raid tracker anyway.
				//might deprecate the writing after widgetloaded in the future, not decided yet.
				fw.writeToFile(raidTracker);

				SwingUtilities.invokeLater(() -> {
					panel.addDrop(raidTracker);
					reset();
				});
			}
		}

		if (tempInTob ^ raidTracker.isInTheatreOfBlood()) {
			if (tempInTob && raidTracker.isLoggedIn()) {
				checkTobPresence();
			}
			else if (raidTracker.isRaidComplete()) {
				//not tested
				log.info("writing to file");
				log.info(raidTracker.toString());

				fw.writeToFile(raidTracker);

				SwingUtilities.invokeLater(() -> {
					panel.addDrop(raidTracker);
					reset();
				});
			}
			else {
				log.info(raidTracker.toString());
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGING_IN)
		{
			fw.updateUsername(client.getUsername());
			SwingUtilities.invokeLater(() -> panel.loadRTList());

		}

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			// skip event while the game decides if the player belongs in a raid or not
			if (client.getLocalPlayer() == null
					|| client.getLocalPlayer().getWorldLocation().equals(TEMP_LOCATION))
			{
				//noinspection UnnecessaryReturnStatement
				return;
			}
		}
		else if (client.getGameState() == GameState.LOGIN_SCREEN
				|| client.getGameState() == GameState.CONNECTION_LOST)
		{
			raidTracker.setLoggedIn(false);
		}
		else if (client.getGameState() == GameState.HOPPING)
		{
			reset();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		checkChatMessage(event, raidTracker);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		switch (event.getGroupId()) {
			case (WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID):
				if (raidTracker.isChestOpened() || !raidTracker.isRaidComplete()) {
					return;
				}

				raidTracker.setTeamSize(client.getVar(Varbits.RAID_PARTY_SIZE));

				raidTracker.setChestOpened(true);

				ItemContainer rewardItemContainer = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);

				if (rewardItemContainer == null) {
					return;
				}

				raidTracker.setLootList(lootListFactory(rewardItemContainer.getItems()));

				fw.writeToFile(raidTracker);

				SwingUtilities.invokeLater(() -> {
					panel.addDrop(raidTracker);
					reset();
				});

				break;

			case (WidgetID.THEATRE_OF_BLOOD_GROUP_ID):
				if (raidTracker.isChestOpened() || !raidTracker.isRaidComplete()) {
					return;
				}

				int teamSize = 0;

				for (int i = 6442; i  < 6447; i++) {
					if (client.getVarbitValue(i) != 0) {
						teamSize++;
					}
				}

				raidTracker.setTeamSize(teamSize);

				raidTracker.setChestOpened(true);

				rewardItemContainer = client.getItemContainer(InventoryID.THEATRE_OF_BLOOD_CHEST);

				if (rewardItemContainer == null) {
					return;
				}

				raidTracker.setLootList(lootListFactory(rewardItemContainer.getItems()));

				break;
			//459 is the mvp screen of TOB
			case (459):
				AtomicReference<String> mvp = new AtomicReference<>("");
				AtomicReference<String> player1 = new AtomicReference<>("");
				AtomicReference<String> player2 = new AtomicReference<>("");
				AtomicReference<String> player3 = new AtomicReference<>("");
				AtomicReference<String> player4 = new AtomicReference<>("");
				AtomicReference<String> player5 = new AtomicReference<>("");
				AtomicInteger deathsPlayer1 = new AtomicInteger();
				AtomicInteger deathsPlayer2 = new AtomicInteger();
				AtomicInteger deathsPlayer3 = new AtomicInteger();
				AtomicInteger deathsPlayer4 = new AtomicInteger();
				AtomicInteger deathsPlayer5 = new AtomicInteger();

				SwingUtilities.invokeLater(() -> {
					mvp.set(getWidgetText(client.getWidget(459, 14)));
					player1.set(getWidgetText(client.getWidget(459, 22)));
					player2.set(getWidgetText(client.getWidget(459, 24)));
					player3.set(getWidgetText(client.getWidget(459, 26)));
					player4.set(getWidgetText(client.getWidget(459, 28)));
					player5.set(getWidgetText(client.getWidget(459, 30)));
					deathsPlayer1.set(getWidgetNumber(client.getWidget(459, 23)));
					deathsPlayer2.set(getWidgetNumber(client.getWidget(459, 25)));
					deathsPlayer3.set(getWidgetNumber(client.getWidget(459, 27)));
					deathsPlayer4.set(getWidgetNumber(client.getWidget(459, 29)));
					deathsPlayer5.set(getWidgetNumber(client.getWidget(459, 31)));

					raidTracker.setMvp(mvp.get());
					raidTracker.setTobPlayer1(player1.get());
					raidTracker.setTobPlayer2(player2.get());
					raidTracker.setTobPlayer3(player3.get());
					raidTracker.setTobPlayer4(player4.get());
					raidTracker.setTobPlayer5(player5.get());

					raidTracker.setTobPlayer1DeathCount(deathsPlayer1.get());
					raidTracker.setTobPlayer2DeathCount(deathsPlayer2.get());
					raidTracker.setTobPlayer3DeathCount(deathsPlayer3.get());
					raidTracker.setTobPlayer4DeathCount(deathsPlayer4.get());
					raidTracker.setTobPlayer5DeathCount(deathsPlayer5.get());

					if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
						raidTracker.setMvpInOwnName(mvp.get().toLowerCase().equals(client.getLocalPlayer().getName().toLowerCase()));
					}
				});

				break;
		}
	}

	private String getWidgetText(Widget widget) {
		if (widget == null) {
			return "";
		}
		else if (widget.getText().equals("-")) {
			return "";
		}
		return widget.getText();
	}

	private int getWidgetNumber(Widget widget) {
		if (widget == null) {
			return 0;
		}
		else if (widget.getText().equals("-")) {
			return 0;
		}
		return Integer.parseInt(widget.getText());
	}

	public void checkChatMessage(ChatMessage event, RaidTracker raidTracker)
	{
		raidTracker.setLoggedIn(true);

		Player localPlayer = client.getLocalPlayer();

		if ((raidTracker.isInRaidChambers() && event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION) || (raidTracker.isInRaidChambers() && event.getType() == ChatMessageType.GAMEMESSAGE)) {
			String message = Text.removeTags(event.getMessage());
			if (message.contains(LEVEL_COMPLETE_MESSAGE)) {
				if (message.startsWith("Upper")) {
					raidTracker.setUpperTime(stringTimeToSeconds(message.split(" level complete! Duration: ")[1]));
				}
				if (message.startsWith("Middle")) {
					raidTracker.setMiddleTime(stringTimeToSeconds(message.split(" level complete! Duration: ")[1]));
				}
				if (message.startsWith("Lower")) {
					raidTracker.setLowerTime(stringTimeToSeconds(message.split(" level complete! Duration: ")[1]));
				}
			}

			if (message.startsWith(RAID_COMPLETE_MESSAGE)) {
				raidTracker.setTotalPoints(client.getVar(Varbits.TOTAL_POINTS));

				raidTracker.setPersonalPoints(client.getVar(Varbits.PERSONAL_POINTS));

				raidTracker.setPercentage(raidTracker.getPersonalPoints() / (raidTracker.getTotalPoints() / 100.0));

				raidTracker.setRaidComplete(true);

				raidTracker.setDate(System.currentTimeMillis());
			}

			//the message after every completed tob wave.
			if (message.toLowerCase().contains("wave '")) {
				String wave = message.toLowerCase().split("'")[1];

				switch (wave) {
					case("the maiden of sugadinti"):
						raidTracker.setMaidenTime(stringTimeToSeconds(message.toLowerCase().split("duration: ")[1].split(" total")[0]));
						break;
					case("the pestilent bloat"):
						raidTracker.setBloatTime(stringTimeToSeconds(message.toLowerCase().split("duration: ")[1].split(" total")[0]));
						break;
					case("the nylocas"):
						raidTracker.setNyloTime(stringTimeToSeconds(message.toLowerCase().split("duration: ")[1].split(" total")[0]));
						break;
					case("sotetseg"):
						raidTracker.setSotetsegTime(stringTimeToSeconds(message.toLowerCase().split("duration: ")[1].split(" total")[0]));
						break;
					case("xarpus"):
						raidTracker.setXarpusTime(stringTimeToSeconds(message.toLowerCase().split("duration: ")[1].split(" total")[0]));
						break;
					case("the final challenge"):
						raidTracker.setVerzikTime(stringTimeToSeconds(message.toLowerCase().split("duration: ")[1].split(" total")[0]));
						break;
				}
			}

			if (message.toLowerCase().contains("theatre of blood wave completion")) {
				raidTracker.setRaidComplete(true);
				raidTracker.setRaidTime(stringTimeToSeconds(message.toLowerCase().split("time: ")[1]));
			}

			if (raidTracker.isRaidComplete() && message.contains("Team size:")) {
				raidTracker.setRaidTime(stringTimeToSeconds(message.split("Duration: ")[1].split(" ")[0]));
			}

			//works for tob
			if (raidTracker.isRaidComplete() && message.contains("count is:")) {
				raidTracker.setChallengeMode(message.contains("Chambers of Xeric Challenge Mode"));
				raidTracker.setCompletionCount(parseInt(message.split("count is:")[1].trim().replace(".", "")));
			}

			//only special loot contain the "-" (except for the raid complete message)
			if (raidTracker.isRaidComplete() && message.contains("-") && !message.startsWith(RAID_COMPLETE_MESSAGE)) {
				if (!raidTracker.getSpecialLootReceiver().isEmpty()) {
					RaidTracker altRT = copyData();

					altRT.setSpecialLootReceiver(message.split(" - ")[0]);
					altRT.setSpecialLoot(message.split(" - ")[1]);

					if (localPlayer != null && localPlayer.getName() != null) {
						altRT.setSpecialLootInOwnName(altRT.getSpecialLootReceiver().toLowerCase().trim().equals(localPlayer.getName().toLowerCase().trim()));
					}

					altRT.setSpecialLootValue(itemManager.search(raidTracker.getSpecialLoot()).get(0).getPrice());

					setSplits(altRT);

					fw.writeToFile(altRT);

					SwingUtilities.invokeLater(() -> {
						panel.addDrop(altRT, false);
					});
				}
				else {
					raidTracker.setSpecialLootReceiver(message.split(" - ")[0]);
					raidTracker.setSpecialLoot(message.split(" - ")[1]);

					raidTracker.setSpecialLootValue(itemManager.search(raidTracker.getSpecialLoot()).get(0).getPrice());

					if (localPlayer != null && localPlayer.getName() != null) {
						raidTracker.setSpecialLootInOwnName(raidTracker.getSpecialLootReceiver().toLowerCase().trim().equals(localPlayer.getName().toLowerCase().trim()));
					}

					setSplits(raidTracker);
				}
			}

			//for tob it works a bit different, not possible to get duplicates.
			if (raidTracker.isRaidComplete() && message.toLowerCase().contains("found something special")) {
				raidTracker.setSpecialLootReceiver(message.toLowerCase().split(" found something special: ")[0]);
				raidTracker.setSpecialLoot(message.toLowerCase().split(" found something special: ")[1]);

				raidTracker.setSpecialLootValue(itemManager.search(raidTracker.getSpecialLoot()).get(0).getPrice());

				if (localPlayer != null && localPlayer.getName() != null) {
					raidTracker.setSpecialLootInOwnName(raidTracker.getSpecialLootReceiver().toLowerCase().trim().equals(localPlayer.getName().toLowerCase().trim()));
				}
			}

			if (raidTracker.isRaidComplete() && message.startsWith(TWISTED_KIT_RECIPIENTS)) {
				if (!raidTracker.getKitReceiver().isEmpty()) {
					RaidTracker altRT = copyData();
					altRT.setKitReceiver(message.split(TWISTED_KIT_RECIPIENTS)[1]);

					fw.writeToFile(altRT);

					SwingUtilities.invokeLater(() -> {
						panel.addDrop(altRT, false);
					});
				}
				else{
					raidTracker.setKitReceiver(message.split(TWISTED_KIT_RECIPIENTS)[1]);
				}

			}

			if (raidTracker.isRaidComplete() && message.startsWith(DUST_RECIPIENTS)) {
				if (!raidTracker.getDustReceiver().isEmpty()) {
					RaidTracker altRT = copyData();
					altRT.setDustReceiver(message.split(DUST_RECIPIENTS)[1]);

					fw.writeToFile(altRT);

					SwingUtilities.invokeLater(() -> {
						panel.addDrop(altRT, false);
					});
				}
				else{
					raidTracker.setDustReceiver(message.split(DUST_RECIPIENTS)[1]);
				}
			}

			if (raidTracker.isRaidComplete() && (message.toLowerCase().contains("olmlet") || message.toLowerCase().contains("lil' zik") || message.toLowerCase().contains("you have a funny feeling"))) {
				if (!raidTracker.getPetReceiver().isEmpty()) {
					RaidTracker altRT = copyData();
					if ((message.toLowerCase().contains("untradeable drop") || message.toLowerCase().contains("you have a funny feeling")) && client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
						altRT.setPetReceiver(client.getLocalPlayer().getName().toLowerCase().trim());
					} else {
						altRT.setPetReceiver(message.split(" ")[0]);
					}

					fw.writeToFile(altRT);

					SwingUtilities.invokeLater(() -> {
						panel.addDrop(altRT, false);
					});
				}
				else {
					if (message.toLowerCase().contains("untradeable drop") && client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
						raidTracker.setPetReceiver(client.getLocalPlayer().getName().toLowerCase().trim());
					} else {
						raidTracker.setPetReceiver(message.split(" ")[0]);
					}
				}
			}
		}
	}

	public void setSplits(RaidTracker raidTracker)
	{

		int lootSplit = raidTracker.getSpecialLootValue() / raidTracker.getTeamSize();

		int cutoff = config.FFACutoff();

		if (raidTracker.getSpecialLoot().length() > 0) {
			if (config.defaultFFA() || lootSplit < cutoff) {
				raidTracker.setFreeForAll(true);
				if (raidTracker.isSpecialLootInOwnName()) {
					raidTracker.setLootSplitReceived(raidTracker.getSpecialLootValue());
				}
			} else if (raidTracker.isSpecialLootInOwnName()) {
				raidTracker.setLootSplitPaid(raidTracker.getSpecialLootValue() - lootSplit);
				raidTracker.setLootSplitReceived(lootSplit);
			} else {
				raidTracker.setLootSplitReceived(lootSplit);
			}
		}
	}

	public ArrayList<RaidTrackerItem> lootListFactory(Item[] items)
	{
		ArrayList<RaidTrackerItem> lootList = new ArrayList<>();
		Arrays.stream(items)
				.filter(item -> item.getId() > -1)
				.forEach(item -> {
					ItemComposition comp = itemManager.getItemComposition(item.getId());
					lootList.add(new RaidTrackerItem() {
						{
							name = comp.getName();
							id = comp.getId();
							quantity = item.getQuantity();
							price = comp.getPrice() * quantity;
						}
					});
				});
		return lootList;
	}

	private void checkRaidPresence()
	{
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		raidTracker.setInRaidChambers(client.getVar(Varbits.IN_RAID) == 1);
	}

	private void checkTobPresence() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		log.info("in theatre");
		//1 = alive in party, 2 = spectating, 3 = dead spectating
		raidTracker.setInTheatreOfBlood(client.getVar(Varbits.THEATRE_OF_BLOOD) == 1 || client.getVar(Varbits.THEATRE_OF_BLOOD) == 3);
	}

	private int stringTimeToSeconds(String s)
	{
		return parseInt(s.split(":")[0]) * 60 + parseInt(s.split(":")[1]);
	}

	public RaidTracker copyData() {
		RaidTracker RT = new RaidTracker();

		RT.setDate(raidTracker.getDate());
		RT.setTeamSize(raidTracker.getTeamSize());
		RT.setChallengeMode(raidTracker.isChallengeMode());
		RT.setInTheatreOfBlood(raidTracker.isInTheatreOfBlood());
		RT.setCompletionCount(raidTracker.getCompletionCount());
		RT.setKillCountID(raidTracker.getKillCountID());

		return RT;
	}

	private void reset()
	{
		raidTracker = new RaidTracker();
	}
}
