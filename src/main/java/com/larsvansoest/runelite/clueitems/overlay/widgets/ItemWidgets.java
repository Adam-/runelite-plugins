package com.larsvansoest.runelite.clueitems.overlay.widgets;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;

public abstract class ItemWidgets
{
	/**
	 * Identifies the origin of given widgetItem, writes found data into given {@link ItemWidgetData} object. The method iterates over the ancestors (parents of parents), and compares it to the ids of all entries of the {@link ItemWidget} enum.
	 *
	 * @param widgetItem    the {@link WidgetItem} to analyse.
	 * @param widgetDataRef the {@link ItemWidgetData} to write found data to.
	 * @param maxDepth      the maximum steps from initial widgetItem parameter to one of the parents specified.
	 * @since 1.2.0
	 */
	public static void Inspect(final WidgetItem widgetItem, final ItemWidgetData widgetDataRef, final int maxDepth)
	{
		Widget widget = widgetItem.getWidget();

		widgetDataRef.setContainer(null);
		widgetDataRef.setContext(null);

		int i = 0;
		while (i < maxDepth && widget != null)
		{
			int id = widget.getId();

			if (id == ItemWidget.BANK.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InBank);
				widgetDataRef.setContainer(ItemWidgetContainer.Bank);
				return;
			}
			else if (id == ItemWidget.BANK_EQUIPMENT.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InBank);
				widgetDataRef.setContainer(ItemWidgetContainer.Equipment);
				return;
			}
			else if (id == ItemWidget.BANK_EQUIPMENT_INVENTORY.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InBank);
				widgetDataRef.setContainer(ItemWidgetContainer.Inventory);
				return;
			}
			else if (id == ItemWidget.BANK_INVENTORY.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InBank);
				widgetDataRef.setContainer(ItemWidgetContainer.Inventory);
				return;
			}

			else if (id == ItemWidget.EQUIPMENT.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.Default);
				widgetDataRef.setContainer(ItemWidgetContainer.Equipment);
				return;
			}
			else if (id == ItemWidget.EQUIPMENT_EQUIPMENT.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InEquipment);
				widgetDataRef.setContainer(ItemWidgetContainer.Equipment);
				return;
			}
			else if (id == ItemWidget.EQUIPMENT_INVENTORY.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InEquipment);
				widgetDataRef.setContainer(ItemWidgetContainer.Inventory);
				return;
			}

			else if (id == ItemWidget.DEPOSIT_BOX.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InDepositBox);
				widgetDataRef.setContainer(ItemWidgetContainer.DepositBox);
				return;
			}

			else if (id == ItemWidget.GUIDE_PRICES.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InGuidePrices);
				widgetDataRef.setContainer(ItemWidgetContainer.GuidePrices);
				return;
			}
			else if (id == ItemWidget.GUIDE_PRICES_INVENTORY.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InGuidePrices);
				widgetDataRef.setContainer(ItemWidgetContainer.Inventory);
				return;
			}

			else if (id == ItemWidget.INVENTORY.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.Default);
				widgetDataRef.setContainer(ItemWidgetContainer.Inventory);
				return;
			}

			else if (id == ItemWidget.KEPT_ON_DEATH.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InKeptOnDeath);
				widgetDataRef.setContainer(ItemWidgetContainer.KeptOnDeath);
				return;
			}

			else if (id == ItemWidget.SHOP.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InShop);
				widgetDataRef.setContainer(ItemWidgetContainer.Shop);
				return;
			}

			else if (id == ItemWidget.SHOP_INVENTORY.id)
			{
				widgetDataRef.setContext(ItemWidgetContext.InShop);
				widgetDataRef.setContainer(ItemWidgetContainer.Inventory);
				return;
			}

			widget = widget.getParent();
			i++;
		}
		return;
	}
}