package com.larsvansoest.runelite.clueitems.progress;

import com.larsvansoest.runelite.clueitems.data.EmoteClue;
import com.larsvansoest.runelite.clueitems.data.EmoteClueAssociations;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.data.StashUnit;
import com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cluescrolls.clues.item.AllRequirementsCollection;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Keeps track of item requirement progression. Contains inventory change and STASHUnit fill status functionality.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public class ProgressManager
{
	private final InventoryMonitor inventoryMonitor;
	private final StashMonitor stashMonitor;
	private final HashMap<EmoteClueItem, UpdatablePanel.Status> inventoryStatusMap;
	private final Map<EmoteClueItem, Map<StashUnit, Boolean>> stashFilledStatusMap;
	private final Client client;
	private final ClientThread clientThread;
	private final BiConsumer<EmoteClueItem, Integer> onEmoteClueItemQuantityChanged;
	private final BiConsumer<EmoteClueItem, UpdatablePanel.Status> onEmoteClueItemInventoryStatusChanged;
	private final BiConsumer<EmoteClueItem, UpdatablePanel.Status> onEmoteClueItemStatusChanged;
	
	private boolean initialState;

	public ProgressManager(
			final ConfigManager configManager, final Client client, final ClientThread clientThread, final BiConsumer<EmoteClueItem, Integer> onEmoteClueItemQuantityChanged,
			final BiConsumer<EmoteClueItem, UpdatablePanel.Status> onEmoteClueItemInventoryStatusChanged, final BiConsumer<EmoteClueItem, UpdatablePanel.Status> onEmoteClueItemStatusChanged)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.inventoryMonitor = new InventoryMonitor();
		this.stashMonitor = new StashMonitor("[EmoteClueItems]", "STASHUnit fill statuses", configManager);
		this.inventoryStatusMap = new HashMap<>(EmoteClueItem.values().length);
		this.stashFilledStatusMap = new HashMap<>(EmoteClueAssociations.EmoteClueItemToEmoteClues.keySet().size());
		this.onEmoteClueItemQuantityChanged = onEmoteClueItemQuantityChanged;
		this.onEmoteClueItemInventoryStatusChanged = onEmoteClueItemInventoryStatusChanged;
		this.onEmoteClueItemStatusChanged = onEmoteClueItemStatusChanged;

		EmoteClueAssociations.EmoteClueItemToEmoteClues.forEach(((emoteClueItem, emoteClues) ->
		{
			final Map<StashUnit, Boolean> emoteClueStashFillStatusMap = new HashMap<>(emoteClues.length);
			for (final EmoteClue emoteClue : emoteClues)
			{
				emoteClueStashFillStatusMap.put(emoteClue.getStashUnit(), false);
			}
			this.stashFilledStatusMap.put(emoteClueItem, emoteClueStashFillStatusMap);
		}));

		this.reset();
	}

	/**
	 * Clears all cached progression data.
	 */
	public void reset()
	{
		this.inventoryMonitor.reset();
		for (final EmoteClueItem emoteClueItem : EmoteClueItem.values())
		{
			this.inventoryStatusMap.put(emoteClueItem, UpdatablePanel.Status.InComplete);
		}
		for (final EmoteClueItem emoteClueItem : EmoteClueAssociations.EmoteClueItemToEmoteClues.keySet())
		{
			final Map<StashUnit, Boolean> emoteClueStashFillStatusMap = this.stashFilledStatusMap.get(emoteClueItem);
			emoteClueStashFillStatusMap.keySet().forEach(key -> emoteClueStashFillStatusMap.put(key, false));
		}
		this.initialState = true;
	}

	/**
	 * Writes item changes to progression data.
	 */
	public void processInventoryChanges(final ItemContainerChanged event)
	{
		final int containerId = event.getContainerId();

		if (this.initialState && containerId == 95)
		{
			this.clientThread.invoke(() ->
			{
				final ItemContainer bankContainer = this.client.getItemContainer(InventoryID.BANK);
				if (bankContainer != null)
				{
					this.handleItemChanges(this.inventoryMonitor.fetchEmoteClueItemChanges(95, bankContainer.getItems()));
					this.initialState = false;
				}
			});
		}
		else
		{
			this.handleItemChanges(this.inventoryMonitor.fetchEmoteClueItemChanges(containerId, event.getItemContainer().getItems()));
		}
	}

	/**
	 * Call this function after user login.
	 * <p>
	 * Using {@link com.larsvansoest.runelite.clueitems.progress.StashMonitor}, verifies if user-data is consistent with data stored in Runelite's {@link net.runelite.client.config.ConfigManager}.
	 * <p>
	 * Resets config data if data is corrupted, possible by game update.
	 */
	public void validateConfig()
	{
		this.stashMonitor.validate();
		for (final StashUnit stashUnit : StashUnit.values())
		{
			this.setStashUnitFilled(stashUnit, this.stashMonitor.getStashFilled(stashUnit));
		}
	}

	private void handleItemChanges(final List<Item> emoteClueItemChanges)
	{
		if (emoteClueItemChanges != null)
		{
			// Set single item (sub-)requirement status
			for (final Item item : emoteClueItemChanges)
			{
				final int quantity = item.getQuantity();
				final EmoteClueItem emoteClueItem = EmoteClueAssociations.ItemIdToEmoteClueItem.get(item.getId());

				this.onEmoteClueItemQuantityChanged.accept(emoteClueItem, quantity);

				final UpdatablePanel.Status status = quantity > 0 ? UpdatablePanel.Status.Complete : UpdatablePanel.Status.InComplete;
				this.inventoryStatusMap.put(emoteClueItem, status);

				this.setEmoteClueItemStatus(emoteClueItem, this.updateEmoteClueItemStatus(emoteClueItem));
			}
		}
	}

	/**
	 * Returns whether given {@link com.larsvansoest.runelite.clueitems.data.StashUnit} is set as filled in progression data.
	 */
	public boolean getStashUnitFilled(final StashUnit stashUnit)
	{
		return this.stashMonitor.getStashFilled(stashUnit);
	}

	/**
	 * Specify given {@link com.larsvansoest.runelite.clueitems.data.StashUnit} as filled in progression data.
	 */
	public void setStashUnitFilled(final StashUnit stashUnit, final boolean filled)
	{
		this.stashMonitor.setStashFilled(stashUnit, filled);
		for (final EmoteClue emoteClue : EmoteClueAssociations.STASHUnitToEmoteClues.get(stashUnit))
		{
			for (final EmoteClueItem emoteClueItem : EmoteClueAssociations.EmoteClueToEmoteClueItems.get(emoteClue))
			{
				this.stashFilledStatusMap.get(emoteClueItem).put(stashUnit, filled);
				this.setEmoteClueItemStatus(emoteClueItem, this.updateEmoteClueItemStatus(emoteClueItem));
			}
		}
	}

	private UpdatablePanel.Status updateEmoteClueItemStatus(final EmoteClueItem emoteClueItem)
	{
		final UpdatablePanel.Status inventoryStatus = this.inventoryStatusMap.get(emoteClueItem);
		this.onEmoteClueItemInventoryStatusChanged.accept(emoteClueItem, inventoryStatus);
		if (inventoryStatus == UpdatablePanel.Status.Complete)
		{
			return UpdatablePanel.Status.Complete;
		}
		final Map<StashUnit, Boolean> emoteClueStashFilledMap = this.stashFilledStatusMap.get(emoteClueItem);
		if (Objects.nonNull(emoteClueStashFilledMap) && !this.initialState)
		{
			if (this.stashFilledStatusMap.get(emoteClueItem).values().stream().allMatch(Boolean::booleanValue))
			{
				return UpdatablePanel.Status.Complete;
			}
			if (this.stashFilledStatusMap.get(emoteClueItem).values().stream().anyMatch(Boolean::booleanValue))
			{
				return UpdatablePanel.Status.InProgress;
			}
		}
		return inventoryStatus;
	}

	private void setEmoteClueItemStatus(final EmoteClueItem emoteClueItem, final UpdatablePanel.Status status)
	{
		this.onEmoteClueItemStatusChanged.accept(emoteClueItem, status);
		for (final EmoteClueItem parent : emoteClueItem.getParents())
		{
			this.setEmoteClueItemStatus(parent, this.getParentStatus(parent));
		}
	}

	private UpdatablePanel.Status getParentStatus(final EmoteClueItem parent)
	{
		final ItemRequirement parentRequirement = parent.getItemRequirement();
		final List<EmoteClueItem> children = parent.getChildren();
		return (parentRequirement instanceof AllRequirementsCollection) ? this.getParentAllStatus(children) : this.getParentAnyStatus(children);
	}

	private UpdatablePanel.Status getParentAnyStatus(final List<EmoteClueItem> children)
	{
		for (final EmoteClueItem child : children)
		{
			if (this.updateEmoteClueItemStatus(child) == UpdatablePanel.Status.Complete)
			{
				return UpdatablePanel.Status.Complete;
			}
		}
		return UpdatablePanel.Status.InComplete;
	}

	private UpdatablePanel.Status getParentAllStatus(final List<EmoteClueItem> children)
	{
		boolean anyMatch = false;
		boolean allMatch = true;
		for (final EmoteClueItem child : children)
		{
			if (this.updateEmoteClueItemStatus(child) == UpdatablePanel.Status.Complete)
			{
				anyMatch = true;
			}
			else
			{
				allMatch = false;
			}
		}
		return allMatch ? UpdatablePanel.Status.Complete : anyMatch ? UpdatablePanel.Status.InProgress : UpdatablePanel.Status.InComplete;
	}
}
