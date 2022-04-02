package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.EasyBlastFurnacePlugin;
import com.toofifty.easyblastfurnace.steps.ItemStep;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import com.toofifty.easyblastfurnace.steps.ObjectStep;
import com.toofifty.easyblastfurnace.utils.BlastFurnaceState;
import net.runelite.api.ItemID;

public abstract class Method
{
    // items
    protected final MethodStep fillCoalBag = new ItemStep(ItemID.COAL_BAG_12019, "Fill coal bag");
    protected final MethodStep emptyCoalBag = new ItemStep(ItemID.COAL_BAG_12019, "Empty coal bag");
    protected final MethodStep withdrawCoalBag = new ItemStep(ItemID.COAL_BAG_12019, "Withdraw coal bag");

    protected final MethodStep withdrawCoal = new ItemStep(ItemID.COAL, "Withdraw coal");
    protected final MethodStep withdrawGoldOre = new ItemStep(ItemID.GOLD_ORE, "Withdraw gold ore");
    protected final MethodStep withdrawMithrilOre = new ItemStep(ItemID.MITHRIL_ORE, "Withdraw mithril ore");
    protected final MethodStep withdrawAdamantiteOre = new ItemStep(ItemID.ADAMANTITE_ORE, "Withdraw adamantite ore");
    protected final MethodStep withdrawRuniteOre = new ItemStep(ItemID.RUNITE_ORE, "Withdraw runite ore");

    protected final MethodStep equipIceGloves = new ItemStep(ItemID.ICE_GLOVES, "Equip ice gloves");
    protected final MethodStep equipGoldsmithGauntlets = new ItemStep(ItemID.GOLDSMITH_GAUNTLETS, "Equip goldsmith gauntlets");

    // objects
    protected final MethodStep depositAllIntoBank = new ObjectStep(EasyBlastFurnacePlugin.BANK_CHEST, "Deposit bars into bank");
    protected final MethodStep putOntoConveyorBelt = new ObjectStep(EasyBlastFurnacePlugin.CONVEYOR_BELT, "Put ore onto conveyor belt");
    protected final MethodStep openBank = new ObjectStep(EasyBlastFurnacePlugin.BANK_CHEST, "Open bank chest");
    protected final MethodStep collectBars = new ObjectStep(EasyBlastFurnacePlugin.BAR_DISPENSER, "Collect bars");

    abstract public MethodStep[] getPrerequisiteSteps();

    abstract public MethodStep[] getSteps();

    abstract public MethodStep next(BlastFurnaceState state);
}
