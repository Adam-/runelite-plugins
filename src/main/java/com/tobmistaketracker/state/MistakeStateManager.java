package com.tobmistaketracker.state;

import com.tobmistaketracker.TobMistake;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Set;

/**
 * In charge of the different MistakeManagers, and knowing which one is the currently viewed one.
 * <p>
 * For now, these are very small and writes are relatively infrequent, so let's write to disk for every write API.
 */
@Slf4j
@Singleton
public class MistakeStateManager {

    private final MistakeManager currentRaidMistakeManager;
    private final MistakeManager allRaidsMistakeManager;

    private transient boolean isAll;
    @Setter
    private transient MistakeStateWriter mistakeStateWriter;

    public MistakeStateManager(MistakeStateWriter mistakeStateWriter) {
        this.currentRaidMistakeManager = new MistakeManager();
        this.allRaidsMistakeManager = new MistakeManager();
        this.mistakeStateWriter = mistakeStateWriter;

        this.isAll = false;
    }

    public void addMistakeForPlayer(String playerName, TobMistake mistake) {
        // Always add to both
        currentRaidMistakeManager.addMistakeForPlayer(playerName, mistake);
        allRaidsMistakeManager.addMistakeForPlayer(playerName, mistake);

        mistakeStateWriter.write(this);
    }

    public void removeAllMistakesForPlayer(String playerName) {
        // Always remove from both
        currentRaidMistakeManager.removeAllMistakesForPlayer(playerName);
        allRaidsMistakeManager.removeAllMistakesForPlayer(playerName);

        mistakeStateWriter.write(this);
    }

    public void resetAll() {
        // Always clear from both
        currentRaidMistakeManager.clearAllMistakes();
        allRaidsMistakeManager.clearAllMistakes();

        mistakeStateWriter.write(this);
    }

    public void newRaid(Set<String> playerNames) {
        // Clear just the current mistakes
        currentRaidMistakeManager.clearAllMistakes();

        // Denote to the manager that there's a new raid, incrementing the raid count for all players in this raid
        // Raid count for current raid isn't valid, so only do for all raids
        allRaidsMistakeManager.newRaid(playerNames);

        mistakeStateWriter.write(this);
    }

    public Set<String> getPlayersWithMistakes() {
        return isAll ?
                allRaidsMistakeManager.getPlayersWithMistakes() :
                currentRaidMistakeManager.getPlayersWithMistakes();
    }

    public int getMistakeCountForPlayer(String playerName, TobMistake mistake) {
        return isAll ?
                allRaidsMistakeManager.getMistakeCountForPlayer(playerName, mistake) :
                currentRaidMistakeManager.getMistakeCountForPlayer(playerName, mistake);
    }

    public int getTotalMistakeCountForAllPlayers() {
        return isAll ?
                allRaidsMistakeManager.getTotalMistakeCountForAllPlayers() :
                currentRaidMistakeManager.getTotalMistakeCountForAllPlayers();
    }

    public int getRaidCountForPlayer(String playerName) {
        return isAll ? allRaidsMistakeManager.getRaidCountForPlayer(playerName) :
                0; // Raid count for current raid isn't valid, so return 0
    }

    public int getTrackedRaids() {
        return isAll ? allRaidsMistakeManager.getTrackedRaids() :
                1; // Tracked raids for current raid is always just the 1 raid
    }

    public void switchMistakes() {
        isAll = !isAll;
    }
}