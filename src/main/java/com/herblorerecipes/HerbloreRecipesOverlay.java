package com.herblorerecipes;

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

public class HerbloreRecipesOverlay extends Overlay
{

    private static final int INVENTORY_ITEM_WIDGETID = WidgetInfo.INVENTORY.getPackedId();
    private static final int BANK_ITEM_WIDGETID = WidgetInfo.BANK_ITEM_CONTAINER.getPackedId();
    private static final int BANKED_INVENTORY_ITEM_WIDGETID = WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getPackedId();
    private static final Color GREY_COLOR = new Color(238, 238, 238);
    private static final Color LIME = new Color(0, 255, 0);
    private static final Color AQUA = new Color(0, 255, 255);
    private static final String TOOLTIP_PRIMARY_TEXT = ColorUtil.wrapWithColorTag("Primary", LIME) + ColorUtil.wrapWithColorTag(" for:", GREY_COLOR);
    private static final String TOOLTIP_SECONDARY_TEXT = ColorUtil.wrapWithColorTag("Secondary", AQUA) + ColorUtil.wrapWithColorTag(" for:", GREY_COLOR);

    private final Client client;
    private final TooltipManager tooltipManager;
    private final ItemManager itemManager;
    private final HerbloreRecipesConfig config;

    private final StringBuilder stringBuilder = new StringBuilder();

    @Inject
    HerbloreRecipesOverlay(Client client, TooltipManager tooltipManager, ItemManager itemManager, HerbloreRecipesConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        this.client = client;
        this.tooltipManager = tooltipManager;
        this.itemManager = itemManager;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.isMenuOpen())
        {
            return null;
        }

        if (!config.showPrimaryIngredients() && !config.showSecondaryIngredients())
        {
            // plugin is effectively disabled
            return null;
        }

        final MenuEntry[] menuEntries = client.getMenuEntries();
        final int last = menuEntries.length - 1;

        if (last < 0)
        {
            return null;
        }

        final MenuEntry menuEntry = menuEntries[last];

        if (StringUtils.isEmpty(menuEntry.getTarget()) ||
                menuEntry.getOption().contains("View") ||
                menuEntry.getParam0() < 0)
        {
            // These are interface buttons, don't render the overlay.
            return null;
        }

        final MenuAction action = MenuAction.of(menuEntry.getType());
        final int widgetId = menuEntry.getParam1();
        final int groupId = WidgetInfo.TO_GROUP(widgetId);

        switch (action)
        {
            case ITEM_USE_ON_WIDGET:
            case CC_OP:
            case ITEM_USE:
            case ITEM_FIRST_OPTION:
            case ITEM_SECOND_OPTION:
            case ITEM_THIRD_OPTION:
            case ITEM_FOURTH_OPTION:
            case ITEM_FIFTH_OPTION:
                switch (groupId)
                {
                    case WidgetID.INVENTORY_GROUP_ID:
                    case WidgetID.BANK_GROUP_ID:
                    case WidgetID.BANK_INVENTORY_GROUP_ID:
                        Optional<ItemContainer> container = getContainer(widgetId);
                        if (container.isPresent())
                        {
                            Optional<Item> item = getContainerItem(container.get(), menuEntry.getParam0());
                            if (item.isPresent())
                            {
                                String itemName = itemManager.getItemComposition(item.get().getId()).getName();

                                if (config.showPrimaryIngredients() && Potion.getPrimaryIngredients().contains(itemName))
                                {
                                    stringBuilder.append(TOOLTIP_PRIMARY_TEXT);
                                    stringBuilder.append(
                                            Potion.getPotionsByPrimaryIngredient(itemName).stream()
                                                    .map(this::makeTooltipText)
                                                    .collect(Collectors.joining("</br>", "</br>", "</br>")));
                                }
                                if (config.showSecondaryIngredients())
                                {
                                    stringBuilder.append(TOOLTIP_SECONDARY_TEXT);
                                }
                            }
                            else
                            {
                                return null;
                            }
                            addTooltip();
                            break;
                        }
                        else
                        {
                            return null;
                        }
                }
                break;
        }
        return null;
    }

    private Optional<ItemContainer> getContainer(int widgetId)
    {
        if (widgetId == INVENTORY_ITEM_WIDGETID || widgetId == BANKED_INVENTORY_ITEM_WIDGETID)
        {
            return Optional.ofNullable(client.getItemContainer(InventoryID.INVENTORY));
        }
        else if (widgetId == BANK_ITEM_WIDGETID)
        {
            return Optional.ofNullable(client.getItemContainer(InventoryID.BANK));
        }
        return Optional.empty();
    }

    private Optional<Item> getContainerItem(ItemContainer container, int itemId)
    {
        return Optional.ofNullable(container.getItem(itemId));
    }

    private void addTooltip()
    {
        tooltipManager.add(new Tooltip(stringBuilder.toString()));
        stringBuilder.setLength(0);
    }

    private String makeTooltipText(Potion potion)
    {
        String text = config.showLevelReqs() ?
                String.format("lvl %d: %s", potion.getLevel(), potion.getPotionName()) :
                potion.getPotionName();
        return ColorUtil.wrapWithColorTag(text, GREY_COLOR);
    }
}
