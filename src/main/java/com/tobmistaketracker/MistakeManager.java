package com.tobmistaketracker;

import com.google.common.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    public Iterator<Map.Entry<TobMistake, Integer>> getMistakesForPlayer(String playerName) {
        return getPlayerMistakes(playerName).entrySet().iterator();
    }

    private static <T> Integer increment(T key, Integer oldValue) {
        return oldValue == null ? 1 : oldValue + 1;
    }

    private Map<TobMistake, Integer> getPlayerMistakes(String playerName) {
        return mistakesForPlayers.computeIfAbsent(playerName, k -> new HashMap<>());
    }
}
