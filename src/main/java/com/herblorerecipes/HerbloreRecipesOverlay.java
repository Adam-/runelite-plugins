package com.herblorerecipes;

import com.herblorerecipes.cache.HerbloreRecipesCacheLoader;
import com.herblorerecipes.model.Potion;
import static com.herblorerecipes.util.Utils.KEY_POTION_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_PRIMARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SECONDARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SEED_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_UNF_IDENTIFIER;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.Keybind;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.lang3.StringUtils;

public class HerbloreRecipesOverlay extends Overlay
{
	private static final int INVENTORY_ITEM_WIDGETID = WidgetInfo.INVENTORY.getPackedId();
	private static final int BANK_ITEM_WIDGETID = WidgetInfo.BANK_ITEM_CONTAINER.getPackedId();
	private static final int BANKED_INVENTORY_ITEM_WIDGETID = WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getPackedId();
	private static final Color GREY = new Color(238, 238, 238);
	private static final Color LIME = new Color(0, 255, 0);
	private static final Color AQUA = new Color(0, 255, 255);
	private static final Color GOLD = new Color(255, 215, 0);
	private static final Color PINK = new Color(236, 128, 255);
	private static final Color ORANGE = new Color(255, 85, 85);
	private static final String TOOLTIP_PRIMARY_TEXT = ColorUtil.wrapWithColorTag("Primary", LIME) + ColorUtil.wrapWithColorTag(" for:", GREY);
	private static final String TOOLTIP_SECONDARY_TEXT = ColorUtil.wrapWithColorTag("Secondary", AQUA) + ColorUtil.wrapWithColorTag(" for:", GREY);
	private static final String TOOLTIP_UNF_TEXT = ColorUtil.wrapWithColorTag("Unfinished", GOLD) + ColorUtil.wrapWithColorTag(" for:", GREY);
	private static final String TOOLTIP_POTION_TEXT = ColorUtil.wrapWithColorTag("Requirements", PINK) + ColorUtil.wrapWithColorTag(" for %s:", GREY);
	private static final String TOOLTIP_SEED_TEXT = ColorUtil.wrapWithColorTag("Seed", ORANGE) + ColorUtil.wrapWithColorTag(" for:", GREY);


	private final Client client;
	private final TooltipManager tooltipManager;
	private final ItemManager itemManager;
	private final HerbloreRecipesConfig config;
	private final HerbloreRecipesPlugin plugin;
	private final HerbloreRecipesCacheLoader tooltipTextCache;

	private final StringBuilder stringBuilder = new StringBuilder();

	@Inject
	HerbloreRecipesOverlay(Client client, TooltipManager tooltipManager, ItemManager itemManager, HerbloreRecipesConfig config, HerbloreRecipesPlugin plugin)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.tooltipManager = tooltipManager;
		this.itemManager = itemManager;
		this.config = config;
		this.plugin = plugin;
		this.tooltipTextCache = new HerbloreRecipesCacheLoader(config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.isMenuOpen())
		{
			return null;
		}

		if (!config.showTooltipOnPrimaries() && !config.showTooltipOnSecondaries() &&
			!config.showTooltipOnPotions() && !config.showTooltipOnUnfinished() &&
			!config.showTooltipOnSeeds())
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
						if (config.showOverlayInInv())
						{
							conditionalRender(menuEntry, widgetId);
						}
						break;
					case WidgetID.BANK_GROUP_ID:
						if (config.showOverlayInBank())
						{
							conditionalRender(menuEntry, widgetId);
						}
						break;
				}
				break;
		}
		return null;
	}

	private void conditionalRender(MenuEntry menuEntry, int widgetId) {
		if (config.useModifierKey())
		{
			if ((config.modifierKey().getKeyCode() != KeyEvent.VK_UNDEFINED || config.modifierKey().getModifiers() != 0) && plugin.isModifierKeyPressed())
			{
				renderHerbloreOverlay(menuEntry, widgetId);
			}
		}
		else if (!config.useModifierKey())
		{
			renderHerbloreOverlay(menuEntry, widgetId);
		}
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

	private String stripExtra(String item)
	{
		String stripped = item.replaceAll("Grimy ", "")
			.replaceAll("\\s?\\(\\d\\)", "");
		return stripped.substring(0, 1).toUpperCase() + stripped.substring(1);
	}

	private void getTooltip(String tooltipPrefix, String key)
	{
		stringBuilder.append(tooltipPrefix);
		try
		{
			stringBuilder.append(tooltipTextCache.get(key));
		}
		catch (ExecutionException e)
		{
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	private void renderHerbloreOverlay(MenuEntry menuEntry, int widgetId) {
		Optional<ItemContainer> container = getContainer(widgetId);
		if (container.isPresent())
		{
			Optional<Item> item = getContainerItem(container.get(), menuEntry.getParam0());
			if (item.isPresent())
			{
				String itemName = stripExtra(itemManager.getItemComposition(item.get().getId()).getName());

				if (config.showTooltipOnPrimaries() && Potion.getPrimaries().contains(itemName))
				{
					getTooltip(TOOLTIP_PRIMARY_TEXT, KEY_PRIMARY_IDENTIFIER + itemName);
				}
				if (config.showTooltipOnSecondaries() && Potion.getSecondariesSet().stream().anyMatch(s -> s.contains(itemName)))
				{
					getTooltip(TOOLTIP_SECONDARY_TEXT, KEY_SECONDARY_IDENTIFIER + itemName);
				}
				if (config.showTooltipOnUnfinished() && Potion.getUnfinishedPotions().contains(itemName))
				{
					getTooltip(TOOLTIP_UNF_TEXT, KEY_UNF_IDENTIFIER + itemName);
				}
				if (config.showTooltipOnPotions() && Potion.getPotionNames().contains(itemName))
				{
					getTooltip(String.format(TOOLTIP_POTION_TEXT, ColorUtil.wrapWithColorTag(itemName, AQUA)), KEY_POTION_IDENTIFIER + itemName);
				}
				if (config.showTooltipOnSeeds() && Potion.getSeeds().contains(itemName))
				{
					getTooltip(TOOLTIP_SEED_TEXT, KEY_SEED_IDENTIFIER + itemName);
				}
			}
			if (stringBuilder.length() > 0)
			{
				addTooltip();
			}
		}
	}
}
