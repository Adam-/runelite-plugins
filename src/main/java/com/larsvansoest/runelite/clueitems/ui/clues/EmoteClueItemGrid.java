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
import com.larsvansoest.runelite.clueitems.ui.components.DataGrid;
import com.larsvansoest.runelite.clueitems.ui.components.FoldablePanelGrid;
import com.larsvansoest.runelite.clueitems.ui.components.RequirementPanel;

import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Primary display of the {@link com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel} of the {@link com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin}, which displays {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} requirement status progression.
 * <p>
 * Includes search bar, buttons to filter and sort by properties.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class EmoteClueItemGrid extends FoldablePanelGrid<EmoteClueItemPanel>
{
	/**
	 * Creates the grid.
	 *
	 * @param palette Colour scheme for the grid.
	 */
	public EmoteClueItemGrid(final EmoteClueItemsPalette palette)
	{
		super(palette, 25);

		this.createRequirementStatusFilterButton();
		this.createDifficultyFilterButton(palette);
		this.createSortFilterButton();
	}

	private void createRequirementStatusFilterButton()
	{
		final String filterKey = "status";
		final String toolTipTextFormat = "Toggle show %s statuses.";

		super.addFilter(filterKey, new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.UNKNOWN), DataGrid.getToolTipText(toolTipTextFormat, "all"), $ -> true);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.INCOMPLETE),
				DataGrid.getToolTipText(toolTipTextFormat, "incomplete"),
				itemPanel -> itemPanel.getStatus() == RequirementPanel.Status.InComplete
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.IN_PROGRESS),
				DataGrid.getToolTipText(toolTipTextFormat, "in progress"),
				itemPanel -> itemPanel.getStatus() == RequirementPanel.Status.InProgress
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.COMPLETE),
				DataGrid.getToolTipText(toolTipTextFormat, "complete"),
				itemPanel -> itemPanel.getStatus() == RequirementPanel.Status.Complete
		);
	}

	private void createDifficultyFilterButton(final EmoteClueItemsPalette palette)
	{
		final String filterKey = "difficulty";
		final String toolTipTextFormat = "Toggle show %s difficulties.";

		super.addFilter(filterKey, new ImageIcon(EmoteClueImages.Ribbon.ALL), DataGrid.getToolTipText(toolTipTextFormat, "all"), $ -> true, palette.getBrandingColor());
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.BEGINNER),
				DataGrid.getToolTipText(toolTipTextFormat, "beginner"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Beginner),
				EmoteClueDifficulty.Beginner.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.EASY),
				DataGrid.getToolTipText(toolTipTextFormat, "easy"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Easy),
				EmoteClueDifficulty.Easy.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.MEDIUM),
				DataGrid.getToolTipText(toolTipTextFormat, "medium"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Medium),
				EmoteClueDifficulty.Medium.getColor()

		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.HARD),
				DataGrid.getToolTipText(toolTipTextFormat, "hard"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Hard),
				EmoteClueDifficulty.Hard.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.ELITE),
				DataGrid.getToolTipText(toolTipTextFormat, "elite"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Elite),
				EmoteClueDifficulty.Elite.getColor()
		);
		super.addFilter(filterKey,
				new ImageIcon(EmoteClueImages.Ribbon.MASTER),
				DataGrid.getToolTipText(toolTipTextFormat, "master"),
				itemPanel -> Arrays.stream(itemPanel.getDifficulties()).anyMatch(difficulty -> difficulty == EmoteClueDifficulty.Master),
				EmoteClueDifficulty.Master.getColor()
		);
	}

	private void createSortFilterButton()
	{
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_ASCENDING),
				DataGrid.getToolTipText("Toggle order by %s (descending).", "quantity"),
				Comparator.comparingInt(EmoteClueItemPanel::getQuantity)
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_DESCENDING),
				DataGrid.getToolTipText("Toggle order by %s (ascending).", "quantity"),
				Comparator.comparingInt(EmoteClueItemPanel::getQuantity).reversed()
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_ASCENDING),
				DataGrid.getToolTipText("Toggle order by %s (ascending).", "name"),
				Comparator.comparing(EmoteClueItemPanel::getName)
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_DESCENDING),
				DataGrid.getToolTipText("Toggle order by %s (ascending).", "name"),
				Comparator.comparing(EmoteClueItemPanel::getName).reversed()
		);
	}
}