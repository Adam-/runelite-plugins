/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, Lars van Soest
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.larsvansoest.runelite.clueitems.config;

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
