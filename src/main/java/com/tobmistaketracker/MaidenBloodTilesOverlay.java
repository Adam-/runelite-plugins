package com.tobmistaketracker;

import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is for testing with a visual aid
 */
public class MaidenBloodTilesOverlay extends Overlay {

    private final Client client;

    @Setter
    private Set<WorldPoint> tiles;

    @Setter
    private Set<WorldPoint> tiles2;

    @Setter
    private List<WorldPoint> raiderTiles;


    MaidenBloodTilesOverlay(Client client) {
        this.client = client;
        this.tiles = new HashSet<>();
        this.tiles2 = new HashSet<>();
        this.raiderTiles = new ArrayList<>();
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        for (WorldPoint worldPoint : raiderTiles) {
            renderTile(graphics, LocalPoint.fromWorld(client, worldPoint), Color.MAGENTA);
        }

        for (WorldPoint worldPoint : tiles) {
            renderTile(graphics, LocalPoint.fromWorld(client, worldPoint), Color.CYAN);
        }

        for (WorldPoint worldPoint : tiles2) {
            renderTile(graphics, LocalPoint.fromWorld(client, worldPoint), Color.CYAN);
        }

        return null;
    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color) {
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
