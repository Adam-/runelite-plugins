package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import junit.framework.TestCase;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.game.ItemManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RaidTrackerTest extends TestCase
{
	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private ItemManager itemManager;

	@Mock
	@Bind
	private RaidTrackerConfig raidTrackerConfig;


	@Inject
	private RaidTrackerPlugin raidTrackerPlugin;


	@Before
	public void setUp()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	@Test
	public void TestRaidComplete()
	{
		RaidTracker raidTracker = new RaidTracker();

		when(client.getVar(ArgumentMatchers.any(Varbits.class))).thenReturn(5); //random integer, I chose 5
		raidTracker.setInRaidChambers(true);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Congratulations - your raid is complete!", "", 0);
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		assertEquals(true, raidTracker.isRaidComplete());
	}

}