package com.tobmistaketracker.overlay;

import com.tobmistaketracker.TobMistakeTrackerConfig;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import com.tobmistaketracker.detector.BaseTobMistakeDetector;
import com.tobmistaketracker.detector.MistakeDetectorManager;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

/**
 * This is for testing with a visual aid
 */
public class DebugOverlayPanel extends OverlayPanel {

    private final static String OVERLAY_NAME = "Tob Mistake Tracker Overlay";

    private final Client client;
    private final TobMistakeTrackerPlugin plugin;
    private final TobMistakeTrackerConfig config;

    private final MistakeDetectorManager mistakeDetectorManager;

    @Inject
    public DebugOverlayPanel(Client client, TobMistakeTrackerPlugin plugin, TobMistakeTrackerConfig config,
                             MistakeDetectorManager mistakeDetectorManager) {
        super(plugin);
        setPosition(OverlayPosition.TOP_RIGHT);
        setPriority(OverlayPriority.MED);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.mistakeDetectorManager = mistakeDetectorManager;

        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, OVERLAY_NAME));
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, "Reset", OVERLAY_NAME));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.isDebug()) return null;

        if (plugin.isInTob()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("In Tob")
                    .color(Color.GREEN)
                    .build());
        } else {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("NOT in Tob")
                    .color(Color.RED)
                    .build());
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(plugin.isAllRaidersLoaded() ? "All raiders loaded" : "All raiders NOT loaded")
                .color(plugin.isAllRaidersLoaded() ? Color.GREEN : Color.RED)
                .build());

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(plugin.isPanelMightNeedReset() ? "panelMightNeedReset TRUE" : "panelMightNeedReset FALSE")
                .color(plugin.isPanelMightNeedReset() ? Color.GREEN : Color.RED)
                .build());

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Game Tick: " + client.getTickCount())
                .build());

        // Add all mistake detectors
        renderMistakeDetector(mistakeDetectorManager.getClass().getSimpleName(),
                mistakeDetectorManager.isDetectingMistakes());
        for (BaseTobMistakeDetector mistakeDetector : mistakeDetectorManager.getMistakeDetectors()) {
            renderMistakeDetector(mistakeDetector.getClass().getSimpleName(), mistakeDetector.isDetectingMistakes());
        }

        return super.render(graphics);
    }

    private void renderMistakeDetector(String name, boolean isOn) {
        panelComponent.getChildren().add(LineComponent.builder()
                .left(name)
                .right(isOn ? "ON" : "OFF")
                .rightColor(isOn ? Color.GREEN : Color.RED)
                .build());
    }
}
