package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.state.BlastFurnaceState;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

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
        if (!state.getInventory().has(ItemID.COAL_BAG_12019)) {
            return state.getBank().isOpen() ? withdrawCoalBag : openBank;
        }

        if (!state.getInventory().has(ItemID.ICE_GLOVES) &&
            !state.getEquipment().equipped(ItemID.ICE_GLOVES)) {
            return state.getBank().isOpen() ? withdrawIceGloves : openBank;
        }

        if (state.getInventory().has(ItemID.ICE_GLOVES)) {
            return equipIceGloves;
        }

        return null;
    }

    @Override
    public MethodStep next(BlastFurnaceState state)
    {
        MethodStep prerequisite = checkPrerequisite(state);
        if (prerequisite != null) return prerequisite;

        if (state.getInventory().has(ItemID.COAL) ||
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

        if (state.getFurnace().has(barItem())) {
            return collectBars;
        }

        if (state.getBank().isOpen()) {
            if (state.getInventory().has(barItem())) {
                return depositInventory;
            }

            if (state.getCoalBag().isEmpty()) {
                return fillCoalBag;
            }

            if (state.getInventory().has(ItemID.COAL)) {
                return putOntoConveyorBelt;
            }

            if (state.getFurnace().getQuantity(ItemID.COAL) < 27 * (coalPer() - 1)) {
                return withdrawCoal;
            }

            if (!state.getInventory().has(oreItem())) {
                return withdrawOre();
            }
        }

        if (state.getInventory().has(barItem())) {
            return openBank;
        }

        if (!state.getInventory().has(ItemID.COAL_BAG_12019)) {
            return openBank;
        }

        return openBank;
    }
}
