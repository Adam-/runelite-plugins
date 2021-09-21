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
import net.runelite.api.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class InventoryMonitor
{
	private final ItemTracker inventoryTracker;
	private final ItemTracker bankTracker;
	private final ItemTracker equipmentTracker;
	private final HashMap<Integer, Integer> collectionLog;

	public InventoryMonitor()
	{
		this.inventoryTracker = new ItemTracker(28);
		this.bankTracker = new ItemTracker(816);
		this.equipmentTracker = new ItemTracker(13);
		this.collectionLog = new HashMap<>(EmoteClueAssociations.ItemIdToEmoteClueItem.keySet().size());
		this.reset();
	}

	public void reset()
	{
		for (final Integer itemId : EmoteClueAssociations.ItemIdToEmoteClueItem.keySet())
		{
			this.collectionLog.put(itemId, 0);
		}
		this.inventoryTracker.reset();
		this.bankTracker.reset();
		this.equipmentTracker.reset();
	}

	public List<Item> fetchEmoteClueItemChanges(final int containerId, final Item[] items)
	{
		final List<Item> deltas;
		switch (containerId)
		{
			case 93:
				deltas = this.inventoryTracker.writeDeltas(items);
				break;
			case 94:
				deltas = this.equipmentTracker.writeDeltas(items);
				break;
			case 95:
				deltas = this.bankTracker.writeDeltas(items);
				break;
			default:
				return null;
		}

		final List<Item> emoteClueDeltas = new ArrayList<>();
		for (final Item delta : deltas)
		{
			final int id = delta.getId();
			final Integer logQuantity = this.collectionLog.get(id);
			if (logQuantity != null)
			{
				final int quantity = logQuantity + delta.getQuantity();
				this.collectionLog.put(id, quantity);
				emoteClueDeltas.add(new Item(id, quantity));
			}
		}
		return emoteClueDeltas;
	}
}
