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
    boolean inTheatreOfBlood = false;
    boolean FreeForAll = false;

    int upperTime = -1;
    int middleTime = -1;
    int lowerTime = -1;
    int raidTime = -1;

    int shamansTime = -1;
    int vasaTime = -1;
    int vanguardsTime = -1;
    int mysticsTime = -1;
    int tektonTime = -1;
    int muttadilesTime = -1;
    int guardiansTime = -1;
    int vespulaTime = -1;
    int iceDemonTime = -1;
    int thievingTime = -1;
    int tightropeTime = -1;
    int crabsTime = -1;

    int totalPoints = -1;
    int personalPoints = -1;
    int teamSize = -1;
    double percentage = -1.0;
    int completionCount = -1;
    String specialLoot = "";
    String specialLootReceiver = "";
    boolean specialLootInOwnName = false;
    int specialLootValue = -1;
    String kitReceiver = "";
    String dustReceiver = "";
    String petReceiver = "";
    boolean petInMyName = false;
    int lootSplitReceived = -1;
    int lootSplitPaid = -1;
    ArrayList<RaidTrackerItem> lootList = new ArrayList<>();

    int maidenTime = -1;
    int bloatTime = -1;
    int nyloTime = -1;
    int sotetsegTime = -1;
    int xarpusTime = -1;
    int verzikTime = -1;

    String mvp= "";
    boolean mvpInOwnName = false;
    String tobPlayer1 = "";
    String tobPlayer2 = "";
    String tobPlayer3 = "";
    String tobPlayer4 = "";
    String tobPlayer5 = "";

    int tobPlayer1DeathCount = 0;
    int tobPlayer2DeathCount = 0;
    int tobPlayer3DeathCount = 0;
    int tobPlayer4DeathCount = 0;
    int tobPlayer5DeathCount = 0;

    //Every RaidTracker has a unique uniqueID but not necessarily a unique killCountID, if there are multiple drops.
    String uniqueID = UUID.randomUUID().toString();
    String killCountID = UUID.randomUUID().toString();
    long date = System.currentTimeMillis();
}
