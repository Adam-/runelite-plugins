package com.larsvansoest.runelite.clueitems.overlay.config;

import com.larsvansoest.runelite.clueitems.EmoteClueItemsConfig;
import com.larsvansoest.runelite.clueitems.overlay.widgets.ItemWidgetContainer;

public class ConfigProvider
{
	private final EmoteClueItemsConfig config;

	public ConfigProvider(EmoteClueItemsConfig config)
	{
		this.config = config;
	}

	public boolean interfaceGroupSelected(ItemWidgetContainer container)
	{
		switch (container)
		{
			case Bank:
				return config.highlightBank();

			case DepositBox:
				return config.highlightDepositBox();

			case Inventory:
				return config.highlightInventory();

			case Equipment:
				return config.highlightEquipment();

			case Shop:
				return config.highlightShop();

			case KeptOnDeath:
				return config.highlightKeptOnDeath();

			case GuidePrices:
				return config.highlightGuidePrices();

			default:
				return false;
		}
	}
}