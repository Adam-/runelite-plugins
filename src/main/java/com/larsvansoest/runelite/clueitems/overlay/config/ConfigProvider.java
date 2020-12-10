package com.larsvansoest.runelite.clueitems.overlay.config;

import com.larsvansoest.runelite.clueitems.EmoteClueItemsConfig;
import com.larsvansoest.runelite.clueitems.overlay.widgets.Container;

public class ConfigProvider
{
	private final EmoteClueItemsConfig config;

	public ConfigProvider(EmoteClueItemsConfig config)
	{
		this.config = config;
	}

	public boolean interfaceGroupSelected(Container container)
	{
		switch (container)
		{
			case Bank:
			case DepositBox:
				return config.highlightBank();

			case Inventory:
				return config.highlightInventory();

			case Equipment:
				return config.highlightEquipment();

			case Shop:
				return config.highlightShop();

			default:
				return false;
		}
	}
}