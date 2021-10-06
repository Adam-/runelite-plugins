package tictac7x.TODO_PLUGIN_NAME;

import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.BasicStroke;

public abstract class Overlay extends net.runelite.client.ui.overlay.Overlay {
    protected final int panel_background_alpha = 80;
    protected final int clickbox_stroke_width = 2;
    protected final int clickbox_fill_alpha = 30;
    protected final int pie_fill_alpha = 90;
    protected final int pie_progress = 1;
    protected final Color color_green = new Color(0, 217, 0);
    protected final Color color_blue = new Color(0, 153, 255);
    protected final Color color_yellow = new Color(255, 187, 0);
    protected final Color color_red = new Color(217, 50, 0);
    protected final Color color_gray = new Color(200, 200, 200);

    protected Color getColor(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    protected void renderClickbox(final Graphics2D graphics, final GameObject object, final Color color) {
        renderClickbox(graphics, object, color, clickbox_stroke_width);
    }

    protected void renderClickbox(final Graphics2D graphics, final GameObject object, final Color color, final int stroke_width) {
        renderClickbox(graphics, object, color, stroke_width, clickbox_fill_alpha);
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

    protected void renderPie(final Graphics2D graphics, final GameObject object, final Color color) {
        renderPie(graphics, object, color, pie_progress);
    }

    protected void renderPie(final Graphics2D graphics, final GameObject object, final Color color, final float progress) {
        renderPie(graphics, object, color, progress, pie_fill_alpha);
    }

    protected void renderPie(final Graphics2D graphics, final GameObject object, final Color color, final float progress, final int fill_alpha) {
        try {
            final ProgressPieComponent progressPieComponent = new ProgressPieComponent();
            progressPieComponent.setPosition(object.getCanvasLocation());
            progressPieComponent.setProgress(progress);
            progressPieComponent.setBorderColor(color);
            progressPieComponent.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), fill_alpha));
            progressPieComponent.render(graphics);
        } catch (Exception exception) {}
    }
}
