package com.tobmistaketracker.overlay;

import com.tobmistaketracker.TobRaider;
import com.tobmistaketracker.detector.MaidenMistakeDetector;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * This is for testing with a visual aid
 */
public class DebugOverlay extends BaseTobMistakeTrackerOverlay {

    private final MaidenMistakeDetector maidenMistakeDetector;

    @Inject
    public DebugOverlay(MaidenMistakeDetector maidenMistakeDetector) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);

        this.maidenMistakeDetector = maidenMistakeDetector;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.isDebug()) return null;

        for (TobRaider raider : plugin.getRaiders()) {
            if (raider.getPreviousWorldLocationForOverlay() != null) {
                renderTile(graphics, toLocalPoint(raider.getPreviousWorldLocationForOverlay()), Color.MAGENTA);
            }
        }

        for (WorldPoint worldPoint : maidenMistakeDetector.getMaidenBloodTiles()) {
            renderTile(graphics, toLocalPoint(worldPoint), Color.CYAN);
        }

        for (WorldPoint worldPoint : maidenMistakeDetector.getBloodSpawnBloodTiles()) {
            renderTile(graphics, toLocalPoint(worldPoint), Color.GREEN);
        }

        return null;
    }

    private LocalPoint toLocalPoint(WorldPoint worldPoint) {
        return LocalPoint.fromWorld(client, worldPoint);
    }
}
