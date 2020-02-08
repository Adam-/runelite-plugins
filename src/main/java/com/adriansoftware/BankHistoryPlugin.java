/*
 * Copyright (c) 2020, Adrian Lee Elder <https://github.com/AdrianLeeElder>
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
package com.adriansoftware;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Bank Value Tracking",
	description = "Track the value of your bank over time",
	tags = {"bank", "value", "history", "tracking"}
)
@Slf4j
public class BankHistoryPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BankValueHistoryTracker tracker;

	@Inject
	private ClientToolbar clientToolbar;

	private NavigationButton navButton;
	private BankHistoryPanel bankHistoryPanel;
	private DefaultBankValuePanel defaultBankValuePanel;

	@Provides
	BankHistoryConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankHistoryConfig.class);
	}

	@Override
	protected void startUp()
	{
		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "bank_logo.png");

		navButton = NavigationButton.builder()
			.tooltip("Bank Value History")
			.icon(icon)
			.priority(10)
			.panel(getDesiredPanel())
			.build();

		clientToolbar.addNavigation(navButton);
	}

	private PluginPanel getDesiredPanel()
	{
		PluginPanel defaultPanel = getDefaultPanel();
		if (getDefaultPanel() == null)
		{
			if (bankHistoryPanel == null)
			{
				bankHistoryPanel = injector.getInstance(BankHistoryPanel.class);
				SwingUtilities.invokeLater(bankHistoryPanel::init);
			}

			return bankHistoryPanel;
		}

		return defaultPanel;
	}

	private PluginPanel getDefaultPanel()
	{
		List<String> accounts = tracker.getAvailableUsers();

		if (accounts.isEmpty())
		{
			if (defaultBankValuePanel == null)
			{
				defaultBankValuePanel = new DefaultBankValuePanel();
				SwingUtilities.invokeLater(defaultBankValuePanel::init);
			}

			return defaultBankValuePanel;
		}

		return null;
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
		{
		if ("setBankTitle".equals(event.getEventName()))
		{
			tracker.addEntry();
		}
	}

	@Subscribe
	public void onWidgetLoaded(final WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetID.BANK_GROUP_ID)
		{

			if (navButton.getPanel() != bankHistoryPanel)
			{
				PluginPanel panel = getDesiredPanel();

				if (panel == bankHistoryPanel)
				{
					navButton.setPanel(panel);
				}
			}

			bankHistoryPanel.setDatasetButton(true);
		}
	}

	@Subscribe
	public void onWidgetHiddenChanged(WidgetHiddenChanged event)
	{
		Widget widget = event.getWidget();
		int group = WidgetInfo.TO_GROUP(widget.getId());

		//when bank interface is closed
		//probably a better way to do this >.>
		if (!widget.isHidden())
		{
			switch (group)
			{
				case WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID:
					bankHistoryPanel.setDatasetButton(false);
					break;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState().getState())
		{
			case 40:
				bankHistoryPanel.setDatasetButton(false);
				break;
		}
	}
}
