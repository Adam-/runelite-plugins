package com.toofifty.easyblastfurnace.methods;

import com.toofifty.easyblastfurnace.steps.MethodStep;
import net.runelite.api.ItemID;

public class MithrilBarMethod extends RegularBarMethod
{
    @Override
    MethodStep withdrawOre()
    {
        return withdrawMithrilOre;
    }

    @Override
    int oreItem()
    {
        return ItemID.MITHRIL_ORE;
    }

    @Override
    int barItem()
    {
        return ItemID.MITHRIL_BAR;
    }

    @Override
    int coalPer()
    {
        return 2;
    }
}
