package com.banktaglayouts;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class Layout {

    // Maps indexes to items.
    private Map<Integer, Integer> layoutMap = new HashMap<>();

    public static Layout fromString(String layoutString) {
        return fromString(layoutString, false);
    }

    public static Layout fromString(String layoutString, boolean ignoreNfe) {
        Layout layout = Layout.emptyLayout();
        if (layoutString.isEmpty()) return layout;
        for (String s1 : layoutString.split(",")) {
            String[] split = s1.split(":");
            try {
                Integer itemId = Integer.valueOf(split[0]);
                Integer index = Integer.valueOf(split[1]);
                if (index >= 0) {
                    layout.putItem(itemId, index);
                } else {
//                    log.debug("Removed item " + itemName(itemId) + " (" + itemId + ") due to it having a negative index (" + index + ")");
                    log.debug("Removed item " + itemId + " due to it having a negative index (" + index + ")");
                }
            } catch (NumberFormatException e) {
                if (!ignoreNfe) throw e;
                log.debug("input string \"" + layoutString + "\"");
            }
        }
        return layout;
    }

    public static Layout emptyLayout() {
        return new Layout();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> integerIntegerEntry : allPairs()) {
            sb.append(integerIntegerEntry.getValue() + ":" + integerIntegerEntry.getKey() + ",");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    public void putItem(int itemId, int index) {
        layoutMap.put(index, itemId);
    }

    /** returns -1 if there is no item there. */
    public int getItemAtIndex(int index) {
        return layoutMap.getOrDefault(index, -1);
    }

    public Iterator<Map.Entry<Integer, Integer>> allPairsIterator() {
        return layoutMap.entrySet().iterator();
    }

    /** finds the index for the EXACT itemId. Does not factor in placeholders or variation items. */
    public Integer getIndexForItem(int itemId) {
        return allPairs().stream()
                .filter(e -> e.getValue() == itemId)
                .map(e -> e.getKey())
                .findAny().orElse(-1);
    }

    // TODO is this supposed to move the old position of the item?
    public void setIndexForItem(int itemId, int index) {
        layoutMap.put(index, itemId);
    }

    public Collection<Integer> getAllUsedItemIds() {
        return new HashSet<>(layoutMap.values());
    }

    public Collection<Integer> getAllUsedIndexes() {
        return layoutMap.keySet();
    }

    public Collection<Map.Entry<Integer, Integer>> allPairs() {
        return layoutMap.entrySet();
    }

    public int getFirstEmptyIndex() {
        return getFirstEmptyIndex(-1);
    }

    public int getFirstEmptyIndex(int afterThisIndex) {
        List<Integer> indexes = new ArrayList<>(getAllUsedIndexes());
        indexes.sort(Integer::compare);
        for (Integer integer : indexes) {
            if (integer < afterThisIndex) continue;

            if (integer - afterThisIndex > 1) {
                break;
            }
            afterThisIndex = integer;
        }
        return afterThisIndex + 1;
    }

    public void clearIndex(int index) {
        layoutMap.remove(index);
    }

    public void swapIndexes(int index1, int index2, Integer index1PreferredId, Integer index2PreferredId) {
        if (index1PreferredId == null) {
            index1PreferredId = getItemAtIndex(index1);
        }
        if (index2PreferredId == null) {
            index2PreferredId = getItemAtIndex(index2);
        }

        clearIndex(index2);
        clearIndex(index1);
        if (index1PreferredId != null && index1PreferredId != -1) putItem(index1PreferredId, index2);
        if (index2PreferredId != null && index2PreferredId != -1) putItem(index2PreferredId, index1);
    }

    public boolean isEmpty()
    {
        return layoutMap.isEmpty();
    }

    public int countItemsWithId(int idAtIndex, int placeholderIdAtIndex)
    {
        int count = 0;
        for (Map.Entry<Integer, Integer> pair : allPairs())
        {
            if (pair.getValue() == idAtIndex || pair.getValue() == placeholderIdAtIndex) {
                count++;
            }
        }
        return count;
    }
}
