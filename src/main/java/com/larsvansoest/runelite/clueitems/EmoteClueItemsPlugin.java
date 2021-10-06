/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, Lars van Soest
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.larsvansoest.runelite.clueitems;

import com.google.inject.Provides;
import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.data.StashUnit;
import com.larsvansoest.runelite.clueitems.overlay.EmoteClueItemsOverlay;
import com.larsvansoest.runelite.clueitems.progress.ProgressManager;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel;
import com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import javax.swing.*;

/**
 * Main class of the plugin.
 * <p>
 * Provides the user with an overlay and item collection database to track {@link com.larsvansoest.runelite.clueitems.data.EmoteClue} requirement progression.
 *
 * @see com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel
 * @see com.larsvansoest.runelite.clueitems.overlay.EmoteClueItemsOverlay
 */
@Slf4j
@PluginDescriptor(name = "Emote Clue Items",
                  description = "Highlight required items for emote clue steps.",
                  tags = {"emote", "clue", "item", "items", "scroll"})
public class EmoteClueItemsPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private EmoteClueItemsConfig config;
	@Inject
	private ConfigManager configManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private ClientToolbar clientToolbar;
	private EmoteClueItemsOverlay overlay;
	private NavigationButton navigationButton;
	private ProgressManager progressManager;
	private EmoteClueItemsPanel emoteClueItemsPanel;

	private boolean updateStashBuiltStatusOnNextGameTick;

	@Override
	protected void startUp()
	{
		this.overlay = new EmoteClueItemsOverlay(this.itemManager, this.config);
		this.overlayManager.add(this.overlay);

		final EmoteClueItemsPalette emoteClueItemsPalette = EmoteClueItemsPalette.RUNELITE;
		this.emoteClueItemsPanel = new EmoteClueItemsPanel(emoteClueItemsPalette,
				this.itemManager,
				this::onStashUnitFilledChanged,
				"Emote Clue Items",
				"v3.1.1",
				"https://github.com/larsvansoest/emote-clue-items"
		);

		this.navigationButton = NavigationButton
				.builder()
				.tooltip("Emote Clue Items")
				.icon(EmoteClueImages.resizeCanvas(EmoteClueImages.Ribbon.ALL, 16, 16))
				.priority(7)
				.panel(this.emoteClueItemsPanel)
				.build();

		this.clientToolbar.addNavigation(this.navigationButton);

		this.progressManager = new ProgressManager(this.configManager,
				this.client,
				this.clientThread,
				this::onEmoteClueItemQuantityChanged,
				this::onEmoteClueItemInventoryStatusChanged,
				this::onEmoteClueItemStatusChanged
		);

		this.reset();
	}

	private void reset()
	{
		this.progressManager.reset();
		this.emoteClueItemsPanel.reset();

		final String loginDisclaimer = "To start display of progression, please login first.";
		for (final StashUnit stashUnit : StashUnit.values())
		{
			this.emoteClueItemsPanel.turnOnSTASHFilledButton(stashUnit);
			this.emoteClueItemsPanel.turnOffSTASHFilledButton(stashUnit, new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.WAITING), loginDisclaimer);
		}
		this.emoteClueItemsPanel.setEmoteClueItemGridDisclaimer(loginDisclaimer);
		this.emoteClueItemsPanel.setSTASHUnitGridDisclaimer(loginDisclaimer);

		this.updateStashBuiltStatusOnNextGameTick = false;

		if (this.client.getGameState() == GameState.LOGGED_IN)
		{
			this.onPlayerLoggedIn();
		}
	}

	private void onStashUnitFilledChanged(final StashUnit stashUnit, final boolean filled)
	{
		this.progressManager.setStashUnitFilled(stashUnit, filled);
	}

	private void onEmoteClueItemQuantityChanged(final EmoteClueItem emoteClueItem, final int quantity)
	{
		this.emoteClueItemsPanel.setEmoteClueItemQuantity(emoteClueItem, quantity);
	}

	private void onEmoteClueItemInventoryStatusChanged(final EmoteClueItem emoteClueItem, final UpdatablePanel.Status status)
	{
		this.emoteClueItemsPanel.setEmoteClueItemCollectionLogStatus(emoteClueItem, status);
	}

	private void onEmoteClueItemStatusChanged(final EmoteClueItem emoteClueItem, final UpdatablePanel.Status status)
	{
		this.emoteClueItemsPanel.setEmoteClueItemStatus(emoteClueItem, status);
	}

	@Subscribe
	protected void onChatMessage(final ChatMessage event)
	{
		if (event.getType() == ChatMessageType.SPAM && event.getMessage().equals("You build a STASH unit."))
		{
			this.updateStashUnitBuildStatuses();
		}
	}

	@Subscribe
	protected void onItemContainerChanged(final ItemContainerChanged event)
	{
		this.progressManager.processInventoryChanges(event);
		if (event.getContainerId() == 95)
		{
			this.emoteClueItemsPanel.removeEmoteClueItemGridDisclaimer();
		}
	}

	private void updateStashUnitBuildStatuses()
	{
		for (final StashUnit stashUnit : StashUnit.values())
		{
			this.clientThread.invokeLater(() ->
			{
				this.client.runScript(ScriptID.WATSON_STASH_UNIT_CHECK, stashUnit.getStashUnit().getObjectId(), 0, 0, 0);
				final boolean built = this.client.getIntStack()[0] == 1;
				this.emoteClueItemsPanel.turnOnSTASHFilledButton(stashUnit);
				this.emoteClueItemsPanel.setSTASHUnitStatus(stashUnit, built, this.progressManager.getStashUnitFilled(stashUnit));
			});
		}
	}

	@Subscribe
	protected void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			this.reset();
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			this.onPlayerLoggedIn();
		}
	}

	private void onPlayerLoggedIn()
	{
		this.progressManager.validateConfig();
		this.updateStashBuiltStatusOnNextGameTick = true;
		this.emoteClueItemsPanel.removeEmoteClueItemGridDisclaimer();
		this.emoteClueItemsPanel.removeSTASHUnitGridDisclaimer();
		this.emoteClueItemsPanel.setEmoteClueItemGridDisclaimer("To include bank items, please open your bank once.");
	}

	@Subscribe
	public void onGameTick(final GameTick event)
	{
		if (this.updateStashBuiltStatusOnNextGameTick)
		{
			this.updateStashBuiltStatusOnNextGameTick = false;
			this.updateStashUnitBuildStatuses();
		}
	}

	protected void onConfigChanged(final ConfigChanged event)
	{
		if (event.getKey().equals("DisplayProgressPanel"))
		{
			if (event.getNewValue().equals("false"))
			{
				this.clientToolbar.removeNavigation(this.navigationButton);
			}
			else
			{
				this.clientToolbar.addNavigation(this.navigationButton);
			}
		}
	}

	@Override
	protected void shutDown()
	{
		this.overlayManager.remove(this.overlay);
		this.clientToolbar.removeNavigation(this.navigationButton);
	}

	@Provides
	EmoteClueItemsConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(EmoteClueItemsConfig.class);
	}
}
