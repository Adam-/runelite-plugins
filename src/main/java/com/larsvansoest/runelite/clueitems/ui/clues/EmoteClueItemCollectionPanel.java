package com.larsvansoest.runelite.clueitems.ui.clues;

import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPalette;
import com.larsvansoest.runelite.clueitems.ui.components.ItemCollectionPanel;
import net.runelite.client.plugins.cluescrolls.clues.item.AllRequirementsCollection;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;
import net.runelite.client.plugins.cluescrolls.clues.item.SingleItemRequirement;

import java.util.HashMap;
import java.util.HashSet;

public class EmoteClueItemCollectionPanel extends ItemCollectionPanel
{
	private final HashSet<EmoteClueItem> parents;
	private final HashMap<EmoteClueItem, Status> requirementStatuses;
	private final boolean strong;

	public EmoteClueItemCollectionPanel(final EmoteClueItemsPalette palette, final String name, final int slotRowSize, final boolean strong)
	{
		super(palette, name, slotRowSize);
		this.requirementStatuses = new HashMap<>();
		this.parents = new HashSet<>();
		this.strong = strong;
	}

	/**
	 * Add an {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} requirement to include in inventory status checking.
	 * <p>
	 * Automatically adds the children of given {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} to the inner data structure, which serve as parameter for {@link #setStatus(com.larsvansoest.runelite.clueitems.data.EmoteClueItem, com.larsvansoest.runelite.clueitems.ui.components.UpdatablePanel.Status)}.
	 *
	 * @param emoteClueItem the requirement to include.
	 */
	public void addRequirement(final EmoteClueItem emoteClueItem)
	{
		this.parents.add(emoteClueItem);
		this.addRequirementStatus(emoteClueItem);
	}

	private void addRequirementStatus(final EmoteClueItem emoteClueItem)
	{
		this.requirementStatuses.put(emoteClueItem, Status.InComplete);
		emoteClueItem.getChildren().forEach(this::addRequirementStatus);
	}

	/**
	 * Set the status of a requirement or any of its children. Automatically checks if collection log is complete.
	 *
	 * @param emoteClueItem the requirement, must have been added through {@link #addRequirement(com.larsvansoest.runelite.clueitems.data.EmoteClueItem)}.
	 * @param status        the status of the requirement.
	 */
	public void setStatus(final EmoteClueItem emoteClueItem, final Status status)
	{
		if (this.requirementStatuses.containsKey(emoteClueItem))
		{
			this.requirementStatuses.put(emoteClueItem, status);
			final boolean complete;
			if (this.strong)
			{
				complete = this.parents.stream().allMatch(this::complete);
			}
			else
			{
				complete = this.parents.stream().anyMatch(this::complete);
			}
			super.setStatus(complete ? Status.Complete : Status.InComplete);
		}
	}

	private boolean complete(final EmoteClueItem emoteClueItem)
	{
		final ItemRequirement itemRequirement = emoteClueItem.getItemRequirement();

		if (itemRequirement instanceof SingleItemRequirement)
		{
			return this.requirementStatuses.get(emoteClueItem) == Status.Complete;
		}
		else if (itemRequirement instanceof AllRequirementsCollection)
		{
			return emoteClueItem.getChildren().stream().allMatch(this::complete);
		}
		else
		{
			return emoteClueItem.getChildren().stream().anyMatch(this::complete);
		}
	}
}