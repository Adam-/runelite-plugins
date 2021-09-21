package com.larsvansoest.runelite.clueitems;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

@SuppressWarnings("unchecked")
public final class EmoteClueItemsPluginTest
{
	public static void main(final String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EmoteClueItemsPlugin.class);
		RuneLite.main(args);
	}
}