package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import junit.framework.TestCase;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemPrice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
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

	@Test
	public void TestTeamSizeDuration()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidChambers(true);
		raidTracker.setRaidComplete(true);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Team size: Solo Duration: 40:26 Personal best: 31:12", "", 0);
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		assertEquals(1, raidTracker.getTeamSize());
		assertEquals(2426, raidTracker.getRaidTime());

		message.setMessage("Team size: 15 Duration: 50:26 Personal best: 31:12");
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		assertEquals(15, raidTracker.getTeamSize());
		assertEquals(3026, raidTracker.getRaidTime());
	}

	@Test
	public void TestPurple()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidChambers(true);
		raidTracker.setRaidComplete(true);

		List<ItemPrice> kodaiTestList = new ArrayList<>();

		ItemPrice kodaiTest = new ItemPrice();

		kodaiTest.setId(0);
		kodaiTest.setName("Kodai Insignia");
		kodaiTest.setPrice(505050);

		kodaiTestList.add(kodaiTest);

		when(itemManager.search(anyString())).thenReturn(kodaiTestList);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "K1NG DK - Kodai insignia", "", 0);
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		assertEquals("K1NG DK", raidTracker.getSpecialLootReceiver());
		assertEquals("Kodai insignia", raidTracker.getSpecialLoot());
		assertEquals(505050, raidTracker.getSpecialLootValue());
	}

	@Test
	public void TestDustAndKitRecipients()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidChambers(true);
		raidTracker.setRaidComplete(true);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Dust recipients: Canvasba", "", 0);
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		message.setMessage("Twisted Kit recipients: BallerTom");
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		assertEquals("Canvasba", raidTracker.getDustReceiver());
		assertEquals("BallerTom", raidTracker.getKitReceiver());
	}

	@Test
	public void TestChallengeModeAndCompletionCount()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidChambers(true);
		raidTracker.setRaidComplete(true);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Your completed Chambers of Xeric Challenge Mode count is: 57", "", 0);
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		assertEquals(true, raidTracker.isChallengeMode());
		assertEquals(57, raidTracker.getCompletionCount());

		message.setMessage("Your completed Chambers of Xeric count is: 443");
		raidTrackerPlugin.onChatMessage(message, raidTracker);

		assertEquals(443, raidTracker.getCompletionCount());
	}

}