package com.banktaglayouts;

import com.banktaglayouts.invsetupsstuff.InventorySetup;

import java.util.*;

import com.banktaglayouts.invsetupsstuff.InventorySetupsItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemVariationMapping;

@Slf4j
@RequiredArgsConstructor
public class LayoutGenerator {
	private final BankTagLayoutsPlugin plugin;

	public Layout generateLayout(List<Integer> equippedItems, List<Integer> inventory, List<Integer> runePouch, List<Integer> additionalItems, Layout currentLayout, int duplicateLimit, BankTagLayoutsConfig.LayoutStyles layoutStyle) {
		equippedItems = equippedItems.stream()
			.map(itemId -> plugin.itemManager.canonicalize(itemId)) // Weight reducing items have different ids when equipped; this fixes that.
			.collect(Collectors.toList());

		switch (layoutStyle){
			case ZIGZAG:
				return zigzagLayout(equippedItems, inventory, Collections.emptyList(), additionalItems, currentLayout, duplicateLimit);
			case PRESETS:
				return presetsLayout(equippedItems, inventory, runePouch, additionalItems, currentLayout);
			default:
				throw new IllegalArgumentException("Please supply a layout style to this method.");
		}
	}

	public Layout basicInventorySetupsLayout(InventorySetup inventorySetup, Layout currentLayout, int duplicateLimit, BankTagLayoutsConfig.LayoutStyles layoutStyle) {
		List<Integer> equippedGear = inventorySetup.getEquipment() == null ? Collections.emptyList() : inventorySetup.getEquipment().stream().map(InventorySetupsItem::getId).collect(Collectors.toList());
		List<Integer> inventory = inventorySetup.getInventory() == null ? Collections.emptyList() : inventorySetup.getInventory().stream().map(InventorySetupsItem::getId).collect(Collectors.toList());
		List<Integer> runePouchRunes = inventorySetup.getRune_pouch() == null ? Collections.emptyList() : inventorySetup.getRune_pouch().stream().map(InventorySetupsItem::getId).collect(Collectors.toList());
		List<Integer> additionalItems = inventorySetup.getAdditionalFilteredItems() == null ? Collections.emptyList() : inventorySetup.getAdditionalFilteredItems().values().stream().map(InventorySetupsItem::getId).collect(Collectors.toList());
		return generateLayout(equippedGear, inventory, runePouchRunes, additionalItems, currentLayout, duplicateLimit, layoutStyle);
	}

	public Layout presetsLayout(List<Integer> equippedItems, List<Integer> inventory, List<Integer> runePouch, List<Integer> additionalItems, Layout currentLayout) {
		Layout previewLayout = Layout.emptyLayout();

		// lay out equipped items.
		// if the player hasn't equipped anything since launching the game this array will not come back with the expected -1 values. so we will just add them in ourselves.
		while (equippedItems.size() < 14) {
			equippedItems.add(-1);
		}
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.HEAD.getSlotIdx()), 1);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.CAPE.getSlotIdx()), 8);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.AMULET.getSlotIdx()), 9);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.WEAPON.getSlotIdx()), 16);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.BODY.getSlotIdx()), 17);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.SHIELD.getSlotIdx()), 18);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.AMMO.getSlotIdx()), 10);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.LEGS.getSlotIdx()), 25);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.GLOVES.getSlotIdx()), 32);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.BOOTS.getSlotIdx()), 33);
		previewLayout.putItem(equippedItems.get(EquipmentInventorySlot.RING.getSlotIdx()), 34);


		int invRow = 0;
		int invCol = 4;
		int width = 8;
		for(Integer i: inventory) {
			previewLayout.putItem(i, invCol + (invRow * width));
			if (invCol == 7) {
				invCol = 4;
				invRow++;
			} else {
				invCol++;
			}
		}
		boolean hasPouch = inventory.stream().anyMatch(Predicate.isEqual(ItemID.RUNE_POUCH)) || inventory.stream().anyMatch(Predicate.isEqual(ItemID.RUNE_POUCH_L));
		if (hasPouch) {
			int c = 0;
			for (Integer r : runePouch) {
				previewLayout.putItem(r, c + 40);
				c++;
			}
		}

		// If the item is in a safe spot copy it over from the old layout
		for (Map.Entry<Integer, Integer> e: currentLayout.allPairs()) {
			if (indexInAllowedSpace(inventory, e.getKey())) {
				previewLayout.putItem(e.getValue(), e.getKey());
			}
		}
		// If the item is in an invalid spot move it to a valid spot in the sandbox area
		for (Map.Entry<Integer, Integer> e: currentLayout.allPairs()) {
			if (!indexInAllowedSpace(inventory, e.getKey())) {
				if (inventory.contains(e.getValue()) || equippedItems.contains(e.getValue()) || (hasPouch && runePouch.contains(e.getValue()))) {
					continue;
				}
				int index = 0;
				while (!indexInAllowedSpace(inventory, index) || (previewLayout.getItemAtIndex(index) != -1)) {
					index++;
				}

                if (!layoutContainsItem(e.getValue(), previewLayout)) {
					previewLayout.putItem(e.getValue(), index);
				}
			}
		}

		// Add additional items
		for (Integer i: additionalItems) {
			if (previewLayout.countItemsWithId(i) > 0) {
				continue;
			}
			int index = 0;
			while (!indexInAllowedSpace(inventory, index) || (previewLayout.getItemAtIndex(index) > 0)) {
				index++;
			}
			previewLayout.putItem(i, index);
		}

		return previewLayout;
	}

	private boolean indexInAllowedSpace(List<Integer> inventory, int index) {
		int inventoryHeight = 0;
		int c = 1;
		for (Integer i : inventory) {
			if (i > 0) {
				inventoryHeight = (int) (Math.ceil(c / 4.0));
			}
			c++;
		}
		// 1 means the inventory spaces are occupied
		int[][] mask = {
				{1, 1, 1, 1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1, 1, 1, 1},
				{1, 1, 1, 1, 1, 1, 1, 1},
				{0, 0, 0, 0, 1, 1, 1, 1},
		};
		for (int x = 0; x < inventoryHeight; x++) {
			if (x == 6) {
				mask[x] = new int[]{0, 0, 0, 0, 1, 1, 1, 1};
			} else {
				mask[x] = new int[]{1, 1, 1, 1, 1, 1, 1, 1};
			}
		}
		int[] flatMask = Arrays.stream(mask).flatMapToInt(Arrays::stream).toArray();
		return index >= 56 || flatMask[index] == 0;
	}

	public Layout zigzagLayout(List<Integer> equippedItems, List<Integer> inventory, List<Integer> runePouch, List<Integer> additionalItems, Layout currentLayout, int duplicateLimit) {
		Layout previewLayout = Layout.emptyLayout();
		List<Integer> displacedItems = new ArrayList<>();

		log.debug("generate layout");
		log.debug("equipped gear is " + equippedItems);
		log.debug("inventory is " + inventory);

		int i = 0;

		// lay out equipped items.
		i = layoutItems(equippedItems, currentLayout, previewLayout, displacedItems, i, true);

		inventory = inventory.stream().filter(integer -> integer != -1).collect(Collectors.toList());

		// lay out the inventory items.
		if (duplicateLimit <= 0)
		{
			// distinct leaves the first duplicate it encounters and removes only duplicates coming after the first.
			inventory = inventory.stream().distinct().collect(Collectors.toList());
		}
		else
		{
			inventory = limitDuplicates(inventory, duplicateLimit);
		}

		i = layoutItems(inventory, currentLayout, previewLayout, displacedItems, i, true);

		i = layoutItems(runePouch, currentLayout, previewLayout, displacedItems, i, false);

		i = layoutItems(additionalItems, currentLayout, previewLayout, displacedItems, i, false);

		int displacedItemsStart = i;

		// copy items from current layout into the empty spots.
		for (Map.Entry<Integer, Integer> itemPosition : currentLayout.allPairs()) {
			int index = itemPosition.getKey();
			int currentItemAtIndex = itemPosition.getValue();
			int previewItemAtIndex = previewLayout.getItemAtIndex(index);

			if (currentItemAtIndex != -1 && previewItemAtIndex == -1) {
				previewLayout.putItem(currentItemAtIndex, index);
			}
		}

		// Remove items that were placed as part of the gear or inventory.
		displacedItems = displacedItems.stream().filter(id -> !layoutContainsItem(id, previewLayout)).collect(Collectors.toList());

		int j = displacedItemsStart;
		while (displacedItems.size() > 0 && j < 2000 / 38 * 8) {
			int currentItemAtIndex = currentLayout.getItemAtIndex(j);
			if (currentItemAtIndex == -1) {
				Integer itemId = displacedItems.remove(0);
				log.debug(itemId + " goes to " + j);
				previewLayout.putItem(itemId, j);
			}

			j++;
		}

		return previewLayout;
	}

	private List<Integer> limitDuplicates(List<Integer> inventory, int duplicateLimit)
	{
		List<Map.Entry<Integer, Integer>> groupedInventory = new ArrayList<>();

		int inARow = 0;
		int lastItemId = -1;
		for (Integer itemId : inventory)
		{
			if (lastItemId != itemId)
			{
				int quantity = inARow > duplicateLimit ? 1 : inARow;
				groupedInventory.add(new AbstractMap.SimpleEntry<>(lastItemId, quantity));
				inARow = 0;
			}
			inARow++;

			lastItemId = itemId;
		}
		int quantity = inARow > duplicateLimit ? 1 : inARow;
		if (quantity > 0) groupedInventory.add(new AbstractMap.SimpleEntry<>(lastItemId, quantity));

		inventory = groupedInventory.stream().flatMap(entry -> Collections.nCopies(entry.getValue(), entry.getKey()).stream()).collect(Collectors.toList());
		return inventory;
	}

	private int layoutItems(List<Integer> inventory, Layout currentLayout, Layout previewLayout, List<Integer> displacedItems, int i, boolean useZigZag) {
		for (Integer itemId : inventory) {
			if (itemId == -1) continue;
			int index = useZigZag ? toZigZagIndex(i, 0, 0) : i;
			previewLayout.putItem(itemId, index);
			int currentLayoutItem = currentLayout.getItemAtIndex(index);
			if (currentLayoutItem != -1) displacedItems.add(currentLayoutItem);
			i++;
		}
		if (!inventory.isEmpty()) {
			Optional<Integer> highestUsedIndex = previewLayout.getAllUsedIndexes().stream().max(Integer::compare);
			if (highestUsedIndex.isPresent()) {
				if (useZigZag) {
					i = (highestUsedIndex.get() / 16 * 2 + 2) * 8;
				} else {
					i = (highestUsedIndex.get() / 8 + 1) * 8;
				}
			}
		}
		return i;
	}

	private boolean layoutContainsItem(int id, Layout previewLayout) {
		int baseId = ItemVariationMapping.map(plugin.getNonPlaceholderId(id));
		for (Integer item : previewLayout.getAllUsedItemIds()) {
			if (baseId == ItemVariationMapping.map(plugin.getNonPlaceholderId(item))) {
				return true;
			}
		}
		return false;
	}

	private static int toZigZagIndex(int inventoryIndex, int row, int col) {
		if (inventoryIndex < 0 || row < 0 || col < 0) throw new IllegalArgumentException();

		row += (inventoryIndex / 16) * 2; // Does this cover multiple pairs of rows?
		inventoryIndex -= (inventoryIndex / 16) * 16;
		int index = 0;
		index += inventoryIndex % 2 == 0 ? 0 : 8; // top or bottom row?
		index += inventoryIndex / 2; // column.
		index += row * 8 + col; // offset.
		return index;
	}
}
