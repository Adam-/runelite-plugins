package tictac7x.tithe;

import tictac7x.Overlay;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
    public final Map<LocalPoint, TithePatch> patches_player = new HashMap<>();

    private LocalPoint location_player_planting_seed;

    public TitheOverlayPatches(final TithePlugin plugin, final TitheConfig config, final Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    protected void onGameObjectSpawned(final GameObject game_object) {
        // Game object is some sort of tithe patch.
        if (TithePatch.isPatch(game_object)) {
            final LocalPoint location_patch = game_object.getLocalLocation();
            patches_all.add(game_object);

            // Empty patch, plant completed.
            if (game_object.getId() == TithePatch.TITHE_EMPTY_PATCH) {
                patches_player.remove(location_patch);

            // Update plant state.
            } else if (patches_player.containsKey(location_patch)) {
                patches_player.get(location_patch).setCyclePatch(game_object);
            }

            // Seedling.
            if (TithePatch.isSeedling(game_object)) {
                // Check if player is next to the patch where player performed seed planting animation.
                if (
                    location_player_planting_seed != null
                    && location_player_planting_seed.getX() + 512 >= location_patch.getX()
                    && location_player_planting_seed.getX() - 512 <= location_patch.getX()
                    && location_player_planting_seed.getY() + 512 >= location_patch.getY()
                    && location_player_planting_seed.getY() - 512 <= location_patch.getY()
                ) {
                    patches_player.put(location_patch, new TithePatch(game_object, config));
                }
            }
        }
    }

    protected void onGameTick() {
        // Save local point where player did seed planting animation.
        if (client.getLocalPlayer() != null && client.getLocalPlayer().getAnimation() == AnimationID.FARMING_PLANT_SEED) {
            location_player_planting_seed = client.getLocalPlayer().getLocalLocation();
        }

        // Update plants progress.
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
