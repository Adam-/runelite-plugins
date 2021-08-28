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

package com.larsvansoest.runelite.clueitems.progress;

import com.larsvansoest.runelite.clueitems.data.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel;
import com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.cluescrolls.clues.item.AllRequirementsCollection;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;

import java.util.*;

/**
 * Monitors player owned items and subsequent changes to {@link ItemContainer} objects for Inventory, Bank and Equipment.
 * <p>
 * Uses {@link EmoteClueItemsPanel} to represent requirement statuses on {@link UpdatablePanel.Status} accordingly.
 *
 * @author Lars van Soest
 * @see EmoteClueItem
 * @since 2.0.0
 */
public class ProgressManager
{
	public final StashMonitor stashMonitor;
	private final Map<EmoteClueItem, UpdatablePanel.Status> statusMap;
	private final ItemMonitor itemsMonitor;
	private final EmoteClueItemsPanel panel;
	private final Client client;
	private final ClientThread clientThread;
	private boolean initialState;

	public ProgressManager(final EmoteClueItemsPanel panel, final Client client, final ClientThread clientThread, final StashMonitor stashMonitor)
	{
		this.statusMap = new HashMap<>(EmoteClueItem.values().length);
		this.itemsMonitor = new ItemMonitor();
		this.panel = panel;
		this.client = client;
		this.clientThread = clientThread;
		this.stashMonitor = stashMonitor;
		this.reset();
	}

	public void reset()
	{
		for (final EmoteClueItem emoteClueItem : EmoteClueItem.values())
		{
			this.statusMap.put(emoteClueItem, UpdatablePanel.Status.InComplete);
			this.panel.setEmoteClueItemStatus(emoteClueItem, UpdatablePanel.Status.InComplete);
			this.panel.setItemSlotStatus(emoteClueItem, 0);
			this.itemsMonitor.reset();
			this.initialState = true;
		}
	}

	public void handleEmoteClueItemChanges(final ItemContainerChanged event)
	{
		final int containerId = event.getContainerId();

		if (this.initialState && containerId == 95)
		{
			this.clientThread.invoke(() ->
			{
				final ItemContainer bankContainer = this.client.getItemContainer(InventoryID.BANK);
				if (bankContainer != null)
				{
					this.handleChanges(this.itemsMonitor.fetchEmoteClueItemChanges(95, bankContainer.getItems()));
					this.initialState = false;
				}
			});
		}
		else
		{
			this.handleChanges(this.itemsMonitor.fetchEmoteClueItemChanges(containerId, event.getItemContainer().getItems()));
		}
	}

	private void handleChanges(final List<Item> emoteClueItemChanges)
	{
		if (emoteClueItemChanges != null)
		{
			final LinkedList<Map.Entry<EmoteClueItem, UpdatablePanel.Status>> parents = new LinkedList<>();

			// Set single item (sub-)requirement status
			for (final Item item : emoteClueItemChanges)
			{
				final int quantity = item.getQuantity();
				final EmoteClueItem emoteClueItem = EmoteClueAssociations.ItemIdToEmoteClueItemSlot.get(item.getId());

				final UpdatablePanel.Status status = quantity > 0 ? UpdatablePanel.Status.Complete : UpdatablePanel.Status.InComplete;
				this.statusMap.put(emoteClueItem, status);
				parents.add(new AbstractMap.SimpleEntry<>(emoteClueItem, status));

				this.panel.setItemSlotStatus(emoteClueItem, quantity);
			}

			final LinkedList<Map.Entry<EmoteClueItem, UpdatablePanel.Status>> parentCache = new LinkedList<>();
			// Update requirement ancestors accordingly
			while (parents.size() > 0)
			{
				while (parents.size() > 0)
				{
					parentCache.add(parents.poll());
				}
				while (parentCache.size() > 0)
				{
					final Map.Entry<EmoteClueItem, UpdatablePanel.Status> childEntry = parentCache.poll();
					final EmoteClueItem child = childEntry.getKey();
					final UpdatablePanel.Status status = childEntry.getValue();
					this.panel.setEmoteClueItemStatus(child, status);
					for (final EmoteClueItem parent : child.getParents())
					{
						final UpdatablePanel.Status parentStatus = this.getParentStatus(parent);
						parents.add(new AbstractMap.SimpleEntry<>(parent, parentStatus));
						this.statusMap.put(parent, parentStatus);
					}
				}
			}
		}
	}

	private UpdatablePanel.Status getParentStatus(final EmoteClueItem parent)
	{
		final ItemRequirement parentRequirement = parent.getItemRequirement();
		final List<EmoteClueItem> children = parent.getChildren();
		return (parentRequirement instanceof AllRequirementsCollection) ? this.getParentAllStatus(children) : this.getParentAnyStatus(children);
	}

	private UpdatablePanel.Status getParentAnyStatus(final List<EmoteClueItem> children)
	{
		for (final EmoteClueItem child : children)
		{
			if (this.statusMap.get(child) == UpdatablePanel.Status.Complete)
			{
				return UpdatablePanel.Status.Complete;
			}
		}
		return UpdatablePanel.Status.InComplete;
	}

	private UpdatablePanel.Status getParentAllStatus(final List<EmoteClueItem> children)
	{
		boolean anyMatch = false;
		boolean allMatch = true;
		for (final EmoteClueItem child : children)
		{
			if (this.statusMap.get(child) == UpdatablePanel.Status.Complete)
			{
				anyMatch = true;
			}
			else
			{
				allMatch = false;
			}
		}
		return allMatch ? UpdatablePanel.Status.Complete : anyMatch ? UpdatablePanel.Status.InProgress : UpdatablePanel.Status.InComplete;
	}
}
