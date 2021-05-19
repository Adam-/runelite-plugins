package com.herblorerecipes;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("herblorerecipes")
public interface HerbloreRecipesConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "showPrimaryIngredients",
            name = "Show Tooltip on Primaries",
            description = "Toggle recipe tooltip on primary ingredients."
    )
    default boolean showPrimaryIngredients()
    {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "showSecondaryIngredients",
            name = "Show Tooltip on Secondaries",
            description = "Toggle recipe tooltip on secondary ingredients."
    )
    default boolean showSecondaryIngredients()
    {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "showSecondaryIngredientsAlongsidePrimaries",
            name = "Show Secondary Ingredients",
            description = "Toggle secondary ingredients alongside primary ingredients in tooltip. This will invalidate the tooltip cache."
    )
    default boolean showSecondaryIngredientsAlongsidePrimaries()
    {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "showLevelReqs",
            name = "Show Herblore level requirements",
            description = "Setting to show or hide herblore level requirements on the tooltip. This will invalidate the tooltip cache"
    )
    default boolean showLevelReqs()
    {
        return true;
    }
}
