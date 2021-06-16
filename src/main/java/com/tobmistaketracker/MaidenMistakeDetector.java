package com.tobmistaketracker;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.*;

@Slf4j
public class MaidenMistakeDetector {

    private static final int BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID = 32984;
    private static final int MAIDEN_BLOOD_GRAPHICS_OBJECT_ID = 1579;

    // It's easier to track these separately and check if player is in either of them, since they can overlap and
    // we don't need to worry about removing one accidentally when the other despawns.
    Set<LocalPoint> bloodSpawnBloodTiles;
    Map<LocalPoint, GraphicsObject> maidenBloodTiles;

    List<GraphicsObject> maidenBloodGraphicsObjects;

    @Inject
    private Client client;

    MaidenMistakeDetector() {
        init();
    }

    public void init() {
        bloodSpawnBloodTiles = new HashSet<>();
        maidenBloodTiles = new HashMap<>();
        maidenBloodGraphicsObjects = new ArrayList<>();
    }

    public void cleanup() {
        bloodSpawnBloodTiles.clear();
        maidenBloodTiles.clear();
        maidenBloodGraphicsObjects.clear();
    }

    public List<TobMistake> detectMistakes(@NonNull Player player) {
        if (isOnBloodTile(player.getLocalLocation())) {
            return Collections.singletonList(TobMistake.MAIDEN_BLOOD);
        }

        return Collections.emptyList();
    }

    private boolean isOnBloodTile(LocalPoint localPoint) {
        return bloodSpawnBloodTiles.contains(localPoint) || maidenBloodTiles.containsKey(localPoint);
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
            bloodSpawnBloodTiles.add(go.getLocalLocation());
        }
    }

    public void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject go = event.getGameObject();
        if (go.getId() == BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID) {
            bloodSpawnBloodTiles.remove(go.getLocalLocation());
        }
    }

    public void onGameTick(GameTick event) {
        log.info(String.format("Maiden - onGameTick: %s", client.getTickCount()));

        if (maidenBloodGraphicsObjects.isEmpty()) {
            return;
        }

        // Compute when a blood tile actually "activates"
        int currentCycle = client.getGameCycle();
        for (GraphicsObject graphicsObject : new ArrayList<>(maidenBloodGraphicsObjects)) {
            if (isInactive(graphicsObject)) {
                maidenBloodGraphicsObjects.remove(graphicsObject);
            } else if (currentCycle >= graphicsObject.getStartCycle()) {
                // This is now an active blood tile
                LocalPoint bloodLocation = graphicsObject.getLocation();
                maidenBloodTiles.put(bloodLocation, graphicsObject);
                maidenBloodGraphicsObjects.remove(graphicsObject);
            }
        }

        // Remove "inactive" blood tiles
        for (Map.Entry<LocalPoint, GraphicsObject> bloodTileEntry : new HashSet<>(maidenBloodTiles.entrySet())) {
            if (isInactive(bloodTileEntry.getValue())) {
                maidenBloodTiles.remove(bloodTileEntry.getKey());
            }
        }
    }

    private boolean isInactive(GraphicsObject graphicsObject) {
        return graphicsObject == null || graphicsObject.finished();
    }
}
