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

import static com.goaltracker.ui.GoalTrackerPanel.BLOCKED_HOVER_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.BLOCKED_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.CHECKBOX_HOVER_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.CHECKBOX_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.CHECKED_HOVER_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.CHECKED_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.DELETE_HOVER_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.DELETE_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.EDIT_HOVER_ICON;
import static com.goaltracker.ui.GoalTrackerPanel.EDIT_ICON;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

public class GoalPanel extends JPanel
{
	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createMatteBorder(3, 0, 3, 0, ColorScheme.DARKER_GRAY_COLOR));

	private final GoalTrackerPlugin plugin;
	public final Goal goal;

	private final JLabel completionCheckbox = new JLabel();
	private final JLabel deleteButton = new JLabel();

	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel saveRenameButton = new JLabel("Save");
	private final JLabel cancelRenameButton = new JLabel("Cancel");
	private final JLabel renameButton = new JLabel();

	private final JTextField chunkInput = new JTextField("", 20);

	private final MouseAdapter nameInputMouseAdapter;

	GoalPanel(GoalTrackerPlugin plugin, JComponent parentPanel, Goal goal)
	{
		this.plugin = plugin;
		this.goal = goal;

		nameInputMouseAdapter = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				goal.cycleState();
				// Cycle backwards if shift is held, which happens to be equivalent to cycling forwards twice
				if (e.isShiftDown())
					goal.cycleState();
				updateCompletion();
			}
		};

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 0, 0, 0));

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

		JPanel nameActions = new JPanel();
		nameActions.setLayout(new BoxLayout(nameActions, BoxLayout.X_AXIS));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		saveRenameButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		saveRenameButton.setVisible(false);
		saveRenameButton.setFont(FontManager.getRunescapeSmallFont());
		saveRenameButton.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		saveRenameButton.addMouseListener(new MouseAdapter()
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
				saveRenameButton.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				saveRenameButton.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}
		});

		cancelRenameButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cancelRenameButton.setVisible(false);
		cancelRenameButton.setFont(FontManager.getRunescapeSmallFont());
		cancelRenameButton.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancelRenameButton.addMouseListener(new MouseAdapter()
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
				cancelRenameButton.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancelRenameButton.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		renameButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		renameButton.setIcon(EDIT_ICON);
		renameButton.setToolTipText("Rename goal");
		renameButton.addMouseListener(new MouseAdapter()
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
				renameButton.setIcon(EDIT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				renameButton.setIcon(EDIT_ICON);
			}
		});

		completionCheckbox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		completionCheckbox.setToolTipText(goal.isCompleted() ? "Mark as incomplete" : "Mark as completed");
		completionCheckbox.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				goal.cycleState();
				// Cycle backwards if shift is held, which happens to be equivalent to cycling forwards twice
				if (e.isShiftDown())
					goal.cycleState();
				updateCompletion();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (goal.isBlocked())
				{
					completionCheckbox.setIcon(BLOCKED_HOVER_ICON);
					completionCheckbox.setToolTipText("Unblock");
				}
				else
				{
					completionCheckbox.setIcon(goal.isCompleted() ? CHECKED_HOVER_ICON : CHECKBOX_HOVER_ICON);
					completionCheckbox.setToolTipText(goal.isCompleted() ? "Mark as incomplete" : "Mark as completed");
				}
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				updateCompletion();
			}
		});
		completionCheckbox.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				{
					completionCheckbox.setToolTipText(goal.isBlocked() ? "Mark as blocked" : "Unblock");
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_SHIFT)
				{
					completionCheckbox.setToolTipText(goal.isCompleted() ? "Mark as incomplete" : "Mark as completed");
				}
			}
		});

		deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		deleteButton.setIcon(DELETE_ICON);
		deleteButton.setToolTipText("Delete goal");
		deleteButton.addMouseListener(new MouseAdapter()
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
				deleteButton.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteButton.setIcon(DELETE_ICON);
			}
		});

		cancelRenameButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		saveRenameButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		renameButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		deleteButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
		nameActions.add(cancelRenameButton);
		nameActions.add(saveRenameButton);
		nameActions.add(renameButton);
		nameActions.add(deleteButton);

		completionCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 4));

		nameInput.setText(goal.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(0, 24));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(2, 0, 0, 0));
		nameInput.getTextField().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		nameInput.getTextField().addMouseListener(nameInputMouseAdapter);

		nameWrapper.add(completionCheckbox, BorderLayout.WEST);
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
		renameButton.addMouseListener(mouseDragEventForwarder);
		renameButton.addMouseMotionListener(mouseDragEventForwarder);
		completionCheckbox.addMouseListener(mouseDragEventForwarder);
		completionCheckbox.addMouseMotionListener(mouseDragEventForwarder);
		deleteButton.addMouseListener(mouseDragEventForwarder);
		deleteButton.addMouseMotionListener(mouseDragEventForwarder);
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
		saveRenameButton.setVisible(saveAndCancel);
		cancelRenameButton.setVisible(saveAndCancel);
		renameButton.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
			nameInput.getTextField().removeMouseListener(nameInputMouseAdapter);
			nameInput.getTextField().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}
		else
		{
			nameInput.getTextField().addMouseListener(nameInputMouseAdapter);
			nameInput.getTextField().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	private void updateCompletion()
	{
		nameInput.getTextField().setForeground(goal.isCompleted() ?
			plugin.config.completedColor() :
			(goal.isBlocked() ? plugin.config.blockedColor() : plugin.config.inProgressColor()));
		completionCheckbox.setToolTipText(
			goal.isBlocked() ? "Unblock" :
			goal.isCompleted() ? "Mark as blocked" : "Mark as completed");
		completionCheckbox.setIcon(
			goal.isBlocked() ? BLOCKED_ICON :
			goal.isCompleted() ? CHECKED_ICON : CHECKBOX_ICON);
		plugin.updateConfig();
	}
}
