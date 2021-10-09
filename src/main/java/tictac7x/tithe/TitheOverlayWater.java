package tictac7x.tithe;

import tictac7x.Overlay;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;


public class TitheOverlayWater extends Overlay {
    private final Client client;
    private final ItemManager items;
    private final TitheConfig config;

    private final PanelComponent panel_water = new PanelComponent();

    private int water_current = 0;
    private int water_total = 0;
    private int water_high = 80;
    private int water_low = 10;

    @Inject
    public TitheOverlayWater(final Client client, final TitheConfig config, final ItemManager items) {
        this.client = client;
        this.items = items;
        this.config = config;

        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setPriority(OverlayPriority.HIGH);
    }

    protected void onItemContainerChanged(final ItemContainer item_container) {
        int water_current = 0;

        int water_0 = item_container.count(ItemID.WATERING_CAN);
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
        this.water_total = 8 * (water_0 + water_1 + water_2 + water_3 + water_4 + water_5 + water_6 + water_7 + water_8);
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (water_total == 0 || !config.showWaterAmount()) return null;

        final int water_remaining = water_current * 100 / water_total;
        final Color color =
            water_remaining >= water_high ? color_green :
            water_remaining >= water_low ? color_yellow :
            color_red;

        panel_water.getChildren().clear();
        panel_water.getChildren().add(
            LineComponent.builder()
                .left("Water:")
                .right(water_current + "/" + water_total)
                .build()
        );
        panel_water.setBackgroundColor(getColor(color, panel_background_alpha));

        return panel_water.render(graphics);
    }
}
