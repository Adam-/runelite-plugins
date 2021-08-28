package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class TabMenu extends JPanel
{
	private final GridBagConstraints c;
	private final EmoteClueItemsPalette palette;
	private JPanel activeButton;
	private JPanel activeContent;

	public TabMenu(final EmoteClueItemsPalette palette, final JPanel defaultContent, final String defaultText, final String defaultToolTipText)
	{
		this(palette, new JShadowedLabel(defaultText), defaultContent, defaultToolTipText);
	}

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
		this.addTab(label, defaultContent, defaultToolTipText, true);
	}

	public void addTab(final JPanel content, final String text, final String toolTipText, final boolean setActive)
	{
		final JLabel label = new JShadowedLabel(text);
		this.addTab(label, content, toolTipText, setActive);
	}

	public void addTab(final JPanel content, final Icon icon, final String toolTipText, final boolean setActive)
	{
		final JLabel label = new JLabel(icon);
		this.addTab(label, content, toolTipText, setActive);
	}

	private void addTab(final JLabel label, final JPanel content, final String toolTipText, final boolean setActive)
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
		super.add(tab, this.c);
		this.c.gridx++;
		if (setActive)
		{
			this.setActive(tab, content);
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
