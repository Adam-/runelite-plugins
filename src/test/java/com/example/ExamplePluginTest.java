package com.example;

import info.sigterm.plugins.gpul.GpuPluginLegacy;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GpuPluginLegacy.class);
		RuneLite.main(args);
	}
}