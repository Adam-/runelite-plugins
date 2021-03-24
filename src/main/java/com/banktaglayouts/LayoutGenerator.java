package com.banktaglayouts;

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
            int index = toZigZagIndex(i, 0, 0);
            previewLayout.put(itemId, index);
            int currentLayoutItem = getItemAtIndex(index, currentLayout);
            if (currentLayoutItem != -1) displacedItems.add(currentLayoutItem);
            System.out.println("adding " + plugin.itemNameWithId(currentLayoutItem));
            i++;
        }
        // If there are equipped items (and therefore the first 2 rows are in use now), copy over items from the current
        // layout in the unused spaces.
        if (i > 0) {
            for (; i < 16; i++) {
                int itemAtIndex = getItemAtIndex(i, currentLayout);
                previewLayout.put(itemAtIndex, i);
            }
            i = 16;
        }

        // lay out the inventory items.
        // TODO uh oh... weight reducing items.
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
            System.out.println("adding " + plugin.itemNameWithId(currentLayoutItem));
            i++;
        }

        Optional<Integer> highestUsedIndex = previewLayout.values().stream().max(Integer::compare);
        if (!highestUsedIndex.isPresent()) return previewLayout; // no items in the layout were moved.
        int displacedItemsStart = (highestUsedIndex.get() / 8 + 1) * 8;

        // If there are inventory items, copy over items from the current layout in the unused spaces.
        if (inventoryItemsStart < i) {
            for (; i < displacedItemsStart; i++) {
                int zigZag = toZigZagIndex(i, 0, 0);
                int itemAtIndex = getItemAtIndex(zigZag, currentLayout);
                previewLayout.put(itemAtIndex, zigZag);
            }
        }

        // Remove items that were placed as part of the gear or inventory.
        displacedItems = displacedItems.stream().filter(id -> !layoutContainsItem(id, previewLayout)).collect(Collectors.toList());

        int j = displacedItemsStart;
        while (displacedItems.size() > 0 && j < 2000 / 38 * 8) {
            int currentItemAtIndex = getItemAtIndex(j, currentLayout);
            if (currentItemAtIndex == -1 || layoutContainsItem(currentItemAtIndex, previewLayout)) {
                Integer itemId = displacedItems.remove(0);
                log.debug(itemId + " goes to " + j);
                previewLayout.put(itemId, j);
            }

            j++;
        }
        System.out.println(displacedItems.size());

        // Add existing items not displaced by autolayout to the preview.
        currentLayout.entrySet().stream().filter(e -> e.getValue() >= displacedItemsStart && !previewLayout.containsKey(e.getKey())).forEach(e -> previewLayout.put(e.getKey(), e.getValue()));

        return previewLayout;
    }

    private boolean layoutContainsItem(Integer id, Map<Integer, Integer> previewLayout) {
        int nonPlaceholderId = plugin.getNonPlaceholderId(id);
        for (Map.Entry<Integer, Integer> entry : previewLayout.entrySet()) {
            if (nonPlaceholderId == plugin.getNonPlaceholderId(entry.getKey())) {
                return true;
            }
        }
        log.debug("removing displaced item: " + plugin.itemNameWithId(id));
        return false;
    }

    public static int getItemAtIndex(int index, Map<Integer, Integer> layout) {
        return layout.entrySet().stream().filter(e -> e.getValue() == index).map(e -> e.getKey()).findAny().orElse(-1);
    }

    public static Map<Integer, Integer> basicLayout_old(List<Integer> equippedItems, List<Integer> inventory, Map<Integer, Integer> currentLayout) {
        Map<Integer, Integer> previewLayout = new HashMap<>();

        System.out.println("equipped gear is " + equippedItems);
        int i = 0;
        for (Integer itemId : equippedItems) {
            if (itemId == -1) continue;
            previewLayout.put(itemId, toZigZagIndex(i, 0, 0));
            i++;
        }

        // TODO uh oh... weight reducing items.
        // distinct leaves the first duplicate it encounters and removes only duplicates coming after the first.
        System.out.println("inventory is " + inventory);
        inventory = inventory.stream().distinct().collect(Collectors.toList());
        System.out.println("de-duped inventory is " + inventory);
        // Equipped items will never have >11 items in it so it will never spill over into the next row.
        i = 16;
        for (Integer itemId : inventory) {
            if (itemId == -1) continue;
            previewLayout.put(itemId, toZigZagIndex(i, 0, 0));
            i++;
        }

        Optional<Integer> highestUsedIndex = previewLayout.values().stream().max(Integer::compare);
        if (!highestUsedIndex.isPresent()) return previewLayout; // no items in the layout were moved.
        int displacedItemsStart = (highestUsedIndex.get() / 8 + 1) * 8;

        System.out.println("starting at " + displacedItemsStart);
        // TODO items present in the tag but not in the gear or inventory.

        List<Integer> displacedItems = currentLayout.entrySet().stream().filter(e -> {
            System.out.println(e.getValue() + " " + displacedItemsStart + " " + !previewLayout.containsKey(e.getKey()) + " " + (e.getValue() <= displacedItemsStart && !previewLayout.containsKey(e.getKey())));
            return e.getValue() < displacedItemsStart && !previewLayout.containsKey(e.getKey());
        }).map(e -> e.getKey()).collect(Collectors.toList()); // TODO.

        int j = displacedItemsStart;
        while (displacedItems.size() > 0 && j < 2000 / 38 * 8) {
            int finalJ = j;
            if (!currentLayout.values().stream().filter(v -> v == finalJ).findAny().isPresent()) {
                Integer itemId = displacedItems.remove(0);
                System.out.println(itemId + " goes to " + j);
                previewLayout.put(itemId, j);
            } else {
                Map.Entry<Integer, Integer> existingLayoutItem = currentLayout.entrySet().stream().filter(e -> e.getValue() == finalJ).findAny().orElseGet(null);
                if (existingLayoutItem != null) {
                    if (previewLayout.containsKey(existingLayoutItem.getKey())) {
                        Integer itemId = displacedItems.remove(0);
                        System.out.println(itemId + " goes to " + j + " (item was moved)");
                        previewLayout.put(itemId, j);
                    }
                }
            }

            j++;
        }

        // Add existing items not displaced by autolayout to the preview.
        currentLayout.entrySet().stream().filter(e -> e.getValue() >= displacedItemsStart && !previewLayout.containsKey(e.getKey())).forEach(e -> previewLayout.put(e.getKey(), e.getValue()));

        return previewLayout;
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
