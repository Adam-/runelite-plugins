package com.raidtracker;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Provides;
import com.google.inject.Inject;
import jdk.nashorn.internal.parser.JSONParser;
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
import net.runelite.http.api.RuneLiteAPI;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

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

			if (raidTracker.isRaidComplete() && message.contains("Team size:"))
			{
				String[] split = message.split(" ");
				if (StringUtils.isNumeric(split[2]))
				{
					raidTracker.setTeamSize(parseInt(split[2]));
				}
				else
				{
					raidTracker.setTeamSize(1);
				}
				raidTracker.setRaidTime(stringTimeToSeconds(split[4]));
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
			if (raidTracker.isRaidComplete() && message.contains("count is:"))
			{
				raidTracker.setChallengeMode(message.contains("Chambers of Xeric Challenge Mode"));
				raidTracker.setCompletionCount(parseInt(message.split("count is:")[1].trim()));
			}
		}
	}


	@Subscribe
	@SuppressWarnings("ConstantConditions")
	public void onWidgetLoaded(WidgetLoaded event) {
		Player localPlayer = client.getLocalPlayer();

		checkChestOpened(event, raidTracker);

		if (event.getGroupId() != WidgetID.CHAMBERS_OF_XERIC_REWARD_GROUP_ID ||
				raidTracker.isChestOpened()) {
			return;
		}

		ItemContainer rewardItemContainer = client.getItemContainer(InventoryID.CHAMBERS_OF_XERIC_CHEST);

		if (rewardItemContainer == null) {
			return;
		}

		raidTracker.setLootList(lootListFactory(rewardItemContainer.getItems()));

		//TODO: cutoff for ffa, to be set in config

		//TODO: change each split in sidebar (for example change split loot to ffa) and update it in the json


		raidTracker.setLootSplitReceived(raidTracker.getSpecialLootValue() / raidTracker.getTeamSize());

		if (raidTracker.getSpecialLootReceiver().toLowerCase().trim().equals(localPlayer.getName().toLowerCase().trim()))
		{
			raidTracker.setLootSplitPaid(raidTracker.getSpecialLootValue() - raidTracker.getLootSplitReceived());
		}

		writeToFile(raidTracker);

		reset();
	}
	@Provides
	RaidTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RaidTrackerConfig.class);
	}

	public void writeToFile(RaidTracker raidTracker)
	{
		//------------- initialization of folders -----------------------

		File dir = new File(RUNELITE_DIR, "loots");
		dir.mkdir();
		dir = new File(dir, client.getUsername());
		dir.mkdir();
		dir = new File(dir, "cox");
		dir.mkdir();

//		--------------- actual writing to the file ----------------------
		try
		{
			//use json format so serializing and deserializing is easy
			Gson gson = new GsonBuilder().create();

			JsonParser parser = new JsonParser();

			String fileName = dir.getAbsolutePath() + "\\raid_tracker_data.log";

			FileWriter fw = new FileWriter(fileName,true); //the true will append the new data

			gson.toJson(parser.parse(getJSONString(raidTracker, gson, parser)), fw);

			fw.append(",\n");

			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public String getJSONString(RaidTracker raidTracker, Gson gson, JsonParser parser)
	{
		JsonObject RTJson =  parser.parse(gson.toJson(raidTracker)).getAsJsonObject();


		List<RaidTrackerItem> lootList = raidTracker.getLootList();

		//------------------ temporary fix until i can get gson.tojson to work for arraylist<RaidTrackerItem> ---------
		JsonArray lootListToString = new JsonArray();


		for (int i = 0; i < lootList.size(); i++)
		{
			lootListToString.add(parser.parse(gson.toJson(lootList.get(i), new TypeToken<RaidTrackerItem>(){}.getType())));
		}

		RTJson.addProperty("lootList", lootListToString.toString());

		//-------------------------------------------------------------------------------------------------------------

//		System.out.println(
//				gson.toJson(lootList, new TypeToken<List<RaidTrackerItem>>(){}.getType())); //[null], raidtrackerplugin is added to the list of types, which is automatically set to skipserialize true -> null return;



		//massive bodge, works for now
		return RTJson.toString().replace("\\\"", "\"").replace("\"[", "[").replace("]\"", "]");
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
		if (raidTracker.isInRaidChambers()) { //inRaidChambers
			log.info("widget loaded");
		}

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
