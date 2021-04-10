/*
 * Copyright (c) 2019, dillydill123 <https://github.com/dillydill123>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.banktaglayouts.invsetupsstuff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

@AllArgsConstructor
public class InventorySetup
{
	@Getter
	private ArrayList<InventorySetupsItem> inventory;

	@Getter
	private ArrayList<InventorySetupsItem> equipment;

	@Getter
	private ArrayList<InventorySetupsItem> rune_pouch;

	@Getter
	private HashMap<Integer, InventorySetupsItem> additionalFilteredItems;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String notes;

	@Getter
	@Setter
	private Color highlightColor;

	@Getter
	@Setter
	private boolean highlightDifference;

	@Getter
	@Setter
	private boolean filterBank;

	@Getter
	@Setter
	private boolean unorderedHighlight;

	/*
		0 = Standard
		1 = Ancient
		2 = Lunar
		3 = Arceuus
		4 = NONE

		Avoiding Enum because won't work well with GSON (defaults to null)
	*/
	@Getter
	@Setter
	private int spellBook;

	public void updateInventory(final ArrayList<InventorySetupsItem> inv)
	{
		inventory = inv;
	}

	public void updateEquipment(final ArrayList<InventorySetupsItem> eqp)
	{
		equipment = eqp;
	}

	public void updateRunePouch(final ArrayList<InventorySetupsItem> rp)
	{
		rune_pouch = rp;
	}

	public void updateAdditionalItems(final HashMap<Integer, InventorySetupsItem> ai)
	{
		additionalFilteredItems = ai;
	}

	public void updateSpellbook(final int sb)
	{
		spellBook = sb;
	}

	public void updateNotes(final String text)
	{
		notes = text;
	}

}
