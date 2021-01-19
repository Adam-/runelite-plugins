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

import com.larsvansoest.runelite.clueitems.data.util.EmoteClueAssociations;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.runelite.api.Item;

class EmoteClueItemsMonitor
{
	private final EmoteClueItemsTracker inventoryTracker;
	private final EmoteClueItemsTracker bankTracker;
	private final EmoteClueItemsTracker equipmentTracker;
	private final HashMap<Integer, Integer> collectionLog;

	public EmoteClueItemsMonitor()
	{
		this.inventoryTracker = new EmoteClueItemsTracker(28);
		this.bankTracker = new EmoteClueItemsTracker(816);
		this.equipmentTracker = new EmoteClueItemsTracker(13);
		this.collectionLog = new HashMap<>(EmoteClueAssociations.ItemIdToEmoteClueItemSlot.keySet().size());
		this.reset();
	}

	public void reset() {
		for (Integer itemId : EmoteClueAssociations.ItemIdToEmoteClueItemSlot.keySet())
		{
			this.collectionLog.put(itemId, 0);
		}
		this.inventoryTracker.reset();
		this.bankTracker.reset();
		this.equipmentTracker.reset();
	}

	public List<Item> fetchEmoteClueItemChanges(int containerId, Item[] items)
	{
		List<Item> deltas;
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

		List<Item> emoteClueDeltas = new LinkedList<>();
		for (Item delta : deltas)
		{
			int id = delta.getId();
			Integer logQuantity = this.collectionLog.get(id);
			if (logQuantity != null)
			{
				int quantity = logQuantity + delta.getQuantity();
				this.collectionLog.put(id, quantity);
				emoteClueDeltas.add(new Item(id, quantity));
			}
		}
		return emoteClueDeltas;
	}
}
