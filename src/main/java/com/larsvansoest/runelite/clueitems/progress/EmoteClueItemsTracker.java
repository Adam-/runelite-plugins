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

import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import net.runelite.api.Item;

class EmoteClueItemsTracker
{
	private final Item[] items;

	public EmoteClueItemsTracker(int capacity)
	{
		this.items = new Item[capacity];
		this.reset();
	}

	public void reset() {
		for (int i = 0; i < this.items.length; i++)
		{
			this.items[i] = new Item(-1, 0);
		}
	}

	public List<Item> writeDeltas(@NonNull Item[] items)
	{
		LinkedList<Item> deltas = new LinkedList<>();
		for (int i = 0; i < items.length; i++)
		{
			Item previousItem = this.items[i];
			Item currentItem = items[i];
			this.items[i] = currentItem;

			int currentItemId = currentItem.getId();
			int currentQuantity = currentItem.getQuantity();
			int previousItemId = previousItem.getId();
			int previousQuantity = previousItem.getQuantity();

			if (previousItemId != currentItemId)
			{
				if (previousItemId == -1)
				{
					deltas.add(currentItem);
				}
				else if (currentItemId == -1)
				{
					deltas.add(new Item(previousItemId, -previousQuantity));
				}
				else
				{
					deltas.add(currentItem);
					deltas.add(new Item(previousItemId, -previousQuantity));
				}
			}
			else if (previousQuantity != currentQuantity)
			{
				deltas.add(new Item(currentItemId, currentQuantity - previousQuantity));
			}
		}
		return deltas;
	}
}