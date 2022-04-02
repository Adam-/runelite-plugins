package com.toofifty.easyblastfurnace.overlays;

import com.google.inject.Inject;
import com.toofifty.easyblastfurnace.steps.ItemStep;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import lombok.Setter;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.TextComponent;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EasyBlastFurnaceItemStepOverlay extends WidgetItemOverlay
{
    @Inject
    private ItemManager itemManager;

    @Setter
    private MethodStep step;

    EasyBlastFurnaceItemStepOverlay()
    {
        showOnBank();
        showOnInventory();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        if (step == null) return;
        if (!(step instanceof ItemStep) || ((ItemStep) step).getItemId() != itemId) return;

        BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), Color.CYAN);

        Rectangle bounds = widgetItem.getCanvasBounds();

        ImageComponent imageComponent = new ImageComponent(outline);
        imageComponent.setPreferredLocation(new Point(bounds.x, bounds.y));

        imageComponent.render(graphics);

        TextComponent textComponent = new TextComponent();

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(step.getTooltip());
        int textHeight = fontMetrics.getHeight();

        textComponent.setPosition(new Point(
            bounds.x + bounds.width / 2 - textWidth / 2,
            bounds.y + bounds.height + textHeight
        ));
        textComponent.setColor(Color.CYAN);
        textComponent.setText(step.getTooltip());

        textComponent.render(graphics);
    }
}
