package com.loottable.controllers;

import java.util.List;
import java.util.Map;

import com.loottable.helpers.ScrapeWiki;
import com.loottable.helpers.UiUtilities;
import com.loottable.views.LootTablePluginPanel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

public class LootTableController {
    private ClientToolbar clientToolbar;
    private LootTablePluginPanel lootTablePluginPanel;
    private NavigationButton navButton;
    private String monsterName;

    final private String LOOT_TABLE_MENU_OPTION = "Loot Table";

    public LootTableController(ClientToolbar clientToolbar) {
        this.clientToolbar = clientToolbar;
        lootTablePluginPanel = new LootTablePluginPanel();
        setUpNavigationButton();
        this.monsterName = null;
    }

    /**
     * Adds "Loot Table" option if "Attack" option is present
     * @todo issue with players when "Attack" option is available
     */
    public void onMenuOpened(MenuOpened event, Client client) {
        MenuEntry[] menuEntries = event.getMenuEntries();
        // Look for Attack option
        for (MenuEntry menuEntry : menuEntries) {
            if (menuEntry.getOption().equals("Attack")) {
                // String monsterName = menuEntry.getTarget();
                int widgetId = menuEntry.getParam1();
                String monsterName = menuEntry.getTarget();
                final MenuEntry lootTableMenuEntry = new MenuEntry();
                lootTableMenuEntry.setOption(LOOT_TABLE_MENU_OPTION);
                lootTableMenuEntry.setTarget(monsterName);
                lootTableMenuEntry.setIdentifier(menuEntry.getIdentifier());
                lootTableMenuEntry.setParam1(widgetId);
                lootTableMenuEntry.setType(MenuAction.RUNELITE.getId());
                client.setMenuEntries(ArrayUtils.addAll(menuEntries, lootTableMenuEntry));
                return;
            }
        }
    }

    /**
     * menuOptionTarget structured like <col=ffff00>Monk<col=ff00>  (level-2)
     * We just want Monk to be returns
     * @param menuOptionTarget
     * @return
     */
    public String parseMenuTarget(String menuOptionTarget) {
        return StringUtils.substringBetween(menuOptionTarget, ">", "<");
    }

    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals(LOOT_TABLE_MENU_OPTION)) {
            this.monsterName = parseMenuTarget(event.getMenuTarget());
            Map<String, List<String[]>> allLootTables = ScrapeWiki.scrapeWiki(this.monsterName);
            lootTablePluginPanel.rebuildPanel(this.monsterName, allLootTables);
        }
    }

    public void onSearchBarTextChange(String newText) {

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