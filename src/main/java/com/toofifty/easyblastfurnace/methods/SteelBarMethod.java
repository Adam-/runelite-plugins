package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class SteelBarMethod extends RegularBarMethod
{
    @Override
    MethodStep withdrawOre()
    {
        return withdrawIronOre;
    }

    @Override
    int oreItem()
    {
        return ItemID.IRON_ORE;
    }

    @Override
    int barItem()
    {
        return ItemID.STEEL_BAR;
    }

    @Override
    int coalPer()
    {
        return 1;
    }
}
