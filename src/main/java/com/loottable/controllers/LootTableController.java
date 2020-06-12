package com.loottable.controllers;

import java.util.List;

import com.loottable.helpers.ScrapeWiki;
import com.loottable.helpers.UiUtilities;
import com.loottable.views.LootTablePluginPanel;

import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

public class LootTableController {
    private ClientToolbar clientToolbar;
    private LootTablePluginPanel lootTablePluginPanel;
    private NavigationButton navButton;

    public LootTableController(ClientToolbar clientToolbar) {
        this.clientToolbar = clientToolbar;
        lootTablePluginPanel = new LootTablePluginPanel();
        setUpNavigationButton();
        String testMonsterName = "Fire Giant";
        List<String[]> lootTable = ScrapeWiki.scrapeWiki(testMonsterName);
        lootTablePluginPanel.rebuildPanel(testMonsterName, lootTable);
    }

    private void setUpNavigationButton() {
        navButton = NavigationButton
            .builder()
            .tooltip("Loot Tables")
            .icon(
                ImageUtil.getResourceStreamFromClass(
                    getClass(), 
                    UiUtilities.lootTableNavIcon
                )
            )
            .priority(2)
            .panel(lootTablePluginPanel)
            .build();
        clientToolbar.addNavigation(navButton);
    }
}