/*
 * Copyright (c) 2019 Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package info.sigterm.plugins.esspouch;

import com.google.common.collect.ImmutableMap;
import static java.lang.Math.min;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetInfo.TO_CHILD;
import static net.runelite.api.widgets.WidgetInfo.TO_GROUP;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Essence Pouch",
	description = "Shows how many essence are in an essence pouch",
	tags = {"ess", "runecraft"}
)
public class EssPouchPlugin extends Plugin
{
	private static final int INVENTORY_SIZE = 28;
	private static final int GOTR_WIDGET_ID = 48889876;

	private static final Pattern POUCH_CHECK_MESSAGE = Pattern.compile("^There (?:is|are) ([a-z-]+)(?: pure| daeyalt| guardian)? essences? in this pouch\\.$");
	private static final ImmutableMap<String, Integer> TEXT_TO_NUMBER = ImmutableMap.<String, Integer>builder()
		.put("no", 0)
		.put("one", 1)
		.put("two", 2)
		.put("three", 3)
		.put("four", 4)
		.put("five", 5)
		.put("six", 6)
		.put("seven", 7)
		.put("eight", 8)
		.put("nine", 9)
		.put("ten", 10)
		.put("eleven", 11)
		.put("twelve", 12)
		.put("thirteen", 13)
		.put("fourteen", 14)
		.put("fifteen", 15)
		.put("sixteen", 16)
		.put("seventeen", 17)
		.put("eighteen", 18)
		.put("nineteen", 19)
		.put("twenty", 20)
		.put("twenty-one", 21)
		.put("twenty-two", 22)
		.put("twenty-three", 23)
		.put("twenty-four", 24)
		.put("twenty-five", 25)
		.put("twenty-six", 26)
		.put("twenty-seven", 27)
		.put("twenty-eight", 28)
		.put("twenty-nine", 29)
		.put("thirty", 30)
		.put("thirty-one", 31)
		.put("thirty-two", 32)
		.put("thirty-three", 33)
		.put("thirty-four", 34)
		.put("thirty-five", 35)
		.put("thirty-six", 36)
		.put("thirty-seven", 37)
		.put("thirty-eight", 38)
		.put("thirty-nine", 39)
		.put("forty", 40)
		.build();

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private EssencePouchOverlay essencePouchOverlay;

	private final Deque<ClickOperation> clickedItems = new ArrayDeque<>();
	private final Deque<ClickOperation> checkedPouches = new ArrayDeque<>();
	private int lastEssence;
	private int lastSpace;
	private boolean gotrStarted;

	@Override
	protected void startUp()
	{
		overlayManager.add(essencePouchOverlay);

		// Reset pouch state
		for (Pouch pouch : Pouch.values())
		{
			pouch.setHolding(0);
			pouch.setUnknown(true);
			pouch.degrade(false);
		}

		lastEssence = lastSpace = -1;
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(essencePouchOverlay);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		// Clear pouches when GotR starts.
		if (event.getMessage().contains("The rift becomes active!"))
		{
			gotrStarted = true;
			for (Pouch pouch : Pouch.values())
			{
				pouch.setHolding(0);
				pouch.setUnknown(false);
			}
		}


		if (!checkedPouches.isEmpty())
		{
			Matcher matcher = POUCH_CHECK_MESSAGE.matcher(event.getMessage());
			if (matcher.matches())
			{
				final int num = TEXT_TO_NUMBER.get(matcher.group(1));
				// Keep getting operations until we get a valid one
				do
				{
					final ClickOperation op = checkedPouches.pop();
					if (op.tick >= client.getTickCount())
					{
						Pouch pouch = op.pouch;
						pouch.setHolding(num);
						pouch.setUnknown(false);
						break;
					}
				}
				while (!checkedPouches.isEmpty());
			}
		}
	}

	public boolean isInGotr()
	{
		Widget gotrWidget = client.getWidget(GOTR_WIDGET_ID);
		return gotrWidget != null;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (InventoryID.INVENTORY.getId() != event.getContainerId())
		{
			return;
		}

		// empty pouches if you left GotR
		if (gotrStarted && !isInGotr()) {
			gotrStarted = false;
			for (Pouch pouch : Pouch.values())
			{
				pouch.setHolding(0);
				pouch.setUnknown(false);
			}
		}

		final Item[] items = event.getItemContainer().getItems();

		int newEss = 0;
		int newSpace = 0;

		// Count ess/space, and change pouch states
		for (Item item : items)
		{
			switch (item.getId())
			{
				case ItemID.PURE_ESSENCE:
				case ItemID.DAEYALT_ESSENCE:
				case ItemID.GUARDIAN_ESSENCE:
					newEss += 1;
					break;
				case -1:
					newSpace += 1;
					break;
				case ItemID.MEDIUM_POUCH:
				case ItemID.LARGE_POUCH:
				case ItemID.GIANT_POUCH:
				case ItemID.COLOSSAL_POUCH:
					Pouch pouch = Pouch.forItem(item.getId());
					pouch.degrade(false);
					break;
				case ItemID.MEDIUM_POUCH_5511:
				case ItemID.LARGE_POUCH_5513:
				case ItemID.GIANT_POUCH_5515:
				case ItemID.COLOSSAL_POUCH_26786:
					pouch = Pouch.forItem(item.getId());
					pouch.degrade(true);
					break;
			}
		}
		if (items.length < INVENTORY_SIZE)
		{
			// Pad newSpace for unallocated inventory slots
			newSpace += INVENTORY_SIZE - items.length;
		}

		if (clickedItems.isEmpty())
		{
			lastSpace = newSpace;
			lastEssence = newEss;
			return;
		}

		if (lastEssence == -1 || lastSpace == -1)
		{
			lastSpace = newSpace;
			lastEssence = newEss;
			clickedItems.clear();
			return;
		}

		final int tick = client.getTickCount();

		int essence = lastEssence;
		int space = lastSpace;

		log.debug("Begin processing {} events, last ess: {} space: {}, cur ess {}: space {}", clickedItems.size(), lastEssence, lastSpace, newEss, newSpace);

		while (essence != newEss)
		{
			ClickOperation op = clickedItems.poll();
			if (op == null)
			{
				log.debug("Ran out of updates while trying to balance essence!");
				break;
			}

			if (tick > op.tick)
			{
				log.debug("Click op timed out");
				continue;
			}

			Pouch pouch = op.pouch;

			final boolean fill = op.delta > 0;
			// How much ess can either be deposited or withdrawn
			final int required = fill ? pouch.getRemaining() : pouch.getHolding();
			// Bound to how much ess or free space we actually have, and optionally negate
			final int essenceGot = op.delta * min(required, fill ? essence : space);

			// if we have enough essence or space to fill or empty the entire pouch, it no
			// longer becomes unknown
			if (pouch.isUnknown() && (fill ? essence : space) >= pouch.getHoldAmount())
			{
				pouch.setUnknown(false);
			}

			log.debug("{}: {}", pouch.name(), essenceGot);

			essence -= essenceGot;
			space += essenceGot;

			pouch.addHolding(essenceGot);
		}

		if (!clickedItems.isEmpty())
		{
			log.debug("End processing with {} events left", clickedItems.size());
		}

		lastSpace = newSpace;
		lastEssence = newEss;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		int itemId = -1;
		switch (event.getMenuAction())
		{
			case ITEM_FIRST_OPTION:
			case ITEM_SECOND_OPTION:
			case ITEM_THIRD_OPTION:
			case ITEM_FOURTH_OPTION:
			case ITEM_FIFTH_OPTION:
			case GROUND_ITEM_THIRD_OPTION: // Take
				itemId = event.getId();
				break;
			case CC_OP:
			case CC_OP_LOW_PRIORITY:
				int widgetId = event.getWidgetId();
				Widget widget = client.getWidget(TO_GROUP(widgetId), TO_CHILD(widgetId));
				if (widget != null)
				{
					int child = event.getActionParam();
					if (child == -1)
					{
						return;
					}

					widget = widget.getChild(child);
					if (widget != null)
					{
						itemId = widget.getItemId();
					}
				}
				break;
			default:
				return;
		}

		if (itemId == -1)
		{
			return;
		}

		final Pouch pouch = Pouch.forItem(itemId);
		if (pouch == null)
		{
			return;
		}

		final int tick = client.getTickCount() + 3;
		switch (event.getMenuOption())
		{
			case "Fill":
				clickedItems.add(new ClickOperation(pouch, tick, 1));
				break;
			case "Empty":
				clickedItems.add(new ClickOperation(pouch, tick, -1));
				break;
			case "Check":
				checkedPouches.add(new ClickOperation(pouch, tick));
				break;
			case "Take":
				// Dropping pouches clears them, so clear when picked up
				pouch.setHolding(0);
				break;
		}
	}
}
