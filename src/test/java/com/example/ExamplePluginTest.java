package com.example;

import info.sigterm.plugins.gpulegacy.GpuPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GpuPlugin.class);
		RuneLite.main(args);
	}
}