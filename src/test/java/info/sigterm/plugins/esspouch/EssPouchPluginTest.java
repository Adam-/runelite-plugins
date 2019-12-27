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

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.Notifier;
import net.runelite.client.ui.overlay.OverlayManager;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EssPouchPluginTest
{
	@Mock
	@Bind
	Client client;

	@Mock
	@Bind
	OverlayManager overlayManager;

	@Mock
	@Bind
	EssencePouchOverlay essencePouchOverlay;

	@Mock
	@Bind
	Notifier notifier;

	@Inject
	EssPouchPlugin runecraftPlugin;

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	@Test
	public void testPouches()
	{
		ItemContainer itemContainer = mock(ItemContainer.class);
		when(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(itemContainer);

		// Add 4 pouches
		when(itemContainer.getItems()).thenReturn(new Item[]{
			new Item(ItemID.SMALL_POUCH, 1),
			new Item(ItemID.MEDIUM_POUCH, 1),
			new Item(ItemID.LARGE_POUCH, 1),
			new Item(ItemID.GIANT_POUCH, 1),
		});

		// Start all pouches off full
		for (Pouch pouch : Pouch.values())
		{
			pouch.setHolding(pouch.getHoldAmount());
			pouch.setUnknown(true);
		}

		// Initialize last counters
		ItemContainerChanged itemContainerChanged = new ItemContainerChanged(InventoryID.INVENTORY.getId(), itemContainer);
		runecraftPlugin.onItemContainerChanged(itemContainerChanged);

		// Empty small
		MenuOptionClicked menuOptionClicked = new MenuOptionClicked();
		menuOptionClicked.setMenuAction(MenuAction.ITEM_FIRST_OPTION);
		menuOptionClicked.setId(ItemID.SMALL_POUCH);
		menuOptionClicked.setMenuOption("Empty");
		runecraftPlugin.onMenuOptionClicked(menuOptionClicked);

		// <server tick here>

		// Empty medium
		menuOptionClicked = new MenuOptionClicked();
		menuOptionClicked.setMenuAction(MenuAction.ITEM_FIRST_OPTION);
		menuOptionClicked.setId(ItemID.MEDIUM_POUCH);
		menuOptionClicked.setMenuOption("Empty");
		runecraftPlugin.onMenuOptionClicked(menuOptionClicked);

		// Update inventory with 3 pess
		when(itemContainer.getItems()).thenReturn(new Item[]{
			new Item(ItemID.SMALL_POUCH, 1),
			new Item(ItemID.MEDIUM_POUCH, 1),
			new Item(ItemID.LARGE_POUCH, 1),
			new Item(ItemID.GIANT_POUCH, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
		});
		// Response from small empty
		runecraftPlugin.onItemContainerChanged(itemContainerChanged);

		assertEquals(0, Pouch.SMALL.getHolding());
		assertEquals(6, Pouch.MEDIUM.getHolding());

		// Add 6 more pess
		when(itemContainer.getItems()).thenReturn(new Item[]{
			new Item(ItemID.SMALL_POUCH, 1),
			new Item(ItemID.MEDIUM_POUCH, 1),
			new Item(ItemID.LARGE_POUCH, 1),
			new Item(ItemID.GIANT_POUCH, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
			new Item(ItemID.PURE_ESSENCE, 1),
		});
		// Response from medium empty
		runecraftPlugin.onItemContainerChanged(itemContainerChanged);

		assertEquals(0, Pouch.SMALL.getHolding());
		assertEquals(0, Pouch.MEDIUM.getHolding());
	}
}