/*
 * Copyright (c) 2019, Slay to Stay, <https://github.com/slaytostay>
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
package com.goaltracker;

import static com.goaltracker.GoalTrackerConfig.COLLAPSE_REQUIREMENTS_KEY;
import static com.goaltracker.GoalTrackerConfig.CONFIG_GROUP;
import static com.goaltracker.GoalTrackerConfig.HIDE_COMPLETED_GOALS_KEY;
import static com.goaltracker.GoalTrackerConfig.OLD_CONFIG_GROUP;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.goaltracker.ui.GoalTrackerPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@PluginDescriptor(
		name = "Region Goal Tracker",
		description = "Add goals to chunks",
		tags = {"chunk", "backlog", "log"},
		enabledByDefault = false
)
public class GoalTrackerPlugin extends Plugin
{
	private static final String PLUGIN_NAME = "Goal Tracker";
	private static final String TASK_LIST_CONFIG_KEY = "goals";
	private static final String ICON_FILE = "panel_icon.png";

	public static final Color COMPLETED_GREEN = Color.decode("#0dc10d");

	@Getter
	private final LinkedList<Goal> goals = new LinkedList<>();

	@Getter
	@Setter
	private boolean hotkeyPressed = false;

	@Inject
	private Client client;

	@Inject
	public GoalTrackerConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientUI clientUi;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GoalTrackerOverlay overlay;

	@Inject
	private KeyManager keyManager;

	@Inject
	private GoalTrackerInput inputListener;

	@Inject
	private Gson gson;

	private GoalTrackerPanel pluginPanel;
	private NavigationButton navigationButton;

	@Provides
	GoalTrackerConfig provideConfig(ConfigManager configManager)
	{
		// Migrate old config values
		if (configManager.getConfiguration(CONFIG_GROUP, "migrationCompleted") == null)
		{
			log.info("Migrating region locker goal tracker config...");
			String[] oldConfigKeys =
			{
				TASK_LIST_CONFIG_KEY,
				"drawMapOverlay",
				"enableTooltip",
				"hotKey",
				"noProgressColor",
				"inProgressColor",
				"completedColor",
				"requiredChunkColor"
			};
			for (String key : oldConfigKeys)
			{
				String value = configManager.getConfiguration(CONFIG_GROUP, key);
				if (value == null)
				{
					value = configManager.getConfiguration(OLD_CONFIG_GROUP, key);
					if (value != null)
					{
						configManager.setConfiguration(CONFIG_GROUP, key, value);
					}
				}
			}

			configManager.setConfiguration(CONFIG_GROUP, "migrationCompleted", true);
		}

		return configManager.getConfig(GoalTrackerConfig.class);
	}

	@Override
	protected void startUp()
	{
		String taskListJson = configManager.getConfiguration(CONFIG_GROUP, TASK_LIST_CONFIG_KEY);
		loadConfig(taskListJson).forEach(goals::add);

		pluginPanel = injector.getInstance(GoalTrackerPanel.class);
		pluginPanel.rebuild();

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), ICON_FILE);

		navigationButton = NavigationButton.builder()
				.tooltip(PLUGIN_NAME)
				.icon(icon)
				.priority(1)
				.panel(pluginPanel)
				.build();

		clientToolbar.addNavigation(navigationButton);

		keyManager.registerKeyListener(inputListener);

		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.removeIf(GoalTrackerOverlay.class::isInstance);
		goals.clear();
		clientToolbar.removeNavigation(navigationButton);
		keyManager.unregisterKeyListener(inputListener);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		switch (event.getKey())
		{
			case HIDE_COMPLETED_GOALS_KEY:
			case COLLAPSE_REQUIREMENTS_KEY:
				updateGoals();
				break;
		}
	}

	public void updateConfig()
	{
		if (goals.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, TASK_LIST_CONFIG_KEY);
		}
		else
		{
			String json = gson.toJson(goals);
			configManager.setConfiguration(CONFIG_GROUP, TASK_LIST_CONFIG_KEY, json);
		}
	}

	public Stream<Goal> loadConfig(String json)
	{
		if (Strings.isNullOrEmpty(json))
		{
			return Stream.empty();
		}

		try
		{
			final List<Goal> goalData = gson.fromJson(json, new TypeToken<ArrayList<Goal>>(){}.getType());

			return goalData.stream();
		}
		catch (Exception e)
		{
			updateConfig();
			return Stream.empty();
		}
	}

	public void addGoal()
	{
		Goal goal = new Goal(
			"Goal " + (goals.size() + 1),
			12850,
			new ArrayList<>(),
			false,
			false
		);

		goals.addFirst(goal);
		updateGoals();
		updateConfig();
	}

	public void deleteGoal(final Goal goal)
	{
		goals.remove(goal);
		updateGoals();
		updateConfig();
	}

	public void updateGoals()
	{
		pluginPanel.updateGoals();
	}

	public void reorderGoal(Goal goal, int newIndex)
	{
		goals.remove(goal);
		goals.add(newIndex, goal);
		updateConfig();
	}
}
