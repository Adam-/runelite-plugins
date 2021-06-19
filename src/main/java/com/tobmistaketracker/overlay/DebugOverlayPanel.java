package com.tobmistaketracker.overlay;

import com.tobmistaketracker.MistakeManager;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerConfig;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import com.tobmistaketracker.TobRaider;
import com.tobmistaketracker.detector.MaidenMistakeDetector;
import com.tobmistaketracker.detector.MistakeDetectorManager;
import com.tobmistaketracker.detector.TobMistakeDetector;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
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

    private final MistakeManager mistakeManager;
    private final MistakeDetectorManager mistakeDetectorManager;
    private final MaidenMistakeDetector maidenMistakeDetector;

    @Inject
    public DebugOverlayPanel(Client client, TobMistakeTrackerPlugin plugin, TobMistakeTrackerConfig config,
                             MistakeManager mistakeManager,
                             MaidenMistakeDetector maidenMistakeDetector,
                             MistakeDetectorManager mistakeDetectorManager) {
        super(plugin);
        setPosition(OverlayPosition.TOP_RIGHT);
        setPriority(OverlayPriority.MED);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.mistakeManager = mistakeManager;
        this.maidenMistakeDetector = maidenMistakeDetector;
        this.mistakeDetectorManager = mistakeDetectorManager;

        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, OVERLAY_NAME));
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY, "Reset", OVERLAY_NAME));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enableDebug()) return null;

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
                .text("Game Tick: " + client.getTickCount())
                .build());

        // Add all raiders
        for (TobRaider raider : plugin.getRaiders()) {
            renderPlayerComponents(raider.getName(), Color.CYAN);
        }

        // Add all non-raiders we've tracked with mistakes
        for (String playerName : mistakeManager.getPlayersWithMistakes()) {
            if (!plugin.isPlayerInRaid(playerName)) {
                renderPlayerComponents(playerName, Color.WHITE);
            }
        }

        // Add all mistake detectors
        renderMistakeDetector(mistakeDetectorManager.getClass().getSimpleName(), mistakeDetectorManager.isDetectingMistakes());
        for (TobMistakeDetector mistakeDetector : mistakeDetectorManager.getMistakeDetectors()) {
            renderMistakeDetector(mistakeDetector.getClass().getSimpleName(), mistakeDetector.isDetectingMistakes());
        }

        return super.render(graphics);
    }

    private void renderPlayerComponents(String playerName, Color playerNameColor) {
        panelComponent.getChildren().add(LineComponent.builder()
                .left(playerName)
                .right("Mistakes:")
                .leftColor(playerNameColor)
                .build());

        if (mistakeManager.hasAnyMistakes(playerName)) {
            renderMistakesForPlayer(playerName);
        } else {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("NONE")
                    .build());
        }

        // Newline
        panelComponent.getChildren().add(LineComponent.builder().build());
    }

    private void renderMistakesForPlayer(String playerName) {
        for (TobMistake mistake : TobMistake.values()) {
            int count = mistakeManager.getMistakeCountForPlayer(playerName, mistake);
            if (count > 0) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left(mistake.getMistakeName())
                        .right(String.valueOf(count))
                        .rightColor(Color.RED)
                        .build());
            }
        }
    }

    private void renderMistakeDetector(String name, boolean isOn) {
        panelComponent.getChildren().add(LineComponent.builder()
                .left(name)
                .right(isOn ? "ON" : "OFF")
                .rightColor(isOn ? Color.GREEN : Color.RED)
                .build());
    }
}
