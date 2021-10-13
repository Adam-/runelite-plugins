package tictac7x.tithe;

import java.util.Map;
import java.util.Set;
import com.google.inject.Provides;
import com.google.common.collect.ImmutableSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.game.ItemManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Tithe Farm Improvements",
	description = "Additions to Tithe Farm official plugin",
	tags = { "tithe", "farm", "farmer", "golovanova", "bologano", "logavano", "gricoller" },
	conflicts = "Tithe Farm"
)
public class TithePlugin extends Plugin {
	private final Set<Integer> TITHE_FARM_REGIONS = ImmutableSet.of(6966, 6967, 7222, 7223);

	@Inject
	private TitheConfig config;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlays;

	@Inject
	private ItemManager items;

	@Inject
	private ConfigManager configs;

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
			watering_cans     = new WateringCansRegular(client);
			gricollers_can    = new WateringCanGricollers(this, config, watering_cans, client, configs);
			overlay_water     = new TitheOverlayWater(this, config, watering_cans, gricollers_can);
			overlay_plants    = new TitheOverlayPlants(this, config, client);
			overlay_points    = new TitheOverlayPoints(this, config, client);
			overlay_patches   = new TitheOverlayPatches(this, config, client);
			overlay_inventory = new TitheOverlayInventory(this, config, client);
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
		overlay_points.shutDown();
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
		watering_cans.onGameTick();
		gricollers_can.onGameTick();
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

	@Provides
	TitheConfig provideConfig(final ConfigManager configs) {
		return configs.getConfig(tictac7x.tithe.TitheConfig.class);
	}

	public boolean inTitheFarm() {
		final int[] regions = client.getMapRegions();

		for (final int region : regions) {
			if (!TITHE_FARM_REGIONS.contains(region)) {
				return false;
			}
		}

		return true;
	}

	public Map<LocalPoint, TithePlant> getPlayerPlants() {
		return overlay_plants.plants;
	}

	public int countPlayerPlantsNotBlighted() {
		return (int) getPlayerPlants().values().stream().filter(
			plant ->
				plant.cycle_state != TithePlant.State.BLIGHTED &&
				plant.cycle_state != TithePlant.State.EMPTY
		).count();
	}
}
