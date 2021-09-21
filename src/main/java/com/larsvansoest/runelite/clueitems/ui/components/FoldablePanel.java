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

import com.larsvansoest.runelite.clueitems.data.EmoteClueImages;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Panel with fold and unfold functionality to allow collapsing and un-collapsing a details panel.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class FoldablePanel extends JPanel
{
	private final EmoteClueItemsPalette emoteClueItemsPalette;
	private final JLabel foldIcon;
	private final JShadowedLabel statusHeaderName;
	private final JPanel foldContentDisplay;
	private final JPanel header;
	private final HashMap<DisplayMode, ArrayList<HeaderElement>> leftHeaderElements;
	private final HashMap<DisplayMode, ArrayList<HeaderElement>> rightHeaderElements;
	private final HashMap<DisplayMode, ArrayList<FoldablePanel>> foldContentPanels;
	private final HashMap<DisplayMode, ArrayList<JComponent>> foldContent;
	@Getter
	private DisplayMode displayMode;
	@Setter
	@Getter
	private int foldContentLeftInset;
	@Setter
	@Getter
	private int foldContentRightInset;
	@Setter
	@Getter
	private int foldContentBottomInset;
	@Setter
	@Getter
	private Integer fixedFoldContentTopInset;
	@Setter
	@Getter
	private Runnable onHeaderMousePressed;
	@Getter
	private Boolean expanded;

	/**
	 * Creates the panel.
	 *
	 * @param emoteClueItemsPalette Colour scheme for the panel.
	 * @param name                  Name displayed as the panel header text.
	 * @param headerNameWidth       Fixed panel width to contain the panel header text.
	 * @param headerMinHeight       Minimum panel header height.
	 */
	public FoldablePanel(final EmoteClueItemsPalette emoteClueItemsPalette, final String name, final int headerNameWidth, final int headerMinHeight)
	{
		super.setLayout(new GridBagLayout());
		super.setBackground(emoteClueItemsPalette.getDefaultColor());
		super.setName(name);

		this.expanded = false;
		this.displayMode = DisplayMode.Default;

		this.emoteClueItemsPalette = emoteClueItemsPalette;
		this.foldContentDisplay = new JPanel(new GridBagLayout());

		this.foldContentDisplay.setBackground(emoteClueItemsPalette.getFoldContentColor());

		this.leftHeaderElements = this.newDisplayModeMap();
		this.rightHeaderElements = this.newDisplayModeMap();
		this.foldContentPanels = this.newDisplayModeMap();
		this.foldContent = this.newDisplayModeMap();

		this.foldIcon = new JLabel(FOLD_ICONS.LEFT);
		this.statusHeaderName = this.getHeaderText(name, headerNameWidth, headerMinHeight);
		this.header = this.getHeader();
		this.paintHeaderLabels();

		this.onHeaderMousePressed = () ->
		{
			if (this.expanded)
			{
				this.fold();
			}
			else
			{
				this.unfold();
			}
		};
		this.foldContentLeftInset = 5;
		this.foldContentRightInset = 5;
		this.foldContentBottomInset = 5;
		this.fixedFoldContentTopInset = null;

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		super.add(this.header, c);

		c.gridy++;
		super.add(this.foldContentDisplay, c);
	}

	private JPanel getHeader()
	{
		final JPanel header = new JPanel(new GridBagLayout());
		header.setBackground(this.emoteClueItemsPalette.getDefaultColor());
		header.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				FoldablePanel.this.onHeaderMousePressed.run();
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				header.setBackground(FoldablePanel.this.emoteClueItemsPalette.getHoverColor());
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				header.setBackground(FoldablePanel.this.expanded ? FoldablePanel.this.emoteClueItemsPalette.getSelectColor() : FoldablePanel.this.emoteClueItemsPalette.getDefaultColor());
			}
		});

		return header;
	}

	/**
	 * Adds a {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} sub-panel to the details.
	 *
	 * @param child        the sub-panel to display when unfolding.
	 * @param displayModes specify in which display modes the sub-panel should be displayed.
	 */
	public void addChild(final FoldablePanel child, final DisplayMode... displayModes)
	{
		this.addDisplayModeComponents(this.foldContentPanels, child, displayModes);
	}

	/**
	 * Adds a {@link javax.swing.JComponent} element to the details.
	 *
	 * @param child        the element to display when unfolding.
	 * @param displayModes specify in which display modes the element should be displayed.
	 */
	public void addChild(final JComponent child, final DisplayMode... displayModes)
	{
		this.addDisplayModeComponents(this.foldContent, child, displayModes);
	}

	/**
	 * Removes a {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} sub-panel from the details.
	 *
	 * @param child        the sub-panel to remove.
	 * @param displayModes specify from which display modes the sub-panel should be removed.
	 */
	public void removeChild(final FoldablePanel child, final DisplayMode... displayModes)
	{
		this.removeDisplayModeComponents(this.foldContentPanels, child, displayModes);
	}

	/**
	 * Removes a {@link javax.swing.JComponent} element from the details.
	 *
	 * @param child        the element to remove.
	 * @param displayModes specify from which display modes the element should be removed.
	 */
	public void removeChild(final JComponent child, final DisplayMode... displayModes)
	{
		this.removeDisplayModeComponents(this.foldContent, child, displayModes);
	}

	/**
	 * Collapses the details view.
	 */
	public void fold()
	{
		this.getDisplayModeComponents(this.foldContentPanels).forEach(FoldablePanel::fold);
		this.foldContentDisplay.removeAll();
		this.foldContentDisplay.setVisible(false);
		this.foldContentDisplay.revalidate();
		this.foldContentDisplay.repaint();
		this.header.setBackground(this.emoteClueItemsPalette.getDefaultColor());
		this.expanded = false;
		this.foldIcon.setIcon(FOLD_ICONS.LEFT);
	}

	/**
	 * Uncollapses the details view.
	 */
	public void unfold()
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.insets = new Insets(0, this.foldContentLeftInset, this.foldContentBottomInset, this.foldContentRightInset);
		c.gridx = 0;
		c.gridy = 0;
		this.getDisplayModeComponents(this.foldContent).forEach(foldContentElement ->
		{
			c.insets.top = Objects.nonNull(this.fixedFoldContentTopInset) ? this.fixedFoldContentTopInset : c.gridy == 0 ? 5 : 0;
			this.foldContentDisplay.add(foldContentElement, c);
			c.gridy++;
		});
		this.getDisplayModeComponents(this.foldContentPanels).forEach(foldablePanel ->
		{
			c.insets.top = Objects.nonNull(this.fixedFoldContentTopInset) ? this.fixedFoldContentTopInset : c.gridy == 0 ? 5 : 0;
			foldablePanel.setDisplayMode(DisplayMode.Nested);
			this.foldContentDisplay.add(foldablePanel, c);
			c.gridy++;
		});
		this.header.setBackground(this.emoteClueItemsPalette.getSelectColor());
		this.foldContentDisplay.setVisible(true);
		this.foldIcon.setIcon(FOLD_ICONS.DOWN);
		this.expanded = true;
		this.foldContentDisplay.revalidate();
		this.foldContentDisplay.repaint();
	}

	/**
	 * Returns the color of the header text.
	 *
	 * @return the color.
	 */
	public Color getHeaderColor()
	{
		return this.statusHeaderName.getForeground();
	}

	/**
	 * Sets the color of the header text.
	 *
	 * @param colour the new color of the header text.
	 */
	public void setHeaderColor(final Color colour)
	{
		this.statusHeaderName.setForeground(colour);
	}

	/**
	 * Changes the display mode of the foldable panel, changing which elements, header icons and sub-panels should be displayed.
	 *
	 * @param displayMode the new display mode.
	 */
	public void setDisplayMode(final DisplayMode displayMode)
	{
		this.displayMode = displayMode;
		this.paintHeaderLabels();
	}

	private <T> HashMap<DisplayMode, ArrayList<T>> newDisplayModeMap()
	{
		final HashMap<DisplayMode, ArrayList<T>> displayModeMap = new HashMap<>();
		for (final DisplayMode displayMode : DisplayMode.values())
		{
			if (!displayMode.equals(DisplayMode.All))
			{
				displayModeMap.put(displayMode, new ArrayList<>());
			}
		}
		return displayModeMap;
	}

	private <T> void addDisplayModeComponents(final HashMap<DisplayMode, ArrayList<T>> map, final T component, final DisplayMode... displayModes)
	{
		if (displayModes.length == 0)
		{
			this.addDisplayModeComponent(map, component, DisplayMode.Default);
		}
		else if (Arrays.asList(displayModes).contains(DisplayMode.All))
		{
			for (final DisplayMode displayMode : DisplayMode.values())
			{
				if (!displayMode.equals(DisplayMode.All))
				{
					this.addDisplayModeComponent(map, component, displayMode);
				}
			}
		}
		else
		{
			for (final DisplayMode displayMode : displayModes)
			{
				this.addDisplayModeComponent(map, component, displayMode);
			}
		}
	}

	private <T> void removeDisplayModeComponents(final HashMap<DisplayMode, ArrayList<T>> map, final T component, final DisplayMode... displayModes)
	{
		if (displayModes.length == 0 || Arrays.asList(displayModes).contains(DisplayMode.All))
		{
			for (final DisplayMode displayMode : DisplayMode.values())
			{
				if (!displayMode.equals(DisplayMode.All))
				{
					this.removeDisplayModeComponent(map, component, displayMode);
				}
			}
		}
		else
		{
			for (final DisplayMode displayMode : displayModes)
			{
				this.removeDisplayModeComponent(map, component, displayMode);
			}
		}
	}

	private <T> void removeDisplayModeComponent(final HashMap<DisplayMode, ArrayList<T>> map, final T component, final DisplayMode displayMode)
	{
		final ArrayList<T> list = map.get(displayMode);
		list.remove(component);
		map.put(displayMode, list);
	}

	private <T> void addDisplayModeComponent(final HashMap<DisplayMode, ArrayList<T>> map, final T component, final DisplayMode displayMode)
	{
		final ArrayList<T> list = map.get(displayMode);
		list.add(component);
		map.put(displayMode, list);
	}

	private <T> Stream<T> getDisplayModeComponents(final HashMap<DisplayMode, ArrayList<T>> map)
	{
		if (this.displayMode.equals(DisplayMode.All))
		{
			return Arrays.stream(DisplayMode.values()).filter(displayMode -> !displayMode.equals(DisplayMode.All)).map(map::get).flatMap(ArrayList::stream).distinct();
		}
		return map.get(this.displayMode).stream();
	}

	/**
	 * Adds a {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} to the right side of the header text.
	 *
	 * @param cycleButton  the button to add to the header.
	 * @param insets       the {@link java.awt.GridBagConstraints} insets to adjust placement on the header.
	 * @param ipadX        the {@link java.awt.GridBagConstraints} ipadX to adjust placement on the header.
	 * @param ipadY        the {@link java.awt.GridBagConstraints} ipadY to adjust placement on the header.
	 * @param displayModes specify in which display modes the sub-panel should be displayed.
	 */
	public final void addRight(final CycleButton cycleButton, final Insets insets, final int ipadX, final int ipadY, final DisplayMode... displayModes)
	{
		this.addDisplayModeComponents(this.rightHeaderElements, new HeaderElement(cycleButton, insets, ipadX, ipadY), displayModes);
		this.paintHeaderLabels();
	}

	/**
	 * Adds a {@link JLabel} to the right side of the header text.
	 *
	 * @param iconLabel    the button to add to the header.
	 * @param insets       the {@link java.awt.GridBagConstraints} insets to adjust placement on the header.
	 * @param ipadX        the {@link java.awt.GridBagConstraints} ipadX to adjust placement on the header.
	 * @param ipadY        the {@link java.awt.GridBagConstraints} ipadY to adjust placement on the header.
	 * @param displayModes specify in which display modes the sub-panel should be displayed.
	 */
	public final void addRight(final JLabel iconLabel, final Insets insets, final int ipadX, final int ipadY, final DisplayMode... displayModes)
	{
		this.addDisplayModeComponents(this.rightHeaderElements, new HeaderElement(iconLabel, insets, ipadX, ipadY), displayModes);
		this.paintHeaderLabels();
	}

	/**
	 * Adds a {@link com.larsvansoest.runelite.clueitems.ui.components.CycleButton} to the left side of the header text.
	 *
	 * @param cycleButton  the button to add to the header.
	 * @param insets       the {@link java.awt.GridBagConstraints} insets to adjust placement on the header.
	 * @param ipadX        the {@link java.awt.GridBagConstraints} ipadX to adjust placement on the header.
	 * @param ipadY        the {@link java.awt.GridBagConstraints} ipadY to adjust placement on the header.
	 * @param displayModes specify in which display modes the sub-panel should be displayed.
	 */
	public final void addLeft(final CycleButton cycleButton, final Insets insets, final int ipadX, final int ipadY, final DisplayMode... displayModes)
	{
		this.addDisplayModeComponents(this.leftHeaderElements, new HeaderElement(cycleButton, insets, ipadX, ipadY), displayModes);
		this.paintHeaderLabels();
	}

	/**
	 * Adds a {@link JLabel} to the left side of the header text.
	 *
	 * @param iconLabel    the button to add to the header.
	 * @param insets       the {@link java.awt.GridBagConstraints} insets to adjust placement on the header.
	 * @param ipadX        the {@link java.awt.GridBagConstraints} ipadX to adjust placement on the header.
	 * @param ipadY        the {@link java.awt.GridBagConstraints} ipadY to adjust placement on the header.
	 * @param displayModes specify in which display modes the sub-panel should be displayed.
	 */
	public final void addLeft(final JLabel iconLabel, final Insets insets, final int ipadX, final int ipadY, final DisplayMode... displayModes)
	{
		this.addDisplayModeComponents(this.leftHeaderElements, new HeaderElement(iconLabel, insets, ipadX, ipadY), displayModes);
		this.paintHeaderLabels();
	}

	private void paintHeaderLabels()
	{
		this.header.removeAll();
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;

		// Add left icons & buttons
		this.addHeaderElements(this.getDisplayModeComponents(this.leftHeaderElements), c);

		this.header.add(this.statusHeaderName, c);
		c.weightx = 1;

		c.gridx++;
		this.header.add(new JLabel(), c);
		c.weightx = 0;

		c.gridx++;
		this.addHeaderElements(this.getDisplayModeComponents(this.rightHeaderElements), c);

		c.insets.right = 4;
		this.header.add(this.foldIcon, c);
		super.revalidate();
		super.repaint();
	}

	private void addHeaderElements(final Stream<HeaderElement> headerElements, final GridBagConstraints c)
	{
		final Insets previousInsets = c.insets;
		final int previousIpadX = c.ipadx;
		final int previousIpadY = c.ipady;
		headerElements.forEach(headerElement ->
		{
			c.insets = headerElement.getInsets();
			c.ipadx = headerElement.getIpadX();
			c.ipady = headerElement.getIpadY();
			this.header.add(headerElement.getElement(), c);
			c.gridx++;
		});
		c.insets = previousInsets;
		c.ipadx = previousIpadX;
		c.ipady = previousIpadY;
	}

	private JShadowedLabel getHeaderText(final String text, final int fixedNameWidth, final int fixedHeight)
	{
		final JShadowedLabel label = new JShadowedLabel(text);
		final Dimension size = new Dimension(fixedNameWidth, fixedHeight);
		label.setMinimumSize(size);
		label.setPreferredSize(size);
		label.setMaximumSize(size);

		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}

	/**
	 * DisplayModes to distinguish different display environments.
	 * <p>
	 * Specifying a display mode allows for choosing which previously added elements, header icons and sub-panels should be displayed on the panel.
	 */
	public enum DisplayMode
	{
		All(),
		Default(),
		Nested()
	}

	private final static class FOLD_ICONS
	{
		static final ImageIcon DOWN = new ImageIcon(EmoteClueImages.Toolbar.Chevron.DOWN);
		static final ImageIcon LEFT = new ImageIcon(EmoteClueImages.Toolbar.Chevron.LEFT);
	}

	@Getter
	@RequiredArgsConstructor
	private static class HeaderElement
	{
		private final JComponent element;
		private final Insets insets;
		private final int ipadX;
		private final int ipadY;
	}
}
