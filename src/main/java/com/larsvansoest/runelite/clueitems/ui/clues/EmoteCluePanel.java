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

import com.larsvansoest.runelite.clueitems.data.EmoteClue;
import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel;
import lombok.Getter;
import net.runelite.client.plugins.cluescrolls.clues.Enemy;
import net.runelite.client.plugins.cluescrolls.clues.emote.Emote;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;


public class EmoteCluePanel extends FoldablePanel
{
	@Getter
	private final Emote firstEmote;
	@Getter
	private final Emote secondEmote;
	@Getter
	private final Enemy enemy;
	@Getter
	private final String description;
	@Getter
	private final EmoteClueDifficulty difficulty;

	public EmoteCluePanel(final EmoteClueItemsPalette palette, final EmoteClue emoteClue)
	{
		super(palette, emoteClue.getLocationName());

		this.difficulty = emoteClue.getEmoteClueDifficulty();
		super.addLeftIcon(new JLabel(new ImageIcon(EmoteClueImages.getScroll(this.difficulty))));

		this.firstEmote = emoteClue.getFirstEmote();
		this.secondEmote = emoteClue.getSecondEmote();
		this.enemy = emoteClue.getEnemy();
		this.description = emoteClue.getText();

		super.addChild(this.getPropertyPanel(palette, "Difficulty", this.difficulty.name()));
		super.addChild(this.getPropertyPanel(palette, "First emote", this.firstEmote.getName()));
		super.addChild(this.getPropertyPanel(palette, "Second emote", this.secondEmote == null ? "none" : this.secondEmote.getName()));
		super.addChild(this.getPropertyPanel(palette, "Enemy", this.enemy == null ? "none" : this.enemy.getText()));
		super.addChild(this.getDescriptionPanel(palette, this.description));
	}

	private JPanel getPropertyPanel(final EmoteClueItemsPalette palette, final String name, final String value)
	{
		final JPanel propertyPanel = new JPanel(new GridBagLayout());
		propertyPanel.setBackground(palette.getFoldContentColor());

		final JLabel nameLabel = new JShadowedLabel(String.format("%s:", name));
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		nameLabel.setForeground(palette.getPropertyNameColor());
		nameLabel.setHorizontalAlignment(JLabel.CENTER);

		final JLabel valueLabel = new JLabel(value.toLowerCase());
		valueLabel.setFont(FontManager.getRunescapeSmallFont());
		valueLabel.setForeground(palette.getPropertyValueColor());
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

	private JPanel getDescriptionPanel(final EmoteClueItemsPalette palette, final String description)
	{
		final JPanel descriptionPanel = new JPanel(new GridBagLayout());
		descriptionPanel.setBackground(palette.getFoldContentColor());

		final JLabel header = new JShadowedLabel("Description");
		header.setFont(FontManager.getRunescapeSmallFont());
		header.setHorizontalAlignment(JLabel.LEFT);
		header.setForeground(palette.getPropertyNameColor());

		final JLabel content = new JLabel(String.format("<html><p style=\"width:100%%\">%s</p></html>", description));
		content.setFont(FontManager.getRunescapeSmallFont());
		content.setHorizontalAlignment(JLabel.LEFT);
		content.setForeground(palette.getPropertyValueColor());

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		header.setBorder(new MatteBorder(0, 0, 1, 0, palette.getPropertyValueColor()));
		descriptionPanel.add(header, c);

		c.insets.top = 3;
		c.insets.bottom = 3;
		c.gridy++;
		descriptionPanel.add(content, c);

		return descriptionPanel;
	}
}