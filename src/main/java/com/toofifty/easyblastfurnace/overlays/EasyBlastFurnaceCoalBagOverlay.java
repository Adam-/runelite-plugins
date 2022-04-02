package com.toofifty.easyblastfurnace.overlays;

import com.google.inject.Inject;
import com.toofifty.easyblastfurnace.EasyBlastFurnacePlugin;
import com.toofifty.easyblastfurnace.utils.BlastFurnaceState;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

import java.awt.*;

public class EasyBlastFurnaceCoalBagOverlay extends WidgetItemOverlay
{
    private final EasyBlastFurnacePlugin plugin;
    private final BlastFurnaceState state;

    @Inject
    EasyBlastFurnaceCoalBagOverlay(EasyBlastFurnacePlugin plugin, BlastFurnaceState state)
    {
        this.plugin = plugin;
        this.state = state;
        showOnInventory();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        if (!plugin.isEnabled() ||
            !(itemId == ItemID.COAL_BAG ||
                itemId == ItemID.COAL_BAG_12019 ||
                itemId == ItemID.COAL_BAG_25627))
            return;

        Rectangle bounds = widgetItem.getCanvasBounds();
        TextComponent textComponent = new TextComponent();

        textComponent.setPosition(new Point(bounds.x - 1, bounds.y + 8));
        textComponent.setColor(Color.CYAN);
        textComponent.setText(Integer.toString(state.getCoalInCoalBag()));

        textComponent.render(graphics);
    }
}
