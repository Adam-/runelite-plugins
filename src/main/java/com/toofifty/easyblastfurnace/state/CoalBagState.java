package com.toofifty.easyblastfurnace.state;

import lombok.Getter;
import net.runelite.api.ItemID;

import javax.inject.Inject;

public class CoalBagState
{
    private static final int MAX_COAL = 27;
    private static final int MIN_COAL = 0;

    @Inject
    private InventoryState inventory;

    @Inject
    private BankState bank;

    @Getter
    private int coal;

    public void setCoal(int quantity)
    {
        coal = Math.min(Math.max(quantity, MIN_COAL), MAX_COAL);
    }

    public boolean isEmpty()
    {
        return coal == MIN_COAL;
    }

    public boolean isFull()
    {
        return coal == MAX_COAL;
    }

    public void empty()
    {
        if (bank.isOpen()) {
            setCoal(MIN_COAL);
            return;
        }

        setCoal(coal - inventory.getFreeSlots());
    }

    public void fill()
    {
        if (bank.isOpen()) {
            setCoal(MAX_COAL);
            return;
        }

        setCoal(coal + inventory.getQuantity(ItemID.COAL));
    }
}
