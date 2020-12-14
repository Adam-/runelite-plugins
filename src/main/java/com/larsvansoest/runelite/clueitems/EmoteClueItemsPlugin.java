package com.larsvansoest.runelite.clueitems;

import com.google.inject.Provides;
import com.larsvansoest.runelite.clueitems.overlay.config.ConfigProvider;
import javax.inject.Inject;

import com.larsvansoest.runelite.clueitems.data.ItemsProvider;
import com.larsvansoest.runelite.clueitems.overlay.EmoteClueItemOverlay;
import com.larsvansoest.runelite.clueitems.overlay.icons.IconProvider;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Emote Clue Items",
	description = "Highlight required items for emote clue steps.",
	tags = {"emote", "clue", "item", "items", "scroll"}
)
public class EmoteClueItemsPlugin extends Plugin
{
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
		IconProvider clueIconProvider = new IconProvider();
		clueIconProvider.fetchBuffers();

		ItemsProvider emoteClueItemsProvider = new ItemsProvider();
		emoteClueItemsProvider.loadItems();

		ConfigProvider configProvider = new ConfigProvider(config);

		this.overlay = new EmoteClueItemOverlay(itemManager, configProvider, emoteClueItemsProvider, clueIconProvider);
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
