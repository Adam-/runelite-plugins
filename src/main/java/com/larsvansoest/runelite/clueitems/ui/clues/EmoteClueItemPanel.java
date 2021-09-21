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

package com.larsvansoest.runelite.clueitems.ui.clues;

import com.larsvansoest.runelite.clueitems.data.*;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import com.larsvansoest.runelite.clueitems.ui.components.ItemRequirementCollectionPanel;
import com.larsvansoest.runelite.clueitems.ui.components.RequirementPanel;
import com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Displays data of a {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem}. Implements {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel}.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class EmoteClueItemPanel extends RequirementPanel
{
	@Getter
	private final EmoteClueDifficulty[] difficulties;
	@Getter
	private final int quantity;
	private final ArrayList<StashUnitPanel> stashUnitPanels;
	private ItemRequirementCollectionPanel itemCollectionPanel;

	/**
	 * Creates the panel.
	 *
	 * @param palette       Colour scheme for the panel.
	 * @param emoteClueItem EmoteClueItem of which data is displayed by this panel.
	 */
	public EmoteClueItemPanel(final EmoteClueItemsPalette palette, final EmoteClueItem emoteClueItem)
	{
		super(palette, emoteClueItem.getCollectiveName(), 160, 20);

		final EmoteClue[] emoteClues = EmoteClueAssociations.EmoteClueItemToEmoteClues.get(emoteClueItem);

		this.stashUnitPanels = new ArrayList<>();
		this.difficulties = Arrays.stream(emoteClues).map(EmoteClue::getEmoteClueDifficulty).distinct().toArray(EmoteClueDifficulty[]::new);
		final Insets insets = new Insets(2, 0, 2, 5);
		Arrays.stream(this.difficulties).map(EmoteClueImages::getRibbon).map(ImageIcon::new).map(JLabel::new).forEach(label -> super.addRight(label, insets, 0, 0, DisplayMode.Default));
		this.quantity = emoteClues.length;
		super.addRight(new JLabel(String.valueOf(this.quantity)), insets, 0, 0, DisplayMode.Default);
	}

	/**
	 * Specify the {@link com.larsvansoest.runelite.clueitems.ui.components.ItemRequirementCollectionPanel} containing all items required to complete the {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} requirement.
	 *
	 * @param itemCollectionPanel Item collection panel displaying items required to complete the {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} requirement.
	 * @param displayModes        Specify when the panel should be displayed.
	 */
	public void setItemCollectionPanel(final ItemRequirementCollectionPanel itemCollectionPanel, final DisplayMode... displayModes)
	{
		if (Objects.nonNull(this.itemCollectionPanel))
		{
			super.removeChild(itemCollectionPanel);
		}
		final Runnable onHeaderMousePressed = itemCollectionPanel.getOnHeaderMousePressed();
		itemCollectionPanel.setOnHeaderMousePressed(() ->
		{
			this.stashUnitPanels.stream().map(StashUnitPanel::getItemCollectionPanel).filter(Objects::nonNull).forEach(ItemRequirementCollectionPanel::fold);
			onHeaderMousePressed.run();
		});
		this.itemCollectionPanel = itemCollectionPanel;
		super.addChild(itemCollectionPanel, displayModes);
	}

	/**
	 * Add a sub-display {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} entry to display a {@link com.larsvansoest.runelite.clueitems.data.StashUnit}.
	 *
	 * @param stashUnitPanel The sub-display which displays {@link com.larsvansoest.runelite.clueitems.data.StashUnit} data.
	 * @param displayModes   Specify when the panel should be displayed.
	 */
	public void addStashUnitPanel(final StashUnitPanel stashUnitPanel, final DisplayMode... displayModes)
	{
		final ItemRequirementCollectionPanel stashUnitItemCollectionPanel = stashUnitPanel.getItemCollectionPanel();
		final Runnable onHeaderMousePressed = stashUnitItemCollectionPanel.getOnHeaderMousePressed();
		stashUnitItemCollectionPanel.setOnHeaderMousePressed(() ->
		{
			if (Objects.nonNull(this.itemCollectionPanel))
			{
				this.itemCollectionPanel.fold();
			}
			onHeaderMousePressed.run();
		});
		this.stashUnitPanels.add(stashUnitPanel);
		super.addChild(stashUnitPanel, displayModes);
	}
}