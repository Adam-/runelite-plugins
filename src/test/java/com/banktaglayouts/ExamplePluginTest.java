package com.banktaglayouts;

import inventorysetups.InventorySetupsPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankTagLayoutsPlugin.class, InventorySetupsPlugin.class);
		RuneLite.main(args);
	}
}