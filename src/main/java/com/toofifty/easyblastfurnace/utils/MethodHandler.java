package com.toofifty.easyblastfurnace.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.methods.*;
import com.toofifty.easyblastfurnace.overlays.*;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

@Singleton
public class MethodHandler
{
    @Inject
    private EasyBlastFurnaceInstructionOverlay instructionOverlay;

    @Inject
    private EasyBlastFurnaceItemStepOverlay itemStepOverlay;

    @Inject
    private EasyBlastFurnaceObjectStepOverlay objectStepOverlay;

    @Inject
    private EasyBlastFurnaceWidgetStepOverlay widgetStepOverlay;

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
        widgetStepOverlay.setStep(step);
    }

    public void clear()
    {
        currentMethod = null;
        instructionOverlay.setMethod(null);
        instructionOverlay.setStep(null);
        itemStepOverlay.setStep(null);
        objectStepOverlay.setStep(null);
        widgetStepOverlay.setStep(null);
    }

    private boolean inInventory(int itemId)
    {
        return state.getInventoryQuantity(itemId) > 0;
    }

    private Method getMethodFromInventory()
    {
        // ensure method doesn't reset after gold/metal has been removed from inventory
        if (currentMethod instanceof GoldHybridMethod) return null;

        if (inInventory(ItemID.GOLD_ORE) ||
            currentMethod instanceof GoldBarMethod) {

            if (inInventory(ItemID.MITHRIL_ORE) ||
                currentMethod instanceof MithrilBarMethod)
                return new MithrilHybridMethod();

            if (inInventory(ItemID.ADAMANTITE_ORE) ||
                currentMethod instanceof AdamantiteBarMethod)
                return new AdamantiteHybridMethod();

            if (inInventory(ItemID.RUNITE_ORE) ||
                currentMethod instanceof RuniteBarMethod)
                return new RuniteHybridMethod();

            return new GoldBarMethod();
        }

        if (inInventory(ItemID.IRON_ORE)) return new SteelBarMethod();
        if (inInventory(ItemID.MITHRIL_ORE)) return new MithrilBarMethod();
        if (inInventory(ItemID.ADAMANTITE_ORE)) return new AdamantiteBarMethod();
        if (inInventory(ItemID.RUNITE_ORE)) return new RuniteBarMethod();

        return null;
    }

    public void setMethodFromInventory()
    {
        Method method = getMethodFromInventory();
        if (method == null ||
            (currentMethod != null && currentMethod.getClass().isInstance(method)))
            return;

        clear();
        currentMethod = method;

        instructionOverlay.setMethod(currentMethod);
    }
}
