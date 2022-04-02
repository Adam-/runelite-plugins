package com.toofifty.easyblastfurnace.overlays;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.EasyBlastFurnacePlugin;
import com.toofifty.easyblastfurnace.methods.Method;
import com.toofifty.easyblastfurnace.steps.MethodStep;
import lombok.Setter;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

@Singleton
public class EasyBlastFurnaceInstructionOverlay extends OverlayPanel
{
    private static final Color TOOLTIP_COLOR = new Color(190, 190, 190);

    private final EasyBlastFurnacePlugin plugin;

    @Inject
    private EasyBlastFurnaceConfig config;

    @Setter
    private MethodStep step;

    @Setter
    private Method method;

    @Inject
    EasyBlastFurnaceInstructionOverlay(EasyBlastFurnacePlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;

        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OverlayManager.OPTION_CONFIGURE, "Easy Blast Furnace overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isEnabled()) return null;
        if (!config.showStepOverlay()) return null;

        String tooltip = step != null ? step.getTooltip() : "Withdraw an ore from the bank to start. You can start a hybrid method by also withdrawing gold ore.";

        panelComponent.getChildren().add(TitleComponent.builder().text("Easy Blast Furnace").build());
        if (method != null) {
            panelComponent.getChildren().add(LineComponent.builder().left(method.getName()).leftColor(config.itemOverlayColor()).build());
        }
        panelComponent.getChildren().add(LineComponent.builder().left(tooltip).leftColor(TOOLTIP_COLOR).build());

        return super.render(graphics);
    }
}
