package com.loottable.views.components;

import java.awt.GridLayout;
import java.awt.BorderLayout;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.runelite.client.ui.ColorScheme;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

public class LootTablePanel extends JPanel {
	private static final long serialVersionUID = 1132676497548426861L;

	public LootTablePanel(Map<String, List<String[]>> allLootTables) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        for (Map.Entry<String, List<String[]>> entry : allLootTables.entrySet()) {
            String tableHeaderString = entry.getKey();
            TableHeader tableHeader = new TableHeader(tableHeaderString);
            container.add(tableHeader);

            List<String[]> lootTable = entry.getValue();
            ListIterator<String[]> iterator = lootTable.listIterator();
            while (iterator.hasNext()) {
                String[] lootRow = iterator.next();
                ItemPanel itemPanel = new ItemPanel(lootRow[0], lootRow[1], lootRow[2], lootRow[3], lootRow[4]);
                container.add(itemPanel);
            }
        }

        add(container, BorderLayout.WEST);
    }
}