/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, Lars van Soest
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.larsvansoest.runelite.clueitems.data.util;

import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImage;
import java.awt.image.BufferedImage;
import net.runelite.client.util.ImageUtil;

/**
 * Static facade which provides utilities to get images from the {@link EmoteClueImage} class.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public abstract class EmoteClueImages
{
	public static BufferedImage getRibbon(EmoteClueDifficulty emoteClueDifficulty)
	{
		switch (emoteClueDifficulty)
		{
			case Beginner:
				return EmoteClueImage.Ribbon.BEGINNER;
			case Easy:
				return EmoteClueImage.Ribbon.EASY;
			case Medium:
				return EmoteClueImage.Ribbon.MEDIUM;
			case Hard:
				return EmoteClueImage.Ribbon.HARD;
			case Elite:
				return EmoteClueImage.Ribbon.ELITE;
			case Master:
				return EmoteClueImage.Ribbon.MASTER;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static BufferedImage getScroll(EmoteClueDifficulty emoteClueDifficulty)
	{
		switch (emoteClueDifficulty)
		{
			case Beginner:
				return EmoteClueImage.Toolbar.Requirement.Scroll.BEGINNER;
			case Easy:
				return EmoteClueImage.Toolbar.Requirement.Scroll.EASY;
			case Medium:
				return EmoteClueImage.Toolbar.Requirement.Scroll.MEDIUM;
			case Hard:
				return EmoteClueImage.Toolbar.Requirement.Scroll.HARD;
			case Elite:
				return EmoteClueImage.Toolbar.Requirement.Scroll.ELITE;
			case Master:
				return EmoteClueImage.Toolbar.Requirement.Scroll.MASTER;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static BufferedImage illuminate(BufferedImage bufferedImage, float scale)
	{
		return ImageUtil.luminanceScale(bufferedImage, scale);
	}

	public static BufferedImage resizeCanvas(BufferedImage bufferedImage, int width, int height)
	{
		return ImageUtil.resizeCanvas(bufferedImage, width, height);
	}
}
