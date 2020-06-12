package com.loottable;

import com.google.inject.Provides;

import com.loottable.controllers.LootTableController;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;

@Slf4j
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

	@Provides
	LootTableConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(LootTableConfig.class);
	}
}
