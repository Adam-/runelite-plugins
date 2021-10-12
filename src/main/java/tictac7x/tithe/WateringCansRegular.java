package tictac7x.tithe;

import net.runelite.api.ItemID;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;

public class WateringCansRegular {
    private final Client client;
    private Integer water_remaining = null;
    private Integer water_total = null;

    public WateringCansRegular(final Client client) {
        this.client = client;
    }

    public int getWaterRemaining() {
        return water_remaining != null ? water_remaining : 0;
    }

    public int getWaterTotal() {
        return water_total != null ? water_total : 0;
    }

    public void onItemContainerChanged(final ItemContainerChanged event) {
        if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
            updateWaterCharges();
        }
    }

    public void onGameTick() {
        if (water_remaining == null) {
            updateWaterCharges();
        }
    }

    private void updateWaterCharges() {
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

        if (inventory != null) {
            int water_remaining = 0;

            int water_0   = inventory.count(ItemID.WATERING_CAN);
            int water_1   = inventory.count(ItemID.WATERING_CAN1);
            int water_2   = inventory.count(ItemID.WATERING_CAN2);
            int water_3   = inventory.count(ItemID.WATERING_CAN3);
            int water_4   = inventory.count(ItemID.WATERING_CAN4);
            int water_5   = inventory.count(ItemID.WATERING_CAN5);
            int water_6   = inventory.count(ItemID.WATERING_CAN6);
            int water_7   = inventory.count(ItemID.WATERING_CAN7);
            int water_8   = inventory.count(ItemID.WATERING_CAN8);

            water_remaining += 1 * water_1;
            water_remaining += 2 * water_2;
            water_remaining += 3 * water_3;
            water_remaining += 4 * water_4;
            water_remaining += 5 * water_5;
            water_remaining += 6 * water_6;
            water_remaining += 7 * water_7;
            water_remaining += 8 * water_8;

            this.water_remaining = water_remaining;
            this.water_total = 8 * (water_0 + water_1 + water_2 + water_3 + water_4 + water_5 + water_6 + water_7 + water_8);
        } else {
            this.water_remaining = null;
            this.water_total = null;
        }
    }
}
