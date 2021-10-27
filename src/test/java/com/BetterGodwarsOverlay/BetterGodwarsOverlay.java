package com.BetterGodwarsOverlay;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BetterGodwarsOverlay
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BetterGodwarsOverlayPlugin.class);
		RuneLite.main(args);
	}
}