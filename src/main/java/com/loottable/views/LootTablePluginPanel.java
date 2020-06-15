package com.loottable.views;

import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import java.awt.BorderLayout;

import com.loottable.views.components.LootTablePanel;
import com.loottable.views.components.Header;

import net.runelite.client.ui.PluginPanel;

public class LootTablePluginPanel extends PluginPanel {
    private static final long serialVersionUID = 5758361368464139958L;

	public void rebuildPanel(String monsterName, Map<String, List<String[]>> allLootTable) {
        SwingUtilities.invokeLater(() -> {
            this.removeAll();
            Header header = new Header(monsterName);
            LootTablePanel lootTablePanel = new LootTablePanel(allLootTable);
            add(header);
            add(lootTablePanel, BorderLayout.WEST);
        });
    }
}