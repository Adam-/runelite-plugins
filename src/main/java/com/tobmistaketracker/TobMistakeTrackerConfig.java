package com.tobmistaketracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TobMistakeTrackerPlugin.CONFIG_GROUP)
public interface TobMistakeTrackerConfig extends Config {

    @ConfigItem(
            keyName = "showMistakesInChat",
            name = "Show Mistakes In Chat",
            description = "When a player makes a mistake in ToB, whether or not to put it in public chat for other " +
                    "raiders who have the plugin to see.",
            position = 1
    )
    default boolean showMistakesInChat() {
        return true;
    }

    @ConfigItem(
            keyName = "showMistakesOnOverheadText",
            name = "Show Mistakes On Overhead Text",
            description = "When a player makes a mistake in ToB, whether or not to show the overhead text for other " +
                    "raiders who have the plugin to see.",
            position = 2
    )
    default boolean showMistakesOnOverheadText() {
        return true;
    }
}
