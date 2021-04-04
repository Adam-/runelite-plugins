package com.banktaglayouts;

import inventorysetups.InventorySetup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class LayoutGenerator {

    private final BankTagLayoutsPlugin plugin;

    public Map<Integer, Integer> basicLayout(List<Integer> equippedItems, List<Integer> inventory, Map<Integer, Integer> currentLayout) {
        Map<Integer, Integer> previewLayout = new HashMap<>();
        List<Integer> displacedItems = new ArrayList<>();

        log.debug("generate layout");
        log.debug("equipped gear is " + equippedItems);
        log.debug("inventory is " + inventory);

        // lay out equipped items.
        int i = 0;
        for (Integer itemId : equippedItems) {
            if (itemId == -1) continue;
            itemId = plugin.itemManager.canonicalize(itemId); // Weight reducing items have different ids when equipped; this fixes that.
            int index = toZigZagIndex(i, 0, 0);
            previewLayout.put(itemId, index);
            int currentLayoutItem = getItemAtIndex(index, currentLayout);
            if (currentLayoutItem != -1) displacedItems.add(currentLayoutItem);
//            System.out.println("adding " + plugin.itemNameWithId(currentLayoutItem));
            i++;
        }
        // If there are equipped items (and therefore the first 2 rows are in use now), copy over items from the current
        // layout in the unused spaces.
        int endOfEquippedItems = i;
        if (i > 0) {
            i = 16;
        }

        // lay out the inventory items.
        // distinct leaves the first duplicate it encounters and removes only duplicates coming after the first.
        inventory = inventory.stream().distinct().collect(Collectors.toList());
        // Equipped items will never have >11 items in it so it will never spill over into the next row.
        int inventoryItemsStart = i;
        for (Integer itemId : inventory) {
            if (itemId == -1) continue;
            int index = toZigZagIndex(i, 0, 0);
            previewLayout.put(itemId, index);
            int currentLayoutItem = getItemAtIndex(index, currentLayout);
            if (currentLayoutItem != -1) displacedItems.add(currentLayoutItem);
//            System.out.println("adding " + plugin.itemNameWithId(currentLayoutItem));
            i++;
        }

        Optional<Integer> highestUsedIndex = previewLayout.values().stream().max(Integer::compare);
        if (!highestUsedIndex.isPresent()) return previewLayout; // no items in the layout were moved.
        int displacedItemsStart = (highestUsedIndex.get() / 16 * 2 + 2) * 8;

        if (endOfEquippedItems > 0) {
            for (; endOfEquippedItems < 16; endOfEquippedItems++) {
                int zigZag = toZigZagIndex(endOfEquippedItems, 0, 0);
                int itemAtIndex = getItemAtIndex(zigZag, currentLayout);
                if (itemAtIndex != -1 && !layoutContainsItem(itemAtIndex, previewLayout)) {
                    previewLayout.put(itemAtIndex, zigZag);
                }
            }
        }

        // If there are inventory items, copy over items from the current layout in the unused spaces.
        if (inventoryItemsStart < i) {
            for (; i < displacedItemsStart; i++) {
                int zigZag = toZigZagIndex(i, 0, 0);
                int itemAtIndex = getItemAtIndex(zigZag, currentLayout);
                if (itemAtIndex != -1 && !layoutContainsItem(itemAtIndex, previewLayout)) {
                    previewLayout.put(itemAtIndex, zigZag);
                }
            }
        }

        log.debug("displaced items: " + displacedItems);
        // Remove items that were placed as part of the gear or inventory.
        displacedItems = displacedItems.stream().filter(id -> !layoutContainsItem(id, previewLayout)).collect(Collectors.toList());
        log.debug("displaced items2: " + displacedItems);

        int j = displacedItemsStart;
        while (displacedItems.size() > 0 && j < 2000 / 38 * 8) {
            int currentItemAtIndex = getItemAtIndex(j, currentLayout);
//            System.out.println((currentItemAtIndex == -1) + " " + (layoutContainsItem(currentItemAtIndex, previewLayout)));
            if (currentItemAtIndex == -1 || listContainsItem(currentItemAtIndex, equippedItems) || listContainsItem(currentItemAtIndex, inventory)) {
                Integer itemId = displacedItems.remove(0);
                log.debug(itemId + " goes to " + j);
                previewLayout.put(itemId, j);
            }

            j++;
        }
//        System.out.println(displacedItems.size());

        // Add existing items not displaced by autolayout to the preview.
        currentLayout.entrySet().stream().filter(e -> e.getValue() >= displacedItemsStart && !previewLayout.containsKey(e.getKey())).forEach(e -> previewLayout.put(e.getKey(), e.getValue()));

        return previewLayout;
    }

    private boolean listContainsItem(int id, List<Integer> items) {
        int nonPlaceholderId = plugin.getNonPlaceholderId(id);
        for (Integer item : items) {
            if (nonPlaceholderId == plugin.getNonPlaceholderId(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean layoutContainsItem(int id, Map<Integer, Integer> previewLayout) {
        int nonPlaceholderId = plugin.getNonPlaceholderId(id);
        for (Integer item : previewLayout.keySet()) {
            if (nonPlaceholderId == plugin.getNonPlaceholderId(item)) {
                return true;
            }
        }
        return false;
    }

    public static int getItemAtIndex(int index, Map<Integer, Integer> layout) {
        return layout.entrySet().stream().filter(e -> e.getValue() == index).map(e -> e.getKey()).findAny().orElse(-1);
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

    // TODO opening inventory setups panel closes auto-layout preview.

    public Map<Integer, Integer> basicInventorySetupsLayout(InventorySetup inventorySetup, Map<Integer, Integer> currentLayout) {
        List<Integer> equippedGear = inventorySetup.getEquipment().stream().map(isi -> isi.getId()).collect(Collectors.toList());
        List<Integer> inventory = inventorySetup.getInventory().stream().map(isi -> isi.getId()).collect(Collectors.toList());
        List<Integer> runePouchRunes = inventorySetup.getRune_pouch().stream().map(isi -> isi.getId()).collect(Collectors.toList());
        List<Integer> additionalItems = inventorySetup.getAdditionalFilteredItems().entrySet().stream().map(isi -> isi.getValue().getId()).collect(Collectors.toList());

        return basicLayout(equippedGear, inventory, currentLayout);
    }
}
