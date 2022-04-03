package com.toofifty.easyblastfurnace.overlays;

import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.config.HighlightOverlayTextSetting;
import com.toofifty.easyblastfurnace.config.ItemOverlaySetting;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.steps.WidgetStep;
import com.toofifty.easyblastfurnace.utils.MethodHandler;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class WidgetStepOverlay extends Overlay
{
    @Inject
    private Client client;

    @Inject
    private EasyBlastFurnaceConfig config;

    @Inject
    private MethodHandler methodHandler;

    WidgetStepOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(OverlayPriority.HIGHEST);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.itemOverlayMode() == ItemOverlaySetting.NONE) return null;

        MethodStep step = methodHandler.getStep();

        if (step == null) return null;
        if (!(step instanceof WidgetStep)) return null;

        Widget widget = client.getWidget(((WidgetStep) step).getWidgetInfo());
        if (widget == null) return null;


        Color color = config.itemOverlayColor();
        Rectangle bounds = widget.getBounds();

        graphics.setColor(color);
        graphics.draw(bounds);
        graphics.setColor(new Color(color.getRed(), color.getBlue(), color.getGreen(), 20));
        graphics.fill(bounds);

        if (config.itemOverlayTextMode() == HighlightOverlayTextSetting.NONE) return null;

        TextComponent textComponent = new TextComponent();
        textComponent.setColor(config.itemOverlayColor());
        textComponent.setText(step.getTooltip());

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
                bounds.y - textHeight / 2
            ));
        }

        textComponent.render(graphics);

        return null;
    }
}
