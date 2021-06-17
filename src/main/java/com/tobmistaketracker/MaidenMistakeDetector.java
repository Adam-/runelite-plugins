package com.tobmistaketracker;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
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
import java.util.Set;

@Slf4j
public class MaidenMistakeDetector {

    private static final int BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID = 32984;
    private static final int MAIDEN_BLOOD_GRAPHICS_OBJECT_ID = 1579;

    private static final int CYCLES_PER_GAME_TICK = Constants.GAME_TICK_LENGTH / Constants.CLIENT_TICK_LENGTH;

    // It's easier to track these separately and check if player is in either of them, since they can overlap and
    // we don't need to worry about removing one accidentally when the other despawns.
    private final Set<WorldPoint> bloodSpawnBloodTiles;
    private final Map<WorldPoint, GraphicsObject> maidenBloodTiles;
    private final List<GraphicsObject> maidenBloodGraphicsObjects;

    private final Client client;
    private final OverlayManager overlayManager;
    private final MaidenBloodTilesOverlay overlay;

    MaidenMistakeDetector(Client client, OverlayManager overlayManager) {
        this.client = client;
        this.overlayManager = overlayManager;
        this.overlay = new MaidenBloodTilesOverlay(client);
        this.overlayManager.add(overlay);

        bloodSpawnBloodTiles = new HashSet<>();
        maidenBloodTiles = new HashMap<>();
        maidenBloodGraphicsObjects = new ArrayList<>();
    }

    public void cleanup() {
        bloodSpawnBloodTiles.clear();
        maidenBloodTiles.clear();
        maidenBloodGraphicsObjects.clear();

        overlayManager.remove(overlay);
    }

    public List<TobMistake> detectMistakes(@NonNull Player player) {
        if (isOnBloodTile(player.getWorldLocation())) {
            return Collections.singletonList(TobMistake.MAIDEN_BLOOD);
        }

        return Collections.emptyList();
    }

    public List<TobMistake> detectMistakes(@NonNull WorldPoint worldPoint) {
        // Ideally we would detect mistakes for the raiders, not just world points.
        // These are technically where the raider was standing on the previous tick's handling invocation, which is
        // when the server handled it (I think).
        if (isOnBloodTile(worldPoint)) {
            return Collections.singletonList(TobMistake.MAIDEN_BLOOD);
        }

        return Collections.emptyList();
    }

    private boolean isOnBloodTile(WorldPoint worldPoint) {
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
            } else if (currentCycle >= graphicsObject.getStartCycle() - CYCLES_PER_GAME_TICK) {
                // This is now an active blood tile (technically it was on the tick before this handle invocation, so
                // we account for that in the condition)
                WorldPoint bloodLocation = WorldPoint.fromLocal(client, graphicsObject.getLocation());

                maidenBloodTiles.put(bloodLocation, graphicsObject);
                maidenBloodGraphicsObjects.remove(graphicsObject);
            }
        }

        // Remove "inactive" blood tiles
        for (Map.Entry<WorldPoint, GraphicsObject> bloodTileEntry : new HashSet<>(maidenBloodTiles.entrySet())) {
            if (isInactive(bloodTileEntry.getValue())) {
                maidenBloodTiles.remove(bloodTileEntry.getKey());
            }
        }

        overlay.setTiles(maidenBloodTiles.keySet());
    }

    private boolean isInactive(GraphicsObject graphicsObject) {
        return graphicsObject == null || graphicsObject.finished();
    }
}
