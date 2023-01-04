package info.sigterm.plugins.itemfiller;

import com.google.inject.Provides;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
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

	private Widget invUpdateWidget;

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
		// also maybe invbig update etc..
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		// [proc,interface_inv_update_big]
		if (scriptPreFired.getScriptId() == 153)
		{
			// [proc,interface_inv_update_big](component $component0, inv $inv1, int $int2, int $int3, int $int4, component $component5, string $string0, string $string1, string $string2, string $string3, string $string4, string $string5, string $string6, string $string7, string $string8)
			int w = client.getIntStack()[client.getIntStackSize() - 6]; // first argument
			invUpdateWidget = client.getWidget(w);
		}
		// [proc,deathkeep_left_setsection]
		else if (scriptPreFired.getScriptId() == 975)
		{
			// [proc,deathkeep_left_setsection](string $text0, component $component0, int $comsubid1, int $int2, int $int3, int $int4)
			int w = client.getIntStack()[client.getIntStackSize() - 5]; // second argument
			invUpdateWidget = client.getWidget(w);
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		int id = scriptPostFired.getScriptId();

		// [proc,inventory_build]
		if (id == 6010)
		{
			Widget w = client.getWidget(WidgetInfo.INVENTORY);
			replaceItems(w);
		}
		// [proc,ge_offer_side_draw]
		else if (id == 783)
		{
			replaceItems(client.getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER));
		}
		// [proc,ge_pricechecker_redraw]
		else if (id == 787)
		{
			replaceItems(client.getWidget(WidgetInfo.GUIDE_PRICES_ITEMS_CONTAINER));
		}
		// [proc,itemsets_side_draw]
		else if (id == 827)
		{
			replaceItems(client.getWidget(430, 0));
		}
		// [proc,interface_inv_update_big]
		// [proc,deathkeep_left_redraw]
		else if (id == 153 || id == 975)
		{
			if (invUpdateWidget != null)
			{
				replaceItems(invUpdateWidget);
				invUpdateWidget = null;
			}
		}
	}

	private void replaceItems(Widget w)
	{
		if (w == null)
		{
			return;
		}

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
