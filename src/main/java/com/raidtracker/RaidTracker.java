package com.raidtracker;

import lombok.Data;

import java.util.ArrayList;

@Data
public class RaidTracker {

    boolean chestOpened;
    boolean raidComplete;
    boolean loggedIn;
    boolean challengeMode;

    int upperTime;
    int middleTime;
    int lowerTime;
    int raidTime;
    int totalPoints;
    int personalPoints;
    int teamSize;
    double percentage;
    String specialLoot;
    String specialLootReceiver;
    int specialLootValue;
    String kitReceiver;
    String dustReceiver;
    int lootSplitReceived;
    int lootSplitPaid;
    ArrayList<RaidTrackerItem> lootList;

}
