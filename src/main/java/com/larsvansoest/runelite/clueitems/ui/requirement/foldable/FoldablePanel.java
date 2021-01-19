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

package com.larsvansoest.runelite.clueitems.ui.requirement.foldable;

import com.larsvansoest.runelite.clueitems.ui.requirement.RequirementStatus;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanelPalette;
import com.larsvansoest.runelite.clueitems.ui.requirement.UpdatablePanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class FoldablePanel extends UpdatablePanel
{
	private final JPanel foldContent;
	private final LinkedList<UpdatablePanel> foldContentElements;
	private final LinkedList<FoldablePanel> foldContentFoldablePanels;
	private final GridBagConstraints foldConstraints;
	private final FoldableHeader foldableHeader;
	private Boolean expanded;

	public FoldablePanel(EmoteClueItemsPanelPalette emoteClueItemsPanelPalette, String name) {
		super.setLayout(new GridBagLayout());
		super.setBackground(emoteClueItemsPanelPalette.getDefaultColor());
		super.setName(name);
		this.foldContent = new JPanel(new GridBagLayout());

		this.foldContent.setBackground(emoteClueItemsPanelPalette.getFoldContentColor());
		this.foldConstraints = new GridBagConstraints();
		this.foldConstraints.fill = GridBagConstraints.BOTH;
		this.foldConstraints.weightx = 1;
		this.foldConstraints.insets = new Insets(0, 5, 5, 5);

		this.foldContentElements = new LinkedList<>();
		this.foldContentFoldablePanels = new LinkedList<>();
		this.foldableHeader = new FoldableHeader(this, emoteClueItemsPanelPalette, new Dimension(140, 20), name);
		this.expanded = false;

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		super.add(this.foldableHeader, c);

		c.gridy++;
		super.add(this.foldContent, c);
	}

	public void addChild(FoldablePanel child) {
		this.foldContentElements.add(child);
		this.foldContentFoldablePanels.add((child));
	}

	public void addChild(UpdatablePanel child) {
		this.foldContentElements.add(child);
	}

	public void fold() {
		this.foldContentFoldablePanels.forEach(FoldablePanel::fold);
		this.foldContent.setVisible(false);
		this.foldableHeader.fold();
		this.foldContentElements.forEach(this.foldContent::remove);
		this.expanded = false;
		super.revalidate();
		super.repaint();
	}

	public void unfold() {
		this.foldConstraints.gridy = 0;
		this.foldableHeader.unfold();

		for(int i = 0; i < this.foldContentElements.size(); i++) {
			this.foldConstraints.insets.top	= i == 0 ? 5 : 0;
			this.foldContent.add(this.foldContentElements.get(i), this.foldConstraints);
			this.foldConstraints.gridy++;
		}

		this.foldContent.setVisible(true);
		this.expanded = true;
		super.revalidate();
		super.repaint();
	}

	public void setStatus(RequirementStatus requirementStatus)
	{
		this.foldableHeader.getNameLabel().setForeground(requirementStatus.colour);
	}

	public JPanel getFoldContent()
	{
		return this.foldContent;
	}

	public LinkedList<UpdatablePanel> getFoldContentElements()
	{
		return this.foldContentElements;
	}

	public LinkedList<FoldablePanel> getFoldContentFoldablePanels()
	{
		return this.foldContentFoldablePanels;
	}

	public Boolean isExpanded()
	{
		return this.expanded;
	}

	public FoldableHeader getFoldableHeader()
	{
		return this.foldableHeader;
	}

	public GridBagConstraints getFoldConstraints()
	{
		return this.foldConstraints;
	}

	public abstract void onHeaderMousePressed();

	public final void addRightIcon(JComponent iconLabel) {
		this.foldableHeader.addRightIcon(iconLabel);
	}

	public final void addLeftIcon(JLabel iconLabel) {
		this.foldableHeader.addLeftIcon(iconLabel);
	}
}
