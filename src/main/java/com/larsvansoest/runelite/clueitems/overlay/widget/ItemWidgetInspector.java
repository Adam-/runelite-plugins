/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, Lars van Soest
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.larsvansoest.runelite.clueitems.overlay.widget;

import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;

public abstract class ItemWidgetInspector
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
	}
}