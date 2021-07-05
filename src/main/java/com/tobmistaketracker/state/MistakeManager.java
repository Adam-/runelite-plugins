package com.tobmistaketracker.state;

import com.tobmistaketracker.TobMistake;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keeps track of mistakes for players
 */
class MistakeManager {

    private final Map<String, PlayerTrackingInfo> trackingInfo;
    private int trackedRaids;

    MistakeManager() {
        trackingInfo = new HashMap<>();
        trackedRaids = 0;
    }

    public void clearAllMistakes() {
        trackingInfo.clear();
        trackedRaids = 0;
    }

    public void addMistakeForPlayer(String playerName, TobMistake mistake) {
        PlayerTrackingInfo playerInfo = trackingInfo.computeIfAbsent(playerName,
                k -> new PlayerTrackingInfo(playerName));
        playerInfo.incrementMistake(mistake);
    }

    public void newRaid(Set<String> playerNames) {
        trackedRaids++;

        for (String playerName : playerNames) {
            PlayerTrackingInfo playerInfo = trackingInfo.get(playerName);
            if (playerInfo != null) {
                playerInfo.incrementRaidCount();
            } else {
                trackingInfo.put(playerName, new PlayerTrackingInfo(playerName));
            }
        }
    }

    public void removeAllMistakesForPlayer(String playerName) {
        trackingInfo.remove(playerName);
    }

    public Set<String> getPlayersWithMistakes() {
        return trackingInfo.values().stream()
                .filter(PlayerTrackingInfo::hasMistakes)
                .map(PlayerTrackingInfo::getPlayerName)
                .collect(Collectors.toSet());
    }

    public int getMistakeCountForPlayer(String playerName, TobMistake mistake) {
        PlayerTrackingInfo playerInfo = trackingInfo.get(playerName);
        if (playerInfo != null) {
            Integer count = playerInfo.getMistakes().get(mistake);
            if (count != null) {
                return count;
            }
        }

        return 0;
    }

    public int getTotalMistakeCountForAllPlayers() {
        int totalMistakes = 0;
        for (PlayerTrackingInfo playerInfo : trackingInfo.values()) {
            for (int mistakes : playerInfo.getMistakes().values()) {
                totalMistakes += mistakes;
            }
        }

        return totalMistakes;
    }

    public int getRaidCountForPlayer(String playerName) {
        PlayerTrackingInfo playerInfo = trackingInfo.get(playerName);
        if (playerInfo != null) {
            return playerInfo.getRaidCount();
        }

        return 0;
    }

    public int getTrackedRaids() {
        return trackedRaids;
    }
}
