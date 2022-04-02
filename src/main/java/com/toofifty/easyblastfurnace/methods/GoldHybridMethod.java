package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.utils.BlastFurnaceState;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.blastfurnace.BarsOres;

abstract public class GoldHybridMethod extends Method
{
    abstract MethodStep withdrawOre();

    abstract int oreItem();

    abstract int barItem();

    abstract int coalPer();

    private MethodStep checkPrerequisite(BlastFurnaceState state)
    {
        // ensure player has both ice gloves & goldsmith gauntlets either in inventory or equipped

        if (state.getInventoryQuantity(ItemID.ICE_GLOVES) == 0 &&
            state.getEquipmentQuantity(ItemID.ICE_GLOVES) == 0) {
            return state.isBankOpen() ? withdrawIceGloves : openBank;
        }

        if (state.getInventoryQuantity(ItemID.GOLDSMITH_GAUNTLETS) == 0 &&
            state.getEquipmentQuantity(ItemID.GOLDSMITH_GAUNTLETS) == 0) {
            return state.isBankOpen() ? withdrawGoldsmithGauntlets : openBank;
        }

        return null;
    }

    @Override
    public MethodStep next(BlastFurnaceState state)
    {
        MethodStep prerequisite = checkPrerequisite(state);
        if (prerequisite != null) return prerequisite;

        // continue doing gold bars until enough coal has been deposited
        // then do one trip of metal bars

        if (state.getInventoryQuantity(ItemID.COAL) > 0) {
            return putOntoConveyorBelt;
        }

        if (state.getFurnaceQuantity(BarsOres.COAL) >= coalPer() &&
            state.getFurnaceQuantity(oreItem()) >= 1) {
            if (state.getEquipmentQuantity(ItemID.ICE_GLOVES) == 0) {
                return equipIceGloves;
            }
            return waitForBars;
        }

        if (state.getFurnaceQuantity(barItem()) > 0 ||
            state.getFurnaceQuantity(ItemID.GOLD_BAR) > 0) {
            if (state.getEquipmentQuantity(ItemID.ICE_GLOVES) == 0) {
                return equipIceGloves;
            }
            return collectBars;
        }

        if (state.getInventoryQuantity(ItemID.GOLD_ORE) > 0 &&
            state.getEquipmentQuantity(ItemID.GOLDSMITH_GAUNTLETS) == 0) {
            return equipGoldsmithGauntlets;
        }

        if (state.isBankOpen()) {
            if (state.getInventoryQuantity(barItem()) > 0 ||
                state.getInventoryQuantity(ItemID.GOLD_BAR) > 0) {
                return depositInventory;
            }

            if (state.getCoalInCoalBag() <= 1) {
                return state.getCoalInCoalBag() > 0 ? refillCoalBag : fillCoalBag;
            }

            if (state.getInventoryQuantity(oreItem()) > 0 ||
                state.getInventoryQuantity(ItemID.GOLD_ORE) > 0) {
                return putOntoConveyorBelt;
            }

            if (state.getFurnaceQuantity(BarsOres.COAL) < 26 * (coalPer() - 1)) {
                return withdrawGoldOre;
            }

            if (state.getInventoryQuantity(oreItem()) == 0) {
                return withdrawOre();
            }
        }

        if (state.getInventoryQuantity(barItem()) > 0 ||
            state.getInventoryQuantity(ItemID.GOLD_BAR) > 0) {
            return openBank;
        }

        if (state.getInventoryQuantity(ItemID.COAL_BAG_12019) == 0) {
            return openBank;
        }

        if (state.getInventoryQuantity(oreItem()) > 0 ||
            state.getInventoryQuantity(ItemID.GOLD_ORE) > 0) {
            return putOntoConveyorBelt;
        }

        if (state.getCoalInCoalBag() > 1) {
            return emptyCoalBag;
        }

        return openBank;
    }
}
