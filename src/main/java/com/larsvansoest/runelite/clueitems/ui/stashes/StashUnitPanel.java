package com.larsvansoest.runelite.clueitems.ui.stashes;

import com.larsvansoest.runelite.clueitems.data.*;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import com.larsvansoest.runelite.clueitems.ui.components.*;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Displays data of a {@link com.larsvansoest.runelite.clueitems.data.StashUnit}. Implements {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel}.
 * <p>
 * Contains a button which allows the user to select which {@link com.larsvansoest.runelite.clueitems.data.StashUnit} has been filled by the player.
 *
 * @author Lars van Soest
 * @since 3.0.0
 */
public class StashUnitPanel extends RequirementPanel
{
	private final CycleButton filledButton;
	private final int filledButtonComplete;
	private final int filledButtonInComplete;
	private final EmoteClueItemsPalette palette;
	@Getter
	private final EmoteClueDifficulty[] difficulties;
	@Getter
	private final int quantity;
	private final ImageIcon stashBuiltIcon;
	private final ImageIcon stashNotBuiltIcon;
	private final JLabel stashUnitImage;
	@Getter
	private boolean filled;
	@Getter
	private boolean built;
	private Color headerColorBeforeTurnOff;
	private boolean filledButtonTurnedOn;
	@Getter
	private ItemRequirementCollectionPanel itemCollectionPanel;

	/**
	 * Creates the panel.
	 *
	 * @param palette                  Colour scheme for the grid.
	 * @param stash                    StashUnit of which data is displayed by this panel.
	 * @param onStashFillStatusChanged Behaviour to run when the player changes stash unit fill status.
	 */
	public StashUnitPanel(final EmoteClueItemsPalette palette, final StashUnit stash, final BiConsumer<StashUnit, Boolean> onStashFillStatusChanged)
	{
		super(palette, stash.getName(), 160, 20);
		this.palette = palette;

		this.stashBuiltIcon = this.getBuiltStashIcon(stash.getType());
		this.stashNotBuiltIcon = this.getNotBuiltStashIcon(stash.getType());
		this.stashUnitImage = new JLabel(this.stashNotBuiltIcon);

		this.filled = true;
		final String toolTipTextFormat = "Set stash unit as %s.";
		this.filledButtonTurnedOn = false;
		this.filledButton = new CycleButton(palette, new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.INCOMPLETE_EMPTY), () ->
		{
			if (this.filledButtonTurnedOn)
			{
				onStashFillStatusChanged.accept(stash, false);
				super.setStatus(Status.InComplete);
				this.filled = false;
			}
		}, DataGrid.getToolTipText(toolTipTextFormat, "filled"));
		this.filledButtonInComplete = 0;
		this.filledButtonComplete = this.filledButton.addOption(new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.COMPLETE), () ->
		{
			if (this.filledButtonTurnedOn)
			{
				onStashFillStatusChanged.accept(stash, true);
				super.setStatus(Status.Complete);
				this.filled = true;
			}
		}, DataGrid.getToolTipText(toolTipTextFormat, "empty"));
		this.filledButton.setOpaque(false);

		super.addLeft(this.filledButton, new Insets(0, 1, 0, 0), 10, 10, DisplayMode.All);

		final EmoteClue[] emoteClues = EmoteClueAssociations.STASHUnitToEmoteClues.get(stash);
		this.quantity = emoteClues.length;
		this.difficulties = Arrays.stream(emoteClues).map(EmoteClue::getEmoteClueDifficulty).distinct().toArray(EmoteClueDifficulty[]::new);
		final Insets insets = new Insets(2, 0, 2, 5);
		Arrays.stream(this.difficulties).map(EmoteClueImages::getRibbon).map(ImageIcon::new).map(JLabel::new).forEach(label -> super.addRight(label, insets, 0, 0, DisplayMode.Default));
		super.addRight(new JLabel(String.valueOf(this.quantity)), insets, 0, 0, DisplayMode.Default);
		super.addChild(this.getDetailsPanel(palette, this.difficulties[0]), DisplayMode.All);
	}

	/**
	 * Specify the {@link com.larsvansoest.runelite.clueitems.ui.components.ItemRequirementCollectionPanel} containing all items required to fill the {@link com.larsvansoest.runelite.clueitems.data.StashUnit}.
	 *
	 * @param itemCollectionPanel Item collection panel displaying items required to fill the {@link com.larsvansoest.runelite.clueitems.data.StashUnit}.
	 * @param displayModes        Specify when the panel should be displayed.
	 */
	public void setItemCollectionPanel(final ItemRequirementCollectionPanel itemCollectionPanel, final DisplayMode... displayModes)
	{
		if (Objects.nonNull(this.itemCollectionPanel))
		{
			super.removeChild(itemCollectionPanel);
		}
		this.itemCollectionPanel = itemCollectionPanel;
		super.addChild(itemCollectionPanel, displayModes);
	}

	private ImageIcon getBuiltStashIcon(final StashUnit.Type type)
	{
		final BufferedImage stashUnitImage;
		switch (type)
		{
			case Bush:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.BUSH_BUILT;
				break;
			case Hole:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.HOLE_BUILT;
				break;
			case Rock:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.ROCK_BUILT;
				break;
			default:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.CRATE_BUILT;
				break;
		}
		return new ImageIcon(stashUnitImage);
	}

	private ImageIcon getNotBuiltStashIcon(final StashUnit.Type type)
	{
		final BufferedImage stashUnitImage;
		switch (type)
		{
			case Bush:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.BUSH;
				break;
			case Hole:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.HOLE;
				break;
			case Rock:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.ROCK;
				break;
			default:
				stashUnitImage = EmoteClueImages.Toolbar.StashUnit.CRATE;
				break;
		}
		return new ImageIcon(stashUnitImage);
	}

	private JPanel getDetailsPanel(final EmoteClueItemsPalette palette, final EmoteClueDifficulty difficulty)
	{
		final JPanel detailsPanel = new JPanel(new GridBagLayout());
		detailsPanel.setBackground(palette.getFoldContentColor());
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;

		c.insets.top = 5;
		c.insets.right = 5;
		final StashUnit.DifficultyRequirements difficultyRequirements = StashUnit.DifficultyRequirements.valueOf(difficulty.name());
		c.gridy++;
		detailsPanel.add(new PropertyPanel(palette, "Difficulty", difficulty.name()), c);
		c.gridy++;
		detailsPanel.add(new PropertyPanel(palette, "Construction lvl", String.valueOf(difficultyRequirements.getConstructionLvl())), c);
		c.gridy++;
		c.insets.top = 5;
		detailsPanel.add(new DescriptionPanel(palette, "Build materials", difficultyRequirements.getConstructionItems()), c);

		c.gridy = 0;
		c.gridx = 1;
		c.gridheight = 4;
		detailsPanel.add(this.stashUnitImage, c);

		return detailsPanel;
	}

	/**
	 * Turn off the stash unit filled button.
	 * <p>
	 * Can be re-enabled by executing {@link #turnOnFilledButton()}.
	 *
	 * @param icon    the icon to display on the button.
	 * @param toolTip the tooltip to display when hovering the button.
	 */
	public void turnOffFilledButton(final Icon icon, final String toolTip)
	{
		if (this.filledButtonTurnedOn)
		{
			this.filledButton.turnOff(icon, toolTip);
			this.filledButtonTurnedOn = this.filledButton.isTurnedOn();
			this.headerColorBeforeTurnOff = super.getHeaderColor();
			super.setHeaderColor(this.palette.getFoldHeaderTextColor());
		}
	}

	/**
	 * Turn on the stash unit filled button.
	 * <p>
	 * Enabled by default.
	 * <p>
	 * Used to re-enable the stash unit filled button after executing {@link #turnOffFilledButton(javax.swing.Icon, String)}.
	 */
	public void turnOnFilledButton()
	{
		if (!this.filledButtonTurnedOn)
		{
			this.filledButton.turnOn();
			this.filledButtonTurnedOn = this.filledButton.isTurnedOn();
			super.setHeaderColor(this.headerColorBeforeTurnOff);
			this.headerColorBeforeTurnOff = null;
		}
	}

	/**
	 * Toggle the built status display of the {@link com.larsvansoest.runelite.clueitems.data.StashUnit} the {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} represents.
	 * <p>
	 * <ul>
	 *     <li>When built = false, disables the stash unit fill button and displays a construction icon.</li>
	 *     <li>When built = true, re-enables the stash unit fill button.</li>
	 * </ul>
	 *
	 * @param built display if {@link com.larsvansoest.runelite.clueitems.data.StashUnit} is built or not.
	 */
	public void setBuilt(final boolean built)
	{
		if (built)
		{
			this.turnOnFilledButton();
			this.stashUnitImage.setIcon(this.stashBuiltIcon);
		}
		else
		{
			this.turnOffFilledButton(new ImageIcon(EmoteClueImages.Toolbar.CheckSquare.UNBUILT), "Please build the STASH unit in-game first.");
			this.stashUnitImage.setIcon(this.stashNotBuiltIcon);
		}
		this.built = built;
	}

	/**
	 * Toggle the fill status display of the {@link com.larsvansoest.runelite.clueitems.data.StashUnit} the {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} represents.
	 * <p>
	 * Changes the {@link com.larsvansoest.runelite.clueitems.ui.components.FoldablePanel} header color corresponding to the stash fill (completion) status.
	 *
	 * @param filled display if {@link com.larsvansoest.runelite.clueitems.data.StashUnit} is built or not.
	 */
	public void setFilled(final boolean filled)
	{
		this.filledButton.cycleToStage(filled ? this.filledButtonComplete : this.filledButtonInComplete);
		this.filled = filled;
	}
}