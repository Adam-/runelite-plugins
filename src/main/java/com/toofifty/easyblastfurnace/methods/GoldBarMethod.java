package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.state.BlastFurnaceState;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class GoldBarMethod extends Method
{
    private MethodStep checkPrerequisite(BlastFurnaceState state)
    {
        // ensure player has both ice gloves & goldsmith gauntlets either in inventory or equipped

        if (!state.getInventory().has(ItemID.ICE_GLOVES) &&
            !state.getEquipment().equipped(ItemID.ICE_GLOVES)) {
            return state.getBank().isOpen() ? withdrawIceGloves : openBank;
        }

        if (!state.getInventory().has(ItemID.GOLDSMITH_GAUNTLETS) &&
            !state.getEquipment().equipped(ItemID.GOLDSMITH_GAUNTLETS)) {
            return state.getBank().isOpen() ? withdrawGoldsmithGauntlets : openBank;
        }

        return null;
    }

    @Override
    public MethodStep next(BlastFurnaceState state)
    {
        MethodStep prerequisite = checkPrerequisite(state);
        if (prerequisite != null) return prerequisite;

        if (state.getFurnace().has(ItemID.GOLD_BAR)) {
            if (!state.getEquipment().equipped(ItemID.ICE_GLOVES)) {
                return equipIceGloves;
            }
            return collectBars;
        }

        if (!state.getEquipment().equipped(ItemID.GOLDSMITH_GAUNTLETS)) {
            return equipGoldsmithGauntlets;
        }

        if (state.getBank().isOpen()) {
            if (state.getInventory().has(ItemID.GOLD_BAR)) {
                return depositInventory;
            }

            if (!state.getInventory().has(ItemID.GOLD_ORE)) {
                return withdrawGoldOre;
            }
        }

        if (state.getInventory().has(ItemID.GOLD_ORE)) {
            return putOntoConveyorBelt;
        }

        return openBank;
    }

    @Override
    public String getName()
    {
        return "Gold bars";
    }
}
