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

import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Displays an item in a collection-log fashion.
 * <p>
 * It displays an item icon with an item quantity.
 * <p>
 * If the quantity is 0, the icon is semi-faded.
 */
public class ItemSlotPanel extends JPanel implements UpdatablePanel
{
	private final ItemManager itemManager;
	private final int itemId;
	private final JLabel itemIcon;
	@Getter
	private Status status;
	private ImageIcon transparentIcon;
	private int quantity;

	/**
	 * Creates the panel.
	 *
	 * @param itemManager Runelite's item manager to derive the item icons from.
	 * @param itemId      The {@link net.runelite.api.ItemID} of the item to display.
	 * @param name        The name of the item to display as tooltip when hovering over the item.
	 */
	public ItemSlotPanel(final ItemManager itemManager, final int itemId, final String name)
	{
		super.setLayout(new GridBagLayout());
		super.setToolTipText(name);

		this.itemManager = itemManager;
		this.itemId = itemId;
		this.itemIcon = new JLabel();
		this.itemIcon.setOpaque(false);
		this.transparentIcon = new ImageIcon();

		final AsyncBufferedImage itemImage = itemManager.getImage(this.itemId, 0, true);
		itemImage.onLoaded(() ->
		{
			this.transparentIcon = new ImageIcon(ImageUtil.alphaOffset(itemImage, 0.38f));
			this.itemIcon.setIcon(this.transparentIcon);
		});

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;

		super.add(this.itemIcon, c);
	}

	/**
	 * Sets the quantity of the item display.
	 *
	 * @param quantity the new item quantity to display.
	 */
	public void setQuantity(final int quantity)
	{
		this.quantity = quantity;
		this.setStatus(quantity > 0 ? Status.Complete : Status.InComplete);
	}

	/**
	 * Sets the status of the item display.
	 * <p>
	 * If the status is complete, the item is displayed normally with its quantity. It is displayed semi-transparent otherwise.
	 *
	 * @param status the new status of the item slot.
	 */
	public void setStatus(final Status status)
	{
		this.itemIcon.setIcon(status == Status.Complete ? new ImageIcon(this.itemManager.getImage(this.itemId, this.quantity, true)) : this.transparentIcon);
		this.status = status;
	}
}
