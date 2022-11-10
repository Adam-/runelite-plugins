package com.raidtracker;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.raidtracker.filereadwriter.FileReadWriter;
import com.raidtracker.ui.RaidTrackerPanel;
import com.raidtracker.utils.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static net.runelite.client.util.Text.toJagexName;

@Slf4j
@PluginDescriptor(
	name = "Raid Data Tracker"
)

public class RaidTrackerPlugin extends Plugin
{
	public static final String LEVEL_COMPLETE_MESSAGE = "complete! Duration:";
	public static final String RAID_COMPLETE_MESSAGE = "Congratulations - your raid is complete!";
	public static final String DUST_RECIPIENTS = "Dust recipients: ";
	public static final String TWISTED_KIT_RECIPIENTS = "Twisted Kit recipients: ";
	private static final String[] ROOM_COMPLETE_MESSAGE =
	{
			LEVEL_COMPLETE_MESSAGE, // Chambers
			"wave '", // Tob
			"Challenge complete:" // Toa
	};
	private static final String[] SPECIAL_LOOT_MESSAGE =
	{
			" - ", // cox
			" found something special: ", // tob and toa
	};

	private static final int REGION_LOBBY = 13454;
	private static final int WIDGET_PARENT_ID = 481;
	private static final int WIDGET_CHILD_ID = 40;

	private RaidState currentState = new RaidState(false, -1);

	private EventBus eventBus;
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private RaidTrackerConfig config;

	@Inject
	private ConfigManager configManager;
	@Inject
	private ItemManager itemManager;

	@Inject
	private RaidTracker raidTracker;

	@Inject RaidStateTracker tracker;

	private static final WorldPoint TEMP_LOCATION = new WorldPoint(3360, 5152, 2);

	@Setter
	private RaidTrackerPanel panel;
	private NavigationButton navButton;

	@Inject
	public raidUtils RaidUtils;
	@Setter
	private FileReadWriter fw = new FileReadWriter();
	@Setter
	private uiUtils uiUtils = new uiUtils();
	private boolean writerStarted = false;
	public String RTName = "";

	public static String profileKey;
	private RaidTrackerPlugin RaidTrackerPlugin;
	public String getProfileKey(ConfigManager configManager)
	{
		return configManager.getRSProfileKey();
	}
	@Provides
	RaidTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidTrackerConfig.class);
	}

	@Override
	public void startUp() {
		tracker.onPluginStart();
		panel = new RaidTrackerPanel(itemManager, fw, config, clientThread,client);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel-icon.png");
		navButton = NavigationButton.builder()
				.tooltip("Raid Data Tracker")
				.priority(6)
				.icon(icon)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);
	}
	@Override
	protected void shutDown() {
		clientToolbar.removeNavigation(navButton);
		tracker.onPluginStop();
		reset();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (writerStarted) return;

		boolean tempInRaid = tracker.isInRaid();

		if (tempInRaid ^ raidTracker.inRaid)
		{
			if (tempInRaid && raidTracker.isLoggedIn())
			{
				//checkRaidPresence();
			} else if (raidTracker.isRaidComplete() && !raidTracker.isChestOpened())
			{
				fw.writeToFile(raidTracker);

				writerStarted = true;

				SwingUtilities.invokeLater(() -> {
					panel.addDrop(raidTracker);
					reset();
				});
			};
		};

	};

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event)
	{
		if (client.getLocalPlayer().getName() != null && RTName  == "")
		{
			RTName = toJagexName(client.getLocalPlayer().getName());
			fw.updateUsername(getProfileKey(configManager));
			profileKey = getProfileKey(configManager);
			SwingUtilities.invokeLater(() -> panel.loadRTList());
		};
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{

		if (event.getGameState() == GameState.LOGGING_IN) {};
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
		if (raidTracker.isChestOpened() || !raidTracker.isRaidComplete()) return;
		raidTracker.setLoggedIn(true);
		if (writerStarted) return;

		switch (event.getGroupId()) {
			case (WidgetID.TOA_REWARD_GROUP_ID) :
				raidTracker.setChestOpened(true);

				ItemContainer toaChestContainer = client.getItemContainer(InventoryID.TOA_REWARD_CHEST);

				if (toaChestContainer == null) return;


				raidTracker.setLootList((lootListFactory(toaChestContainer.getItems())));
				raidTracker.inRaidType = 2;
				fw.writeToFile(raidTracker);

				writerStarted = true;
				SwingUtilities.invokeLater(() -> {
					panel.addDrop(raidTracker);
					reset();
				});
				break;
			case (WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID):


				raidTracker.setChestOpened(true);

				ItemContainer rewardItemContainer = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);

				if (rewardItemContainer == null) {
					return;
				}


				raidTracker.setLootList(lootListFactory(rewardItemContainer.getItems()));
				raidTracker.inRaidType = 0;
				fw.writeToFile(raidTracker);

				writerStarted = true;

				SwingUtilities.invokeLater(() -> {
					panel.addDrop(raidTracker);
					reset();
				});

				break;

			case (WidgetID.THEATRE_OF_BLOOD_GROUP_ID):


				raidTracker.setChestOpened(true);

				rewardItemContainer = client.getItemContainer(InventoryID.THEATRE_OF_BLOOD_CHEST);

				if (rewardItemContainer == null) {
					return;
				}

				raidTracker.setLootList(lootListFactory(rewardItemContainer.getItems()));
				raidTracker.inRaidType = 1;
				fw.writeToFile(raidTracker);

				writerStarted = true;

				SwingUtilities.invokeLater(() -> {
					panel.addDrop(raidTracker);
					reset();
				});

				break;
			//459 is the mvp screen of TOB
			case (459):
				AtomicReference<String> mvp = new AtomicReference<>("");
				AtomicReference<String>[] tobPlayers;
				AtomicInteger[] tobDeaths = {new AtomicInteger(), new AtomicInteger(), new AtomicInteger(), new AtomicInteger()};

				String[] players;

				clientThread.invokeLater(() -> {

					raidTracker.tobPlayers = new String[]{
							getWidgetText(client.getWidget(459, 22)),
							getWidgetText(client.getWidget(459, 24)),
							getWidgetText(client.getWidget(459, 26)),
							getWidgetText(client.getWidget(459, 28)),
							getWidgetText(client.getWidget(459, 30))
					};

					raidTracker.tobDeaths = new int[]{
							getWidgetNumber(client.getWidget(459, 23)),
							getWidgetNumber(client.getWidget(459, 25)),
							getWidgetNumber(client.getWidget(459, 27)),
							getWidgetNumber(client.getWidget(459, 29)),
							getWidgetNumber(client.getWidget(459, 31))
					};

					raidTracker.setMvp(getWidgetText(client.getWidget(459, 14)));

					if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
						raidTracker.setMvpInOwnName(getWidgetText(client.getWidget(459, 14)).equalsIgnoreCase(client.getLocalPlayer().getName()));
					};
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
		String playerName = "";
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
			playerName = client.getLocalPlayer().getName();
		}

		if (tracker.isInRaid() && (event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION || event.getType() == ChatMessageType.GAMEMESSAGE))
		{
			//unescape java to avoid unicode
			String message = unescapeJavaString(Text.removeTags(event.getMessage()));
			String[] pets = {"olmlet", "lil' zik", "tumeken's guardian"};
			boolean hasPet = Utils.containsCaseInsensitive(message, pets);
			if (raidTracker.isRaidComplete() && (hasPet || message.toLowerCase().contains("would have been followed")))
			{
				RaidUtils.parsePets(message, raidTracker, tracker);
				return;
			};
			if (Utils.containsCaseInsensitive(Arrays.asList(ROOM_COMPLETE_MESSAGE), message))
			{
				RaidUtils.parseRaidTime(message, raidTracker, tracker);
				return;
			};

			if (
					raidTracker.isRaidComplete() && // if the raid is complete
					Utils.containsCaseInsensitive(Arrays.asList(SPECIAL_LOOT_MESSAGE), message) && // and it comtains special loot message
					!Utils.containsCaseInsensitive(Arrays.asList(ROOM_COMPLETE_MESSAGE), message) // and it doesn't contain raid completed message
			)
			{
				if (Utils.containsCaseInsensitive(Arrays.asList(SPECIAL_LOOT_MESSAGE), message))
				{
					String[] tobUntradables = {"Sanguine Ornement Kit", "Holy Ornement Kit", "Sanguine Dust"};
					if (Utils.containsCaseInsensitive(Arrays.asList(tobUntradables), message))
					{
						RaidUtils.parseUntradables(message, raidTracker, tracker);
						return;
					};
				};
				RaidUtils.parseRaidUniques(message, raidTracker, tracker);
				return;
			};

			if (raidTracker.isRaidComplete() && (message.startsWith(TWISTED_KIT_RECIPIENTS) || (raidTracker.isRaidComplete() && message.startsWith(DUST_RECIPIENTS))))
			{
				RaidUtils.parseUntradables(message, raidTracker, tracker);
				return;
			};

			if (raidTracker.isRaidComplete() && message.contains("Team size:"))
			{
				raidTracker.setRaidTime(stringTimeToSeconds(message.split("Duration: ")[1].split(" ")[0]));
				return;
			}

			//works for tob
			if (message.contains("count is:")) {
				raidTracker.setChallengeMode(message.contains("Chambers of Xeric Challenge Mode"));
				raidTracker.setCompletionCount(parseInt(message.split("count is:")[1].trim().replace(".", "")));
				System.out.println(tracker);
				if (tracker.getCurrentState().getRaidType() == 0)
				{
					raidTracker.setTotalPoints(client.getVarbitValue(Varbits.TOTAL_POINTS));
					raidTracker.setPersonalPoints(client.getVarbitValue(Varbits.PERSONAL_POINTS));
					raidTracker.setPercentage(raidTracker.getPersonalPoints() / (raidTracker.getTotalPoints() / 100.0));
					raidTracker.setTeamSize(client.getVarbitValue(Varbits.RAID_PARTY_SIZE));
					raidTracker.setRaidComplete(true);
					raidTracker.setDate(System.currentTimeMillis());
				};

				if (tracker.getCurrentState().getRaidType() == 2)
				{
					int[] playerVarbits = {
							Varbits.TOA_MEMBER_0_HEALTH, Varbits.TOA_MEMBER_1_HEALTH, Varbits.TOA_MEMBER_2_HEALTH,
							Varbits.TOA_MEMBER_3_HEALTH, Varbits.TOA_MEMBER_4_HEALTH, Varbits.TOA_MEMBER_5_HEALTH,
							Varbits.TOA_MEMBER_6_HEALTH, Varbits.TOA_MEMBER_7_HEALTH

					};

					raidTracker.setTeamSize(Arrays.stream(playerVarbits).filter(vb -> (client.getVarbitValue(vb) > 0)).toArray().length);
					raidTracker.setRaidComplete(true);
					raidTracker.setDate(System.currentTimeMillis());
					raidTracker.setInvocation(client.getVarbitValue(Varbits.TOA_RAID_LEVEL));
				};


				if (tracker.getCurrentState().getRaidType() == 1)
				{
					int teamSize = 0;

					for (int i = 6442; i  < 6447; i++) {
						if (client.getVarbitValue(i) != 0) {
							teamSize++;
						}
					}
					raidTracker.setTeamSize(teamSize);
					raidTracker.setRaidComplete(true);
				}
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
					lootList.add(new RaidTrackerItem(comp.getName(), comp.getId(), item.getQuantity(),comp.getPrice() * item.getQuantity()));
				});
		return lootList;
	}

	public static int stringTimeToSeconds(String s)
	{
		String[] split = s.split(":");
		return split.length == 3 ? parseInt(split[0]) * 3600 + parseInt(split[1]) * 60 + Math.round(parseFloat(split[2])) : parseInt(split[0]) * 60 + Math.round(parseFloat(split[1]));
	}

	private void reset()
	{
		raidTracker = new RaidTracker();
		writerStarted = false;
	}

	//from stackoverflow
	public String unescapeJavaString(String st) {return uiUtils.unescapeJavaString(st);}
}
