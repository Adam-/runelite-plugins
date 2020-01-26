package com.adriansoftware;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Panel shown when there is no account data available to display.
 */
public class DefaultBankValuePanel extends PluginPanel
{
	public void init()
	{
		JLabel label = new JLabel("<html>No account data found.<br/>Log in to start tracking.</html>");
		label.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		label.setHorizontalAlignment(JLabel.CENTER);
		add(label, BorderLayout.NORTH);
	}
}
