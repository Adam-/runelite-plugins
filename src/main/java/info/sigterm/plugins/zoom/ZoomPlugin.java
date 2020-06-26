package info.sigterm.plugins.zoom;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Zoom Extender",
	description = "Increases the inner zoom limit further"
)
public class ZoomPlugin extends Plugin
{
	private static final int INNER_ZOOM_LIMIT = 1200;

	@Inject
	private Client client;

	@Subscribe(priority = -1) // after camera plugin
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (event.getEventName().equals("innerZoomLimit"))
		{
			int[] intStack = client.getIntStack();
			int intStackSize = client.getIntStackSize();
			intStack[intStackSize - 1] = INNER_ZOOM_LIMIT;
		}
	}
}
