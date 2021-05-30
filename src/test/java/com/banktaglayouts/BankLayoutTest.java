package com.banktaglayouts;

import com.google.inject.testing.fieldbinder.Bind;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BankLayoutTest
{

    @Mock
    @Bind
    private BankTagLayoutsPlugin plugin;

    public static class BankSlot {
        enum Type {
            REAL_ITEM, DUPLICATE_ITEM, LAYOUT_PLACEHOLDER
        }
        private final Type type;
        private final Widget widget;
        private final int id;

        private BankSlot(Type type, int id, Widget widget) {
           this.type = type;
           this.widget = widget;
           this.id = id;
        }

        public static BankSlot realItem(Widget widget) {
            return new BankSlot(Type.REAL_ITEM, -1, widget);
        }

        // TODO what if I have a 1-dose ppot as a fake with duplicates - will a 4-dose fill all the spots of that?

        // TODO what if i create a duplicate when the wrong item is in a fake item? e.g. 1-dose ppot is in layout but there is a 4-dose appearing there, and I then duplicate that?

        public static BankSlot duplicateItem(int itemId) {
            return new BankSlot(Type.DUPLICATE_ITEM, itemId, null);
        }

        public static BankSlot realItem(int itemId) {
            return new BankSlot(Type.LAYOUT_PLACEHOLDER, itemId, null);
        }
    }

    @Test
    public void testLayout() {
        Layout layout = null;
        List<Widget> bankItems = null;

        Map<Integer, Widget> indexToWidget = plugin.assignItemPositions(layout, bankItems);
        Set<BankTagLayoutsPlugin.FakeItem> fakeItems = plugin.calculateFakeItems(layout, indexToWidget);
    }

}
