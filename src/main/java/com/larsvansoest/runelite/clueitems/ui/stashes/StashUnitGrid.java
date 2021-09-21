package com.larsvansoest.runelite.clueitems.ui.stashes;

import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import com.larsvansoest.runelite.clueitems.ui.components.DataGrid;
import com.larsvansoest.runelite.clueitems.ui.components.FoldablePanelGrid;

import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Secondary display of the {@link com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel} of the {@link com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin}, which displays {@link com.larsvansoest.runelite.clueitems.data.StashUnit} requirement status progression.
 * <p>
 * Includes search bar, buttons to filter and sort by properties.
 *
 * @author Lars van Soest
 * @since 3.0.0
 */
public class StashUnitGrid extends FoldablePanelGrid<StashUnitPanel>
{
	/**
	 * Creates the grid.
	 *
	 * @param palette Colour scheme for the grid.
	 */
	public StashUnitGrid(final EmoteClueItemsPalette palette)
	{
		super(palette, 25);
		this.createFilledFilterButton();
		this.createSortFilterButton();
		this.createDifficultyFilterButton(palette);
	}

	private void createFilledFilterButton()
	{
		final String toolTipTextFormat = "Toggle show %s stashes.";
		super.addFilter("filled", new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.UNKNOWN), DataGrid.getToolTipText(toolTipTextFormat, "all"), $ -> true);
		super.addFilter("filled", new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.UNBUILT), DataGrid.getToolTipText(toolTipTextFormat, "not built"), stashUnitPanel -> !stashUnitPanel.isBuilt());
		super.addFilter("filled",
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.INCOMPLETE_EMPTY),
				DataGrid.getToolTipText(toolTipTextFormat, "empty"),
				stashUnitPanel -> stashUnitPanel.isBuilt() && !stashUnitPanel.isFilled()
		);
		super.addFilter("filled",
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.COMPLETE),
				DataGrid.getToolTipText(toolTipTextFormat, "filled"),
				stashUnitPanel -> stashUnitPanel.isBuilt() && stashUnitPanel.isFilled()
		);
	}

	private void createSortFilterButton()
	{
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_ASCENDING),
				DataGrid.getToolTipText("Toggle order by %s (descending).", "quantity"),
				Comparator.comparingInt(StashUnitPanel::getQuantity)
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_DESCENDING),
				DataGrid.getToolTipText("Toggle order by %s (ascending).", "quantity"),
				Comparator.comparingInt(StashUnitPanel::getQuantity).reversed()
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_ASCENDING),
				DataGrid.getToolTipText("Toggle order by %s (ascending).", "name"),
				Comparator.comparing(StashUnitPanel::getName)
		);
		super.addSort(new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_DESCENDING),
				DataGrid.getToolTipText("Toggle order by %s (ascending).", "name"),
				Comparator.comparing(StashUnitPanel::getName).reversed()
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
}