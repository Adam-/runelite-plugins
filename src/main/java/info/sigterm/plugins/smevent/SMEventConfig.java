package info.sigterm.plugins.smevent;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("smevent")
public interface SMEventConfig extends Config
{
	@ConfigItem(
		keyName = "config",
		name = "Config",
		description = "Config"
	)
	default String config()
	{
		return "{}";
	}
}
