package com.example;

import info.sigterm.plugins.fossilisland.FossilIslandPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FossilIslandPlugin.class);
		RuneLite.main(args);
	}
}