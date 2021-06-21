package com.tobmistaketracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import com.tobmistaketracker.detector.MistakeDetectorManager;
import com.tobmistaketracker.overlay.DebugOverlay;
import com.tobmistaketracker.overlay.DebugOverlayPanel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
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

    private static final int THEATRE_RAIDERS_VARC = 330;

    private static final int MAX_RAIDERS = 5;

    private static final Pattern STORY_MODE_FAILED_PATTERN = Pattern.compile("You have failed.");

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private TobMistakeTrackerConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DebugOverlay debugOverlay;

    @Inject
    private DebugOverlayPanel debugOverlayPanel;

    @Inject
    private EventBus eventBus;

    @Inject
    private MistakeManager mistakeManager;

    @Inject
    private MistakeDetectorManager mistakeDetectorManager;

    private int raidState;
    @Getter
    @VisibleForTesting
    private boolean inTob;
    private boolean isRaider;
    @Getter
    @VisibleForTesting
    private boolean allRaidersLoaded;

    private String[] raiderNames;
    private Map<String, TobRaider> raiders; // name -> raider

    @Override
    protected void startUp() throws Exception {
        resetRaidState();
        clientThread.invokeLater(() -> {
            computeInTob();
            if (inTob) {
                tryLoadRaiders();
            }
        });

        overlayManager.add(debugOverlay);
        overlayManager.add(debugOverlayPanel);
    }

    @Override
    protected void shutDown() throws Exception {
        resetRaidState();

        overlayManager.remove(debugOverlay);
        overlayManager.remove(debugOverlayPanel);
    }

    private void resetRaidState() {
        raidState = TOB_STATE_NO_PARTY;
        inTob = false;
        isRaider = false;
        allRaidersLoaded = false;

        raiderNames = new String[MAX_RAIDERS];
        raiders = new HashMap<>(MAX_RAIDERS);

        mistakeDetectorManager.shutdown();
    }

    // This should run *after* all detectors have handled the GameTick.
    @Subscribe(priority = -1)
    public void onGameTick(GameTick event) {
        client.getLocalPlayer().setOverheadText("" + client.getTickCount());

        if (!inTob) return;

        if (!allRaidersLoaded) {
            tryLoadRaiders();
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
            log.debug("" + client.getTickCount() + " Found mistakes for " + raider.getName() + " - " + mistakes);

            for (TobMistake mistake : mistakes) {
                // Handle special logic for deaths
                if (mistake == TobMistake.DEATH) {
                    raider.setDead(true);
                }

                int mistakeCount = addMistakeForPlayer(raider.getName(), mistake);

                // TODO: Have this timeout after 5 ticks
                raider.setOverheadText(mistake.getChatMessage());
                client.addChatMessage(ChatMessageType.PUBLICCHAT, raider.getName(), mistake.getChatMessage(), null);
                if (config.isDebug()) {
                    raider.setOverheadText(
                            String.format("%s - %s %s", client.getTickCount(), mistake.getMistakeName(), mistakeCount));
                }
            }
        }

        afterDetect(raider);
    }

    private void afterDetect(TobRaider raider) {
        raider.setPreviousWorldLocationForOverlay(raider.getPreviousWorldLocation());
        raider.setPreviousWorldLocation(raider.getCurrentWorldLocation());
    }

    private void afterDetectAll() {
        mistakeDetectorManager.afterDetect();
    }

    private void tryLoadRaiders() {
        // Look through all players and see if they should be a raider
        Set<String> raiderNamesSet = new HashSet<>(getRaiderNames());
        if (raiderNamesSet.isEmpty()) {
            // Let's try loading raider names manually, since we might already be in a raid and thus
            // onVarClientStrChanged will never get invoked
            tryLoadRaiderNames();
            raiderNamesSet = new HashSet<>(getRaiderNames());
        }

        for (Player player : client.getPlayers()) {
            if (player != null && player.getName() != null &&
                    !raiders.containsKey(player.getName()) && raiderNamesSet.contains(player.getName())) {
                raiders.put(player.getName(), new TobRaider(player));
            }
        }

        int totalRaiders = raiderNamesSet.size();
        if (totalRaiders > 0 && raiders.size() == totalRaiders) {
            allRaidersLoaded = true;

        }
    }

    private void tryLoadRaiderNames() {
        for (int i = 0; i < MAX_RAIDERS; i++) {
            String playerName = client.getVarcStrValue(THEATRE_RAIDERS_VARC + i);

            if (playerName != null && !playerName.isEmpty()) {
                raiderNames[i] = (Text.sanitize(playerName));
            }
        }
    }

    private boolean shouldTrackMistakes() {
        return inTob;
    }

    private int addMistakeForPlayer(String playerName, TobMistake mistake) {
        if (shouldTrackMistakes()) {
            return mistakeManager.addMistakeForPlayer(playerName, mistake);
        }

        return 0;
    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned event) {
        // We only care about players that despawn when in a raid.
        if (inTob && raiders.containsKey(event.getPlayer().getName())) {
            raiders.remove(event.getPlayer().getName());
            allRaidersLoaded = false;
        }
    }

    @Subscribe
    public void onVarClientStrChanged(VarClientStrChanged event) {
        if (event.getIndex() >= THEATRE_RAIDERS_VARC && event.getIndex() < THEATRE_RAIDERS_VARC + MAX_RAIDERS) {
            // A raider has joined or left -- reset allRaidersLoaded
            allRaidersLoaded = false;

            int raiderIndex = event.getIndex() - THEATRE_RAIDERS_VARC;
            String raiderName = client.getVarcStrValue(event.getIndex());
            if (raiderName != null && !raiderName.isEmpty()) {
                raiderNames[raiderIndex] = Text.sanitize(raiderName);
            } else {
                raiderNames[raiderIndex] = null;
            }
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        event.getActor().setOverheadText("Whoopsies I died!");
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        computeInTob();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!CONFIG_GROUP.equals(event.getGroup())) {
            return;
        }

        if (CLEAR_MISTAKES_KEY.equals(event.getKey())) {
            mistakeManager.clearAllMistakes();
        }
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        // For Testing
        if (event.getActor().equals(client.getLocalPlayer())) {
            if (event.getOverheadText().startsWith("Test blood")) {
                char id = event.getOverheadText().charAt(event.getOverheadText().length() - 1);
                addMistakeForPlayer("TestPlayer" + id, TobMistake.MAIDEN_BLOOD);
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOADING) {
            // If there are still raiders, they can't be dead anymore after loading.
            for (TobRaider raider : raiders.values()) {
                raider.setDead(false);
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getMessageNode().getType() == ChatMessageType.GAMEMESSAGE) {
            if (STORY_MODE_FAILED_PATTERN.matcher(event.getMessage()).find()) {
                // Failed a story mode attempt, all raiders are no longer dead.
                for (TobRaider raider : raiders.values()) {
                    raider.setDead(false);
                }
            }
        }
    }

    private boolean isNewRaiderInRaid(int newRaidState) {
        return raidState == TOB_STATE_IN_PARTY && newRaidState == TOB_STATE_IN_TOB;
    }

    private boolean isNewAllowedSpectator(int newRaidState) {
        return newRaidState == TOB_STATE_IN_TOB;
    }

    private void computeInTob() {
        if (client.getGameState() != GameState.LOGGED_IN) return;

        int newRaidState = client.getVar(Varbits.THEATRE_OF_BLOOD);
        if (raidState != newRaidState) {
            if (newRaidState == TOB_STATE_NO_PARTY || newRaidState == TOB_STATE_IN_PARTY) {
                // We're not in a raid
                resetRaidState();
            } else if (newRaidState == TOB_STATE_IN_TOB) {
                inTob = true;
                mistakeDetectorManager.startup();
            }
            raidState = newRaidState;
        }
    }

    public boolean isLoadedRaider(String playerName) {
        return playerName != null && raiders.containsKey(playerName);
    }

    public Iterable<TobRaider> getRaiders() {
        return Collections.unmodifiableCollection(raiders.values());
    }

    /**
     * Gets copy of all the raider names. Elements returned are guaranteed to be non-null.
     */
    public List<String> getRaiderNames() {
        return Arrays.stream(raiderNames).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Provides
    TobMistakeTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TobMistakeTrackerConfig.class);
    }
}
