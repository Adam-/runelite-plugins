/*
 * Copyright (c) 2020, Truth Forger <http://github.com/Blackberry0Pie>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package bbp.equipmentscreenshot;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.util.ImageUploadStyle;

@ConfigGroup("equipmentscreenshot")
public interface EquipmentScreenshotConfig extends Config
{
	@ConfigSection(
		name = "Equipment Stats",
		description = "Options for equipment stats",
		position = 99,
		closedByDefault = true
	)
	String equipmentStats = "equipmentStats";

	@ConfigItem(
		position = 0,
		keyName = "uploadScreenshot",
		name = "Upload equipment screenshot",
		description = "Uploads the equipment screenshot to Imgur or the clipboard"
	)
	default ImageUploadStyle uploadScreenshot()
	{
		return ImageUploadStyle.CLIPBOARD;
	}

	@ConfigItem(
		position = 1,
		keyName = "notifyWhenTaken",
		name = "Notify When Taken",
		description = "Configures whether or not you are notified when a screenshot has been taken"
	)
	default boolean notifyWhenTaken()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "useResourcePack",
		name = "Use resource pack",
		description = "Uses the currently enabled resource pack instead of default sprites, if one is enabled"
	)
	default boolean useResourcePack()
{
	return false;
}

	@ConfigItem(
		position = 4,
		keyName = "columnBetween",
		name = "Column between",
		description = "Adds a column between the inventory and equipment"
	)
	default boolean columnBetween()
	{
		return false;
	}

	@ConfigItem(
		position = 89,
		keyName = "showStats",
		name = "Show equipment stats",
		description = "Shows equipment stats such as slash, prayer, strength, weight, ..."
	)
	default boolean showStats()
	{
		return true;
	}

	@ConfigItem(
		section = equipmentStats,
		position = 3,
		keyName = "showStyleIcons",
		name = "Show Atk/Def style icons",
		description = "Shows style icons for attack and defence such as slash"
	)
	default boolean showStyleIcons()
	{
		return true;
	}

	@ConfigItem(
		section = equipmentStats,
		position = 97,
		keyName = "writeSpellbook",
		name = "Write spellbook name",
		description = "Writes currently active spellbook name below the spellbook icon"
	)
	default boolean writeSpellbook()
	{
		return false;
	}

	@ConfigItem(
		section = equipmentStats,
		position = 99,
		keyName = "showPreciseWeight",
		name = "Show accurate weight",
		description = "Shows the precise player to 3 decimal places"
	)
	default boolean showPreciseWeight()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "textColor",
		name = "Text color",
		description = "The color of the text used"
	)
	default Color textColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		position = 5,
		keyName = "runepouchOverlay",
		name = "Runepouch overlay",
		description = "Adds an overlay of the rune pouch contents"
)
	default boolean runepouchOverlay()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "blowpipeOverlay",
		name = "Blowpipe overlay",
		description = "Adds an overlay of the blowpipe contents. Requires player to check blowpipe contents to work."
	)
	default boolean blowpipeOverlay()
	{
		return true;
	}

	@ConfigItem(
		position = 88,
		keyName = "showEmptyEquipment",
		name = "Show empty equipment",
		description = "Shows equipment even if nothing is worn"
)
	default boolean showEmptyEquipment()
	{
		return false;
	}

	@ConfigItem(
		section = equipmentStats,
		position = -2,
		keyName = "showAllOptions",
		name = "Use all equipment stat options",
		description = "Enable all of the following options in the equipment stats section"
	)
	default boolean showAllOptions()
	{
		return false;
	}

	@ConfigItem(
		section = equipmentStats,
		position = 98,
		keyName = "showPrayerAndWeight",
		name = "Show Prayer and Weight",
		description = "Shows prayer and weight"
	)
	default boolean showPrayerAndWeight()
	{
		return false;
	}

	@ConfigItem(
		section = equipmentStats,
		position = 96,
		keyName = "showAllStr",
		name = "Show all 3 strength stats",
		description = "Shows melee, ranged, and magic strength irrespective of gear"
	)
	default boolean showAllStr()
	{
		return false;
	}

	@ConfigItem(
		section = equipmentStats,
		position = 95,
		keyName = "overlapStats",
		name = "Overlap Atk/Def stats on icons",
		description = "Overlaps stats over the icons"
)
	default boolean overlapStats()
	{
		return true;
	}

	@ConfigItem(
		position = 87,
		keyName = "button",
		name = "Show button",
		description = "Shows a button in the equipment interface to take a screenshot"
	)
	default boolean button()
	{
		return true;
	}
}
