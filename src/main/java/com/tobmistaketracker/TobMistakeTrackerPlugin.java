package com.tobmistaketracker;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


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

	@Inject
	private MistakeManager mistakeManager;

	@Inject
	MaidenMistakeDetector maidenMistakeDetector;

	private int raidState;
	private boolean inTob;
	private boolean isRaider;

	private Set<Player> raiders;

	@Override
	protected void startUp() throws Exception
	{
		log.info("@@@@@@@@@@@@@ started! @@@@@@@@@@");
		log.info("new test");
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
        maidenMistakeDetector.init();
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

			if (raiders.contains(player)) {
				// A Raider has died
				log.info("Death: " + name);
				client.getLocalPlayer().setOverheadText("Death: " + name);
				mistakeManager.addMistakeForPlayer(name, TobMistake.DEATH);
			}
		}

		event.getActor().setOverheadText("Whoopsies I died!");
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event) {
		log.info(String.format("onHitsplatApplied: %s", client.getTickCount()));
		if (!inTob) return;

		if (event.getHitsplat().getHitsplatType() == Hitsplat.HitsplatType.HEAL) {
			if (event.getActor().getName() == null) {
				return;
			}

			int amount = event.getHitsplat().getAmount();
			switch (event.getActor().getName()) {
				case TobBossNames.MAIDEN:
					// Healing Maiden:
					// If maiden has a heal hitsplat
					// And player is on a blood
					break;
				default:
					// Hitsplat not applied to a boss
					break;
			}
		}

		log.info("hitsplat actor: " + event.getActor().getName());
		log.info("hitsplat actor interacting: " + event.getActor().getInteracting());
		log.info("hitsplat type: " + event.getHitsplat().getHitsplatType().name());
		log.info("hitsplat amount: " + event.getHitsplat().getAmount());
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
		log.info(String.format("onGameTick: %s", client.getTickCount()));

		if (!inTob) return;

		if (raiders.isEmpty()) {
			loadRaiders();
		}

		maidenMistakeDetector.onGameTick(event);
		for (Player player : raiders) {
			if (player != null) {
			    List<TobMistake> mistakes = maidenMistakeDetector.detectMistakes(player);
			    for (TobMistake mistake : mistakes) {
                    mistakeManager.addMistakeForPlayer(player.getName(), mistake);
                }
            }
		}

        logState();
    }

	private void loadRaiders() {
		Set<String> raiderNames = new HashSet<>(MAX_RAIDERS);
		for (int i = 0; i < MAX_RAIDERS; i++) {
			String name = Text.sanitize(client.getVarcStrValue(THEATRE_RAIDERS_VARC + i));
			if (name != null && !name.isEmpty()) {
				raiderNames.add(name);
			}
		}

		for (Player player : client.getPlayers()) {
			if (raiderNames.contains(player.getName())) {
				raiders.add(player);
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
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event) {
		// For Testing
		if (event.getActor().equals(client.getLocalPlayer())) {
			if (event.getOverheadText().startsWith("Blood")) {
				char id = event.getOverheadText().charAt(event.getOverheadText().length()-1);
				mistakeManager.addMistakeForPlayer("TestPlayer" + id, TobMistake.MAIDEN_BLOOD);
				logState();
			}
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
		maidenMistakeDetector.onGraphicsObjectCreated(event);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event) {
		maidenMistakeDetector.onGameObjectSpawned(event);
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event) {
		maidenMistakeDetector.onGameObjectDespawned(event);
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

		log.info("raiders: " + getRaiderNames());
		log.info("mistakes: " + mistakeManager.mistakesForPlayers);
	}

    private String getRaiderNames() {
        return "[" + raiders.stream().filter(Objects::nonNull).map(Player::getName).collect(Collectors.joining(", ")) + "]";
    }

	@Provides
	TobMistakeTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobMistakeTrackerConfig.class);
	}
}
