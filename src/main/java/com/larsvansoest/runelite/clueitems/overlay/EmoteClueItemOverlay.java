package com.larsvansoest.runelite.clueitems.overlay;

import com.larsvansoest.runelite.clueitems.data.EmoteClueItemsProvider;
import com.larsvansoest.runelite.clueitems.overlay.icons.EmoteClueIconProvider;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.ImageComponent;

import javax.inject.Inject;
import java.util.HashSet;

/**
 * Extends {@link WidgetItemOverlay}. Scans and marks items required for emote clue scroll steps.
 */
public class EmoteClueItemOverlay extends WidgetItemOverlay
{
	private final EmoteClueItemsProvider items;
	private final EmoteClueIconProvider icons;
	private final ItemManager itemManager;

	private final Point point; // Single allocation, to be re-used every iteration.

	@Inject
	public EmoteClueItemOverlay(ItemManager itemManager, EmoteClueItemsProvider items, EmoteClueIconProvider icons)
	{
		this.itemManager = itemManager;
		this.items = items;
		this.icons = icons;

		this.point = new Point();

		super.showOnBank();
		super.showOnEquipment();
		super.showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		final int _id = this.itemManager.canonicalize(itemId);
		final Rectangle bounds = itemWidget.getCanvasBounds();
		final int x = bounds.x + bounds.width - 6;

		int y = bounds.y;

		y = this.renderClueItemDetection(graphics, this.items.getBeginnerItems(), this.icons.getRibbons().getBeginnerRibbon(), _id, x, y);
		y = this.renderClueItemDetection(graphics, this.items.getEasyItems(), this.icons.getRibbons().getEasyRibbon(), _id, x, y);
		y = this.renderClueItemDetection(graphics, this.items.getMediumItems(), this.icons.getRibbons().getMediumRibbon(), _id, x, y);
		y = this.renderClueItemDetection(graphics, this.items.getHardItems(), this.icons.getRibbons().getHardRibbon(), _id, x, y);
		y = this.renderClueItemDetection(graphics, this.items.getEliteItems(), this.icons.getRibbons().getEliteRibbon(), _id, x, y);
		this.renderClueItemDetection(graphics, this.items.getMasterItems(), this.icons.getRibbons().getMasterRibbon(), _id, x, y);
	}

	private int renderClueItemDetection(Graphics2D graphics, HashSet<Integer> items, ImageComponent component, int id, int x, int y)
	{
		if (items.contains(id))
		{
			y += renderRibbon(graphics, component, x, y).getHeight() + 1;
		}

		return y;
	}

	private Rectangle renderRibbon(Graphics2D graphics, ImageComponent ribbon, int x, int y)
	{
		this.point.setLocation(x, y);
		ribbon.setPreferredLocation(this.point);
		ribbon.render(graphics);
		return ribbon.getBounds();
	}
}
