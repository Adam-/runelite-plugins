package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class SteelBarMethod extends MetalBarMethod
{
    @Override
    protected MethodStep withdrawOre()
    {
        return withdrawIronOre;
    }

    @Override
    public int oreItem()
    {
        return ItemID.IRON_ORE;
    }

    @Override
    protected int barItem()
    {
        return ItemID.STEEL_BAR;
    }

    @Override
    protected int coalPer()
    {
        return 1;
    }

    @Override
    public String getName()
    {
        return "Steel bars";
    }
}
