package com.github.m0bilebtw;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(CEngineerCompletedConfig.GROUP)
public interface CEngineerCompletedConfig extends Config
{
	String GROUP = "cengineercompleted";

	@ConfigItem(
			keyName = "announceLevelUp",
			name = "Level ups",
			description = "Should C Engineer announce when you gain a level in a skill?"
	)
	default boolean announceLevelUp()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announceQuestCompletion",
		name = "Quest completions",
		description = "Should C Engineer announce when you complete a quest?"
	)
	default boolean announceQuestCompletion()
	{
		return true;
	}

	@ConfigItem(
			keyName = "announceDeath",
			name = "When you die",
			description = "Should C Engineer relive his PvP HCIM death when you die?"
	)
	default boolean announceDeath()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announceCollectionLog",
		name = "New collection log entry",
		description = "Should C Engineer announce when you fill in a new slot in your collection log?"
	)
	default boolean announceCollectionLog()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announceAchievementDiary",
		name = "Completed achievement diary tasks",
		description = "Should C Engineer announce when you complete a new achievement diary task?"
	)
	default boolean announceAchievementDiaryTask()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announceClueScrolls",
		name = "Each completed clue scroll step",
		description = "Should C Engineer announce when you complete a clue scroll step?"
	)
	default boolean announceClueScrollStep()
	{
		return true;
	}


}
