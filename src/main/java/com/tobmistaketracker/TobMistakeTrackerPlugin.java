package com.tobmistaketracker;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
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
public class TobMistakeTrackerPlugin extends Plugin {

	static final String CONFIG_GROUP = "tobMistakeTracker";
	static final String CLEAR_MISTAKES_KEY = "clearMistakes";

	private static final int TOB_STATE_NO_PARTY = 0;
	private static final int TOB_STATE_IN_PARTY = 1;
	private static final int TOB_STATE_IN_TOB = 2;

	// Not totally sure where this comes from, but this is the start of the raiders
	private static final int THEATRE_RAIDERS_VARC = 330;

	private static final int MAX_RAIDERS = 5;

	@Inject
	Client client;

	@Inject
	OverlayManager overlayManager;

	@Inject
	TobMistakeTrackerConfig config;

	@Inject
	MistakeManager mistakeManager;

	private MistakeDetectorManager mistakeDetectorManager;

	private int raidState;
	private boolean inTob;
	private boolean isRaider;
	private boolean allRaidersLoaded;

	@Getter
	private Set<TobRaider> raiders;

	@Override
	protected void startUp() throws Exception
	{
		log.info("@@@@@@@@@@@@@ started! @@@@@@@@@@");
		resetRaidState();

		mistakeDetectorManager = new MistakeDetectorManager(this);
		installMistakeDetectors();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("@@@@@@@@@@@@ stopped! @@@@@@@@@");
		raiders.clear();
		mistakeDetectorManager.cleanup();
		mistakeDetectorManager = null;
	}

	private void resetRaidState() {
		raidState = 0;
		inTob = false;
		isRaider = false;
		allRaidersLoaded = false;

		raiders = new HashSet<>(MAX_RAIDERS);
	}

	private void installMistakeDetectors() throws Exception {
		// TODO: We don't need certain detectors installed all the time (e.g. Bloat detector during Maiden)
		mistakeDetectorManager.installMistakeDetector(MaidenMistakeDetector.class);
	}

	@Subscribe
	public void onActorDeath(ActorDeath event) {
		if (notReadyToDetectMistakes()) return;

		Actor actor = event.getActor();
		if (actor instanceof TobRaider) {
			TobRaider raider = (TobRaider) actor;
			if (raider.getName() == null) {
				return;
			}

			String name = Text.sanitize(raider.getName());

			if (raiders.contains(raider)) {
				// A Raider has died
				log.info("Death: " + name);
				client.getLocalPlayer().setOverheadText("Death: " + name);
                addMistakeForPlayer(name, TobMistake.DEATH);
			}
		}

		event.getActor().setOverheadText("Whoopsies I died!");
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event) {
		if (notReadyToDetectMistakes()) return;

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
//
//		log.info("hitsplat actor: " + event.getActor().getName());
//		log.info("hitsplat actor interacting: " + event.getActor().getInteracting());
//		log.info("hitsplat type: " + event.getHitsplat().getHitsplatType().name());
//		log.info("hitsplat amount: " + event.getHitsplat().getAmount());
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
//		log.info(String.format("onGameTick: %s", client.getTickCount()));
		client.getLocalPlayer().setOverheadText("" + client.getTickCount());

		if (!inTob) return;

		if (!allRaidersLoaded) {
			loadRaiders();
		}

		if (notReadyToDetectMistakes()) return;

		mistakeDetectorManager.onEvent("onGameTick", event);

		for (TobRaider raider : raiders) {
			if (raider != null) {
				List<TobMistake> mistakes = mistakeDetectorManager.detectMistakes(raider);
				if (!mistakes.isEmpty()) {
					log.info("FOUND MISTAKES FOR " + raider.getName() + " - " + mistakes);

					for (TobMistake mistake : mistakes) {
						int mistakeCount = addMistakeForPlayer(raider.getName(), mistake);
						raider.setOverheadText("" + client.getTickCount() + " - BLOOD " + mistakeCount);
					}
					logState();
				}

				raider.setPreviousWorldLocation(raider.getCurrentWorldLocation());
			}
		}
    }

	private void loadRaiders() {
		Set<String> raiderNames = new HashSet<>(MAX_RAIDERS);
		for (int i = 0; i < MAX_RAIDERS; i++) {
			String name = Text.sanitize(client.getVarcStrValue(THEATRE_RAIDERS_VARC + i));
			if (name != null && !name.isEmpty()) {
				raiderNames.add(name);
			}
		}

		Set<TobRaider> raidersTemp = new HashSet<>(MAX_RAIDERS);
		for (Player player : client.getPlayers()) {
			if (raiderNames.contains(player.getName())) {
				raidersTemp.add(new TobRaider(player));
			}
		}

		if (!raiderNames.isEmpty() && raiderNames.size() == raidersTemp.size()) {
			raiders = raidersTemp;
			allRaidersLoaded = true;
		}
	}

	private boolean notReadyToDetectMistakes() {
		return !inTob || !allRaidersLoaded;
	}

	private boolean shouldTrackMistakes() {
	    // Currently the only reason not to track a mistake is if spectating is turned off
        return isRaider || config.spectatingEnabled();
    }

    private int addMistakeForPlayer(String playerName, TobMistake mistake) {
	    if (shouldTrackMistakes()) {
            return mistakeManager.addMistakeForPlayer(playerName, mistake);
        }

	    return 0;
    }

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!CONFIG_GROUP.equals(event.getGroup())) {
			return;
		}

		// I think this is handled by shouldTrackMistakes()
//		if (raidState == TOB_STATE_IN_TOB) {
//			if (config.spectatingEnabled()) {
//				inTob = true;
//			} else if (inTob && !isRaider) {
//				inTob = false;
//			}
//		}

		if (CLEAR_MISTAKES_KEY.equals(event.getKey())) {
			mistakeManager.clearAllMistakes();
		}

		logState();
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event) {
		// For Testing
		if (event.getActor().equals(client.getLocalPlayer())) {
			if (event.getOverheadText().startsWith("Blood")) {
				char id = event.getOverheadText().charAt(event.getOverheadText().length()-1);
                addMistakeForPlayer("TestPlayer" + id, TobMistake.MAIDEN_BLOOD);
				logState();
			}
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
		if (notReadyToDetectMistakes()) return;

		mistakeDetectorManager.onEvent("onGraphicsObjectCreated", event);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event) {
		if (notReadyToDetectMistakes()) return;

		mistakeDetectorManager.onEvent("onGameObjectSpawned", event);
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event) {
		if (notReadyToDetectMistakes()) return;

		mistakeDetectorManager.onEvent("onGameObjectDespawned", event);
	}

	private boolean isNewRaiderInRaid(int newRaidState) {
		return raidState == TOB_STATE_IN_PARTY && newRaidState == TOB_STATE_IN_TOB;
	}

	private boolean isNewAllowedSpectator(int newRaidState) {
		return newRaidState == TOB_STATE_IN_TOB && config.spectatingEnabled();
	}

	private void logState() {
//		log.info("raidState: " + raidState);
//		log.info("inTob: " + inTob);
//		log.info("isRaider: " + isRaider);

//		log.info("raiders: " + getRaiderNames());
		log.info("mistakes: " + mistakeManager.mistakesForPlayers + " - " + client.getTickCount());
	}

    private String getRaiderNames() {
        return "[" + raiders.stream().filter(Objects::nonNull).map(TobRaider::getName).collect(Collectors.joining(", ")) + "]";
    }

	@Provides
	TobMistakeTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TobMistakeTrackerConfig.class);
	}
}
