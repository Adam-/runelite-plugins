package com.banktaglayouts;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

@Slf4j
public class FakeItemOverlay extends Overlay {
    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private BankTagLayoutsPlugin plugin;

    @Inject
    private BankTagLayoutsConfig config;

    FakeItemOverlay()
    {
        drawAfterLayer(WidgetInfo.BANK_ITEM_CONTAINER);
        setPosition(OverlayPosition.DYNAMIC);
    }

    private Tooltip tooltip = null;

    @Override
    public Dimension render(Graphics2D graphics)
    {
        BankTagLayoutsPlugin.LayoutableThing currentLayoutableThing = plugin.getCurrentLayoutableThing();
        if (currentLayoutableThing == null) return null;

        Map<Integer, Integer> itemIdToIndexes = plugin.getBankOrder(currentLayoutableThing);
        if (itemIdToIndexes == null) return null;

        if (config.showLayoutPlaceholders() && log.isDebugEnabled()) updateTooltip(itemIdToIndexes);

        Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankItemContainer == null) return null;
        int scrollY = bankItemContainer.getScrollY();
        Point canvasLocation = bankItemContainer.getCanvasLocation();
        Rectangle bankItemArea = new Rectangle(canvasLocation.getX() + 51 - 6, canvasLocation.getY(), bankItemContainer.getWidth() - 51 + 6, bankItemContainer.getHeight());
        if (config.showLayoutPlaceholders()) {
            graphics.clip(bankItemArea);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            for (BankTagLayoutsPlugin.FakeItem fakeItem : plugin.fakeItems) {
                int dragDeltaX = 0;
                int dragDeltaY = 0;
                if (fakeItem.index == plugin.draggedItemIndex && plugin.antiDrag.mayDrag()) {
                    dragDeltaX = client.getMouseCanvasPosition().getX() - plugin.dragStartX;
                    dragDeltaY = client.getMouseCanvasPosition().getY() - plugin.dragStartY;
                }
                int fakeItemId = fakeItem.getItemId();

                int x = plugin.getXForIndex(fakeItem.index) + canvasLocation.getX() + dragDeltaX;
                int y = plugin.getYForIndex(fakeItem.index) + canvasLocation.getY() - scrollY + dragDeltaY;
                if (y + BankTagLayoutsPlugin.BANK_ITEM_HEIGHT > bankItemArea.getMinY() && y < bankItemArea.getMaxY())
                {
                    BufferedImage image = itemManager.getImage(fakeItemId, 1000, false);
                    graphics.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
                    BufferedImage outline = itemManager.getItemOutline(fakeItemId, 1000, Color.GRAY);
                    graphics.drawImage(outline, x, y, null);
                }
            }
        }

        return null;
    }

    private void updateTooltip(Map<Integer, Integer> itemIdToIndexes) {
        tooltipManager.getTooltips().remove(tooltip);
        tooltip = null;

        int index = plugin.getIndexForMousePosition(true);
        if (!log.isDebugEnabled() && !plugin.fakeItems.stream().filter(fakeItem -> fakeItem.index == index).findAny().isPresent()) return;

        if (index != -1) {
            Map.Entry<Integer, Integer> entry = itemIdToIndexes.entrySet().stream()
                    .filter(e -> e.getValue() == index)
                    .findAny().orElse(null);

            if (entry != null) {
                int itemIdForTooltip = entry.getKey();

                if (tooltip == null) {
                    String tooltipString = ColorUtil.wrapWithColorTag(plugin.itemName(itemIdForTooltip), plugin.itemTooltipColor);
                    if (log.isDebugEnabled())
                        tooltipString += " (" + itemIdForTooltip + (plugin.isPlaceholder(itemIdForTooltip) ? ", ph" : "") + ")";
                    tooltip = new Tooltip(tooltipString);
                    tooltipManager.add(tooltip);
                }
            }
        }
    }
}
