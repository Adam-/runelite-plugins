/*
 * Copyright (c) 2021, neilrush
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
package com.playeroutline;

import java.awt.Color;

import net.runelite.client.config.*;

@ConfigGroup("playeroutline")
public interface PlayerOutlineConfig extends Config
{

	@ConfigSection(
			name = "Prayer Color Highlight",
			description = "Changes color of your highlight based on overhead prayer.",
			position = 3,
			closedByDefault = true
	)
	String colorHighlightSection = "colorHighlight";
	@Alpha
	@ConfigItem(
		keyName = "playerOutlineColor",
		name = "Outline Color",
		description = "The color for the players outline",
		position = 0
	)
	default Color playerOutlineColor()
	{
		return new Color(0x3D000000, true);
	}

	@ConfigItem(
		keyName = "borderWidth",
		name = "Border Width",
		description = "Width of the player outline border",
		position = 1
	)
	default int borderWidth()
	{
		return 4;
	}

	@ConfigItem(
		keyName = "outlineFeather",
		name = "Outline Feather",
		description = "Specify between 0-4 how much the player outline should be faded",
		position = 2
	)
	@Range(
		max = 4
	)
	default int outlineFeather()
	{
		return 4;
	}

	@ConfigItem(
			name = "Enable Overhead Changing",
			description = "Changes color based on the overhead being prayed",
			position = 0,
			keyName = "prayerChanging",
			section = colorHighlightSection
	)
	default boolean prayerChanging()
	{
		return false;
	}
	@Alpha
	@ConfigItem(
			keyName = "playerOutlineColorMage",
			name = "Mage Protection Color",
			description = "The color for the players outline when praying mage protection",
			position = 1,
			section = colorHighlightSection
	)
	default Color playerOutlineColorMage()
	{
		return new Color(0,0,255,90);
	}
	@Alpha
	@ConfigItem(
			keyName = "playerOutlineColorRange",
			name = "Range Protection Color",
			description = "The color for the players outline when praying range protection",
			position = 2,
			section = colorHighlightSection
	)
	default Color playerOutlineColorRange()
	{
		return new Color(0,255,0,90);
	}
	@Alpha
	@ConfigItem(
			keyName = "playerOutlineColorMelee",
			name = "Melee Protection Color",
			description = "The color for the players outline when praying melee protection",
			position = 3,
			section = colorHighlightSection
	)
	default Color playerOutlineMelee()
	{
		return new Color(255,0,0,90);
	}
	@Alpha
	@ConfigItem(
			keyName = "playerOutlineColorRedemption",
			name = "Redemption Color",
			description = "The color for the players outline when praying redemption",
			position = 4,
			section = colorHighlightSection
	)
	default Color playerOutlineColorRedemption()
	{
		return new Color(0x3D000000, true);
	}

	@Alpha
	@ConfigItem(
			keyName = "playerOutlineColorSmite",
			name = "Smite Color",
			description = "The color for the players outline when praying smite",
			position = 5,
			section = colorHighlightSection
	)
	default Color playerOutlineColorSmite()
	{
		return new Color(0x3D000000, true);
	}
	@Alpha
	@ConfigItem(
			keyName = "playerOutlineColorRet",
			name = "Retribution Color",
			description = "The color for the players outline when praying retribution",
			position = 6,
			section = colorHighlightSection
	)
	default Color playerOutlineColorRet()
	{
		return new Color(0x3D000000, true);
	}

}
