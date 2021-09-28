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

package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import net.runelite.client.input.KeyListener;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.IconTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;

/**
 * Lists {@link FoldablePanel} entries, provides functionality to display filtered sub-sets.
 *
 * @author Lars van Soest
 * @since 3.0.0
 */
public class DataGrid<T extends JPanel> extends JPanel
{
	protected final List<T> entries;
	private final IconTextField searchBar;
	private final Map<String, CycleButton> filterButtons;
	private final Map<String, Predicate<T>> filters;
	private final EmoteClueItemsPalette palette;
	private final DisclaimerPanel disclaimerPanel;
	private final JSeparator separator;
	private final JPanel entryList;
	private Comparator<T> sort;
	private CycleButton sortButton;

	/**
	 * Creates the grid.
	 *
	 * @param palette            Colour scheme for the grid.
	 * @param minSearchBarHeight Minimum height for the grid's searchbar.
	 */
	public DataGrid(final EmoteClueItemsPalette palette, final int minSearchBarHeight)
	{
		super(new GridBagLayout());
		this.entries = new ArrayList<>();

		this.palette = palette;
		this.searchBar = this.getSearchBar(minSearchBarHeight);
		this.filterButtons = new HashMap<>();
		this.filters = new HashMap<>();
		this.sort = Comparator.comparing(T::hashCode);
		this.sortButton = null;
		this.filters.put("_searchBar", panel -> panel.getName().toLowerCase().contains(this.searchBar.getText().toLowerCase()));

		this.separator = new JSeparator(SwingConstants.HORIZONTAL);
		final Dimension separatorSize = new Dimension(this.separator.getWidth(), 1);
		this.separator.setMinimumSize(separatorSize);
		this.separator.setPreferredSize(separatorSize);
		this.separator.setMaximumSize(separatorSize);
		this.setSeparatorColor(palette.getBrandingColor());

		this.disclaimerPanel = new DisclaimerPanel(palette, this::removeDisclaimer);
		this.disclaimerPanel.setVisible(false);

		this.entryList = new JPanel(new GridBagLayout());

		this.reset();
	}

	/**
	 * Returns html-formatted {@link java.lang.String}, adding bold markup to the specified keyword.
	 *
	 * @param format  String format which depicts where the bold keyword should be displayed (e.g. "Toggle show %s statuses.").
	 * @param keyword Keyword to display with bold formatting inside of the specified format (e.g. "complete").
	 * @return html-formatted {@link java.lang.String}.
	 */
	public static String getToolTipText(final String format, final String keyword)
	{
		return String.format("<html>%s</html>", String.format(format, String.format("<b>%s</b>", keyword)));
	}

	/**
	 * Clears all filter and sort buttons and displays the resulting entries.
	 * <p>
	 * Searchbar input will remain the same.
	 */
	public void reset()
	{
		this.filterButtons.values().forEach(CycleButton::reset);
		if (Objects.nonNull(this.sortButton))
		{
			this.sortButton.reset();
		}
		this.paint();
	}

	private void paint()
	{
		super.removeAll();
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		super.add(this.searchBar, c);
		c.weightx = 0;
		c.ipadx = 10;
		c.ipady = 10;
		if (Objects.nonNull(this.sortButton))
		{
			c.gridx++;
			super.add(this.sortButton, c);
		}
		for (final CycleButton filterButton : this.filterButtons.values())
		{
			c.gridx++;
			super.add(filterButton, c);
		}
		c.ipadx = 0;
		c.ipady = 0;
		c.gridwidth = this.filterButtons.size() + 1 + (Objects.nonNull(this.sortButton) ? 1 : 0); // searchbar consists of text input (1), filter (x) and sort (0|1) buttons.
		c.gridx = 0;
		c.weightx = 1;
		c.gridy++;
		super.add(this.separator, c);
		c.gridy++;
		super.add(this.disclaimerPanel, c);
		c.gridy++;
		super.add(this.entryList, c);
		this.query();
	}

	/**
	 * Apply all filters and sort the grid entries.
	 * <p>
	 * Result corresponds to currently active filters and sorting method selected using the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 */
	public final void query()
	{
		this.entryList.removeAll();
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridy = 0;
		c.gridx = 0;
		this.entries.stream().sorted(this.sort).filter(e -> this.filters.values().stream().allMatch(p -> p.test(e))).forEach(entry ->
		{
			this.entryList.add(entry, c);
			c.gridy++;
		});
		super.revalidate();
		super.repaint();
	}

	/**
	 * Adds a {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} to the search bar to allow sorting the entries by specified sort comparator.
	 * <p>
	 * If a sort {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} already exists, adds a new cycle stage as value to the existing {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 *
	 * @param icon    Icon to display when given sort comparator is selected.
	 * @param toolTip Tooltip to display on the sort {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} when the comparator is selected.
	 * @param sort    Sort comparator to order the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s entries.
	 */
	public void addSort(final Icon icon, final String toolTip, final Comparator<T> sort)
	{
		final Runnable onSelect = () ->
		{
			this.sort = sort;
			this.query();
		};
		if (Objects.isNull(this.sortButton))
		{
			this.sortButton = new CycleButton(this.palette, icon, onSelect, toolTip);
		}
		else
		{
			this.sortButton.addOption(icon, onSelect, toolTip);
		}
		this.paint();
	}

	/**
	 * Adds a {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} to the search bar to allow filtering the entries by specified predicate.
	 * <p>
	 * Adds a new {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} for every unique key, and adds subsequent added filters with the same key.
	 * <p>
	 * When given predicate is selected, changes the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s searchbar separator color to given color.
	 *
	 * @param key            Key which represents a unique filter {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 * @param icon           Icon to display when given predicate is selected.
	 * @param toolTip        Tooltip to display on the filter {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} when the predicate is selected.
	 * @param predicate      Predicate to filter the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s entries.
	 * @param separatorColor The {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s separator color to display when given predicate is selected.
	 */
	public void addFilter(final String key, final Icon icon, final String toolTip, final Predicate<T> predicate, final Color separatorColor)
	{
		final Runnable onSelect = () ->
		{
			this.filters.put(key, predicate);
			if (Objects.nonNull(separatorColor))
			{
				this.setSeparatorColor(separatorColor);
			}
			this.query();
		};
		if (this.filters.containsKey(key))
		{
			final CycleButton filterButton = this.filterButtons.get(key);
			filterButton.addOption(icon, onSelect, toolTip);
		}
		else
		{
			final CycleButton filterButton = new CycleButton(this.palette, icon, onSelect, toolTip);
			this.filterButtons.put(key, filterButton);
		}
		this.paint();
	}

	/**
	 * Adds a {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} to the search bar to allow filtering the entries by specified predicate.
	 * <p>
	 * Adds a new {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} for every unique key, and adds subsequent added filters with the same key.
	 *
	 * @param key       Key which represents a unique filter {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 * @param icon      Icon to display when given predicate is selected.
	 * @param toolTip   Tooltip to display on the filter {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} when the predicate is selected.
	 * @param predicate Predicate to filter the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s entries.
	 */
	public void addFilter(final String key, final Icon icon, final String toolTip, final Predicate<T> predicate)
	{
		this.addFilter(key, icon, toolTip, predicate, null);
	}

	private IconTextField getSearchBar(final int minSearchBarHeight)
	{
		final IconTextField searchBar = new IconTextField();
		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setBackground(this.palette.getDefaultColor());
		searchBar.setHoverBackgroundColor(this.palette.getHoverColor());
		searchBar.setFont(FontManager.getRunescapeSmallFont());
		final Dimension size = new Dimension(searchBar.getWidth(), minSearchBarHeight);
		searchBar.setMinimumSize(size);
		searchBar.setPreferredSize(size);
		searchBar.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
				DataGrid.this.query();
			}
		});
		searchBar.addClearListener(this::query);
		return searchBar;
	}

	/**
	 * Specify which data set the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid} should display. Replaces possible existing displayed data set.
	 *
	 * @param entries data set for the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid} to display.
	 */
	public void load(final Collection<T> entries)
	{
		this.entries.clear();
		this.entries.addAll(entries);
		this.query();
	}

	/**
	 * Underneath the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s searchbar, add a notification with given text.
	 * <p>
	 * Overwrites existing notification.
	 * <p>
	 * Notification can be removed by {@link #removeDisclaimer()}.
	 *
	 * @param text text to display in te notification.
	 */
	public void setDisclaimer(final String text)
	{
		this.disclaimerPanel.setText(text);
		this.disclaimerPanel.setVisible(true);
	}

	/**
	 * Set the {@link com.larsvansoest.runelite.clueitems.ui.components.DataGrid}'s searchbar separator color.
	 * <p>
	 * Specifying a separator color in {@link #addFilter(String, javax.swing.Icon, String, java.util.function.Predicate, java.awt.Color)} overwrites this setting once the corresponding value is selected.
	 *
	 * @param color the new separator color.
	 */
	public void setSeparatorColor(final Color color)
	{
		this.separator.setBackground(color);
	}

	/**
	 * Removes any notification added by {@link #setDisclaimer(String)}.
	 */
	public void removeDisclaimer()
	{
		this.disclaimerPanel.setVisible(false);
	}
}