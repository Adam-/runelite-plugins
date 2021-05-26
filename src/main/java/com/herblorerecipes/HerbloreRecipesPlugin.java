package com.herblorerecipes;

import com.google.inject.Provides;
import com.herblorerecipes.cache.HerbloreRecipesCacheLoader;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Herblore Recipes",
	description = "Hover over a herblore ingredient or potion in your inventory or bank to see which potions can be made with it or that potion's recipe",
	tags = {"recipes", "herblore", "herb"}
)
public class HerbloreRecipesPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private HerbloreRecipesConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HerbloreRecipesOverlay herbloreRecipesOverlay;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(herbloreRecipesOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(herbloreRecipesOverlay);
	}

	@Provides
	HerbloreRecipesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HerbloreRecipesConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("showSecondariesInTooltip".equals(event.getKey()) ||
			"showPrimariesInTooltip".equals(event.getKey()) ||
			"showLevelReqsInTooltip".equals(event.getKey()))
		{
			HerbloreRecipesCacheLoader.clearCache();
		}
	}
}
