package com.tobmistaketracker;

import com.google.inject.Provides;
import com.tobmistaketracker.detector.MaidenMistakeDetector;
import com.tobmistaketracker.detector.MistakeDetectorManager;
import com.tobmistaketracker.detector.TobMistakeDetector;
import com.tobmistaketracker.overlay.DebugOverlay;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private Client client;

    @Inject
    private TobMistakeTrackerConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DebugOverlay debugOverlay;

    @Inject
    private EventBus eventBus;

    @Inject
    private MistakeManager mistakeManager;

    @Inject
    private MistakeDetectorManager mistakeDetectorManager;

    private int raidState;
    private boolean inTob;
    private boolean isRaider;
    private boolean allRaidersLoaded;

    @Getter
    private Map<String, TobRaider> raiders; // name -> raider

    @Override
    protected void startUp() throws Exception {
        resetRaidState();

        overlayManager.add(debugOverlay);
        mistakeDetectorManager.registerToEventBus(eventBus);
    }

    @Override
    protected void shutDown() throws Exception {
        raiders.clear();
        mistakeDetectorManager.shutdown();

        overlayManager.remove(debugOverlay);
        mistakeDetectorManager.unregisterFromEventBus(eventBus);
    }

    private void resetRaidState() {
        raidState = 0;
        inTob = false;
        isRaider = false;
        allRaidersLoaded = false;

        raiders = new HashMap<>(MAX_RAIDERS);
        mistakeDetectorManager.reset();
        mistakeDetectorManager.logRunningDetectors();
    }

    // This should run *after* all detectors have handled the GameTick.
    @Subscribe(priority = -1)
    public void onGameTick(GameTick event) {
        client.getLocalPlayer().setOverheadText("" + client.getTickCount());

        if (!inTob) return;

        if (!allRaidersLoaded) {
            loadRaiders();
        }

        // Try detecting all possible mistakes for this GameTick
        detectAll();

        // Invoke post-processing method for detectors to get ready for the next GameTick
        afterDetectAll();
    }

    private void detectAll() {
        for (TobRaider raider : raiders.values()) {
            if (raider != null) {
                detect(raider);
            }
        }
    }

    private void detect(@NonNull TobRaider raider) {
        List<TobMistake> mistakes = mistakeDetectorManager.detectMistakes(raider);
        if (!mistakes.isEmpty()) {
            log.info("" + client.getTickCount() + " Found mistakes for " + raider.getName() + " - " + mistakes);

            for (TobMistake mistake : mistakes) {
                int mistakeCount = addMistakeForPlayer(raider.getName(), mistake);
                raider.setOverheadText("" + client.getTickCount() + " - BLOOD " + mistakeCount);
            }

            logState();
        }

        afterDetect(raider);
    }

    private void afterDetect(TobRaider raider) {
        raider.setPreviousWorldLocation(raider.getCurrentWorldLocation());
        raider.setPreviousIsDead(raider.isDead());
    }

    private void afterDetectAll() {
        mistakeDetectorManager.afterDetect();
    }

    private void loadRaiders() {
        Set<String> raiderNames = new HashSet<>(MAX_RAIDERS);
        for (int i = 0; i < MAX_RAIDERS; i++) {
            String name = Text.sanitize(client.getVarcStrValue(THEATRE_RAIDERS_VARC + i));
            if (name != null && !name.isEmpty()) {
                raiderNames.add(name);
            }
        }

        Map<String, TobRaider> raidersTemp = new HashMap<>(MAX_RAIDERS);
        for (Player player : client.getPlayers()) {
            if (raiderNames.contains(player.getName())) {
                raidersTemp.put(player.getName(), new TobRaider(player));
            }
        }

        if (!raiderNames.isEmpty() && raiderNames.size() == raidersTemp.size()) {
            raiders = raidersTemp;
            allRaidersLoaded();
        }
    }

    private void allRaidersLoaded() {
        allRaidersLoaded = true;
        mistakeDetectorManager.startup();
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
    public void onActorDeath(ActorDeath event) {
        Actor actor = event.getActor();
        if (actor instanceof Player) {
            Player player = (Player) actor;
            if (player.getName() == null) {
                return;
            }

            if (isPlayerInRaid(player)) {
                // A Raider has died
                TobRaider raider = raiders.get(player.getName());
                log.info("Death: " + raider.getName());
                addMistakeForPlayer(raider.getName(), TobMistake.DEATH);
                raider.setDead(true); // TODO: Set this to false during next wave/room, when I get to that logic.
                // TODO: Probably on next room start actually, since Story mode is a thing...
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
    public void onConfigChanged(ConfigChanged event) {
        if (!CONFIG_GROUP.equals(event.getGroup())) {
            return;
        }

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
                char id = event.getOverheadText().charAt(event.getOverheadText().length() - 1);
                addMistakeForPlayer("TestPlayer" + id, TobMistake.MAIDEN_BLOOD);
                logState();
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        // TODO
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

        log.info("raiders: " + raiders.keySet());
        log.info("mistakes: " + mistakeManager.mistakesForPlayers + " - " + client.getTickCount());
        mistakeDetectorManager.logRunningDetectors();
    }

    public List<WorldPoint> getRaiderPreviousWorldLocations() {
        return raiders.values().stream()
                .map(TobRaider::getPreviousWorldLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isPlayerInRaid(Player player) {
        return raiders.containsKey(player.getName());
    }

    @Provides
    TobMistakeTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TobMistakeTrackerConfig.class);
    }

    @Provides
    List<TobMistakeDetector> provideMistakeDetectors(MaidenMistakeDetector maidenMistakeDetector) {
        List<TobMistakeDetector> mistakeDetectors = new ArrayList<>();

        mistakeDetectors.add(maidenMistakeDetector);

        return mistakeDetectors;
    }
}
