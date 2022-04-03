package com.toofifty.easyblastfurnace.state;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.inject.Inject;

public class BankState
{
    @Inject
    private Client client;

    private ItemContainer bank;

    private void load()
    {
        if (bank == null) {
            bank = client.getItemContainer(InventoryID.BANK);
        }
    }

    public boolean isOpen()
    {
        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return bankContainer != null && !bankContainer.isHidden();
    }

    public int getQuantity(int itemId)
    {
        load();

        return bank.count(itemId);
    }

    public boolean has(int itemId)
    {
        return getQuantity(itemId) > 0;
    }
}
