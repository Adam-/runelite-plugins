package com.toofifty.easyblastfurnace.state;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;

import javax.inject.Inject;

public class EquipmentState
{
    @Inject
    private Client client;

    private ItemContainer equipment;

    private void load()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment != null) {
            this.equipment = equipment;
        }
    }

    public boolean equipped(int itemId)
    {
        load();

        return equipment.count(itemId) > 0;
    }
}
