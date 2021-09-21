package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Displays a title and description. The title has a shadowed markup and is supported by a separator. Underneath this separator, the description is html-formatted and displayed using html automatic line breaks.
 *
 * @author Lars van Soest
 * @since 3.0.0
 */
public class DescriptionPanel extends JPanel
{
	/**
	 * Creates the panel.
	 *
	 * @param palette     Colour scheme for the panel.
	 * @param title       Title to display above the separator.
	 * @param description Description to display underneath the separator.
	 */
	public DescriptionPanel(final EmoteClueItemsPalette palette, final String title, final String description)
	{
		super(new GridBagLayout());
		super.setBackground(palette.getFoldContentColor());

		final JLabel header = new JShadowedLabel(title);
		header.setFont(FontManager.getRunescapeSmallFont());
		header.setHorizontalAlignment(JLabel.LEFT);
		header.setForeground(palette.getPropertyNameColor());

		final JLabel content = new JLabel(String.format("<html><p style=\"width:100%%\">%s</p></html>", description));
		content.setFont(FontManager.getRunescapeSmallFont());
		content.setHorizontalAlignment(JLabel.LEFT);
		content.setForeground(palette.getPropertyValueColor());

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		header.setBorder(new MatteBorder(0, 0, 1, 0, palette.getPropertyValueColor()));
		super.add(header, c);

		c.insets.top = 3;
		c.insets.bottom = 3;
		c.gridy++;
		super.add(content, c);
	}
}
