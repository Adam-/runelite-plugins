package com.raidtracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunClientAndPlugin {
    public static void main(String... args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(RaidTrackerPlugin.class);
        RuneLite.main(args);
    }
}
