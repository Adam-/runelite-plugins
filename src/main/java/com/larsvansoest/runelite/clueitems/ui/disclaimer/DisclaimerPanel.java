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

package com.larsvansoest.runelite.clueitems.ui.disclaimer;

import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.Palette;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DisclaimerPanel extends JPanel
{
	private final JLabel textLabel;

	public DisclaimerPanel(final Palette palette, final Runnable onClick)
	{
		super(new GridBagLayout());
		super.setBackground(palette.getDisclaimerColor());

		final JLabel questionCircleIconLabel = new JLabel(new ImageIcon(EmoteClueImages.Toolbar.Disclaimer.QUESTION_CIRCLE));

		this.textLabel = new JShadowedLabel();
		this.textLabel.setHorizontalAlignment(JLabel.LEFT);
		this.textLabel.setVerticalAlignment(JLabel.CENTER);
		this.textLabel.setFont(FontManager.getRunescapeSmallFont());

		final Icon closeIllumatedIcon = new ImageIcon(EmoteClueImages.illuminate(EmoteClueImages.Toolbar.Disclaimer.CLOSE, 150));
		final Icon closeIcon = new ImageIcon(EmoteClueImages.Toolbar.Disclaimer.CLOSE);
		final JLabel closeIconLabel = new JLabel(closeIcon);
		closeIconLabel.setToolTipText("Close");
		closeIconLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				onClick.run();
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				closeIconLabel.setIcon(closeIllumatedIcon);
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				closeIconLabel.setIcon(closeIcon);
			}
		});


		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 10, 5, 0);
		c.weightx = 0;
		c.weighty = 0;
		super.add(questionCircleIconLabel, c);

		c.gridx++;
		c.weightx = 1;
		c.weighty = 1;
		super.add(this.textLabel, c);

		c.gridx++;
		c.weightx = 0;
		c.weighty = 0;
		c.insets.right = 10;
		super.add(closeIconLabel, c);
	}

	public void setText(final String text)
	{
		this.textLabel.setText(String.format("<html><p style=\"width:100%%\">%s</p></html>", text));
	}
}
