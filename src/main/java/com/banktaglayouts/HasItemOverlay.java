package com.banktaglayouts;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Point;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.banktags.tabs.TabInterface;
import net.runelite.client.plugins.banktags.tabs.TagTab;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

public class HasItemOverlay extends WidgetItemOverlay {
    private static final Dimension IMAGE_SIZE = new Dimension(11, 11);

    private final Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private TabInterface tabInterface;

    private final BankTagLayoutsPlugin bankTagLayoutsPlugin;

    @Inject
    HasItemOverlay(BankTagLayoutsPlugin bankTagLayoutsPlugin, Client client)
    {
        this.bankTagLayoutsPlugin = bankTagLayoutsPlugin;
        this.client = client;
		showOnBank();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
//        if (bankTagLayoutsPlugin.tabInterface.getActiveTab() == null)
//        {
////            System.out.println("returnign cause no tab active");
//            return;
//        }
//
//        if (!bankTagLayoutsPlugin.debugOverlay) return;
//
//        int yoffset = itemWidget.getWidget().getCanvasLocation().getY() - itemWidget.getWidget().getOriginalY() + 3;
//        TagTab activeTab = tabInterface.getActiveTab();
//        if (activeTab == null) {
//            System.out.println("active tab was null.");
//            return;
//        }
//        Map<Integer, Integer> itemIdToIndexes = bankTagLayoutsPlugin.getBankOrder(activeTab.getTag());
//        for (Map.Entry<Integer, Integer> integerIntegerEntry : itemIdToIndexes.entrySet()) {
//            int index = integerIntegerEntry.getValue();
//            int x = (index % 8) * 48 + 51 + 37;
//            int y = (index / 8) * 36 + yoffset;
//            int indexItemId = integerIntegerEntry.getKey();
//            ItemComposition itemComposition = itemManager.getItemComposition(indexItemId);
//            boolean placeholder = itemComposition.getPlaceholderTemplateId() == 14401;
//            int nonPlaceholderId = bankTagLayoutsPlugin.getNonPlaceholderId(indexItemId);
//            boolean hasVariants = ItemVariationMapping.getVariations(ItemVariationMapping.map(indexItemId)).size() > 1;
//            int variantBase = ItemVariationMapping.map(nonPlaceholderId);
//            String itemShortName = bankTagLayoutsPlugin.itemName(indexItemId);
//            if (itemShortName.contains("(") && itemShortName.length() > 8) {
//                itemShortName = itemShortName.substring(0, Math.min(4, itemShortName.length())) + itemShortName.substring(itemShortName.length() - 4);
//            } else {
//                itemShortName = itemShortName.substring(0, Math.min(8, itemShortName.length()));
//            }
//            graphics.setColor((placeholder) ? Color.CYAN : Color.GRAY);
//            graphics.drawString(itemShortName, x, y + 36);
//
////                System.out.println("item " + itemShortName + " has variants: " + hasVariants);
////
//            graphics.setColor(Color.GRAY);
//            if (hasVariants) graphics.drawString(String.valueOf(variantBase), x, y + 12);
//            graphics.drawString(String.valueOf(indexItemId), x, y + 24);
//        }
////
////            ItemComposition itemComposition = itemManager.getItemComposition(itemId);
////            boolean placeholder = itemComposition.getPlaceholderTemplateId() == 14401;
////            int nonPlaceholderId = bankTagsPlugin.getNonPlaceholderId(itemId);
////            int variantBase = ItemVariationMapping.map(nonPlaceholderId);
////            String itemShortName = bankTagsPlugin.itemName(itemId);
////            itemShortName = itemShortName.substring(0, Math.min(3, itemShortName.length()));
////            String debugString1 = itemShortName + " " + ((placeholder) ? "ph" : "");
////
////            graphics.drawString(String.valueOf(variantBase), location.getX(), location.getY() + 12);
////            graphics.drawString(String.valueOf((variantBase != itemId) ? itemId : ""), location.getX(), location.getY() + 24);
////            graphics.drawString(debugString1, location.getX(), location.getY() + 36);
    }

    private int getCountInInventoryOrEquipped(int itemId) {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) return 0;
        ItemComposition itemComposition = itemManager.getItemComposition(itemId);
        if (itemComposition.getPlaceholderTemplateId() == 14401) {
            itemId = itemComposition.getPlaceholderId();
        }
        int count = inventory.count(itemId);
//        System.out.println("count 1: " + count);
//        ItemContainer equipped = client.getItemContainer(InventoryID.EQUIPMENT);
//        count += equipped.count(itemId);
//        System.out.println("item id " + itemComposition.getName() + " has count " + count);
        return count;
    }
}
