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
		keyName = "hotKey",
		name = "Tooltip hotkey",
		description = "Which key to hold to view the goals tooltip on the map",
		position = 3
	)
	default Keybind hotKey()
	{
		return Keybind.SHIFT;
	}

	@ConfigItem(
			keyName = "enableQuickDelete",
			name = "Enable shift quick-delete",
			description = "Allows deleting goals more quickly by holding down shift while clicking the delete button",
			position = 4
	)
	default boolean enableQuickDelete()
	{
		return false;
	}

	String HIDE_COMPLETED_GOALS_KEY = "hideCompletedGoals";
	@ConfigItem(
			keyName = HIDE_COMPLETED_GOALS_KEY,
			name = "Hide goals when completed",
			description = "Hides all completed goals from the goal tracker panel",
			position = 5
	)
	default boolean hideCompletedGoals()
	{
		return false;
	}

	String COLLAPSE_REQUIREMENTS_KEY = "collapseRequirements";
	@ConfigItem(
			keyName = COLLAPSE_REQUIREMENTS_KEY,
			name = "Collapse requirements",
			description = "Hides all goal requirements by default",
			position = 6
	)
	default boolean collapseRequirements()
	{
		return false;
	}

	String BLOCKED_COLOR_KEY = "noProgressColor";
	@Alpha
	@ConfigItem(
			keyName = BLOCKED_COLOR_KEY,
			name = "Blocked color",
			description = "Color of goals with no progress",
			position = 7
	)
	default Color blockedColor()
	{
		return Color.RED;
	}

	String IN_PROGRESS_COLOR_KEY = "inProgressColor";
	@Alpha
	@ConfigItem(
			keyName = IN_PROGRESS_COLOR_KEY,
			name = "In-progress color",
			description = "Color of goals that are in progress",
			position = 8
	)
	default Color inProgressColor()
	{
		return Color.YELLOW;
	}

	String COMPLETED_COLOR_KEY = "completedColor";
	@Alpha
	@ConfigItem(
			keyName = COMPLETED_COLOR_KEY,
			name = "Completed color",
			description = "Color of completed goals",
			position = 9
	)
	default Color completedColor()
	{
		return Color.decode("#0dc10d"); // Same color as Jagex uses for completed quests
	}

	String REQUIRED_CHUNK_COLOR_KEY = "requiredChunkColor";
	@Alpha
	@ConfigItem(
			keyName = REQUIRED_CHUNK_COLOR_KEY,
			name = "Required chunk color",
			description = "Color of chunks that are a requirement for goals",
			position = 10
	)
	default Color requiredChunkColor()
	{
		return Color.MAGENTA;
	}
}
