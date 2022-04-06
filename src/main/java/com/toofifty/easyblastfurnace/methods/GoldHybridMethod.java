package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.state.BlastFurnaceState;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

abstract public class GoldHybridMethod extends MetalBarMethod
{
    private MethodStep checkPrerequisite(BlastFurnaceState state)
    {
        if (!state.getInventory().has(ItemID.COAL_BAG_12019) &&
            !state.getInventory().has(ItemID.OPEN_COAL_BAG)) {
            return state.getBank().isOpen() ? withdrawCoalBag : openBank;
        }

        if (!state.getInventory().has(ItemID.ICE_GLOVES) &&
            !state.getEquipment().equipped(ItemID.ICE_GLOVES)) {
            return state.getBank().isOpen() ? withdrawIceGloves : openBank;
        }

        if (!state.getInventory().has(ItemID.GOLDSMITH_GAUNTLETS) &&
            !state.getEquipment().equipped(ItemID.GOLDSMITH_GAUNTLETS)) {
            return state.getBank().isOpen() ? withdrawGoldsmithGauntlets : openBank;
        }

        if (!state.getEquipment().equipped(ItemID.ICE_GLOVES) &&
            !state.getEquipment().equipped(ItemID.GOLDSMITH_GAUNTLETS)) {
            return equipGoldsmithGauntlets;
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

        if (state.getInventory().has(ItemID.GOLD_ORE) &&
            !state.getEquipment().equipped(ItemID.GOLDSMITH_GAUNTLETS)) {
            return equipGoldsmithGauntlets;
        }

        if (state.getInventory().has(ItemID.COAL) ||
            state.getInventory().has(ItemID.GOLD_ORE) ||
            state.getInventory().has(oreItem())) {
            return putOntoConveyorBelt;
        }

        if (state.getPlayer().isAtConveyorBelt() &&
            state.getCoalBag().isFull()) {
            return emptyCoalBag;
        }

        if (state.getPlayer().hasLoadedOres()) {
            return waitForBars;
        }

        if (state.getFurnace().has(barItem()) ||
            state.getFurnace().has(ItemID.GOLD_BAR)) {
            if (!state.getEquipment().equipped(ItemID.ICE_GLOVES)) {
                return equipIceGloves;
            }
            return collectBars;
        }

        if (state.getBank().isOpen()) {
            if (state.getInventory().has(barItem()) ||
                state.getInventory().has(ItemID.GOLD_BAR)) {
                return depositInventory;
            }

            if (!state.getCoalBag().isFull()) {
                return state.getCoalBag().isEmpty() ? fillCoalBag : refillCoalBag;
            }

            if (state.getInventory().has(oreItem()) ||
                state.getInventory().has(ItemID.GOLD_ORE)) {
                return putOntoConveyorBelt;
            }

            if (state.getFurnace().getQuantity(ItemID.COAL) < 26 * (coalPer() - 1)) {
                return withdrawGoldOre;
            }

            if (!state.getInventory().has(oreItem())) {
                return withdrawOre();
            }
        }

        return openBank;
    }
}
