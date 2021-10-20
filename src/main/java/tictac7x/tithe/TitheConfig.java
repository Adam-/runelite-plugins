package tictac7x.tithe;

import tictac7x.Overlay;
import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(TitheConfig.group)
public interface TitheConfig extends Config {
	String group = "tictac7x.tithe";

	@ConfigSection(
		position = 1,
		name = "Farming patches",
		description = "Highlight farming patches and show progress of plants"
	) String section_patches = "farming_patches";

		@ConfigItem(
			position = 1,
			keyName = "plants_dry",
			name = "Highlight dry plants",
			description = "Highlight dry plants that need to be watered",
			section = section_patches
		) default boolean highlightPlantsDry() {
			return true;
		}

		@ConfigItem(
			position = 2,
			keyName = "plants_dry_color",
			name = "Dry plants color",
			description = "Color of the dry plants progress",
			section = section_patches
		) default Color getPlantsDryColor() {
			return Overlay.color_red;
		}

		@ConfigItem(
			position = 3,
			keyName = "plants_watered",
			name = "Highlight watered plants",
			description = "Highlight watered plants",
			section = section_patches
		) default boolean highlightPlantsWatered() {
			return true;
		}

		@ConfigItem(
			position = 4,
			keyName = "plants_watered_color",
			name = "Watered plants color",
			description = "Color of the watered plants progress",
			section = section_patches
		) default Color getPlantsWateredColor() {
			return Overlay.color_green;
		}

		@ConfigItem(
			position = 5,
			keyName = "plants_grown",
			name = "Highlight grown plants",
			description = "Highlight grown plants",
			section = section_patches
		) default boolean highlightPlantsGrown() {
			return true;
		}

		@ConfigItem(
			position = 6,
			keyName = "plants_grown_color",
			name = "Grown plants color",
			description = "Color of the grown plants progress",
			section = section_patches
		) default Color getPlantsGrownColor() {
			return Overlay.color_yellow;
		}

		@ConfigItem(
			position = 7,
			keyName = "plants_blighted",
			name = "Highlight blighted plants",
			description = "Highlight blighted plants",
			section = section_patches
		) default boolean highlightPlantsBlighted() {
			return true;
		}

		@ConfigItem(
			position = 8,
			keyName = "plants_blighted_color",
			name = "Blighted plants color",
			description = "Color of the blighted plants progress",
			section = section_patches
		) default Color getPlantsBlightedColor() {
			return Overlay.color_gray;
		}

		@ConfigItem(
			position = 9,
			keyName = "farm_patches_hover",
			name = "Highlight farm patches",
			description = "Highlight farm patches on hover",
			section = section_patches
		) default boolean highlightPatchesOnHover() {
			return true;
		}

		@ConfigItem(
			position = 10,
			keyName = "farm_patches_hover_color",
			name = "Farm patches color",
			description = "Color of the highlighted farm patches on hover",
			section = section_patches
		) default Color getPatchesColor() {
			return Overlay.color_gray;
		}

	@ConfigSection(
		position = 2,
		name = "Inventory",
		description = "Highlight items needed for the tithe farming in the inventory"
	) String section_inventory = "inventory";

		@ConfigItem(
			position = 1,
			keyName = "seeds",
			name = "Highlight seeds",
			description = "Highlight seeds",
			section = section_inventory
		) default boolean highlightSeeds() {
			return true;
		}

		@ConfigItem(
			position = 2,
			keyName = "watering_cans",
			name = "Highlight watering cans",
			description = "Highlight watering cans based on how much water they contain",
			section = section_inventory
		) default boolean highlightWaterCans() {
			return true;
		}

		@ConfigItem(
			position = 3,
			keyName = "farmer_outfit",
			name = "Highlight farmer outfit",
			description = "Highlight farmer outfit when you only have fruits in inventory",
			section = section_inventory
		) default boolean highlightFarmersOutfit() {
			return true;
		}

	@ConfigSection(
		position = 3,
		name = "Water",
		description = "Show amount of available and total waters"
	) String section_water = "water";

		@ConfigItem(
			position = 1,
			keyName = "water",
			name = "Show amount of water",
			description = "Show total and available amount of water in watering cans",
			section = section_water
		) default boolean showWaterAmount() {
			return true;
		}

		String gricollers_can_charges = "gricollers_can_charges";
		@ConfigItem(
			position = 2,
			keyName = gricollers_can_charges,
			name = "Gricoller's can charges",
			description = "Hold amount of charges left in Gricoller's can",
			section = section_water,
			hidden = true
		) default int getGricollersCanCharges() {
			return 0;
		}

	@ConfigSection(
		position = 4,
		name = "Points",
		description = "Show custom information about tithe farm points"
	) String section_points = "points";

		String points = "points";
		@ConfigItem(
			position = 1,
			keyName = points,
			name = "Show custom points widget",
			description = "Show total, earned points and harvested fruits.",
			section = section_points
		) default boolean showCustomPoints() {
			return true;
		}
}
