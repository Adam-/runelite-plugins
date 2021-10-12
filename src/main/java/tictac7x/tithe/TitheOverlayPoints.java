package tictac7x.tithe;

import tictac7x.Overlay;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.ItemID;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class TitheOverlayPoints extends Overlay {
    private final TithePlugin plugin;
    private final TitheConfig config;
    private final Client client;
    private final PanelComponent panel = new PanelComponent();

    private final static int TITHE_FARM_POINTS = Varbits.TITHE_FARM_POINTS.getId();
    private final static int TITHE_FARM_SACK = Varbits.TITHE_FARM_SACK_AMOUNT.getId();
    private final static int TITHE_FARM_SACK_TOTAL = 100;

    private int points_total = 0;
    private int fruits_sack = 0;
    private int fruits_inventory = 0;
    private int seeds_inventory = 0;

    public TitheOverlayPoints(final TithePlugin plugin, final TitheConfig config, final Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        setPosition(OverlayPosition.TOP_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void shutDown() {
        final Widget widget_tithe = client.getWidget(WidgetInfo.TITHE_FARM);
        if (widget_tithe != null && widget_tithe.isHidden()) {
            widget_tithe.setHidden(false);
        }
    }

    public void onVarbitChanged(final VarbitChanged event) {
        final int points = client.getVarbitValue(TITHE_FARM_POINTS);
        final int sack = client.getVarbitValue(TITHE_FARM_SACK);

        // Amount of points.
        if (this.points_total != points) {
            this.points_total = points;
        }

        // Amount of fruit in the sack.
        if (this.fruits_sack != sack) {
            this.fruits_sack = sack;
        }
    }

    public void onItemContainerChanged(final ItemContainerChanged event) {
        if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
            final ItemContainer inventory = event.getItemContainer();
            fruits_inventory = inventory.count(ItemID.GOLOVANOVA_FRUIT) + inventory.count(ItemID.BOLOGANO_FRUIT) + inventory.count(ItemID.LOGAVANO_FRUIT);
            seeds_inventory = inventory.count(ItemID.GOLOVANOVA_SEED) + inventory.count(ItemID.BOLOGANO_SEED) + inventory.count(ItemID.LOGAVANO_SEED);
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        final Widget widget_tithe = client.getWidget(WidgetInfo.TITHE_FARM);

        if (config.showCustomPoints()) {
            if (widget_tithe != null && !widget_tithe.isHidden()) widget_tithe.setHidden(true);

            final int fruits = fruits_sack + fruits_inventory;
            final int fruits_possible = fruits + seeds_inventory;
            final int points_added = Math.max(0, fruits - 74);

            panel.getChildren().clear();

            // Total points.
            panel.getChildren().add(LineComponent.builder()
                .left("Points:").leftColor(color_orange)
                .right((points_total - points_added) + (points_added > 0 ? " +" + points_added : "")).rightColor(color_orange)
                .build()
            );

            // Fruits.
            panel.getChildren().add(LineComponent.builder()
                .left("Fruits:").leftColor(color_gray)
                .right((fruits_sack + fruits_inventory) + "/" + TITHE_FARM_SACK_TOTAL)
                .rightColor(
                    fruits_possible == TITHE_FARM_SACK_TOTAL ? color_green :
                    fruits_possible > 76 ? color_yellow :
                    color_red)
                .build()
            );

            return panel.render(graphics);
        } else {
            if (widget_tithe != null && widget_tithe.isHidden()) widget_tithe.setHidden(false);
        }

        return null;
    }
}
