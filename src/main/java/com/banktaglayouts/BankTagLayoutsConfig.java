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
            description = "Lets you know how to enable layouts if you drag an item in a tag tab without layout enabled, and do not currently have any layout-ed bank tag tabs.",
            position = 4
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

    @ConfigItem(
            keyName = "warnForAccidentalBankReorder",
            name = "Bank reorder warning",
            description = "Warns you know when you reorder items in your actual bank and not in a layout.",
            position = 3
    )
    default boolean warnForAccidentalBankReorder() {
        return true;
    }

    @ConfigItem(
            keyName = "showAutoLayoutButton",
            name = "Auto Layout button",
            description = "Disabling this hides the auto layout button and adds auto layout to the menu where you import tags.",
            position = 5
    )
    default boolean showAutoLayoutButton() {
        return true;
    }

    @ConfigItem(
            keyName = "useWithInventorySetups",
            name = "Use with Inventory Setups",
            description = "Allows laying out of filters applied by the Inventory Setups plugin.",
            position = 6
    )
    default boolean useWithInventorySetups() {
        return true;
    }

    @ConfigItem(
            keyName = "shiftModifierForExtraBankItemOptions",
            name = "Require Shift key for extra bank item options",
            description = "When enabled, the menu entries for adding duplicate items aren't shown unless shift is held when right-clicking",
            position = 7
    )
    default boolean shiftModifierForExtraBankItemOptions() {
        return false;
    }

	@ConfigItem(
		keyName = "autoLayoutDuplicateLimit",
		name = "Auto-layout duplicate limit",
		description = "The maximum number of items in a row to create duplicates for with auto-layout. Set to 1 to never create duplicates. Set to 28 to always create duplicates.",
		position = 8
	)
	default int autoLayoutDuplicateLimit() {
		return 4;
	}

}
