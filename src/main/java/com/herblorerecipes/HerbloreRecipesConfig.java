package com.herblorerecipes;

import static com.herblorerecipes.util.Utils.*;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("herblorerecipes")
public interface HerbloreRecipesConfig extends Config
{

	@ConfigItem(
		position = 0,
		keyName = SHOW_TOOLTIP_ON_POTIONS,
		name = "Show Tooltip on Potions (Recipes)",
		description = "Display the ingredients to make the potion on hover."
	)
	default boolean showTooltipOnPotions()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = SHOW_TOOLTIP_ON_PRIMARIES,
		name = "Show Tooltip on Primaries",
		description = "Toggle recipe tooltip on primary ingredients."
	)
	default boolean showTooltipOnPrimaries()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = SHOW_TOOLTIP_ON_SECONDARIES,
		name = "Show Tooltip on Secondaries",
		description = "Toggle recipe tooltip on secondary ingredients."
	)
	default boolean showTooltipOnSecondaries()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = SHOW_TOOLTIP_ON_UNFINISHED,
		name = "Show Tooltip on Unfinished Potions",
		description = "Toggle recipe tooltip on unfinished potions."
	)
	default boolean showTooltipOnUnfinished()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = SHOW_TOOLTIP_ON_SEEDS,
		name = "Show Tooltip on Seeds",
		description = "Toggle recipe tooltip on seeds."
	)
	default boolean showTooltipOnSeeds()
	{
		return true;
	}

	@ConfigItem(
		position = 5,
		keyName = SHOW_PRIMARIES_IN_TOOLTIP,
		name = "Show Primary Ingredients in Tooltip",
		description = "Toggle primary ingredients alongside secondary ingredients in tooltip. This will clear the tooltip cache."
	)
	default boolean showPrimariesInTooltip()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = SHOW_SECONDARIES_IN_TOOLTIP,
		name = "Show Secondary Ingredients in Tooltip",
		description = "Toggle secondary ingredients alongside primary ingredients in tooltip. This will clear the tooltip cache."
	)
	default boolean showSecondariesInTooltip()
	{
		return true;
	}

	@ConfigItem(
		position = 7,
		keyName = SHOW_LEVEL_REQS_IN_TOOLTIP,
		name = "Show Herblore level requirements in Tooltip",
		description = "Setting to show or hide herblore level requirements on the tooltip. This will clear the tooltip cache"
	)
	default boolean showLevelReqsInTooltip()
	{
		return true;
	}

	@ConfigItem(
		position = 8,
		keyName = SHOW_OVERLAY_IN_BANK,
		name = "Show Recipes Overlay in Bank",
		description = "Setting to toggle whether the herblore recipes overlay appears on herblore items in the bank"
	)
	default boolean showOverlayInBank()
	{
		return true;
	}

	@ConfigItem(
		position = 9,
		keyName = SHOW_OVERLAY_IN_INV,
		name = "Show Recipes Overlay in Inventory",
		description = "Setting to toggle whether the herblore recipes overlay appears on herblore items in the inventory"
	)
	default boolean showOverlayInInv()
	{
		return true;
	}
}
