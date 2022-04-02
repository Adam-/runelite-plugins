package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.utils.BlastFurnaceState;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.blastfurnace.BarsOres;

/**
 * Represents a basic method for all regular bars (using coal)
 * - Fill coal until threshold
 * - Do trips with ores
 * - Repeat
 */
abstract public class RegularBarMethod extends Method
{
    abstract MethodStep withdrawOre();

    abstract int oreItem();

    abstract int barItem();

    abstract int coalPer();

    @Override
    public MethodStep next(BlastFurnaceState state)
    {
        if (state.getFurnaceQuantity(BarsOres.COAL) >= coalPer() &&
            state.getFurnaceQuantity(oreItem()) >= 1) {
            return waitForBars;
        }

        if (state.getFurnaceQuantity(barItem()) > 0) {
            return collectBars;
        }

        if (state.isBankOpen()) {
            if (state.getInventoryQuantity(ItemID.COAL_BAG_12019) == 0) {
                return withdrawCoalBag;
            }

            if (state.getInventoryQuantity(barItem()) > 0) {
                return depositAllIntoBank;
            }

            if (state.getCoalInCoalBag() == 0) {
                return fillCoalBag;
            }

            if (state.getInventoryQuantity(ItemID.COAL) > 0) {
                return putOntoConveyorBelt;
            }

            if (state.getFurnaceQuantity(BarsOres.COAL) < 27 * (coalPer() - 1)) {
                return withdrawCoal;
            }

            if (state.getInventoryQuantity(oreItem()) == 0) {
                return withdrawOre();
            }
        }

        if (state.getInventoryQuantity(barItem()) > 0) {
            return openBank;
        }

        if (state.getInventoryQuantity(ItemID.COAL_BAG_12019) == 0) {
            return openBank;
        }

        if (state.getInventoryQuantity(ItemID.COAL) > 0) {
            return putOntoConveyorBelt;
        }

        if (state.getInventoryQuantity(oreItem()) > 0) {
            return putOntoConveyorBelt;
        }

        if (state.getCoalInCoalBag() > 0) {
            return emptyCoalBag;
        }

        return openBank;
    }
}
