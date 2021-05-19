package com.recipes;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("recipes")
public interface RecipesConfig extends Config
{
	@ConfigItem(
		keyName = "showPrimaryIngredients",
		name = "Show Primary Ingredients",
		description = "Setting to see which potions an herb is a primary ingredient for."
	)
	default boolean showPrimaryIngredients()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showSecondaryIngredients",
			name = "Show Secondary Ingredients",
			description = "Setting to see which potions an item is a secondary ingredient for."
	)
	default boolean showSecondaryIngredients() {
		return true;
	}

	@ConfigItem(
			keyName="showLevelReqs",
			name = "Show Herblore level requirements",
			description = "Setting to show or hide herblore level requirements on the tooltip."
	)
	default boolean showLevelReqs() {
		return true;
	}
}
