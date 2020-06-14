package com.loottable.views;

import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import java.awt.BorderLayout;

import com.loottable.views.components.LootTablePanel;

import net.runelite.client.ui.PluginPanel;

public class LootTablePluginPanel extends PluginPanel {
    private static final long serialVersionUID = 5758361368464139958L;
    
    public LootTablePluginPanel() {
    }

	public void rebuildPanel(String monsterName, Map<String, List<String[]>> allLootTable, String filterText) {
        SwingUtilities.invokeLater(() -> {
            this.removeAll();
            

            LootTablePanel lootTablePanel = new LootTablePanel(allLootTable);
            add(lootTablePanel, BorderLayout.WEST);
        });
    }
}