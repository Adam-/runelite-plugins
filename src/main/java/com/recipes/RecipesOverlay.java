package com.recipes;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipesOverlay extends Overlay {

    private static final int INVENTORY_ITEM_WIDGETID = WidgetInfo.INVENTORY.getPackedId();
    private static final int BANK_ITEM_WIDGETID = WidgetInfo.BANK_ITEM_CONTAINER.getPackedId();
    private static final int BANKED_INVENTORY_ITEM_WIDGETID = WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getPackedId();

    private final Client client;
    private final TooltipManager tooltipManager;
    private final ItemManager itemManager;
    private final RecipesConfig config;

    private final StringBuilder stringBuilder = new StringBuilder();

    @Inject
    RecipesOverlay(Client client, TooltipManager tooltipManager, ItemManager itemManager, RecipesConfig config) {
        setPosition(OverlayPosition.DYNAMIC);
        this.client = client;
        this.tooltipManager = tooltipManager;
        this.itemManager = itemManager;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (client.isMenuOpen()) {
            return null;
        }

        final MenuEntry[] menuEntries = client.getMenuEntries();
        final int last = menuEntries.length - 1;

        if (last < 0) {
            return null;
        }

        final MenuEntry menuEntry = menuEntries[last];

        if (StringUtils.isEmpty(menuEntry.getTarget()) || menuEntry.getOption().contains("View") || menuEntry.getParam0() < 0) {
            // These are interface buttons, don't want to render the overlay.
            return null;
        }

        final MenuAction action = MenuAction.of(menuEntry.getType());
        final int widgetId = menuEntry.getParam1();
        final int groupId = WidgetInfo.TO_GROUP(widgetId);

        switch (action) {
            case ITEM_USE_ON_WIDGET:
            case CC_OP:
            case ITEM_USE:
            case ITEM_FIRST_OPTION:
            case ITEM_SECOND_OPTION:
            case ITEM_THIRD_OPTION:
            case ITEM_FOURTH_OPTION:
            case ITEM_FIFTH_OPTION:
                switch (groupId) {
                    case WidgetID.INVENTORY_GROUP_ID:
                    case WidgetID.BANK_GROUP_ID:
                    case WidgetID.BANK_INVENTORY_GROUP_ID:
                        ItemContainer container = getContainer(widgetId)
                                .orElseThrow(() -> new RuntimeException("Could not retrieve item container."));
                        Item item = getContainerItem(container, menuEntry.getParam0())
                                .orElseThrow(() -> new RuntimeException("Could not retrieve item with id " + menuEntry.getParam0()));
                        String itemName = itemManager.getItemComposition(item.getId()).getName();

                        if (config.showPrimaryIngredients() && Potion.getPrimaryIngredients().contains(itemName)) {
                            stringBuilder.append("Primary ingredient for: ");
                            stringBuilder.append(
                                    Potion.getPotionsByPrimaryIngredient(itemName).stream()
                                            .map(Potion::getPotionName)
                                            .collect(Collectors.joining(",</br>")));
                        } else {
                            return null;
                        }
                        addTooltip();
                        break;
                }
                break;
        }
        return null;
    }

    private Optional<ItemContainer> getContainer(int widgetId) {
        if (widgetId == INVENTORY_ITEM_WIDGETID || widgetId == BANKED_INVENTORY_ITEM_WIDGETID) {
            return Optional.ofNullable(client.getItemContainer(InventoryID.INVENTORY));
        } else if (widgetId == BANK_ITEM_WIDGETID) {
            return Optional.ofNullable(client.getItemContainer(InventoryID.BANK));
        }
        return Optional.empty();
    }

    private Optional<Item> getContainerItem(ItemContainer container, int itemId) {
        return Optional.ofNullable(container.getItem(itemId));
    }

    private void addTooltip() {
        tooltipManager.add(new Tooltip(ColorUtil.prependColorTag(stringBuilder.toString(), new Color(238, 238, 238))));
        stringBuilder.setLength(0);
    }
}
