package com.larsvansoest.runelite.clueitems.ui;

import com.larsvansoest.runelite.clueitems.data.EmoteClue;
import com.larsvansoest.runelite.clueitems.data.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemPanel;
import com.larsvansoest.runelite.clueitems.ui.clues.EmoteClueItemsGrid;
import com.larsvansoest.runelite.clueitems.ui.clues.EmoteCluePanel;
import com.larsvansoest.runelite.clueitems.ui.components.*;
import com.larsvansoest.runelite.clueitems.ui.stashes.StashUnitPanel;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EmoteClueItemsPanel extends PluginPanel
{
	private final Map<EmoteClueItem, EmoteClueItemPanel> emoteClueItemPanelMap;
	private final Map<EmoteClueItem, ItemSlotPanel> slotPanelMap;
	private final EmoteClueItemsGrid clueItemsGrid;

	public EmoteClueItemsPanel(final EmoteClueItemsPalette palette, final ItemManager itemManager, final String pluginName, final String pluginVersion, final String gitHubUrl)
	{
		super();
		super.setLayout(new GridBagLayout());
		super.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// Create parent EmoteClueItem panels.
		this.emoteClueItemPanelMap = EmoteClueAssociations.EmoteClueItemToEmoteClues
				.keySet()
				.stream()
				.collect(Collectors.toMap(Function.identity(), emoteClueItem -> new EmoteClueItemPanel(palette, emoteClueItem)));

		// Create an item panel for all required items.
		this.slotPanelMap = EmoteClueAssociations.ItemIdToEmoteClueItemSlot
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, entry -> new ItemSlotPanel(itemManager, entry.getKey(), entry.getValue().getCollectiveName())));

		// Create EmoteClueItem-EmoteClue (*-1) sub-panels.
		final Map<EmoteClue, EmoteCluePanel> emoteCluePanelMap = EmoteClue.CLUES.stream().collect(Collectors.toMap(Function.identity(), emoteClue -> new EmoteCluePanel(palette, emoteClue)));

		this.emoteClueItemPanelMap.forEach((emoteClueItem, itemPanel) ->
		{
			// Add item collection log
			final ItemCollectionPanel collectionPanel = new ItemCollectionPanel(palette, 6);
			this.addSubItems(collectionPanel, emoteClueItem);
			collectionPanel.setHeaderColor(palette.getFoldHeaderTextColor()); // Header will not display collection progress.
			itemPanel.addChild(collectionPanel);

			// Add emote clue panels & info
			Arrays.stream(EmoteClueAssociations.EmoteClueItemToEmoteClues.get(emoteClueItem)).map(emoteCluePanelMap::get).forEach(itemPanel::addChild);
		});

		this.clueItemsGrid = new EmoteClueItemsGrid(palette);
		this.clueItemsGrid.load(this.emoteClueItemPanelMap.values());

		final StashUnitPanel stashUnitPanel = new StashUnitPanel(palette);

		final TabMenu tabMenu = new TabMenu(palette, this.clueItemsGrid, "Items", "Emote Clue Items");
		tabMenu.addTab(stashUnitPanel, "Stashes", "Stash Units", false);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;

		super.add(tabMenu, c);
		c.gridy++;
		super.add(this.clueItemsGrid, c);
		super.add(stashUnitPanel, c);

		c.gridy++;
		c.insets = new Insets(10, 20, 0, 20);
		super.add(new FooterPanel(palette, pluginName, pluginVersion, gitHubUrl), c);
	}

	private void addSubItems(final ItemCollectionPanel subPanel, final EmoteClueItem child)
	{
		final ItemSlotPanel childSlotPanel = this.slotPanelMap.get(child);
		if (childSlotPanel != null)
		{
			subPanel.addItem(childSlotPanel);
			return;
		}

		final List<EmoteClueItem> successors = child.getChildren();
		if (successors != null)
		{
			for (final EmoteClueItem successor : successors)
			{
				this.addSubItems(subPanel, successor);
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
		final ItemSlotPanel slotPanel = this.slotPanelMap.get(emoteClueItem);
		if (slotPanel != null)
		{
			slotPanel.setQuantity(quantity);
		}
	}

	/**
	 * Changes an {@link EmoteClue} {@link EmoteClueItem} status panel to represent given {@link UpdatablePanel.Status} status, if a mapping to {@link EmoteClueItemPanel} exists.
	 *
	 * @param emoteClueItem the {@link EmoteClue} {@link EmoteClueItem} requirement to display.
	 * @param status        the desired {@link UpdatablePanel.Status} status to display.
	 */
	public void setEmoteClueItemStatus(final EmoteClueItem emoteClueItem, final UpdatablePanel.Status status)
	{
		final EmoteClueItemPanel itemPanel = this.emoteClueItemPanelMap.get(emoteClueItem);
		if (itemPanel != null)
		{
			itemPanel.setStatus(status);
		}
	}

	public void setEmoteClueItemGridDisclaimer(final String text)
	{
		this.clueItemsGrid.setDisclaimer(text);
	}

	public void removeEmoteClueItemGridDisclaimer()
	{
		this.clueItemsGrid.removeDisclaimer();
	}
}
