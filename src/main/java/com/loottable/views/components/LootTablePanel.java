package com.loottable.views.components;

import java.awt.GridLayout;
import java.awt.BorderLayout;

import java.util.List;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.BoxLayout;

public class LootTablePanel extends JPanel {
	private static final long serialVersionUID = 1132676497548426861L;

	public LootTablePanel(List<String[]> lootTable) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel lootTablePanel = new JPanel(new GridLayout(lootTable.size(), 1));

        ListIterator<String[]> iterator = lootTable.listIterator();
        while (iterator.hasNext()) {
            String[] lootRow = iterator.next();
            ItemPanel itemPanel = new ItemPanel(lootRow[0], lootRow[1], lootRow[2], lootRow[3], lootRow[4]);
            lootTablePanel.add(itemPanel);
        }
        
        add(lootTablePanel, BorderLayout.WEST);
    }
}