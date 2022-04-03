package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.state.BlastFurnaceState;
import com.toofifty.easyblastfurnace.steps.ItemStep;
import com.toofifty.easyblastfurnace.steps.MethodStep;
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
        if (!state.getPlayer().needsStamina() &&
            (state.getInventory().has(ItemID.VIAL) ||
                state.getInventory().has(ItemID.STAMINA_POTION1) ||
                state.getInventory().has(ItemID.STAMINA_POTION2) ||
                state.getInventory().has(ItemID.STAMINA_POTION3))) {
            return depositInventory;
        }

        if (!state.getBank().isOpen() || !state.getPlayer().needsStamina()) return null;

        if (!state.getInventory().hasFreeSlots()) {
            return depositInventory;
        }

        if (state.getInventory().has(ItemID.STAMINA_POTION1)) {
            return drinkStaminaPotion1;
        }

        if (state.getInventory().has(ItemID.STAMINA_POTION2)) {
            return drinkStaminaPotion2;
        }

        if (state.getInventory().has(ItemID.STAMINA_POTION3)) {
            return drinkStaminaPotion3;
        }

        if (state.getInventory().has(ItemID.STAMINA_POTION4)) {
            return drinkStaminaPotion4;
        }

        if (state.getBank().has(ItemID.STAMINA_POTION1)) {
            return withdrawStaminaPotion1;
        }

        if (state.getBank().has(ItemID.STAMINA_POTION2)) {
            return withdrawStaminaPotion2;
        }

        if (state.getBank().has(ItemID.STAMINA_POTION3)) {
            return withdrawStaminaPotion3;
        }

        if (state.getBank().has(ItemID.STAMINA_POTION4)) {
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
