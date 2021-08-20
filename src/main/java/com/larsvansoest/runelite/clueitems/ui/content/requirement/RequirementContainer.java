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

package com.larsvansoest.runelite.clueitems.ui.content.requirement;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

/**
 * Lists {@link RequirementPanel} entries, provides functionality to display filtered sub-sets.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class RequirementContainer extends JPanel
{
	private final GridBagConstraints c;
	private final Map<String, Object> filterables;
	private RequirementPanel expandedPanel;
	private List<? extends RequirementPanel> requirementPanels;

	public RequirementContainer()
	{
		super(new GridBagLayout());
		this.filterables = new HashMap<>();
		this.expandedPanel = null;
		this.c = new GridBagConstraints();
		this.c.fill = GridBagConstraints.HORIZONTAL;
		this.c.gridx = 0;
		this.c.weightx = 1;
	}

	public void load(final Collection<? extends RequirementPanel> requirementPanelCollection)
	{
		this.requirementPanels = new ArrayList<>(requirementPanelCollection);
		this.display(this.requirementPanels.stream());
	}

	public void toggleFold(final RequirementPanel requirementPanel)
	{
		final RequirementPanel previous = this.expandedPanel;
		if (this.expandedPanel != null)
		{
			this.expandedPanel.fold();
			this.expandedPanel = null;
		}
		if (previous != requirementPanel)
		{
			requirementPanel.unfold();
			this.expandedPanel = requirementPanel;
		}
		super.revalidate();
		super.repaint();
	}

	public void setFilter(final String key, final Object value)
	{
		this.filterables.put(key, value);
	}

	public void runFilters()
	{
		this.display(this.requirementPanels.stream().filter(requirementPanel -> this.filterables.entrySet().stream().allMatch(filter ->
		{
			final Object requirementValue = requirementPanel.getFilterable(filter.getKey());
			if (requirementValue instanceof Collection<?>)
			{
				return ((Collection<?>) requirementValue).stream().anyMatch(filterValueElement -> this.filterValueMatches(filterValueElement, filter.getValue()));
			}
			return this.filterValueMatches(requirementValue, filter.getValue());
		})));
	}

	private Boolean filterValueMatches(final Object filterValue, final Object value)
	{
		return filterValue == null || value == null || (value instanceof String) && (filterValue instanceof String) && ((String) filterValue)
				.toLowerCase()
				.contains(((String) value).toLowerCase()) || value.equals(filterValue);
	}

	public void sort(final SortType sortType, final Boolean reversed)
	{
		switch (sortType)
		{
			case Name:
				this.requirementPanels.sort(Comparator.comparing(RequirementPanel::getName));
				break;
			case Quantity:
				this.requirementPanels.sort(Comparator.comparing(RequirementPanel::getQuantity));
				break;
			default:
				throw new IllegalArgumentException();
		}
		if (reversed)
		{
			Collections.reverse(this.requirementPanels);
		}
		this.runFilters();
	}

	private void display(final Stream<? extends RequirementPanel> requirementPanels)
	{
		super.removeAll();
		this.c.gridy = 0;

		requirementPanels.forEachOrdered(requirementPanel ->
		{
			super.add(requirementPanel, this.c);
			this.c.gridy++;
		});
		super.revalidate();
		super.repaint();
	}
}
