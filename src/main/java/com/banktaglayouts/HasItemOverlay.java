package com.banktaglayouts;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Point;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.banktags.tabs.TabInterface;
import net.runelite.client.plugins.banktags.tabs.TagTab;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class HasItemOverlay extends WidgetItemOverlay {
    private static final Dimension IMAGE_SIZE = new Dimension(11, 11);

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private TabInterface tabInterface;

    @Inject
    private BankTagLayoutsPlugin bankTagLayoutsPlugin;

    HasItemOverlay()
    {
		showOnBank();
    }

    private Tooltip tooltip = null;

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        System.out.println("render");
//        if (tabInterface.getActiveTab() == null)
//        {
//            return;
//        }

//        if (!bankTagLayoutsPlugin.debugOverlay) return;

        System.out.println(tabInterface + " " + bankTagLayoutsPlugin.tabInterface);
        TagTab activeTab = tabInterface.getActiveTab();
        if (activeTab == null) {
            return;
        }
        System.out.println(bankTagLayoutsPlugin.fakeItems.size());

        Map<Integer, Integer> itemIdToIndexes = bankTagLayoutsPlugin.getBankOrder(activeTab.getTag());
        Point mouseCanvasPosition = client.getMouseCanvasPosition();
        tooltipManager.getTooltips().remove(tooltip);
        tooltip = null;
        Point canvasLocation = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getCanvasLocation();
        int scrollY = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getScrollY();
        for (BankTagLayoutsPlugin.FakeItem fakeItem : bankTagLayoutsPlugin.fakeItems) {
            int fakeItemId = fakeItem.itemId;

            int row = (mouseCanvasPosition.getY() + scrollY + 2) / 36;
            int col = (mouseCanvasPosition.getX() - 51 + 6) / 48;
            int index = row * 8 + col;
            Integer integer = itemIdToIndexes.entrySet().stream().map(e -> e.getValue()).filter(e -> e == index).findAny().orElse(null);
            if (fakeItem.contains(mouseCanvasPosition)) {
                if (tooltip == null) {
                    tooltip = new Tooltip("" + fakeItemId);
                    tooltipManager.add(tooltip);
                }

                ItemComposition itemComposition = itemManager.getItemComposition(fakeItemId);
                boolean placeholder = itemComposition.getPlaceholderTemplateId() == 14401;
                int nonPlaceholderId = bankTagLayoutsPlugin.getNonPlaceholderId(fakeItemId);
                boolean hasVariants = ItemVariationMapping.getVariations(ItemVariationMapping.map(fakeItemId)).size() > 1;
                int variantBase = ItemVariationMapping.map(nonPlaceholderId);
                String itemShortName = bankTagLayoutsPlugin.itemName(fakeItemId);
                if (itemShortName.contains("(") && itemShortName.length() > 8) {
                    itemShortName = itemShortName.substring(0, Math.min(4, itemShortName.length())) + itemShortName.substring(itemShortName.length() - 4);
                } else {
                    itemShortName = itemShortName.substring(0, Math.min(8, itemShortName.length()));
                }

            }

            int x = fakeItem.originalX + canvasLocation.getX();
            int y = fakeItem.originalY + canvasLocation.getY() - scrollY;
            AsyncBufferedImage image = bankTagLayoutsPlugin.itemManager.getImage(fakeItemId);
            graphics.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
            final BufferedImage outline = itemManager.getItemOutline(fakeItemId, 1, Color.GRAY);
            graphics.drawImage(outline, x, y, null);
        }
    }

}
