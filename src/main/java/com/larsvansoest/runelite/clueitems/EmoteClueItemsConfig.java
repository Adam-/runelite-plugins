package com.larsvansoest.runelite.clueitems;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface EmoteClueItemsConfig extends Config
{
	@ConfigSection(
		name = "Interface Selection",
		description = "Toggle highlighting per interface type.",
		position = 0
	)
	String Section_selectInterface = "selectInterface";

	@ConfigItem(
		keyName = "HighlightBank",
		name = "Bank",
		description = "Show highlights on bank interface.",
		section = Section_selectInterface,
		position = 00
	)
	default boolean highlightBank() { return true; }

	@ConfigItem(
		keyName = "HighlightInventory",
		name = "Inventory",
		description = "Show highlights on inventory interface.",
		section = Section_selectInterface,
		position = 01
	)
	default boolean highlightInventory() { return true; }

	@ConfigItem(
		keyName = "HighlightEquipment",
		name = "Equipment",
		description = "Show highlights on equipment interface.",
		section = Section_selectInterface,
		position = 02
	)
	default boolean highlightEquipment() { return false; }

	@ConfigItem(
		keyName = "HighlightShop",
		name = "Shops",
		description = "Show highlights on shop interfaces.",
		section = Section_selectInterface,
		position = 03
	)
	default boolean highlightShop() { return false; }
}
