package com.larsvansoest.runelite.clueitems.overlay;

import com.larsvansoest.runelite.clueitems.data.EmoteClueItemsProvider;
import com.larsvansoest.runelite.clueitems.overlay.icon.ClueIconProvider;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.ImageComponent;

import javax.inject.Inject;
import java.awt.*;

public class ClueItemOverlay extends WidgetItemOverlay {

    private EmoteClueItemsProvider items;
    private ClueIconProvider icons;

    @Inject
    public ClueItemOverlay(EmoteClueItemsProvider items, ClueIconProvider icons)
    {
        this.items = items;
        this.icons = icons;
        super.showOnBank();
        super.showOnInventory();
        super.showOnEquipment();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        if(items.getBeginnerItems().contains(itemId) || true)
        {
            Rectangle bounds = itemWidget.getCanvasBounds();
            ImageComponent imageComponent = new ImageComponent(icons.getBeginner());
            imageComponent.setPreferredLocation(new Point(bounds.x - 1, bounds.y + bounds.height - 1));
            imageComponent.render(graphics);
        }
    }
}
