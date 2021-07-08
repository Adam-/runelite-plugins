package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobBossNames;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tobmistaketracker.TobMistakeTrackerPlugin.TOB_ROOM_TRANSITION_SCRIPT_ID;
import static com.tobmistaketracker.TobMistakeTrackerPlugin.getTobRoomEnterText;

@Slf4j
@Singleton
public class DeathMistakeDetector extends BaseTobMistakeDetector {

    private static final String REJOINED_PARTY_TEXT = "You have rejoined your party";
    private static final String NYLOCAS_NPC_NAME_PREFIX = "Nylocas";

    private final Set<String> playerDeaths;

    private TobRoom currentRoom;

    @NonNull
    private String previousRoomText; // For optimization so we only compute currentRoom on a new roomText

    @Inject
    public DeathMistakeDetector() {
        this.playerDeaths = new HashSet<>();
        currentRoom = null;
        this.previousRoomText = "";
    }

    @Override
    protected void computeDetectingMistakes() {
        // Always detect deaths throughout the raid
        detectingMistakes = true;

        if (currentRoom == null) {
            computeCurrentRoom();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        playerDeaths.clear();
        currentRoom = null;
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        List<TobMistake> mistakes = new ArrayList<>();
        if (playerDeaths.contains(raider.getName())) {
            mistakes.add(TobMistake.DEATH);

            if (currentRoom == null) {
                // Try one more time to compute currentRoom
                computeCurrentRoom();
            }

            // In the case that we still can't determine which room we're in, we just won't add the room death
            if (currentRoom != null) {
                mistakes.add(currentRoom.getDeathMistake());
            }
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        playerDeaths.clear();
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        // This is a bit more accurate than using onActorDeath, since this guarantees that the server thinks the player
        // is dead, as opposed to just the client thinking so.
        if (event.getActor() instanceof Player && event.getActor().getAnimation() == AnimationID.DEATH) {
            playerDeaths.add(event.getActor().getName());
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (currentRoom == null) {
            computeCurrentRoomForBossNpc(event.getNpc());
        }
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() != TOB_ROOM_TRANSITION_SCRIPT_ID) return;

        final String roomText = getTobRoomEnterText(client);

        if (currentRoom == null && REJOINED_PARTY_TEXT.equals(roomText)) {
            computeCurrentRoom();
        }

        if (previousRoomText.equals(roomText)) {
            // No need to try to find a new TobRoom value as it's the same text as we just got previously.
            return;
        }
        previousRoomText = roomText;

        TobRoom tobRoom = TobRoom.fromRoomName(roomText);
        if (tobRoom != null && tobRoom != currentRoom) {
            log.debug("New Tob Room: " + tobRoom);
            currentRoom = tobRoom;
        }
    }

    /**
     * This is for handling edge cases in detecting the currentRoom. Normally, as long as there are no disconnects or
     * turning off/on the plugin mid-raid, the currentRoom will be computed during normal room transitions. This is to
     * catch the scenarios in which that wasn't possible, like detecting if there's a boss in the room etc.
     */
    private void computeCurrentRoom() {
        if (currentRoom != null) return; // Nothing to do

        // Let's check bosses first
        for (NPC npc : client.getNpcs()) {
            computeCurrentRoomForBossNpc(npc);

            if (currentRoom != null) return; // Compute completed
        }

        // Nylo should be the only room in which the just going off the boss won't necessarily work. For this case,
        // we can check for if there's *any* nylo in the room, as I don't think it's possible for a nylo to be in
        // a tob room without a boss already existing, except for Nylo room itself.
        for (NPC npc : client.getNpcs()) {
            if (npc.getName() != null && npc.getName().startsWith(NYLOCAS_NPC_NAME_PREFIX)) {
                currentRoom = TobRoom.NYLOCAS;
                log.debug("Computed Nylo Tob Room");
                return;
            }
        }

        // TODO: Technically Sotetseg maze shadow realm is still unaccounted for, but currently any deaths in there
        // anyway already don't fully work for everyone else, so let's just ignore it for now. The only way to get in
        // that state is to turn off and on your plugin while inside sot maze shadow realm and then have someone die.
    }

    private void computeCurrentRoomForBossNpc(NPC npc) {
        if (npc.getName() == null) return;

        TobRoom tobRoom = TobRoom.fromRoomBossName(npc.getName());
        if (tobRoom != null) {
            log.debug("Computed Tob Room: " + tobRoom);
            currentRoom = tobRoom;
        }
    }

    @AllArgsConstructor
    private enum TobRoom {
        MAIDEN("The Maiden of Sugadinti", TobMistake.DEATH_MAIDEN, TobBossNames.MAIDEN),
        BLOAT("The Pestilent Bloat", TobMistake.DEATH_BLOAT, TobBossNames.BLOAT),
        NYLOCAS("The Nylocas", TobMistake.DEATH_NYLOCAS, TobBossNames.NYLO_BOSS),
        SOTETSEG("Sotetseg", TobMistake.DEATH_SOTETSEG, TobBossNames.SOTETSEG),
        XARPUS("Xarpus", TobMistake.DEATH_XARPUS, TobBossNames.XARPUS),
        VERZIK("The Final Challenge", TobMistake.DEATH_VERZIK, TobBossNames.VERZIK);

        @Getter
        private final String roomName;

        @Getter
        private final TobMistake deathMistake;

        @Getter
        private final String roomBossName;

        /**
         * Retrieves the corresponding TobRoom for the specified roomName
         *
         * @param roomName - The name of the tob room
         * @return The TobRoom for the specified roomName, or null if none could be found
         */
        public static TobRoom fromRoomName(final String roomName) {
            for (TobRoom tobRoom : TobRoom.values()) {
                if (tobRoom.getRoomName().equals(roomName)) {
                    return tobRoom;
                }
            }

            return null;
        }

        /**
         * Retrieves the corresponding TobRoom for the specified boss
         *
         * @param roomBossName - The name of the main boss for the tob room
         * @return The TobRoom for the specified boss, or null if none could be found
         */
        public static TobRoom fromRoomBossName(final String roomBossName) {
            for (TobRoom tobRoom : TobRoom.values()) {
                if (tobRoom.getRoomBossName().equals(roomBossName)) {
                    return tobRoom;
                }
            }

            return null;
        }
    }
}
