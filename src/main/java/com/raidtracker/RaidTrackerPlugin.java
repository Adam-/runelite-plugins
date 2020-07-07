package com.raidtracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import com.google.inject.Inject;
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
import net.runelite.client.util.Text;
import net.runelite.client.game.ItemManager;

import java.io.FileWriter;
import java.io.IOException;
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
	private ItemManager itemManager;

	@Getter
	private int raidPartyID;

	@Inject
	private RaidTracker raidTracker;
	
	private static final WorldPoint TEMP_LOCATION = new WorldPoint(3360, 5152, 2);


	@Override
	protected void startUp() {
		log.info("Raid Tracker Started!");
	}

	@Override
	protected void shutDown() {
		raidTracker.setInRaidChambers(false);
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

	@SuppressWarnings("unused")
	public void onGameStateChanged(GameStateChanged event)
	{
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

	@Inject
	@Subscribe
	public void onChatMessage(ChatMessage event, RaidTracker raidTracker)
	{
		this.raidTracker = raidTracker;
		raidTracker.setLoggedIn(true);
		if (raidTracker.isInRaidChambers() && event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION) {
			String message = Text.removeTags(event.getMessage());
			log.info(message);
			if (message.contains(LEVEL_COMPLETE_MESSAGE)) {
				if (message.startsWith("Upper"))
				{
					raidTracker.setUpperTime(stringTimeToSeconds(message.split(" level complete! Duration: ")[1]));
				}
				if (message.startsWith("Middle"))
				{
					raidTracker.setMiddleTime(stringTimeToSeconds(message.split(" level complete! Duration: ")[1]));
				}
				if (message.startsWith("Lower"))
				{
					raidTracker.setLowerTime(stringTimeToSeconds(message.split(" level complete! Duration: ")[1]));
				}
			}

			if (message.startsWith(RAID_COMPLETE_MESSAGE)) {
				raidTracker.setTotalPoints(client.getVar(Varbits.TOTAL_POINTS));

				raidTracker.setPersonalPoints(client.getVar(Varbits.PERSONAL_POINTS));

				raidTracker.setPercentage(raidTracker.getPersonalPoints() / (raidTracker.getTotalPoints() / 100.0));

				raidTracker.setRaidComplete(true);
				return;
			}


			//this won't run yet (team size wrong syntax?)
			if (raidTracker.isRaidComplete() && message.contains("Team size:"))
			{
				String[] split = message.split(" ");
				if (split[2].length() > 1)
				{
					raidTracker.setTeamSize(parseInt(split[2]));
				}
				else
				{
					raidTracker.setTeamSize(1);
				}
				raidTracker.setRaidTime(stringTimeToSeconds(split[5]));
			}

			//only special loot contain the "-" (except for the raid complete message)
			if (raidTracker.isRaidComplete() && message.contains("-"))
			{
				raidTracker.setSpecialLootReceiver(message.split(" - ")[0]);
				raidTracker.setSpecialLoot(message.split(" - ")[1]);

				raidTracker.setSpecialLootValue(itemManager.search(raidTracker.getSpecialLoot()).get(0).getPrice());

			}

			//not sure if it's possible to get multiples but i'm not gonna bother coding that in
			if (raidTracker.isRaidComplete() && message.startsWith(TWISTED_KIT_RECIPIENTS))
			{
				raidTracker.setKitReceiver(message.split(TWISTED_KIT_RECIPIENTS)[1]);
			}
			if (raidTracker.isRaidComplete() && message.startsWith(DUST_RECIPIENTS)) {
				raidTracker.setDustReceiver(message.split(DUST_RECIPIENTS)[1]);
			}

			//challenge mode check, won't run yet
			if (raidTracker.isRaidComplete() && message.contains("Chambers of Xeric Challenge Mode"))
			{
				raidTracker.setChallengeMode(true);
			}
		}
	}

	@Subscribe
	@SuppressWarnings("ConstantConditions")
	public void onWidgetLoaded(WidgetLoaded event) {
		if (raidTracker.isInRaidChambers()) { //inRaidChambers
			log.info("widget loaded");
		}
		Player localPlayer = client.getLocalPlayer();

		if (event.getGroupId() != WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID ||
				raidTracker.isChestOpened()) {
			return;
		}

		raidTracker.setChestOpened(true);

		ItemContainer rewardItemContainer = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);
		if (rewardItemContainer == null) {
			return;
		}

		ArrayList<RaidTrackerItem> lootList = raidTracker.getLootList();

		Arrays.stream(rewardItemContainer.getItems())
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

		raidTracker.setLootList(lootList);

		//TODO: cutoff for ffa, to be set in config

		//TODO: change each split in sidebar (for example change split loot to ffa) and update it in the json
		raidTracker.setLootSplitReceived(raidTracker.getSpecialLootValue() / raidTracker.getTeamSize());


		if (raidTracker.getSpecialLootReceiver().toLowerCase().trim().equals(localPlayer.getName().toLowerCase().trim()))
		{
			raidTracker.setLootSplitPaid(raidTracker.getSpecialLootValue() - raidTracker.getLootSplitReceived());
		}
		try
		{

			//json format
			String filename= "D:\\Projects\\raid-tracker\\testlogger.json";
			FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			Gson gson = new GsonBuilder().create();

			gson.toJson(raidTracker, fw);

			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		}
		finally {
			reset();
		}
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

		raidTracker.setInRaidChambers(client.getVar(Varbits.IN_RAID) == 1);
	}

	private int stringTimeToSeconds(String s)
	{
		return parseInt(s.split(":")[0]) * 60 + parseInt(s.split(":")[1]);
	}

	private void reset()
	{
		raidTracker = new RaidTracker() {{
			chestOpened = false;
			raidComplete = false;
			loggedIn = false;
			challengeMode = false;
			upperTime = -1;
			middleTime = -1;
			lowerTime = -1;
			raidTime = -1;
			totalPoints = -1;
			personalPoints = -1;
			teamSize = -1;
			percentage = -1.0;
			specialLoot = "";
			specialLootReceiver = "";
			specialLootValue = -1;
			kitReceiver = "";
			dustReceiver = "";
			lootSplitReceived = -1;
			lootSplitPaid = -1;
		}};
	}
}
