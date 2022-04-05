package com.toofifty.easyblastfurnace.overlays;

import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import com.toofifty.easyblastfurnace.EasyBlastFurnacePlugin;
import com.toofifty.easyblastfurnace.utils.RSNumberFormat;
import com.toofifty.easyblastfurnace.utils.SessionStatistics;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class StatisticsOverlay extends OverlayPanel
{
    public static final String CLEAR_ACTION = "Clear";

    @Inject
    private SessionStatistics statistics;

    @Inject
    private EasyBlastFurnaceConfig config;

    private final EasyBlastFurnacePlugin plugin;

    @Inject
    StatisticsOverlay(EasyBlastFurnacePlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;

        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, CLEAR_ACTION, "Easy blast furnace statistics"));
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OverlayManager.OPTION_CONFIGURE, "Easy blast furnace statistics"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isEnabled()) return null;
        if (!config.showStatisticsOverlay()) return null;

        if (config.showBarsTodo()) {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Bars todo:")
                .right(RSNumberFormat.format(statistics.getTotalActionsBanked()))
                .build());
        }

        if (config.showBarsMade()) {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Bars made:")
                .right(RSNumberFormat.format(statistics.getTotalActionsDone()))
                .build());
        }

        if (config.showXpBanked()) {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("XP banked:")
                .right(RSNumberFormat.format(statistics.getTotalXpBanked()))
                .build());
        }

        if (config.showXpGained()) {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("XP gained:")
                .right(RSNumberFormat.format(statistics.getTotalXpGained()))
                .build());
        }

        if (config.showStaminaDoses()) {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Stamina doses:")
                .right(RSNumberFormat.format(statistics.getStaminaDoses()))
                .build());
        }

        return super.render(graphics);
    }
}
