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

import com.larsvansoest.runelite.clueitems.overlay.widget.ItemWidgetContainer;

/**
 * Functions as a facade for {@link EmoteClueItemsConfig}, provides methods surrounding user-defined configuration parameters.
 * @since 1.2.0
 * @author Lars van Soest
 */
public class EmoteClueItemsConfigProvider
{
	private final EmoteClueItemsConfig config;

	public EmoteClueItemsConfigProvider(EmoteClueItemsConfig config)
	{
		this.config = config;
	}

	public boolean interfaceGroupSelected(ItemWidgetContainer container)
	{
		switch (container)
		{
			case Bank:
				return this.config.highlightBank();

			case DepositBox:
				return this.config.highlightDepositBox();

			case Inventory:
				return this.config.highlightInventory();

			case Equipment:
				return this.config.highlightEquipment();

			case Shop:
				return this.config.highlightShop();

			case KeptOnDeath:
				return this.config.highlightKeptOnDeath();

			case GuidePrices:
				return this.config.highlightGuidePrices();

			default:
				return false;
		}
	}
}