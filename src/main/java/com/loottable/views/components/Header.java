package com.loottable.views.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class Header extends JPanel {
	private static final long serialVersionUID = -5426065729242203114L;

	public Header(String monsterName) {
        JLabel monsterNameLabel = new JLabel(monsterName);
        monsterNameLabel.setFont(FontManager.getRunescapeBoldFont());
        monsterNameLabel.setForeground(ColorScheme.BRAND_ORANGE);
        monsterNameLabel.setHorizontalAlignment(JLabel.CENTER);
        Border border = BorderFactory.createMatteBorder(0, 0, 3, 0, ColorScheme.BRAND_ORANGE);
        setBorder(border);
        add(monsterNameLabel);
    }
}