package tictac7x.TODO_PLUGIN_NAME;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tictac7x-TODO_PLUGIN_NAME")
public interface TODO_PLUGIN_NAMEConfig extends Config {
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting() { return "Hello"; }
}
