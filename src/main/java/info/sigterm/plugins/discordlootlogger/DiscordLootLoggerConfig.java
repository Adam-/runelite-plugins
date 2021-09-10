package info.sigterm.plugins.discordlootlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

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

	@ConfigSection(
			name="Clan Drop Notifications",
			description = "Settings for clan drop notifications",
			position = 2
	)

	String clanDropNotifications = "Clan Drop Notifications";

	@ConfigItem(
			keyName = "enableClanDrops",
			name = "Enable Clan Drops",
			description = "Enable this if you want to automatically post a screenshot when you receive a clan drop",
			position = 1,
			section = clanDropNotifications
	)

	default boolean enableClanDrops() {
		return false;
	}

	@ConfigItem(
			keyName = "autoMessageEnabled",
			name = "Enable auto message",
			description = "Enables an auto message in the chatbox",
			section = clanDropNotifications,
			position = 2
	)
	default boolean autoMessageEnabled() {return false;}

	@ConfigItem(
			keyName = "autoMessageDate",
			name = "Enable date post",
			description = "Enables automatic date posting in the chat",
			section = clanDropNotifications,
			position = 3
	)
	default boolean autoMessageDate() {return false;}

	@ConfigItem(
			keyName = "autoMessage",
			name = "Auto Message",
			description = "The auto message to type",
			section = clanDropNotifications,
			position = 4)
	default String autoMessage() {return "";}


}
