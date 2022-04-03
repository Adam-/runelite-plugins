package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class MithrilBarMethod extends MetalBarMethod
{
    @Override
    protected MethodStep withdrawOre()
    {
        return withdrawMithrilOre;
    }

    @Override
    public int oreItem()
    {
        return ItemID.MITHRIL_ORE;
    }

    @Override
    protected int barItem()
    {
        return ItemID.MITHRIL_BAR;
    }

    @Override
    protected int coalPer()
    {
        return 2;
    }

    @Override
    public String getName()
    {
        return "Mithril bars";
    }
}
