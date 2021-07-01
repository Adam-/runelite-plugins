package com.tobmistaketracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import com.tobmistaketracker.detector.MistakeDetectorManager;
import com.tobmistaketracker.panel.TobMistakeTrackerPanel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
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

import static net.runelite.api.widgets.WidgetID.TOB_GROUP_ID;

@Singleton
@Slf4j
@PluginDescriptor(
        name = "Tob Mistake Tracker"
)
public class TobMistakeTrackerPlugin extends Plugin {

    static final String CONFIG_GROUP = "tobMistakeTracker";

    private static final int TOB_BOSS_INTERFACE_ID = 1;
    private static final int TOB_BOSS_INTERFACE_TEXT_ID = 2;

    // These are some of the possible values for Varbits.THEATRE_OF_BLOOD
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
    private ClientToolbar clientToolbar;

    @Inject
    private EventBus eventBus;

    @Inject
    private MistakeDetectorManager mistakeDetectorManager;

    @Inject
    private TobMistakeChatMessageManager chatMessageManager;

    private final BufferedImage icon = ImageUtil.loadImageResource(TobMistakeTrackerPlugin.class, "panel_icon.png");
    private TobMistakeTrackerPanel panel;
    private NavigationButton navButton;

    private int raidState;
    @Getter
    @VisibleForTesting
    private boolean inTob;
    @Getter
    @VisibleForTesting
    private boolean allRaidersLoaded;

    @Getter
    @VisibleForTesting
    // This is in charge of determining if we need to reset the current raid panel. We need this boolean here due to
    // disconnects/turning off and on the plugin mid-raid. This let's us know that we might need to reset the panel
    // the next time that we transition back into a raid and the boss is Maiden.
    // We set this to true any time we reset the raid state, and to false when we determine that we've entered a brand
    // new raid.
    private boolean panelMightNeedReset;

    private String[] raiderNames;
    private Map<String, TobRaider> raiders; // name -> raider

    @Override
    protected void startUp() throws Exception {
        // Can't @Inject because we null it out in shutdown()
        panel = injector.getInstance(TobMistakeTrackerPanel.class);

        // Add panel and icon
        panel.loadHeaderIcon(icon);
        navButton = NavigationButton.builder()
                .tooltip("Tob Mistake Tracker")
                .icon(icon)
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);

        // Let the ChatMessageManager handle events
        eventBus.register(chatMessageManager);

        // Reset all state
        resetRaidState();

        // If the plugin was turned on mid-raid, try to load the right state
        clientThread.invokeLater(() -> {
            computeInTob();
            if (inTob) {
                tryLoadRaiders();
            }
        });

        panel.reload();
    }

    @Override
    protected void shutDown() throws Exception {
        resetRaidState();

        // Tell the ChatMessageManager to shutdown, including removing all outstanding overhead texts from the plugin
        chatMessageManager.shutdown();
        eventBus.unregister(chatMessageManager);

        clientToolbar.removeNavigation(navButton);
        panel = null;
    }

    private void resetRaidState() {
        raidState = TOB_STATE_NO_PARTY;
        inTob = false;
        allRaidersLoaded = false;
        panelMightNeedReset = true;

        raiderNames = new String[MAX_RAIDERS];
        raiders = new HashMap<>(MAX_RAIDERS);

        mistakeDetectorManager.shutdown();
    }

    // This should run *after* all detectors have handled the GameTick.
    @Subscribe(priority = -1)
    public void onGameTick(GameTick event) {
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

                addMistakeForPlayer(raider.getName(), mistake);
                chatMessageManager.playerMadeMistake(raider.getPlayer(), mistake);
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

    private void addMistakeForPlayer(String playerName, TobMistake mistake) {
        if (shouldTrackMistakes()) {
            SwingUtilities.invokeLater(() -> panel.addMistakeForPlayer(playerName, mistake));
        }
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
    public void onVarbitChanged(VarbitChanged event) {
        computeInTob();
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (inTob && panelMightNeedReset && event.getScriptId() == 2315) {
            Widget widget = client.getWidget(TOB_GROUP_ID, TOB_BOSS_INTERFACE_ID);
            if (widget != null && widget.getChild(TOB_BOSS_INTERFACE_TEXT_ID) != null) {
                Widget childWidget = widget.getChild(TOB_BOSS_INTERFACE_TEXT_ID);
                if (TobBossNames.MAIDEN.equals(childWidget.getText())) {
                    panel.newRaid();
                    // Set to false until next time we're no longer sure if we're in a raid.
                    panelMightNeedReset = false;
                }
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
                // TODO: I'm not sure this works for spectators, but I mean, who spectates story mode I guess...
                // TODO: I'll need to test it more if I care to fix it for story mode spectators...
                for (TobRaider raider : raiders.values()) {
                    raider.setDead(false);
                }
            }
        }
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
     * Gets a copy of all the raider names. Elements returned are guaranteed to be non-null.
     */
    public List<String> getRaiderNames() {
        return Arrays.stream(raiderNames).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Provides
    TobMistakeTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TobMistakeTrackerConfig.class);
    }
}
