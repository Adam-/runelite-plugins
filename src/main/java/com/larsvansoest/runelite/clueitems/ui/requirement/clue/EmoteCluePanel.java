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

package com.larsvansoest.runelite.clueitems.ui.requirement.clue;

import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.util.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.requirement.foldable.FoldablePanel;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanelPalette;
import com.larsvansoest.runelite.clueitems.ui.requirement.RequirementStatus;
import com.larsvansoest.runelite.clueitems.vendor.runelite.client.plugins.cluescrolls.clues.EmoteClue;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.MatteBorder;
import net.runelite.client.plugins.cluescrolls.clues.Enemy;
import net.runelite.client.plugins.cluescrolls.clues.emote.Emote;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

public class EmoteCluePanel extends FoldablePanel
{
	private static Color propertyNameColor = ColorScheme.LIGHT_GRAY_COLOR;
	private static Color propertyValueColor = new Color(propertyNameColor.getRed(), propertyNameColor.getGreen(), propertyNameColor.getBlue(), 150);

	public EmoteCluePanel(EmoteClueItemsPanelPalette emoteClueItemsPanelPalette, EmoteClue emoteClue) {
		super(emoteClueItemsPanelPalette, emoteClue.getLocationName());

		EmoteClueDifficulty difficulty = emoteClue.getEmoteClueDifficulty();
		super.addLeftIcon(new JLabel(new ImageIcon(EmoteClueImages.getScroll(difficulty))));

		Emote firstEmote = emoteClue.getFirstEmote();
		Emote secondEmote = emoteClue.getSecondEmote();
		boolean requiresLight = emoteClue.isRequiresLight();
		boolean requiresSpade = emoteClue.isRequiresSpade();
		Enemy enemy = emoteClue.getEnemy();
		String description = emoteClue.getText();

		JPanel foldContent = super.getFoldContent();
		foldContent.setBackground(emoteClueItemsPanelPalette.getSubPanelBackgroundColor());
		foldContent.setVisible(false);

		JPanel difficultyPanel = this.getPropertyPanel("Difficulty", difficulty.name());
		JPanel lightSourcePanel = this.getPropertyPanel("Light source", requiresLight ? "required" : "not required");
		JPanel spadePanel = this.getPropertyPanel("Spade", requiresSpade ? "required" : "not required");
		JPanel firstEmotePanel = this.getPropertyPanel("First emote", firstEmote.getName());
		JPanel secondEmotePanel = this.getPropertyPanel("Second emote", secondEmote == null ? "none" : secondEmote.getName());
		JPanel enemyPanel = this.getPropertyPanel("Enemy", enemy == null ? "none" : enemy.getText());

		JPanel descriptionLabel = this.getDescriptionPanel(description);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(5, 5, 0, 5);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;

		foldContent.add(difficultyPanel, c);

		c.gridy++;
		foldContent.add(lightSourcePanel, c);

		c.gridy++;
		foldContent.add(spadePanel, c);

		c.gridy++;
		foldContent.add(firstEmotePanel, c);

		c.gridy++;
		foldContent.add(secondEmotePanel, c);

		c.gridy++;
		foldContent.add(enemyPanel, c);

		c.gridy++;
		foldContent.add(descriptionLabel, c);
	}

	private JPanel getPropertyPanel(String name, String value) {
		JPanel propertyPanel = new JPanel(new GridBagLayout());

		JLabel nameLabel = new JShadowedLabel(String.format("%s:", name));
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		nameLabel.setForeground(propertyNameColor);
		nameLabel.setOpaque(false);
		nameLabel.setHorizontalAlignment(JLabel.CENTER);

		JLabel valueLabel = new JLabel(value.toLowerCase());
		valueLabel.setFont(FontManager.getRunescapeSmallFont());
		valueLabel.setForeground(propertyValueColor);
		valueLabel.setOpaque(false);
		valueLabel.setHorizontalAlignment(JLabel.CENTER);

		GridBagConstraints c = new GridBagConstraints();
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

	private JPanel getDescriptionPanel(String description) {
		JPanel descriptionPanel = new JPanel(new GridBagLayout());
		descriptionPanel.setBackground(new Color(0, 0, 0,0));

		JLabel header = new JShadowedLabel("Description");
		header.setFont(FontManager.getRunescapeSmallFont());
		header.setHorizontalAlignment(JLabel.LEFT);
		header.setForeground(propertyNameColor);

		JSeparator separator = new JSeparator();
		separator.setBorder(new MatteBorder(1, 0, 0, 0, propertyValueColor));

		JLabel content = new JLabel(String.format("<html><p style=\"width:100%%\">%s</p></html>", description));
		content.setFont(FontManager.getRunescapeSmallFont());
		content.setHorizontalAlignment(JLabel.LEFT);
		content.setForeground(propertyValueColor);

		GridBagConstraints c = new GridBagConstraints();
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
	public void setStatus(RequirementStatus requirementStatus)
	{

	}

	@Override
	public void onHeaderMousePressed()
	{
		if (super.isExpanded())
		{
			super.fold();
		}
		else
		{
			super.unfold();
		}
	}
}
