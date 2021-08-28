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

import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ItemCollectionPanel extends FoldablePanel
{
	private final int slotRowSize;
	private final ArrayList<ItemSlotPanel> itemSlots;
	private final JPanel itemsPanel;
	private final GridBagConstraints c;
	private final Color itemSlotBackGround;

	public ItemCollectionPanel(final EmoteClueItemsPalette palette, final int slotRowSize)
	{
		super(palette, "Collection log");
		super.setStatus(Status.Unknown);
		super.addLeftIcon(new JLabel(new ImageIcon(EmoteClueImages.Toolbar.Requirement.INVENTORY)));

		this.itemSlotBackGround = palette.getFoldContentColor();

		this.itemsPanel = new JPanel(new GridBagLayout());
		this.itemsPanel.setBackground(this.itemSlotBackGround);
		super.setFoldContentLeftInset(0);
		super.setFoldContentRightInset(0);
		super.setFixedFoldContentTopInset(1);
		super.addChild(this.itemsPanel);

		this.slotRowSize = slotRowSize;
		this.c = new GridBagConstraints();
		this.itemSlots = new ArrayList<>();
	}

	@Override
	public void fold()
	{
		this.itemsPanel.removeAll();
		super.fold();
	}

	@Override
	public void unfold()
	{
		this.c.gridx = 0;
		this.c.gridy = 0;
		int i = 0;
		while (i < this.itemSlots.size())
		{
			this.itemsPanel.add(this.itemSlots.get(i), this.c);
			i++;
			final int x = i % this.slotRowSize;
			if (x == 0)
			{
				this.c.gridy++;
			}
			this.c.gridx = x;
		}
		super.unfold();
	}

	public void addItem(final ItemSlotPanel itemSlotPanel)
	{
		itemSlotPanel.setBackground(this.itemSlotBackGround);
		this.itemSlots.add(itemSlotPanel);
	}
}