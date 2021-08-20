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

package com.larsvansoest.runelite.clueitems.ui.search;

import net.runelite.client.input.KeyListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.IconTextField;

import java.awt.*;
import java.awt.event.KeyEvent;

public class SearchBarFactory
{
	private final Runnable onChange;

	private Color defaultColor;
	private Color hoverColor;

	public SearchBarFactory(final Runnable onChange)
	{
		this.onChange = onChange;
		this.defaultColor = ColorScheme.DARKER_GRAY_COLOR;
		this.hoverColor = ColorScheme.DARKER_GRAY_COLOR;
	}

	public SearchBarFactory defaultColor(final Color color)
	{
		this.defaultColor = color;
		return this;
	}

	public SearchBarFactory hoverColor(final Color color)
	{
		this.hoverColor = color;
		return this;
	}

	public IconTextField build()
	{
		final IconTextField searchBar = new IconTextField();
		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setBackground(this.defaultColor);
		searchBar.setHoverBackgroundColor(this.hoverColor);
		searchBar.setFont(FontManager.getRunescapeSmallFont());
		searchBar.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(final KeyEvent e)
			{
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
				SearchBarFactory.this.onChange.run();
			}
		});
		searchBar.addClearListener(this.onChange);
		return searchBar;
	}
}
