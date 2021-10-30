package com.BetterGodwarsOverlay;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("BetterGodwarsOverlayPlugin")
public interface BetterGodwarsOverlayConfig extends Config
{
	@ConfigItem(
		keyName = "ShortGodNames",
		name = "Shorten God Names",
		description = "Shorten god names on the killcount overlay",
		position = 1
	)
	default boolean shortGodNames()
	{
		return false;
	}

	@ConfigItem(
		keyName = "godNameColour",
		name = "God Name Colour",
		description = "Change the colour of the god names displayed on the overlay",
		position = 2
	)
	default Color godNameColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		keyName = "highlightOnKC",
		name = "Highlight On Entry Kill Count",
		description = "Enter the KC required for room entry and it will highlight it green when you have enough",
		position = 3
	)
	default int highlightOnKC()
	{
		return 40;
	}

	@ConfigItem(keyName = "highlightOnKCColour",
		name = "Killcount Highlight Colour",
		description = "Change the colour of the killcount highlight displayed on the overlay",
		position = 4)
	default Color highlightOnKCColor()
	{
		return Color.GREEN;
	}
}
