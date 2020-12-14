package com.larsvansoest.runelite.clueitems.overlay.icons;

import com.larsvansoest.runelite.clueitems.overlay.EmoteClueItemOverlay;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.components.ImageComponent;

import java.io.IOException;
import net.runelite.client.util.ImageUtil;

/**
 * <p>Provides ImageComponent loading utilities for icons used in the {@link EmoteClueItemOverlay} class.</p>
 *
 * @author Lars van Soest
 * @since 1.0
 */
public class IconProvider
{
	public RibbonCollection getRibbons()
	{
		return ribbons;
	}

	private RibbonCollection ribbons;

	/**
	 * Initialise {@link BufferedImage} references and store in internal {@link RibbonCollection} reference.
	 */
	public void fetchBuffers() throws IOException
	{
		this.ribbons = new RibbonCollection(fetchBuffer(IconSources.BEGINNER_RIBBON), fetchBuffer(IconSources.EASY_RIBBON), fetchBuffer(IconSources.MEDIUM_RIBBON), fetchBuffer(IconSources.HARD_RIBBON), fetchBuffer(IconSources.ELITE_RIBBON), fetchBuffer(IconSources.MASTER_RIBBON));
	}

	private ImageComponent fetchBuffer(String iconSource) throws IOException
	{
		return new ImageComponent(ImageUtil.getResourceStreamFromClass(this.getClass(), iconSource));
	}
}
