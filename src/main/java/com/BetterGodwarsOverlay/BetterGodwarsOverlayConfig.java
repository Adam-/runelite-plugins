package com.BetterGodwarsOverlay;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("BetterGodwarsOverlayPlugin")
public interface BetterGodwarsOverlayConfig extends Config
{

	@ConfigSection(
		name = "General",
		description = "General plugin features can be modified here",
		position = 0
	)
	String generalSection = "general";

	@ConfigSection(
		name = "Hide Gods",
		description = "Toggle options to prevent specific god killcounts appearing on the overlay",
		position = 1
	)
	String hideSection = "hide";

	@ConfigItem(
		keyName = "ShortGodNames",
		name = "Shorten God Names",
		description = "Shorten god names on the killcount overlay",
		section = generalSection
	)
	default boolean shortGodNames()
	{
		return false;
	}

	@ConfigItem(
		keyName = "godNameColour",
		name = "God Name Colour",
		description = "Change the colour of the god names displayed on the overlay",
		section = generalSection
	)
	default Color godNameColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		keyName = "highlightOnKC",
		name = "Highlight On Entry Kill Count",
		description = "Enter the KC required for room entry and it will highlight it green when you have enough",
		section = generalSection
	)
	default int highlightOnKC()
	{
		return 40;
	}

	@ConfigItem(keyName = "highlightOnKCColour",
		name = "Killcount Highlight Colour",
		description = "Change the colour of the killcount highlight displayed on the overlay",
		section = generalSection)
	default Color highlightOnKCColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "hideArmadyl",
		name = "Hide Armadyl",
		description = "Hide Armadyl killcount from the overlay",
		section = hideSection
	)
	default boolean hideArmadyl()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideBandos",
		name = "Hide Bandos",
		description = "Hide Bandos killcount from the overlay",
		section = hideSection
	)

	default boolean hideBandos()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideSaradmomin",
		name = "Hide Saradomin",
		description = "Hide Saradomin killcount from the overlay",
		section = hideSection
	)

	default boolean hideSaradomin()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideZamorak",
		name = "Hide Zamorak",
		description = "Hide Zamorak killcount from the overlay",
		section = hideSection
	)

	default boolean hideZamorak()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideAncient",
		name = "Hide Ancient",
		description = "Hide Ancient killcount from the overlay",
		section = hideSection
	)

	default boolean hideAncient()
	{
		return false;
	}
}
