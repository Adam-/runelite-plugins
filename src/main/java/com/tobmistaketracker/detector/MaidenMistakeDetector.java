package com.tobmistaketracker.detector;

import com.google.common.annotations.VisibleForTesting;
import com.tobmistaketracker.TobBossNames;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * When Maiden throws her blood at players, she always throws 1 to where each player is currently standing, and 2 extra
 * to the furthest player. The remainingCycles on the blood Projectile depends on how far the player is from her
 * actual hitbox (not what's shown in-game, NE is closer). 1 tile away takes 65 cycles, incrementing by 15 for every
 * extra tile away from her hitbox (80, 95, 110, etc.). The extra two bloods thrown at the furthest player are *always*
 * +25 cycles from that player's blood spot (65 -> 90, 80 -> 105, 95 -> 120, etc.), which guarantees that they will
 * activate one tick later than the main blood spot.
 * <p>
 * Since there are 30 cycles per GameTick (a cycle happens once every 20ms), we can do some basic math to figure out
 * when the projectile is supposed to land on the tile and start becoming an active blood spot.
 * The math is: gameTicksToActivate = floor(remainingCycles / CYCLES_PER_GAME_TICK).
 * <p>
 * Once the blood spot is active, it *always* last for exactly 11 GameTicks.
 * <p>
 * Additionally, I was going to add splashing on a nylo as a mistake, but there were too many edge cases that couldn't
 * be resolved guaranteed, so I ended up scrapping it for now. I might revisit it in the future. In case I do, this
 * is how it works:
 * <p>
 * A player will freeze on tick 1, which is also when the Projectile is created and the graphic
 * on the npc is changed. By tick 2, the npc is actually frozen (if it caught), but the rest of the projectile
 * could take several more ticks to finish (up to 5 total?) -- We should add a delay on when to show the mistake
 * so it can't be used for decision-making.
 */
@Slf4j
@Singleton
public class MaidenMistakeDetector extends BaseTobMistakeDetector {

    private static final int BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID = 32984;
    private static final int MAIDEN_BLOOD_PROJECTILE_ID = 1578;

    private static final int CYCLES_PER_GAME_TICK = Constants.GAME_TICK_LENGTH / Constants.CLIENT_TICK_LENGTH;

    // Each blood tile from maiden lasts exactly 11 ticks
    private static final int MAIDEN_BLOOD_GAME_TICK_LENGTH = 11;

    // It's easier to track these separately and check if a player is in either of them, since they (maybe?) can overlap
    // and we don't need to worry about removing one accidentally when the other despawns.
    private final Set<WorldPoint> bloodSpawnBloodTiles;
    private final Set<WorldPoint> maidenBloodTiles;

    private final Map<Integer, List<WorldPoint>> maidenBloodTilesToActivate; // Key is activationTick
    private final Map<Integer, List<WorldPoint>> activeMaidenBloodTiles; // Key is deactivationTick

    // From what I can tell, we need to remove the blood spawn tiles *after* we detect for that tick, so aggregate here
    private final Set<WorldPoint> bloodSpawnBloodTilesToRemove;

    @Inject
    public MaidenMistakeDetector() {
        bloodSpawnBloodTiles = new HashSet<>();
        bloodSpawnBloodTilesToRemove = new HashSet<>();
        maidenBloodTiles = new HashSet<>();
        maidenBloodTilesToActivate = new HashMap<>();
        activeMaidenBloodTiles = new HashMap<>();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        bloodSpawnBloodTiles.clear();
        bloodSpawnBloodTilesToRemove.clear();
        maidenBloodTiles.clear();
        maidenBloodTilesToActivate.clear();
        activeMaidenBloodTiles.clear();
    }

    @Override
    protected void computeDetectingMistakes() {
        if (!detectingMistakes && isAlreadySpawned()) {
            detectingMistakes = true;
        }
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        if (!raider.isDead() && isOnBloodTile(raider.getPreviousWorldLocation())) {
            return Collections.singletonList(TobMistake.MAIDEN_BLOOD);
        }

        return Collections.emptyList();
    }

    @Override
    public void afterDetect() {
        // Remove the blood spawn blood tiles *after* detecting this tick, so that it's still around for detection.
        bloodSpawnBloodTiles.removeAll(bloodSpawnBloodTilesToRemove);
        bloodSpawnBloodTilesToRemove.clear();
    }

    private boolean isOnBloodTile(WorldPoint worldPoint) {
        return bloodSpawnBloodTiles.contains(worldPoint) || maidenBloodTiles.contains(worldPoint);
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (event.getProjectile().getId() == MAIDEN_BLOOD_PROJECTILE_ID) {
            int gameTicksToActivate =
                    (int) Math.floor((double) event.getProjectile().getRemainingCycles() / CYCLES_PER_GAME_TICK);
            int activationTick = client.getTickCount() + gameTicksToActivate;
            WorldPoint worldPoint = WorldPoint.fromLocal(client, event.getPosition());

            maidenBloodTilesToActivate.computeIfAbsent(activationTick, k -> new ArrayList<>()).add(worldPoint);
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject go = event.getGameObject();
        if (go.getId() == BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID) {
            // TODO: These are missing if the plugin is turned on during the wave, after some have spawned. We could
            // TODO: aggregate all of them on startup too, to catch whatever's already been spawned. Pretty minor though
            bloodSpawnBloodTiles.add(go.getWorldLocation());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject go = event.getGameObject();
        if (go.getId() == BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID) {
            // Remove these *after* detecting this tick, since they were still present in the previous player location.
            bloodSpawnBloodTilesToRemove.add(go.getWorldLocation());
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (!detectingMistakes && TobBossNames.MAIDEN.equals(event.getActor().getName())) {
            detectingMistakes = true;
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        Actor actor = event.getActor();
        if (actor instanceof NPC) {
            if (TobBossNames.MAIDEN.equals(event.getActor().getName())) {
                shutdown();
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        int currentGameTick = client.getTickCount();

        // Find all blood tiles to activate this Game Tick
        if (maidenBloodTilesToActivate.containsKey(currentGameTick)) {
            int deactivationTick = currentGameTick + MAIDEN_BLOOD_GAME_TICK_LENGTH;
            for (WorldPoint worldPoint : maidenBloodTilesToActivate.remove(currentGameTick)) {
                activeMaidenBloodTiles.computeIfAbsent(deactivationTick, k -> new ArrayList<>()).add(worldPoint);
                // Also add to maiden blood tiles Set for fast detection
                maidenBloodTiles.add(worldPoint);
            }
        }

        // Remove all blood tiles that should deactivate this Game Tick
        if (activeMaidenBloodTiles.containsKey(currentGameTick)) {
            for (WorldPoint worldPoint : activeMaidenBloodTiles.remove(currentGameTick)) {
                maidenBloodTiles.remove(worldPoint);
            }
        }
    }

    private boolean isAlreadySpawned() {
        return client.getNpcs().stream().anyMatch(npc -> TobBossNames.MAIDEN.equals(npc.getName()));
    }

    @VisibleForTesting
    public Set<WorldPoint> getBloodSpawnBloodTiles() {
        return Collections.unmodifiableSet(bloodSpawnBloodTiles);
    }

    @VisibleForTesting
    public Set<WorldPoint> getMaidenBloodTiles() {
        return Collections.unmodifiableSet(maidenBloodTiles);
    }
}
