package com.tobmistaketracker;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
// TODO: Instead of a singleton, have one for all-time and one for the current raid.
public class MistakeManager {

    private final Map<String, Map<TobMistake, Integer>> mistakesForPlayers;

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

    public Map<TobMistake, Integer> removeMistakesForPlayer(String playerName) {
        return mistakesForPlayers.remove(playerName);
    }

    public Integer removeMistakeForPlayer(String playerName, TobMistake mistake) {
        Map<TobMistake, Integer> playerMistakes = mistakesForPlayers.get(playerName);
        if (playerMistakes != null) {
            return playerMistakes.remove(mistake);
        }

        return null;
    }

    public boolean hasAnyMistakes(String playerName) {
        return mistakesForPlayers.containsKey(playerName);
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

    public int getTotalMistakesForAllPlayers() {
        int totalMistakes = 0;
        for (Map<TobMistake, Integer> mistakesForPlayer : mistakesForPlayers.values()) {
            for (int mistakes : mistakesForPlayer.values()) {
                totalMistakes += mistakes;
            }
        }

        return totalMistakes;
    }

//    public Iterable<TobMistake> getMistakesForPlayer(String playerName) {
//        if (hasAnyMistakes(playerName)) {
//            return Collections.unmodifiableList(mistakesForPlayers.get(playerName))
//        }
//
//        return Collections.emptyList();
//    }

    private static <T> Integer increment(T key, Integer oldValue) {
        return oldValue == null ? 1 : oldValue + 1;
    }
}
