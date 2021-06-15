package com.tobmistaketracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TobMistakeTrackerPlugin.CONFIG_GROUP)
public interface TobMistakeTrackerConfig extends Config
{
	@ConfigItem(
			keyName =  "spectatingEnabled",
			name = "Enable Spectacting",
			description = "Enable tracking mistakes while spectating another raid",
			position = 1
	)
	default boolean spectatingEnabled() {
		return false;
	}
}
