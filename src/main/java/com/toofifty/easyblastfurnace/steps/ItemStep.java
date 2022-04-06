package com.toofifty.easyblastfurnace.steps;

import lombok.Getter;

@Getter
public class ItemStep extends MethodStep
{
    private final int itemId;
    private final int alternateItemId;

    public ItemStep(int itemId, String tooltip)
    {
        this(itemId, -1, tooltip);
    }

    public ItemStep(int itemId, int alternateItemId, String tooltip)
    {
        super(tooltip);
        this.itemId = itemId;
        this.alternateItemId = alternateItemId;
    }
}
