package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class RuniteHybridMethod extends GoldHybridMethod
{
    @Override
    protected MethodStep withdrawOre()
    {
        return withdrawRuniteOre;
    }

    @Override
    public int oreItem()
    {
        return ItemID.RUNITE_ORE;
    }

    @Override
    protected int barItem()
    {
        return ItemID.RUNITE_BAR;
    }

    @Override
    protected int coalPer()
    {
        return 4;
    }

    @Override
    public String getName()
    {
        return "Gold + runite bars";
    }
}
