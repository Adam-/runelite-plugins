package tictac7x.tithe;

import tictac7x.Overlay;
import java.util.Map;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TitheOverlayInventory extends Overlay {
    private final Client client;
    private final ItemManager items;
    private final TithePlugin plugin;
    private final TitheConfig config;

    private final int color_alpha = 60;
    private final Color color_green = getColor(Overlay.color_green, color_alpha);
    private final Color color_yellow = getColor(Overlay.color_yellow, color_alpha);
    private final Color color_blue = getColor(Overlay.color_blue, color_alpha);
    private final Color color_red = getColor(Overlay.color_red, color_alpha);

    private final Map<Integer, Color> seeds = new HashMap<Integer, Color>(){{
        put(ItemID.GOLOVANOVA_SEED, color_green);
        put(ItemID.BOLOGANO_SEED,   color_green);
        put(ItemID.LOGAVANO_SEED,   color_green);
    }};

    private final Map<Integer, Color> watering_cans = new HashMap<Integer, Color>(){{
        put(ItemID.WATERING_CAN8, color_blue);
        put(ItemID.WATERING_CAN7, color_yellow);
        put(ItemID.WATERING_CAN6, color_yellow);
        put(ItemID.WATERING_CAN5, color_yellow);
        put(ItemID.WATERING_CAN4, color_yellow);
        put(ItemID.WATERING_CAN3, color_yellow);
        put(ItemID.WATERING_CAN2, color_yellow);
        put(ItemID.WATERING_CAN1, color_yellow);
        put(ItemID.WATERING_CAN,  color_red);
    }};


    @Inject
    public TitheOverlayInventory(final Client client, final TithePlugin plugin, final TitheConfig config, final ItemManager items) {
        this.client = client;
        this.items = items;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (!plugin.inTitheFarm()) return null;

        // Highlight seeds.
        if (config.highlightSeeds()) {
            highlightInventoryItems(client, items, graphics, seeds);
        }

        // Highlight water cans.
        if (config.highlightWaterCans()) {
            highlightInventoryItems(client, items, graphics, watering_cans);
        }

        return null;
    }
}
