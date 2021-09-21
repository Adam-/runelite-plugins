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

package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

/**
 * Button which cycles values each time it is clicked. Allows use of unique icons to display currently selected value.
 *
 * @author Lars van Soest
 * @since 3.0.0
 */
public class CycleButton extends JPanel
{
	private final JLabel optionLabel;
	private final Queue<Stage> stageQueue;
	private final String defaultToolTip;
	private Stage currentStage;
	private Icon currentValue;
	private int minWidth;
	private int minHeight;

	@Getter
	private boolean turnedOn;
	private String toolTipBeforeDisabled;
	private Icon iconBeforeDisabled;

	private int stageCount;

	/**
	 * Creates the button with specified default value.
	 *
	 * @param primary         Icon to display when value is selected.
	 * @param onSelectPrimary Runnable to execute when value is selected.
	 * @param defaultToolTip  Tooltip to display when hovering the cycle button.
	 */
	public CycleButton(
			final EmoteClueItemsPalette emoteClueItemsPalette, final Icon primary, final Runnable onSelectPrimary, final String defaultToolTip)
	{
		this(emoteClueItemsPalette, primary, onSelectPrimary, null, null, defaultToolTip);
	}

	/**
	 * Creates the button with specified default left and right-click value.
	 *
	 * @param emoteClueItemsPalette Colour scheme for the button.
	 * @param primary               Icon to display when value is selected.
	 * @param onSelectPrimary       Runnable to execute when value is selected.
	 * @param secondary             Runnable to execute when secondary value is selected.
	 * @param onSelectSecondary     Runnable to execute when secondary value is selected.
	 * @param defaultToolTip        Tooltip to display when hovering the cycle button.
	 */
	public CycleButton(
			final EmoteClueItemsPalette emoteClueItemsPalette, final Icon primary, final Runnable onSelectPrimary, final Icon secondary, final Runnable onSelectSecondary, final String defaultToolTip)
	{
		super(new GridBagLayout());
		super.setBackground(emoteClueItemsPalette.getDefaultColor());
		super.setToolTipText(defaultToolTip);
		super.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if (CycleButton.this.turnedOn)
				{
					CycleButton.this.next(e.getButton() == MouseEvent.BUTTON1);
				}
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				if (CycleButton.this.turnedOn)
				{
					CycleButton.super.setBackground(emoteClueItemsPalette.getHoverColor());
				}
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				CycleButton.super.setBackground(emoteClueItemsPalette.getDefaultColor());
			}
		});

		this.stageCount = 0;

		this.optionLabel = new JLabel();
		this.optionLabel.setHorizontalAlignment(JLabel.CENTER);
		this.optionLabel.setVerticalAlignment(JLabel.CENTER);
		this.optionLabel.setIcon(primary);
		this.minWidth = primary.getIconWidth();
		this.minHeight = primary.getIconHeight();
		final Dimension size = new Dimension(this.minWidth, this.minHeight);
		super.setMinimumSize(size);
		super.setPreferredSize(size);

		this.stageQueue = new ArrayDeque<>();
		this.defaultToolTip = defaultToolTip;
		this.currentStage = new Stage(primary, onSelectPrimary, secondary, onSelectSecondary, defaultToolTip, this.stageCount++);
		this.currentValue = primary;
		onSelectPrimary.run();

		this.turnedOn = true;

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		super.add(this.optionLabel, c);
	}

	/**
	 * Cycle through values until value with specified stage id is reached.
	 *
	 * @param id Value stage returned by {@link #addOption(javax.swing.Icon, Runnable, String)} and {@link #addOption(javax.swing.Icon, Runnable, javax.swing.Icon, Runnable, String)}.
	 */
	public void cycleToStage(final int id)
	{
		final int initialId = this.currentStage.id;
		while (this.currentStage.id != id)
		{
			this.next(true);
			if (this.currentStage.id == initialId)
			{
				break;
			}
		}
	}

	private void next(final Boolean isPrimaryMouseKey)
	{
		final Stage stage;
		final Runnable runnable;
		if (isPrimaryMouseKey || Objects.isNull(this.currentStage.getSecondary()))
		{
			stage = Objects.requireNonNull(this.stageQueue.poll());
			this.stageQueue.add(this.currentStage);
			if (stage == this.currentStage)
			{
				return;
			}
			this.currentStage = stage;
			this.currentValue = stage.getPrimary();
			this.optionLabel.setIcon(stage.getPrimary());
			runnable = stage.getOnSelectPrimary();
		}
		else
		{
			stage = this.currentStage;
			final boolean isPrimaryValue = stage.primary == this.currentValue;
			this.currentValue = isPrimaryValue ? this.currentStage.getSecondary() : this.currentStage.getPrimary();
			this.optionLabel.setIcon(isPrimaryValue ? this.currentStage.getSecondary() : this.currentStage.getPrimary());
			runnable = isPrimaryValue ? stage.getOnSelectSecondary() : stage.getOnSelectPrimary();
		}
		final String toolTip = stage.getToolTip();
		super.setToolTipText(toolTip == null ? this.defaultToolTip : toolTip);
		runnable.run();
	}

	/**
	 * Add a new value to the button cycle.
	 *
	 * @param icon     Icon to display when value is selected.
	 * @param onSelect Runnable to execute when value is selected.
	 * @param toolTip  Tooltip to display when hovering the {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 * @return Returns the stage id of added value, to be used as parameter for {@link #cycleToStage(int)}.
	 */
	public int addOption(final Icon icon, final Runnable onSelect, final String toolTip)
	{
		return this.addOption(icon, onSelect, null, null, toolTip);
	}

	/**
	 * Add a new value to the button cycle.
	 * <p>
	 * When the value is selected, right-clicking the {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} toggles the specified primary value to the secondary, and vice versa.
	 *
	 * @param primary           Icon to display when primary value is selected.
	 * @param onSelectPrimary   Runnable to execute when primary value is selected.
	 * @param secondary         Runnable to execute when secondary value is selected.
	 * @param onSelectSecondary Runnable to execute when secondary value is selected.
	 * @param toolTip           Tooltip to display when hovering the {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 * @return Returns the stage id of added value, to be used as parameter for {@link #cycleToStage(int)}.
	 */
	public int addOption(
			final Icon primary, final Runnable onSelectPrimary, final Icon secondary, final Runnable onSelectSecondary, final String toolTip)
	{
		final int stageId = this.stageCount++;
		this.stageQueue.add(new Stage(primary, onSelectPrimary, secondary, onSelectSecondary, toolTip, stageId));
		this.minWidth = Math.max(this.minWidth, primary.getIconWidth());
		this.minHeight = Math.max(this.minHeight, primary.getIconHeight());
		final Dimension size = new Dimension(this.minWidth, this.minHeight);
		super.setMinimumSize(size);
		super.setPreferredSize(size);
		return stageId;
	}

	/**
	 * Enables cycle function when clicking the {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 * <p>
	 * Enabled by default. Needed to re-enable cycling after executing {@link #turnOff(javax.swing.Icon, String)}.
	 */
	public void turnOn()
	{
		if (!this.turnedOn)
		{
			super.setToolTipText(this.toolTipBeforeDisabled);
			this.toolTipBeforeDisabled = null;
			this.optionLabel.setIcon(this.iconBeforeDisabled);
			this.iconBeforeDisabled = null;
			this.turnedOn = true;
		}
	}

	/**
	 * Disable cycle function when clicking {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 * <p>
	 * When turned off, the {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} will display given icon, and shows given tooltip when the user hovers the button.
	 * <p>
	 * To re-enable the cycle function, execute {@link #turnOn()}.
	 *
	 * @param disabledIcon    The {@link javax.swing.Icon} to display while the button is disabled.
	 * @param disabledToolTip Tooltip to display when hovering the {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton}.
	 */
	public void turnOff(
			@NonNull
			final Icon disabledIcon, final String disabledToolTip)
	{
		if (this.turnedOn)
		{
			this.toolTipBeforeDisabled = super.getToolTipText();
			this.iconBeforeDisabled = this.optionLabel.getIcon();
			this.optionLabel.setIcon(disabledIcon);
			if (Objects.nonNull(disabledToolTip))
			{
				super.setToolTipText(disabledToolTip);
			}
			this.turnedOn = false;
		}
	}

	@RequiredArgsConstructor
	@Getter
	private static final class Stage
	{
		private final Icon primary;
		private final Runnable onSelectPrimary;
		private final Icon secondary;
		private final Runnable onSelectSecondary;
		private final String toolTip;
		private final int id;
	}
}
