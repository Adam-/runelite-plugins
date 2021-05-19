package com.recipes;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
	name = "Recipes"
)
public class RecipesPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RecipesConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RecipesOverlay recipesOverlay;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Recipes started!");
		overlayManager.add(recipesOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Recipes stopped!");
		overlayManager.remove(recipesOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{

	}

	@Provides
	RecipesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RecipesConfig.class);
	}
}
