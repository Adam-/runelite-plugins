package com.toofifty.easyblastfurnace.steps;

import lombok.Getter;

@Getter
public class ItemStep extends MethodStep
{
    private final int itemId;

    public ItemStep(int itemId, String tooltip)
    {
        super(tooltip);
        this.itemId = itemId;
    }
}
