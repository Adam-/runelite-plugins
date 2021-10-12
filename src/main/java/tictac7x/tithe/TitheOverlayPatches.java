package tictac7x.tithe;

import net.runelite.api.*;
import tictac7x.Overlay;

import java.util.Map;
import java.util.HashMap;
import java.awt.Dimension;
import java.awt.Graphics2D;

import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TitheOverlayPatches extends Overlay {
    private final Client client;
    private final TithePlugin plugin;
    private final TitheConfig config;

    private LocalPoint location_player_planting_seed;

    public final Map<LocalPoint, TithePlant> plants = new HashMap<>();

    public TitheOverlayPatches(final TithePlugin plugin, final TitheConfig config, final Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    protected void onGameObjectSpawned(final GameObject game_object) {
        // Game object is some sort of tithe patch.
        if (TithePlant.isPatch(game_object)) {
            final LocalPoint location_patch = game_object.getLocalLocation();

            // Empty patch, plant completed.
            if (game_object.getId() == TithePlant.TITHE_EMPTY_PATCH) {
                plants.remove(location_patch);

            // Update plant state.
            } else if (plants.containsKey(location_patch)) {
                plants.get(location_patch).setCyclePatch(game_object);
            }

            // Seedling.
            if (TithePlant.isSeedling(game_object)) {
                // Check if player is next to the patch where player performed seed planting animation.
                if (
                    location_player_planting_seed != null
                    && location_player_planting_seed.getX() + 512 >= location_patch.getX()
                    && location_player_planting_seed.getX() - 512 <= location_patch.getX()
                    && location_player_planting_seed.getY() + 512 >= location_patch.getY()
                    && location_player_planting_seed.getY() - 512 <= location_patch.getY()
                ) {
                    plants.put(location_patch, new TithePlant(game_object, config));
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
        for (final TithePlant patch : plants.values()) {
            patch.onGameTick();
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
        for (TithePlant plant : plants.values()) {
            plant.render(graphics);
        }
    }

    private void renderHighlightedPatch(final Graphics2D graphics) {
        MenuEntry[] menu_entries = client.getMenuEntries();
        if (menu_entries.length != 0) {
            MenuEntry entry = menu_entries[menu_entries.length - 1];
            final TileObject object = findTileObject(client, entry.getParam0(), entry.getParam1(), entry.getIdentifier());

            if (object != null && TithePlant.isPatch(object)) {
                renderTile(graphics, object, config.getPatchesColor());
            }
        }
    }
}
