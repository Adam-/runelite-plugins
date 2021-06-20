package com.tobmistaketracker.detector;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.tobmistaketracker.TobBossNames;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import com.tobmistaketracker.TobRaider;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * When Bloat spawns hands/feet, they hit the ground after 3 GameTicks. However, they also create a new GraphicsObject
 * for a blood squirt animation on that tick, so we can just detect when that spawns instead. For any player standing
 * on the same tile as the blood object, they are stunned.
 * <p>
 * I've decided that flying and taking a stomp are *not* universal mistakes, as they have legitimate use cases in the
 * current meta. I'd argue that taking a bloat hand/foot and getting stunned is *always* incorrect, though.
 * <p>
 * Getting hit by a hand/foot after Bloat has already died will *not* count as a mistake, though if the player dies,
 * it will still count as a death mistake.
 */
@Slf4j
@Singleton
public class BloatMistakeDetector implements TobMistakeDetector {

    // 1570 and 1572 are hands, 1571 and 1573 are feet. Let's just call everything hands for consistency.
    private static final Set<Integer> BLOAT_HAND_GRAPHICS_OBJECT_IDS = ImmutableSet.of(1570, 1571, 1572, 1573);

    // This is the blood squirt animation that spawns and plays when a hand hits the ground.
    private static final int BLOAT_HAND_BLOOD_GRAPHICS_OBJECT_ID = 1576;

    private final Set<WorldPoint> activeHandTiles;

    private final TobMistakeTrackerPlugin plugin;

    private final Client client;

    @Getter
    private boolean detectingMistakes;

    @Inject
    public BloatMistakeDetector(TobMistakeTrackerPlugin plugin, Client client) {
        this.plugin = plugin;
        this.client = client;

        this.activeHandTiles = new HashSet<>();
    }

    @Override
    public void startup() {
        detectingMistakes = true;
    }

    @Override
    public void shutdown() {
        activeHandTiles.clear();
        detectingMistakes = false;
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        if (activeHandTiles.contains(raider.getPreviousWorldLocation())) {
            return Collections.singletonList(TobMistake.BLOAT_HAND);
        }

        return Collections.emptyList();
    }

    @Override
    public void afterDetect() {
        activeHandTiles.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == BLOAT_HAND_BLOOD_GRAPHICS_OBJECT_ID) {
            LocalPoint localPoint = event.getGraphicsObject().getLocation();
            activeHandTiles.add(WorldPoint.fromLocal(client, localPoint));
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (TobBossNames.BLOAT.equals(event.getActor().getName())) {
            startup();
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        Actor actor = event.getActor();
        if (actor instanceof NPC) {
            if (TobBossNames.BLOAT.equals(event.getActor().getName())) {
                shutdown();
            }
        }
    }

    @VisibleForTesting
    public Set<WorldPoint> getActiveHandTiles() {
        return Collections.unmodifiableSet(activeHandTiles);
    }
}
