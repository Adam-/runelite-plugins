package com.toofifty.easyblastfurnace.state;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;

public class InventoryState
{
    @Inject
    private Client client;

    private ItemContainer inventory;

    private Item[] previousInventory = new Item[]{};

    private void load()
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory != null) {
            this.inventory = inventory;
        }
    }

    private int getPreviousQuantity(int itemId)
    {
        Optional<Item> item = Arrays.stream(previousInventory).filter(i -> i.getId() == itemId).findFirst();

        return item.map(Item::getQuantity).orElse(0);
    }

    public void update()
    {
        load();

        if (inventory != null) {
            previousInventory = inventory.getItems().clone();
        }
    }

    public int getChange(int itemId)
    {
        return getQuantity(itemId) - getPreviousQuantity(itemId);
    }

    public boolean hasChanged(int itemId)
    {
        return getChange(itemId) != 0;
    }

    public int getQuantity(int itemId)
    {
        load();

        return inventory.count(itemId);
    }

    public boolean has(int itemId)
    {
        return getQuantity(itemId) > 0;
    }

    public int getFreeSlots()
    {
        load();

        int freeSlots = 28;
        for (Item item : inventory.getItems()) {
            if (item.getQuantity() > 0) {
                freeSlots--;
            }
        }
        return freeSlots;
    }

    public boolean hasFreeSlots()
    {
        return getFreeSlots() > 0;
    }
}
