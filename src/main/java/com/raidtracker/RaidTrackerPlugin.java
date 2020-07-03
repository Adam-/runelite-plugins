package com.raidtracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.client.game.ItemManager;

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
	private RaidTrackerConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Getter
	private boolean inRaidChambers;

	@Getter
	private int raidPartyID;
	private boolean chestOpened;
	private boolean raidComplete;
	private boolean loggedIn;
	private boolean challengeMode = false;

	private int upperTime = -1;
	private int middleTime = -1;
	private int lowerTime = -1;
	private int raidTime = -1;
	private int totalPoints = -1;
	private int personalPoints = -1;
	private int teamSize = -1;
	private double percentage = -1.0;
	private String specialLoot = "";
	private String specialLootReceiver = "";
	private String kitReceiver = "";
	private String dustReceiver = "";
	private int lootSplit = -1;

	private static final WorldPoint TEMP_LOCATION = new WorldPoint(3360, 5152, 2);

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		inRaidChambers = false;
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
			if (loggedIn
					&& !tempInRaid)
			{
				reset();
			}

			raidPartyID = tempPartyID;
		}

		// if the player's raid state has changed
		if (tempInRaid != inRaidChambers)
		{
			// if the player is inside of a raid then check the raid
			if (tempInRaid && loggedIn)
			{
				checkRaidPresence();
			}

			inRaidChambers = tempInRaid;
		}
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			// skip event while the game decides if the player belongs in a raid or not
			if (client.getLocalPlayer() == null
					|| client.getLocalPlayer().getWorldLocation().equals(TEMP_LOCATION))
			{
				return;
			}
		}
		else if (client.getGameState() == GameState.LOGIN_SCREEN
				|| client.getGameState() == GameState.CONNECTION_LOST)
		{
			loggedIn = false;
		}
		else if (client.getGameState() == GameState.HOPPING)
		{
			reset();
		}
	}


	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (inRaidChambers && event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION) {
			String message = Text.removeTags(event.getMessage());
			if (message.contains(LEVEL_COMPLETE_MESSAGE)) {
				//TODO: save level times
			}

			if (message.startsWith(RAID_COMPLETE_MESSAGE)) {
				totalPoints = client.getVar(Varbits.TOTAL_POINTS);
				personalPoints = client.getVar(Varbits.PERSONAL_POINTS);

				percentage = personalPoints / (totalPoints / 100.0);

				raidComplete = true;
			}

			if (raidComplete && message.startsWith("Team size:"))
			{
				String[] split = message.split(" ");
				if (split[2].length() > 1)
				{
					teamSize = parseInt(split[2]);
				}
				else
				{
					teamSize = 1;
				}
				raidTime = parseInt(split[5].split(":")[0]) * 60 + parseInt(split[5].split(":")[1]);
			}

			//only special loot contain the "-" (except for the raid complete message)
			if (raidComplete && message.contains("-"))
			{
				specialLootReceiver = message.split(" - ")[0];
				specialLoot = message.split(" - ")[1];
				//TODO: get value of special loot
			}

			//not sure if it's possible to get multiples but i'm not gonna bother coding that in
			if (raidComplete && message.startsWith(TWISTED_KIT_RECIPIENTS))
			{
				kitReceiver = message.split(TWISTED_KIT_RECIPIENTS)[1];
			}
			if (raidComplete && message.startsWith(DUST_RECIPIENTS)) {
				dustReceiver = message.split(DUST_RECIPIENTS)[1];
			}

			//challenge mode check
			if (raidComplete && message.contains("Chambers of Xeric Challenge Mode"))
			{
				challengeMode = true;
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (event.getGroupId() != WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID ||
				chestOpened) {
			return;
		}

		chestOpened = true;

		ItemContainer rewardItemContainer = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);
		if (rewardItemContainer == null) {
			return;
		}

		ArrayList<int[]> lootList = new ArrayList<int[]>();

		Arrays.stream(rewardItemContainer.getItems())
				.filter(item -> item.getId() > -1)
				.forEach(item -> {
					lootList.add(new int[] {
							item.getId(),
							item.getQuantity(),
							itemManager.getItemPrice(item.getId()) * item.getQuantity()
					});
				});

		//TODO: save values to logger
	}
	@Provides
	RaidTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidTrackerConfig.class);
	}

	private void checkRaidPresence() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		inRaidChambers = client.getVar(Varbits.IN_RAID) == 1;

		if (!inRaidChambers) {
			return;
		}
	}

	private void reset()
	{
		chestOpened = false;
		raidComplete = false;
		challengeMode = false;
		upperTime = -1;
		middleTime = -1;
		lowerTime = -1;
		raidTime = -1;
		teamSize = -1;
		totalPoints = -1;
		personalPoints = -1;
		percentage = -1.0;
		specialLoot = "";
		specialLootReceiver = "";
		kitReceiver = "";
		dustReceiver = "";
		lootSplit = -1;
	}
}
