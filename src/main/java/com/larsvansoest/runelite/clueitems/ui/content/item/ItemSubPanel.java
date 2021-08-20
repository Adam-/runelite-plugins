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

import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.Palette;
import com.larsvansoest.runelite.clueitems.ui.content.UpdatablePanel;
import com.larsvansoest.runelite.clueitems.ui.content.foldable.FoldablePanel;
import com.larsvansoest.runelite.clueitems.ui.content.requirement.Status;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class ItemSubPanel extends FoldablePanel
{
	private final static int ROW_SIZE = 6;

	private final GridBagConstraints foldContentConstraints;
	private Boolean expanded;

	public ItemSubPanel(final Palette palette)
	{
		super(palette, "Collection log");
		super.getFoldContent().setBackground(palette.getSubPanelBackgroundColor());
		super.setStatus(Status.Unknown);

		final JLabel nameLabel = super.getHeader().getNameLabel();
		nameLabel.setHorizontalAlignment(JLabel.LEFT);
		nameLabel.setPreferredSize(null);
		nameLabel.setMinimumSize(null);
		nameLabel.setMaximumSize(null);
		nameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		super.addLeftIcon(new JLabel(new ImageIcon(EmoteClueImages.Toolbar.Requirement.INVENTORY)));

		this.foldContentConstraints = new GridBagConstraints();
		this.foldContentConstraints.weightx = 0;
		this.foldContentConstraints.fill = GridBagConstraints.BOTH;
		this.expanded = false;
	}

	@Override
	public void fold()
	{
		this.expanded = false;
		super.fold();
	}

	@Override
	public void unfold()
	{
		final JPanel foldContent = super.getFoldContent();
		final LinkedList<UpdatablePanel> itemSlots = super.getFoldContentElements();
		super.getHeader().unfold();
		this.foldContentConstraints.gridx = 0;
		this.foldContentConstraints.gridy = 0;

		int i = 0;
		while (i < itemSlots.size())
		{
			foldContent.add(itemSlots.get(i), this.foldContentConstraints);
			i++;
			final int x = i % ROW_SIZE;
			if (x == 0)
			{
				this.foldContentConstraints.gridy++;
			}
			this.foldContentConstraints.gridx = x;
		}

		this.expanded = true;
		foldContent.setVisible(true);
		super.revalidate();
		super.repaint();
	}

	@Override
	public void addChild(final UpdatablePanel child)
	{
		super.addChild(child);
	}

	@Override
	public void onHeaderMousePressed()
	{
		if (this.expanded)
		{
			this.fold();
		}
		else
		{
			this.unfold();
		}
	}
}