package com.toofifty.easyblastfurnace.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.methods.*;
import com.toofifty.easyblastfurnace.overlays.EasyBlastFurnaceCoalBagOverlay;
import com.toofifty.easyblastfurnace.overlays.EasyBlastFurnaceInstructionOverlay;
import com.toofifty.easyblastfurnace.overlays.EasyBlastFurnaceItemStepOverlay;
import com.toofifty.easyblastfurnace.overlays.EasyBlastFurnaceObjectStepOverlay;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.Item;
import net.runelite.api.ItemID;

@Singleton
public class MethodInstructor
{
    @Inject
    private EasyBlastFurnaceInstructionOverlay instructionOverlay;

    @Inject
    private EasyBlastFurnaceItemStepOverlay itemStepOverlay;

    @Inject
    private EasyBlastFurnaceObjectStepOverlay objectStepOverlay;

    @Inject
    private EasyBlastFurnaceCoalBagOverlay coalBagOverlay;

    @Inject
    private EasyBlastFurnaceConfig config;

    @Inject
    private BlastFurnaceState state;

    private final DrinkStaminaMethod drinkStaminaMethod = new DrinkStaminaMethod();

    private Method currentMethod;

    public void next()
    {
        if (currentMethod == null) return;

        MethodStep step = drinkStaminaMethod.next(state);
        if (step == null) step = currentMethod.next(state);

        instructionOverlay.setStep(step);
        itemStepOverlay.setStep(step);
        objectStepOverlay.setStep(step);
    }

    public void reset()
    {
        currentMethod = null;
        instructionOverlay.setStep(null);
        itemStepOverlay.setStep(null);
        objectStepOverlay.setStep(null);
    }

    public void setMethodFromInventory(Item[] items)
    {
        for (Item item : items) {
            switch (item.getId()) {
                case ItemID.IRON_ORE:
                    currentMethod = new SteelBarMethod();
                    break;

                case ItemID.MITHRIL_ORE:
                    currentMethod = new MithrilBarMethod();
                    break;

                case ItemID.ADAMANTITE_ORE:
                    currentMethod = new AdamantiteBarMethod();
                    break;

                case ItemID.RUNITE_ORE:
                    currentMethod = new RuniteBarMethod();
                    break;
            }
        }
    }
}
