package com.tobmistaketracker;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
// TODO: Instead of a singleton, have one for all-time and one for the current raid.
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
        Map<TobMistake, Integer> playerMistakes = mistakesForPlayers.computeIfAbsent(playerName, k -> new HashMap<>());
        return playerMistakes.compute(mistake, MistakeManager::increment);
    }

    public boolean hasAnyMistakes(String playerName) {
        return mistakesForPlayers.containsKey(playerName);
    }

    public List<String> getPlayersWithMistakes() {
        return mistakesForPlayers.keySet().stream().sorted().collect(Collectors.toList());
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

    private static <T> Integer increment(T key, Integer oldValue) {
        return oldValue == null ? 1 : oldValue + 1;
    }
}
