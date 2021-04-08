package com.banktaglayouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Layout {

    private Map<Integer, Integer> layoutMap = new HashMap<>();

    public static Layout fromString(String layoutString) {
        Layout layout = Layout.emptyLayout();
        if (layoutString.isEmpty()) return layout;
        for (String s1 : layoutString.split(",")) {
            String[] split = s1.split(":");
            Integer itemId = Integer.valueOf(split[0]);
            Integer index = Integer.valueOf(split[1]);
            if (index >= 0) {
                layout.putItem(itemId, index);
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
            sb.append(integerIntegerEntry.getKey() + ":" + integerIntegerEntry.getValue() + ",");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    public void putItem(int itemId, int index) {
        layoutMap.put(itemId, index);
    }

    public int getItemAtIndex(int index) {
        return allPairs().stream()
                .filter(e -> e.getValue() == index)
                .map(e -> e.getKey())
                .findAny().orElse(-1);
    }

    public Iterator<Map.Entry<Integer, Integer>> allPairsIterator() {
        return layoutMap.entrySet().iterator();
    }

    public Integer getIndexForItem(int itemId) {
        return layoutMap.get(itemId);
    }

    public void setIndexForItem(int itemId, int index) {
        layoutMap.put(itemId, index);
    }

    public Collection<Integer> getAllUsedItemIds() {
        return layoutMap.keySet();
    }

    public Collection<Integer> getAllUsedIndexes() {
        return layoutMap.values();
    }

    public Collection<Map.Entry<Integer, Integer>> allPairs() {
        return layoutMap.entrySet();
    }

    /**
     * removes the item that's currently there if there is one.
     */
    public void removeItem(int itemId) {
        layoutMap.remove(itemId);
    }

    public int getFirstEmptyIndex() {
        List<Integer> indexes = new ArrayList<>(getAllUsedIndexes());
        indexes.sort(Integer::compare);
        int lastIndex = -1;
        for (Integer integer : indexes) {
            if (integer - lastIndex > 1) {
                break;
            }
            lastIndex = integer;
        }
        return lastIndex + 1;
    }

}
