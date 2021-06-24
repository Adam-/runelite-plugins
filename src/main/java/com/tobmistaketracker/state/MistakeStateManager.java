package com.tobmistaketracker.state;

import com.tobmistaketracker.TobMistake;
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

    private int raidAttempts;

    private transient boolean isAll;
    private final transient boolean developerMode;

    public MistakeStateManager(boolean developerMode) {
        this.currentRaidMistakeManager = new MistakeManager();
        this.allRaidsMistakeManager = new MistakeManager();

        this.raidAttempts = 0;
        this.isAll = false;
        this.developerMode = developerMode;
    }

    public MistakeStateManager(MistakeStateManager other, boolean developerMode) {
        this.currentRaidMistakeManager = other.currentRaidMistakeManager;
        this.allRaidsMistakeManager = other.allRaidsMistakeManager;
        this.raidAttempts = other.raidAttempts;
        this.isAll = other.isAll;

        // Use the passed in developerMode
        this.developerMode = developerMode;
    }

    public void addMistakeForPlayer(String playerName, TobMistake mistake) {
        // Always add to both
        currentRaidMistakeManager.addMistakeForPlayer(playerName, mistake);
        allRaidsMistakeManager.addMistakeForPlayer(playerName, mistake);

        MistakeStateWriter.write(this, developerMode);
    }

    public void removeAllMistakesForPlayer(String playerName) {
        // Always remove from both
        currentRaidMistakeManager.removeAllMistakesForPlayer(playerName);
        allRaidsMistakeManager.removeAllMistakesForPlayer(playerName);

        MistakeStateWriter.write(this, developerMode);
    }

    public void resetAll() {
        // Always clear from both
        currentRaidMistakeManager.clearAllMistakes();
        allRaidsMistakeManager.clearAllMistakes();
        raidAttempts = 0;

        MistakeStateWriter.write(this, developerMode);
    }

    public void newRaid() {
        // Clear just the current mistakes
        currentRaidMistakeManager.clearAllMistakes();

        // Increment our raid attempts since this is now a new raid
        raidAttempts += 1;

        MistakeStateWriter.write(this, developerMode);
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

    public void switchMistakes() {
        isAll = !isAll;
    }
}