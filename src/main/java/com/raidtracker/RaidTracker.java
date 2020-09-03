package com.raidtracker;

import lombok.Data;

import java.util.ArrayList;
import java.util.UUID;

@Data
public class RaidTracker {

    boolean chestOpened = false;
    boolean raidComplete = false;
    boolean loggedIn = false;
    boolean challengeMode = false;
    boolean inRaidChambers = false;
    boolean FreeForAll = false;

    int upperTime = -1;
    int middleTime = -1;
    int lowerTime = -1;
    int raidTime = -1;
    int totalPoints = -1;
    int personalPoints = -1;
    int teamSize = -1;
    double percentage = -1.0;
    int completionCount = -1;
    String specialLoot = "";
    String specialLootReceiver = "";
    int specialLootValue = -1;
    String kitReceiver = "";
    String dustReceiver = "";
    int lootSplitReceived = -1;
    int lootSplitPaid = -1;
    ArrayList<RaidTrackerItem> lootList = new ArrayList<>();
    String uniqueID = UUID.randomUUID().toString();
    long date = System.currentTimeMillis();
}
