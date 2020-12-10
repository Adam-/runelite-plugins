package com.larsvansoest.runelite.clueitems.overlay;

import com.larsvansoest.runelite.clueitems.overlay.config.ConfigProvider;
import com.larsvansoest.runelite.clueitems.data.ItemsProvider;
import com.larsvansoest.runelite.clueitems.overlay.icons.IconProvider;
import com.larsvansoest.runelite.clueitems.overlay.widgets.Container;
import com.larsvansoest.runelite.clueitems.overlay.widgets.Window;
import com.larsvansoest.runelite.clueitems.overlay.widgets.WindowProvider;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetID.*;
import static net.runelite.api.widgets.WidgetInfo.TO_CHILD;
import static net.runelite.api.widgets.WidgetInfo.TO_GROUP;
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
	private final ItemManager itemManager;
	private final ItemsProvider itemsProvider;
	private final IconProvider iconProvider;
	private final ConfigProvider configProvider;
	private final WindowProvider windowProvider;

	private final Point point; // Single allocation, to be re-used every iteration.

	@Inject
	public EmoteClueItemOverlay(ItemManager itemManager, ConfigProvider config, ItemsProvider itemsProvider, IconProvider icons)
	{
		this.itemManager = itemManager;
		this.configProvider = config;
		this.itemsProvider = itemsProvider;
		this.iconProvider = icons;
		this.windowProvider = new WindowProvider();
		this.point = new Point();

		super.showOnInterfaces(
			// supported
			BANK_GROUP_ID,
			BANK_INVENTORY_GROUP_ID,
			DEPOSIT_BOX_GROUP_ID,
			SHOP_GROUP_ID,
			SHOP_INVENTORY_GROUP_ID,
			EQUIPMENT_INVENTORY_GROUP_ID,
			INVENTORY_GROUP_ID,
			SEED_VAULT_INVENTORY_GROUP_ID,
			EQUIPMENT_GROUP_ID,
			GUIDE_PRICES_INVENTORY_GROUP_ID

			/* not yet supported
			GRAND_EXCHANGE_INVENTORY_GROUP_ID,
			DUEL_INVENTORY_GROUP_ID,
			DUEL_INVENTORY_OTHER_GROUP_ID,
			PLAYER_TRADE_SCREEN_GROUP_ID,
			PLAYER_TRADE_INVENTORY_GROUP_ID,
			KEPT_ON_DEATH_GROUP_ID,
			GUIDE_PRICE_GROUP_ID,
			LOOTING_BAG_GROUP_ID,
			SEED_BOX_GROUP_ID,
			KINGDOM_GROUP_ID */
		);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		Widget widget = itemWidget.getWidget();

		int child = TO_CHILD(widget.getId());
		int group = TO_GROUP(widget.getId());

		Window window = windowProvider.getWindow(group);

		// filter unsupported or turned off interfaces.
		if (window == null)
		{
			return;
		}
		Container container = window.getContainer(child);
		if (container == null || !configProvider.interfaceGroupSelected(container))
		{
			return;
		}

		final int item = this.itemManager.canonicalize(itemId);
		final Rectangle bounds = itemWidget.getCanvasBounds();

		final int x = bounds.x + bounds.width + getXOffset(window, container);

		int y = bounds.y;
		y = this.renderClueItemDetection(graphics, this.itemsProvider.getBeginnerItems(), this.iconProvider.getRibbons().getBeginnerRibbon(), item, x, y);
		y = this.renderClueItemDetection(graphics, this.itemsProvider.getEasyItems(), this.iconProvider.getRibbons().getEasyRibbon(), item, x, y);
		y = this.renderClueItemDetection(graphics, this.itemsProvider.getMediumItems(), this.iconProvider.getRibbons().getMediumRibbon(), item, x, y);
		y = this.renderClueItemDetection(graphics, this.itemsProvider.getHardItems(), this.iconProvider.getRibbons().getHardRibbon(), item, x, y);
		y = this.renderClueItemDetection(graphics, this.itemsProvider.getEliteItems(), this.iconProvider.getRibbons().getEliteRibbon(), item, x, y);
		this.renderClueItemDetection(graphics, this.itemsProvider.getMasterItems(), this.iconProvider.getRibbons().getMasterRibbon(), item, x, y);
	}

	private int getXOffset(Window window, Container container)
	{
		return container == Container.Equipment ? -10 : this.iconProvider.getOffset(window);
	}

	private int renderClueItemDetection(Graphics2D graphics, HashSet<Integer> items, ImageComponent component, int id, int x, int y)
	{
		return items.contains(id) ? (int) (y + renderRibbon(graphics, component, x, y).getHeight()) + 1 : y;
	}

	private Rectangle renderRibbon(Graphics2D graphics, ImageComponent ribbon, int x, int y)
	{
		this.point.setLocation(x, y);
		ribbon.setPreferredLocation(this.point);
		ribbon.render(graphics);
		return ribbon.getBounds();
	}
}
