package com.loottable;

import com.google.inject.Provides;

import com.loottable.controllers.LootTableController;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;

@PluginDescriptor(name = "loottable")
public class LootTablePlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private LootTableConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private LootTableController lootTableController;

	@Override
	protected void startUp() throws Exception {
		lootTableController = new LootTableController(clientToolbar);
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event) {
		lootTableController.onMenuOpened(event, client);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		lootTableController.onMenuOptionClicked(event);
	}

	@Provides
	LootTableConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(LootTableConfig.class);
	}
}
