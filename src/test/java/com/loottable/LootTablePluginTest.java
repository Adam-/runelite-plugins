package com.loottable;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LootTablePluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(LootTablePlugin.class);
		RuneLite.main(args);
	}
}