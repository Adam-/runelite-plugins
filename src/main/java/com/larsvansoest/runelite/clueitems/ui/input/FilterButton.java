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

package com.larsvansoest.runelite.clueitems.ui.input;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FilterButton<T> extends JPanel
{
	private final JLabel optionLabel;
	private final Queue<FilterButtonOption<T>> optionQueue;
	private final Runnable onChange;

	private FilterButtonOption<T> currentOption;
	private Map.Entry<T, Icon> currentValue;

	private final String defaultToolTip;

	public FilterButton(T defaultValue, Icon defaultIcon, String defaultToolTip, Dimension dimension, Color defaultColor, Color hoverColor, int capacity, Runnable onChange) {
		this(new AbstractMap.SimpleImmutableEntry<>(defaultValue, defaultIcon), defaultToolTip, dimension, defaultColor, hoverColor, capacity, onChange);
	}

	FilterButton(Map.Entry<T, Icon> primary, String defaultToolTip, Dimension dimension, Color defaultColor, Color hoverColor, int capacity, Runnable onChange) {
		this(primary, primary, defaultToolTip, dimension, defaultColor, hoverColor, capacity, onChange);
	}

	public FilterButton(T defaultPrimaryValue, Icon defaultPrimaryIcon, T defaultSecondaryValue, Icon defaultSecondaryIcon, String defaultToolTip, Dimension dimension, Color defaultColor, Color hoverColor, int capacity, Runnable onChange)
	{
		this(new AbstractMap.SimpleImmutableEntry<>(defaultPrimaryValue, defaultPrimaryIcon), new AbstractMap.SimpleImmutableEntry<>(defaultSecondaryValue, defaultSecondaryIcon), defaultToolTip, dimension, defaultColor, hoverColor, capacity, onChange);
	}

	FilterButton(Map.Entry<T, Icon> primary, Map.Entry<T, Icon> secondary, String defaultToolTip, Dimension dimension, Color defaultColor, Color hoverColor, int capacity, Runnable onChange) {
		super(new GridBagLayout());
		super.setPreferredSize(dimension);
		super.setMinimumSize(dimension);
		super.setMaximumSize(dimension);
		super.setBackground(defaultColor);
		super.setToolTipText(defaultToolTip);
		super.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				FilterButton.this.next(e.getButton() == MouseEvent.BUTTON1);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				FilterButton.super.setBackground(hoverColor);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				FilterButton.super.setBackground(defaultColor);
			}
		});

		this.optionLabel = new JLabel();
		this.optionLabel.setHorizontalAlignment(JLabel.CENTER);
		this.optionLabel.setVerticalAlignment(JLabel.CENTER);
		this.optionLabel.setIcon(primary.getValue());

		this.optionQueue = new ArrayDeque<>(capacity);
		this.defaultToolTip = defaultToolTip;
		this.currentOption = new FilterButtonOption<>(primary, secondary, defaultToolTip);
		this.currentValue = primary;
		this.onChange = onChange;

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		super.add(this.optionLabel, c);
	}

	private void next(Boolean isPrimaryMouseKey)
	{
		FilterButtonOption<T> option;
		if(isPrimaryMouseKey || this.currentOption.getPrimary() == this.currentOption.getSecondary()) {
			option = Objects.requireNonNull(this.optionQueue.poll());
			this.optionQueue.add(this.currentOption);
			this.currentOption = option;
			this.currentValue = option.getPrimary();
			this.optionLabel.setIcon(option.getPrimary().getValue());
		}
		else
		{
			option = this.currentOption;
			Boolean isPrimaryValue = this.isPrimaryValue();
			this.currentValue = isPrimaryValue ? this.currentOption.getSecondary() : this.currentOption.getPrimary();
			this.optionLabel.setIcon(isPrimaryValue ? this.currentOption.getSecondary().getValue() : this.currentOption.getPrimary().getValue());
		}
		String toolTip = option.getToolTip();
		super.setToolTipText(toolTip == null ? this.defaultToolTip : toolTip);
		this.onChange.run();
	}

	public void addOption(T value, Icon icon, String toolTip)
	{
		Map.Entry<T, Icon> primary = new AbstractMap.SimpleImmutableEntry<>(value, icon);
		this.optionQueue.add(new FilterButtonOption<>(primary, primary, toolTip));
	}

	public void addOption(T primaryValue, Icon primaryIcon, T secondaryValue, Icon secondaryIcon, String toolTip)
	{
		Map.Entry<T, Icon> primary = new AbstractMap.SimpleImmutableEntry<>(primaryValue, primaryIcon);
		Map.Entry<T, Icon> secondary = new AbstractMap.SimpleImmutableEntry<>(secondaryValue, secondaryIcon);
		this.optionQueue.add(new FilterButtonOption<>(primary, secondary, toolTip));
	}

	public T getSelectedValue()
	{
		return this.currentValue.getKey();
	}

	public Boolean isPrimaryValue() { return this.currentValue == this.currentOption.getPrimary(); }
}
