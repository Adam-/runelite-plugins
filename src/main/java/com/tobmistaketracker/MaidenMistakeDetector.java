package com.tobmistaketracker;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.*;

public class MaidenMistakeDetector {

    private static int BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID = 32984;
    private static int MAIDEN_BLOOD_PROJECTILE_ID = 1578;
    private static int MAIDEN_BLOOD_GRAPHICS_OBJECT_ID = 1579;

    // It's easier to track these separately and check if player is in either of them, since they can overlap and
    // we don't need to worry about removing one accidentally when the other despawns.
    Set<LocalPoint> bloodSpawnBloodTiles;
    Set<LocalPoint> maidenBloodTiles;

    // This is a map of end positions to Projectile objects for fast lookups
    Map<LocalPoint, Projectile> maidenBloodProjectiles;
    List<GraphicsObject> maidenBloodGraphicsObjects;

    @Inject
    private Client client;

    public void init() {
        bloodSpawnBloodTiles = new HashSet<>();
        maidenBloodTiles = new HashSet<>();

        maidenBloodProjectiles = new HashMap<>();
        maidenBloodGraphicsObjects = new ArrayList<>();
    }

    public void cleanup() {
        bloodSpawnBloodTiles.clear();
        maidenBloodTiles.clear();

        maidenBloodProjectiles.clear();
        maidenBloodGraphicsObjects.clear();
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        Projectile projectile = event.getProjectile();
        if (projectile.getId() == MAIDEN_BLOOD_PROJECTILE_ID) {
            maidenBloodProjectiles.put(event.getPosition(), projectile);
        }
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        GraphicsObject go = event.getGraphicsObject();
        if (go.getId() == MAIDEN_BLOOD_GRAPHICS_OBJECT_ID) {
            maidenBloodGraphicsObjects.add(go);
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject go = event.getGameObject();
        if (go.getId() == BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID) {
            bloodSpawnBloodTiles.add(go.getLocalLocation());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject go = event.getGameObject();
        if (go.getId() == BLOOD_SPAWN_BLOOD_GAME_OBJECT_ID) {
            bloodSpawnBloodTiles.remove(go.getLocalLocation());
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (maidenBloodProjectiles.isEmpty() || maidenBloodGraphicsObjects.isEmpty()) {
            return;
        }

        // We need to calculate when the blood tile actually "activates" and heals maiden
        int currentCycle = client.getGameCycle();
        for (GraphicsObject graphicsObject : new ArrayList<>(maidenBloodGraphicsObjects)) {
            if (currentCycle > graphicsObject.getStartCycle()) {
                // This is now an active blood tile
                LocalPoint bloodLocation = graphicsObject.getLocation();
                maidenBloodGraphicsObjects.remove(graphicsObject);
            }

            if (maidenBloodProjectiles.containsKey(bloodLocation)) {
                Projectile matchedProjectile = maidenBloodProjectiles.get(bloodLocation);
                matchedProjectile.getEndCycle();
            }
        }


    }
}
