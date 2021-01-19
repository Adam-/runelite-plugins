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

package com.larsvansoest.runelite.clueitems.ui.requirement;

import com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.data.util.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanelPalette;
import com.larsvansoest.runelite.clueitems.ui.requirement.clue.EmoteCluePanel;
import com.larsvansoest.runelite.clueitems.ui.requirement.item.EmoteClueItemPanel;
import com.larsvansoest.runelite.clueitems.ui.requirement.item.EmoteClueItemSlotPanel;
import com.larsvansoest.runelite.clueitems.ui.requirement.item.EmoteClueItemSubPanel;
import com.larsvansoest.runelite.clueitems.vendor.runelite.client.plugins.cluescrolls.clues.EmoteClue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.cluescrolls.clues.item.SingleItemRequirement;

/**
 * Creates front-end ui requirement visualisation panels, and caches requirement-panel mappings to display requirement status, provided by {@link EmoteClueItemsPlugin}.
 *
 * @author Lars van Soest
 * @see javax.swing.JPanel
 * @see EmoteClueItemPanel
 * @see EmoteClueItemSubPanel
 * @see EmoteClueItemSlotPanel
 * @since 2.0.0
 */
public class RequirementPanelProvider
{
	private final RequirementContainer requirementContainer;
	private final Map<EmoteClueItem, EmoteClueItemPanel> emoteClueItemPanelMap;
	private final Map<EmoteClueItem, EmoteClueItemSlotPanel> slotPanelMap;

	public RequirementPanelProvider(EmoteClueItemsPanelPalette palette, ItemManager itemManager)
	{
		/* Create EmoteClueItem requirement panel network. */
		this.requirementContainer = new RequirementContainer();

		// Create parent EmoteClueItem panels.
		this.emoteClueItemPanelMap = EmoteClueAssociations.EmoteClueItemToEmoteClues.keySet().stream()
			.collect(Collectors.toMap(
				Function.identity(),
				emoteClueItem -> new EmoteClueItemPanel(this.requirementContainer, palette, emoteClueItem)
			));

		// Create an item panel for all required items.
		this.slotPanelMap = EmoteClueAssociations.ItemIdToEmoteClueItemSlot.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getValue,
				entry -> new EmoteClueItemSlotPanel(itemManager, entry.getKey(), entry.getValue().getCollectiveName())
			));

		// Create EmoteClueItem-EmoteClue (*-1) sub-panels.
		Map<EmoteClue, EmoteCluePanel> emoteCluePanelMap = EmoteClue.CLUES.stream()
			.collect(Collectors.toMap(
				Function.identity(),
				emoteClue -> new EmoteCluePanel(palette, emoteClue)
			));

		this.emoteClueItemPanelMap.forEach((emoteClueItem, emoteClueItemPanel) -> {
			// Add item collection log
			EmoteClueItemSubPanel subPanel = new EmoteClueItemSubPanel(palette);
			this.addSubItems(subPanel, emoteClueItem);
			emoteClueItemPanel.addChild(subPanel);

			// Add emote clue panels & info
			Arrays.stream(EmoteClueAssociations.EmoteClueItemToEmoteClues.get(emoteClueItem)).map(emoteCluePanelMap::get).forEach(emoteClueItemPanel::addChild);
		});

		this.requirementContainer.load(this.emoteClueItemPanelMap.values());
	}

	private void addSubItems(EmoteClueItemSubPanel subPanel, EmoteClueItem child)
	{
		EmoteClueItemSlotPanel childSlotPanel = this.slotPanelMap.get(child);
		if (childSlotPanel != null)
		{
			subPanel.addChild(childSlotPanel);
			return;
		}

		List<EmoteClueItem> successors = child.getChildren();
		if (successors != null)
		{
			for (EmoteClueItem successor : successors)
			{
				this.addSubItems(subPanel, successor);
			}
		}
	}

	/**
	 * Changes an item sprite to represent given quantity, if a mapping to {@link EmoteClueItemSlotPanel} exists.
	 *
	 * @param emoteClueItem the {@link SingleItemRequirement} {@link EmoteClueItem} requirement containing the item sprite.
	 * @param quantity      the item quantity the item sprite should show.
	 */
	public void setItemSlotStatus(EmoteClueItem emoteClueItem, int quantity)
	{
		EmoteClueItemSlotPanel slotPanel = this.slotPanelMap.get(emoteClueItem);
		if (slotPanel != null)
		{
			slotPanel.setStatus(quantity);
		}
	}

	/**
	 * Changes an {@link EmoteClue} {@link EmoteClueItem} status panel to represent given {@link RequirementStatus} status, if a mapping to {@link EmoteClueItemPanel} exists.
	 *
	 * @param emoteClueItem the {@link EmoteClue} {@link EmoteClueItem} requirement to display.
	 * @param status        the desired {@link RequirementStatus} status to display.
	 */
	public void setEmoteClueItemStatus(EmoteClueItem emoteClueItem, RequirementStatus status)
	{
		EmoteClueItemPanel emoteClueItemPanel = this.emoteClueItemPanelMap.get(emoteClueItem);
		if (emoteClueItemPanel != null)
		{
			emoteClueItemPanel.setStatus(status);
		}
	}

	public RequirementContainer getRequirementContainer()
	{
		return this.requirementContainer;
	}
}
