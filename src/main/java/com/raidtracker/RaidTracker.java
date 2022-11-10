package com.raidtracker;

import com.raidtracker.utils.UniqueDrop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RaidTracker {
    boolean chestOpened = false;
    boolean raidComplete = false;
    boolean loggedIn = false;
    boolean challengeMode = false;
    boolean inRaid = false;
    /*
    boolean inRaidCox = false;
    boolean inRaidTob = false;
    boolean inRaidToa = false;
    */
    // Global
    public int[] roomTimes = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    int teamSize = -1;
    int completionCount = -1;
    ArrayList<UniqueDrop> Uniques = new ArrayList<>();
    ArrayList<UniqueDrop> nTradables = new ArrayList<>();
    ArrayList<UniqueDrop> pets = new ArrayList<>();

    int inRaidType = -1;
    int raidTime = -1;
    int totalPoints = -1;
    int personalPoints = -1;
    double percentage = -1.0;
    ArrayList<RaidTrackerItem> lootList = new ArrayList<>();

    String mvp= "";
    boolean mvpInOwnName = false;
    int[] tobDeaths = {0,0,0,0,0};
    String[] tobPlayers = {"", "", "", "", ""};

    // toa specific
    int invocation = -1;
    //Every RaidTracker has a unique uniqueID but not necessarily a unique killCountID, if there are multiple drops.
    String uniqueID = UUID.randomUUID().toString();
    String killCountID = UUID.randomUUID().toString();
    long date = System.currentTimeMillis();
}
