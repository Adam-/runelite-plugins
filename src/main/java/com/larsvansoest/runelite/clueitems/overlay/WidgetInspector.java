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

package com.larsvansoest.runelite.clueitems.overlay;

import net.runelite.api.widgets.WidgetItem;

/**
 * Utility class with Runescape item container widget inspection functionality.
 *
 * @author Lars van Soest
 * @since 1.2.0
 */
public abstract class WidgetInspector
{
	/**
	 * Identifies the origin of given widgetItem, writes found data into given {@link WidgetData} object. The method iterates over the ancestors (parents of parents), and compares it to the ids of all entries of the {@link Widget} enum.
	 *
	 * @param widgetItem    the {@link WidgetItem} to analyse.
	 * @param widgetDataRef the {@link WidgetData} to write found data to.
	 * @param maxDepth      the maximum steps from initial widgetItem parameter to one of the parents specified.
	 * @since 1.2.0
	 */
	public static void Inspect(final WidgetItem widgetItem, final WidgetData widgetDataRef, final int maxDepth)
	{
		net.runelite.api.widgets.Widget widget = widgetItem.getWidget();

		widgetDataRef.setWidgetContainer(null);
		widgetDataRef.setWidgetContext(null);

		int i = 0;
		while (i < maxDepth && widget != null)
		{
			final int id = widget.getId();

			if (id == Widget.BANK.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InBank);
				widgetDataRef.setWidgetContainer(WidgetContainer.Bank);
				return;
			}
			else if (id == Widget.BANK_EQUIPMENT.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InBank);
				widgetDataRef.setWidgetContainer(WidgetContainer.Equipment);
				return;
			}
			else if (id == Widget.BANK_EQUIPMENT_INVENTORY.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InBank);
				widgetDataRef.setWidgetContainer(WidgetContainer.Inventory);
				return;
			}
			else if (id == Widget.BANK_INVENTORY.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InBank);
				widgetDataRef.setWidgetContainer(WidgetContainer.Inventory);
				return;
			}

			else if (id == Widget.EQUIPMENT.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.Default);
				widgetDataRef.setWidgetContainer(WidgetContainer.Equipment);
				return;
			}
			else if (id == Widget.EQUIPMENT_EQUIPMENT.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InEquipment);
				widgetDataRef.setWidgetContainer(WidgetContainer.Equipment);
				return;
			}
			else if (id == Widget.EQUIPMENT_INVENTORY.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InEquipment);
				widgetDataRef.setWidgetContainer(WidgetContainer.Inventory);
				return;
			}

			else if (id == Widget.DEPOSIT_BOX.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InDepositBox);
				widgetDataRef.setWidgetContainer(WidgetContainer.DepositBox);
				return;
			}

			else if (id == Widget.GUIDE_PRICES.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InGuidePrices);
				widgetDataRef.setWidgetContainer(WidgetContainer.GuidePrices);
				return;
			}
			else if (id == Widget.GUIDE_PRICES_INVENTORY.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InGuidePrices);
				widgetDataRef.setWidgetContainer(WidgetContainer.Inventory);
				return;
			}

			else if (id == Widget.INVENTORY.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.Default);
				widgetDataRef.setWidgetContainer(WidgetContainer.Inventory);
				return;
			}

			else if (id == Widget.KEPT_ON_DEATH.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InKeptOnDeath);
				widgetDataRef.setWidgetContainer(WidgetContainer.KeptOnDeath);
				return;
			}

			else if (id == Widget.SHOP.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InShop);
				widgetDataRef.setWidgetContainer(WidgetContainer.Shop);
				return;
			}

			else if (id == Widget.SHOP_INVENTORY.id)
			{
				widgetDataRef.setWidgetContext(WidgetContext.InShop);
				widgetDataRef.setWidgetContainer(WidgetContainer.Inventory);
				return;
			}

			widget = widget.getParent();
			i++;
		}
	}
}