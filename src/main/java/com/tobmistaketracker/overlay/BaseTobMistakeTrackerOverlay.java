package com.tobmistaketracker.overlay;

import com.tobmistaketracker.TobMistakeTrackerPlugin;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

public abstract class BaseTobMistakeTrackerOverlay extends Overlay {

    @Inject
    protected Client client;
    @Inject
    protected TobMistakeTrackerPlugin plugin;

    protected BaseTobMistakeTrackerOverlay() {
    }

    protected void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color) {
        if (dest == null) {
            return;
        }

        final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

        if (poly == null) {
            return;
        }

        OverlayUtil.renderPolygon(graphics, poly, color);
    }
}
