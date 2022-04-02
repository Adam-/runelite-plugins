package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class AdamantiteBarMethod extends RegularBarMethod
{
    @Override
    MethodStep withdrawOre()
    {
        return withdrawAdamantiteOre;
    }

    @Override
    int oreItem()
    {
        return ItemID.ADAMANTITE_ORE;
    }

    @Override
    int barItem()
    {
        return ItemID.ADAMANTITE_BAR;
    }

    @Override
    int coalPer()
    {
        return 3;
    }
}
