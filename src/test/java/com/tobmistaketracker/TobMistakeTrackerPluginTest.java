package com.tobmistaketracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TobMistakeTrackerPluginTest {

    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(TobMistakeTrackerPlugin.class);
        RuneLite.main(args);
    }
}