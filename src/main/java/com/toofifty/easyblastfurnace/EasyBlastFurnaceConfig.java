package com.toofifty.easyblastfurnace;

import com.toofifty.easyblastfurnace.config.HighlightOverlayTextSetting;
import com.toofifty.easyblastfurnace.config.ItemOverlaySetting;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("easy-blastfurnace")
public interface EasyBlastFurnaceConfig extends Config
{
    @ConfigSection(
        name = "Guidance overlays",
        description = "Configure instruction, item and object overlays",
        position = 0
    )
    String guidanceOverlays = "guidanceOverlays";

    @ConfigItem(
        position = 0,
        keyName = "showStepOverlay",
        name = "Show step overlay",
        description = "Show an instructional overlay of the next step",
        section = guidanceOverlays
    )
    default boolean showStepOverlay()
    {
        return true;
    }

    @ConfigItem(
        position = 1,
        keyName = "itemOverlayMode",
        name = "Item overlay mode",
        description = "Select how highlighted items appear",
        section = guidanceOverlays
    )
    default ItemOverlaySetting itemOverlayMode()
    {
        return ItemOverlaySetting.BOX;
    }

    @ConfigItem(
        position = 2,
        keyName = "itemOverlayTextMode",
        name = "Item overlay tooltip",
        description = "Select where to display tooltip text for items",
        section = guidanceOverlays
    )
    default HighlightOverlayTextSetting itemOverlayTextMode()
    {
        return HighlightOverlayTextSetting.BELOW;
    }

    @ConfigItem(
        position = 3,
        keyName = "itemOverlayColor",
        name = "Item overlay color",
        description = "Change the color of the item overlay",
        section = guidanceOverlays
    )
    default Color itemOverlayColor()
    {
        return Color.CYAN;
    }

    @ConfigItem(
        position = 4,
        keyName = "showObjectOverlays",
        name = "Show object overlays",
        description = "Enables clickbox overlays for the next object to click",
        section = guidanceOverlays
    )
    default boolean showObjectOverlays()
    {
        return true;
    }

    @ConfigItem(
        position = 5,
        keyName = "objectOverlayTextMode",
        name = "Object overlay tooltip",
        description = "Select where to display tooltip text for game objects",
        section = guidanceOverlays
    )
    default HighlightOverlayTextSetting objectOverlayTextMode()
    {
        return HighlightOverlayTextSetting.ABOVE;
    }

    @ConfigItem(
        position = 6,
        keyName = "objectOverlayColor",
        name = "Object overlay color",
        description = "Change the color of the object overlay",
        section = guidanceOverlays
    )
    default Color objectOverlayColor()
    {
        return Color.CYAN;
    }

    @ConfigSection(
        name = "Coal bag overlay",
        description = "Configure coal bag overlay",
        position = 1
    )
    String coalBagOverlay = "coalBagOverlay";

    @ConfigItem(
        position = 0,
        keyName = "showCoalBagOverlay",
        name = "Show coal bag overlay",
        description = "Display the amount of coal inside your coal bag",
        section = coalBagOverlay
    )
    default boolean showCoalBagOverlay()
    {
        return true;
    }

    @ConfigItem(
        position = 1,
        keyName = "coalBagOverlayColor",
        name = "Coal bag overlay color",
        description = "Change the color of the coal bag count",
        section = coalBagOverlay
    )
    default Color coalBagOverlayColor()
    {
        return Color.CYAN;
    }

    @ConfigSection(
        name = "Statistics overlay",
        description = "Configure statistics overlay",
        position = 2
    )
    String statisticsOverlay = "statisticsOverlay";

    @ConfigItem(
        position = 0,
        keyName = "showStatisticsOverlay",
        name = "Show statistics",
        description = "Show an overlay with statistics such as bars todo/done, XP banked & stamina doses used.",
        section = statisticsOverlay
    )
    default boolean showStatisticsOverlay()
    {
        return true;
    }

    @ConfigItem(
        position = 3,
        keyName = "requireStaminaThreshold",
        name = "Require stamina threshold",
        description = "Require a stamina dose when run energy is lower than this amount. Feature is disabled when set to 0."
    )
    default int requireStaminaThreshold()
    {
        return 50;
    }
}
