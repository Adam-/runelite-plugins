package info.sigterm.plugins.playtime;

import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@PluginDescriptor(
	name = "Playtime",
	description = "Shows an overlay of your accounts playtime"
)
public class PlayTimePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PlayTimeOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	private int startMinutes;
	private long startMs;
	String overlayText;

	@Override
	protected void startUp()
	{
		overlayText = null;
		startMinutes = -1;
		startMs = 0L;
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
				startMinutes = -1;
		}
	}

	@Schedule(
		period = 1,
		unit = ChronoUnit.SECONDS
	)
	public void tick()
	{
		int varcMinutes = client.getVarcIntValue(526);
		if (varcMinutes != startMinutes)
		{
			startMinutes = varcMinutes;
			startMs = System.currentTimeMillis();
		}

		long now = System.currentTimeMillis();

		int total = varcMinutes * 60 // seconds
			+ (int) ((now - startMs) / 1000L);

		int days = total / 86400;
		total %= 84600;

		int hours = total / 3600;
		total %= 3600;

		int minutes = total / 60;

		StringBuilder sb = new StringBuilder("Time played: ");
		if (days > 0)
		{
			sb.append(days).append(' ').append(days == 1 ? "day" : "days").append(' ');
		}
		if (hours > 0)
		{
			sb.append(hours).append(' ').append(hours == 1 ? "hour" : "hours").append(' ');
		}
		sb.append(minutes).append(' ').append(minutes == 1 ? "min" : "mins");

		overlayText = sb.toString();
	}
}
