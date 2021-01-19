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

package com.larsvansoest.runelite.clueitems.overlay;

import com.larsvansoest.runelite.clueitems.config.EmoteClueItemsConfigProvider;
import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImage;
import com.larsvansoest.runelite.clueitems.data.util.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.overlay.widget.ItemWidget;
import com.larsvansoest.runelite.clueitems.overlay.widget.ItemWidgetContainer;
import com.larsvansoest.runelite.clueitems.overlay.widget.ItemWidgetContext;
import com.larsvansoest.runelite.clueitems.overlay.widget.ItemWidgetData;
import com.larsvansoest.runelite.clueitems.overlay.widget.ItemWidgetInspector;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.ImageComponent;

/**
 * Extends {@link WidgetItemOverlay}. Scans and marks items required for emote clue scroll steps.
 */
public class EmoteClueItemOverlay extends WidgetItemOverlay
{
	static class Component {
		static class Ribbon {
			static ImageComponent BEGINNER = new ImageComponent(EmoteClueImage.Ribbon.BEGINNER);
			static ImageComponent EASY = new ImageComponent(EmoteClueImage.Ribbon.EASY);
			static ImageComponent MEDIUM = new ImageComponent(EmoteClueImage.Ribbon.MEDIUM);
			static ImageComponent HARD = new ImageComponent(EmoteClueImage.Ribbon.HARD);
			static ImageComponent ELITE = new ImageComponent(EmoteClueImage.Ribbon.ELITE);
			static ImageComponent MASTER = new ImageComponent(EmoteClueImage.Ribbon.MASTER);
		}
	}

	private final ItemManager itemManager;
	private final EmoteClueItemsConfigProvider emoteClueItemsConfigProvider;

	// Single object allocations, re-used every sequential iteration.
	private final ItemWidgetData itemWidgetData;
	private final Point point;

	@Inject
	public EmoteClueItemOverlay(ItemManager itemManager, EmoteClueItemsConfigProvider config)
	{
		this.itemManager = itemManager;
		this.emoteClueItemsConfigProvider = config;

		this.itemWidgetData = new ItemWidgetData();
		this.point = new Point();

		super.showOnInterfaces(
			Arrays.stream(ItemWidget.values()).mapToInt(itemWidget -> itemWidget.groupId).toArray()
		);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		ItemWidgetInspector.Inspect(itemWidget, this.itemWidgetData, 3);
		ItemWidgetContainer container = this.itemWidgetData.getContainer();
		ItemWidgetContext context = this.itemWidgetData.getContext();

		// Filter unsupported and turned off interfaces.
		if (context == null || container == null || !this.emoteClueItemsConfigProvider.interfaceGroupSelected(container))
		{
			return;
		}

		final int item = this.itemManager.canonicalize(itemId);

		final Rectangle bounds = itemWidget.getCanvasBounds();
		final int x = bounds.x + bounds.width + this.getXOffset(container, context);
		int y = bounds.y;
		y = this.renderClueItemDetection(graphics, EmoteClueDifficulty.Beginner, Component.Ribbon.BEGINNER, item, x, y);
		y = this.renderClueItemDetection(graphics, EmoteClueDifficulty.Easy, Component.Ribbon.EASY, item, x, y);
		y = this.renderClueItemDetection(graphics, EmoteClueDifficulty.Medium, Component.Ribbon.MEDIUM, item, x, y);
		y = this.renderClueItemDetection(graphics, EmoteClueDifficulty.Hard, Component.Ribbon.HARD, item, x, y);
		y = this.renderClueItemDetection(graphics, EmoteClueDifficulty.Elite, Component.Ribbon.ELITE, item, x, y);
		this.renderClueItemDetection(graphics, EmoteClueDifficulty.Master, Component.Ribbon.MASTER, item, x, y);
	}

	private int getXOffset(ItemWidgetContainer container, ItemWidgetContext context)
	{
		return container == ItemWidgetContainer.Equipment ? -10 : context == ItemWidgetContext.Default ? -1 : -5;
	}

	private int renderClueItemDetection(Graphics2D graphics, EmoteClueDifficulty emoteClueDifficulty, ImageComponent component, int id, int x, int y)
	{
		return Arrays.stream(EmoteClueAssociations.DifficultyToEmoteClues.get(emoteClueDifficulty)).anyMatch(emoteClue -> Arrays.stream(emoteClue.getItemRequirements()).anyMatch(itemRequirement -> itemRequirement.fulfilledBy(id))) ? (int) (y + this.renderRibbon(graphics, component, x, y).getHeight()) + 1 : y;
	}

	private Rectangle renderRibbon(Graphics2D graphics, ImageComponent ribbon, int x, int y)
	{
		this.point.setLocation(x, y);
		ribbon.setPreferredLocation(this.point);
		ribbon.render(graphics);
		return ribbon.getBounds();
	}
}
