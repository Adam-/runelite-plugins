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

package com.larsvansoest.runelite.clueitems.ui.content.clue;

import com.larsvansoest.runelite.clueitems.data.EmoteClue;
import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.Palette;
import com.larsvansoest.runelite.clueitems.ui.content.foldable.FoldablePanel;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.Status;
import net.runelite.client.plugins.cluescrolls.clues.Enemy;
import net.runelite.client.plugins.cluescrolls.clues.emote.Emote;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class EmoteCluePanel extends FoldablePanel
{
	private static final Color propertyNameColor = ColorScheme.LIGHT_GRAY_COLOR;
	private static final Color propertyValueColor = new Color(propertyNameColor.getRed(), propertyNameColor.getGreen(), propertyNameColor.getBlue(), 150);

	public EmoteCluePanel(final Palette palette, final EmoteClue emoteClue)
	{
		super(palette, emoteClue.getLocationName());

		final EmoteClueDifficulty emoteClueDifficulty = emoteClue.getEmoteClueDifficulty();
		super.addLeftIcon(new JLabel(new ImageIcon(EmoteClueImages.getScroll(emoteClueDifficulty))));

		final Emote firstEmote = emoteClue.getFirstEmote();
		final Emote secondEmote = emoteClue.getSecondEmote();
		final Enemy enemy = emoteClue.getEnemy();
		final String description = emoteClue.getText();

		final JPanel foldContent = super.getFoldContent();
		foldContent.setBackground(palette.getSubPanelBackgroundColor());
		foldContent.setVisible(false);

		final JPanel difficultyPanel = this.getPropertyPanel("Difficulty", emoteClueDifficulty.name());
		final JPanel firstEmotePanel = this.getPropertyPanel("First emote", firstEmote.getName());
		final JPanel secondEmotePanel = this.getPropertyPanel("Second emote", secondEmote == null ? "none" : secondEmote.getName());
		final JPanel enemyPanel = this.getPropertyPanel("Enemy", enemy == null ? "none" : enemy.getText());

		final JPanel descriptionLabel = this.getDescriptionPanel(description);

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(5, 5, 0, 5);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;

		foldContent.add(difficultyPanel, c);

		c.gridy++;
		foldContent.add(firstEmotePanel, c);

		c.gridy++;
		foldContent.add(secondEmotePanel, c);

		c.gridy++;
		foldContent.add(enemyPanel, c);

		c.gridy++;
		foldContent.add(descriptionLabel, c);
	}

	private JPanel getPropertyPanel(final String name, final String value)
	{
		final JPanel propertyPanel = new JPanel(new GridBagLayout());

		final JLabel nameLabel = new JShadowedLabel(String.format("%s:", name));
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		nameLabel.setForeground(propertyNameColor);
		nameLabel.setOpaque(false);
		nameLabel.setHorizontalAlignment(JLabel.CENTER);

		final JLabel valueLabel = new JLabel(value.toLowerCase());
		valueLabel.setFont(FontManager.getRunescapeSmallFont());
		valueLabel.setForeground(propertyValueColor);
		valueLabel.setOpaque(false);
		valueLabel.setHorizontalAlignment(JLabel.CENTER);

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets.left = 5;
		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		propertyPanel.add(nameLabel, c);

		c.gridx++;
		propertyPanel.add(valueLabel, c);

		return propertyPanel;
	}

	private JPanel getDescriptionPanel(final String description)
	{
		final JPanel descriptionPanel = new JPanel(new GridBagLayout());
		descriptionPanel.setBackground(new Color(0, 0, 0, 0));

		final JLabel header = new JShadowedLabel("Description");
		header.setFont(FontManager.getRunescapeSmallFont());
		header.setHorizontalAlignment(JLabel.LEFT);
		header.setForeground(propertyNameColor);

		final JSeparator separator = new JSeparator();
		separator.setBorder(new MatteBorder(1, 0, 0, 0, propertyValueColor));

		final JLabel content = new JLabel(String.format("<html><p style=\"width:100%%\">%s</p></html>", description));
		content.setFont(FontManager.getRunescapeSmallFont());
		content.setHorizontalAlignment(JLabel.LEFT);
		content.setForeground(propertyValueColor);

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		descriptionPanel.add(header, c);

		c.gridy++;
		descriptionPanel.add(separator, c);

		c.insets.top = 3;
		c.insets.bottom = 3;
		c.gridy++;
		descriptionPanel.add(content, c);

		return descriptionPanel;
	}

	@Override
	public void setStatus(final Status status)
	{

	}

	@Override
	public void onHeaderMousePressed()
	{
		if (super.getExpanded())
		{
			super.fold();
		}
		else
		{
			super.unfold();
		}
	}
}
