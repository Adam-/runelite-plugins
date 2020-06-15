package com.loottable.views.components;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class TableHeader extends JPanel {
	private static final long serialVersionUID = 3175481576622397397L;

	public TableHeader(String tableHeader) {
        JLabel tableHeaderLabel = new JLabel(tableHeader);
        tableHeaderLabel.setFont(FontManager.getRunescapeBoldFont());
        tableHeaderLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        tableHeaderLabel.setHorizontalAlignment(JLabel.CENTER);
        add(tableHeaderLabel);
    }
}