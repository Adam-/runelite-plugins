package com.larsvansoest.runelite.clueitems;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.larsvansoest.runelite.clueitems.data.EmoteClueItemsProvider;
import com.larsvansoest.runelite.clueitems.overlay.EmoteClueItemOverlay;
import com.larsvansoest.runelite.clueitems.overlay.icons.EmoteClueIconProvider;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Emote Clue Items",
	description = "Highlight required items for emote clue steps.",
	tags = {"emote", "clue", "item", "items", "scroll"},
	enabledByDefault = true
)
public class EmoteClueItemsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EmoteClueItemsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	private EmoteClueItemOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		// Load images
		EmoteClueIconProvider clueIconProvider = new EmoteClueIconProvider();
		clueIconProvider.fetchBuffers();

		// Load clue item data
		EmoteClueItemsProvider emoteClueItemsProvider = new EmoteClueItemsProvider();
		emoteClueItemsProvider.loadItems();

		this.overlay = new EmoteClueItemOverlay(itemManager, emoteClueItemsProvider, clueIconProvider);
		this.overlayManager.add(this.overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(this.overlay);
	}

	@Provides
	EmoteClueItemsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EmoteClueItemsConfig.class);
	}
}
