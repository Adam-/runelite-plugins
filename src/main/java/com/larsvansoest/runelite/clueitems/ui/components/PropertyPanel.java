package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import java.awt.*;

/**
 * Displays a key-value pair in similar fashion to "<b>key:</b> value".
 *
 * @author Lars van Soest
 * @since 3.0.0
 */
public class PropertyPanel extends JPanel
{
	private final JLabel nameLabel;
	private final JLabel valueLabel;

	/**
	 * Creates the panel.
	 *
	 * @param palette Colour scheme for the panel.
	 * @param name    Name to display before the value.
	 * @param value   Value to display after the name.
	 */
	public PropertyPanel(final EmoteClueItemsPalette palette, final String name, final String value)
	{
		super(new GridBagLayout());
		super.setBackground(palette.getFoldContentColor());

		this.nameLabel = new JShadowedLabel();
		this.setName(name);
		this.nameLabel.setFont(FontManager.getRunescapeSmallFont());
		this.nameLabel.setForeground(palette.getPropertyNameColor());
		this.nameLabel.setHorizontalAlignment(JLabel.CENTER);

		this.valueLabel = new JLabel();
		this.setValue(value);
		this.valueLabel.setFont(FontManager.getRunescapeSmallFont());
		this.valueLabel.setForeground(palette.getPropertyValueColor());
		this.valueLabel.setHorizontalAlignment(JLabel.CENTER);

		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets.left = 5;
		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		super.add(this.nameLabel, c);

		c.gridx++;
		super.add(this.valueLabel, c);
	}

	/**
	 * Sets the name of the property panel.
	 *
	 * @param name the new name to display before the value.
	 */
	public void setName(final String name)
	{
		this.nameLabel.setText(String.format("%s:", name));
	}

	/**
	 * Sets the value of the property panel.
	 *
	 * @param value the new value to display after the name.
	 */
	public void setValue(final String value)
	{
		this.valueLabel.setText(value.toLowerCase());
	}
}
