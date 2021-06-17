package com.tobmistaketracker;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Listen, I know this is jank, but I think it works... I'm open to suggestions for better ways of detecting this.
 */
@Slf4j
public class MaidenMistakeDetector implements TobMistakeDetector {

    private static final int BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID = 32984;
    private static final int MAIDEN_BLOOD_GRAPHICS_OBJECT_ID = 1579;

    private static final int CYCLES_PER_GAME_TICK = Constants.GAME_TICK_LENGTH / Constants.CLIENT_TICK_LENGTH;

    // Sometimes the activation calculation is actually off by a few cycles. We'll account for that with an offset.
    private static final int BLOOD_SPAWN_ACTIVATION_CYCLE_OFFSET = 5;

    // It's easier to track these separately and check if player is in either of them, since they can overlap and
    // we don't need to worry about removing one accidentally when the other despawns. They're also different objects.
    private final Set<WorldPoint> bloodSpawnBloodTiles;
    private final Map<WorldPoint, GraphicsObject> maidenBloodTiles;
    private final List<GraphicsObject> maidenBloodGraphicsObjects;

    private final TobMistakeTrackerPlugin plugin;
    private final Client client;
    private final OverlayManager overlayManager;
    private final MaidenBloodTilesOverlay overlay;

    public MaidenMistakeDetector(TobMistakeTrackerPlugin plugin) {
        this.plugin = plugin;
        this.client = plugin.client;
        this.overlayManager = plugin.overlayManager;
        this.overlay = new MaidenBloodTilesOverlay(client);
        this.overlayManager.add(overlay);

        bloodSpawnBloodTiles = new HashSet<>();
        maidenBloodTiles = new HashMap<>();
        maidenBloodGraphicsObjects = new ArrayList<>();
    }

    @Override
    public void cleanup() {
        bloodSpawnBloodTiles.clear();
        maidenBloodTiles.clear();
        maidenBloodGraphicsObjects.clear();

        overlayManager.remove(overlay);
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        // TODO: stop when maiden dies
        if (isOnBloodTile(raider.getPreviousWorldLocation())) {
            return Collections.singletonList(TobMistake.MAIDEN_BLOOD);
        }

        return Collections.emptyList();
    }

    private boolean isOnBloodTile(WorldPoint worldPoint) {
        // TODO: bloodSpawnBloodTiles might be 1 tick off -- will need to do more testing.
        return bloodSpawnBloodTiles.contains(worldPoint) || maidenBloodTiles.containsKey(worldPoint);
    }

    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        GraphicsObject go = event.getGraphicsObject();
        if (go.getId() == MAIDEN_BLOOD_GRAPHICS_OBJECT_ID) {
            maidenBloodGraphicsObjects.add(go);
        }
    }

    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject go = event.getGameObject();
        if (go.getId() == BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID) {
            bloodSpawnBloodTiles.add(go.getWorldLocation());
        }
    }

    public void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject go = event.getGameObject();
        if (go.getId() == BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID) {
            bloodSpawnBloodTiles.remove(go.getWorldLocation());
        }
    }

    public void onGameTick(GameTick event) {
//        log.info(String.format("Maiden - onGameTick: %s", client.getTickCount()));

        // Compute when a blood tile actually "activates"
        int currentCycle = client.getGameCycle();
        for (GraphicsObject graphicsObject : new ArrayList<>(maidenBloodGraphicsObjects)) {
            if (isInactive(graphicsObject)) {
                maidenBloodGraphicsObjects.remove(graphicsObject);
            } else {
                int activationCycle = graphicsObject.getStartCycle() - CYCLES_PER_GAME_TICK + BLOOD_SPAWN_ACTIVATION_CYCLE_OFFSET;
                if (currentCycle >= activationCycle) {
                    // This is now an active blood tile (technically it was on the tick before this handle invocation,
                    // so we account for that in the condition)
                    WorldPoint bloodLocation = WorldPoint.fromLocal(client, graphicsObject.getLocation());
                    log.info("" + client.getTickCount() + " - Activated blood on " + bloodLocation + "\n" +
                            "Current Cycle: " + currentCycle + " Activation Cycle: " + activationCycle + " - diff: " +
                            (currentCycle - activationCycle));

                    maidenBloodTiles.put(bloodLocation, graphicsObject);
                    maidenBloodGraphicsObjects.remove(graphicsObject);
                }
            }
        }

        // Remove "inactive" blood tiles
        for (Map.Entry<WorldPoint, GraphicsObject> bloodTileEntry : new HashSet<>(maidenBloodTiles.entrySet())) {
            if (isInactive(bloodTileEntry.getValue())) {
                maidenBloodTiles.remove(bloodTileEntry.getKey());
            }
        }

        overlay.setTiles(maidenBloodTiles.keySet());
        overlay.setRaiderTiles(plugin.getRaiders().stream()
                .map(TobRaider::getPreviousWorldLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private boolean isInactive(GraphicsObject graphicsObject) {
        return graphicsObject == null || graphicsObject.finished();
    }
}
