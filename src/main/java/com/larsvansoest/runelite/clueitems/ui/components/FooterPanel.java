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
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FooterPanel extends JPanel
{
	public FooterPanel(final EmoteClueItemsPalette emoteClueItemsPalette, final String pluginName, final String pluginVersion, final String gitHubUrl)
	{
		super(new GridBagLayout());

		final Color color = emoteClueItemsPalette.getFooterColor();
		final Font font = FontManager.getRunescapeSmallFont();

		final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setBackground(color);

		final JLabel pluginNameLabel = this.getTextLabel(String.format("%s %s", pluginName, pluginVersion), font, color);
		final JLabel gitHubLabel = this.getGitHubLabel(gitHubUrl);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		super.add(separator, c);
		c.insets.top = 5;
		c.gridy++;
		c.gridwidth = 1;
		c.insets.left = 1;
		c.insets.right = 1;
		c.anchor = GridBagConstraints.EAST;
		super.add(pluginNameLabel, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		c.insets.bottom = 2;
		super.add(gitHubLabel, c);
	}

	private JLabel getTextLabel(final String string, final Font font, final Color color)
	{
		final JLabel label = new JLabel();
		label.setText(string);
		label.setHorizontalAlignment(JLabel.RIGHT);
		label.setVerticalAlignment(JLabel.CENTER);
		label.setFont(font);
		label.setForeground(color);
		return label;
	}

	private JLabel getGitHubLabel(final String gitHubUrl)
	{
		final ImageIcon defaultIcon = new ImageIcon(EmoteClueImages.Toolbar.Footer.GITHUB);
		final ImageIcon illuminatedIcon = new ImageIcon(EmoteClueImages.illuminate(EmoteClueImages.Toolbar.Footer.GITHUB, 150));
		final JLabel gitHubLabel = new JLabel();
		gitHubLabel.setToolTipText("Visit the GitHub repository webpage.");
		gitHubLabel.setHorizontalAlignment(JLabel.LEFT);
		gitHubLabel.setVerticalAlignment(JLabel.CENTER);
		gitHubLabel.setIcon(defaultIcon);
		gitHubLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				LinkBrowser.browse(gitHubUrl);
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				gitHubLabel.setIcon(illuminatedIcon);
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				gitHubLabel.setIcon(defaultIcon);
			}
		});
		return gitHubLabel;
	}
}
