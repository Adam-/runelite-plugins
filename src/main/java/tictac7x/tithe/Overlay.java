package tictac7x.tithe;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

import java.awt.*;
import java.util.Map;

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
        renderShape(graphics, object.getClickbox(), color, stroke_width, fill_alpha);
    }

    protected void renderTile(final Graphics2D graphics, final GameObject object, final Color color) {
        renderTile(graphics, object, color, clickbox_stroke_width);
    }

    protected void renderTile(final Graphics2D graphics, final GameObject object, final Color color, final int stroke_width) {
        renderTile(graphics, object, color, stroke_width, clickbox_fill_alpha);
    }

    protected void renderTile(final Graphics2D graphics, final GameObject object, final Color color, final int stroke_width, final int fill_alpha) {
        renderShape(graphics, object.getCanvasTilePoly(), color, stroke_width, fill_alpha);
    }

    private void renderShape(final Graphics2D graphics, final Shape shape, final Color color, final int stroke_width, final int fill_alpha) {
        try {
            // Area border.
            graphics.setColor(color);
            graphics.draw(shape);
            graphics.setStroke(new BasicStroke(stroke_width));

            // Area fill.
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fill_alpha));
            graphics.fill(shape);
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

    protected void highlightInventoryItem(final Client client, final ItemManager items, final Graphics2D graphics, final int item_id) {
        highlightInventoryItem(client, items, graphics, item_id, getColor(color_green, 80));
    }

    protected void highlightInventoryItem(final Client client, final ItemManager items, final Graphics2D graphics, final int item_id, final Color color) {
        try {
            final Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

            for (final WidgetItem item : inventory.getWidgetItems()) {
                if (item.getId() == item_id) {
                    final Rectangle bounds = item.getCanvasBounds(false);
                    graphics.setColor(color);
                    graphics.fill(bounds);
                    graphics.drawImage(items.getImage(item_id, item.getQuantity(), true), (int) bounds.getX(), (int) bounds.getY(), null);
                }
            }
        } catch (Exception exception) {}
    }

    protected void highlightInventoryItems(final Client client, final ItemManager items, final Graphics2D graphics, Map<Integer, Color> items_to_highlight) {
        try {
            final Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

            for (final WidgetItem item : inventory.getWidgetItems()) {
                final int id = item.getId();

                if (items_to_highlight.containsKey(id)) {
                    final Rectangle bounds = item.getCanvasBounds();
                    graphics.setColor(items_to_highlight.get(id));
                    graphics.fill(bounds);
                    graphics.draw(bounds);
                }
            }

        } catch (Exception exception) {}
    }
}
