package com.tobmistaketracker.overlay;

import com.tobmistaketracker.MistakeManager;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerConfig;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import com.tobmistaketracker.TobRaider;
import com.tobmistaketracker.detector.MaidenMistakeDetector;
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
import java.util.Map;

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
    private final MaidenMistakeDetector maidenMistakeDetector;

    @Inject
    public DebugOverlayPanel(Client client, TobMistakeTrackerPlugin plugin, TobMistakeTrackerConfig config,
                             MistakeManager mistakeManager,
                             MaidenMistakeDetector maidenMistakeDetector) {
        super(plugin);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setPriority(OverlayPriority.MED);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.mistakeManager = mistakeManager;
        this.maidenMistakeDetector = maidenMistakeDetector;

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

        // Maybe loop over all mistakes instead of current raiders for Debug?
        // TODO: Show active detectors running too
        for (TobRaider raider : plugin.getRaiders().values()) {
            String name = raider.getName();
            Map<TobMistake, Integer> mistakes = mistakeManager.getMistakesForPlayer(name);

            String mistakesString = "Deaths: " + mistakes.get(TobMistake.DEATH) + " - Bloods: " +
                    mistakes.get(TobMistake.MAIDEN_BLOOD);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(name)
                    .right(mistakesString)
                    .build());
        }

        return super.render(graphics);
    }
}
