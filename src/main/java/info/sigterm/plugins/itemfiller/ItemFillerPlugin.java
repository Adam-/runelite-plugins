package info.sigterm.plugins.itemfiller;

import com.google.inject.Provides;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Item Filler"
)
public class ItemFillerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemFillerConfig config;

	@Override
	protected void startUp()
	{
		clientThread.invoke(this::redrawInventory);
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(this::redrawInventory);
	}

	@Provides
	ItemFillerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ItemFillerConfig.class);
	}

	private void redrawInventory()
	{
		client.runScript(client.getWidget(WidgetInfo.INVENTORY).getOnInvTransmitListener());
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		// [proc,inventory_build]
		if (scriptPostFired.getScriptId() == 6010)
		{
			Widget w = client.getWidget(WidgetInfo.INVENTORY);
			int filler = config.filler();
			for (Widget i : w.getDynamicChildren())
			{
				if (i.getItemId() == filler)
				{
					log.debug("Replacing {} with an item filler", i.getName());
					i.setName("Filler");
					i.setTargetVerb(null);
					i.setItemId(ItemID.BANK_FILLER);
					i.setClickMask(0);
					i.setOnDragCompleteListener(null);
					i.setOnDragListener(null);
					Arrays.fill(i.getActions(), "");
				}
			}
		}
	}
}
