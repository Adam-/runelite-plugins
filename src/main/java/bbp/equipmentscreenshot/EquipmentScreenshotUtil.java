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

import com.google.common.collect.ImmutableMap;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

public class EquipmentScreenshotUtil
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private EquipmentScreenshotConfig config;

	public static final int EQUIPMENT_PADDING = 2;

	public static final Map<EquipmentInventorySlot, Point> EQUIPMENT_ICON_OFFSETS = new ImmutableMap.Builder<EquipmentInventorySlot, Point>().
			put(EquipmentInventorySlot.HEAD, new Point(6, 4)).
			put(EquipmentInventorySlot.CAPE, new Point(5, 1)).
			put(EquipmentInventorySlot.AMULET, new Point(8, 9)).
			put(EquipmentInventorySlot.WEAPON, new Point(4, 4)).
			put(EquipmentInventorySlot.BODY, new Point(2, 9)).
			put(EquipmentInventorySlot.SHIELD, new Point(3, 3)).
			put(EquipmentInventorySlot.LEGS, new Point(8, 2)).
			put(EquipmentInventorySlot.GLOVES, new Point(1, 2)).
			put(EquipmentInventorySlot.BOOTS, new Point(4, 6)).
			put(EquipmentInventorySlot.RING, new Point(8, 8)).
			put(EquipmentInventorySlot.AMMO, new Point(5, 6)).
			build();

	public BufferedImage getImage(Item item)
	{
		ItemComposition itemComposition = itemManager.getItemComposition(item.getId());
		return itemManager.getImage(item.getId(), item.getQuantity(), itemComposition.isStackable());
	}

	public BufferedImage copy(BufferedImage bi)
	{
		return new BufferedImage(
				bi.getColorModel(),
				bi.copyData(null),
				bi.getColorModel().isAlphaPremultiplied(),
				null);
	}

	public BufferedImage getImageFromSpriteID(int spriteID, boolean crop, boolean useResourcePack)
	{
		BufferedImage bi = null;
		if (useResourcePack && client.getSpriteOverrides().get(spriteID) != null)
			bi = copy(client.getSpriteOverrides().get(spriteID).toBufferedImage());
		if (crop)
			bi = trimImage(bi);
		if (bi == null)
		{
			final BufferedImage sprite = spriteManager.getSprite(spriteID, 0);
			if (sprite != null)
				bi = copy(sprite);
		}
		return bi;
	}

	public BufferedImage getCroppedImageFromSpriteID(int spriteID, boolean useResourcePack)
	{
		return getImageFromSpriteID(spriteID, true, useResourcePack);
	}

	public BufferedImage getResourcePackImageFromSpriteID(int spriteID, boolean crop)
	{
		return getImageFromSpriteID(spriteID, crop, true);
	}

	public void drawStringFromInt(Graphics2D g2d, int string, Point p, int offsetX, int offsetY)
	{
		String str = Integer.toString(string);
		if (str.charAt(0) != '-' && str.charAt(0) != '0')
			str = "+" + str;
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		int centerTextOffset = metrics.stringWidth(str) / 2;
		g2d.drawString(str, p.x + offsetX - centerTextOffset, p.y + offsetY);
	}

	public void drawString(Graphics2D g2d, String str, Point p, int offsetX, int offsetY, boolean addPlusSign)
	{
		if (str.endsWith(".0%") || str.endsWith(".0"))
			str = str.replace(".0", "");
		if (addPlusSign && str.charAt(0) != '-' && str.charAt(0) != '0')
			str = "+" + str;
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		int centerTextOffset = metrics.stringWidth(str) / 2;
		g2d.drawString(str, p.x + offsetX - centerTextOffset, p.y + offsetY);
	}

	void drawEquipmentIcon(Graphics2D g2d, EquipmentInventorySlot eis, Point p, Map<EquipmentInventorySlot, BufferedImage> EQUIPMENT_ICONS, boolean useResourcePack)
	{
		Point iconOffset = new Point(0, 0);
		if (!useResourcePack)
			iconOffset = EQUIPMENT_ICON_OFFSETS.get(eis);
		BufferedImage bi = EQUIPMENT_ICONS.get(eis);
		g2d.drawImage(bi, null, p.x + EQUIPMENT_PADDING + iconOffset.x, p.y + EQUIPMENT_PADDING + iconOffset.y);
	}

	void drawEquipmentIcon(Graphics2D g2d, EquipmentInventorySlot eis, Point p, Map<EquipmentInventorySlot, BufferedImage> EQUIPMENT_ICONS)
	{
		drawEquipmentIcon(g2d, eis, p, EQUIPMENT_ICONS, true);
	}

	public boolean isEmpty(ItemContainer ic)
	{
		boolean empty = true;
		for (Item i : ic.getItems())
		{
			if (i.getId() != -1)
			{
				empty = false;
				break;
			}
		}
		return empty;
	}

	public BufferedImage trimImage(BufferedImage image)
	{
		if (image == null)
			return null;
		WritableRaster raster = image.getAlphaRaster();
		if (raster == null)
			return image;
		int width = raster.getWidth();
		int height = raster.getHeight();
		int left = 0;
		int top = 0;
		int right = width - 1;
		int bottom = height - 1;
		int minRight = width - 1;
		int minBottom = height - 1;

		top:
		for (;top < bottom; top++)
		{
			for (int x = 0; x < width; x++)
			{
				if (raster.getSample(x, top, 0) != 0)
				{
					minRight = x;
					minBottom = top;
					break top;
				}
			}
		}

		left:
		for (;left < minRight; left++)
		{
			for (int y = height - 1; y > top; y--)
			{
				if (raster.getSample(left, y, 0) != 0)
				{
					minBottom = y;
					break left;
				}
			}
		}

		bottom:
		for (;bottom > minBottom; bottom--)
		{
			for (int x = width - 1; x >= left; x--)
			{
				if (raster.getSample(x, bottom, 0) != 0)
				{
					minRight = x;
					break bottom;
				}
			}
		}

		right:
		for (;right > minRight; right--)
		{
			for (int y = bottom; y >= top; y--)
			{
				if (raster.getSample(right, y, 0) != 0)
				{
					break right;
				}
			}
		}

		return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
	}

	public void tileImage(Graphics2D g2d, BufferedImage bi, Point start, Point finish)
	{
		int x = Math.abs(start.x - finish.x) / bi.getWidth();
		int y = Math.abs(start.y - finish.y) / bi.getHeight();
		boolean horizontal = x > y;
		int times = (horizontal ? x : y);
		for (; times >= 0 ; times--)
			g2d.drawImage(bi, null, start.x + (horizontal ? times * bi.getWidth() : 0),
					start.y + (!horizontal ? times * bi.getHeight() : 0));
	}

	public void drawIconWithText(Graphics2D g2d, Point d, BufferedImage bi, int spacing, String str, boolean addPlusSign)
	{
		Point p = new Point(d.getLocation());
		if (str.endsWith(".0%") || str.endsWith(".0"))
			str = str.replace(".0", "");
		if (addPlusSign && str.charAt(0) != '-' && str.charAt(0) != '0')
			str = "+" + str;
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		int textHOffset = metrics.stringWidth(str) / 2;
		int imageHOffset = bi.getWidth() / 2;
		int imageVOffset = (bi.getHeight() + (!str.equals("") ? metrics.getHeight() - metrics.getLeading() : 0) + spacing) / 2;
		g2d.drawImage(bi, null, p.x - imageHOffset, p.y - imageVOffset);
		p.translate(- textHOffset, - imageVOffset + bi.getHeight() + (!str.equals("") ? spacing + metrics.getHeight() : 0));
		drawTextWithShadow(g2d, p, str);
	}

	public void drawIconWithText(Graphics2D g2d, Point p, int spriteID, boolean useResourcePack, int spacing, String str, boolean addPlusSign)
	{
		BufferedImage bi = getCroppedImageFromSpriteID(spriteID, useResourcePack);
		drawIconWithText(g2d, p, bi, spacing, str, addPlusSign);
	}

	public void drawIconWithText(Graphics2D g2d, Point p, BufferedImage bi, int spacing, int i, boolean addPlusSign)
	{
		String str = Integer.toString(i);
		drawIconWithText(g2d, p, bi, spacing, str, addPlusSign);
	}

	public void drawIconWithText(Graphics2D g2d, Point p, int spriteID, boolean useResourcePack, int spacing, int i, boolean addPlusSign)
	{
		BufferedImage bi = getCroppedImageFromSpriteID(spriteID, useResourcePack);
		drawIconWithText(g2d, p, bi, spacing, i, addPlusSign);
	}

	public void drawTextWithShadow(Graphics2D g2d, Point p, String str)
	{
		g2d.setColor(Color.black);
		g2d.drawString(str, p.x + 1, p.y + 1);

		g2d.setColor(config.textColor());
		g2d.drawString(str, p.x, p.y);
	}

	public void drawTextWithShadow(Graphics2D g2d, Point d, int hOffset, int vOffset, String str)
	{
		Point p = new Point(d.getLocation());
		p.translate(hOffset, vOffset);
		drawTextWithShadow(g2d, p, str);
	}

	public void drawTextWithShadow(Graphics2D g2d, Point p, int string)
	{
		String str = intToSignedString(string);
		drawTextWithShadow(g2d, p, str);
	}

	void drawStatIcon(Graphics2D g2d, Point p, BufferedImage bi, int a, int d)
	{
		boolean showStyleIcons = config.showAllOptions() || config.showStyleIcons();
		boolean overlapStatIcons = config.showAllOptions() || config.overlapStats();
		int spacing = (overlapStatIcons ? 0 : 10);
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		int imageHOffset = bi.getWidth() / 2;
		int imageVOffset = bi.getHeight() / 2;
		int unleadTextHeight = metrics.getHeight() - metrics.getLeading();
		int textVOffset = unleadTextHeight / 2 + spacing;

		if (showStyleIcons)
			g2d.drawImage(bi, null, p.x - imageHOffset, p.y - imageVOffset);

		String acc = intToSignedString(a);
		int text1HOffset = metrics.stringWidth(acc) / 2;
		drawTextWithShadow(g2d, p, -text1HOffset, -textVOffset, acc);

		String def = intToSignedString(d);
		int text2HOffset = metrics.stringWidth(def) / 2;
		drawTextWithShadow(g2d, p, -text2HOffset, textVOffset + metrics.getHeight(), def);
	}

	private String intToSignedString(int i)
	{
		String str = Integer.toString(i);
		if (str.charAt(0) != '-' && str.charAt(0) != '0')
			str = "+" + str;
		return str;
	}

	Point translate(Point p, int xOffset, int yOffset)
	{
		p.translate(xOffset, yOffset);
		return p;
	}
}
