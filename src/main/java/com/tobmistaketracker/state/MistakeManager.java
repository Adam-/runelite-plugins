package com.tobmistaketracker.state;

import com.tobmistaketracker.TobMistake;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of mistakes for players
 */
class MistakeManager {

    private final Map<String, Map<TobMistake, Integer>> mistakesForPlayers;

    MistakeManager() {
        mistakesForPlayers = new HashMap<>();
    }

    public void clearAllMistakes() {
        mistakesForPlayers.clear();
    }

    public void addMistakeForPlayer(String playerName, TobMistake mistake) {
        Map<TobMistake, Integer> playerMistakes = mistakesForPlayers.computeIfAbsent(playerName, k -> new HashMap<>());
        playerMistakes.compute(mistake, MistakeManager::increment);
    }

    public void removeAllMistakesForPlayer(String playerName) {
        mistakesForPlayers.remove(playerName);
    }

    public Set<String> getPlayersWithMistakes() {
        return Collections.unmodifiableSet(mistakesForPlayers.keySet());
    }

    public int getMistakeCountForPlayer(String playerName, TobMistake mistake) {
        Map<TobMistake, Integer> playerMistakes = mistakesForPlayers.get(playerName);
        if (playerMistakes != null) {
            Integer count = playerMistakes.get(mistake);
            if (count != null) {
                return count;
            }
        }

        return 0;
    }

    public int getTotalMistakeCountForAllPlayers() {
        int totalMistakes = 0;
        for (Map<TobMistake, Integer> mistakesForPlayer : mistakesForPlayers.values()) {
            for (int mistakes : mistakesForPlayer.values()) {
                totalMistakes += mistakes;
            }
        }

        return totalMistakes;
    }

    private static <T> Integer increment(T key, Integer oldValue) {
        return oldValue == null ? 1 : oldValue + 1;
    }
}
