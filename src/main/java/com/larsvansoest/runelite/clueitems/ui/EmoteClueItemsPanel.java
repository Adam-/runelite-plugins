package com.larsvansoest.runelite.clueitems.ui;

import com.larsvansoest.runelite.clueitems.data.EmoteClue;
import com.larsvansoest.runelite.clueitems.data.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.data.StashUnit;
import com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemGrid;
import com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemPanel;
import com.larsvansoest.runelite.clueitems.ui.clues.EmoteCluePanel;
import com.larsvansoest.runelite.clueitems.ui.components.*;
import com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitGrid;
import com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Main {@link net.runelite.client.ui.PluginPanel} of the {@link com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin}.
 * <p>
 * Creates a hierarchy of all {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} and {@link com.larsvansoest.runelite.clueitems.data.StashUnit} requirements for completing {@link com.larsvansoest.runelite.clueitems.data.EmoteClue} in Runescape.
 * <p>
 * Provides functionality to change requirement statuses and toggling UI functionality.
 * <p>
 * Consists of a {@link com.larsvansoest.runelite.clueitems.ui.components.TabMenu} with tabs for a {@link com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemGrid} and a {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitGrid}.
 *
 * @author Lars van Soest
 * @since 1.0.4
 */
public class EmoteClueItemsPanel extends PluginPanel
{
	private final Map<EmoteClueItem, EmoteClueItemPanel> itemPanelMap;
	private final Map<StashUnit, StashUnitPanel> stashUnitPanelMap;
	private final Map<EmoteClue, EmoteCluePanel> emoteCluePanelMap;
	private final Map<EmoteClueItem, ArrayList<ItemRequirementCollectionPanel>> emoteClueItemCollectionPanelMap;

	private final Map<EmoteClueItem, ItemSlotPanel> itemSlotPanelMap;

	private final EmoteClueItemGrid clueItemsGrid;
	private final StashUnitGrid STASHUnitGrid;

	/**
	 * Creates the panel.
	 *
	 * @param palette                  Colour scheme for the grid.
	 * @param itemManager              RuneLite's itemManager to derive item icons in {@link com.larsvansoest.runelite.clueitems.ui.components.ItemCollectionPanel}.
	 * @param onStashFillStatusChanged Behaviour to run when the player changes stash unit fill status.
	 * @param pluginName               Plugin name to display in the footer.
	 * @param pluginVersion            Plugin version to display in the footer.
	 * @param gitHubUrl                Hyperlink when clicking the GitHub icon in the footer.
	 */
	public EmoteClueItemsPanel(
			final EmoteClueItemsPalette palette, final ItemManager itemManager, final BiConsumer<StashUnit, Boolean> onStashFillStatusChanged, final String pluginName, final String pluginVersion,
			final String gitHubUrl)
	{
		super();
		super.setLayout(new GridBagLayout());
		super.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.emoteClueItemCollectionPanelMap = new HashMap<>();

		// Create item panels.
		this.itemPanelMap = EmoteClueAssociations.EmoteClueItemToEmoteClues
				.keySet()
				.stream()
				.collect(Collectors.toMap(Function.identity(), emoteClueItem -> new EmoteClueItemPanel(palette, emoteClueItem)));

		// Create an item slot panels for collection logs.
		this.itemSlotPanelMap = EmoteClueAssociations.ItemIdToEmoteClueItem
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, entry -> new ItemSlotPanel(itemManager, entry.getKey(), entry.getValue().getCollectiveName())));

		// Create emote clue panels.
		this.emoteCluePanelMap = EmoteClue.CLUES.stream().collect(Collectors.toMap(Function.identity(), emoteClue -> new EmoteCluePanel(palette, emoteClue)));

		// Create STASHUnit panels.
		this.stashUnitPanelMap = Arrays.stream(StashUnit.values()).collect(Collectors.toMap(Function.identity(), stash -> new StashUnitPanel(palette, stash, onStashFillStatusChanged)));

		// Setup item panels.
		this.itemPanelMap.forEach((emoteClueItem, itemPanel) ->
		{
			final ItemRequirementCollectionPanel collectionPanel = new ItemRequirementCollectionPanel(palette, "Eligible Inventory Items", 6);
			itemPanel.setItemCollectionPanel(collectionPanel);
			this.addEmoteClueItemToCollectionPanel(collectionPanel, emoteClueItem);
			collectionPanel.setStatus(UpdatablePanel.Status.InComplete);
			Arrays.stream(EmoteClueAssociations.EmoteClueItemToEmoteClues.get(emoteClueItem)).map(this.emoteCluePanelMap::get).forEach(itemPanel::addChild);
		});

		// Setup STASHUnit panels.
		this.stashUnitPanelMap.forEach((stashUnit, stashUnitPanel) ->
		{
			final ItemRequirementCollectionPanel collectionPanel = new ItemRequirementCollectionPanel(palette, "Eligible Inventory Items", 6);
			stashUnitPanel.setItemCollectionPanel(collectionPanel, FoldablePanel.DisplayMode.All);
			collectionPanel.setStatus(UpdatablePanel.Status.InComplete);
			for (final EmoteClue emoteClue : EmoteClueAssociations.STASHUnitToEmoteClues.get(stashUnit))
			{
				for (final ItemRequirement itemRequirement : emoteClue.getItemRequirements())
				{
					if (itemRequirement instanceof EmoteClueItem)
					{
						final EmoteClueItem emoteClueItem = (EmoteClueItem) itemRequirement;
						this.addEmoteClueItemToCollectionPanel(collectionPanel, emoteClueItem);
						this.itemPanelMap.get(emoteClueItem).addStashUnitPanel(stashUnitPanel);
					}
				}
				stashUnitPanel.addChild(this.emoteCluePanelMap.get(emoteClue));
			}
		});

		this.clueItemsGrid = new EmoteClueItemGrid(palette);
		this.clueItemsGrid.load(this.itemPanelMap.values());

		this.STASHUnitGrid = new StashUnitGrid(palette);
		this.STASHUnitGrid.load(this.stashUnitPanelMap.values());

		final TabMenu tabMenu = new TabMenu(palette, this.clueItemsGrid, "Items", "Emote Clue Items");
		tabMenu.addTab(this.STASHUnitGrid, "Stashes", "Stash Units", false, 1);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;

		super.add(tabMenu, c);
		c.gridy++;
		super.add(this.clueItemsGrid, c);
		super.add(this.STASHUnitGrid, c);

		c.gridy++;
		c.insets = new Insets(10, 20, 0, 20);
		super.add(new FooterPanel(palette, pluginName, pluginVersion, gitHubUrl), c);
	}

	private void addEmoteClueItemToCollectionPanel(final ItemRequirementCollectionPanel collectionPanel, final EmoteClueItem emoteClueItem)
	{
		collectionPanel.addRequirement(emoteClueItem);
		final ArrayList<ItemRequirementCollectionPanel> currentPanels = this.emoteClueItemCollectionPanelMap.getOrDefault(emoteClueItem, new ArrayList<>());
		currentPanels.add(collectionPanel);
		this.emoteClueItemCollectionPanelMap.put(emoteClueItem, currentPanels);

		final ItemSlotPanel slotPanel = this.itemSlotPanelMap.get(emoteClueItem);
		if (slotPanel != null)
		{
			collectionPanel.addItem(slotPanel);
			return;
		}

		final List<EmoteClueItem> successors = emoteClueItem.getChildren();
		if (successors != null)
		{
			for (final EmoteClueItem successor : successors)
			{
				this.addEmoteClueItemToCollectionPanel(collectionPanel, successor);
			}
		}
	}

	/**
	 * Changes an item sprite to represent given quantity, if a mapping to {@link ItemSlotPanel} exists.
	 *
	 * @param emoteClueItem the {@link net.runelite.client.plugins.cluescrolls.clues.item.SingleItemRequirement} {@link EmoteClueItem} requirement containing the item sprite.
	 * @param quantity      the item quantity the item sprite should show.
	 */
	public void setItemSlotStatus(final EmoteClueItem emoteClueItem, final int quantity)
	{
		final ItemSlotPanel slotPanel = this.itemSlotPanelMap.get(emoteClueItem);
		if (slotPanel != null)
		{
			slotPanel.setQuantity(quantity);
		}
	}

	/**
	 * Changes an {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} requirement status for all {@link com.larsvansoest.runelite.clueitems.ui.components.ItemRequirementCollectionPanel} that contain it and are used by the {@link com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel}.
	 *
	 * @param emoteClueItem the EmoteClueItem requirement to change the status of in all corresponding {@link com.larsvansoest.runelite.clueitems.ui.components.ItemRequirementCollectionPanel}.
	 * @param status        the new status of the EmoteClueItem requirement.
	 */
	public void setCollectionLogStatus(final EmoteClueItem emoteClueItem, final UpdatablePanel.Status status)
	{
		for (final ItemRequirementCollectionPanel itemRequirementCollectionPanel : this.emoteClueItemCollectionPanelMap.get(emoteClueItem))
		{
			itemRequirementCollectionPanel.setRequirementStatus(emoteClueItem, status);
		}
	}

	/**
	 * Changes an {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} {@link com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemPanel} status panel to represent given status, if a mapping to {@link com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemPanel} exists.
	 *
	 * @param emoteClueItem the emote to change the status of in the corresponding {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel}.
	 * @param status        the desired {@link UpdatablePanel.Status} status to display.
	 */
	public void setEmoteClueItemStatus(final EmoteClueItem emoteClueItem, final UpdatablePanel.Status status)
	{
		final EmoteClueItemPanel itemPanel = this.itemPanelMap.get(emoteClueItem);
		if (itemPanel != null)
		{
			itemPanel.setStatus(status);
		}
	}

	/**
	 * Changes a {@link com.larsvansoest.runelite.clueitems.data.StashUnit} {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} status panel to represent given STASHUnit build and fill status, if a mapping to {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} exists.
	 * <p>
	 * Does not check fill status if built = false.
	 *
	 * @param stashUnit the STASHUnit to change the status of in the corresponding {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel}.
	 * @param built     specify if the given STASHUnit has been built.
	 * @param filled    specify if the given STASHUnit has been filled.
	 */
	public void setSTASHUnitStatus(final StashUnit stashUnit, final boolean built, final boolean filled)
	{
		final StashUnitPanel stashUnitPanel = this.stashUnitPanelMap.get(stashUnit);
		if (stashUnitPanel != null)
		{
			if (!built)
			{
				stashUnitPanel.setBuilt(false);
			}
			else
			{
				stashUnitPanel.setBuilt(true);
				stashUnitPanel.setFilled(filled);
			}
		}
	}

	/**
	 * Turn on the stash unit filled button of the {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} of the {@link com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel} that corresponds to given STASHUnit, if a mapping to {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} exists.
	 * <p>
	 * Enabled by default.
	 * <p>
	 * Used to re-enable the stash unit filled button after executing {@link #turnOffSTASHFilledButton(com.larsvansoest.runelite.clueitems.data.StashUnit, javax.swing.Icon, String)}.
	 *
	 * @param stashUnit the STASHUnit of which to turn on the filled button.
	 */
	public void turnOnSTASHFilledButton(final StashUnit stashUnit)
	{
		final StashUnitPanel stashUnitPanel = this.stashUnitPanelMap.get(stashUnit);
		if (stashUnitPanel != null)
		{
			stashUnitPanel.turnOnFilledButton();
		}
	}

	/**
	 * Turn off the stash unit filled button of the {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} of the {@link com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel} that corresponds to given STASHUnit, if a mapping to {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel} exists.
	 * <p>
	 * Can be re-enabled by executing {@link #turnOnSTASHFilledButton(com.larsvansoest.runelite.clueitems.data.StashUnit)}.
	 *
	 * @param stashUnit the STASHUnit of which to turn off the filled button.
	 * @param icon      the icon to display on the button.
	 * @param toolTip   the tooltip to display when hovering the button.
	 */
	public void turnOffSTASHFilledButton(final StashUnit stashUnit, final Icon icon, final String toolTip)
	{
		final StashUnitPanel stashUnitPanel = this.stashUnitPanelMap.get(stashUnit);
		if (stashUnitPanel != null)
		{
			stashUnitPanel.turnOffFilledButton(icon, toolTip);
		}
	}

	/**
	 * Underneath the {@link com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemGrid}'s searchbar, add a notification with given text.
	 * <p>
	 * Overwrites existing notification.
	 * <p>
	 * Notification can be removed by {@link #removeEmoteClueItemGridDisclaimer()}.
	 *
	 * @param text text to display in te notification.
	 */
	public void setEmoteClueItemGridDisclaimer(final String text)
	{
		this.clueItemsGrid.setDisclaimer(text);
	}

	/**
	 * Underneath the {@link com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitGrid}'s searchbar, add a notification with given text.
	 * <p>
	 * Overwrites existing notification.
	 * <p>
	 * Notification can be removed by {@link #removeSTASHUnitGridDisclaimer()}.
	 *
	 * @param text text to display in te notification.
	 */
	public void setSTASHUnitGridDisclaimer(final String text)
	{
		this.STASHUnitGrid.setDisclaimer(text);
	}

	/**
	 * Removes any notification added by {@link #setEmoteClueItemGridDisclaimer(String)}.
	 */
	public void removeEmoteClueItemGridDisclaimer()
	{
		this.clueItemsGrid.removeDisclaimer();
	}

	/**
	 * Removes any notification added by {@link #setSTASHUnitGridDisclaimer(String)}.
	 */
	public void removeSTASHUnitGridDisclaimer()
	{
		this.STASHUnitGrid.removeDisclaimer();
	}
}