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
		description = "Should C Engineer announce when you fill in a new slot in your collection log? This one relies on you having chat messages (included with the popup option) enabled in game settings!"
	)
	default boolean announceCollectionLog()
	{
		return true;
	}

	@ConfigItem(
		keyName = "announceAchievementDiary",
		name = "Completed achievement diaries",
		description = "Should C Engineer announce when you complete a new achievement diary?"
	)
	default boolean announceAchievementDiary()
	{
		return true;
	}

//	@ConfigItem(
//		keyName = "announceClueScrolls",
//		name = "Each completed clue scroll step",
//		description = "Should C Engineer announce when you complete a clue scroll step?"
//	)
//	default boolean announceClueScrollStep()
//	{
//		return true;
//	}


}
