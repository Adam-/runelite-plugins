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
		position = 0
	)
	default boolean highlightBank() { return true; }

	@ConfigItem(
		keyName = "HighlightInventory",
		name = "Inventory",
		description = "Show highlights on inventory interface.",
		section = Section_selectInterface,
		position = 1
	)
	default boolean highlightInventory() { return true; }


	@ConfigItem(
		keyName = "HighlightDepositBox",
		name = "Deposit Box",
		description = "Show highlights on deposit box interface.",
		section = Section_selectInterface,
		position = 2
	)
	default boolean highlightDepositBox() { return false; }

	@ConfigItem(
		keyName = "HighlightEquipment",
		name = "Equipment",
		description = "Show highlights on equipment interface.",
		section = Section_selectInterface,
		position = 3
	)
	default boolean highlightEquipment() { return false; }

	@ConfigItem(
		keyName = "HighlightGuidePrices",
		name = "Guide Prices",
		description = "Show highlights on guide prices interface.",
		section = Section_selectInterface,
		position = 4
	)
	default boolean highlightGuidePrices() { return false; }

	@ConfigItem(
		keyName = "HighlightKeptOnDeath",
		name = "Kept on Death",
		description = "Show highlights on kept on death interface.",
		section = Section_selectInterface,
		position = 5
	)
	default boolean highlightKeptOnDeath() { return false; }

	@ConfigItem(
		keyName = "HighlightShop",
		name = "Shops",
		description = "Show highlights on shop interfaces.",
		section = Section_selectInterface,
		position = 6
	)
	default boolean highlightShop() { return false; }
}
