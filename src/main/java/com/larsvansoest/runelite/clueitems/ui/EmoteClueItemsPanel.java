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
import com.larsvansoest.runelite.clueitems.data.EmoteClueImage;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.ui.requirement.RequirementPanelProvider;
import com.larsvansoest.runelite.clueitems.ui.requirement.RequirementStatus;
import com.larsvansoest.runelite.clueitems.ui.disclaimer.DisclaimerPanel;
import com.larsvansoest.runelite.clueitems.ui.footer.FooterPanel;
import com.larsvansoest.runelite.clueitems.ui.input.FilterButton;
import com.larsvansoest.runelite.clueitems.ui.input.SearchBarFactory;
import com.larsvansoest.runelite.clueitems.ui.requirement.RequirementContainer;
import com.larsvansoest.runelite.clueitems.ui.requirement.RequirementSortType;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.AbstractMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.MatteBorder;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

/**
 * Main {@link PluginPanel} of the {@link EmoteClueItemsPlugin}, which displays {@link EmoteClueItem} requirement status progression.
 * <p>
 * Includes search bar, {@link FilterButton} buttons to filter and sort by properties, such as {@link RequirementStatus}, requirement name, and more.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class EmoteClueItemsPanel extends PluginPanel
{
	private final EmoteClueItemsPanelPalette emoteClueItemsPanelPalette;

	private final IconTextField searchBar;
	private final JSeparator separator;
	private final DisclaimerPanel disclaimerPanel;
	private final RequirementContainer requirementContainer;

	private final FilterButton<RequirementStatus> requirementStatusFilterButton;
	private final FilterButton<EmoteClueDifficulty> difficultyFilterButton;
	private final FilterButton<Map.Entry<RequirementSortType, Boolean>> sortFilterButton;

	public EmoteClueItemsPanel(EmoteClueItemsPanelPalette emoteClueItemsPanelPalette, RequirementPanelProvider requirementPanelProvider)
	{
		super();
		super.setLayout(new GridBagLayout());
		super.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.emoteClueItemsPanelPalette = emoteClueItemsPanelPalette;
		this.searchBar = new SearchBarFactory(this::onSearchBarTextChanged).defaultColor(emoteClueItemsPanelPalette.getDefaultColor()).hoverColor(emoteClueItemsPanelPalette.getHoverColor()).build();
		this.requirementContainer = requirementPanelProvider.getRequirementContainer();
		this.requirementContainer.sort(RequirementSortType.Quantity, true);

		this.separator = new JSeparator();
		this.setSeparatorColor(null);

		this.disclaimerPanel = new DisclaimerPanel(emoteClueItemsPanelPalette, this::removeDisclaimer);
		this.disclaimerPanel.setVisible(false);

		this.requirementStatusFilterButton = this.createRequirementStatusFilterButton(emoteClueItemsPanelPalette);
		this.difficultyFilterButton = this.createDifficultyFilterButton(emoteClueItemsPanelPalette);
		this.sortFilterButton = this.createSortFilterButton(emoteClueItemsPanelPalette);

		FooterPanel footerPanel = new FooterPanel(emoteClueItemsPanelPalette, "Emote Clue Items", "v2.0.0", "https://github.com/larsvansoest/emote-clue-items");

		GridBagConstraints c = new GridBagConstraints();
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

	public void setDisclaimer(String text)
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
		EmoteClueDifficulty emoteClueDifficulty = this.difficultyFilterButton.getSelectedValue();
		this.requirementContainer.setFilter("difficulty", emoteClueDifficulty);
		this.setSeparatorColor(emoteClueDifficulty);
		this.search();
	}

	private void onSortFilterChanged()
	{
		Map.Entry<RequirementSortType, Boolean> selected = this.sortFilterButton.getSelectedValue();
		this.requirementContainer.sort(selected.getKey(), selected.getValue());
	}

	private FilterButton<RequirementStatus> createRequirementStatusFilterButton(EmoteClueItemsPanelPalette emoteClueItemsPanelPalette)
	{
		FilterButton<RequirementStatus> requirementStatusFilterButton = new FilterButton<>(null, new ImageIcon(EmoteClueImage.Toolbar.CheckSquare.ALL), this.getToolTipText("Toggle show %s statuses.", "all"), new Dimension(25, 30), emoteClueItemsPanelPalette.getDefaultColor(), emoteClueItemsPanelPalette.getHoverColor(), 4, this::onRequirementStatusFilterChanged);
		String toolTipTextFormat = "Toggle show %s status.";
		requirementStatusFilterButton.addOption(RequirementStatus.InComplete, new ImageIcon(EmoteClueImage.Toolbar.CheckSquare.INCOMPLETE), this.getToolTipText(toolTipTextFormat, "incomplete"));
		requirementStatusFilterButton.addOption(RequirementStatus.Complete, new ImageIcon(EmoteClueImage.Toolbar.CheckSquare.COMPLETE), this.getToolTipText(toolTipTextFormat, "complete"));
		return requirementStatusFilterButton;
	}

	private FilterButton<EmoteClueDifficulty> createDifficultyFilterButton(EmoteClueItemsPanelPalette emoteClueItemsPanelPalette)
	{
		FilterButton<EmoteClueDifficulty> difficultyFilterButton = new FilterButton<>(null, new ImageIcon(EmoteClueImage.Ribbon.ALL), this.getToolTipText("Toggle show %s difficulties.", "all"), new Dimension(25, 30), emoteClueItemsPanelPalette.getDefaultColor(), emoteClueItemsPanelPalette.getHoverColor(), 7, this::onDifficultyFilterChanged);
		String toolTipTextFormat = "Toggle show %s difficulty.";
		difficultyFilterButton.addOption(EmoteClueDifficulty.Beginner, new ImageIcon(EmoteClueImage.Ribbon.BEGINNER), this.getToolTipText(toolTipTextFormat, "beginner"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Easy, new ImageIcon(EmoteClueImage.Ribbon.EASY), this.getToolTipText(toolTipTextFormat, "easy"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Medium, new ImageIcon(EmoteClueImage.Ribbon.MEDIUM), this.getToolTipText(toolTipTextFormat, "medium"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Hard, new ImageIcon(EmoteClueImage.Ribbon.HARD), this.getToolTipText(toolTipTextFormat, "hard"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Elite, new ImageIcon(EmoteClueImage.Ribbon.ELITE), this.getToolTipText(toolTipTextFormat, "elite"));
		difficultyFilterButton.addOption(EmoteClueDifficulty.Master, new ImageIcon(EmoteClueImage.Ribbon.MASTER), this.getToolTipText(toolTipTextFormat, "master"));
		return difficultyFilterButton;
	}

	private FilterButton<Map.Entry<RequirementSortType, Boolean>> createSortFilterButton(EmoteClueItemsPanelPalette emoteClueItemsPanelPalette)
	{
		FilterButton<Map.Entry<RequirementSortType, Boolean>> sortFilterButton = new FilterButton<>(new AbstractMap.SimpleImmutableEntry<>(RequirementSortType.Quantity, true), new ImageIcon(EmoteClueImage.Toolbar.SortType.QUANTITY_DESCENDING), this.getToolTipText("Toggle order by %s (descending).", "quantity"), new Dimension(25, 30), emoteClueItemsPanelPalette.getDefaultColor(), emoteClueItemsPanelPalette.getHoverColor(), 7, this::onSortFilterChanged);
		sortFilterButton.addOption(new AbstractMap.SimpleImmutableEntry<>(RequirementSortType.Quantity, false), new ImageIcon(EmoteClueImage.Toolbar.SortType.QUANTITY_ASCENDING), this.getToolTipText("Toggle order by %s (ascending).", "quantity"));
		sortFilterButton.addOption(new AbstractMap.SimpleImmutableEntry<>(RequirementSortType.Name, true), new ImageIcon(EmoteClueImage.Toolbar.SortType.NAME_DESCENDING), this.getToolTipText("Toggle order by %s (descending).", "name"));
		sortFilterButton.addOption(new AbstractMap.SimpleImmutableEntry<>(RequirementSortType.Name, false), new ImageIcon(EmoteClueImage.Toolbar.SortType.NAME_ASCENDING), this.getToolTipText("Toggle order by %s (ascending).", "name"));
		return sortFilterButton;
	}

	private String getToolTipText(String format, String keyword)
	{
		return String.format("<html>%s</html>", String.format(format, String.format("<b>%s</b>", keyword)));
	}

	private void setSeparatorColor(EmoteClueDifficulty emoteClueDifficulty)
	{
		this.separator.setBorder(new MatteBorder(1, 0, 0, 0, emoteClueDifficulty == null ? this.emoteClueItemsPanelPalette.getSeparatorColor() : emoteClueDifficulty.getColor()));
	}

	public void search()
	{
		this.requirementContainer.runFilters();
	}
}