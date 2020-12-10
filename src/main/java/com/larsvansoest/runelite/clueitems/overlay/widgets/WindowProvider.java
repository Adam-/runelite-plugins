package com.larsvansoest.runelite.clueitems.overlay.widgets;

import static net.runelite.api.widgets.WidgetID.*;
import static com.larsvansoest.runelite.clueitems.overlay.widgets.Window.*;

public class WindowProvider
{
	public Window getWindow(int group)
	{
		switch (group)
		{
			case BANK_GROUP_ID:
			case BANK_INVENTORY_GROUP_ID:
				return Bank;

			case DEPOSIT_BOX_GROUP_ID:
				return DepositBox;

			case SHOP_GROUP_ID:
			case SHOP_INVENTORY_GROUP_ID:
				return Shop;

			case GUIDE_PRICES_INVENTORY_GROUP_ID:
				return GuidePrices;

			case SEED_VAULT_INVENTORY_GROUP_ID:
				return SeedVault;

			case EQUIPMENT_INVENTORY_GROUP_ID:
				return Equipment;

			case INVENTORY_GROUP_ID:
			case EQUIPMENT_GROUP_ID:
				return Default;

			default:
				return null;
		}
	}
}
