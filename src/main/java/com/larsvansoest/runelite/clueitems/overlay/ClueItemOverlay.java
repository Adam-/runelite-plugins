package com.larsvansoest.runelite.clueitems.overlay;

import com.larsvansoest.runelite.clueitems.data.EmoteClueItemsProvider;
import com.larsvansoest.runelite.clueitems.overlay.icons.ClueIconProvider;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.ImageComponent;

import javax.inject.Inject;
import java.awt.*;

public class ClueItemOverlay extends WidgetItemOverlay {

    private EmoteClueItemsProvider items;
    private ClueIconProvider icons;
    private ItemManager itemManager;
    private Point _point;

    @Inject
    public ClueItemOverlay(ItemManager itemManager, EmoteClueItemsProvider items, ClueIconProvider icons)
    {
        this.itemManager = itemManager;
        this.items = items;
        this.icons = icons;

        this._point = new Point(0,0);

        super.showOnBank();
        super.showOnInventory();
        super.showOnEquipment();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        int _id = this.itemManager.canonicalize(itemId);

        final Rectangle bounds = itemWidget.getCanvasBounds();
        final int x = bounds.x + bounds.width - 1;
        int y = bounds.y;

        if(items.getBeginnerItems().contains(_id))
        {
            y += renderRibbon(graphics, this.icons.getRibbons().getBeginnerRibbon(), x, y).getHeight() + 1;
        }

        if(items.getEasyItems().contains(_id))
        {
            y += renderRibbon(graphics, this.icons.getRibbons().getEasyRibbon(), x, y).getHeight() + 1;
        }

        if(items.getMediumItems().contains(_id))
        {
            y += renderRibbon(graphics, this.icons.getRibbons().getMediumRibbon(), x, y).getHeight() + 1;
        }

        if(items.getHardItems().contains(_id))
        {
            y += renderRibbon(graphics, this.icons.getRibbons().getHardRibbon(), x, y).getHeight() + 1;
        }

        if(items.getEliteItems().contains(_id))
        {
            y += renderRibbon(graphics, this.icons.getRibbons().getEliteRibbon(), x, y).getHeight() + 1;
        }

        if(items.getMasterItems().contains(_id))
        {
            y += renderRibbon(graphics, this.icons.getRibbons().getMasterRibbon(), x, y).getHeight() + 1;
        }
    }

    private Rectangle renderRibbon(Graphics2D graphics, ImageComponent ribbon, int x, int y)
    {
        this._point.setLocation(x, y);
        ribbon.setPreferredLocation(this._point);
        ribbon.render(graphics);
        return ribbon.getBounds();
    }
}
