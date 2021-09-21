package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * Displays a tab menu to control display of {@link javax.swing.JPanel}. Clicking a tab will set a corresponding display visible, while setting the rest of the tab displays as invisible.
 */
public class TabMenu extends JPanel
{
	private final GridBagConstraints c;
	private final EmoteClueItemsPalette palette;
	private JPanel activeButton;
	private JPanel activeContent;

	/**
	 * Creates the tab menu.
	 * <p>
	 * Initialises with one default tab button.
	 *
	 * @param palette            Colour scheme for the tab menu.
	 * @param defaultContent     Default panel to display when tab menu loaded.
	 * @param defaultText        Default tab button text to for default display.
	 * @param defaultToolTipText Tooltip to display when hovering the tab button.
	 */
	public TabMenu(final EmoteClueItemsPalette palette, final JPanel defaultContent, final String defaultText, final String defaultToolTipText)
	{
		this(palette, new JShadowedLabel(defaultText), defaultContent, defaultToolTipText);
	}

	/**
	 * Creates the tab menu.
	 * <p>
	 * Initialises with one default tab button.
	 *
	 * @param palette            Colour scheme for the tab menu.
	 * @param defaultContent     Default panel to display when tab menu loaded.
	 * @param defaultIcon        Default tab button icon for default display.
	 * @param defaultToolTipText Tooltip to display when hovering the tab button.
	 */
	public TabMenu(final EmoteClueItemsPalette palette, final JPanel defaultContent, final Icon defaultIcon, final String defaultToolTipText)
	{
		this(palette, new JLabel(defaultIcon), defaultContent, defaultToolTipText);
	}

	private TabMenu(final EmoteClueItemsPalette palette, final JLabel label, final JPanel defaultContent, final String defaultToolTipText)
	{
		super(new GridBagLayout());
		this.palette = palette;
		this.c = new GridBagConstraints();
		this.c.gridx = 0;
		this.c.gridy = 0;
		this.c.weightx = 1;
		this.c.fill = GridBagConstraints.BOTH;
		this.addTab(label, defaultContent, defaultToolTipText, true, 0);
	}

	/**
	 * Adds a new tab to the {@link com.larsvansoest.runelite.clueitems.ui.components.TabMenu}.
	 *
	 * @param content     panel to display when tab button is selected.
	 * @param text        tab button text for the new panel.
	 * @param toolTipText Tooltip to display when hovering the tab button.
	 * @param setActive   set new tab as active.
	 * @param ipadX       {@link java.awt.GridBagConstraints} ipadX to adjust tab button sizing, and to compensate for string size variations.
	 */
	public void addTab(final JPanel content, final String text, final String toolTipText, final boolean setActive, final int ipadX)
	{
		final JLabel label = new JShadowedLabel(text);
		this.addTab(label, content, toolTipText, setActive, ipadX);
	}

	/**
	 * Adds a new tab to the {@link com.larsvansoest.runelite.clueitems.ui.components.TabMenu}.
	 *
	 * @param content     panel to display when tab button is selected.
	 * @param icon        tab button icon for the new panel.
	 * @param toolTipText Tooltip to display when hovering the tab button.
	 * @param setActive   set new tab as active.
	 * @param ipadX       {@link java.awt.GridBagConstraints} ipadX to adjust tab button sizing, and to compensate for string size variations.
	 */
	public void addTab(final JPanel content, final Icon icon, final String toolTipText, final boolean setActive, final int ipadX)
	{
		final JLabel label = new JLabel(icon);
		this.addTab(label, content, toolTipText, setActive, ipadX);
	}

	private void addTab(final JLabel label, final JPanel content, final String toolTipText, final boolean setActive, final int ipadX)
	{
		label.setHorizontalAlignment(SwingConstants.CENTER);
		final JPanel tab = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		tab.add(label, c);
		tab.setToolTipText(toolTipText);
		tab.setBackground(TabMenu.this.palette.getDefaultColor());
		tab.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				TabMenu.this.setActive(tab, content);
			}

			@Override
			public void mouseEntered(final MouseEvent e)
			{
				tab.setBackground(TabMenu.this.palette.getHoverColor());
				if (tab != TabMenu.this.activeButton)
				{
					TabMenu.this.activeButton.setBackground(TabMenu.this.palette.getDefaultColor());
				}
			}

			@Override
			public void mouseExited(final MouseEvent e)
			{
				tab.setBackground(TabMenu.this.activeButton == tab ? TabMenu.this.palette.getSelectColor() : TabMenu.this.palette.getDefaultColor());
				if (tab != TabMenu.this.activeButton)
				{
					TabMenu.this.activeButton.setBackground(TabMenu.this.palette.getSelectColor());
				}
			}
		});
		this.c.ipadx = ipadX;
		super.add(tab, this.c);
		this.c.gridx++;
		if (setActive)
		{
			this.setActive(tab, content);
		}
		else
		{
			content.setVisible(false);
		}
		super.revalidate();
		super.repaint();
	}

	private void setActive(final JPanel tabButton, final JPanel tabContent)
	{
		if (tabButton != this.activeButton)
		{
			if (Objects.nonNull(this.activeButton))
			{
				this.activeButton.setBackground(this.palette.getDefaultColor());
			}
			tabButton.setBackground(this.palette.getSelectColor());
			this.activeButton = tabButton;
		}
		if (tabContent != this.activeContent)
		{
			if (Objects.nonNull(this.activeContent))
			{
				this.activeContent.setVisible(false);
			}
			tabContent.setVisible(true);
			this.activeContent = tabContent;
		}
	}
}
