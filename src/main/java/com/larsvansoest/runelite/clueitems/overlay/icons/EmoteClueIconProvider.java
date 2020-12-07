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
public class EmoteClueIconProvider
{
	public EmoteClueRibbonCollection getRibbons()
	{
		return ribbons;
	}

	private EmoteClueRibbonCollection ribbons;

	/**
	 * Initialise {@link BufferedImage} references and store in internal {@link EmoteClueRibbonCollection} reference.
	 */
	public void fetchBuffers() throws IOException
	{
		this.ribbons = new EmoteClueRibbonCollection(fetchBuffer(EmoteClueIconSources.BEGINNER_RIBBON), fetchBuffer(EmoteClueIconSources.EASY_RIBBON), fetchBuffer(EmoteClueIconSources.MEDIUM_RIBBON), fetchBuffer(EmoteClueIconSources.HARD_RIBBON), fetchBuffer(EmoteClueIconSources.ELITE_RIBBON), fetchBuffer(EmoteClueIconSources.MASTER_RIBBON));
	}

	private ImageComponent fetchBuffer(String iconSource) throws IOException
	{
		return new ImageComponent(ImageUtil.getResourceStreamFromClass(this.getClass(), iconSource));
	}
}
