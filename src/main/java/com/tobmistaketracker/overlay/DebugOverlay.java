package com.tobmistaketracker.overlay;

import com.tobmistaketracker.TobRaider;
import com.tobmistaketracker.detector.BloatMistakeDetector;
import com.tobmistaketracker.detector.MaidenMistakeDetector;
import com.tobmistaketracker.detector.VerzikP2MistakeDetector;
import com.tobmistaketracker.detector.VerzikP3MistakeDetector;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * This is for testing with a visual aid
 */
@Slf4j
public class DebugOverlay extends BaseTobMistakeTrackerOverlay {

    private final MaidenMistakeDetector maidenMistakeDetector;
    private final BloatMistakeDetector bloatMistakeDetector;
    private final VerzikP2MistakeDetector verzikP2MistakeDetector;
    private final VerzikP3MistakeDetector verzikP3MistakeDetector;
    private final boolean developerMode;

    @Inject
    public DebugOverlay(MaidenMistakeDetector maidenMistakeDetector,
                        BloatMistakeDetector bloatMistakeDetector,
                        VerzikP2MistakeDetector verzikP2MistakeDetector,
                        VerzikP3MistakeDetector verzikP3MistakeDetector,
                        @Named("developerMode") boolean developerMode) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);

        this.maidenMistakeDetector = maidenMistakeDetector;
        this.bloatMistakeDetector = bloatMistakeDetector;
        this.verzikP2MistakeDetector = verzikP2MistakeDetector;
        this.verzikP3MistakeDetector = verzikP3MistakeDetector;
        this.developerMode = developerMode;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!developerMode) return null;

        for (TobRaider raider : plugin.getRaiders()) {
            if (raider.getPreviousWorldLocationForOverlay() != null) {
                LocalPoint localPoint = toLocalPoint(raider.getPreviousWorldLocationForOverlay());
                renderTile(graphics, localPoint, Color.MAGENTA);
            }
        }

        for (WorldPoint worldPoint : maidenMistakeDetector.getMaidenBloodTiles()) {
            renderTile(graphics, toLocalPoint(worldPoint), Color.CYAN);
        }

        for (WorldPoint worldPoint : maidenMistakeDetector.getBloodSpawnBloodTiles()) {
            renderTile(graphics, toLocalPoint(worldPoint), Color.GREEN);
        }

        for (WorldPoint worldPoint : verzikP2MistakeDetector.getVerzikP2AcidTiles()) {
            renderTile(graphics, toLocalPoint(worldPoint), Color.GREEN);
        }

        for (WorldPoint worldPoint : verzikP3MistakeDetector.getVerzikP3WebTiles()) {
            renderTile(graphics, toLocalPoint(worldPoint), Color.ORANGE);
        }

        return null;
    }

    private LocalPoint toLocalPoint(WorldPoint worldPoint) {
        return LocalPoint.fromWorld(client, worldPoint);
    }
}
