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

package com.larsvansoest.runelite.clueitems.ui.content.item;

import com.larsvansoest.runelite.clueitems.data.EmoteClue;
import com.larsvansoest.runelite.clueitems.data.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.Palette;
import com.larsvansoest.runelite.clueitems.ui.content.foldable.FoldablePanel;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.RequirementContainer;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.RequirementPanel;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPanel extends RequirementPanel
{
	public ItemPanel(final RequirementContainer parent, final Palette palette, final com.larsvansoest.runelite.clueitems.data.EmoteClueItem emoteClueItem)
	{
		super(parent, palette, emoteClueItem.getCollectiveName());
		final EmoteClue[] emoteClues = EmoteClueAssociations.EmoteClueItemToEmoteClues.get(emoteClueItem);
		final List<EmoteClueDifficulty> difficulties = Arrays.stream(emoteClues).map(EmoteClue::getEmoteClueDifficulty).distinct().collect(Collectors.toList());
		super.setFilterable("difficulty", difficulties);
		super.setFilterable("quantity", emoteClues.length);
		super.setQuantity(String.valueOf(emoteClues.length));
		difficulties.stream().map(EmoteClueImages::getRibbon).map(ImageIcon::new).map(JLabel::new).forEach(super::addRightIcon);
		super.getHeader().getNameLabel().setHorizontalAlignment(JLabel.CENTER);
	}

	@Override
	public void unfold()
	{
		final List<FoldablePanel> foldablePanels = super.getFoldContentFoldablePanels();
		if (foldablePanels.size() > 1)
		{
			foldablePanels.get(0).unfold();
			foldablePanels.get(1).unfold();
		}
		super.unfold();
	}
}
