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
abstract public class MetalBarMethod extends Method
{
    public abstract int oreItem();

    protected abstract MethodStep withdrawOre();

    protected abstract int barItem();

    protected abstract int coalPer();

    private MethodStep checkPrerequisite(BlastFurnaceState state)
    {
        if (state.getInventoryQuantity(ItemID.COAL_BAG_12019) == 0) {
            return state.isBankOpen() ? withdrawCoalBag : openBank;
        }

        if (state.getInventoryQuantity(ItemID.ICE_GLOVES) == 0 &&
            state.getEquipmentQuantity(ItemID.ICE_GLOVES) == 0) {
            return state.isBankOpen() ? withdrawIceGloves : openBank;
        }

        if (state.getInventoryQuantity(ItemID.ICE_GLOVES) > 0) {
            return equipIceGloves;
        }

        return null;
    }

    @Override
    public MethodStep next(BlastFurnaceState state)
    {
        MethodStep prerequisite = checkPrerequisite(state);
        if (prerequisite != null) return prerequisite;

        if (state.getFurnaceQuantity(BarsOres.COAL) >= coalPer() &&
            state.getFurnaceQuantity(oreItem()) >= 1) {
            return waitForBars;
        }

        if (state.getFurnaceQuantity(barItem()) > 0) {
            return collectBars;
        }

        if (state.isBankOpen()) {
            if (state.getInventoryQuantity(barItem()) > 0) {
                return depositInventory;
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
