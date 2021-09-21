package com.larsvansoest.runelite.clueitems.ui.components;

import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;

import java.util.HashMap;

/**
 * Implements {@link com.larsvansoest.runelite.clueitems.ui.components.ItemCollectionPanel}.
 * <p>
 * Tracks {@link net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement} statuses, and displays its header text in a colour corresponding to that status.
 */
public class ItemRequirementCollectionPanel extends ItemCollectionPanel
{
	private final HashMap<ItemRequirement, UpdatablePanel.Status> requirementStatusMap;

	/**
	 * Creates the panel.
	 *
	 * @param palette     Colour scheme for the panel.
	 * @param name        Name displayed as the panel header text.
	 * @param slotRowSize The amount of item icons per row.
	 */
	public ItemRequirementCollectionPanel(final EmoteClueItemsPalette palette, final String name, final int slotRowSize)
	{
		super(palette, name, slotRowSize);
		this.requirementStatusMap = new HashMap<>();
	}

	/**
	 * Add an item requirement to track {@link com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel.Status}.
	 * <p>
	 * When all statuses are set to complete, changes header text color using {@link #setRequirementStatus(net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement, com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel.Status)}.
	 *
	 * @param itemRequirement the item requirement to track.
	 */
	public void addRequirement(final ItemRequirement itemRequirement)
	{
		this.requirementStatusMap.put(itemRequirement, Status.Unknown);
	}

	/**
	 * Set the {@link com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel.Status} of a previously added item requirement with {@link #addRequirement(net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement)}.
	 * <p>
	 * When all statuses are set to complete, changes header text color.
	 *
	 * @param itemRequirement the item requirement of which to change status.
	 * @param status          the new status of the given item requirement.
	 */
	public void setRequirementStatus(final ItemRequirement itemRequirement, final UpdatablePanel.Status status)
	{
		if (this.requirementStatusMap.containsKey(itemRequirement))
		{
			this.requirementStatusMap.put(itemRequirement, status);
		}
		super.setStatus(this.requirementStatusMap.values().stream().allMatch(Status.Complete::equals) ? Status.Complete : Status.InComplete);
	}
}