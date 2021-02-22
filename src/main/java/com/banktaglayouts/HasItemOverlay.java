package com.banktaglayouts;

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
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
        if (tabInterface.getActiveTab() == null)
        {
            return;
        }

        if (!bankTagLayoutsPlugin.debugOverlay) return;

        int yoffset = itemWidget.getWidget().getCanvasLocation().getY() - itemWidget.getWidget().getOriginalY() + 3;
        TagTab activeTab = tabInterface.getActiveTab();
        if (activeTab == null) {
            System.out.println("active tab was null.");
            return;
        }
        Map<Integer, Integer> itemIdToIndexes = bankTagLayoutsPlugin.getBankOrder(activeTab.getTag());
        for (Map.Entry<Integer, Integer> integerIntegerEntry : itemIdToIndexes.entrySet()) {
            int index = integerIntegerEntry.getValue();
            int x = (index % 8) * 48 + 51 + 37;
            int y = (index / 8) * 36 + yoffset;
            int indexItemId = integerIntegerEntry.getKey();
            ItemComposition itemComposition = itemManager.getItemComposition(indexItemId);
            boolean placeholder = itemComposition.getPlaceholderTemplateId() == 14401;
            int nonPlaceholderId = bankTagLayoutsPlugin.getNonPlaceholderId(indexItemId);
            boolean hasVariants = ItemVariationMapping.getVariations(ItemVariationMapping.map(indexItemId)).size() > 1;
            int variantBase = ItemVariationMapping.map(nonPlaceholderId);
            String itemShortName = bankTagLayoutsPlugin.itemName(indexItemId);
            if (itemShortName.contains("(") && itemShortName.length() > 8) {
                itemShortName = itemShortName.substring(0, Math.min(4, itemShortName.length())) + itemShortName.substring(itemShortName.length() - 4);
            } else {
                itemShortName = itemShortName.substring(0, Math.min(8, itemShortName.length()));
            }
            graphics.setColor((placeholder) ? Color.CYAN : Color.GRAY);
            graphics.drawString(itemShortName, x, y + 36);

            graphics.setColor(Color.GRAY);
            if (hasVariants) graphics.drawString(String.valueOf(variantBase), x, y + 12);
            graphics.drawString(String.valueOf(indexItemId), x, y + 24);
        }
    }

}
