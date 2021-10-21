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

import lombok.NonNull;
import net.runelite.api.Item;

import java.util.ArrayList;
import java.util.List;

class ItemTracker
{
	private final ArrayList<Item> items;

	public ItemTracker()
	{
		this.items = new ArrayList<>();
		this.reset();
	}

	public void reset()
	{
		this.items.clear();
	}

	public List<Item> writeDeltas(
			@NonNull
			final Item[] items)
	{
		final ArrayList<Item> deltas = new ArrayList<>();
		for (int i = 0; i < items.length; i++)
		{
			if (this.items.size() == i)
			{
				this.items.add(new Item(-1, 0));
			}

			final Item previousItem = this.items.get(i);
			final Item currentItem = items[i];
			this.items.set(i, currentItem);

			final int currentItemId = currentItem.getId();
			final int currentQuantity = currentItem.getQuantity();
			final int previousItemId = previousItem.getId();
			final int previousQuantity = previousItem.getQuantity();

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