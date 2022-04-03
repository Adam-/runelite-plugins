package com.toofifty.easyblastfurnace.steps;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

public class TileStep extends MethodStep
{
    @Getter
    private final WorldPoint worldPoint;

    public TileStep(WorldPoint worldPoint, String tooltip)
    {
        super(tooltip);
        this.worldPoint = worldPoint;
    }
}
