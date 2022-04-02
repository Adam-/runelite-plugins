package com.toofifty.easyblastfurnace.steps;

import lombok.Getter;
import net.runelite.api.widgets.WidgetInfo;

public class WidgetStep extends MethodStep
{
    @Getter
    private final WidgetInfo widgetInfo;

    public WidgetStep(WidgetInfo widgetInfo, String tooltip)
    {
        super(tooltip);
        this.widgetInfo = widgetInfo;
    }
}
