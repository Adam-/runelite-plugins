package tictac7x.tithe;

import tictac7x.Overlay;
import java.util.Map;
import java.util.HashMap;
import java.awt.Graphics2D;
import java.awt.Dimension;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TitheOverlayPlants extends Overlay {
    private final Client client;
    private final TithePlugin plugin;
    private final TitheConfig config;

    private LocalPoint location_player_planting_seed;
    public final Map<LocalPoint, TithePlant> plants = new HashMap<>();

    public TitheOverlayPlants(final TithePlugin plugin, final TitheConfig config, final Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    public void onGameObjectSpawned(final GameObject game_object) {
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

    public void onGameTick() {
        // Save local point where player did seed planting animation.
        if (client.getLocalPlayer() != null && client.getLocalPlayer().getAnimation() == AnimationID.FARMING_PLANT_SEED) {
            location_player_planting_seed = client.getLocalPlayer().getLocalLocation();
        }

        // Update plants progress.
        for (final TithePlant plant : plants.values()) {
            plant.onGameTick();
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (plugin.inTitheFarm()) {
            for (TithePlant plant : plants.values()) {
                plant.render(graphics);
            }
        }

        return null;
    }
}
