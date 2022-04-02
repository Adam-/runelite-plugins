package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.utils.BlastFurnaceState;
import net.runelite.api.ItemID;

public class GoldBarMethod extends Method
{
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

        if (state.getFurnaceQuantity(ItemID.GOLD_BAR) > 0) {
            if (state.getEquipmentQuantity(ItemID.ICE_GLOVES) == 0) {
                return equipIceGloves;
            }
            return collectBars;
        }

        if (state.getEquipmentQuantity(ItemID.GOLDSMITH_GAUNTLETS) == 0) {
            return equipGoldsmithGauntlets;
        }

        if (state.isBankOpen()) {
            if (state.getInventoryQuantity(ItemID.GOLD_BAR) > 0) {
                return depositInventory;
            }

            if (state.getInventoryQuantity(ItemID.GOLD_ORE) == 0) {
                return withdrawGoldOre;
            }
        }

        if (state.getInventoryQuantity(ItemID.GOLD_ORE) > 0) {
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
