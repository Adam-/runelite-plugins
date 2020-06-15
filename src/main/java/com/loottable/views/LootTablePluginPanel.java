package com.loottable.views;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import com.loottable.views.components.LootTablePanel;
import com.loottable.views.components.Header;

import net.runelite.client.ui.PluginPanel;

public class LootTablePluginPanel extends PluginPanel {
    private static final long serialVersionUID = 5758361368464139958L;

    private ActionListener onSearchButtonPressed;
    private Consumer<String> onSearchBarTextChanged;

    public LootTablePluginPanel(ActionListener onSearchButtonPressed, Consumer<String> onSearchBarTextChanged) {
        this.onSearchButtonPressed = onSearchButtonPressed;
        this.onSearchBarTextChanged = onSearchBarTextChanged;
        Header header = new Header("", onSearchButtonPressed, onSearchBarTextChanged);
        add(header);
    }

	public void rebuildPanel(String monsterName, Map<String, List<String[]>> allLootTable) {
        SwingUtilities.invokeLater(() -> {
            this.removeAll();
            Header header = new Header(monsterName, onSearchButtonPressed, onSearchBarTextChanged);
            LootTablePanel lootTablePanel = new LootTablePanel(allLootTable);
            add(header);
            add(lootTablePanel, BorderLayout.WEST);
        });
    }
}