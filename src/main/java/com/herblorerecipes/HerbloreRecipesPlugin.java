package com.herblorerecipes;

import com.google.inject.Provides;
import com.herblorerecipes.cache.HerbloreRecipesCacheLoader;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "Herblore Recipes",
        description = "Find out what potions herbs are an ingredient of.",
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
        if ("showSecondaryIngredientsAlongsidePrimaries".equals(event.getKey()) || "showLevelReqs".equals(event.getKey()))
        {
            HerbloreRecipesCacheLoader.clearCache();
        }
    }
}
