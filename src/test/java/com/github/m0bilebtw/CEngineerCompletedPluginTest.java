package com.github.m0bilebtw;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CEngineerCompletedPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CEngineerCompletedPlugin.class);
		RuneLite.main(args);
	}
}