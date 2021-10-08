package tictac7x.tithe;

import tictac7x.Overlay;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import javax.inject.Inject;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;
import java.util.HashMap;

public class TitheOverlayInventory extends Overlay {
    private final Client client;
    private final ItemManager items;
    private final TitheConfig config;

    private final int color_alpha = 60;
    private final Color color_green = getColor(super.color_green, color_alpha);
    private final Color color_yellow = getColor(super.color_yellow, color_alpha);
    private final Color color_blue = getColor(super.color_blue, color_alpha);
    private final Color color_red = getColor(super.color_red, color_alpha);

    private final Map<Integer, Color> items_to_highlight = new HashMap<Integer, Color>(){{
        put(ItemID.GOLOVANOVA_SEED, color_green);
        put(ItemID.BOLOGANO_SEED,   color_green);
        put(ItemID.LOGAVANO_SEED,   color_green);
        put(ItemID.WATERING_CAN8,   color_blue);
        put(ItemID.WATERING_CAN7,   color_yellow);
        put(ItemID.WATERING_CAN6,   color_yellow);
        put(ItemID.WATERING_CAN5,   color_yellow);
        put(ItemID.WATERING_CAN4,   color_yellow);
        put(ItemID.WATERING_CAN3,   color_yellow);
        put(ItemID.WATERING_CAN2,   color_yellow);
        put(ItemID.WATERING_CAN1,   color_yellow);
        put(ItemID.WATERING_CAN,    color_red);
    }};


    @Inject
    public TitheOverlayInventory(final Client client, final TitheConfig config, final ItemManager items) {
        this.client = client;
        this.items = items;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        highlightInventoryItems(client, items, graphics, items_to_highlight);
        return null;
    }
}
