package com.regionlocker;

import com.goaltracker.GoalTrackerPlugin;
import com.gpu.RegionGpuPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RegionLockerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RegionLockerPlugin.class, RegionGpuPlugin.class, GoalTrackerPlugin.class);
		RuneLite.main(args);
	}
}