package com.herblorerecipes;

import static com.herblorerecipes.util.Utils.*;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup("herblorerecipes")
public interface HerbloreRecipesConfig extends Config
{
	@ConfigSection(
		name = "Tooltip Content Settings",
		description = "Settings for the tooltip overlay content",
		position = 0
	)
	String tooltipSection = "tooltip";

	@ConfigSection(
		name = "Keybind",
		description = "Define a custom hotkey to control the display of the overlay. Keybind changes are applied when focus is returned to the game.",
		position = 1
	)
	String keybindSection = "keybind";

	@ConfigItem(
		position = 0,
		keyName = SHOW_TOOLTIP_ON_POTIONS,
		name = "Show Tooltip on Potions (Recipes)",
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
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
		section = tooltipSection,
		description = "Setting to toggle whether the herblore recipes overlay appears on herblore items in the inventory"
	)
	default boolean showOverlayInInv()
	{
		return true;
	}

	@ConfigItem(
		position = 10,
		keyName = USE_MODIFIER_KEY,
		name = "Use Overlay Hotkey",
		section = keybindSection,
		description = "Show overlay only when pressing on bound key. After enabling you need to define a custom keybind"
	)
	default boolean useModifierKey()
	{
		return false;
	}

	@ConfigItem(
		position = 11,
		keyName = MODIFIER_KEYBIND,
		name = "Custom Overlay Hotkey",
		section = keybindSection,
		description = "Define the overlay hotkey"
	)
	default Keybind modifierKey()
	{
		return Keybind.NOT_SET;
	}
}
