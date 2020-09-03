package com.raidtracker;

import com.google.inject.Provides;
import com.google.inject.Inject;
import com.raidtracker.filereadwriter.FileReadWriter;
import com.raidtracker.ui.RaidTrackerPanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
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

	@Getter
	private int raidPartyID;

	@Inject
	private RaidTracker raidTracker;
	
	private static final WorldPoint TEMP_LOCATION = new WorldPoint(3360, 5152, 2);

	private RaidTrackerPanel panel;
	private NavigationButton navButton;

	private final FileReadWriter fw = new FileReadWriter();

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
		int tempPartyID = client.getVar(VarPlayer.IN_RAID_PARTY);
		boolean tempInRaid = client.getVar(Varbits.IN_RAID) == 1;

		// if the player's party state has changed
		if (tempPartyID != raidPartyID)
		{
			// if the player is outside of a raid when the party state changed
			if (raidTracker.isLoggedIn()
					&& !tempInRaid)
			{
				reset();
			}

			raidPartyID = tempPartyID;
		}

		// if the player's raid state has changed
		if (tempInRaid != raidTracker.isInRaidChambers())
		{
			// if the player is inside of a raid then check the raid
			if (tempInRaid && raidTracker.isLoggedIn())
			{
				checkRaidPresence();
			}

			raidTracker.setInRaidChambers(tempInRaid);
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

		if (event.getGroupId() != WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID ||
				raidTracker.isChestOpened() || !raidTracker.isRaidComplete()) {
			return;
		}

		raidTracker.setTeamSize(client.getVar(Varbits.RAID_PARTY_SIZE));

		checkChestOpened(event, raidTracker);

		ItemContainer rewardItemContainer = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);

		if (rewardItemContainer == null) {
			return;
		}

		raidTracker.setLootList(lootListFactory(rewardItemContainer.getItems()));
		//TODO: cutoff for ffa, to be set in config

		//TODO: change each split in sidebar (for example change split loot to ffa) and update it in the json

		setSplits(raidTracker);

		fw.writeToFile(raidTracker);

		SwingUtilities.invokeLater(() -> {
			panel.addDrop(raidTracker);
			reset();
		});
	}

	public void checkChatMessage(ChatMessage event, RaidTracker raidTracker)
	{
		raidTracker.setLoggedIn(true);
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
			}

			if (raidTracker.isRaidComplete() && message.contains("Team size:")) {
				raidTracker.setRaidTime(stringTimeToSeconds(message.split("Duration: ")[1].split(" ")[0]));
			}

			//only special loot contain the "-" (except for the raid complete message)
			if (raidTracker.isRaidComplete() && message.contains("-") && !message.startsWith(RAID_COMPLETE_MESSAGE)) {
				raidTracker.setSpecialLootReceiver(message.split(" - ")[0]);
				raidTracker.setSpecialLoot(message.split(" - ")[1]);

				raidTracker.setSpecialLootValue(itemManager.search(raidTracker.getSpecialLoot()).get(0).getPrice());
			}

			//not sure if it's possible to get multiples but i'm not gonna bother coding that in
			if (raidTracker.isRaidComplete() && message.startsWith(TWISTED_KIT_RECIPIENTS)) {
				raidTracker.setKitReceiver(message.split(TWISTED_KIT_RECIPIENTS)[1]);
			}
			if (raidTracker.isRaidComplete() && message.startsWith(DUST_RECIPIENTS)) {
				raidTracker.setDustReceiver(message.split(DUST_RECIPIENTS)[1]);
			}

			if (raidTracker.isRaidComplete() && message.contains("count is:")) {
				raidTracker.setChallengeMode(message.contains("Chambers of Xeric Challenge Mode"));
				raidTracker.setCompletionCount(parseInt(message.split("count is:")[1].trim().replace(".", "")));
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	public void setSplits(RaidTracker raidTracker)
	{
		Player localPlayer = client.getLocalPlayer();

		int lootSplit = raidTracker.getSpecialLootValue() / raidTracker.getTeamSize();

		int cutoff = config.FFACutoff();

		if (raidTracker.getSpecialLoot().length() > 0) {
			if (config.defaultFFA() || lootSplit < cutoff) {
				raidTracker.setFreeForAll(true);
				if (raidTracker.getSpecialLootReceiver().toLowerCase().trim().equals(localPlayer.getName().toLowerCase().trim())) {
					raidTracker.setLootSplitReceived(raidTracker.getSpecialLootValue());
				}
			} else if (raidTracker.getSpecialLootReceiver().toLowerCase().trim().equals(localPlayer.getName().toLowerCase().trim())) {
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

	public void checkChestOpened(WidgetLoaded event, RaidTracker raidTracker)
	{
		if (event.getGroupId() != WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID ||
				raidTracker.isChestOpened()) {
			return;
		}

		raidTracker.setChestOpened(true);
	}

	private void checkRaidPresence()
	{
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		raidTracker.setInRaidChambers(client.getVar(Varbits.IN_RAID) == 1);
	}

	private int stringTimeToSeconds(String s)
	{
		return parseInt(s.split(":")[0]) * 60 + parseInt(s.split(":")[1]);
	}

	private void reset()
	{
		raidTracker = new RaidTracker();
	}
}
