package com.theatrepoints;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TheatrePointsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TheatrePointsPlugin.class);
		RuneLite.main(args);
	}
}