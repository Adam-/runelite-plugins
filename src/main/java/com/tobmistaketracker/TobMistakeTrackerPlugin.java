package com.tobmistaketracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@PluginDescriptor(
	name = "Tob Mistake Tracker"
)
public class TobMistakeTrackerPlugin extends Plugin
{

	static final String CONFIG_GROUP = "tobMistakeTracker";

	private static final int TOB_STATE_NO_PARTY = 0;
	private static final int TOB_STATE_IN_PARTY = 1;
	private static final int TOB_STATE_IN_TOB = 2;

	// Not totally sure where this comes from, but this is the start of the raiders
	private static final int THEATRE_RAIDERS_VARC = 330;

	private static final int MAX_RAIDERS = 5;

	@Inject
	private Client client;

	@Inject
	private TobMistakeTrackerConfig config;

	private int raidState;
	private boolean inTob;
	private boolean isRaider;

	private Map<String, Integer> mistakes;
	private Set<String> raiders;

	@Override
	protected void startUp() throws Exception
	{
		log.info("@@@@@@@@@@@@@ started! @@@@@@@@@@");
		log.info("new test");
		mistakes = new HashMap<>();
		raiders = new HashSet<>(MAX_RAIDERS);
		resetRaidState();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("@@@@@@@@@@@@ stopped! @@@@@@@@@");
	}

	private void resetRaidState() {
		raidState = 0;
		inTob = false;
		isRaider = false;
		raiders.clear();
	}

	@Subscribe
	public void onActorDeath(ActorDeath event) {
		if (!inTob) return;

		Actor actor = event.getActor();
		if (actor instanceof Player) {
			Player player = (Player) actor;
			if (player.getName() == null) {
				return;
			}

			String name = Text.sanitize(player.getName());

			if (raiders.contains(name)) {
				// A Raider has died
				log.info("Death: " + name);
				client.getLocalPlayer().setOverheadText("Death: " + name);
			}
		}

		event.getActor().setOverheadText("Whoopsies I died!");
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		int newRaidState = client.getVar(Varbits.THEATRE_OF_BLOOD);
		if (raidState != newRaidState) {
			if (newRaidState == TOB_STATE_NO_PARTY || newRaidState == TOB_STATE_IN_PARTY) {
				// We're not in a raid
				resetRaidState();
			} else if (isNewRaiderInRaid(newRaidState) || isNewAllowedSpectator(newRaidState)) {
				inTob = true;
				isRaider = isNewRaiderInRaid(newRaidState);
			}
			raidState = newRaidState;

			logState();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (inTob && raiders.isEmpty()) {
			loadRaiders();
			logState();
		}
	}

	private void loadRaiders() {
		for (int i = 0; i < MAX_RAIDERS; i++) {
			String name = Text.sanitize(client.getVarcStrValue(THEATRE_RAIDERS_VARC + i));
			if (name != null && !name.equals("")) {
				raiders.add(name);
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!CONFIG_GROUP.equals(event.getGroup())) {
			return;
		}

		if (raidState == TOB_STATE_IN_TOB) {
			if (config.spectatingEnabled()) {
				inTob = true;
			} else if (inTob && !isRaider) {
				inTob = false;
			}
		}

		logState();

		client.getLocalPlayer().setOverheadText(String.valueOf(config.spectatingEnabled()));
	}

	private boolean isNewRaiderInRaid(int newRaidState) {
		return raidState == TOB_STATE_IN_PARTY && newRaidState == TOB_STATE_IN_TOB;
	}

	private boolean isNewAllowedSpectator(int newRaidState) {
		return newRaidState == TOB_STATE_IN_TOB && config.spectatingEnabled();
	}

	private void logState() {
		log.info("raidState: " + raidState);
		log.info("inTob: " + inTob);
		log.info("isRaider: " + isRaider);

		log.info("raiders: " + raiders);
	}

	@Provides
	TobMistakeTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobMistakeTrackerConfig.class);
	}
}
