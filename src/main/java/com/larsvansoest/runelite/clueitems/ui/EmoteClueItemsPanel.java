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

package com.larsvansoest.runelite.clueitems.ui;

import com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin;
import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.RequirementContainer;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.RequirementPanelProvider;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.SortType;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.Status;
import com.larsvansoest.runelite.clueitems.ui.disclaimer.DisclaimerPanel;
import com.larsvansoest.runelite.clueitems.ui.footer.FooterPanel;
import com.larsvansoest.runelite.clueitems.ui.search.FilterButton;
import com.larsvansoest.runelite.clueitems.ui.search.SearchBarFactory;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Main {@link PluginPanel} of the {@link EmoteClueItemsPlugin}, which displays {@link EmoteClueItem} requirement status progression.
 * <p>
 * Includes search bar, {@link FilterButton} buttons to filter and sort by properties, such as {@link Status}, requirement name, and more.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class EmoteClueItemsPanel extends PluginPanel
{
	private final Palette palette;

	private final IconTextField searchBar;
	private final JSeparator separator;
	private final DisclaimerPanel disclaimerPanel;
	private final RequirementContainer requirementContainer;

	private final FilterButton<Status> requirementStatusFilterButton;
	private final FilterButton<EmoteClueDifficulty> difficultyFilterButton;
	private final FilterButton<Map.Entry<SortType, Boolean>> sortFilterButton;

	public EmoteClueItemsPanel(final Palette palette, final RequirementPanelProvider requirementPanelProvider)
	{
		super();
		super.setLayout(new GridBagLayout());
		super.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.palette = palette;
		this.searchBar = new SearchBarFactory(this::onSearchBarTextChanged).defaultColor(palette.getDefaultColor()).hoverColor(palette.getHoverColor()).build();
		this.requirementContainer = requirementPanelProvider.getRequirementContainer();
		this.requirementContainer.sort(SortType.Quantity, true);

		this.separator = new JSeparator();
		this.setSeparatorColor(null);

		this.disclaimerPanel = new DisclaimerPanel(palette, this::removeDisclaimer);
		this.disclaimerPanel.setVisible(false);

		this.requirementStatusFilterButton = this.createRequirementStatusFilterButton(palette);
		this.difficultyFilterButton = this.createDifficultyFilterButton(palette);
		this.sortFilterButton = this.createSortFilterButton(palette);

		final FooterPanel footerPanel = new FooterPanel(palette, "Emote Clue Items", "v2.0.2", "https://github.com/larsvansoest/emote-clue-items");

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		super.add(this.searchBar, c);

		c.weightx = 0;
		c.gridx++;
		super.add(this.sortFilterButton, c);
		c.gridx++;
		super.add(this.difficultyFilterButton, c);
		c.gridx++;
		super.add(this.requirementStatusFilterButton, c);

		c.weightx = 1;
		c.gridwidth = 4;
		c.gridx = 0;
		c.gridy++;
		super.add(this.separator, c);

		c.gridy++;
		super.add(this.disclaimerPanel, c);

		c.gridy++;
		super.add(this.requirementContainer, c);

		c.gridy++;
		c.insets.top = 10;
		c.insets.left = 20;
		c.insets.right = 20;
		super.add(footerPanel, c);
	}

	public void setDisclaimer(final String text)
	{
		this.disclaimerPanel.setText(text);
		this.disclaimerPanel.setVisible(true);
	}

	public void removeDisclaimer()
	{
		this.disclaimerPanel.setVisible(false);
	}

	private void onSearchBarTextChanged()
	{
		this.requirementContainer.setFilter("name", this.searchBar.getText());
		this.search();
	}

	private void onRequirementStatusFilterChanged()
	{
		this.requirementContainer.setFilter("status", this.requirementStatusFilterButton.getSelectedValue());
		this.search();
	}

	private void onDifficultyFilterChanged()
	{
		final EmoteClueDifficulty emoteClueDifficulty = this.difficultyFilterButton.getSelectedValue();
		this.requirementContainer.setFilter("difficulty", emoteClueDifficulty);
		this.setSeparatorColor(emoteClueDifficulty);
		this.search();
	}

	private void onSortFilterChanged()
	{
		final Map.Entry<SortType, Boolean> selected = this.sortFilterButton.getSelectedValue();
		this.requirementContainer.sort(selected.getKey(), selected.getValue());
	}

	private FilterButton<Status> createRequirementStatusFilterButton(final Palette palette)
	{
		final FilterButton<Status> requirementStatusFilterButton = new FilterButton<>(
				null,
				new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.ALL),
				this.getToolTipText("Toggle show %s statuses.", "all"),
				new Dimension(25, 30),
				palette.getDefaultColor(),
				palette.getHoverColor(),
				4,
				this::onRequirementStatusFilterChanged
		);
		final String toolTipTextFormat = "Toggle show %s status.";
		requirementStatusFilterButton.addOption(Status.InComplete, new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.INCOMPLETE), this.getToolTipText(toolTipTextFormat, "incomplete"));
		requirementStatusFilterButton.addOption(Status.Complete, new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.COMPLETE), this.getToolTipText(toolTipTextFormat, "complete"));
		return requirementStatusFilterButton;
	}

	private FilterButton<EmoteClueDifficulty> createDifficultyFilterButton(final Palette palette)
	{
		final FilterButton<EmoteClueDifficulty> difficultyFilterButton = new FilterButton<>(
				null,
				new ImageIcon(EmoteClueImages.Ribbon.ALL),
				this.getToolTipText("Toggle show %s difficulties.", "all"),
				new Dimension(25, 30),
				palette.getDefaultColor(),
				palette.getHoverColor(),
				7,
				this::onDifficultyFilterChanged
		);
		final String toolTipTextFormat = "Toggle show %s difficulty.";
		difficultyFilterButton.addOption(EmoteClueDifficulty.Beginner, new ImageIcon(EmoteClueImages.Ribbon.BEGINNER), this.getToolTipText(toolTipTextFormat, "beginner"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Easy, new ImageIcon(EmoteClueImages.Ribbon.EASY), this.getToolTipText(toolTipTextFormat, "easy"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Medium, new ImageIcon(EmoteClueImages.Ribbon.MEDIUM), this.getToolTipText(toolTipTextFormat, "medium"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Hard, new ImageIcon(EmoteClueImages.Ribbon.HARD), this.getToolTipText(toolTipTextFormat, "hard"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Elite, new ImageIcon(EmoteClueImages.Ribbon.ELITE), this.getToolTipText(toolTipTextFormat, "elite"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Master, new ImageIcon(EmoteClueImages.Ribbon.MASTER), this.getToolTipText(toolTipTextFormat, "master"));
		return difficultyFilterButton;
	}

	private FilterButton<Map.Entry<SortType, Boolean>> createSortFilterButton(final Palette palette)
	{
		final FilterButton<Map.Entry<SortType, Boolean>> sortFilterButton = new FilterButton<>(
				new AbstractMap.SimpleImmutableEntry<>(SortType.Quantity, true),
				new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_DESCENDING),
				this.getToolTipText("Toggle order by %s (descending).", "quantity"),
				new Dimension(25, 30),
				palette.getDefaultColor(),
				palette.getHoverColor(),
				7,
				this::onSortFilterChanged
		);
		sortFilterButton.addOption(
				new AbstractMap.SimpleImmutableEntry<>(SortType.Quantity, false),
				new ImageIcon(EmoteClueImages.Toolbar.SortType.QUANTITY_ASCENDING),
				this.getToolTipText("Toggle order by %s (ascending).", "quantity")
		);
		sortFilterButton.addOption(
				new AbstractMap.SimpleImmutableEntry<>(SortType.Name, true),
				new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_DESCENDING),
				this.getToolTipText("Toggle order by %s (descending).", "name")
		);
		sortFilterButton.addOption(
				new AbstractMap.SimpleImmutableEntry<>(SortType.Name, false),
				new ImageIcon(EmoteClueImages.Toolbar.SortType.NAME_ASCENDING),
				this.getToolTipText("Toggle order by %s (ascending).", "name")
		);
		return sortFilterButton;
	}

	private String getToolTipText(final String format, final String keyword)
	{
		return String.format("<html>%s</html>", String.format(format, String.format("<b>%s</b>", keyword)));
	}

	private void setSeparatorColor(final EmoteClueDifficulty emoteClueDifficulty)
	{
		this.separator.setBorder(new MatteBorder(1, 0, 0, 0, emoteClueDifficulty == null ? this.palette.getSeparatorColor() : emoteClueDifficulty.getColor()));
	}

	public void search()
	{
		this.requirementContainer.runFilters();
	}
}