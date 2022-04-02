package com.toofifty.easyblastfurnace.overlays;

import com.google.inject.Inject;
import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.EasyBlastFurnacePlugin;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import lombok.Setter;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

public class EasyBlastFurnaceOverlay extends OverlayPanel
{
    private static final Color TOOLTIP_COLOR = new Color(190, 190, 190);

    private final EasyBlastFurnacePlugin plugin;

    @Inject
    private EasyBlastFurnaceConfig config;

    @Setter
    private MethodStep step;

    @Inject
    EasyBlastFurnaceOverlay(EasyBlastFurnacePlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;

        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OverlayManager.OPTION_CONFIGURE, "Perfect Blast Furnace overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isEnabled()) return null;
        if (!config.showStepOverlay()) return null;

        String tooltip = step != null ? step.getTooltip() : "Withdraw an ore from the bank to start";

        panelComponent.getChildren().add(TitleComponent.builder().text("Easy Blast Furnace").build());
        panelComponent.getChildren().add(LineComponent.builder().left(tooltip).leftColor(TOOLTIP_COLOR).build());

        return super.render(graphics);
    }
}
