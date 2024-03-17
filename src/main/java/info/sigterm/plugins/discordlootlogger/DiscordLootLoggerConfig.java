package info.sigterm.plugins.discordlootlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(DiscordLootLoggerConfig.GROUP)
public interface DiscordLootLoggerConfig extends Config
{
	String GROUP = "discordlootlogger";

	@ConfigItem(
		keyName = "webhook",
		name = "Webhook URL",
		description = "The Discord Webhook URL to send messages to"
	)
	String webhook();

	@ConfigItem(
		keyName = "sendScreenshot",
		name = "Send Screenshot",
		description = "Includes a screenshot when receiving the loot"
	)
	default boolean sendScreenshot()
	{
		return false;
	}

	@ConfigItem(
		keyName = "lootnpcs",
		name = "Loot NPCs",
		description = "Only logs loot from these NPCs, comma separated"
	)
	String lootNpcs();

	@ConfigItem(
			keyName = "hiddenItems",
			name = "Hidden items",
			description = "Items that should not trigger a notification, comma separated. Accepts wildcards."
	)
	default String hiddenItems() {return "";};

	@ConfigItem(
		keyName = "includeLowValueItems",
		name = "Include Low Value Items",
		description = "Only log loot items worth more than the value set in loot value option."
	)
	default boolean includeLowValueItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lootvalue",
		name = "Loot Value",
		description = "Only logs loot worth more then the given value. 0 to disable."
	)
	default int lootValue()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "stackvalue",
		name = "Include Stack Value",
		description = "Include the value of each stack."
	)
	default boolean stackValue()
	{
		return false;
	}

	@ConfigItem(
		keyName = "includeusername",
		name = "Include Username",
		description = "Include your RSN in the post."
	)
	default boolean includeUsername()
	{
		return false;
	}

}
