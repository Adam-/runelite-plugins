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

package com.larsvansoest.runelite.clueitems.ui.requirement.item;

import com.larsvansoest.runelite.clueitems.ui.requirement.RequirementStatus;
import com.larsvansoest.runelite.clueitems.ui.requirement.UpdatablePanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

public class EmoteClueItemSlotPanel extends UpdatablePanel
{
	private final ItemManager itemManager;
	private final int itemId;
	private final JLabel itemIcon;
	private ImageIcon transparentIcon;

	public EmoteClueItemSlotPanel(ItemManager itemManager, int itemId, String name) {
		super.setLayout(new GridBagLayout());
		super.setToolTipText(name);

		this.itemManager = itemManager;
		this.itemId = itemId;
		this.itemIcon = new JLabel();
		this.itemIcon.setOpaque(false);
		this.transparentIcon = new ImageIcon();

		AsyncBufferedImage itemImage = itemManager.getImage(this.itemId, 0, true);
		itemImage.onLoaded(() -> {
			this.transparentIcon = new ImageIcon(ImageUtil.alphaOffset(itemImage, 0.38f));
			this.itemIcon.setIcon(this.transparentIcon);
		});

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;

		super.add(this.itemIcon, c);
	}

	public void setStatus(int quantity) {
		this.itemIcon.setIcon(quantity > 0 ? new ImageIcon(this.itemManager.getImage(this.itemId, quantity, true)) : this.transparentIcon);
	}

	@Override
	public void setStatus(RequirementStatus requirementStatus)
	{
	}
}
