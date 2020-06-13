package com.loottable.views;

import java.util.List;

import javax.swing.SwingUtilities;

import java.awt.BorderLayout;

import com.loottable.views.components.LootTablePanel;

import net.runelite.client.ui.PluginPanel;

public class LootTablePluginPanel extends PluginPanel {
    private static final long serialVersionUID = 5758361368464139958L;
    
    public LootTablePluginPanel() {
        setLayout(new BorderLayout());
    }

	public void rebuildPanel(String monsterName, List<String[]> lootTable, String filterText) {
        SwingUtilities.invokeLater(() -> {
            this.removeAll();
            LootTablePanel lootTablePanel = new LootTablePanel(lootTable);
            add(lootTablePanel, BorderLayout.WEST);
        });
    }
}