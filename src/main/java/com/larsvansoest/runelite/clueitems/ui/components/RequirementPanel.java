package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import lombok.Getter;

/**
 * A requirement progression visualisation {@link javax.swing.JPanel} extension with changeable {@link Status} representation.
 *
 * @author Lars van Soest
 * @see FoldablePanel
 * @since 2.0.0
 */
public class RequirementPanel extends FoldablePanel implements UpdatablePanel
{
	@Getter
	private Status status;

	/**
	 * Creates the panel.
	 *
	 * @param emoteClueItemsPalette Colour scheme for the panel.
	 * @param name                  Name displayed as the panel header text.
	 * @param headerNameWidth       Fixed panel width to contain the panel header text.
	 * @param headerMinHeight       Minimum panel header height.
	 */
	public RequirementPanel(final EmoteClueItemsPalette emoteClueItemsPalette, final String name, final int headerNameWidth, final int headerMinHeight)
	{
		super(emoteClueItemsPalette, name, headerNameWidth, headerMinHeight);
	}

	/**
	 * Change the status of the {@link com.larsvansoest.runelite.clueitems.data.EmoteClueImages.Toolbar.Requirement}.
	 * <p>
	 * Changes the header text color the color corresponding with given {@link com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel.Status}.
	 *
	 * @param status the new status of the panel.
	 */
	public void setStatus(final Status status)
	{
		super.setHeaderColor(status.colour);
		this.status = status;
	}
}