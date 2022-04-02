package com.toofifty.easyblastfurnace.overlays;

import com.google.inject.Inject;
import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.config.HighlightOverlayTextSetting;
import com.toofifty.easyblastfurnace.config.ItemOverlaySetting;
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

    @Inject
    private EasyBlastFurnaceConfig config;

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
        if (!(step instanceof ItemStep)) return;
        if (((ItemStep) step).getItemId() != itemId) return;
        if (config.itemOverlayMode() == ItemOverlaySetting.NONE) return;

        Color color = config.itemOverlayColor();

        BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), color);

        Rectangle bounds = widgetItem.getCanvasBounds();

        ImageComponent imageComponent = new ImageComponent(outline);
        imageComponent.setPreferredLocation(new Point(bounds.x, bounds.y));

        imageComponent.render(graphics);

        if (config.itemOverlayTextMode() == HighlightOverlayTextSetting.NONE) return;

        TextComponent textComponent = new TextComponent();

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(step.getTooltip());
        int textHeight = fontMetrics.getHeight();

        if (config.itemOverlayTextMode() == HighlightOverlayTextSetting.BELOW) {
            textComponent.setPosition(new Point(
                bounds.x + bounds.width / 2 - textWidth / 2,
                bounds.y + bounds.height + textHeight
            ));
        } else {
            textComponent.setPosition(new Point(
                bounds.x + bounds.width / 2 - textWidth / 2,
                bounds.y - textHeight
            ));
        }
        
        textComponent.setColor(color);
        textComponent.setText(step.getTooltip());

        textComponent.render(graphics);
    }
}
