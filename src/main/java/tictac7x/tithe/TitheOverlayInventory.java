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

    private final Map<Integer, Color> seeds = new HashMap<>();
    private final Map<Integer, Color> watering_cans = new HashMap<>();
    private final Map<Integer, Color> farmers_outfit = new HashMap<>();

    public TitheOverlayInventory(final TithePlugin plugin, final TitheConfig config, final Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        seeds.put(ItemID.GOLOVANOVA_SEED, config.getHighlightSeedsColor());
        seeds.put(ItemID.BOLOGANO_SEED,   config.getHighlightSeedsColor());
        seeds.put(ItemID.LOGAVANO_SEED,   config.getHighlightSeedsColor());

        watering_cans.put(ItemID.WATERING_CAN8, getColor(color_blue, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN7, getColor(color_yellow, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN6, getColor(color_yellow, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN5, getColor(color_yellow, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN4, getColor(color_yellow, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN3, getColor(color_yellow, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN2, getColor(color_yellow, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN1, getColor(color_yellow, alpha_normal));
        watering_cans.put(ItemID.WATERING_CAN,  getColor(color_red, alpha_normal));

        farmers_outfit.put(ItemID.FARMERS_STRAWHAT, getColor(color_red, alpha_normal));
        farmers_outfit.put(ItemID.FARMERS_JACKET, getColor(color_red, alpha_normal));
        farmers_outfit.put(ItemID.FARMERS_BORO_TROUSERS, getColor(color_red, alpha_normal));
        farmers_outfit.put(ItemID.FARMERS_BOOTS, getColor(color_red, alpha_normal));
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (plugin.inTitheFarm()) {
            // Highlight seeds.
            highlightInventoryItems(client, graphics, seeds);

            // Highlight regular watering cans.
            highlightInventoryItems(client, graphics, watering_cans);

            // Gricoller's can empty.
            if (config.getGricollersCanCharges() == 0) {
                highlightInventoryItem(client, graphics, ItemID.GRICOLLERS_CAN, getColor(color_red, alpha_normal));

                // Gricoller's can has enough charges for 1 run (25 plants).
            } else if (config.getGricollersCanCharges() < 75) {
                highlightInventoryItem(client, graphics, ItemID.GRICOLLERS_CAN, getColor(color_yellow, alpha_normal));

                // Gricoller's can has charges for multiple runs.
            } else {
                highlightInventoryItem(client, graphics, ItemID.GRICOLLERS_CAN, getColor(color_blue, alpha_normal));
            }

            // Highlight farmers outfit.
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

        return null;
    }
}
