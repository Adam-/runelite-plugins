package com.toofifty.easyblastfurnace.utils;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.blastfurnace.BarsOres;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BlastFurnaceState
{
    private static final int MAX_COAL = 27;
    private static final int MIN_COAL = 0;

    @Inject
    private Client client;

    @Getter
    private int coalInCoalBag = 0;

    @Getter
    @Setter
    private int previousCoalInInventory = 0;

    public boolean isBankOpen()
    {
        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return bankContainer != null && !bankContainer.isHidden();
    }

    public int getInventoryQuantity(int itemId)
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        return inventory == null ? 0 : inventory.count(itemId);
    }

    public int getFurnaceQuantity(BarsOres varbit)
    {
        return client.getVar(varbit.getVarbit());
    }

    private void setCoalInCoalBag(int amount)
    {
        coalInCoalBag = Math.min(Math.max(amount, MIN_COAL), MAX_COAL);
    }

    private int getFreeInventorySlots()
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        assert inventory != null;

        int freeSlots = 28;
        for (Item item : inventory.getItems()) {
            if (item.getQuantity() > 0) {
                freeSlots--;
            }
        }
        return freeSlots;
    }

    public void emptyCoalBag()
    {
        if (isBankOpen()) {
            setCoalInCoalBag(0);
            return;
        }

        setCoalInCoalBag(coalInCoalBag - getFreeInventorySlots());
    }

    public void fillCoalBag()
    {
        if (isBankOpen()) {
            setCoalInCoalBag(27);
            return;
        }

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        assert inventory != null;

        setCoalInCoalBag(coalInCoalBag + inventory.count(ItemID.COAL));
    }
}
