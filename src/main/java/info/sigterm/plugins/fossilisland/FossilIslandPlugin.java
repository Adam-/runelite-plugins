package info.sigterm.plugins.fossilisland;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(
	name = "Fossil Island",
	description = "Removes scenery from Fossil Island to improve performance"
)
@Slf4j
public class FossilIslandPlugin extends Plugin
{
	private static final Set<Integer> HIDE = ImmutableSet.of(
		30822, // Small white mushrooms A
		30825, // Small white mushrooms B
		30799, // Small yellow mushrooms A
		30823, // smallRedMushroomsA
		30824, // smallBlueMushroomsA
		30828, // smallBlueMushroomsB
		30829, // smallBlueMushroomsC
		30830, // smallRedPlantsA
		30831, // smallRedPlantsB
		30826,  // smallFlowerA
		30827, // smallFlowerB
		30834, // mediumYellowMushroomA
		30835, // mediumBlueMushroomA
		30836, // mediumRedMushroomA
		30832, // mediumRedPlantA
		30840 // mediumRedPlantB
	);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(this::hide);
		}
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			hide();
		}
	}

	private void hide()
	{
		if (!isInMushroomForest())
		{
			return;
		}

		Scene scene = client.getScene();
		Tile[][] tiles = scene.getTiles()[0];
		int cnt = 0;
		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[x][y];
				if (tile == null)
				{
					continue;
				}

				for (GameObject gameObject : tile.getGameObjects())
				{
					if (gameObject != null && HIDE.contains(gameObject.getId()))
					{
						scene.removeGameObject(gameObject);
						++cnt;
						break;
					}
				}
			}
		}

		log.debug("Removed {} objects", cnt);
	}

	private boolean isInMushroomForest()
	{
		// 57,60 or 57,59
		return ArrayUtils.contains(client.getMapRegions(), (57 << 8) | 60)
			|| ArrayUtils.contains(client.getMapRegions(), (57 << 8) | 59);
	}
}
