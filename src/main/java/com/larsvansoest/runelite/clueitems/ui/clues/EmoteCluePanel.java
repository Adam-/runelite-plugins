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
import com.larsvansoest.runelite.clueitems.ui.components.DescriptionPanel;
import com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel;
import com.larsvansoest.runelite.clueitems.ui.components.PropertyPanel;
import lombok.Getter;
import net.runelite.client.plugins.cluescrolls.clues.Enemy;
import net.runelite.client.plugins.cluescrolls.clues.emote.Emote;

import javax.swing.*;
import java.awt.*;

/**
 * Displays data of a {@link com.larsvansoest.runelite.clueitems.data.EmoteClue}. Implements {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel}.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
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

	/**
	 * Creates the panel.
	 *
	 * @param palette   Colour scheme for the panel.
	 * @param emoteClue EmoteClue of which the data is displayed by this panel.
	 */
	public EmoteCluePanel(final EmoteClueItemsPalette palette, final EmoteClue emoteClue)
	{
		super(palette, emoteClue.getLocationName(), 160, 20);

		this.difficulty = emoteClue.getEmoteClueDifficulty();
		super.addLeft(new JLabel(new ImageIcon(EmoteClueImages.getScroll(this.difficulty))), new Insets(2, 4, 2, 0), 0, 0, DisplayMode.All);

		this.firstEmote = emoteClue.getFirstEmote();
		this.secondEmote = emoteClue.getSecondEmote();
		this.enemy = emoteClue.getEnemy();
		this.description = emoteClue.getText();

		super.addChild(new PropertyPanel(palette, "Difficulty", this.difficulty.name()), DisplayMode.All);
		super.addChild(new PropertyPanel(palette, "First emote", this.firstEmote.getName()), DisplayMode.All);
		super.addChild(new PropertyPanel(palette, "Second emote", this.secondEmote == null ? "none" : this.secondEmote.getName()), DisplayMode.All);
		super.addChild(new PropertyPanel(palette, "Enemy", this.enemy == null ? "none" : this.enemy.getText()), DisplayMode.All);
		super.addChild(new DescriptionPanel(palette, "Description", this.description), DisplayMode.All);
	}
}