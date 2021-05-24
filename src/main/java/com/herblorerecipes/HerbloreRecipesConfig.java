package com.herblorerecipes;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("herblorerecipes")
public interface HerbloreRecipesConfig extends Config
{

	@ConfigItem(
		position = 0,
		keyName = "showPotionRecipes",
		name = "Show Potion Recipes",
		description = "Display the ingredients to make the potion on hover."
	)
	default boolean showPotionRecipes()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "showPrimaryIngredients",
		name = "Show Tooltip on Primaries",
		description = "Toggle recipe tooltip on primary ingredients."
	)
	default boolean showPrimaryIngredients()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "showSecondaryIngredients",
		name = "Show Tooltip on Secondaries",
		description = "Toggle recipe tooltip on secondary ingredients."
	)
	default boolean showSecondaryIngredients()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "showUnfinishedPotions",
		name = "Show Tooltip on Unfinished Potions",
		description = "Toggle recipe tooltip on unfinished potions."
	)
	default boolean showUnfinishedPotions()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "showPrimaryIngredientsAlongsidePrimaries",
		name = "Show Primary Ingredients",
		description = "Toggle primary ingredients alongside secondary ingredients in tooltip. This will clear the tooltip cache."
	)
	default boolean showPrimaryIngredientsAlongsideSecondaries()
	{
		return true;
	}

	@ConfigItem(
		position = 5,
		keyName = "showSecondaryIngredientsAlongsidePrimaries",
		name = "Show Secondary Ingredients",
		description = "Toggle secondary ingredients alongside primary ingredients in tooltip. This will clear the tooltip cache."
	)
	default boolean showSecondaryIngredientsAlongsidePrimaries()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "showLevelReqs",
		name = "Show Herblore level requirements",
		description = "Setting to show or hide herblore level requirements on the tooltip. This will clear the tooltip cache"
	)
	default boolean showLevelReqs()
	{
		return true;
	}
}
