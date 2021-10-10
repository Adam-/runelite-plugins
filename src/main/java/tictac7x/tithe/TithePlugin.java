package tictac7x.tithe;

import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.events.*;
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
		"golovanova",
		"bologano",
		"logavano"
	}
)
public class TithePlugin extends Plugin {
	private final Set<Integer> TITHE_FARM_REGIONS = ImmutableSet.of(6965, 6966, 6967, 7221, 7222, 7223);

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlays;

	@Inject
	private ItemManager items;

	@Inject
	private TitheConfig config;

	@Inject
	private TitheOverlayPatches overlay_patches;

	@Inject
	private TitheOverlayWater overlay_water;

	@Inject
	private TitheOverlayInventory overlay_inventory;

	@Override
	protected void startUp() throws Exception {
		overlays.add(overlay_patches);
		overlays.add(overlay_water);
		overlays.add(overlay_inventory);
	}

	@Override
	protected void shutDown() throws Exception {
		overlays.remove(overlay_patches);
		overlays.remove(overlay_water);
		overlays.remove(overlay_inventory);
	}

	protected boolean inTitheFarm() {
		final int[] regions = client.getMapRegions();

		for (final int region : regions) {
			if (!TITHE_FARM_REGIONS.contains(region)) {
				return false;
			}
		}

		return true;
	}

	@Subscribe
	protected void onGameObjectSpawned(final GameObjectSpawned event) {
		overlay_patches.onGameObjectSpawned(event.getGameObject());
	}

	@Subscribe
	protected void onGameObjectDespawned(final GameObjectDespawned event) {
		overlay_patches.onGameObjectDespawned(event.getGameObject());
	}

	@Subscribe
	protected void onGameStateChanged(final GameStateChanged event) {
		overlay_patches.onGameStateChanged(event.getGameState());
	}

	@Subscribe
	protected void onItemContainerChanged(final ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
			overlay_water.onItemContainerChanged(event.getItemContainer());
		}
	}

	@Subscribe
	protected void onGameTick(final GameTick event) {
		overlay_patches.onGameTick();
	}

	@Provides
	TitheConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TitheConfig.class);
	}
}
