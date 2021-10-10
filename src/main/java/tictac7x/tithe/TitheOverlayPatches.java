package tictac7x.tithe;

import tictac7x.Overlay;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Point;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GameObject;
import net.runelite.api.AnimationID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TitheOverlayPatches extends Overlay {
    private final Client client;
    private final TithePlugin plugin;
    private final TitheConfig config;

    private final Set<GameObject> patches_all = new HashSet<>();
    private final Map<LocalPoint, TithePatch> patches_player = new HashMap<>();

    @Inject
    public TitheOverlayPatches(final Client client, final TithePlugin plugin, final TitheConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    protected void onGameObjectSpawned(final GameObject patch) {
        if (client.getLocalPlayer() == null) return;
        final LocalPoint location_player = client.getLocalPlayer().getLocalLocation();
        final LocalPoint location_patch = patch.getLocalLocation();

        // All patches.
        if (TithePatch.isPatch(patch)) {
            patches_all.add(patch);

            // Empty patch.
            if (patch.getId() == TithePatch.TITHE_EMPTY_PATCH) {
                patches_player.remove(location_patch);

            // Update patch state.
            } else if (patches_player.containsKey(location_patch)) {
                patches_player.get(location_patch).setCyclePatch(patch);
            }
        }

        // Seedling.
        if (TithePatch.isSeedling(patch)) {
            System.out.println("SEEDLING");
            // Check if player is doing seed planting animation.
            if (client.getLocalPlayer().getAnimation() == AnimationID.FARMING_PLANT_SEED) {
                System.out.println("ANIMATING");
                // Check if player is next to the patch.
                if (
                    location_player.getX() + 256 >= location_patch.getX() &&
                    location_player.getX() - 256 <= location_patch.getX() &&
                    location_player.getY() + 256 >= location_patch.getY() &&
                    location_player.getY() - 256 <= location_patch.getY()
                ) {
                    System.out.println("CLOSE");
                    patches_player.put(location_patch, new TithePatch(patch, config));
                }
            }
        }
    }

    protected void onGameTick() {
        for (final TithePatch patch : patches_player.values()) {
            patch.onGameTick();
        }
    }

    protected void onGameObjectDespawned(final GameObject object) {
        patches_all.remove(object);
    }

    protected void onGameStateChanged(final GameState game_state) {
        if (game_state == GameState.LOADING) {
            patches_all.clear();
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (!plugin.inTitheFarm()) return null;
        renderPlants(graphics);

        if (config.highlightPatchesOnHover()) {
            renderHighlightedPatch(graphics);
        }

        return null;
    }

    private void renderPlants(final Graphics2D graphics) {
        for (TithePatch plant : patches_player.values()) {
            plant.render(graphics);
        }
    }

    private void renderHighlightedPatch(final Graphics2D graphics) {
        final Point cursor = client.getMouseCanvasPosition();
        int distance_min = Integer.MAX_VALUE;
        GameObject patch_hover = null;

        // Find the closest patch that is hovered.
        for (final GameObject patch : patches_all) {
            // On screen.
            if (patch.getClickbox() != null && client.getLocalPlayer() != null) {
                final int distance = patch.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation());

                if (patch.getClickbox().contains(cursor.getX(), cursor.getY()) && distance < distance_min) {
                    distance_min = distance;
                    patch_hover = patch;
                }
            }
        }

        // Highlight only the closest patch.
        if (patch_hover != null) {
            renderTile(graphics, patch_hover,config.getPatchesColor());
        }
    }
}
