package com.toofifty.easyblastfurnace.state;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Inject;

public class InventoryState
{
    @Inject
    private Client client;

    private ItemContainer inventory;

    private void load()
    {
        if (inventory == null) {
            inventory = client.getItemContainer(InventoryID.INVENTORY);
        }
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
