/*
 * Copyright (c) 2019, Slay to Stay, <https://github.com/slaytostay>
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
package com.goaltracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import com.goaltracker.Goal;
import com.goaltracker.GoalTrackerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.ui.components.MouseDragEventForwarder;
import net.runelite.client.util.ImageUtil;

public class GoalPanel extends JPanel
{
	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createMatteBorder(3, 0, 3, 0, ColorScheme.DARKER_GRAY_COLOR));

	private static final ImageIcon CHECKED_ICON;
	private static final ImageIcon CHECKED_HOVER_ICON;
	private static final ImageIcon CHECKBOX_ICON;
	private static final ImageIcon CHECKBOX_HOVER_ICON;

	private static final ImageIcon EDIT_ICON;
	private static final ImageIcon EDIT_HOVER_ICON;
	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	private final GoalTrackerPlugin plugin;
	public final Goal goal;

	private final JLabel completeLabel = new JLabel();
	private final JLabel deleteLabel = new JLabel();

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel();

	private final JTextField chunkInput = new JTextField("", 20);

	static
	{
		final BufferedImage checkedImg = ImageUtil.getResourceStreamFromClass(GoalTrackerPlugin.class, "checked_icon.png");
		CHECKED_ICON = new ImageIcon(checkedImg);
		CHECKED_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(checkedImg, -100));

		final BufferedImage checkboxImg = ImageUtil.getResourceStreamFromClass(GoalTrackerPlugin.class, "checkbox_icon.png");
		CHECKBOX_ICON = new ImageIcon(checkboxImg);
		CHECKBOX_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(checkboxImg, -100));

		final BufferedImage deleteImg = ImageUtil.getResourceStreamFromClass(GoalTrackerPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));

		final BufferedImage editImg = ImageUtil.getResourceStreamFromClass(GoalTrackerPlugin.class, "edit_icon.png");
		EDIT_ICON = new ImageIcon(editImg);
		EDIT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(editImg, -100));
	}

	GoalPanel(GoalTrackerPlugin plugin, JComponent parentPanel, Goal goal)
	{
		this.plugin = plugin;
		this.goal = goal;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 0, 0, 0));

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

		JPanel nameActions = new JPanel();
		nameActions.setLayout(new BoxLayout(nameActions, BoxLayout.X_AXIS));
		nameActions.setBorder(new EmptyBorder(0, 0, 0, 8));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		save.setVisible(false);
		save.setFont(FontManager.getRunescapeSmallFont());
		save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		save.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				goal.setName(nameInput.getText());
				plugin.updateConfig();

				nameInput.setEditable(false);
				updateNameActions(false);
				requestFocusInWindow();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancel.setVisible(false);
		cancel.setFont(FontManager.getRunescapeSmallFont());
		cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				nameInput.setEditable(false);
				nameInput.setText(goal.getName());
				updateNameActions(false);
				requestFocusInWindow();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		rename.setIcon(EDIT_ICON);
		rename.setToolTipText("Rename goal");
		rename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				nameInput.setEditable(true);
				updateNameActions(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				rename.setIcon(EDIT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				rename.setIcon(EDIT_ICON);
			}
		});

		completeLabel.setToolTipText(goal.isCompleted() ? "Mark as incomplete" : "Mark as completed");
		completeLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				goal.setCompleted(!goal.isCompleted());
				updateCompletion();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				completeLabel.setIcon(goal.isCompleted() ? CHECKED_HOVER_ICON : CHECKBOX_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateCompletion();
			}
		});

		deleteLabel.setIcon(DELETE_ICON);
		deleteLabel.setToolTipText("Delete goal");
		deleteLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				int confirm = 0;

				boolean quickDelete = mouseEvent.isShiftDown() && plugin.config.enableQuickDelete();
				if (!quickDelete) {
					confirm = JOptionPane.showConfirmDialog(GoalPanel.this,
						"Are you sure you want to permanently delete this goal?",
						"Warning", JOptionPane.OK_CANCEL_OPTION);
				}

				if (confirm == 0)
				{
					plugin.deleteGoal(goal);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_ICON);
			}
		});

		cancel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		save.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		rename.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		completeLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		deleteLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		nameActions.add(cancel);
		nameActions.add(save);
		nameActions.add(rename);
		nameActions.add(completeLabel);
		nameActions.add(deleteLabel);

		nameInput.setText(goal.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(0, 24));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(2, 8, 0, 0));

		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);

		JPanel bottomContainer = new JPanel(new GridBagLayout());
		bottomContainer.setBorder(new EmptyBorder(8, 0, 8, 0));
		bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;

		JPanel leftActions = new JPanel(new BorderLayout());
		leftActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel chunkWrapper = new JPanel(new BorderLayout(8, 0));
		chunkWrapper.setPreferredSize(new Dimension(0, 24));
		chunkWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		chunkWrapper.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

		chunkInput.setText(Integer.toString(goal.getChunk()));
		chunkInput.setPreferredSize(new Dimension(0, 24));
		chunkInput.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				int chunk = goal.getChunk();
				try
				{
					chunk = Integer.parseInt(chunkInput.getText());
				}
				catch (Exception ex)
				{
					// ignore
				}
				goal.setChunk(chunk);
				chunkInput.setText(Integer.toString(goal.getChunk()));
			}
		});

		JLabel chunkLabel = new JLabel("Chunk ID:");
		chunkWrapper.add(chunkLabel, BorderLayout.WEST);
		chunkWrapper.add(chunkInput, BorderLayout.CENTER);
		bottomContainer.add(chunkWrapper, gbc);
		gbc.gridy++;
		bottomContainer.add(Box.createRigidArea(new Dimension(0, 8)), gbc);
		gbc.gridy++;

		RequirementsPanel requirementsPanel = new RequirementsPanel(plugin, parentPanel, goal);
		bottomContainer.add(requirementsPanel, gbc);
		gbc.gridy++;

		add(nameWrapper, BorderLayout.NORTH);
		add(bottomContainer, BorderLayout.CENTER);

		updateCompletion();

		// forward mouse drag events to parent panel for drag and drop reordering
		MouseDragEventForwarder mouseDragEventForwarder = new MouseDragEventForwarder(parentPanel);
		addMouseListener(mouseDragEventForwarder);
		addMouseMotionListener(mouseDragEventForwarder);
		nameWrapper.addMouseListener(mouseDragEventForwarder);
		nameWrapper.addMouseMotionListener(mouseDragEventForwarder);
		nameActions.addMouseListener(mouseDragEventForwarder);
		nameActions.addMouseMotionListener(mouseDragEventForwarder);
		nameInput.getTextField().addMouseListener(mouseDragEventForwarder);
		nameInput.getTextField().addMouseMotionListener(mouseDragEventForwarder);
		bottomContainer.addMouseListener(mouseDragEventForwarder);
		bottomContainer.addMouseMotionListener(mouseDragEventForwarder);
		leftActions.addMouseListener(mouseDragEventForwarder);
		leftActions.addMouseMotionListener(mouseDragEventForwarder);
		rightActions.addMouseListener(mouseDragEventForwarder);
		rightActions.addMouseMotionListener(mouseDragEventForwarder);
		rename.addMouseListener(mouseDragEventForwarder);
		rename.addMouseMotionListener(mouseDragEventForwarder);
		completeLabel.addMouseListener(mouseDragEventForwarder);
		completeLabel.addMouseMotionListener(mouseDragEventForwarder);
		deleteLabel.addMouseListener(mouseDragEventForwarder);
		deleteLabel.addMouseMotionListener(mouseDragEventForwarder);
		chunkWrapper.addMouseListener(mouseDragEventForwarder);
		chunkWrapper.addMouseMotionListener(mouseDragEventForwarder);
		chunkInput.addMouseListener(mouseDragEventForwarder);
		chunkInput.addMouseMotionListener(mouseDragEventForwarder);
		chunkLabel.addMouseListener(mouseDragEventForwarder);
		chunkLabel.addMouseMotionListener(mouseDragEventForwarder);
		requirementsPanel.addMouseListener(mouseDragEventForwarder);
		requirementsPanel.addMouseMotionListener(mouseDragEventForwarder);
	}

	private void updateNameActions(boolean saveAndCancel)
	{
		save.setVisible(saveAndCancel);
		cancel.setVisible(saveAndCancel);
		rename.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
		}
	}

	private void updateCompletion()
	{
		completeLabel.setToolTipText(goal.isCompleted() ? "Mark as incomplete" : "Mark as completed");
		completeLabel.setIcon(goal.isCompleted() ? CHECKED_ICON : CHECKBOX_ICON);
		plugin.updateConfig();
	}
}
