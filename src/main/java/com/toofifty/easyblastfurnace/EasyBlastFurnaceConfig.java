package com.toofifty.easyblastfurnace;

import com.toofifty.easyblastfurnace.config.HighlightOverlayTextSetting;
import com.toofifty.easyblastfurnace.config.ItemOverlaySetting;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("easy-blastfurnace")
public interface EasyBlastFurnaceConfig extends Config
{
    @ConfigItem(
        position = 1,
        keyName = "itemOverlayMode",
        name = "Item overlay mode",
        description = "Select how highlighted items appear"
    )
    default ItemOverlaySetting itemOverlayMode()
    {
        return ItemOverlaySetting.OUTLINE;
    }

    @ConfigItem(
        position = 2,
        keyName = "itemOverlayTextMode",
        name = "Item overlay text mode",
        description = "Select where to display tooltip text for items"
    )
    default HighlightOverlayTextSetting itemOverlayTextMode()
    {
        return HighlightOverlayTextSetting.BELOW;
    }

    @ConfigItem(
        position = 3,
        keyName = "itemOverlayColor",
        name = "Item overlay color",
        description = "Change the color of the item overlay"
    )
    default Color itemOverlayColor()
    {
        return Color.CYAN;
    }

    @ConfigItem(
        position = 4,
        keyName = "showObjectOverlays",
        name = "Show object overlays",
        description = "Enables clickbox overlays for the next object to click"
    )
    default boolean showObjectOverlays()
    {
        return true;
    }

    @ConfigItem(
        position = 5,
        keyName = "objectOverlayTextMode",
        name = "Object overlay text mode",
        description = "Select where to display tooltip text for game objects"
    )
    default HighlightOverlayTextSetting objectOverlayTextMode()
    {
        return HighlightOverlayTextSetting.ABOVE;
    }

    @ConfigItem(
        position = 6,
        keyName = "objectOverlayColor",
        name = "Object overlay color",
        description = "Change the color of the object overlay"
    )
    default Color objectOverlayColor()
    {
        return Color.CYAN;
    }

    @ConfigItem(
        position = 7,
        keyName = "showCoalBagOverlay",
        name = "Show coal bag contents",
        description = "Display the amount of coal inside your coal bag"
    )
    default boolean showCoalBagOverlay()
    {
        return true;
    }

    @ConfigItem(
        position = 8,
        keyName = "coalBagOverlayColor",
        name = "Coal bag overlay color",
        description = "Change the color of the coal bag count"
    )
    default Color coalBagOverlayColor()
    {
        return Color.CYAN;
    }

    @ConfigItem(
        position = 9,
        keyName = "showStepOverlay",
        name = "Show step overlay",
        description = "Show a generic overlay of the next step"
    )
    default boolean showStepOverlay()
    {
        return true;
    }

    @ConfigItem(
        position = 10,
        keyName = "requireStaminaThreshold",
        name = "Require stamina threshold",
        description = "Require a stamina dose when run energy is lower than this amount. Feature is disabled when set to 0."
    )
    default int requireStaminaThreshold()
    {
        return 50;
    }
}
