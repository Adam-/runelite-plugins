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
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class CycleButton extends JPanel
{
	private final JLabel optionLabel;
	private final Queue<Stage> stageQueue;
	private final String defaultToolTip;
	private Stage currentStage;
	private Icon currentValue;
	private int minWidth;
	private int minHeight;

	CycleButton(
			final EmoteClueItemsPalette emoteClueItemsPalette, final Icon primary, final Runnable onSelectPrimary, final String defaultToolTip)
	{
		this(emoteClueItemsPalette, primary, onSelectPrimary, null, null, defaultToolTip);
	}

	CycleButton(
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
				CycleButton.this.next(e.getButton() == MouseEvent.BUTTON1);
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				CycleButton.super.setBackground(emoteClueItemsPalette.getHoverColor());
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				CycleButton.super.setBackground(emoteClueItemsPalette.getDefaultColor());
			}
		});

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
		this.currentStage = new Stage(primary, onSelectPrimary, secondary, onSelectSecondary, defaultToolTip);
		this.currentValue = primary;
		onSelectPrimary.run();

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		super.add(this.optionLabel, c);
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

	public void addOption(final Icon icon, final Runnable onSelect, final String toolTip)
	{
		this.addOption(icon, onSelect, null, null, toolTip);
	}

	public void addOption(
			final Icon primary, final Runnable onSelectPrimary, final Icon secondary, final Runnable onSelectSecondary, final String toolTip)
	{
		this.stageQueue.add(new Stage(primary, onSelectPrimary, secondary, onSelectSecondary, toolTip));
		this.minWidth = Math.max(this.minWidth, primary.getIconWidth());
		this.minHeight = Math.max(this.minHeight, primary.getIconHeight());
		final Dimension size = new Dimension(this.minWidth, this.minHeight);
		super.setMinimumSize(size);
		super.setPreferredSize(size);
	}

	@RequiredArgsConstructor
	@Getter
	private final class Stage
	{
		private final Icon primary;
		private final Runnable onSelectPrimary;
		private final Icon secondary;
		private final Runnable onSelectSecondary;
		private final String toolTip;
	}
}
