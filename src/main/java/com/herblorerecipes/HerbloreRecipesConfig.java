package com.herblorerecipes;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("herblorerecipes")
public interface HerbloreRecipesConfig extends Config
{

	@ConfigItem(
		position = 0,
		keyName = "showTooltipOnPotions",
		name = "Show Tooltip on Potions (Recipes)",
		description = "Display the ingredients to make the potion on hover."
	)
	default boolean showTooltipOnPotions()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "showTooltipOnPrimaries",
		name = "Show Tooltip on Primaries",
		description = "Toggle recipe tooltip on primary ingredients."
	)
	default boolean showTooltipOnPrimaries()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "showTooltipOnSecondaries",
		name = "Show Tooltip on Secondaries",
		description = "Toggle recipe tooltip on secondary ingredients."
	)
	default boolean showTooltipOnSecondaries()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "showTooltipOnUnfinished",
		name = "Show Tooltip on Unfinished Potions",
		description = "Toggle recipe tooltip on unfinished potions."
	)
	default boolean showTooltipOnUnfinished()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "showTooltipOnSeeds",
		name = "Show Tooltip on Seeds",
		description = "Toggle recipe tooltip on seeds."
	)
	default boolean showTooltipOnSeeds()
	{
		return true;
	}

	@ConfigItem(
		position = 5,
		keyName = "showPrimariesInTooltip",
		name = "Show Primary Ingredients in Tooltip",
		description = "Toggle primary ingredients alongside secondary ingredients in tooltip. This will clear the tooltip cache."
	)
	default boolean showPrimariesInTooltip()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "showSecondariesInTooltip",
		name = "Show Secondary Ingredients in Tooltip",
		description = "Toggle secondary ingredients alongside primary ingredients in tooltip. This will clear the tooltip cache."
	)
	default boolean showSecondariesInTooltip()
	{
		return true;
	}

	@ConfigItem(
		position = 7,
		keyName = "showLevelReqsInTooltip",
		name = "Show Herblore level requirements in Tooltip",
		description = "Setting to show or hide herblore level requirements on the tooltip. This will clear the tooltip cache"
	)
	default boolean showLevelReqsInTooltip()
	{
		return true;
	}
}
