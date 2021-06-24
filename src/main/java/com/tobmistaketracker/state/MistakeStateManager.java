package com.tobmistaketracker.state;

import com.tobmistaketracker.TobMistake;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Set;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

/**
 * In charge of the different MistakeManagers, and knowing which one is the currently viewed one.
 *
 * For now, these are very small and writes are relatively infrequent, so let's write to disk for every write API.
 */
@Slf4j
@Singleton
public class MistakeStateManager {

    static final Path MISTAKE_STATE_DIR = RUNELITE_DIR.toPath().resolve("tob-mistake-tracker");
    // Use the same mistake state file regardless of user
    static final Path MISTAKE_STATE_FILE_PATH = MISTAKE_STATE_DIR.resolve("mistake-state.txt");

    private final MistakeManager currentRaidMistakeManager;
    private final MistakeManager allRaidsMistakeManager;

    private int raidAttempts;

    private transient boolean isAll;

    public MistakeStateManager() {
        this.currentRaidMistakeManager = new MistakeManager();
        this.allRaidsMistakeManager = new MistakeManager();

        this.raidAttempts = 0;
        this.isAll = false;
    }

    public void addMistakeForPlayer(String playerName, TobMistake mistake) {
        // Always add to both
        currentRaidMistakeManager.addMistakeForPlayer(playerName, mistake);
        allRaidsMistakeManager.addMistakeForPlayer(playerName, mistake);

        MistakeStateWriter.write(this);
    }

    public void removeAllMistakesForPlayer(String playerName) {
        // Always remove from both
        currentRaidMistakeManager.removeAllMistakesForPlayer(playerName);
        allRaidsMistakeManager.removeAllMistakesForPlayer(playerName);

        MistakeStateWriter.write(this);
    }

    public void resetAll() {
        // Always clear from both
        currentRaidMistakeManager.clearAllMistakes();
        allRaidsMistakeManager.clearAllMistakes();
        raidAttempts = 0;

        MistakeStateWriter.write(this);
    }

    public void newRaid() {
        // Clear just the current mistakes
        currentRaidMistakeManager.clearAllMistakes();

        // Increment our raid attempts since this is now a new raid
        raidAttempts += 1;

        MistakeStateWriter.write(this);
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