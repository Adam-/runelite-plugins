package com.BetterGodwarsOverlay;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
	name = "Better Godwars Overlay",
	description = "Goodbye to Jagex's ugly GWD overlay",
	tags = {"gwd", "pvm", "bossing"}
)
public class BetterGodwarsOverlayPlugin extends Plugin
{

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private BetterGodwarsOverlayOverlay gwdOverlay;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private BetterGodwarsOverlayConfig config;

	@Provides
	BetterGodwarsOverlayConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterGodwarsOverlayConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(gwdOverlay);

	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(gwdOverlay);

		//restore widgets
		final Widget killCount = client.getWidget(WidgetInfo.GWD_KC);
		if (killCount != null)
		{
			killCount.setHidden(false);
		}

	}


}
