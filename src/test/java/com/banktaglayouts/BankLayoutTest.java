package com.banktaglayouts;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.TabInterface;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class BankLayoutTest
{
    @Mock
    @Bind
    private Client client;

    @Mock
    @Bind
    private ConfigManager configManager;

    @Mock
    @Bind
    private ItemManager itemManager;

    @Mock
    @Bind
    private OverlayManager overlayManager;

    @Mock
    @Bind
    private BankTagLayoutsConfig config;

    @Mock
    @Bind
    private FakeItemOverlay fakeItemOverlay;

    @Mock
    @Bind
    private MouseManager mouseManager;

    @Mock
    @Bind
    private KeyManager keyManager;

    @Mock
    @Bind
    private SpriteManager spriteManager;

    @Mock
    @Bind
    private ClientThread clientThread;

    @Mock
    @Bind
    private TabInterface tabInterface;

    @Mock
    @Bind
    private TagManager tagManager;

    @Mock
    @Bind
    private BankSearch bankSearch;

    @Mock
    @Bind
    private ChatboxPanelManager chatboxPanelManager;

//    @Mock
//    @Bind
//    private RuneLiteConfig runeliteConfig;
//
    @Inject
    private BankTagLayoutsPlugin plugin;

    @Before
    public void before()
    {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
//        plugin = Mockito.spy(plugin);
    }

    private static final Item GAMES_NECKLACE_8 = new Item("game-necklace-8", null);
    private static final Item GAMES_NECKLACE_7 = new Item("game-necklace-7", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_6 = new Item("game-necklace-6", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_5 = new Item("game-necklace-5", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_4 = new Item("game-necklace-4", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_3 = new Item("game-necklace-3", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_2 = new Item("game-necklace-2", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_1 = new Item("game-necklace-1", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_1_PH = new Item("game-necklace-1 (ph)", GAMES_NECKLACE_8);
    private static final Item GAMES_NECKLACE_8_PH = new Item("game-necklace-8 (ph)", GAMES_NECKLACE_8);

    @Test
    public void testLayout() {
        Layout layout = generateLayout(
                new LayoutItem(GAMES_NECKLACE_8, 1),
                new LayoutItem(GAMES_NECKLACE_1, 8),
                new LayoutItem(GAMES_NECKLACE_1_PH, 9)
        );
        List<Widget> bankItems = Arrays.asList(
                createBankItemWidget(GAMES_NECKLACE_8, 70),
                createBankItemWidget(GAMES_NECKLACE_1_PH)
        );

        Map<Integer, BankSlot> laidOutBank = layOutBank(layout, bankItems);
        Map<Integer, BankSlot> expectedLaidOutBank = new HashMap<>();

        expectedLaidOutBank.put(1, BankSlot.realItem(GAMES_NECKLACE_8, 70));
        expectedLaidOutBank.put(8, BankSlot.layoutPlaceholder(GAMES_NECKLACE_1.id));
        expectedLaidOutBank.put(9, BankSlot.realItem(GAMES_NECKLACE_1_PH));

        assertEquals(expectedLaidOutBank, laidOutBank);

        plugin.duplicateItem(8);

    }

    public static class BankSlot {
        enum Type {
            REAL_ITEM, DUPLICATE_ITEM, LAYOUT_PLACEHOLDER
        }

        private final Type type;
        private final Widget widget;
        private final int id;
        private final int quantity;

        private BankSlot(Type type, int id, Widget widget, int quantity) {
            this.type = type;
            this.widget = widget;
            this.id = id;
            this.quantity = quantity;
        }

        public static BankSlot realItem(Item item) {
            int quantity = item.placeholder ? 0 : 1;
            return realItem(item, quantity);
        }

        public static BankSlot realItem(Item item, int quantity) {
            return new BankSlot(Type.REAL_ITEM, item.id, null, quantity);
        }

        public static BankSlot realItem(Widget widget) {
            return new BankSlot(Type.REAL_ITEM, -1, widget, widget.getItemQuantity());
        }

        // TODO what if I have a 1-dose ppot as a fake with duplicates - will a 4-dose fill all the spots of that?

        // TODO what if i create a duplicate when the wrong item is in a fake item? e.g. 1-dose ppot is in layout but there is a 4-dose appearing there, and I then duplicate that?

        public static BankSlot duplicateItem(int itemId, int quantity) {
            return new BankSlot(Type.DUPLICATE_ITEM, itemId, null, quantity);
        }

        public static BankSlot layoutPlaceholder(int itemId) {
            return new BankSlot(Type.LAYOUT_PLACEHOLDER, itemId, null, -1);
        }
    }

    private Widget createBankItemWidget(Item item)
    {
        return createBankItemWidget(item, item.placeholder ? 0 : 1);
    }

    private Widget createBankItemWidget(Item item, int quantity)
    {
        Widget widget = Mockito.mock(Widget.class);
        Mockito.when(widget.getItemId()).thenReturn(item.id);
        Mockito.when(widget.getItemQuantity()).thenReturn(quantity);
        return widget;
    }

    private Map<Integer, BankSlot> layOutBank(Layout layout, List<Widget> bankItems)
    {
        Map<Integer, BankSlot> laidOutBank = new HashMap<>();

        Map<Integer, Widget> indexToWidget = plugin.assignItemPositions(layout, bankItems);

        for (Map.Entry<Integer, Widget> entry : indexToWidget.entrySet())
        {
            assertFalse(laidOutBank.containsKey(entry.getKey()));
            laidOutBank.put(entry.getKey(), BankSlot.realItem(entry.getValue()));
        }

        Set<BankTagLayoutsPlugin.FakeItem> fakeItems = plugin.calculateFakeItems(layout, indexToWidget);

        for (BankTagLayoutsPlugin.FakeItem fakeItem : fakeItems)
        {
            assertFalse(laidOutBank.containsKey(fakeItem.getIndex()));
            laidOutBank.put(
                    fakeItem.getIndex(),
                    fakeItem.layoutPlaceholder ?
                            BankSlot.layoutPlaceholder(fakeItem.getItemId()) :
                            BankSlot.duplicateItem(fakeItem.getItemId(), fakeItem.getQuantity())
            );
        }

        return laidOutBank;
    }

    private static class Item {
        int id;
        boolean placeholder;
        @Nullable
        Item variantClass;

        public Item(String uniqueName) {
            this(uniqueName, null);
        }

        public Item(String uniqueName, Item variantClass) {
            if (uniqueName.endsWith(" (ph)")) {
                uniqueName = uniqueName.substring(0, uniqueName.length() - " (ph)".length());
                this.placeholder = true;
            } else if (uniqueName.endsWith("(ph")) {
                uniqueName = uniqueName.substring(0, uniqueName.length() - "(ph)".length());
                this.placeholder = true;
            } else {
                this.id = Math.abs(uniqueName.hashCode());
                this.placeholder = false;
            }

            System.out.println("name is \"" + uniqueName + "\"");
            this.id = Math.abs(uniqueName.hashCode());
            this.variantClass = variantClass;
        }

        public Item(int itemId, boolean placeholder, Item variantClass) {
            this.id = itemId;
            this.placeholder = placeholder;
            this.variantClass = variantClass;
        }
    }

    private static class LayoutItem {
        final Item item;
        final int index;

        public LayoutItem(Item item, int index) {
            this.item = item;
            this.index = index;
        }
    }

    private Layout generateLayout(LayoutItem... layoutItems) {
        Layout layout = new Layout();
        for (LayoutItem layoutItem : layoutItems)
        {
            layout.putItem(layoutItem.item.id, layoutItem.index);
        }
        return layout;
    }

}
