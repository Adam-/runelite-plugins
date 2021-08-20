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

package com.larsvansoest.runelite.clueitems.ui.content.requirement;

import com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin;
import com.larsvansoest.runelite.clueitems.data.EmoteClue;
import com.larsvansoest.runelite.clueitems.data.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.ui.Palette;
import com.larsvansoest.runelite.clueitems.ui.content.clue.EmoteCluePanel;
import com.larsvansoest.runelite.clueitems.ui.content.item.ItemPanel;
import com.larsvansoest.runelite.clueitems.ui.content.item.ItemSlotPanel;
import com.larsvansoest.runelite.clueitems.ui.content.item.ItemSubPanel;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.cluescrolls.clues.item.SingleItemRequirement;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Creates front-end ui requirement visualisation panels, and caches requirement-panel mappings to display requirement status, provided by {@link EmoteClueItemsPlugin}.
 *
 * @author Lars van Soest
 * @see javax.swing.JPanel
 * @see ItemPanel
 * @see ItemSubPanel
 * @see ItemSlotPanel
 * @since 2.0.0
 */
public class RequirementPanelProvider
{
	private final RequirementContainer requirementContainer;
	private final Map<com.larsvansoest.runelite.clueitems.data.EmoteClueItem, ItemPanel> emoteClueItemPanelMap;
	private final Map<com.larsvansoest.runelite.clueitems.data.EmoteClueItem, ItemSlotPanel> slotPanelMap;

	public RequirementPanelProvider(final Palette palette, final ItemManager itemManager)
	{
		/* Create EmoteClueItem requirement panel network. */
		this.requirementContainer = new RequirementContainer();

		// Create parent EmoteClueItem panels.
		this.emoteClueItemPanelMap = EmoteClueAssociations.EmoteClueItemToEmoteClues
				.keySet()
				.stream()
				.collect(Collectors.toMap(Function.identity(), emoteClueItem -> new ItemPanel(this.requirementContainer, palette, emoteClueItem)));

		// Create an item panel for all required items.
		this.slotPanelMap = EmoteClueAssociations.ItemIdToEmoteClueItemSlot
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, entry -> new ItemSlotPanel(itemManager, entry.getKey(), entry.getValue().getCollectiveName())));

		// Create EmoteClueItem-EmoteClue (*-1) sub-panels.
		final Map<EmoteClue, EmoteCluePanel> emoteCluePanelMap = EmoteClue.CLUES.stream().collect(Collectors.toMap(Function.identity(), emoteClue -> new EmoteCluePanel(palette, emoteClue)));

		this.emoteClueItemPanelMap.forEach((emoteClueItem, itemPanel) ->
		{
			// Add item collection log
			final ItemSubPanel subPanel = new ItemSubPanel(palette);
			this.addSubItems(subPanel, emoteClueItem);
			itemPanel.addChild(subPanel);

			// Add emote clue panels & info
			Arrays.stream(EmoteClueAssociations.EmoteClueItemToEmoteClues.get(emoteClueItem)).map(emoteCluePanelMap::get).forEach(itemPanel::addChild);
		});

		this.requirementContainer.load(this.emoteClueItemPanelMap.values());
	}

	private void addSubItems(final ItemSubPanel subPanel, final com.larsvansoest.runelite.clueitems.data.EmoteClueItem child)
	{
		final ItemSlotPanel childSlotPanel = this.slotPanelMap.get(child);
		if (childSlotPanel != null)
		{
			subPanel.addChild(childSlotPanel);
			return;
		}

		final List<com.larsvansoest.runelite.clueitems.data.EmoteClueItem> successors = child.getChildren();
		if (successors != null)
		{
			for (final com.larsvansoest.runelite.clueitems.data.EmoteClueItem successor : successors)
			{
				this.addSubItems(subPanel, successor);
			}
		}
	}

	/**
	 * Changes an item sprite to represent given quantity, if a mapping to {@link ItemSlotPanel} exists.
	 *
	 * @param emoteClueItem the {@link SingleItemRequirement} {@link EmoteClueItem} requirement containing the item sprite.
	 * @param quantity      the item quantity the item sprite should show.
	 */
	public void setItemSlotStatus(final com.larsvansoest.runelite.clueitems.data.EmoteClueItem emoteClueItem, final int quantity)
	{
		final ItemSlotPanel slotPanel = this.slotPanelMap.get(emoteClueItem);
		if (slotPanel != null)
		{
			slotPanel.setStatus(quantity);
		}
	}

	/**
	 * Changes an {@link EmoteClue} {@link EmoteClueItem} status panel to represent given {@link Status} status, if a mapping to {@link ItemPanel} exists.
	 *
	 * @param emoteClueItem the {@link EmoteClue} {@link EmoteClueItem} requirement to display.
	 * @param status        the desired {@link Status} status to display.
	 */
	public void setEmoteClueItemStatus(final com.larsvansoest.runelite.clueitems.data.EmoteClueItem emoteClueItem, final Status status)
	{
		final ItemPanel itemPanel = this.emoteClueItemPanelMap.get(emoteClueItem);
		if (itemPanel != null)
		{
			itemPanel.setStatus(status);
		}
	}

	public RequirementContainer getRequirementContainer()
	{
		return this.requirementContainer;
	}
}
