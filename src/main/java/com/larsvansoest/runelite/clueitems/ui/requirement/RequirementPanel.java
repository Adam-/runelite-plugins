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

package com.larsvansoest.runelite.clueitems.ui.requirement;

import com.larsvansoest.runelite.clueitems.ui.requirement.foldable.FoldablePanel;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanelPalette;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class which represents requirement visualisation in a {@link RequirementContainer}.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public abstract class RequirementPanel extends FoldablePanel
{
	private final RequirementContainer parent;
	private String quantityText;
	private final Map<Object, Object> filterables;

	public RequirementPanel(RequirementContainer parent, EmoteClueItemsPanelPalette emoteClueItemsPanelPalette, String name)
	{
		super(emoteClueItemsPanelPalette, name);

		this.parent = parent;
		this.filterables = new HashMap<>();
		this.setFilterable("name", name);

		this.setStatus(RequirementStatus.Unknown);
	}

	@Override
	public final void setStatus(RequirementStatus status)
	{
		this.setFilterable("status", status);
		super.setStatus(status);
	}

	@Override
	public final void onHeaderMousePressed()
	{
		this.parent.toggleFold(this);
	}

	public final void setFilterable(Object key, Object value)
	{
		this.filterables.put(key, value);
	}

	public final Object getFilterable(Object key)
	{
		return this.filterables.get(key);
	}

	public String getQuantity()
	{
		return this.quantityText;
	}

	public void setQuantity(String text)
	{
		super.getFoldableHeader().setQuantityLabel(text);
		this.quantityText = text;
	}
}