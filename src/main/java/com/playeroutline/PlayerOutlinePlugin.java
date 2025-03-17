/*
 * Copyright (c) 2021, neilrush
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
package com.playeroutline;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;

@Slf4j
@PluginDescriptor(
	name = "Player Outline",
	description = "A simple plugin that outlines the player allowing you to see the player behind objects.",
	tags = "highlight, player, outline, color"
)
public class PlayerOutlinePlugin extends Plugin {
	@Inject
	PlayerOutlineOverlay playerOutlineOverlay;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	PlayerOutlineConfig config;
	@Inject
	Client client;
	private Color activeColor;

	@Override
	protected void startUp() {
		activeColor = config.playerOutlineColor();
		overlayManager.add(playerOutlineOverlay);
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(playerOutlineOverlay);
	}

	@Subscribe
	protected void onGameTick(GameTick tick)
	{
		if(config.prayerChanging())
			updateColor();
	}

	public Color getActiveColor()
	{
		return activeColor;
	}

	@Provides
	PlayerOutlineConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerOutlineConfig.class);
	}

	void updateColor()
	{
		Player p = client.getLocalPlayer();
		if(p==null)
			return;
        HeadIcon currentOverhead = p.getOverheadIcon();
		if(currentOverhead == null)
		{
			activeColor = config.playerOutlineColor();
			return;
		}
		switch(currentOverhead)
		{
			case MAGIC:
				activeColor = config.playerOutlineColorMage();
				break;
			case MELEE:
				activeColor = config.playerOutlineMelee();
				break;
			case RANGED:
				activeColor = config.playerOutlineColorRange();
				break;
			case SMITE:
				activeColor = config.playerOutlineColorSmite();
				break;
			case REDEMPTION:
				activeColor = config.playerOutlineColorRedemption();
				break;
			case RETRIBUTION:
				activeColor = config.playerOutlineColorRet();
				break;
		}
	}
}
