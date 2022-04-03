package com.toofifty.easyblastfurnace;

import com.toofifty.easyblastfurnace.config.HighlightOverlayTextSetting;
import com.toofifty.easyblastfurnace.config.ItemOverlaySetting;
import net.runelite.client.config.*;

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
        position = 1,
        keyName = "showBarsTodo",
        name = "Show bars todo",
        description = "Show amount of bars that can be made from ores in the bank.",
        section = statisticsOverlay
    )
    default boolean showBarsTodo()
    {
        return true;
    }

    @ConfigItem(
        position = 2,
        keyName = "showBarsMade",
        name = "Show bars made",
        description = "Show amount of bars made in the session.",
        section = statisticsOverlay
    )
    default boolean showBarsMade()
    {
        return true;
    }

    @ConfigItem(
        position = 3,
        keyName = "showXpBanked",
        name = "Show XP banked",
        description = "Show amount of Smithing XP that can be gained from ores in the bank.",
        section = statisticsOverlay
    )
    default boolean showXpBanked()
    {
        return true;
    }

    @ConfigItem(
        position = 4,
        keyName = "showXpGained",
        name = "Show XP gained",
        description = "Show amount of Smithing XP gained in the session.",
        section = statisticsOverlay
    )
    default boolean showXpGained()
    {
        return true;
    }

    @ConfigItem(
        position = 5,
        keyName = "showStaminaDoses",
        name = "Show stamina doses",
        description = "Show amount of stamina potion doses consumed in the session.",
        section = statisticsOverlay
    )
    default boolean showStaminaDoses()
    {
        return true;
    }

    @ConfigSection(
        name = "Cleanup",
        description = "Configure auto-clearing plugin state",
        position = 3,
        closedByDefault = true
    )
    String cleanup = "cleanup";

    @ConfigItem(
        position = 0,
        keyName = "clearMethodOnLogout",
        name = "Clear method on logout",
        description = "Clear the current method on logout. If disabled, the method can still be cleared by (shift) right clicking the overlay.",
        section = cleanup
    )
    default boolean clearMethodOnLogout()
    {
        return true;
    }

    @ConfigItem(
        position = 1,
        keyName = "clearMethodOnExit",
        name = "Clear method on BF exit",
        description = "Clear the current method when exiting the Blast Furnace. If disabled, the method can still be cleared by (shift) right clicking the overlay.",
        section = cleanup
    )
    default boolean clearMethodOnExit()
    {
        return true;
    }

    @ConfigItem(
        position = 2,
        keyName = "clearStatisticsOnLogout",
        name = "Clear stats on logout",
        description = "Clear the statistics on logout. If disabled, the statistics can still be cleared by (shift) right clicking the overlay.",
        section = cleanup
    )
    default boolean clearStatisticsOnLogout()
    {
        return true;
    }

    @ConfigItem(
        position = 3,
        keyName = "clearStatisticsOnExit",
        name = "Clear stats on BF exit",
        description = "Clear the statistics when exiting the Blast Furnace. If disabled, the statistics can still be cleared by (shift) right clicking the overlay.",
        section = cleanup
    )
    default boolean clearStatisticsOnExit()
    {
        return true;
    }

    @ConfigItem(
        position = 4,
        keyName = "requireStaminaThreshold",
        name = "Low energy threshold",
        description = "Require a stamina dose when run energy is lower than this amount. Feature is disabled when set to 0."
    )
    @Units(Units.PERCENT)
    default int requireStaminaThreshold()
    {
        return 50;
    }
}
