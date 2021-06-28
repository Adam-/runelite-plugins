package com.tobmistaketracker.detector;

import com.google.common.annotations.VisibleForTesting;
import com.tobmistaketracker.TobBossNames;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verzik P2 is pretty straightforward. Detect the specific game objects/graphics objects/animations and we're done.
 * Currently this tracks bounces, bombs, and acid tiles.
 */
@Slf4j
@Singleton
public class VerzikP2MistakeDetector extends BaseTobMistakeDetector {

    private static final int VERZIK_P2_POSE_ANIMATION_ID = 8113;
    private static final int VERZIK_BOMB_GRAPHICS_OBJECT_ID = 1584;
    private static final int PLAYER_BOUNCE_ANIMATION_ID = 1157;
    private static final int VERZIK_ACID_GAME_OBJECT_ID = 41747;

    private final Set<WorldPoint> activeBombTiles;

    private final Set<WorldPoint> activeAcidTiles;
    private final Set<WorldPoint> acidTilesToRemove;

    // AFAIK you can only have one person bounced per tick, but just in case that ever changes...
    private final Set<String> playerNamesBounced;

    @Inject
    public VerzikP2MistakeDetector() {
        activeBombTiles = new HashSet<>();
        activeAcidTiles = new HashSet<>();
        acidTilesToRemove = new HashSet<>();
        playerNamesBounced = new HashSet<>();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        activeBombTiles.clear();
        activeAcidTiles.clear();
        acidTilesToRemove.clear();
        playerNamesBounced.clear();
    }

    @Override
    protected void computeDetectingMistakes() {
        if (!detectingMistakes && isAlreadySpawned()) {
            detectingMistakes = true;
        }
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        List<TobMistake> mistakes = new ArrayList<>();

        if (raider.isDead()) {
            return mistakes;
        }

        // Put acid mistake first, so if acid and bomb happen on the same tick, the chat overhead is for the latter.
        if (activeAcidTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(TobMistake.VERZIK_P2_ACID);
        }

        if (activeBombTiles.contains(raider.getPreviousWorldLocation())) {
            mistakes.add(TobMistake.VERZIK_P2_BOMB);
        }

        // Currently, there doesn't seem to be a way to be both bombed *and* bounced on the same tick, but let's
        // write it up this way anyway in case that ever changes, since it's not a problem to do so.
        if (playerNamesBounced.contains(raider.getName())) {
            mistakes.add(TobMistake.VERZIK_P2_BOUNCE);
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        activeBombTiles.clear();
        activeAcidTiles.removeAll(acidTilesToRemove);
        acidTilesToRemove.clear();
        playerNamesBounced.clear();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == VERZIK_BOMB_GRAPHICS_OBJECT_ID) {
            WorldPoint worldPoint = WorldPoint.fromLocal(client, event.getGraphicsObject().getLocation());
            activeBombTiles.add(worldPoint);
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == VERZIK_ACID_GAME_OBJECT_ID) {
            activeAcidTiles.add(event.getGameObject().getWorldLocation());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (event.getGameObject().getId() == VERZIK_ACID_GAME_OBJECT_ID) {
            // Remove these *after* detecting this tick, since they were still present in the previous player location.
            acidTilesToRemove.add(event.getGameObject().getWorldLocation());
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() instanceof Player && event.getActor().getAnimation() == PLAYER_BOUNCE_ANIMATION_ID) {
            playerNamesBounced.add(event.getActor().getName());
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (!detectingMistakes && isVerzikP2(event.getActor())) {
            detectingMistakes = true;
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        if (!detectingMistakes && isVerzikP2(event.getNpc())) {
            detectingMistakes = true;
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (event.getActor() instanceof NPC && isVerzikP2(event.getActor())) {
            shutdown();
        }
    }

    private boolean isAlreadySpawned() {
        return client.getNpcs().stream().anyMatch(VerzikP2MistakeDetector::isVerzikP2);
    }

    private static boolean isVerzikP2(Actor actor) {
        return TobBossNames.VERZIK.equals(actor.getName()) && actor.getPoseAnimation() == VERZIK_P2_POSE_ANIMATION_ID;
    }

    @VisibleForTesting
    public Set<WorldPoint> getVerzikP2AcidTiles() {
        return Collections.unmodifiableSet(activeAcidTiles);
    }
}
