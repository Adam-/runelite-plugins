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

/**
 * Displays an item collection log, with item icons and quantity indicators.
 * <p>
 * Items are semi-transparent when quantity is 0.
 */
public class ItemCollectionPanel extends RequirementPanel
{
	private final int slotRowSize;
	private final ArrayList<ItemSlotPanel> itemSlots;
	private final JPanel itemsPanel;
	private final Color itemSlotBackGround;

	/**
	 * Creates the item collection panel.
	 *
	 * @param palette     Colour scheme for the grid.
	 * @param name        Name to display as {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} header text.
	 * @param slotRowSize The amount of item icons per row.
	 */
	public ItemCollectionPanel(final EmoteClueItemsPalette palette, final String name, final int slotRowSize)
	{
		super(palette, name, 160, 20);
		super.setStatus(Status.InComplete);
		super.addLeft(new JLabel(new ImageIcon(EmoteClueImages.Toolbar.Requirement.INVENTORY)), new Insets(2, 4, 2, 0), 0, 0, DisplayMode.All);

		this.itemSlotBackGround = palette.getFoldContentColor();

		this.itemsPanel = new JPanel(new GridBagLayout());
		this.itemsPanel.setBackground(this.itemSlotBackGround);
		super.setFoldContentLeftInset(0);
		super.setFoldContentRightInset(0);
		super.setFixedFoldContentTopInset(1);
		super.addChild(this.itemsPanel, DisplayMode.All);

		this.slotRowSize = slotRowSize;
		this.itemSlots = new ArrayList<>();
	}

	/**
	 * Collapses the collection log.
	 * <p>
	 * Also removes all item panels to enable re-using them in another panel.
	 */
	@Override
	public void fold()
	{
		this.itemSlots.forEach(this.itemsPanel::remove);
		super.fold();
	}

	/**
	 * Un-collapses the collection log.
	 * <p>
	 * Also re-adds all item panels to enable re-using them in another panel. s
	 */
	@Override
	public void unfold()
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		int i = 0;
		while (i < this.itemSlots.size())
		{
			this.itemsPanel.add(this.itemSlots.get(i), c);
			i++;
			final int x = i % this.slotRowSize;
			if (x == 0)
			{
				c.gridy++;
			}
			c.gridx = x;
		}
		super.unfold();
	}

	/**
	 * Adds an item to the item collection log.
	 *
	 * @param itemSlotPanel the panel which displays the item.
	 */
	public void addItem(final ItemSlotPanel itemSlotPanel)
	{
		if (!this.itemSlots.contains(itemSlotPanel))
		{
			itemSlotPanel.setBackground(this.itemSlotBackGround);
			this.itemSlots.add(itemSlotPanel);
		}
	}
}