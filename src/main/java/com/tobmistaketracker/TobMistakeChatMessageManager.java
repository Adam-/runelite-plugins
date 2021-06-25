package com.tobmistaketracker;

import com.google.common.annotations.VisibleForTesting;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages chat messages being sent by the plugin on behalf of the player. Whenever a mistake happens for a player,
 * this will add the corresponding overhead text and optionally send a message to the chat box. After an appropriate
 * delay, the manager will also clear the overhead text that it added on the player's behalf, since this doesn't happen
 * automatically for overhead text changes outside of OverheadTextChanged events.
 * <p>
 * NOTE: Currently, there's a bug where the chat message will clear prematurely if the player typed something before
 * making a mistake and the manager changed its overhead on their behalf. The initial message will trigger its reset
 * regardless of the manager changing the overhead text.
 * <p>
 * For example, if a player types something on tick 1, and then on tick 5 the manager sets their overhead text, on
 * tick 6 the overhead text automatically gets removed from the initial player's overhead message hook.
 * <p>
 * Update: Turns out, Adam is awesome and is publicly exposing the Player's setOverheadCycle() API. Once that's done
 * we can largely simplify this class, and/or do away with it altogether.
 */
@Singleton
public class TobMistakeChatMessageManager {

    @VisibleForTesting
    public static final int OVERHEAD_TEXT_TICK_TIMEOUT = 5;

    private final TobMistakeTrackerConfig config;
    private final Client client;
    private final boolean developerMode;

    private final Map<String, Integer> playerNameToTimeoutTick;
    private final Map<Integer, Map<String, Player>> timeoutTickToPlayers;

    @Inject
    TobMistakeChatMessageManager(TobMistakeTrackerConfig config, Client client,
                                 @Named("developerMode") boolean developerMode) {
        this.config = config;
        this.client = client;
        this.developerMode = developerMode;

        this.playerNameToTimeoutTick = new HashMap<>();
        this.timeoutTickToPlayers = new HashMap<>();
    }

    /**
     * Denotes that the specified player made a mistake. This will put a chat message above their head with the mistake,
     * and add a public chat message to the chat window (if the configuration is enabled).
     * <p>
     * The overhead text will timeout after OVERHEAD_TEXT_TICK_TIMEOUT. If the player already had an overhead text from
     * a mistake, this will replace that text and reset the timeout.
     *
     * @param player  - The player that made the mistake
     * @param mistake - The mistake that the player made
     */
    public void playerMadeMistake(Player player, TobMistake mistake) {
        String overheadText = mistake.getChatMessage();
        if (developerMode) {
            overheadText = String.format("%s - %s", client.getTickCount(), overheadText);
        }

        final int timeoutTick = client.getTickCount() + OVERHEAD_TEXT_TICK_TIMEOUT;

        final String playerName = player.getName();
        removeExistingOverheadTextForPlayer(playerName);

        // This will overwrite any existing mistake overhead, and we just need to worry about the new timeout.
        player.setOverheadText(overheadText);
        playerNameToTimeoutTick.put(playerName, timeoutTick);
        timeoutTickToPlayers.computeIfAbsent(timeoutTick, k -> new HashMap<>()).put(playerName, player);

        // Add to chat box if config is enabled
        if (config.showMistakesInChat()) {
            client.addChatMessage(ChatMessageType.PUBLICCHAT, playerName, mistake.getChatMessage(), null);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        int currentTick = client.getTickCount();

        removeAllPlayersOverheadTextForTick(currentTick);
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        Actor actor = event.getActor();
        if (actor instanceof Player) {
            Player player = (Player) actor;

            // We don't get OverheadTextChanged events when manually setting the overhead text in onGameTick, so we can
            // be sure that this didn't come from us. Whatever sent this event is in charge of managing the timeout, so
            // we can just remove any entry we have for this.
            removeExistingOverheadTextForPlayer(player.getName());
        }
    }

    public void shutdown() {
        for (int timeoutTick : new ArrayList<>(playerNameToTimeoutTick.values())) {
            removeAllPlayersOverheadTextForTick(timeoutTick);
        }
        playerNameToTimeoutTick.clear();
        timeoutTickToPlayers.clear();
    }

    /**
     * If we're already keeping track of a message for this player, remove it.
     *
     * @param playerName - The player name to remove an existing entry for
     */
    private void removeExistingOverheadTextForPlayer(String playerName) {
        final Integer existingTimeoutTick = playerNameToTimeoutTick.remove(playerName);
        if (existingTimeoutTick != null) {
            // We're already keeping track of a message for this player -- let's remove it
            timeoutTickToPlayers.get(existingTimeoutTick).remove(playerName);
        }
    }

    private void removeAllPlayersOverheadTextForTick(int timeoutTick) {
        // Get all the players we need to timeout for this timeout tick
        Map<String, Player> playersToTimeout = timeoutTickToPlayers.remove(timeoutTick);

        if (playersToTimeout != null) {
            for (Player player : playersToTimeout.values()) {
                if (player != null) {
                    player.setOverheadText(null);
                    playerNameToTimeoutTick.remove(player.getName());
                }
            }
        }
    }
}
