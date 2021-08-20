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

package com.larsvansoest.runelite.clueitems.ui.content.foldable;

import com.larsvansoest.runelite.clueitems.ui.Palette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Header extends JPanel
{
	private final Palette palette;
	private final HeaderText name;
	private final HeaderText quantity;
	private final FoldablePanel parent;
	private final FoldIcon foldIcon;
	private final GridBagConstraints c;
	private final HeaderIconsPanel leftIconsPanel;
	private final HeaderIconsPanel rightIconsPanel;
	private Boolean expanded;

	public Header(final FoldablePanel parent, final Palette palette, final Dimension dimension, final String name)
	{
		this.palette = palette;
		this.name = new HeaderText(dimension, name);
		this.quantity = new HeaderText(new Dimension(7, 15), "-1");
		this.quantity.setVisible(false);
		this.foldIcon = new FoldIcon();
		this.parent = parent;
		this.leftIconsPanel = new HeaderIconsPanel(false);
		this.rightIconsPanel = new HeaderIconsPanel(true);
		this.expanded = false;
		super.setBackground(Header.this.palette.getDefaultColor());

		super.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				Header.this.onMousePressed();
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				Header.super.setBackground(Header.this.palette.getHoverColor());
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				Header.super.setBackground(Header.this.expanded ? Header.this.palette.getSelectColor() : Header.this.palette.getDefaultColor());
			}
		});
		super.setLayout(new GridBagLayout());
		this.c = new GridBagConstraints();
		this.c.fill = GridBagConstraints.BOTH;
		this.c.gridx = 0;
		this.c.gridy = 0;
		this.c.insets.top = 2;
		this.c.insets.bottom = 2;
		this.c.insets.left = 5;
		super.add(this.leftIconsPanel, this.c);

		this.c.gridx++;
		this.c.insets.left = 0;
		super.add(this.name, this.c);

		this.c.gridx++;
		this.c.weightx = 1;
		super.add(new JLabel(), this.c);

		this.c.gridx++;
		this.c.weightx = 0;
		this.c.insets.right = 5;
		super.add(this.rightIconsPanel, this.c);

		this.c.gridx++;
		super.add(this.quantity, this.c);

		this.c.gridx++;
		super.add(this.foldIcon, this.c);
	}

	private void onMousePressed()
	{
		this.parent.onHeaderMousePressed();
	}

	public void fold()
	{
		this.foldIcon.fold();
		super.setBackground(this.palette.getDefaultColor());
		this.expanded = false;
	}

	public void unfold()
	{
		this.foldIcon.unfold();
		super.setBackground(this.palette.getSelectColor());
		this.expanded = true;
	}

	public final void setQuantityLabel(final String text)
	{
		this.quantity.setText(text);
		this.quantity.setVisible(true);
	}

	public final HeaderText getNameLabel()
	{
		return this.name;
	}

	public final void addRightIcon(final JComponent iconLabel)
	{
		this.rightIconsPanel.addIcon(iconLabel);
	}

	public final void addLeftIcon(final JLabel iconLabel)
	{
		this.leftIconsPanel.addIcon(iconLabel);
	}
}
