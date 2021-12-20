package com.banktaglayouts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("banktaglayouts")
public interface BankTagLayoutsConfig extends Config {
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
            keyName = "warnForAccidentalBankReorder",
            name = "Bank reorder warning",
            description = "Warns you know when you reorder items in your actual bank and not in a layout.",
            position = 3
    )
    default boolean warnForAccidentalBankReorder() {
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
		keyName = "preventVanillaPlaceholderMenuBug",
		name = "Prevent placeholder menu bug",
		description = "Prevents bug in the vanilla client that can prevent item withdrawal and inadvertent placeholder removal. See https://github.com/geheur/bank-tag-custom-layouts/issues/33 for more info.",
		position = 11
	)
	default boolean preventVanillaPlaceholderMenuBug() { return true; }

	@ConfigItem(
		keyName = "updateMessages",
		name = "Plugin update message",
		description = "Show a message about new features when the plugin updates.",
		position = 12
	)
	default boolean updateMessages() { return true; }

	@ConfigSection(
		name = "Auto-layout",
		description = "Auto-layout lays out your tab automatically using items from your equipment and inventory.",
		position = 100
	)
	String autoLayout = "autoLayout";

	enum LayoutStyles {
		ZIGZAG,
		PRESETS,
	}

	@ConfigItem(
            keyName = "autoLayoutStyle",
            name = "Auto-layout style",
            description = "The method auto-layout will choose.",
            position = 1,
            section = autoLayout
    )
    default LayoutStyles autoLayoutStyle() { return LayoutStyles.ZIGZAG; }

    @ConfigItem(
            keyName = "autoLayoutDuplicatesEnabled",
            name = "ZigZag: Create duplicates",
            description = "Whether or not to create duplicates when there are multiple of the same item when using auto-layout.",
            position = 2,
            section = autoLayout
    )
    default boolean autoLayoutDuplicatesEnabled() {
        return true;
    }

    @ConfigItem(
            keyName = "autoLayoutDuplicateLimit",
            name = "ZigZag: Duplicate limit",
            description = "The maximum number of items in a row to create duplicates for with auto-layout. Set to 28 to create duplicates for every item. To disable duplicate creation, toggle the \"Auto-layout: Create duplicates\" option off.",
            position = 3,
            section = autoLayout
    )
    default int autoLayoutDuplicateLimit() {
        return 4;
    }

	@ConfigItem(
		keyName = "showAutoLayoutButton",
		name = "Auto Layout button",
		description = "Disabling this hides the auto layout button and adds auto layout to the menu where you import tags.",
		position = 4,
		section = autoLayout
	)
	default boolean showAutoLayoutButton() {
		return true;
	}
}
