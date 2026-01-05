package info.sigterm.plugins.smevent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "SM Event"
)
public class SMEvent extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SMEventConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PlayerOverlay playerOverlay;

	@Inject
	private Gson gson;

	private final Map<String, PlayerConfig> configs = new HashMap<>();
	final Map<Player, SMPlayer> players = new HashMap<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(playerOverlay);
		clientThread.invokeLater(this::loadConfig);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(playerOverlay);
		clientThread.invokeLater(() ->
		{
			for (SMPlayer p : players.values())
			{
				if (p.rlo != null)
				{
					p.rlo.setActive(false);
					p.rlo = null;
				}
			}
			players.clear();
			configs.clear();
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("smevent"))
		{
			clientThread.invokeLater(this::loadConfig);
		}
	}

	@Provides
	SMEventConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SMEventConfig.class);
	}

	private void loadConfig()
	{
		List<PlayerConfig> c = config.config().isBlank() ? Collections.emptyList() : gson.fromJson(config.config(), new TypeToken<List<PlayerConfig>>()
		{
		}.getType());

		configs.clear();
		for (PlayerConfig pc : c)
		{
			configs.put(Text.sanitize(pc.name), pc);
		}

		for (SMPlayer p : players.values())
		{
			if (p.rlo != null)
			{
				p.rlo.setActive(false);
				p.rlo = null;
			}
		}
		players.clear();

		for (Player player : client.getPlayers())
		{
			PlayerConfig pc = configs.get(Text.sanitize(player.getName()));
			if (pc != null)
			{
				SMPlayer p = new SMPlayer();
				p.player = player;
				p.config = pc;
				players.put(p.player, p);
			}
		}
	}

	@Subscribe
	public void onWorldViewLoaded(WorldViewLoaded event)
	{
		if (event.getWorldView().isTopLevel())
		{
			for (SMPlayer p : players.values())
			{
				if (p.rlo != null)
				{
					p.rlo.setActive(false);
					p.rlo = null;
				}
			}
		}
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event)
	{
		PlayerConfig pc = configs.get(Text.sanitize(event.getPlayer().getName()));
		if (pc != null)
		{
			SMPlayer p = new SMPlayer();
			p.player = event.getPlayer();
			p.config = pc;
			players.put(p.player, p);
		}
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		SMPlayer p = players.get(event.getPlayer());
		if (p != null)
		{
			if (p.rlo != null)
			{
				p.rlo.setActive(false);
			}
			players.remove(event.getPlayer());
		}
	}
}
