package tictac7x.tithe;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.util.HashSet;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.util.Set;

public class TitheOverlay extends Overlay {
    private final Client client;
    private final ItemManager items;
    private final TitheConfig config;

    private final int TITHE_FARM_PATCH_EMPTY = 27383;
    private final int TITHE_FARM_PATCH_BOLOGNE = 27385;

    private Set<GameObject> patches = new HashSet<>();

    @Inject
    public TitheOverlay(final Client client, final TitheConfig config, final ItemManager items) {
        this.client = client;
        this.items = items;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void patchSpawned(final GameObject patch) {
        if (patch.getId() == TITHE_FARM_PATCH_EMPTY) {
            patches.add(patch);
        }
    }

    public void patchDespawned(final GameObject patch) {
        if (patch.getId() == TITHE_FARM_PATCH_EMPTY) {
            patches.remove(patch);
        }
    }

    public void gameStateChanged(final GameState game_state) {
        if (game_state == GameState.LOADING) {
            patches.clear();
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        final Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

        // Water cans.
        if (config.highlightWateringCans() && inventory != null) {
//            final WidgetItem item = inventory.getWidgetItem(0);
//            Rectangle bounds = item.getCanvasBounds();
//
//            graphics.setComposite(AlphaComposite.SrcOver.derive(0.3f));
//            final BufferedImage image = items.getImage(item.getId(), item.getQuantity(), false);
//            graphics.drawImage(image, (int) bounds.getX(), (int) bounds.getY(), null);
//            graphics.setComposite(AlphaComposite.SrcOver);
        }

        // Patches.
        if (config.highlightFarmPatches()) {
            for (final GameObject patch : patches) {
                renderClickbox(graphics, patch, Color.BLACK, 2, 0);
            }
        }

        return null;
    }
}
