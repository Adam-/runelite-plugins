package com.larsvansoest.runelite.clueitems;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.larsvansoest.runelite.clueitems.data.EmoteClueItemsProvider;
import com.larsvansoest.runelite.clueitems.overlay.ClueItemOverlay;
import com.larsvansoest.runelite.clueitems.overlay.icon.ClueIconProvider;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Clue Items"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private OverlayManager overlayManager;

	private ClueItemOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		ClueIconProvider clueIconProvider = new ClueIconProvider();
		clueIconProvider.fetchBuffers();

		EmoteClueItemsProvider emoteClueItemsProvider = new EmoteClueItemsProvider();
		emoteClueItemsProvider.loadItems();

		this.overlay = new ClueItemOverlay(emoteClueItemsProvider, clueIconProvider);

		this.overlayManager.add(this.overlay);

		log.info("Overlay booted.");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(this.overlay);
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
