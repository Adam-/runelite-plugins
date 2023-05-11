/*
 * Copyright (c) 2019, Slay to Stay <https://github.com/slaytostay>
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
package com.regionlocker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = RegionLockerPlugin.PLUGIN_NAME,
		description = "Settings for the Region Locker plugin.",
		tags = {"region", "locker", "chunk", "map", "square"}
)
public class RegionLockerPlugin extends Plugin
{
	static final String PLUGIN_NAME = "Region Locker";
	static final String CONFIG_KEY = "regionlocker";

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RegionLockerConfig config;

	@Inject
	private RegionLockerOverlay regionLockerOverlay;

	@Inject
	private RegionBorderOverlay regionBorderOverlay;

	@Inject
	private RegionLockerInput inputListener;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ConfigManager configManager;

	@Setter(AccessLevel.PACKAGE)
	private boolean unlockKeyPressed = false;

	@Setter(AccessLevel.PACKAGE)
	private boolean blockKeyPressed = false;

	@Setter(AccessLevel.PACKAGE)
	@Getter
	private int hoveredRegion = -1;

	private RegionLocker regionLocker;

	@Provides
	RegionLockerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RegionLockerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		regionLocker = new RegionLocker(config, configManager);
		overlayManager.add(regionLockerOverlay);
		overlayManager.add(regionBorderOverlay);
		keyManager.registerKeyListener(inputListener);
		setKeys();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(regionLockerOverlay);
		overlayManager.remove(regionBorderOverlay);
		keyManager.unregisterKeyListener(inputListener);
		RegionLocker.renderLockedRegions = false;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(RegionLockerPlugin.CONFIG_KEY))
		{
			return;
		}

		setKeys();
		regionLocker.readConfig();
	}

	@Subscribe
	public void onFocusChanged(FocusChanged focusChanged)
	{
		if (!focusChanged.isFocused())
		{
			unlockKeyPressed = false;
			blockKeyPressed = false;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		Widget map = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
		if (!(unlockKeyPressed || blockKeyPressed) || hoveredRegion == -1 || map == null) return;

		if (unlockKeyPressed) regionLocker.addRegion(hoveredRegion);
		if (blockKeyPressed) regionLocker.blockRegion(hoveredRegion);
	}

	private void setKeys()
	{
		RegionLockerInput.UNLOCK_KEY = config.unlockKey();
		RegionLockerInput.BLOCK_KEY = config.blacklistKey();
	}
}
