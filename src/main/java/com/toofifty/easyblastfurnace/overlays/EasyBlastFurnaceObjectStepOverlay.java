package com.toofifty.easyblastfurnace.overlays;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.config.HighlightOverlayTextSetting;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.steps.ObjectStep;
import com.toofifty.easyblastfurnace.utils.MethodHandler;
import com.toofifty.easyblastfurnace.utils.ObjectManager;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;

import java.awt.*;

@Singleton
public class EasyBlastFurnaceObjectStepOverlay extends Overlay
{
    @Inject
    private ObjectManager objectManager;

    @Inject
    private EasyBlastFurnaceConfig config;

    @Inject
    private MethodHandler methodHandler;

    EasyBlastFurnaceObjectStepOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showObjectOverlays()) return null;

        MethodStep step = methodHandler.getStep();

        if (step == null) return null;
        if (!(step instanceof ObjectStep)) return null;

        Color color = config.objectOverlayColor();

        GameObject object = objectManager.get(((ObjectStep) step).getObjectId());
        Shape clickBox = object.getClickbox();

        if (clickBox == null) return null;

        graphics.setColor(color);
        graphics.draw(clickBox);
        graphics.setColor(new Color(color.getRed(), color.getBlue(), color.getGreen(), 20));
        graphics.fill(clickBox);

        if (config.objectOverlayTextMode() == HighlightOverlayTextSetting.NONE) return null;

        TextComponent textComponent = new TextComponent();
        Rectangle bounds = object.getClickbox().getBounds();

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(step.getTooltip());
        int textHeight = fontMetrics.getHeight();

        if (config.objectOverlayTextMode() == HighlightOverlayTextSetting.ABOVE) {
            textComponent.setPosition(new Point(
                bounds.x + bounds.width / 2 - textWidth / 2,
                bounds.y - textHeight
            ));
        } else {
            textComponent.setPosition(new Point(
                bounds.x + bounds.width / 2 - textWidth / 2,
                bounds.y + bounds.height
            ));
        }

        textComponent.setColor(color);
        textComponent.setText(step.getTooltip());

        textComponent.render(graphics);

        return null;
    }
}
