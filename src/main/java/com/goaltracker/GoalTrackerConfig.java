package com.goaltracker;

import static com.goaltracker.GoalTrackerConfig.CONFIG_GROUP;
import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup(CONFIG_GROUP)
public interface GoalTrackerConfig extends Config
{
	String OLD_CONFIG_GROUP = "goaltracker";
	String CONFIG_GROUP = "RegionLockerGoalTracker";

	@ConfigItem(
			keyName = "drawMapOverlay",
			name = "Draw goal chunks on map",
			description = "Draw a colored border for each chunk with goals",
			position = 1
	)
	default boolean drawMapOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "enableTooltip",
			name = "Enable tooltip",
			description = "Show tooltip with goals of the chunk you hover over while holding the hotkey below",
			position = 2
	)
	default boolean enableTooltip()
	{
		return true;
	}

	@ConfigItem(
			keyName = "enableQuickDelete",
			name = "Enable shift delete",
			description = "Allows deleting goals more quickly by holding down shift while clicking the delete button",
			position = 3
	)
	default boolean enableQuickDelete()
	{
		return false;
	}

	String HIDE_COMPLETED_GOALS_KEY = "hideCompletedGoals";
	@ConfigItem(
			keyName = HIDE_COMPLETED_GOALS_KEY,
			name = "Hide completed goals",
			description = "Hides all completed goals from the goal tracker panel",
			position = 4
	)
	default boolean hideCompletedGoals()
	{
		return false;
	}

	@ConfigItem(
			keyName = "hotKey",
			name = "Hover hotkey",
			description = "Which key to hold to view the goals tooltip on the map",
			position = 5
	)
	default Keybind hotKey()
	{
		return Keybind.ALT;
	}

	@Alpha
	@ConfigItem(
			keyName = "noProgressColor",
			name = "No progress color",
			description = "Color of goals with no progress",
			position = 6
	)
	default Color noProgressColor()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
			keyName = "inProgressColor",
			name = "In-progress color",
			description = "Color of goals that are in progress",
			position = 7
	)
	default Color inProgressColor()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
			keyName = "completedColor",
			name = "Completed color",
			description = "Color of completed goals",
			position = 8
	)
	default Color completedColor()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			keyName = "requiredChunkColor",
			name = "Required chunk color",
			description = "Color of chunks that are a requirement for goals",
			position = 9
	)
	default Color requiredChunkColor()
	{
		return Color.MAGENTA;
	}
}
