package tictac7x.tithe;

import tictac7x.Overlay;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import javax.inject.Inject;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import static net.runelite.api.ObjectID.TITHE_PATCH;
import static net.runelite.api.ObjectID.BLIGHTED_GOLOVANOVA_PLANT;
import static net.runelite.api.ObjectID.BLIGHTED_GOLOVANOVA_SEEDLING;
import static net.runelite.api.ObjectID.BLIGHTED_GOLOVANOVA_PLANT_27392;
import static net.runelite.api.ObjectID.BLIGHTED_GOLOVANOVA_PLANT_27394;
import static net.runelite.api.ObjectID.BLIGHTED_BOLOGANO_SEEDLING;
import static net.runelite.api.ObjectID.BLIGHTED_BOLOGANO_PLANT;
import static net.runelite.api.ObjectID.BLIGHTED_BOLOGANO_PLANT_27403;
import static net.runelite.api.ObjectID.BLIGHTED_BOLOGANO_PLANT_27405;
import static net.runelite.api.ObjectID.BLIGHTED_LOGAVANO_SEEDLING;
import static net.runelite.api.ObjectID.BLIGHTED_LOGAVANO_PLANT;
import static net.runelite.api.ObjectID.BLIGHTED_LOGAVANO_PLANT_27414;
import static net.runelite.api.ObjectID.BLIGHTED_LOGAVANO_PLANT_27416;
import static net.runelite.api.ObjectID.GOLOVANOVA_SEEDLING;
import static net.runelite.api.ObjectID.GOLOVANOVA_SEEDLING_27385;
import static net.runelite.api.ObjectID.GOLOVANOVA_PLANT;
import static net.runelite.api.ObjectID.GOLOVANOVA_PLANT_27388;
import static net.runelite.api.ObjectID.GOLOVANOVA_PLANT_27390;
import static net.runelite.api.ObjectID.GOLOVANOVA_PLANT_27391;
import static net.runelite.api.ObjectID.GOLOVANOVA_PLANT_27393;
import static net.runelite.api.ObjectID.BOLOGANO_SEEDLING;
import static net.runelite.api.ObjectID.BOLOGANO_SEEDLING_27396;
import static net.runelite.api.ObjectID.BOLOGANO_PLANT;
import static net.runelite.api.ObjectID.BOLOGANO_PLANT_27399;
import static net.runelite.api.ObjectID.BOLOGANO_PLANT_27401;
import static net.runelite.api.ObjectID.BOLOGANO_PLANT_27402;
import static net.runelite.api.ObjectID.BOLOGANO_PLANT_27404;
import static net.runelite.api.ObjectID.LOGAVANO_SEEDLING;
import static net.runelite.api.ObjectID.LOGAVANO_SEEDLING_27407;
import static net.runelite.api.ObjectID.LOGAVANO_PLANT;
import static net.runelite.api.ObjectID.LOGAVANO_PLANT_27410;
import static net.runelite.api.ObjectID.LOGAVANO_PLANT_27412;
import static net.runelite.api.ObjectID.LOGAVANO_PLANT_27413;
import static net.runelite.api.ObjectID.LOGAVANO_PLANT_27415;

public class TitheOverlayPatches extends Overlay {
    private final Client client;
    private final TitheConfig config;

    private final float PLANT_BLIGHTED_TIME = 60000;

    private final Set<GameObject> patches = new HashSet<>();
    private final Map<GameObject, Date> patches_blighted = new HashMap<>();


    @Inject
    public TitheOverlayPatches(final Client client, final TitheConfig config) {
        this.client = client;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    protected void onGameObjectSpawned(final GameObject object) {
        // Blighted patches.
        if (isBlightedPatch(object) && !patches_blighted.containsKey(object)) {
            patches_blighted.put(object, new Date());
        }

        // All patches.
        if (isPatch(object)) {
            patches.add(object);
        }
    }

    protected void onGameObjectDespawned(final GameObject object) {
        if (isPatch(object)) {
            patches_blighted.remove(object);
            patches.remove(object);
        }
    }

    protected void onGameStateChanged(final GameState game_state) {
        if (game_state == GameState.LOADING) {
            patches.clear();
            patches_blighted.clear();
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        // Blighted patches.
        final Date now = new Date();
        for (final Map.Entry<GameObject, Date> plant_blighted : patches_blighted.entrySet()) {
            renderPie(graphics, plant_blighted.getKey(), color_gray, 1 - (now.getTime() - plant_blighted.getValue().getTime()) / PLANT_BLIGHTED_TIME);
        }

        // Highlight hovered patch.
        final Point cursor = client.getMouseCanvasPosition();
        for (final GameObject patch : patches) {
            if (patch.getCanvasTilePoly().contains(cursor.getX(), cursor.getY())) {
                renderTile(graphics, patch, color_gray);
            }
        }

        return null;
    }

    private boolean isBlightedPatch(final GameObject object) {
        final int id = object.getId();

        return id == BLIGHTED_GOLOVANOVA_SEEDLING
            || id == BLIGHTED_GOLOVANOVA_PLANT
            || id == BLIGHTED_GOLOVANOVA_PLANT_27392
            || id == BLIGHTED_GOLOVANOVA_PLANT_27394
            || id == BLIGHTED_BOLOGANO_SEEDLING
            || id == BLIGHTED_BOLOGANO_PLANT
            || id == BLIGHTED_BOLOGANO_PLANT_27403
            || id == BLIGHTED_BOLOGANO_PLANT_27405
            || id == BLIGHTED_LOGAVANO_SEEDLING
            || id == BLIGHTED_LOGAVANO_PLANT
            || id == BLIGHTED_LOGAVANO_PLANT_27414
            || id == BLIGHTED_LOGAVANO_PLANT_27416
        ;
    }

    private boolean isPatch(final GameObject object) {
        final int id = object.getId();

        return isBlightedPatch(object)
            || id == TITHE_PATCH
            || id == GOLOVANOVA_SEEDLING
            || id == GOLOVANOVA_SEEDLING_27385
            || id == GOLOVANOVA_PLANT
            || id == GOLOVANOVA_PLANT_27388
            || id == GOLOVANOVA_PLANT_27390
            || id == GOLOVANOVA_PLANT_27391
            || id == GOLOVANOVA_PLANT_27393
            || id == BOLOGANO_SEEDLING
            || id == BOLOGANO_SEEDLING_27396
            || id == BOLOGANO_PLANT
            || id == BOLOGANO_PLANT_27399
            || id == BOLOGANO_PLANT_27401
            || id == BOLOGANO_PLANT_27402
            || id == BOLOGANO_PLANT_27404
            || id == LOGAVANO_SEEDLING
            || id == LOGAVANO_SEEDLING_27407
            || id == LOGAVANO_PLANT
            || id == LOGAVANO_PLANT_27410
            || id == LOGAVANO_PLANT_27412
            || id == LOGAVANO_PLANT_27413
            || id == LOGAVANO_PLANT_27415
        ;
    }
}
