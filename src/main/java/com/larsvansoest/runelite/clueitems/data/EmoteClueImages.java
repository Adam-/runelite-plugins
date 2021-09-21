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

package com.larsvansoest.runelite.clueitems.data;

import com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

/**
 * Provides static objects for each image used by {@link EmoteClueItemsPlugin}.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public abstract class EmoteClueImages
{
	private static final String folder = "/icons";

	private static BufferedImage bufferedImage(final String folder, final String name)
	{
		return ImageUtil.loadImageResource(EmoteClueItemsPlugin.class, path(folder, name));
	}

	private static String path(final String current, final String next)
	{
		return String.format("%s/%s", current, next);
	}

	public static BufferedImage getRibbon(final EmoteClueDifficulty emoteClueDifficulty)
	{
		switch (emoteClueDifficulty)
		{
			case Beginner:
				return EmoteClueImages.Ribbon.BEGINNER;
			case Easy:
				return EmoteClueImages.Ribbon.EASY;
			case Medium:
				return EmoteClueImages.Ribbon.MEDIUM;
			case Hard:
				return EmoteClueImages.Ribbon.HARD;
			case Elite:
				return EmoteClueImages.Ribbon.ELITE;
			case Master:
				return EmoteClueImages.Ribbon.MASTER;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static BufferedImage getScroll(final EmoteClueDifficulty emoteClueDifficulty)
	{
		switch (emoteClueDifficulty)
		{
			case Beginner:
				return EmoteClueImages.Toolbar.Requirement.Scroll.BEGINNER;
			case Easy:
				return EmoteClueImages.Toolbar.Requirement.Scroll.EASY;
			case Medium:
				return EmoteClueImages.Toolbar.Requirement.Scroll.MEDIUM;
			case Hard:
				return EmoteClueImages.Toolbar.Requirement.Scroll.HARD;
			case Elite:
				return EmoteClueImages.Toolbar.Requirement.Scroll.ELITE;
			case Master:
				return EmoteClueImages.Toolbar.Requirement.Scroll.MASTER;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static BufferedImage illuminate(final BufferedImage bufferedImage, final float scale)
	{
		return ImageUtil.luminanceScale(bufferedImage, scale);
	}

	public static BufferedImage resizeCanvas(final BufferedImage bufferedImage, final int width, final int height)
	{
		return ImageUtil.resizeCanvas(bufferedImage, width, height);
	}

	public static final class Ribbon
	{
		private static final String folder = path(EmoteClueImages.folder, "ribbon");
		public static final BufferedImage ALL = bufferedImage(Ribbon.folder, "all.png");
		public static final BufferedImage BEGINNER = bufferedImage(Ribbon.folder, "beginner.png");
		public static final BufferedImage EASY = bufferedImage(Ribbon.folder, "easy.png");
		public static final BufferedImage MEDIUM = bufferedImage(Ribbon.folder, "medium.png");
		public static final BufferedImage HARD = bufferedImage(Ribbon.folder, "hard.png");
		public static final BufferedImage ELITE = bufferedImage(Ribbon.folder, "elite.png");
		public static final BufferedImage MASTER = bufferedImage(Ribbon.folder, "master.png");

		private Ribbon()
		{
		}
	}

	public static final class Toolbar
	{
		private static final String folder = path(EmoteClueImages.folder, "toolbar");

		public static final class CheckSquare
		{
			private static final String folder = path(Toolbar.folder, "check-square");
			public static final BufferedImage UNKNOWN = bufferedImage(CheckSquare.folder, "unknown.png");
			public static final BufferedImage COMPLETE = bufferedImage(CheckSquare.folder, "complete.png");
			public static final BufferedImage INCOMPLETE = bufferedImage(CheckSquare.folder, "incomplete.png");
			public static final BufferedImage INCOMPLETE_EMPTY = bufferedImage(CheckSquare.folder, "incomplete-empty.png");
			public static final BufferedImage IN_PROGRESS = bufferedImage(CheckSquare.folder, "in-progress.png");
			public static final BufferedImage WAITING = bufferedImage(CheckSquare.folder, "waiting.png");
			public static final BufferedImage UNBUILT = bufferedImage(CheckSquare.folder, "unbuilt.png");

			private CheckSquare()
			{
			}
		}

		public static final class Chevron
		{
			private static final String folder = path(Toolbar.folder, "chevron");
			public static final BufferedImage DOWN = bufferedImage(Chevron.folder, "down.png");
			public static final BufferedImage LEFT = bufferedImage(Chevron.folder, "left.png");

			private Chevron()
			{
			}
		}

		public static final class Footer
		{
			private static final String folder = path(Toolbar.folder, "footer");
			public static final BufferedImage GITHUB = bufferedImage(Footer.folder, "github.png");

			private Footer()
			{
			}
		}

		public static final class SortType
		{
			private static final String folder = path(Toolbar.folder, "sort-type");
			public static final BufferedImage NAME_ASCENDING = bufferedImage(SortType.folder, "name-ascending.png");
			public static final BufferedImage NAME_DESCENDING = bufferedImage(SortType.folder, "name-descending.png");
			public static final BufferedImage QUANTITY_ASCENDING = bufferedImage(SortType.folder, "quantity-ascending.png");
			public static final BufferedImage QUANTITY_DESCENDING = bufferedImage(SortType.folder, "quantity-descending.png");

			private SortType()
			{
			}
		}

		public static final class StashUnit
		{
			private static final String folder = path(Toolbar.folder, "stash-unit");
			public static final BufferedImage BUSH = bufferedImage(StashUnit.folder, "bush.png");
			public static final BufferedImage BUSH_BUILT = bufferedImage(StashUnit.folder, "bush-built.png");
			public static final BufferedImage CRATE = bufferedImage(StashUnit.folder, "crate.png");
			public static final BufferedImage CRATE_BUILT = bufferedImage(StashUnit.folder, "crate-built.png");
			public static final BufferedImage HOLE = bufferedImage(StashUnit.folder, "hole.png");
			public static final BufferedImage HOLE_BUILT = bufferedImage(StashUnit.folder, "hole-built.png");
			public static final BufferedImage ROCK = bufferedImage(StashUnit.folder, "rock.png");
			public static final BufferedImage ROCK_BUILT = bufferedImage(StashUnit.folder, "rock-built.png");

			private StashUnit()
			{
			}
		}

		public static final class Disclaimer
		{
			private static final String folder = path(Toolbar.folder, "disclaimer");
			public static final BufferedImage QUESTION_CIRCLE = bufferedImage(Disclaimer.folder, "question-circle.png");
			public static final BufferedImage CLOSE = bufferedImage(Disclaimer.folder, "close.png");

			private Disclaimer()
			{
			}
		}

		public static final class Requirement
		{
			private static final String folder = path(Toolbar.folder, "requirement");
			public static final BufferedImage INVENTORY = bufferedImage(Requirement.folder, "inventory.png");

			private Requirement()
			{
			}

			public static final class Scroll
			{
				private static final String folder = path(Requirement.folder, "scroll");
				public static final BufferedImage BEGINNER = bufferedImage(Scroll.folder, "beginner.png");
				public static final BufferedImage EASY = bufferedImage(Scroll.folder, "easy.png");
				public static final BufferedImage MEDIUM = bufferedImage(Scroll.folder, "medium.png");
				public static final BufferedImage HARD = bufferedImage(Scroll.folder, "hard.png");
				public static final BufferedImage ELITE = bufferedImage(Scroll.folder, "elite.png");
				public static final BufferedImage MASTER = bufferedImage(Scroll.folder, "master.png");

				private Scroll()
				{
				}
			}
		}
	}
}
