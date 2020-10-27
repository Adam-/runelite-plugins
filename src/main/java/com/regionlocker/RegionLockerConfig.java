/*
 * Copyright (c) 2019, Slay to Stay <https://github.com/slaytostay>
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
package com.regionlocker;

import java.awt.Color;

import net.runelite.client.config.*;

@ConfigGroup(RegionLockerPlugin.CONFIG_KEY)
public interface RegionLockerConfig extends Config
{
	@ConfigSection(
			name = "Regions",
			description = "Settings relating to chunks that you can unlock",
			position = 0
	)
	String regionSettings = "regionSettings";

	@ConfigSection(
			name = "Environment Looks",
			description = "Settings relating to locked regions look",
			position = 1
	)
	String environmentSettings = "environmentSettings";

	@ConfigSection(
			name = "Map Settings",
			description = "Settings relating to the map overlay",
			position = 2
	)
	String mapSettings = "mapSettings";

	// Region Settings

	@ConfigItem(
			keyName = "trailblazerRegion",
			name = "Unlock area",
			description = "Unlock a complete area on the surface based on Trailblazer Leagues",
			position = 15,
			section = regionSettings
	)
	default TrailblazerRegion trailblazerRegion()
	{
		return TrailblazerRegion.NONE;
	}

	@ConfigItem(
			keyName = "unlockUnderground",
			name = "Unlock underground",
			description = "Unlock all underground chunks",
			position = 16,
			section = regionSettings
	)
	default boolean unlockUnderground()
	{
		return true;
	}

	@ConfigItem(
			keyName = "unlockRealms",
			name = "Unlock realms",
			description = "Unlock all realm chunks like Zanaris and the TzHaar area",
			position = 17,
			section = regionSettings
	)
	default boolean unlockRealms()
	{
		return true;
	}

	@ConfigItem(
			keyName = "unlockedRegions",
			name = "Unlocked chunks",
			description = "List of unlocked regions seperated by a ',' symbol",
			position = 18,
			section = regionSettings
	)
	default String unlockedRegions()
	{
		return "";
	}

	@ConfigItem(
			keyName = "unlockableRegions",
			name = "Unlockable chunks",
			description = "List of unlockable regions seperated by a ',' symbol",
			position = 19,
			section = regionSettings
	)
	default String unlockableRegions()
	{
		return "";
	}

	@ConfigItem(
			keyName = "blacklistedRegions",
			name = "Blacklisted chunks",
			description = "List of blacklisted regions seperated by a ',' symbol",
			position = 20,
			section = regionSettings
	)
	default String blacklistedRegions()
	{
		return "";
	}

	// Environment Looks

	@ConfigItem(
			keyName = "renderLockedRegions",
			name = "Locked chunk shader",
			description = "Adds graphical change to all chunk that are locked",
			position = 21,
			section = environmentSettings
	)
	default boolean renderLockedRegions()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "shaderGrayColor",
			name = "Chunk shader color",
			description = "The color of the locked chunks in the shader",
			position = 22,
			section = environmentSettings
	)
	default Color shaderGrayColor()
	{
		return new Color(0, 31, 77, 204);
	}

	@Alpha
	@ConfigItem(
			keyName = "shaderGrayAmount",
			name = "Chunk shader opacity",
			description = "The amount of gray scale that is applied to a locked chunk in the shader (alpha only)",
			position = 23,
			section = environmentSettings
	)
	default Color shaderGrayAmount()
	{
		return new Color(0, 0, 0, 204);
	}

	@ConfigItem(
			keyName = "hardBorder",
			name = "Hard chunk border",
			description = "True = hard border cutoff, False = chunk border gradient",
			position = 24,
			section = environmentSettings
	)
	default boolean hardBorder()
	{
		return true;
	}

	@ConfigItem(
			keyName = "renderRegionBorders",
			name = "Draw chunk border lines",
			description = "Draw the chunk borders in the environment marked by lines",
			position = 25,
			section = environmentSettings
	)
	default boolean renderRegionBorders()
	{
		return false;
	}

	@ConfigItem(
			keyName = "regionBorderWidth",
			name = "Chunk border width",
			description = "How wide the region border will be",
			position = 26,
			section = environmentSettings
	)
	default int regionBorderWidth()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(
			keyName = "regionBorderColor",
			name = "Chunk border color",
			description = "The color of the chunk borders",
			position = 27,
			section = environmentSettings
	)
	default Color regionBorderColor()
	{
		return new Color(0, 200, 83, 200);
	}

	// Map Settings

	@ConfigItem(
			keyName = "drawMapOverlay",
			name = "Draw chunks on map",
			description = "Draw a color overlay for each locked/unlocked chunk",
			position = 28,
			section = mapSettings
	)
	default boolean drawMapOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "invertMapOverlay",
			name = "Invert map overlay",
			description = "Switches which chunks the map will draw the color overlay for (true = locked, false = unlocked)",
			position = 29,
			section = mapSettings
	)
	default boolean invertMapOverlay()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "mapOverlayColor",
			name = "Map overlay color",
			description = "The color the map overlay will draw the chunks in",
			position = 30,
			section = mapSettings
	)
	default Color mapOverlayColor()
	{
		return new Color(200, 16, 0, 100);
	}

	@Alpha
	@ConfigItem(
			keyName = "unlockableOverlayColor",
			name = "Unlockable overlay color",
			description = "The color the map overlay will draw the unlockable chunks in",
			position = 31,
			section = mapSettings
	)
	default Color unlockableOverlayColor()
	{
		return new Color(60, 200, 160, 100);
	}

	@Alpha
	@ConfigItem(
			keyName = "blacklistedOverlayColor",
			name = "Blacklisted overlay color",
			description = "The color the map overlay will draw the blacklisted chunks in",
			position = 32,
			section = mapSettings
	)
	default Color blacklistedOverlayColor()
	{
		return new Color(0, 0, 0, 200);
	}

	@ConfigItem(
			keyName = "drawMapGrid",
			name = "Draw map grid",
			description = "Draw the grid of chunks on the map",
			position = 33,
			section = mapSettings
	)
	default boolean drawMapGrid()
	{
		return true;
	}

	@ConfigItem(
			keyName = "drawRegionId",
			name = "Draw region IDs",
			description = "Draw the chunk ID for each chunk on the map",
			position = 34,
			section = mapSettings
	)
	default boolean drawRegionId()
	{
		return true;
	}

	@ConfigItem(
			keyName = "unlockKey",
			name = "Unlock hotkey",
			description = "When you hold this key you can click on the map to unlock a chunk",
			position = 35,
			section = mapSettings
	)
	default Keybind unlockKey()
	{
		return Keybind.SHIFT;
	}

	@ConfigItem(
			keyName = "blacklistKey",
			name = "Blacklist hotkey",
			description = "When you hold this key you can click on the map to blacklist a chunk",
			position = 36,
			section = mapSettings
	)
	default Keybind blacklistKey()
	{
		return Keybind.CTRL;
	}
}
