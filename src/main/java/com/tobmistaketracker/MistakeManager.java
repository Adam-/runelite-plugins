package com.tobmistaketracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Singleton
public class MistakeManager {

    @VisibleForTesting
    final Map<String, Map<TobMistake, Integer>> mistakesForPlayers;

    MistakeManager() {
        mistakesForPlayers = new HashMap<>();
    }

    public void clearAllMistakes() {
        mistakesForPlayers.clear();
    }

    public int addMistakeForPlayer(String playerName, TobMistake mistake) {
        Map<TobMistake, Integer> playerMistakes = getPlayerMistakes(playerName);
        return playerMistakes.compute(mistake, MistakeManager::increment);
    }

    public Map<TobMistake, Integer> getMistakesForPlayer(String playerName) {
        return ImmutableMap.copyOf(getPlayerMistakes(playerName)); // TODO: Probably not needed like this. Probably
        // Just allow clients to give a TobMistake and access the map ourselves.
    }

    private static <T> Integer increment(T key, Integer oldValue) {
        return oldValue == null ? 1 : oldValue + 1;
    }

    private Map<TobMistake, Integer> getPlayerMistakes(String playerName) {
        return mistakesForPlayers.computeIfAbsent(playerName, k -> new HashMap<>());
    }
}
