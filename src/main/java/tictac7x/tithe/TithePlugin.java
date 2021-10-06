package tictac7x.tithe;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Tithe Farm Improvements",
	description = "Additions to Tithe Farm official plugin",
	tags = {
		"tithe",
		"farm",
		"bologano"
	}
)
public class TithePlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlays;

	@Inject
	private ItemManager items;

	@Inject
	private TitheConfig config;

	@Inject
	private TitheOverlayPlants overlay_plants;

	@Inject
	private TithePanelWater panel_water;

	@Override
	protected void startUp() throws Exception {
		overlays.add(overlay_plants);
		overlays.add(panel_water);
	}

	@Override
	protected void shutDown() throws Exception {
		overlays.remove(overlay_plants);
		overlays.remove(panel_water);
	}

	@Subscribe
	protected void onGameObjectSpawned(final GameObjectSpawned event) {
		overlay_plants.onGameObjectSpawned(event.getGameObject());
	}

	@Subscribe
	protected void onGameObjectDespawned(final GameObjectDespawned event) {
		overlay_plants.onGameObjectDespawned(event.getGameObject());
	}

	@Subscribe
	protected void onGameStateChanged(final GameStateChanged event) {
		overlay_plants.onGameStateChanged(event.getGameState());
	}

	@Subscribe
	protected void onItemContainerChanged(final ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
			panel_water.onItemContainerChanged(event.getItemContainer());
		}
	}

	@Provides
	TitheConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TitheConfig.class);
	}
}
