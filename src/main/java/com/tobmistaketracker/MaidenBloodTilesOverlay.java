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

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.HashSet;
import java.util.Set;

/**
 * This is for testing with a visual aid
 */
public class MaidenBloodTilesOverlay extends Overlay {

    private final Client client;

    @Setter
    private Set<WorldPoint> tiles;

    @Setter
    private Set<LocalPoint> raiderTiles;

    MaidenBloodTilesOverlay(Client client) {
        this.client = client;
        this.tiles = new HashSet<>();
        this.raiderTiles = new HashSet<>();
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        for (WorldPoint localPoint : tiles) {
            renderTile(graphics, LocalPoint.fromWorld(client, localPoint), Color.CYAN);
        }

        for (LocalPoint localPoint : raiderTiles) {
            renderTile(graphics, localPoint, Color.MAGENTA);
        }

        WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
        LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);

//        renderTile(graphics, playerPosLocal, Color.MAGENTA);

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
