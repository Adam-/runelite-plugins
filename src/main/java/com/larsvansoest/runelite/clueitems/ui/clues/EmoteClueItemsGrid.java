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

import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import com.larsvansoest.runelite.clueitems.ui.components.FoldablePanelGrid;
import com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Main {@link net.runelite.client.ui.PluginPanel} of the {@link com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin}, which displays {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} requirement status progression.
 * <p>
 * Includes search bar, buttons to filter and sort by properties, such as {@link com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel}, requirement name, and more.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class EmoteClueItemsGrid extends FoldablePanelGrid<EmoteClueItemPanel>
{
	public EmoteClueItemsGrid(final EmoteClueItemsPalette emoteClueItemsPalette)
	{
		super(emoteClueItemsPalette);

		this.createRequirementStatusFilterButton();
		this.createDifficultyFilterButton();
		this.createSortFilterButton();
	}

	private void createRequirementStatusFilterButton()
	{
		final String filterKey = "status";
		final String toolTipTextFormat = "Toggle show %s statuses.";

		super.addFilter(filterKey, new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.ALL), this.getToolTipText(toolTipTextFormat, "all"), $ -> true);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.INCOMPLETE),
				this.getToolTipText(toolTipTextFormat, "incomplete"),
				itemPanel -> itemPanel.getStatus() == UpdatablePanel.Status.InComplete
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.IN_PROGRESS),
				this.getToolTipText(toolTipTextFormat, "incomplete"),
				itemPanel -> itemPanel.getStatus() == UpdatablePanel.Status.InProgress
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.COMPLETE),
				this.getToolTipText(toolTipTextFormat, "incomplete"),
				itemPanel -> itemPanel.getStatus() == UpdatablePanel.Status.Complete
		);
	}

	private void createDifficultyFilterButton()
	{
		final String filterKey = "difficulty";
		final String toolTipTextFormat = "Toggle show %s difficulties.";

		super.addFilter(filterKey, new ImageIcon(EmoteClueImages.Ribbon.ALL), this.getToolTipText(toolTipTextFormat, "all"), $ -> true, ColorScheme.BRAND_ORANGE);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.BEGINNER),
				this.getToolTipText(toolTipTextFormat, "beginner"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Beginner),
				EmoteClueDifficulty.Beginner.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.EASY),
				this.getToolTipText(toolTipTextFormat, "easy"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Easy),
				EmoteClueDifficulty.Easy.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.MEDIUM),
				this.getToolTipText(toolTipTextFormat, "medium"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Medium),
				EmoteClueDifficulty.Medium.getColor()

		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.HARD),
				this.getToolTipText(toolTipTextFormat, "hard"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Hard),
				EmoteClueDifficulty.Hard.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.ELITE),
				this.getToolTipText(toolTipTextFormat, "elite"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Elite),
				EmoteClueDifficulty.Elite.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.MASTER),
				this.getToolTipText(toolTipTextFormat, "master"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Master),
				EmoteClueDifficulty.Master.getColor()
		);
	}

	private void createSortFilterButton()
	{
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_DESCENDING),
				this.getToolTipText("Toggle order by %s (descending).", "quantity"),
				Comparator.comparingInt(EmoteClueItemPanel::getQuantity)
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_ASCENDING),
				this.getToolTipText("Toggle order by %s (ascending).", "quantity"),
				Comparator.comparingInt(EmoteClueItemPanel::getQuantity).reversed()
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_DESCENDING),
				this.getToolTipText("Toggle order by %s (ascending).", "name"),
				Comparator.comparing(EmoteClueItemPanel::getName)
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_DESCENDING),
				this.getToolTipText("Toggle order by %s (ascending).", "name"),
				Comparator.comparing(EmoteClueItemPanel::getName).reversed()
		);
	}

	private String getToolTipText(final String format, final String keyword)
	{
		return String.format("<html>%s</html>", String.format(format, String.format("<b>%s</b>", keyword)));
	}
}