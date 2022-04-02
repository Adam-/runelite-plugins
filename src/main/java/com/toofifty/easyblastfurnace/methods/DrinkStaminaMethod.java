package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.ItemStep;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.utils.BlastFurnaceState;
import net.runelite.api.ItemID;

public class DrinkStaminaMethod extends Method
{
    private final MethodStep withdrawStaminaPotion1 = new ItemStep(ItemID.STAMINA_POTION1, "Withdraw stamina potion");
    private final MethodStep withdrawStaminaPotion2 = new ItemStep(ItemID.STAMINA_POTION2, "Withdraw stamina potion");
    private final MethodStep withdrawStaminaPotion3 = new ItemStep(ItemID.STAMINA_POTION3, "Withdraw stamina potion");
    private final MethodStep withdrawStaminaPotion4 = new ItemStep(ItemID.STAMINA_POTION4, "Withdraw stamina potion");

    private final MethodStep drinkStaminaPotion1 = new ItemStep(ItemID.STAMINA_POTION1, "Drink stamina potion");
    private final MethodStep drinkStaminaPotion2 = new ItemStep(ItemID.STAMINA_POTION2, "Drink stamina potion");
    private final MethodStep drinkStaminaPotion3 = new ItemStep(ItemID.STAMINA_POTION3, "Drink stamina potion");
    private final MethodStep drinkStaminaPotion4 = new ItemStep(ItemID.STAMINA_POTION4, "Drink stamina potion");

    private final MethodStep getMoreStaminaPotions = new ItemStep(ItemID.COAL_BAG_12019, "Get more stamina potions! Check settings to disable this");

    @Override
    public MethodStep next(BlastFurnaceState state)
    {
        if (!state.needsStaminaDose() && state.getInventoryQuantity(new int[]{
            ItemID.VIAL,
            ItemID.STAMINA_POTION1,
            ItemID.STAMINA_POTION2,
            ItemID.STAMINA_POTION3,
        }) > 0) {
            return depositInventory;
        }

        if (!state.isBankOpen() || !state.needsStaminaDose()) return null;

        if (state.getFreeInventorySlots() == 0) {
            return depositInventory;
        }

        if (state.getInventoryQuantity(ItemID.STAMINA_POTION1) > 0) {
            return drinkStaminaPotion1;
        }

        if (state.getInventoryQuantity(ItemID.STAMINA_POTION2) > 0) {
            return drinkStaminaPotion2;
        }

        if (state.getInventoryQuantity(ItemID.STAMINA_POTION3) > 0) {
            return drinkStaminaPotion3;
        }

        if (state.getInventoryQuantity(ItemID.STAMINA_POTION4) > 0) {
            return drinkStaminaPotion4;
        }

        if (state.getBankQuantity(ItemID.STAMINA_POTION1) > 0) {
            return withdrawStaminaPotion1;
        }

        if (state.getBankQuantity(ItemID.STAMINA_POTION2) > 0) {
            return withdrawStaminaPotion2;
        }

        if (state.getBankQuantity(ItemID.STAMINA_POTION3) > 0) {
            return withdrawStaminaPotion3;
        }

        if (state.getBankQuantity(ItemID.STAMINA_POTION4) > 0) {
            return withdrawStaminaPotion4;
        }

        return getMoreStaminaPotions;
    }

    @Override
    public String getName()
    {
        return null;
    }
}
