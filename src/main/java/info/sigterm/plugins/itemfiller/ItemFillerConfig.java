package info.sigterm.plugins.itemfiller;

import net.runelite.api.ItemID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("itemfiller")
public interface ItemFillerConfig extends Config
{
	@ConfigItem(
		keyName = "itemid",
		name = "Item ID",
		description = "The item to replace with the filler"
	)
	default int filler()
	{
		return ItemID.AL_KHARID_FLYER;
	}
}
