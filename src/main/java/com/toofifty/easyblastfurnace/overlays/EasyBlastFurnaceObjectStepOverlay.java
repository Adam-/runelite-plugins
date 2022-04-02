package com.toofifty.easyblastfurnace.overlays;


import com.google.inject.Inject;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.steps.ObjectStep;
import com.toofifty.easyblastfurnace.utils.ObjectManager;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;

import java.awt.*;

public class EasyBlastFurnaceObjectStepOverlay extends Overlay
{
    @Inject
    private ObjectManager objectManager;

    @Setter
    private MethodStep step;

    EasyBlastFurnaceObjectStepOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (step == null || !(step instanceof ObjectStep)) return null;

        GameObject object = objectManager.get(((ObjectStep) step).getObjectId());
        Shape clickBox = object.getClickbox();
        if (clickBox != null) {
            graphics.setColor(Color.CYAN);
            graphics.draw(clickBox);
            graphics.setColor(new Color(Color.CYAN.getRed(), Color.CYAN.getBlue(), Color.CYAN.getGreen(), 20));
            graphics.fill(clickBox);
        }

        TextComponent textComponent = new TextComponent();
        Rectangle bounds = object.getClickbox().getBounds();

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(step.getTooltip());
        int textHeight = fontMetrics.getHeight();

        textComponent.setPosition(new Point(
            bounds.x + bounds.width / 2 - textWidth / 2,
            bounds.y - textHeight
        ));
        textComponent.setColor(Color.CYAN);
        textComponent.setText(step.getTooltip());

        textComponent.render(graphics);

        return null;
    }
}
