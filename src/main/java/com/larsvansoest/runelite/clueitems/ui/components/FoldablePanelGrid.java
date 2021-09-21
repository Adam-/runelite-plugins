package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;

import java.util.Collection;
import java.util.Objects;

/**
 * {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid} extension to display {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} entries.
 * <p>
 * Adjusts {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} fold behaviour to limit the number of simultaneously unfolded panels to one.
 *
 * @param <T> Type of {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} to display.
 */
public class FoldablePanelGrid<T extends FoldablePanel> extends DataGrid<T>
{
	private FoldablePanel unfoldedPanel;

	/**
	 * Creates the grid.
	 *
	 * @param palette            Colour scheme for the grid.
	 * @param minSearchBarHeight Minimum height for the grid's searchbar.
	 */
	public FoldablePanelGrid(final EmoteClueItemsPalette palette, final int minSearchBarHeight)
	{
		super(palette, minSearchBarHeight);
	}

	/**
	 * Specify which data set the {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanelGrid} should display. Replaces possible existing displayed data set.
	 * <p>
	 * Adjusts {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} fold behaviour to limit the number of simultaneously unfolded panels to one.
	 *
	 * @param entries data set for the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid} to display.
	 */
	@Override
	public void load(final Collection<T> entries)
	{
		this.unfoldedPanel = null;
		for (final T entry : entries)
		{
			entry.setDisplayMode(FoldablePanel.DisplayMode.Default);
			entry.setOnHeaderMousePressed(() ->
			{
				if (entry.getExpanded())
				{
					entry.fold();
					this.unfoldedPanel = null;
				}
				else
				{
					if (Objects.nonNull(this.unfoldedPanel))
					{
						this.unfoldedPanel.fold();
					}
					entry.unfold();
					this.unfoldedPanel = entry;
				}
			});
		}
		super.load(entries);
	}

	/**
	 * Toggles the grid's visibility.
	 * <p>
	 * Folds all entries when set to invisible.
	 * <p>
	 * Sets all entries to {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel.DisplayMode} Default when set to visible.
	 *
	 * @param visible set the grid to visible if true, invisible otherwise.
	 */
	@Override
	public void setVisible(final boolean visible)
	{
		if (!visible)
		{
			for (final T entry : super.entries)
			{
				entry.fold();
			}
		}
		else
		{
			for (final T entry : super.entries)
			{
				entry.setDisplayMode(FoldablePanel.DisplayMode.Default);
			}
		}
		super.setVisible(visible);
	}
}
