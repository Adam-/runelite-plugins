package com.tobmistaketracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TobMistakeTrackerPlugin.CONFIG_GROUP)
public interface TobMistakeTrackerConfig extends Config {

    @ConfigItem(
            keyName = "isDebug",
            name = "Debug",
            description = "Toggle Debug Mode",
            position = 99,
            hidden = false // This must be true when pushing to prod
    )
    default boolean isDebug() {
        return false;
    }

    // TODO: Add a config for public chat messages and/or overheads. It might be annoying for people.
}
