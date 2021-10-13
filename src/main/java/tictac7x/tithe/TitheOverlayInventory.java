package tictac7x.tithe;

import tictac7x.Overlay;
import java.util.Map;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TitheOverlayInventory extends Overlay {
    private final Client client;
    private final TithePlugin plugin;
    private final TitheConfig config;

    private final Map<Integer, Color> seeds = new HashMap<Integer, Color>(){{
        put(ItemID.GOLOVANOVA_SEED, Overlay.color_green);
        put(ItemID.BOLOGANO_SEED,   Overlay.color_green);
        put(ItemID.LOGAVANO_SEED,   Overlay.color_green);
    }};

    private final Map<Integer, Color> watering_cans = new HashMap<Integer, Color>(){{
        put(ItemID.WATERING_CAN8, Overlay.color_blue);
        put(ItemID.WATERING_CAN7, Overlay.color_yellow);
        put(ItemID.WATERING_CAN6, Overlay.color_yellow);
        put(ItemID.WATERING_CAN5, Overlay.color_yellow);
        put(ItemID.WATERING_CAN4, Overlay.color_yellow);
        put(ItemID.WATERING_CAN3, Overlay.color_yellow);
        put(ItemID.WATERING_CAN2, Overlay.color_yellow);
        put(ItemID.WATERING_CAN1, Overlay.color_yellow);
        put(ItemID.WATERING_CAN,  Overlay.color_red);
    }};

    private final Map<Integer, Color> farmers_outfit = new HashMap<Integer, Color>(){{
        put(ItemID.FARMERS_STRAWHAT, color_red);
        put(ItemID.FARMERS_JACKET, color_red);
        put(ItemID.FARMERS_BORO_TROUSERS, color_red);
        put(ItemID.FARMERS_BOOTS, color_red);
    }};

    public TitheOverlayInventory(final TithePlugin plugin, final TitheConfig config, final Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (plugin.inTitheFarm()) {
            // Highlight seeds.
            if (config.highlightSeeds()) {
                highlightInventoryItems(client, graphics, seeds);
            }

            // Highlight watering cans.
            if (config.highlightWaterCans()) {
                // Highlight regular watering cans.
                highlightInventoryItems(client, graphics, watering_cans);

                // Gricoller's can empty.
                if (config.getGricollersCanCharges() == 0) {
                    highlightInventoryItem(client, graphics, ItemID.GRICOLLERS_CAN, Overlay.color_red);

                    // Gricoller's can has enough charges for 1 run (25 plants).
                } else if (config.getGricollersCanCharges() < 75) {
                    highlightInventoryItem(client, graphics, ItemID.GRICOLLERS_CAN, Overlay.color_yellow);

                    // Gricoller's can has charges for multiple runs.
                } else {
                    highlightInventoryItem(client, graphics, ItemID.GRICOLLERS_CAN, Overlay.color_blue);
                }
            }

            // Highlight farmers outfit.
            if (config.highlightFarmersOutfit()) {
                final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

                if (
                    plugin.countPlayerPlantsNotBlighted() == 0 && inventory != null
                    && inventory.count(ItemID.GOLOVANOVA_SEED) == 0
                    && inventory.count(ItemID.BOLOGANO_SEED) == 0
                    && inventory.count(ItemID.LOGAVANO_SEED) == 0
                ) {
                    highlightInventoryItems(client, graphics, farmers_outfit);
                }
            }
        }

        return null;
    }
}
