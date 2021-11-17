/*
 * Copyright (c) 2017, Tyler <http://github.com/tylerthardy>
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

import net.runelite.client.game.RunepouchRune;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;

class EquipmentRunepouchOverlay
{
	private static final Varbits[] AMOUNT_VARBITS =
			{
					Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3
			};
	private static final Varbits[] RUNE_VARBITS =
			{
					Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3
			};
	private static final Dimension IMAGE_SIZE = new Dimension(11, 11);

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EquipmentScreenshotConfig config;

	@Inject
	private EquipmentScreenshotUtil util;

	void renderRunepouchOverlay(Graphics2D graphics, Point location)
	{
		boolean showIcons = true;
		if (configManager.getConfiguration("runepouch", "runeicons", Boolean.class) != null)
			showIcons = configManager.getConfiguration("runepouch", "runeicons", Boolean.class);

		graphics.setFont(FontManager.getRunescapeSmallFont());

		for (int i = 0; i < AMOUNT_VARBITS.length; i++)
		{
			Varbits amountVarbit = AMOUNT_VARBITS[i];

			int amount = client.getVar(amountVarbit);
			if (amount <= 0)
				continue;

			Varbits runeVarbit = RUNE_VARBITS[i];
			int runeId = client.getVar(runeVarbit);
			RunepouchRune rune = RunepouchRune.getRune(runeId);
			if (rune == null)
				continue;

			int hOffset = (showIcons ? 11 : 4);
			int vOffset = 12 + (graphics.getFontMetrics().getHeight() - 1) * i;
			util.drawTextWithShadow(graphics, location, hOffset, vOffset, "" + formatNumber(amount));

			if (!showIcons)
				continue;

			BufferedImage image = getRuneImage(rune);
			if (image != null)
			{
				OverlayUtil.renderImageLocation(graphics,
						new Point(location.getX() - 1, location.getY() + graphics.getFontMetrics().getHeight() * i - 1),
						image);
			}
		}
	}

	private BufferedImage getRuneImage(RunepouchRune rune)
	{
		BufferedImage runeImg = rune.getImage();
		if (runeImg != null)
			return runeImg;

		runeImg = itemManager.getImage(rune.getItemId());
		if (runeImg == null)
			return null;

		BufferedImage resizedImg = new BufferedImage(IMAGE_SIZE.width, IMAGE_SIZE.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImg.createGraphics();
		g.drawImage(runeImg, 0, 0, IMAGE_SIZE.width, IMAGE_SIZE.height, null);
		g.dispose();

		rune.setImage(resizedImg);
		return resizedImg;
	}

	private static String formatNumber(int amount)
	{
		return amount < 1000 ? String.valueOf(amount) : amount / 1000 + "K";
	}
}

