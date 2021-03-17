package com.banktaglayouts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("banktaglayouts")
public interface BankTagLayoutsConfig extends Config {
    @ConfigItem(
            keyName = "showLayoutPlaceholders",
            name = "Show Layout Placeholders",
            description = "Show the location of items that are in the layout and in the tag, but not in your bank.",
            position = 2
    )
    default boolean showLayoutPlaceholders()
    {
        return true;
    }

    @ConfigItem(
            keyName = "tutorialMessage",
            name = "Layout enable tutorial message",
            description = "Lets you know how to enable layouts if you drag an item in a tag tab without layout enabled.",
            position = 3
    )
    default boolean tutorialMessage() {
        return true;
    }

    @ConfigItem(
            keyName = "layoutEnabledByDefault",
            name = "Enable layout by default",
            description = "When opening a tag tab without layout enabled, automatically enable layout on the tab.",
            position = 1
    )
    default boolean layoutEnabledByDefault() {
        return false;
    }
}
