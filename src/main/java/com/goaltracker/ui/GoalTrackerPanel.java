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

import com.google.gson.Gson;
import com.google.inject.Inject;

import java.awt.Component;
import java.awt.Cursor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.Getter;
import com.goaltracker.Goal;
import com.goaltracker.GoalTrackerPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.DragAndDropReorderPane;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageUtil;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GoalTrackerPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;
	private static final ImageIcon IMPORT_ICON;
	private static final ImageIcon IMPORT_HOVER_ICON;
	private static final ImageIcon EXPORT_ICON;
	private static final ImageIcon EXPORT_HOVER_ICON;

	private static final Color DEFAULT_BORDER_COLOR = Color.GREEN;
	private static final Color DEFAULT_FILL_COLOR = new Color(0, 255, 0, 0);

	private static final int DEFAULT_BORDER_THICKNESS = 3;

	private final JLabel addGoal = new JLabel(ADD_ICON);
	private final JLabel importButton = new JLabel(IMPORT_ICON);
	private final JLabel exportButton = new JLabel(EXPORT_ICON);
	private final JLabel title = new JLabel();

	private final IconTextField searchBar = new IconTextField();

	private DragAndDropReorderPane goalListPanel = new DragAndDropReorderPane();

	@Inject
	private GoalTrackerPlugin plugin;

	@Getter
	private Color selectedColor = DEFAULT_BORDER_COLOR;

	@Getter
	private Color selectedFillColor = DEFAULT_FILL_COLOR;

	@Getter
	private int selectedBorderThickness = DEFAULT_BORDER_THICKNESS;

	@Inject
	private Gson gson;

	static
	{
		final BufferedImage addIcon = ImageUtil.getResourceStreamFromClass(GoalTrackerPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));

		final BufferedImage importIcon = ImageUtil.getResourceStreamFromClass(GoalTrackerPlugin.class, "import.png");
		IMPORT_ICON = new ImageIcon(importIcon);
		IMPORT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(importIcon, 0.53f));

		final BufferedImage exportIcon = ImageUtil.getResourceStreamFromClass(GoalTrackerPlugin.class, "export.png");
		EXPORT_ICON = new ImageIcon(exportIcon);
		EXPORT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(exportIcon, 0.53f));
	}

	public void init()
	{
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel northPanel = new JPanel(new BorderLayout(0, 5));

		JPanel actionWrapper = new JPanel(new BorderLayout(8, 0));
		actionWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

		title.setText("Goal Tracker");
		title.setForeground(Color.WHITE);

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.addActionListener(e -> onSearchBarChanged());

		actionWrapper.add(importButton, BorderLayout.WEST);
		actionWrapper.add(exportButton, BorderLayout.CENTER);
		actionWrapper.add(addGoal, BorderLayout.EAST);

		northPanel.add(title, BorderLayout.WEST);
		northPanel.add(actionWrapper, BorderLayout.EAST);
		northPanel.add(searchBar, BorderLayout.SOUTH);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		addGoal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addGoal.setToolTipText("Add new goal");
		addGoal.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				plugin.addGoal();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addGoal.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addGoal.setIcon(ADD_ICON);
			}
		});

		importButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		importButton.setToolTipText("Import goals file...");
		importButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				JFileChooser fc = new JFileChooser();
				fc.setDialogType(JFileChooser.OPEN_DIALOG);
				fc.setDialogTitle("Choose a goals json file to import");
				fc.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
				int returnVal = fc.showOpenDialog(GoalTrackerPanel.this);
				if (returnVal != JFileChooser.APPROVE_OPTION) return;

				File file = fc.getSelectedFile();

				if (plugin.getGoals().size() > 0)
				{
					int confirm = JOptionPane.showConfirmDialog(GoalTrackerPanel.this,
							"Are you sure you want to import this file? This will DELETE all your current goals.",
							"Warning", JOptionPane.OK_CANCEL_OPTION);

					if (confirm != 0) return;
				}

				try (FileInputStream fileStream = new FileInputStream(file);
					InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
					BufferedReader in = new BufferedReader(reader))
				{
					String line = in.readLine();
					StringBuilder json = new StringBuilder();
					while (line != null)
					{
						json.append(line + System.lineSeparator());
						line = in.readLine();
					}
					plugin.getGoals().clear();
					plugin.loadConfig(json.toString()).forEach(plugin.getGoals()::add);
					plugin.updateConfig();
					updateGoals();
				}
				catch (FileNotFoundException ex)
				{
					JOptionPane.showConfirmDialog(GoalTrackerPanel.this,
							"That file doesn't exist!",
							"Error", JOptionPane.DEFAULT_OPTION);
				}
				catch (IOException ex)
				{
					JOptionPane.showConfirmDialog(GoalTrackerPanel.this,
							"Cannot parse file!",
							"Error", JOptionPane.DEFAULT_OPTION);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				importButton.setIcon(IMPORT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				importButton.setIcon(IMPORT_ICON);
			}
		});

		exportButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		exportButton.setToolTipText("Export goals file...");
		exportButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				JFileChooser fc = new JFileChooser();
				fc.setDialogType(JFileChooser.SAVE_DIALOG);
				fc.setDialogTitle("Save your goals to a json file");
				fc.setSelectedFile(new File("goals.json"));
				fc.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
				int returnVal = fc.showOpenDialog(GoalTrackerPanel.this);
				if (returnVal != JFileChooser.APPROVE_OPTION) return;

				File file = fc.getSelectedFile();

				if (file == null) return;
				if (!file.getName().toLowerCase().endsWith(".json"))
				{
					file = new File(file.getParentFile(), file.getName() + ".json");
				}

				try (FileOutputStream fileStream = new FileOutputStream(file);
					OutputStreamWriter writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8))
				{
					final String json = gson.toJson(plugin.getGoals());
					writer.write(json);
				}
				catch (IOException ex)
				{
					JOptionPane.showConfirmDialog(GoalTrackerPanel.this,
							"Cannot write file!",
							"Error", JOptionPane.DEFAULT_OPTION);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				exportButton.setIcon(EXPORT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				exportButton.setIcon(EXPORT_ICON);
			}
		});

		goalListPanel = new DragAndDropReorderPane();
		goalListPanel.addDragListener(draggedComponent -> {
			try
			{
				Component[] components = goalListPanel.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
				for (int i = 0; i < components.length; i++)
				{
					if (components[i] == draggedComponent) {
						GoalPanel goalPanel = (GoalPanel) components[i];
						plugin.reorderGoal(goalPanel.goal, i);
						break;
					}
				}
			}
			catch (Exception ex)
			{
				log.error("The goal panel should only contain GoalPanel instances", ex);
			}
		});
		centerPanel.add(goalListPanel);

		add(northPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);

		updateGoals();
	}

	private void onSearchBarChanged()
	{
		final String text = searchBar.getText();

		updateGoals(text);

		repaint();
		revalidate();
	}

	public void rebuild()
	{
		removeAll();
		repaint();
		revalidate();
		init();
	}

	public void updateGoals()
	{
		updateGoals("");
	}

	public void updateGoals(String text)
	{
		goalListPanel.removeAll();

		for (final Goal goal : plugin.getGoals())
		{
			if (
				goal != null &&
				matchesSearchTerms(goal, text) &&
				!(goal.isCompleted() && plugin.config.hideCompletedGoals())
			) {
				goalListPanel.add(new GoalPanel(plugin, goalListPanel, goal));
			}
		}

		repaint();
		revalidate();
	}

	public boolean matchesSearchTerms(Goal goal, String text)
	{
		if (text.isEmpty())
			return true;

		text = text.toLowerCase();

		Pattern p = Pattern.compile("((?:\\\")([^\\\"]*)(?:\\\")|\\w+)");
		Matcher m = p.matcher(text);

		while (m.find())
		{
			String term = m.group();
			term = term.replaceAll("^\"|\"$", "");
			final String t = term;
			if (t.isEmpty()) continue;
			if (t.equals(Integer.toString(goal.getChunk())))
			{
				return true;
			}
			else if (goal.getName().toLowerCase().contains(t))
			{
				return true;
			}
			else if (goal.getRequirements().stream().anyMatch(str -> str.getName().toLowerCase().contains(t)))
			{
				return true;
			}
		}
		return false;
	}
}
