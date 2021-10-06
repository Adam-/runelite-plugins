package tictac7x.tithe;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tictac7x.tithe")
public interface TitheConfig extends Config {
	@ConfigItem(
		keyName = "watering_cans",
		name = "Highligh Watering Cans",
		description = "Highlight watering cans based on how much water they contain"
	)
	default boolean highlightWateringCans() {
		return true;
	}

	@ConfigItem(
		keyName = "farm_patches",
		name = "Highligh Farm Patches",
		description = "Highlight farm patches that are available to be used"
	)
	default boolean highlightFarmPatches() {
		return true;
	}
}
