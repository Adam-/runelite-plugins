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

	@ConfigItem(
		keyName = "defaultBankTab",
		name = "Default bank tab",
		description = "The bank tab to select by default"
	)
	default int getDefaultBankTab()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "defaultDatasetEntry",
		name = "Default dataset entry",
		description = "The amount of time in hours that should elapse before adding a new data entry"
	)
	default int getDefaultDatasetEntry() {
		return 1;
	}
}
