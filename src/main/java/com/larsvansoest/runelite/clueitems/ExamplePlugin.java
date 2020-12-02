package com.larsvansoest.runelite.clueitems;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.larsvansoest.runelite.clueitems.items.EmoteClueItemsProvider;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.HashSet;

@Slf4j
@PluginDescriptor(
	name = "Clue Items"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	private EmoteClueItemsProvider emoteClueItems;

	@Override
	protected void startUp() throws Exception
	{
		this.emoteClueItems = new EmoteClueItemsProvider();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		ItemContainer container = itemContainerChanged.getItemContainer();
		Item[] items = container.getItems();

		for(Item item : items) {
			if (this.emoteClueItems.getEasyItems().contains(item.getId()))
				log.info("Found easy clue item with id {} in container {}", item.getId(), itemContainerChanged.getContainerId());
		}

		//log.info(((Number)itemContainerChanged.getContainerId()).toString());
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
