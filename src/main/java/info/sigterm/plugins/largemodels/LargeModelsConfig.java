package info.sigterm.plugins.largemodels;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("largemodels")
public interface LargeModelsConfig extends Config
{
	@ConfigItem(
		keyName = "numFaces",
		name = "Number of faces",
		description = "",
		position = -1
	)
	default int numFaces()
	{
		return 512;
	}

	@ConfigItem(
		keyName = "groundObjects",
		name = "Ground objects",
		description = ""
	)
	default boolean groundObjects()
	{
		return true;
	}

	@ConfigItem(
		keyName = "gameObjects",
		name = "Game objects",
		description = ""
	)
	default boolean gameObjects()
	{
		return true;
	}

	@ConfigItem(
		keyName = "walls",
		name = "Walls",
		description = ""
	)
	default boolean walls()
	{
		return true;
	}

	@ConfigItem(
		keyName = "decorations",
		name = "Decorations",
		description = ""
	)
	default boolean decorations()
	{
		return true;
	}
}
