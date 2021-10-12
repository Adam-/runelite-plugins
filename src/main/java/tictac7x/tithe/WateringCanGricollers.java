package tictac7x.tithe;

import net.runelite.api.ItemID;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;


public class WateringCanGricollers {
    public static final int CHARGES_TOTAL = 1000;

    private final TithePlugin plugin;
    private final TitheConfig config;
    private final WateringCansRegular inventory;
    private final Client client;
    private final ConfigManager configs;

    private Integer inventory_water_remaining = null;
    private Integer water_remaining = null;
    private Integer water_total = null;

    public WateringCanGricollers(final TithePlugin plugin, final TitheConfig config, final WateringCansRegular inventory, final Client client, final ConfigManager configs) {
        this.plugin = plugin;
        this.config = config;
        this.inventory = inventory;
        this.client = client;
        this.configs = configs;
    }

    public int getWaterRemaining() {
        return water_remaining != null ? water_remaining : 0;
    }

    public int getWaterTotal() {
        return water_total != null ? water_total : 0;
    }

    public void onChatMessage(final ChatMessage event) {
        // Player checked Gricollers can charges.
        if (event.getType() == ChatMessageType.GAMEMESSAGE && event.getMessage().contains("Watering can charges remaining:")) {
            updateGricollersCanCharges(Double.parseDouble(event.getMessage().split(":")[1].replace("%", "")));

        // Player fills a watering can.
        } else if (event.getType() == ChatMessageType.SPAM && event.getMessage().equals("You fill the watering can from the water barrel.")) {
            // Gricoller's can was filled.
            if (inventory_water_remaining == inventory.getWaterRemaining()) {
                updateGricollersCanCharges(CHARGES_TOTAL);
            }
        }
    }

    public void onItemContainerChanged(final ItemContainerChanged event) {
        if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
            updateWaterCharges();
        }
    }

    public void onGameObjectSpawned(final GameObject game_object) {
        // Game object is some sort of tithe patch.
        if (TithePatch.isWatered(game_object)) {
            final LocalPoint location_plant = game_object.getLocalLocation();

            // Watered plant is player owned.
            if (plugin.getPlayerPlants().containsKey(location_plant)) {
                // If remaining water charges didn't change, Gricollers can was used.
                if (inventory.getWaterRemaining() == inventory_water_remaining) {
                    updateGricollersCanCharges(config.getGricollersCanCharges() - 1);
                }
            }
        }
    }

    public void onGameTick() {
        if (water_remaining == null) {
            updateWaterCharges();
        }
    }

    private void updateGricollersCanCharges(final double percentage) {
        updateGricollersCanCharges((int) percentage * CHARGES_TOTAL / 100);
    }

    private void updateGricollersCanCharges(final int charges) {
        configs.setConfiguration(config.group, config.gricollers_can_charges, charges);
        updateWaterCharges();
    }

    private void updateWaterCharges() {
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

        if (inventory != null) {
            this.inventory_water_remaining = this.inventory.getWaterRemaining();
            this.water_remaining = inventory.count(ItemID.GRICOLLERS_CAN) * config.getGricollersCanCharges();
            this.water_total = inventory.count(ItemID.GRICOLLERS_CAN) * CHARGES_TOTAL;
        } else {
            this.inventory_water_remaining = null;
            this.water_remaining = null;
            this.water_total = null;
        }
    }
}
