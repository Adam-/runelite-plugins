package com.toofifty.easyblastfurnace.steps;

import lombok.Getter;

@Getter
public class ObjectStep extends MethodStep
{
    private final int objectId;

    public ObjectStep(int objectId, String tooltip)
    {
        super(tooltip);
        this.objectId = objectId;
    }
}
