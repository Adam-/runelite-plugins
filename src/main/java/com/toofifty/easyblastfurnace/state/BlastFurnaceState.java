package com.toofifty.easyblastfurnace.state;

import lombok.Getter;
import net.runelite.api.ItemID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Getter
@Singleton
public class BlastFurnaceState
{
    @Inject
    private CoalBagState coalBag;

    @Inject
    private InventoryState inventory;

    @Inject
    private EquipmentState equipment;

    @Inject
    private PlayerState player;

    @Inject
    private FurnaceState furnace;

    @Inject
    private BankState bank;

    public void update()
    {
        if (player.isAtConveyorBelt() &&
            (inventory.hasChanged(ItemID.GOLD_ORE) ||
                inventory.hasChanged(ItemID.IRON_ORE) ||
                inventory.hasChanged(ItemID.MITHRIL_ORE) ||
                inventory.hasChanged(ItemID.ADAMANTITE_ORE) ||
                inventory.hasChanged(ItemID.RUNITE_ORE))) {
            player.hasLoadedOres(true);
        }

        if (furnace.has(ItemID.GOLD_BAR) ||
            furnace.has(ItemID.STEEL_BAR) ||
            furnace.has(ItemID.MITHRIL_BAR) ||
            furnace.has(ItemID.ADAMANTITE_BAR) ||
            furnace.has(ItemID.RUNITE_BAR)) {
            player.hasLoadedOres(false);
        }

        inventory.update();
        furnace.update();
    }
}
