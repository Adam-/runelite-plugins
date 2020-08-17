package com.example;

import info.sigterm.plugins.gpue.GpuPluginExperimental;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GpuPluginExperimental.class);
		RuneLite.main(args);
	}
}