package com.herblorerecipes;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HerbloreRecipesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HerbloreRecipesPlugin.class);
		RuneLite.main(args);
	}
}