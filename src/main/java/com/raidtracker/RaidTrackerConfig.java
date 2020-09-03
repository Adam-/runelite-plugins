package com.raidtracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("raidtracker")
public interface RaidTrackerConfig extends Config
{
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
}
