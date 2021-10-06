package tictac7x.tithe;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
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
	private TitheOverlay overlay;

	@Override
	protected void startUp() throws Exception {
		overlays.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlays.remove(overlay);
	}

	@Subscribe
	protected void onGameObjectSpawned(final GameObjectSpawned game_object) {
		overlay.patchSpawned(game_object.getGameObject());
	}

	@Subscribe
	protected void onGameObjectDespawned(final GameObjectDespawned game_object) {
		overlay.patchDespawned(game_object.getGameObject());
	}

	@Subscribe
	protected void onGameStateChanged(final GameStateChanged game_state) {
		overlay.gameStateChanged(game_state.getGameState());
	}

	@Provides
	TitheConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TitheConfig.class);
	}
}
