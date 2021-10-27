package com.BetterGodwarsOverlay;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class BetterGodwarsOverlayOverlay extends OverlayPanel
{

	private final Client client;
	private final BetterGodwarsOverlayConfig config;

	@Inject
	private BetterGodwarsOverlayOverlay(BetterGodwarsOverlayPlugin plugin, Client client, BetterGodwarsOverlayConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
		this.client = client;
		this.config = config;

		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Godwars Overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		//hide original overlay
		final Widget godwars = client.getWidget(WidgetInfo.GWD_KC);
		if (godwars != null)
		{
			godwars.setHidden(true);
		}

		for (BetterGodwarsOverlayGods gods : BetterGodwarsOverlayGods.values())
		{

			if (godwars == null)
			{
				continue;
			}

			final int killcounts = client.getVarbitValue(gods.getKillCountVarbit().getId());

			panelComponent.getChildren().add(LineComponent.builder()
				.left(config.shortGodNames() ? gods.getName().substring(0, 2) : gods.getName())
				.right(Integer.toString(killcounts))
				.leftColor(config.godNameColor())
				.rightColor(killcounts >= config.highLightOnKC() ? Color.GREEN : Color.WHITE)
				.build());
		}

		return super.render(graphics);


	}
}
