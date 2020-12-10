package com.larsvansoest.runelite.clueitems.overlay.widgets;

import com.larsvansoest.runelite.clueitems.support.Util;
import java.util.HashSet;

public enum Window
{
	Default(null, -1, new int[]{0}, new int[]{14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24}),

	Bank(Container.Bank, 12, new int[]{3, 4}, new int[]{75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85}),

	DepositBox(Container.DepositBox, 2, new int[]{}, new int[]{}),

	Shop(Container.Shop, 16, new int[]{0}, new int[]{}),

	GuidePrices(Container.GuidePrices, 2, new int[]{0}, new int[]{}),

	SeedVault(Container.SeedVault, 15, new int[]{1}, new int[]{}),

	Equipment(null, -1, new int[]{0}, new int[]{});

	private final Container container;
	private final int main;

	private final HashSet<Integer> inventory;
	private final HashSet<Integer> equipment;

	Window(Container container, int main, int[] inventory, int[] equipment)
	{
		this.container = container;
		this.main = main;
		this.inventory = Util.toHashSet(inventory);
		this.equipment = Util.toHashSet(equipment);
	}

	public Container getContainer(int child)
	{
		if (child == this.main)
		{
			return this.container;
		}
		if (this.inventory.contains(child))
		{
			return Container.Inventory;
		}
		if (this.equipment.contains(child))
		{
			return Container.Equipment;
		}
		return null;
	}
}