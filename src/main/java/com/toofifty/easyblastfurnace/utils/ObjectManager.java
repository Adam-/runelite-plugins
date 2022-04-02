package com.toofifty.easyblastfurnace.utils;

import com.google.inject.Singleton;
import net.runelite.api.GameObject;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ObjectManager
{
    private final Map<Integer, GameObject> gameObjects = new HashMap<>();

    public void add(GameObject object)
    {
        gameObjects.put(object.getId(), object);
    }

    public GameObject get(int id)
    {
        return gameObjects.get(id);
    }
}
