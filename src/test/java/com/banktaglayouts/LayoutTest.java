package com.banktaglayouts;

public class LayoutTest {

    // 1 or 2 digit numbers shall be non-variant item ids.
    // 3 digit numbers shall be variant item ids where the hundreds place indicates which base id it belongs to.
    // a 1 in the thousands place indicates a placeholder.

    private static boolean isVariantItem(int itemId) {
        if (itemId == -1) return false;

        int tens = itemId % 100;
        int variantCategory = itemId % 1000 / 100;
        return (variantCategory != 0);
    }

    private static boolean isPlaceholder(int itemId) {
        if (itemId == -1) return false;

        return itemId % 10000 / 1000 == 1;
    }

    public void testLayoutEquality() {
        Layout layout1 = fromPrettyString(
                1, 2, null, 3, null, null, null, null,
                null, 101, 102, 100
        );
        Layout layout2 = fromPrettyString(
                1, 2, null, 3, null, null, null, null,
                null, 101, 102, 100
        );
        assert layout1.equals(layout2);
    }

    public static Layout fromPrettyString(Integer... ids) {
        Layout layout = Layout.emptyLayout();
        int index = 0;
        for (Integer id : ids) {
            layout.putItem(id, index++);
        }
        return layout;
    }

}
