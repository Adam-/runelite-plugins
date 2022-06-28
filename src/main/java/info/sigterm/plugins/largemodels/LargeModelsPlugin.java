package info.sigterm.plugins.largemodels;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Large models",
	description = "Identify large models",
	enabledByDefault = false
)
public class LargeModelsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private LargeModelsConfig config;

	@Inject
	LargeModelsOverlay largeModelsOverlay;

	@Inject
	OverlayManager overlayManager;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(largeModelsOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(largeModelsOverlay);
	}

	@Provides
	LargeModelsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LargeModelsConfig.class);
	}
}
