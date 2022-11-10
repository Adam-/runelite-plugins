package com.raidtracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("raidtracker")
public interface RaidTrackerConfig extends Config
{
	@ConfigSection(
			name = "Tombs of Amascot",
			description = "Settings for Tombs of Amascot",
			position = 1
	)
	String toasection = "Tombs of Amascot";
	@ConfigSection(
			name = "Chambers of Xeric",
			description = "Settings for Chambers of Xeric",
			position = 1
	)
	String coxSection = "Chambers of Xeric";
	@ConfigItem(
		keyName = "defaultFFA",
		name = "default FFA",
		description = "Sets the default split to free for all, rather than split"
	)
	default boolean defaultFFA()
	{
		return false;
	}

	@ConfigItem(
			keyName = "FFACutoff",
			name = "FFA cut off",
			description = "The value of which, when the split reaches under that value, is considered free for all"
	)

	default int FFACutoff() {
		return 1000000;
	}
	@ConfigItem(
			keyName = "lastXKills",
			name = "Last X Kills",
			description = "When the 'Last X Kills' option is selected, this value is used as X"
	)

	default int lastXKills() {return 50;}

	@ConfigItem(
			keyName = "showTitle",
			name = "Show Title",
			description = "Disable this checkmark to hide the title in the ui"
	)
	default boolean showTitle()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dey0Tracker",
		name = "Log raid room times (dey0)",
		description = "Track raid room times with dey0's raid timers plugin. This will replace the regular time splits panel with more detailed times of each part of the raid",
		section = coxSection
	)
	default boolean dey0Tracker() { return false; }
	@ConfigItem(
			keyName = "toatracker",
			name = "Compact TOA times",
			description = "If enabled will track seperate room times, not combining path/boss",
			section = toasection
	)
	default boolean toatracker() { return false; }

	@ConfigItem(
			keyName = "showKillsLogged",
			name = "Show Kills Logged",
			description = "Disable this checkmark to hide the Kills Logged panel in the ui"

	)
	default boolean showKillsLogged()
	{
		return true;
	}


	@ConfigItem(
			keyName = "showFilters",
			name = "Show Filters",
			description = "Disable this checkmark to hide the Filter Panel in the ui"
	)
	default boolean showFilters()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showUniquesTable",
			name = "Show Uniques Table",
			description = "Disable this checkmark to hide the Uniques Table in the ui"
	)
	default boolean showUniquesTable()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showPoints",
			name = "Show Points",
			description = "Disable this checkmark to hide the Points Panel in the ui",
			section = coxSection
	)
	default boolean showPoints()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showTimeSplits",
			name = "Show Time Splits",
			description = "Disable this checkmark to hide the Time Splits Panel in the ui"
	)
	default boolean showTimeSplits()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showSplitGPEarned",
			name = "Show Split GP Earned",
			description = "Disable this checkmark to hide the Split GP Earned Panel in the ui"
	)
	default boolean showSplitGPEarned()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showMVPs",
			name = "Show MVP's",
			description = "Disable this checkmark to hide the MVP's Panel in the ui"
	)
	default boolean showMVPs()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showRegularDrops",
			name = "Show Regular Drops",
			description = "Disable this checkmark to hide the Regular Drops Panel in the ui"
	)
	default boolean showRegularDrops()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showSplitChanger",
			name = "Show Split Changer",
			description = "Disable this checkmark to hide the Split Changer in the ui"
	)
	default boolean showSplitChanger()
	{
		return true;
	}

}
