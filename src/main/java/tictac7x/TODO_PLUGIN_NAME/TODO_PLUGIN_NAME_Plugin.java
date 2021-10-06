package tictac7x.TODO_PLUGIN_NAME;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "TODO_PLUGIN_NAME",
	description = "TODO_PLUGIN_DESCRIPTION",
	tags = {
		"TODO_PLUGIN_TAGS",
		"TODO_PLUGIN_TAGS"
	}
)
public class TODO_PLUGIN_NAME_Plugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private TODO_PLUGIN_NAME_Config config;

	@Override
	protected void startUp() throws Exception {
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception {
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Provides
	TODO_PLUGIN_NAME_Config provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TODO_PLUGIN_NAME_Config.class);
	}
}
