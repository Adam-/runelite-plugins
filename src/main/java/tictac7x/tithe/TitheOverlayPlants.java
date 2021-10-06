package tictac7x.tithe;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.util.*;
import java.awt.Graphics2D;
import java.awt.Dimension;

public class TitheOverlayPlants extends Overlay {
    private final Client client;
    private final ItemManager items;
    private final TitheConfig config;

    private final int TITHE_PATCH = 27383;
    private final float PLANT_BLIGHTED_TIME = 60000;

    // Golovanova plants.
    private final int GOLOVANOVA_PLANT_0 = 27384;
    private final int GOLOVANOVA_PLANT_0_WATERED = 27385;
    private final int GOLOVANOVA_PLANT_0_BLIGHTED = 27386;
    private final int GOLOVANOVA_PLANT_1 = 27387;
    private final int GOLOVANOVA_PLANT_1_WATERED = 27388;
    private final int GOLOVANOVA_PLANT_1_BLIGHTED = 27389;
    private final int GOLOVANOVA_PLANT_2 = 27390;
    private final int GOLOVANOVA_PLANT_2_WATERED = 27391;
    private final int GOLOVANOVA_PLANT_2_BLIGHTED = 27392;
    private final int GOLOVANOVA_PLANT_3 = 27393;
    private final int GOLOVANOVA_PLANT_3_BLIGHTED = 27394;

    // Bologano plants.
    private final int BOLOGANO_PLANT_0 = 27395;
    private final int BOLOGANO_PLANT_0_WATERED = 27396;
    private final int BOLOGANO_PLANT_0_BLIGHTED = 27397;
    private final int BOLOGANO_PLANT_1 = 27398;
    private final int BOLOGANO_PLANT_1_WATERED = 27399;
    private final int BOLOGANO_PLANT_1_BLIGHTED = 27400;
    private final int BOLOGANO_PLANT_2 = 27401;
    private final int BOLOGANO_PLANT_2_WATERED = 27402;
    private final int BOLOGANO_PLANT_2_BLIGHTED = 27403;
    private final int BOLOGANO_PLANT_3 = 27404;
    private final int BOLOGANO_PLANT_3_BLIGHTED = 27405;

    // Logavano plants.
    private final int LOGAVANO_PLANT_0 = 27406;
    private final int LOGAVANO_PLANT_0_WATERED = 27407;
    private final int LOGAVANO_PLANT_0_BLIGHTED = 27408;
    private final int LOGAVANO_PLANT_1 = 27409;
    private final int LOGAVANO_PLANT_1_WATERED = 27410;
    private final int LOGAVANO_PLANT_1_BLIGHTED = 27411;
    private final int LOGAVANO_PLANT_2 = 27412;
    private final int LOGAVANO_PLANT_2_WATERED = 27413;
    private final int LOGAVANO_PLANT_2_BLIGHTED = 27414;
    private final int LOGAVANO_PLANT_3 = 27415;
    private final int LOGAVANO_PLANT_3_BLIGHTED = 27416;

    private final PanelComponent panel_water = new PanelComponent();

    private final Map<Integer, Set<Integer>> patches_used = new HashMap<>();
    private final Set<GameObject> patches_empty = new HashSet<>();
    private final Map<GameObject, Date> plants_blighted = new HashMap<>();

    private int water_current = 0;
    private int water_total = 0;

    @Inject
    public TitheOverlayPlants(final Client client, final TitheConfig config, final ItemManager items) {
        this.client = client;
        this.items = items;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    protected void onGameObjectSpawned(final GameObject object) {
        final int id = object.getId();

        // Seed planted.
        if (id == GOLOVANOVA_PLANT_0 || id == BOLOGANO_PLANT_0 || id == LOGAVANO_PLANT_0) {
            // Patch with current x coordinate already existed, extend the set.
            if (patches_used.containsKey(object.getY())) {
                patches_used.get(object.getY()).add(object.getX());

            // First patch with given x coordinate, create new set with patch y coordinate.
            } else {
                patches_used.put(object.getY(), new HashSet<>(Arrays.asList(object.getX())));
            }

        // Previously used empty tithe patch.
        } else if (
            id == TITHE_PATCH &&
            patches_used.containsKey(object.getY()) &&
            patches_used.get(object.getY()).contains(object.getX())
        ) {
            patches_empty.add(object);
        } else if ((
            id == GOLOVANOVA_PLANT_0_BLIGHTED ||
            id == GOLOVANOVA_PLANT_1_BLIGHTED ||
            id == GOLOVANOVA_PLANT_2_BLIGHTED ||
            id == GOLOVANOVA_PLANT_3_BLIGHTED ||
            id == BOLOGANO_PLANT_0_BLIGHTED ||
            id == BOLOGANO_PLANT_1_BLIGHTED ||
            id == BOLOGANO_PLANT_2_BLIGHTED ||
            id == BOLOGANO_PLANT_3_BLIGHTED ||
            id == LOGAVANO_PLANT_0_BLIGHTED ||
            id == LOGAVANO_PLANT_1_BLIGHTED ||
            id == LOGAVANO_PLANT_2_BLIGHTED ||
            id == LOGAVANO_PLANT_3_BLIGHTED) &&
            !plants_blighted.containsKey(object)
        ) {
            plants_blighted.put(object, new Date());
        }
    }

    protected void onGameObjectDespawned(final GameObject object) {
        final int id = object.getId();

        // Empty patch despawned.
        if (id == TITHE_PATCH) {
            patches_empty.remove(object);

        // Blighted plant despawned.
        } else if (
            id == GOLOVANOVA_PLANT_0_BLIGHTED ||
            id == GOLOVANOVA_PLANT_1_BLIGHTED ||
            id == GOLOVANOVA_PLANT_2_BLIGHTED ||
            id == GOLOVANOVA_PLANT_3_BLIGHTED ||
            id == BOLOGANO_PLANT_0_BLIGHTED ||
            id == BOLOGANO_PLANT_1_BLIGHTED ||
            id == BOLOGANO_PLANT_2_BLIGHTED ||
            id == BOLOGANO_PLANT_3_BLIGHTED ||
            id == LOGAVANO_PLANT_0_BLIGHTED ||
            id == LOGAVANO_PLANT_1_BLIGHTED ||
            id == LOGAVANO_PLANT_2_BLIGHTED ||
            id == LOGAVANO_PLANT_3_BLIGHTED
        ) {
            plants_blighted.remove(object);
        }
    }

    protected void onItemContainerChanged(final ItemContainer item_container) {
        int water_current = 0;

        int water_1 = item_container.count(ItemID.WATERING_CAN1);
        int water_2 = item_container.count(ItemID.WATERING_CAN2);
        int water_3 = item_container.count(ItemID.WATERING_CAN3);
        int water_4 = item_container.count(ItemID.WATERING_CAN4);
        int water_5 = item_container.count(ItemID.WATERING_CAN5);
        int water_6 = item_container.count(ItemID.WATERING_CAN6);
        int water_7 = item_container.count(ItemID.WATERING_CAN7);
        int water_8 = item_container.count(ItemID.WATERING_CAN8);

        water_current += 1 * water_1;
        water_current += 2 * water_2;
        water_current += 3 * water_3;
        water_current += 4 * water_4;
        water_current += 5 * water_5;
        water_current += 6 * water_6;
        water_current += 7 * water_7;
        water_current += 8 * water_8;

        this.water_current = water_current;
        this.water_total = 8 * (water_1 + water_2 + water_3 + water_4 + water_5 + water_6 + water_7 + water_8);
    }

    protected void onGameStateChanged(final GameState game_state) {
        if (game_state == GameState.LOADING) {
            patches_empty.clear();
            plants_blighted.clear();
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        final Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

        // Water cans.
//        if (config.highlightWateringCans() && inventory != null) {
//            final WidgetItem item = inventory.getWidgetItem(0);
//            Rectangle bounds = item.getCanvasBounds();
//
//            graphics.setComposite(AlphaComposite.SrcOver.derive(0.3f));
//            final BufferedImage image = items.getImage(item.getId(), item.getQuantity(), false);
//            graphics.drawImage(image, (int) bounds.getX(), (int) bounds.getY(), null);
//            graphics.setComposite(AlphaComposite.SrcOver);
//        }

        // Empty tithe patches.
        if (config.highlightFarmPatches()) {
            for (final GameObject patch : patches_empty) {
                renderPie(graphics, patch, color_gray);
            }
        }

        // Blighted plants.
        final Date now = new Date();
        for (final Map.Entry<GameObject, Date> plant_blighted : plants_blighted.entrySet()) {
            renderPie(graphics, plant_blighted.getKey(), color_red, 1 - (now.getTime() - plant_blighted.getValue().getTime()) / PLANT_BLIGHTED_TIME);
        }

        // Water panel.
        panel_water.getChildren().clear();
        panel_water.getChildren().add(LineComponent.builder().left("Waters:").right(water_current + "/" + water_total).build());

        return null;
    }
}
