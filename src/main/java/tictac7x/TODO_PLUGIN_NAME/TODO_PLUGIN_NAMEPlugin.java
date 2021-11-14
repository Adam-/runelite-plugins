package tictac7x.TODO_PLUGIN_NAME;

import javax.inject.Inject;
import net.runelite.api.Client;
import lombok.extern.slf4j.Slf4j;
import com.google.inject.Provides;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "TODO_PLUGIN_DISPLAY_NAME",
	description = "TODO_PLUGIN_DESCRIPTION",
	tags = { "TODO_PLUGIN_TAGS"	}
)
public class TODO_PLUGIN_NAMEPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private TODO_PLUGIN_NAMEConfig config;

	@Provides
    TODO_PLUGIN_NAMEConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(TODO_PLUGIN_NAMEConfig.class);
	}

	@Override
	protected void startUp() {
	}

	@Override
	protected void shutDown() {
	}
}
