package com.banktaglayouts;

import com.banktaglayouts.invsetupsstuff.InventorySetup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
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
        return basicLayout(equippedItems, inventory, Collections.emptyList(), Collections.emptyList(), currentLayout);
    }

    public Map<Integer, Integer> basicLayout(List<Integer> equippedItems, List<Integer> inventory, List<Integer> runePouch, List<Integer> additionalItems, Map<Integer, Integer> currentLayout) {
        Map<Integer, Integer> previewLayout = new HashMap<>();
        List<Integer> displacedItems = new ArrayList<>();

        log.debug("generate layout");
        log.debug("equipped gear is " + equippedItems);
        log.debug("inventory is " + inventory);

        int i = 0;

        // lay out equipped items.
        equippedItems = equippedItems.stream()
                .distinct()
                .map(itemId -> plugin.itemManager.canonicalize(itemId)) // Weight reducing items have different ids when equipped; this fixes that.
                .collect(Collectors.toList());
        i = layoutItems(equippedItems, currentLayout, previewLayout, displacedItems, i, true);

        // lay out the inventory items.
        // distinct leaves the first duplicate it encounters and removes only duplicates coming after the first.
        inventory = inventory.stream().distinct().collect(Collectors.toList());
        i = layoutItems(inventory, currentLayout, previewLayout, displacedItems, i, true);

        i = layoutItems(runePouch, currentLayout, previewLayout, displacedItems, i, false);

        i = layoutItems(additionalItems, currentLayout, previewLayout, displacedItems, i, false);

        int displacedItemsStart = i;

        // copy items from current layout into the empty spots.
        for (Map.Entry<Integer, Integer> itemPosition : currentLayout.entrySet()) {
            int index = itemPosition.getValue();
            int currentItemAtIndex = itemPosition.getKey();
            int previewItemAtIndex = getItemAtIndex(index, previewLayout);

            if (currentItemAtIndex != -1 && previewItemAtIndex == -1 && !layoutContainsItem(currentItemAtIndex, previewLayout)) {
                previewLayout.put(currentItemAtIndex, index);
            }
        }

        // Remove items that were placed as part of the gear or inventory.
        displacedItems = displacedItems.stream().filter(id -> !layoutContainsItem(id, previewLayout)).collect(Collectors.toList());

        int j = displacedItemsStart;
        while (displacedItems.size() > 0 && j < 2000 / 38 * 8) {
            int currentItemAtIndex = getItemAtIndex(j, currentLayout);
            if (currentItemAtIndex == -1 || listContainsItem(currentItemAtIndex, equippedItems) || listContainsItem(currentItemAtIndex, inventory)) {
                Integer itemId = displacedItems.remove(0);
                log.debug(itemId + " goes to " + j);
                previewLayout.put(itemId, j);
            }

            j++;
        }

        return previewLayout;
    }

    private int layoutItems(List<Integer> inventory, Map<Integer, Integer> currentLayout, Map<Integer, Integer> previewLayout, List<Integer> displacedItems, int i, boolean useZigZag) {
        for (Integer itemId : inventory) {
            if (itemId == -1) continue;
            int index = useZigZag ? toZigZagIndex(i, 0, 0) : i;
            previewLayout.put(itemId, index);
            int currentLayoutItem = getItemAtIndex(index, currentLayout);
            if (currentLayoutItem != -1) displacedItems.add(currentLayoutItem);
            i++;
        }
        if (!inventory.isEmpty()) {
            Optional<Integer> highestUsedIndex = previewLayout.values().stream().max(Integer::compare);
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
        List<Integer> equippedGear = inventorySetup.getEquipment() == null ? Collections.emptyList() : inventorySetup.getEquipment().stream().map(isi -> isi.getId()).collect(Collectors.toList());
        List<Integer> inventory = inventorySetup.getInventory() == null ? Collections.emptyList() : inventorySetup.getInventory().stream().map(isi -> isi.getId()).collect(Collectors.toList());
        List<Integer> runePouchRunes = inventorySetup.getRune_pouch() == null ? Collections.emptyList() : inventorySetup.getRune_pouch().stream().map(isi -> isi.getId()).collect(Collectors.toList());
        List<Integer> additionalItems = inventorySetup.getAdditionalFilteredItems() == null ? Collections.emptyList() : inventorySetup.getAdditionalFilteredItems().entrySet().stream().map(isi -> isi.getValue().getId()).collect(Collectors.toList());

        return basicLayout(equippedGear, inventory, runePouchRunes, additionalItems, currentLayout);
    }
}
