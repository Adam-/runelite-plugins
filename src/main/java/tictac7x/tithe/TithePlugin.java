package tictac7x.tithe;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import lombok.extern.slf4j.Slf4j;
import com.google.inject.Provides;
import net.runelite.api.GameState;
import net.runelite.api.events.*;
import net.runelite.client.plugins.Plugin;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import com.google.common.collect.ImmutableSet;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Tithe Farm Improved",
	description = "Improve overall experience for Tithe farm",
	tags = { "tithe", "farm", "farmer", "golovanova", "bologano", "logavano", "gricoller" },
	conflicts = "Tithe Farm"
)
public class TithePlugin extends Plugin {
	private final Set<Integer> REGIONS = ImmutableSet.of(6966, 6967, 7222, 7223);
	private boolean in_tithe_farm;

	@Inject
	private TitheConfig config;

	@Inject
	private Client client;

	@Inject
	private ClientThread client_thread;

	@Inject
	private OverlayManager overlays;

	@Inject
	private ItemManager items;

	@Inject
	private ConfigManager configs;

	@Provides
	TitheConfig provideConfig(final ConfigManager configs) {
		return configs.getConfig(tictac7x.tithe.TitheConfig.class);
	}

	private WateringCansRegular   watering_cans;
	private WateringCanGricollers gricollers_can;
	private TitheOverlayWater     overlay_water;
	private TitheOverlayPlants    overlay_plants;
	private TitheOverlayPoints    overlay_points;
	private TitheOverlayPatches   overlay_patches;

	private TitheOverlayInventory overlay_inventory;

	@Override
	protected void startUp() {
		if (watering_cans == null) {
			watering_cans     = new WateringCansRegular();
			gricollers_can    = new WateringCanGricollers(this, config, watering_cans, client, configs);
			overlay_water     = new TitheOverlayWater(this, config, watering_cans, gricollers_can);
			overlay_plants    = new TitheOverlayPlants(this, config, client);
			overlay_points    = new TitheOverlayPoints(this, config, client);
			overlay_patches   = new TitheOverlayPatches(this, config, client);
			overlay_inventory = new TitheOverlayInventory(this, config, gricollers_can, client);
		}

		overlays.add(overlay_water);
		overlays.add(overlay_plants);
		overlays.add(overlay_points);
		overlays.add(overlay_patches);
		overlays.add(overlay_inventory);
	}

	@Override
	protected void shutDown() {
		overlays.remove(overlay_water);
		overlays.remove(overlay_plants);
		overlays.remove(overlay_points);
		overlays.remove(overlay_patches);
		overlays.remove(overlay_inventory);
		client_thread.invokeLater(() -> overlay_points.shutDown());
	}

	@Subscribe
	public void onGameObjectSpawned(final GameObjectSpawned event) {
		overlay_plants.onGameObjectSpawned(event.getGameObject());
		gricollers_can.onGameObjectSpawned(event.getGameObject());
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event) {
		watering_cans.onItemContainerChanged(event);
		gricollers_can.onItemContainerChanged(event);
		overlay_points.onItemContainerChanged(event);
	}

	@Subscribe
	public void onGameTick(final GameTick event) {
		overlay_plants.onGameTick();
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event) {
		gricollers_can.onChatMessage(event);
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event) {
		overlay_points.onVarbitChanged();
	}

	@Subscribe
	public void onWidgetLoaded(final WidgetLoaded event) {
		if (event.getGroupId() == WidgetInfo.TITHE_FARM.getGroupId()) {
			if (config.showCustomPoints()) {
				overlay_points.hideNativePoints();
			} else {
				overlay_points.showNativePoints();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event) {
		if (event.getGroup().equals(config.group) && event.getKey().equals(config.points)) {
			client_thread.invokeLater(() -> {
				if (config.showCustomPoints()) {
					overlay_points.hideNativePoints();
				} else {
					overlay_points.showNativePoints();
				}
			});
		}
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event) {
		if (event.getGameState() == GameState.LOADING) {
			updateInTitheFarm();
		}
	}

	public boolean inTitheFarm() {
		return in_tithe_farm;
	}

	private void updateInTitheFarm() {
		final int[] regions = client.getMapRegions();

		for (final int region : regions) {
			if (REGIONS.contains(region)) {
				in_tithe_farm = true;
				return;
			}
		}

		in_tithe_farm = false;
	}

	public Map<LocalPoint, TithePlant> getPlayerPlants() {
		return overlay_plants.plants;
	}

	public int countPlayerPlantsNotBlighted() {
		return (int) getPlayerPlants().values().stream().filter(plant ->
			plant.cycle_state != TithePlant.State.BLIGHTED &&
			plant.cycle_state != TithePlant.State.EMPTY
		).count();
	}
}
