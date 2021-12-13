/*
 * Copyright (c) 2020, Truth Forger <http://github.com/Blackberry0Pie>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package bbp.equipmentscreenshot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Getter;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.SpriteID;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

@PluginDescriptor(
	name = "Equipment Screenshot",
	description = "Takes screenshots of inventory and equipt items",
	tags = {"items", "inventory", "equipment", "screenshot"},
	enabledByDefault = false
)
@Slf4j
public class EquipmentScreenshotPlugin extends Plugin
{
	private static final int INV_ROW_SIZE = 4;
	private static final int ITEM_H_SIZE = Constants.ITEM_SPRITE_WIDTH + 6 /*horizontal padding*/;
	private static final int ITEM_V_SIZE = Constants.ITEM_SPRITE_HEIGHT + 4 /*vertical padding*/;
	private static final int SPELLBOOK_VARBIT = 4070;
	private static final int COLUMN_WIDTH = 20; //actually 28 but we're cropping the side off
	private static final Map<EquipmentInventorySlot, Point> EQUIPMENT_ICON_LOCATIONS = new ImmutableMap.Builder<EquipmentInventorySlot, Point>().
			put(EquipmentInventorySlot.HEAD, new Point(77, 4)).
			put(EquipmentInventorySlot.CAPE, new Point(36, 43)).
			put(EquipmentInventorySlot.AMULET, new Point(77, 43)).
			put(EquipmentInventorySlot.WEAPON, new Point(21, 82)).
			put(EquipmentInventorySlot.BODY, new Point(77, 82)).
			put(EquipmentInventorySlot.SHIELD, new Point(133, 82)).
			put(EquipmentInventorySlot.LEGS, new Point(77, 122)).
			put(EquipmentInventorySlot.GLOVES, new Point(21, 162)).
			put(EquipmentInventorySlot.BOOTS, new Point(77, 162)).
			put(EquipmentInventorySlot.RING, new Point(133, 162)).
			put(EquipmentInventorySlot.AMMO, new Point(118, 43)).
			build();

	@Getter
	private int dartID;
	@Getter
	private int dartCount;
	@Getter
	private int scaleCount;
	private static final Pattern BLOWPIPE_REGEX = Pattern.compile("Darts: (.*)\\. Scales: ([0-9,]+) .*");
	private static final Pattern DARTS_REGEX = Pattern.compile("(.*) x ([0-9,]+)");
	private static final Map<String, Integer> DART_IDS = new ImmutableMap.Builder<String, Integer>().
			put("Bronze dart", ItemID.BRONZE_DART).
			put("Iron dart", ItemID.IRON_DART).
			put("Steel dart", ItemID.STEEL_DART).
			put("Black dart", ItemID.BLACK_DART).
			put("Mithril dart", ItemID.MITHRIL_DART).
			put("Adamant dart", ItemID.ADAMANT_DART).
			put("Rune dart", ItemID.RUNE_DART).
			put("Amethyst dart", ItemID.AMETHYST_DART).
			put("Dragon dart", ItemID.DRAGON_DART).
			build();

	private static final List<Integer> SNOWFLAKE_MAGIC_WEAPONS = new ImmutableList.Builder<Integer>().
			add(ItemID.GUTHIX_STAFF).
			add(ItemID.SARADOMIN_STAFF).
			add(ItemID.ZAMORAK_STAFF).
			add(ItemID.VOID_KNIGHT_MACE).
			add(ItemID.VOID_KNIGHT_MACE_L).
			build();

	private static final List<Integer> SNOWFLAKE_RANGED_WEAPONS = new ImmutableList.Builder<Integer>().
			add(ItemID.CRYSTAL_BOW).
			add(ItemID.BOW_OF_FAERDHINEN).
			add(ItemID.CRAWS_BOW).
			add(ItemID.CRAWS_BOW_U).
			add(ItemID.TOXIC_BLOWPIPE).
			add(ItemID.TOXIC_BLOWPIPE_EMPTY).
			build();

	private static final List<Integer> SALAMANDERS = new ImmutableList.Builder<Integer>().
			add(ItemID.SWAMP_LIZARD).
			add(ItemID.ORANGE_SALAMANDER).
			add(ItemID.RED_SALAMANDER).
			add(ItemID.BLACK_SALAMANDER).
			build();

	private static final List<Integer> THROWN_WEAPONS = new ImmutableList.Builder<Integer>().
			// Darts
			add(ItemID.BRONZE_DART).
			add(ItemID.IRON_DART).
			add(ItemID.STEEL_DART).
			add(ItemID.BLACK_DART).
			add(ItemID.MITHRIL_DART).
			add(ItemID.ADAMANT_DART).
			add(ItemID.RUNE_DART).
			add(ItemID.AMETHYST_DART).
			add(ItemID.DRAGON_DART).
			add(ItemID.BRONZE_DARTP).
			add(ItemID.IRON_DARTP).
			add(ItemID.STEEL_DARTP).
			add(ItemID.BLACK_DARTP).
			add(ItemID.MITHRIL_DARTP).
			add(ItemID.ADAMANT_DARTP).
			add(ItemID.RUNE_DARTP).
			add(ItemID.AMETHYST_DARTP).
			add(ItemID.DRAGON_DARTP).
			add(ItemID.BRONZE_DARTP_5628).
			add(ItemID.IRON_DARTP_5636).
			add(ItemID.STEEL_DARTP_5630).
			add(ItemID.BLACK_DARTP_5631).
			add(ItemID.MITHRIL_DARTP_5632).
			add(ItemID.ADAMANT_DARTP_5633).
			add(ItemID.RUNE_DARTP_5634).
			add(ItemID.AMETHYST_DARTP_25855).
			add(ItemID.DRAGON_DARTP_11233).
			add(ItemID.BRONZE_DARTP_5635).
			add(ItemID.IRON_DARTP_5636).
			add(ItemID.STEEL_DARTP_5637).
			add(ItemID.BLACK_DARTP_5638).
			add(ItemID.MITHRIL_DARTP_5639).
			add(ItemID.ADAMANT_DARTP_5640).
			add(ItemID.RUNE_DARTP_5641).
			add(ItemID.AMETHYST_DARTP_25857).
			add(ItemID.DRAGON_DARTP_11234).

			// Knives
			add(ItemID.BRONZE_KNIFE).
			add(ItemID.IRON_KNIFE).
			add(ItemID.STEEL_KNIFE).
			add(ItemID.BLACK_KNIFE).
			add(ItemID.MITHRIL_KNIFE).
			add(ItemID.ADAMANT_KNIFE).
			add(ItemID.RUNE_KNIFE).
			add(ItemID.DRAGON_KNIFE).
			add(ItemID.BRONZE_KNIFEP).
			add(ItemID.IRON_KNIFEP).
			add(ItemID.STEEL_KNIFEP).
			add(ItemID.BLACK_KNIFEP).
			add(ItemID.MITHRIL_KNIFEP).
			add(ItemID.ADAMANT_KNIFEP).
			add(ItemID.RUNE_KNIFEP).
			add(ItemID.DRAGON_KNIFEP).
			add(ItemID.BRONZE_KNIFEP_5654).
			add(ItemID.IRON_KNIFEP_5655).
			add(ItemID.STEEL_KNIFEP_5656).
			add(ItemID.BLACK_KNIFEP_5658).
			add(ItemID.MITHRIL_KNIFEP_5657).
			add(ItemID.ADAMANT_KNIFEP_5659).
			add(ItemID.RUNE_KNIFEP_5660).
			add(ItemID.DRAGON_KNIFEP_22808).
			add(ItemID.BRONZE_KNIFEP_5661).
			add(ItemID.IRON_KNIFEP_5662).
			add(ItemID.STEEL_KNIFEP_5663).
			add(ItemID.BLACK_KNIFEP_5665).
			add(ItemID.MITHRIL_KNIFEP_5664).
			add(ItemID.ADAMANT_KNIFEP_5666).
			add(ItemID.RUNE_KNIFEP_5667).
			add(ItemID.DRAGON_KNIFEP_22810).

			// Throwing Axes
			add(ItemID.BRONZE_THROWNAXE).
			add(ItemID.IRON_THROWNAXE).
			add(ItemID.STEEL_THROWNAXE).
			add(ItemID.MITHRIL_THROWNAXE).
			add(ItemID.ADAMANT_THROWNAXE).
			add(ItemID.RUNE_THROWNAXE).
			add(ItemID.DRAGON_THROWNAXE).
			add(ItemID.MORRIGANS_THROWING_AXE).

			// Snowflakes
			add(ItemID.CHINCHOMPA_10033).
			add(ItemID.RED_CHINCHOMPA_10034).
			add(ItemID.BLACK_CHINCHOMPA).
			add(ItemID.HOLY_WATER).
			add(ItemID.TOKTZXILUL).
			build();

	private static final Map<Integer, Double> WEIGHT_REDUCING_EQUIPMENT = new ImmutableMap.Builder<Integer, Double>().
			//item ids here are for the *worn* items, not inventory items
			put(ItemID.SPOTTED_CAPE_10073, 2.267).
			put(ItemID.SPOTTIER_CAPE_10074, 4.535).
			put(ItemID.BOOTS_OF_LIGHTNESS_89, 4.535).
			put(ItemID.PENANCE_GLOVES_10554, 4.535).
			//untested exact weights
			put(ItemID.AGILITY_CAPE_13340, 4d).
			put(ItemID.AGILITY_CAPET_13341, 4d).
			put(ItemID.MAX_CAPE_13342, 4d).
			//vanilla graceful
			put(ItemID.GRACEFUL_HOOD_11851, 3d).
			put(ItemID.GRACEFUL_CAPE_11853, 4d).
			put(ItemID.GRACEFUL_TOP_11855, 5d).
			put(ItemID.GRACEFUL_LEGS_11857, 6d).
			put(ItemID.GRACEFUL_GLOVES_11859, 3d).
			put(ItemID.GRACEFUL_BOOTS_11861, 4d).
			//Arceuus graceful
			put(ItemID.GRACEFUL_HOOD_13580, 3d).
			put(ItemID.GRACEFUL_CAPE_13582, 4d).
			put(ItemID.GRACEFUL_TOP_13584, 5d).
			put(ItemID.GRACEFUL_LEGS_13586, 6d).
			put(ItemID.GRACEFUL_GLOVES_13588, 3d).
			put(ItemID.GRACEFUL_BOOTS_13590, 4d).
			//Piscarilius graceful
			put(ItemID.GRACEFUL_HOOD_13592, 3d).
			put(ItemID.GRACEFUL_CAPE_13594, 4d).
			put(ItemID.GRACEFUL_TOP_13596, 5d).
			put(ItemID.GRACEFUL_LEGS_13598, 6d).
			put(ItemID.GRACEFUL_GLOVES_13600, 3d).
			put(ItemID.GRACEFUL_BOOTS_13602, 4d).
			//Lovakengj graceful
			put(ItemID.GRACEFUL_HOOD_13604, 3d).
			put(ItemID.GRACEFUL_CAPE_13606, 4d).
			put(ItemID.GRACEFUL_TOP_13608, 5d).
			put(ItemID.GRACEFUL_LEGS_13610, 6d).
			put(ItemID.GRACEFUL_GLOVES_13612, 3d).
			put(ItemID.GRACEFUL_BOOTS_13614, 4d).
			//Shayzien graceful
			put(ItemID.GRACEFUL_HOOD_13616, 3d).
			put(ItemID.GRACEFUL_CAPE_13618, 4d).
			put(ItemID.GRACEFUL_TOP_13620, 5d).
			put(ItemID.GRACEFUL_LEGS_13622, 6d).
			put(ItemID.GRACEFUL_GLOVES_13624, 3d).
			put(ItemID.GRACEFUL_BOOTS_13626, 4d).
			//Hosidius graceful
			put(ItemID.GRACEFUL_HOOD_13628, 3d).
			put(ItemID.GRACEFUL_CAPE_13630, 4d).
			put(ItemID.GRACEFUL_TOP_13632, 5d).
			put(ItemID.GRACEFUL_LEGS_13634, 6d).
			put(ItemID.GRACEFUL_GLOVES_13636, 3d).
			put(ItemID.GRACEFUL_BOOTS_13638, 4d).
			//Kourend graceful
			put(ItemID.GRACEFUL_HOOD_13668, 3d).
			put(ItemID.GRACEFUL_CAPE_13670, 4d).
			put(ItemID.GRACEFUL_TOP_13672, 5d).
			put(ItemID.GRACEFUL_LEGS_13674, 6d).
			put(ItemID.GRACEFUL_GLOVES_13676, 3d).
			put(ItemID.GRACEFUL_BOOTS_13678, 4d).
			//Brimhaven graceful
			put(ItemID.GRACEFUL_HOOD_21063, 3d).
			put(ItemID.GRACEFUL_CAPE_21066, 4d).
			put(ItemID.GRACEFUL_TOP_21069, 5d).
			put(ItemID.GRACEFUL_LEGS_21072, 6d).
			put(ItemID.GRACEFUL_GLOVES_21075, 3d).
			put(ItemID.GRACEFUL_BOOTS_21078, 4d).
			//Sepulchre graceful
			put(ItemID.GRACEFUL_HOOD_24745, 3d).
			put(ItemID.GRACEFUL_CAPE_24748, 4d).
			put(ItemID.GRACEFUL_TOP_24751, 5d).
			put(ItemID.GRACEFUL_LEGS_24754, 6d).
			put(ItemID.GRACEFUL_GLOVES_24757, 3d).
			put(ItemID.GRACEFUL_BOOTS_24760, 4d).
			//Trailblazer graceful
			put(ItemID.GRACEFUL_HOOD_25071, 3d).
			put(ItemID.GRACEFUL_CAPE_25074, 4d).
			put(ItemID.GRACEFUL_TOP_25077, 5d).
			put(ItemID.GRACEFUL_LEGS_25080, 6d).
			put(ItemID.GRACEFUL_GLOVES_25083, 3d).
			put(ItemID.GRACEFUL_BOOTS_25086, 4d).
			build();

	private static final String MENU_TARGET = "Equipment";
	private static final String TAKE_SCREENSHOT = "Screenshot";
	private static final WidgetMenuOption FIXED_EQUIPMENT_TAB_SCREENSHOT = new WidgetMenuOption(TAKE_SCREENSHOT,
			MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_EQUIPMENT_TAB);
	private static final WidgetMenuOption RESIZABLE_EQUIPMENT_TAB_SCREENSHOT = new WidgetMenuOption(TAKE_SCREENSHOT,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_EQUIPMENT_TAB);
	private static final WidgetMenuOption BOTTOM_LINE_INVENTORY_SCREENSHOT = new WidgetMenuOption(TAKE_SCREENSHOT,
			MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB);

	private Widget button = null;
	private boolean useResourcePack = false;
	private double preciseWeight;
	private int weaponAmagic;
	private int weaponAranged;
	private int weaponSranged;
	private boolean isSalamander;
	private boolean isSnowflakeMagicWeapon;
	private int attackStyleVarbit = -1;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EquipmentScreenshotConfig config;

	@Inject
	private MenuManager menuManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ImageCapture imageCapture;

	@Inject
	private ClientThread clientThread;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private EquipmentRunepouchOverlay equipmentRunepouchOverlay;

	@Inject
	private EquipmentBlowpipeOverlay equipmentBlowpipeOverlay;

	@Inject
	private EquipmentScreenshotUtil util;

	@Provides
	EquipmentScreenshotConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EquipmentScreenshotConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		menuManager.addManagedCustomMenu(FIXED_EQUIPMENT_TAB_SCREENSHOT, null);
		menuManager.addManagedCustomMenu(RESIZABLE_EQUIPMENT_TAB_SCREENSHOT, null);
		menuManager.addManagedCustomMenu(BOTTOM_LINE_INVENTORY_SCREENSHOT, null);
		clientThread.invokeLater(this::createButton);
		useResourcePack = false;
		dartID = 0;
		dartCount = 0;
		scaleCount = 0;
		attackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
	}

	@Override
	protected void shutDown() throws Exception
	{
		menuManager.removeManagedCustomMenu(FIXED_EQUIPMENT_TAB_SCREENSHOT);
		menuManager.removeManagedCustomMenu(RESIZABLE_EQUIPMENT_TAB_SCREENSHOT);
		menuManager.removeManagedCustomMenu(BOTTOM_LINE_INVENTORY_SCREENSHOT);
		clientThread.invoke(this::hideButton);
	}

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked event)
	{
		if (event.getMenuAction() != MenuAction.RUNELITE)
			return;

		if (event.getMenuOption().equals(TAKE_SCREENSHOT))
			screenshotEquipment();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
			return;

		String message = event.getMessage();
		if (!message.startsWith("Darts: "))
			return;

		final Matcher m = BLOWPIPE_REGEX.matcher(Text.removeTags(message));
		if (m.find())
		{
			final Matcher n = DARTS_REGEX.matcher(m.group(1));
			if (n.find())
			{
				dartID = DART_IDS.get(n.group(1));
				dartCount = Integer.parseInt(n.group(2).replaceAll(",", ""));
			}
			else
			{
				dartID = 0;
				dartCount = 0;
			}

			scaleCount = Integer.parseInt(m.group(2).replaceAll(",", ""));
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int currentAttackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
		if (attackStyleVarbit != currentAttackStyleVarbit)
			attackStyleVarbit = currentAttackStyleVarbit;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("equipmentscreenshot") && event.getKey().equals("button"))
		{
			if (config.button())
			{
				clientThread.invoke(this::createButton);
			}
			else
			{
				clientThread.invoke(this::hideButton);
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != WidgetID.EQUIPMENT_GROUP_ID)
		{
			return;
		}

		createButton();
	}

	private BufferedImage paintInventory(BufferedImage bi)
	{
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer == null || util.isEmpty(itemContainer))
			return null;

		final Item[] items = itemContainer.getItems();

		Graphics2D g2d = bi.createGraphics();

		for (int i = 0; i < 28 /*inventory size*/; i++)
		{
			if (i < items.length)
			{
				final Item item = items[i];
				if (item.getQuantity() > 0)
				{
					final int itemId = item.getId();
					if (itemManager.getItemStats(itemId, false) != null &&
							!itemManager.getItemComposition(itemId).isStackable())
					{
						preciseWeight += itemManager.getItemStats(itemId, false).getWeight();
					}

					final BufferedImage image = util.getImage(item);
					if (image != null)
					{
						int x = 16 /*horizontal offset*/ + ITEM_H_SIZE * (i % INV_ROW_SIZE);
						int y = 8 /*vertical offset*/ + ITEM_V_SIZE * (i / INV_ROW_SIZE);
						g2d.drawImage(image, null, x, y);
						if (config.runepouchOverlay() && (itemId == ItemID.RUNE_POUCH || itemId == ItemID.RUNE_POUCH_L))
						{
							equipmentRunepouchOverlay.renderRunepouchOverlay(g2d, new net.runelite.api.Point(x, y));
						}

						if (config.blowpipeOverlay() && itemId == ItemID.TOXIC_BLOWPIPE)
						{
							equipmentBlowpipeOverlay.renderBlowpipeOverlay(g2d, new net.runelite.api.Point(x, y));
						}
						g2d.setFont(FontManager.getRunescapeSmallFont());
					}
				}
			}
		}

		g2d.dispose();
		return bi;
	}

	private BufferedImage paintEquipment(BufferedImage bi)
	{
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (!config.showEmptyEquipment() && (itemContainer == null || util.isEmpty(itemContainer)))
			return null;

		final BufferedImage VERTICAL_RIVETS = util.getCroppedImageFromSpriteID(SpriteID.IRON_RIVETS_VERTICAL, useResourcePack);
		BufferedImage HORIZONTAL_RIVETS = util.getCroppedImageFromSpriteID(SpriteID.IRON_RIVETS_HORIZONTAL, useResourcePack);
		if (useResourcePack && HORIZONTAL_RIVETS.getHeight() > 6) // Crop the resouce pack image if it is doubled
			HORIZONTAL_RIVETS = HORIZONTAL_RIVETS.getSubimage(0, 0, HORIZONTAL_RIVETS.getWidth(), 6);
		final BufferedImage EQUIPMENT_SLOT = util.getCroppedImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_TILE, useResourcePack);
		Graphics2D g2d = bi.createGraphics();

		util.tileImage(g2d, VERTICAL_RIVETS, new Point(92, 39), new Point(92, 163));
		util.tileImage(g2d, VERTICAL_RIVETS, new Point(36, 118), new Point(36, 163));
		util.tileImage(g2d, VERTICAL_RIVETS, new Point(148, 118), new Point(148, 163));

		util.tileImage(g2d, HORIZONTAL_RIVETS, new Point(56, 96), new Point(134, 96));
		util.tileImage(g2d, HORIZONTAL_RIVETS, new Point(71, 57), new Point(119, 57));

		final Map<EquipmentInventorySlot, BufferedImage> EQUIPMENT_ICONS = new ImmutableMap.Builder<EquipmentInventorySlot, BufferedImage>().
				put(EquipmentInventorySlot.HEAD, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_HEAD, false, useResourcePack)).
				put(EquipmentInventorySlot.CAPE, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_CAPE, false, useResourcePack)).
				put(EquipmentInventorySlot.AMULET, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_NECK, false, useResourcePack)).
				put(EquipmentInventorySlot.WEAPON, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_WEAPON, false, useResourcePack)).
				put(EquipmentInventorySlot.BODY, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_TORSO, false, useResourcePack)).
				put(EquipmentInventorySlot.SHIELD, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_SHIELD, false, useResourcePack)).
				put(EquipmentInventorySlot.LEGS, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_LEGS, false, useResourcePack)).
				put(EquipmentInventorySlot.GLOVES, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_HANDS, false, useResourcePack)).
				put(EquipmentInventorySlot.BOOTS, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_FEET, false, useResourcePack)).
				put(EquipmentInventorySlot.RING, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_RING, false, useResourcePack)).
				put(EquipmentInventorySlot.AMMO, util.getImageFromSpriteID(SpriteID.EQUIPMENT_SLOT_AMMUNITION, false, useResourcePack)).
				build();

		int prayer = 0;
		int str = 0;
		int rstr = 0;
		float mdmg = 0;

		int stabA = 0;
		int slashA = 0;
		int crushA = 0;
		int magicA = 0;
		int rangeA = 0;

		int stabD = 0;
		int slashD = 0;
		int crushD = 0;
		int magicD = 0;
		int rangeD = 0;

		Point p;
		for (EquipmentInventorySlot eis : EquipmentInventorySlot.values())
		{
			p = new Point(EQUIPMENT_ICON_LOCATIONS.get(eis).getLocation());
			g2d.drawImage(EQUIPMENT_SLOT, null, p.x, p.y);
			Item item = null;
			if (itemContainer != null)
				item = itemContainer.getItem(eis.getSlotIdx());
			if (item != null && item.getQuantity() > 0)
			{
				final int itemId = item.getId();
				if (config.showAllOptions() || config.showStats())
				{
					final ItemStats is = itemManager.getItemStats(itemId, false);
					if (is == null) {
						log.info("Error finding item stats for the {} slot with item {} using itemID: {}", eis.name().toLowerCase(), itemManager.getItemComposition(itemId).getName(), itemId);
						log.info("This probably means the itemID is new and not yet cached in the Runelite item stats database");
						continue;
					}
					final ItemEquipmentStats ies = is.getEquipment();
					prayer += ies.getPrayer();
					str += ies.getStr();
					if (!eis.equals(EquipmentInventorySlot.AMMO) ||
							itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()) == null ||
							(itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()) != null &&
							!THROWN_WEAPONS.contains(itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()).getId()) &&
							!SNOWFLAKE_RANGED_WEAPONS.contains(itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()).getId()) ))
					{
						rstr += ies.getRstr();
					}
					mdmg += ies.getMdmg();

					stabA += ies.getAstab();
					slashA += ies.getAslash();
					crushA += ies.getAcrush();
					magicA += ies.getAmagic();
					rangeA += ies.getArange();

					stabD += ies.getDstab();
					slashD += ies.getDslash();
					crushD += ies.getDcrush();
					magicD += ies.getDmagic();
					rangeD += ies.getDrange();

					if (eis.equals(EquipmentInventorySlot.WEAPON))
					{
						weaponAmagic = ies.getAmagic();
						weaponAranged = ies.getArange();
						weaponSranged = ies.getRstr();
						isSalamander = SALAMANDERS.contains(itemId);
						isSnowflakeMagicWeapon = SNOWFLAKE_MAGIC_WEAPONS.contains(itemId);
					}

					if (itemManager.getItemStats(itemId, false) != null &&
							!itemManager.getItemComposition(itemId).isStackable())
					{
						if (WEIGHT_REDUCING_EQUIPMENT.containsKey(itemId))
						{
							preciseWeight -= WEIGHT_REDUCING_EQUIPMENT.get(itemId);
						}
						else {
							preciseWeight += itemManager.getItemStats(itemId, false).getWeight();
						}
					}
				}

				final BufferedImage image = util.getImage(item);
				if (image != null)
				{
					g2d.drawImage(image, null, p.x + util.EQUIPMENT_PADDING, p.y + util.EQUIPMENT_PADDING);

					if (config.blowpipeOverlay() && itemId == ItemID.TOXIC_BLOWPIPE)
					{
						equipmentBlowpipeOverlay.renderBlowpipeOverlay(g2d, new net.runelite.api.Point(p.x, p.y));
					}
				}
			}
			else
			{
				util.drawEquipmentIcon(g2d, eis, p, EQUIPMENT_ICONS, useResourcePack);
			}
		}

		if (config.showAllOptions() || config.showStats())
		{
			// Add elite void mage set magic damage boost
			if (itemContainer != null)
			{
				Item head = itemContainer.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
				Item body = itemContainer.getItem(EquipmentInventorySlot.BODY.getSlotIdx());
				Item legs = itemContainer.getItem(EquipmentInventorySlot.LEGS.getSlotIdx());
				Item gloves = itemContainer.getItem(EquipmentInventorySlot.GLOVES.getSlotIdx());
				if (head != null && body != null && legs != null && gloves != null &&
						(head.getId() == ItemID.VOID_MAGE_HELM || head.getId() == ItemID.VOID_MAGE_HELM_L) &&
						(body.getId() == ItemID.ELITE_VOID_TOP || body.getId() == ItemID.ELITE_VOID_TOP_L) &&
						(legs.getId() == ItemID.ELITE_VOID_ROBE || legs.getId() == ItemID.ELITE_VOID_ROBE_L) &&
						(gloves.getId() == ItemID.VOID_KNIGHT_GLOVES || gloves.getId() == ItemID.VOID_KNIGHT_GLOVES_L))
				{
					mdmg += 2.5;
				}
			}

			// Add blowpipe ammo stats
			if (itemContainer != null)
			{
				Item weapon = itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
				if (weapon != null && (weapon.getId() == ItemID.TOXIC_BLOWPIPE ||
						weapon.getId() == ItemID.TOXIC_BLOWPIPE_EMPTY) && dartID != 0)
				{
					final ItemStats is = itemManager.getItemStats(dartID, false);
					if(is != null)
					{
						final ItemEquipmentStats ies = is.getEquipment();
						rangeA += ies.getArange();
						rstr += ies.getRstr();
					}
				}
			}

			g2d.setFont(FontManager.getRunescapeSmallFont());
			g2d.setColor(config.textColor());

			final BufferedImage MACIC_ICON = util.getCroppedImageFromSpriteID(SpriteID.SKILL_MAGIC, useResourcePack);
			final BufferedImage RANGED_ICON = util.getCroppedImageFromSpriteID(SpriteID.SKILL_RANGED, useResourcePack);
			final BufferedImage STR_ICON = util.getCroppedImageFromSpriteID(SpriteID.SKILL_STRENGTH, useResourcePack);
			BufferedImage MDMG_ICON = new BufferedImage(28, 29, STR_ICON.getType());
			Graphics2D g = MDMG_ICON.createGraphics();
			g.drawImage(STR_ICON, null, 12, 1);
			g.drawImage(MACIC_ICON, null, 0, 6);
			g.dispose();
			BufferedImage RSTR_ICON = new BufferedImage(23, 23, STR_ICON.getType());
			Graphics2D h = RSTR_ICON.createGraphics();
			h.drawImage(RANGED_ICON, null, 0, 0);
			h.drawImage(STR_ICON, null, 6, 1);
			h.dispose();

			if (config.showAllOptions() || config.showAllStr())
			{
				util.drawIconWithText(g2d, new Point(50, 22), MDMG_ICON, 1,
						mdmg + "%", true);
				util.drawIconWithText(g2d, new Point(170, 62), RSTR_ICON, 2,
						rstr, true);
				util.drawIconWithText(g2d, new Point(19, 62), STR_ICON, 2,
						str, true);
			}
			else
			{
				if (weaponAmagic >= 10 || isSnowflakeMagicWeapon || (isSalamander && attackStyleVarbit == 2))
				{
					util.drawIconWithText(g2d, new Point(50, 22), MDMG_ICON, 1,
							mdmg + "%", true);
				}
				else if ((!isSalamander && (weaponAranged > 0 || weaponSranged > 0)) || (isSalamander && attackStyleVarbit == 1))
				{
					util.drawIconWithText(g2d, new Point(50, 23), RSTR_ICON, 2,
							rstr, true);
				}
				else
				{
					util.drawIconWithText(g2d, new Point(50, 23), STR_ICON, 2,
							str, true);
				}
			}

			//Spellbook
			int spellBookID = SpriteID.TAB_MAGIC;
			String spellbook = "Modern";
			switch (client.getVarbitValue(SPELLBOOK_VARBIT))
			{
				case 1:
					spellBookID = SpriteID.TAB_MAGIC_SPELLBOOK_ANCIENT_MAGICKS;
					spellbook = "Ancient";
					break;
				case 2:
					spellBookID = SpriteID.TAB_MAGIC_SPELLBOOK_LUNAR;
					spellbook = "Lunar";
					break;
				case 3:
					spellBookID = SpriteID.TAB_MAGIC_SPELLBOOK_ARCEUUS;
					spellbook = "Arceuus";
					break;
			}
			if (!config.showAllOptions() && !config.writeSpellbook())
				spellbook = "";
			util.drawIconWithText(g2d, new Point(136, 23), spellBookID, useResourcePack, 2,
					spellbook, false);

			if (config.showAllOptions() || config.showPrayerAndWeight())
			{
				// Weight
				preciseWeight = new BigDecimal(preciseWeight).setScale(3, RoundingMode.HALF_UP).doubleValue();
				String weight = Double.toString(config.showAllOptions() || config.showPreciseWeight() ? preciseWeight : client.getWeight());
				util.drawIconWithText(g2d, new Point(59, 139), SpriteID.EQUIPMENT_WEIGHT, useResourcePack, 2,
						weight, false);

				// Prayer
				util.drawIconWithText(g2d, new Point(130, 139), SpriteID.SKILL_PRAYER, useResourcePack, 2,
						prayer, true);
			}

			// Equipment accuracy and defence
			final BufferedImage STAB_ICON = itemManager.getImage(ItemID.STEEL_DAGGER);
			final BufferedImage SLASH_ICON = itemManager.getImage(ItemID.STEEL_SCIMITAR);
			final BufferedImage CRUSH_ICON = itemManager.getImage(ItemID.STEEL_WARHAMMER);

			p = new Point(21, 228);
			util.drawStatIcon(g2d, p, STAB_ICON, stabA, stabD);
			util.drawStatIcon(g2d, util.nextIconPosition(p), SLASH_ICON, slashA, slashD);
			util.drawStatIcon(g2d, util.nextIconPosition(p), CRUSH_ICON, crushA, crushD);
			util.drawStatIcon(g2d, util.nextIconPosition(p), MACIC_ICON, magicA, magicD);
			util.drawStatIcon(g2d, util.nextIconPosition(p), RANGED_ICON, rangeA, rangeD);
		}

		g2d.dispose();
		return bi;
	}

	private void screenshotEquipment()
	{
		useResourcePack = config.useResourcePack() && isResourcePackActive();
		preciseWeight = 0;

		BufferedImage bi = util.getCroppedImageFromSpriteID(SpriteID.FIXED_MODE_SIDE_PANEL_BACKGROUND, useResourcePack);
		BufferedImage beq = util.copy(bi);
		BufferedImage binv = paintInventory(bi);
		BufferedImage beqpw = paintEquipment(beq);

		// Select image(s)
		BufferedImage frankensteinsMonster = null;
		if (binv != null)
		{
			if (beqpw != null)
			{
				int columnWidth = (config.columnBetween() ? COLUMN_WIDTH : 0);
				frankensteinsMonster = new BufferedImage(2 * binv.getWidth() + columnWidth,
						binv.getHeight(), 1);
				Graphics2D g2d = frankensteinsMonster.createGraphics();
				g2d.drawImage(binv, 0, 0, null);

				// Add column
				if (config.columnBetween())
				{
					final BufferedImage column = util.getCroppedImageFromSpriteID(SpriteID.OLD_SCHOOl_MODE_SIDE_PANEL_EDGE_RIGHT, useResourcePack).
							getSubimage(0, 0, COLUMN_WIDTH, binv.getHeight());
					g2d.drawImage(column, binv.getWidth(), 0, null);
				}

				g2d.drawImage(beqpw, binv.getWidth() + columnWidth, 0, null);
				g2d.dispose();
			}
			else
			{
				frankensteinsMonster = binv;
			}
		}
		else if (beqpw != null)
		{
			frankensteinsMonster = beqpw;
		}

		if (frankensteinsMonster != null)
			imageCapture.takeScreenshot(frankensteinsMonster, "Equipment-", config.notifyWhenTaken(), config.uploadScreenshot());
	}

	private boolean isResourcePackActive()
	{
		String str = configManager.getConfiguration("runelite", "resourcepacksplugin");
		if (str == null || str.equals("false") || str.isEmpty())
			return false;

		str = configManager.getConfiguration("resourcepacks", "resourcePack");
		if (str == null || str.isEmpty())
			return false;
		switch (str)
		{
			case "HUB":
				str = configManager.getConfiguration("resourcepacks", "selectedHubPack");
				if (str != null && !str.isEmpty())
					return true;
				break;
			case "FIRST":
				str = configManager.getConfiguration("resourcepacks", "resourcePackPath");
				if (str != null && !str.isEmpty())
					return true;
				break;
			case "SECOND":
				str = configManager.getConfiguration("resourcepacks", "resourcePack2Path");
				if (str != null && !str.isEmpty())
					return true;
				break;
			case "THIRD":
				str = configManager.getConfiguration("resourcepacks", "resourcePack3Path");
				if (str != null && !str.isEmpty())
					return true;
				break;
		}
		return false;
	}

	private void hideButton()
	{
		if (button == null)
		{
			return;
		}

		button.setHidden(true);
		button = null;
	}

	private void createButton()
	{
		if (!config.button())
		{
			return;
		}

		Widget parent = client.getWidget(WidgetInfo.EQUIPMENT);
		if (parent == null)
		{
			return;
		}

		hideButton();

		button = parent.createChild(-1, WidgetType.GRAPHIC);
		button.setOriginalHeight(20);
		button.setOriginalWidth(20);
		button.setOriginalX(48);
		button.setOriginalY(14);
		button.setSpriteId(573);
		button.setAction(0, "Screenshot");
		button.setOnOpListener((JavaScriptCallback) (e) -> clientThread.invokeLater(this::screenshotEquipment));
		button.setHasListener(true);
		button.revalidate();

		button.setOnMouseOverListener((JavaScriptCallback) (e) -> button.setSpriteId(570));
		button.setOnMouseLeaveListener((JavaScriptCallback) (e) -> button.setSpriteId(573));
	}
}
