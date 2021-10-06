package tictac7x.TODO_PLUGIN_NAME;

import net.runelite.api.GameObject;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;

public abstract class Overlay extends net.runelite.client.ui.overlay.Overlay {
    protected final int stroke_width = 2;
    protected final int fill_alpha = 30;

    protected void renderClickbox(final Graphics2D graphics, final GameObject object, final Color color) {
        renderClickbox(graphics, object, color, stroke_width);
    }

    protected void renderClickbox(final Graphics2D graphics, final GameObject object, final Color color, final int stroke) {
        renderClickbox(graphics, object, color, stroke, fill_alpha);
    }

    protected void renderClickbox(final Graphics2D graphics, final GameObject object, final Color color, final int stroke_width, final int fill_alpha) {
        try {
            final Shape clickbox = object.getClickbox();

            // Area border.
            graphics.setColor(color);
            graphics.draw(clickbox);
            graphics.setStroke(new BasicStroke(stroke_width));

            // Area fill.
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fill_alpha));
            graphics.fill(clickbox);
        } catch (Exception exception) {}
    }
}
