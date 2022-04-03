package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class AdamantiteBarMethod extends MetalBarMethod
{
    @Override
    protected MethodStep withdrawOre()
    {
        return withdrawAdamantiteOre;
    }

    @Override
    public int oreItem()
    {
        return ItemID.ADAMANTITE_ORE;
    }

    @Override
    protected int barItem()
    {
        return ItemID.ADAMANTITE_BAR;
    }

    @Override
    protected int coalPer()
    {
        return 3;
    }

    @Override
    public String getName()
    {
        return "Adamantite bars";
    }
}
