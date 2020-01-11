package com.adriansoftware;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bankhistory")
public interface BankHistoryConfig extends Config
{
	@ConfigItem(
		keyName = "defaultAccount",
		name = "Default account",
		description = "The account to show data for by default"
	)
	default String getDefaultAccount()
	{
		return "";
	}
}
