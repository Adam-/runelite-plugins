package com.tobmistaketracker.overlay;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * This is for testing with a visual aid
 */
public class DebugOverlay extends BaseTobMistakeTrackerOverlay {

    public DebugOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enableDebug()) return null;

        for (WorldPoint worldPoint : plugin.getRaiderPreviousWorldLocations()) {
            renderTile(graphics, LocalPoint.fromWorld(client, worldPoint), Color.MAGENTA);
        }

        // TODO: Add maiden CYAN and blood spawn GREEN (new class for maiden?)

        return null;
    }
}
