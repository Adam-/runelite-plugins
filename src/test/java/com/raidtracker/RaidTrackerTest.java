package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.filereadwriter.FileReadWriter;
import com.raidtracker.utils.RaidState;
import com.raidtracker.utils.RaidStateTracker;
import junit.framework.TestCase;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigClient;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemPrice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RaidTrackerTest extends TestCase
{
	@Mock
	@Bind
	Client client;

	@Mock
	@Bind
	EventBus eventBus;

	@Mock
	@Bind
	ScheduledExecutorService executor;

	@Mock
	@Bind
	RuneLiteConfig runeliteConfig;

	@Bind
	@Named("sessionfile")
	File sessionfile = RuneLite.DEFAULT_SESSION_FILE;

	@Bind
	@Named("config")
	File config = RuneLite.DEFAULT_CONFIG_FILE;


	@Mock
	@Bind
	ConfigClient configClient;

	@Mock
	@Bind
	ConfigManager manager;

	@Mock
	@Bind
	private ItemManager itemManager;

	@Mock
	@Bind
	private RaidTrackerConfig raidTrackerConfig;

	@Inject
	RaidTrackerPlugin RaidTrackerPlugin;

	@Mock
	@Bind
	RaidTracker raidTracker;
	@Mock
	@Bind
	RaidStateTracker RaidStateTracker;
	/*@Test
	public void TestLootSplits() {
		//TODO: double purples
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidCox(true);
		raidTracker.setRaidComplete(true);

		List<ItemPrice> kodaiTestList = new ArrayList<>();

		ItemPrice kodaiTest = new ItemPrice();

		kodaiTest.setId(0);
		kodaiTest.setName("Kodai Insignia");
		kodaiTest.setPrice(50505050);

		kodaiTestList.add(kodaiTest);

		Player player = mock(Player.class);
		RaidTrackerPanel panel = mock(RaidTrackerPanel.class);

		RaidTrackerPlugin.setPanel(panel);

		FileReadWriter fw = mock(FileReadWriter.class);
		fw.updateUsername("Test_user");
		RaidTrackerPlugin.setFw(fw);


		when(itemManager.search(anyString())).thenReturn(kodaiTestList);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Test_user");
		when(raidTrackerConfig.FFACutoff()).thenReturn(1000000);
		raidTracker.setTeamSize(3);

		ChatMessage message = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "K1NG DK - Kodai insignia", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);


		RaidTrackerPlugin.setSplits(raidTracker);
		System.out.println(raidTracker);
		assertTrue(raidTracker.getLootSplitReceived() > -1);
		assertEquals(-1, raidTracker.lootSplitPaid);


		raidTracker.setSpecialLootReceiver("Canvasba");
		raidTracker.setSpecialLootInOwnName(true);
		RaidTrackerPlugin.setSplits(raidTracker);

		assertTrue(raidTracker.getLootSplitReceived() > -1);
		assertTrue(raidTracker.getLootSplitPaid() > -1);

		assertFalse(raidTracker.isFreeForAll());

		//check ffa for below 1m splitmatcher
		raidTracker.setSpecialLootValue(2000000);
		raidTracker.setLootSplitPaid(-1);
		RaidTrackerPlugin.setSplits(raidTracker);

		assertTrue(raidTracker.isFreeForAll());
		assertEquals(raidTracker.getLootSplitReceived(), 2000000);
		assertEquals(-1, raidTracker.getLootSplitPaid());

		raidTracker.setSpecialLootReceiver("K1NG DK");
		raidTracker.setSpecialLootInOwnName(false);

		raidTracker.setLootSplitReceived(-1);
		RaidTrackerPlugin.setSplits(raidTracker);

		assertEquals(-1, raidTracker.getLootSplitReceived());
	}*/


	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
		RaidTrackerPlugin.startUp();
		when(RaidStateTracker.isInRaid()).thenReturn(true);
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Test_user");
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 0));
	};
	@After
	public void after()
	{
		RaidTrackerPlugin.shutDown();
	}
	@Test
	public void TestRaidComplete()
	{
		RaidTracker raidTracker = new RaidTracker();
		ChatMessage message  = new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "Your completed Chambers of Xeric Challenge Mode count is: 357", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);
		assertTrue(raidTracker.isRaidComplete());
	}


	@Test
	public void ChambersTest()
	{
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Test_user");

		List<ItemPrice> ItemList = new ArrayList<>();
		ItemPrice KodaiItem = new ItemPrice();
		KodaiItem.setId(0);
		KodaiItem.setName("Kodai insignia");
		KodaiItem.setPrice(505050);

		ItemPrice TbowItem = new ItemPrice();
		TbowItem.setId(1);
		TbowItem.setName("Twisted Bow");
		TbowItem.setPrice(999999);

		ItemList.add(KodaiItem);
		ItemList.add(TbowItem);


		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setRaidComplete(true);

		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 0));
		when(itemManager.search(anyString()))
			.thenAnswer(invocation -> {
				return ItemList.stream().filter(e -> e.getName().equalsIgnoreCase(invocation.getArgument(0, String.class))).collect(Collectors.toList());
			});
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "Your completed Chambers of Xeric Challenge Mode count is: 57.", "", 0), raidTracker);

		raidTracker.setTeamSize(5);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Player 1 - Kodai insignia", "", 0),raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Player 2 - Twisted Bow", "", 0),raidTracker);
		System.out.println(raidTracker.getUniques());
	}
	@Test
	public void TobTest()
	{
		FileReadWriter fw = new FileReadWriter();
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Test_user");
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setRaidComplete(true);
		List<ItemPrice> tobList = new ArrayList<>();
		ItemPrice AvernicItem = new ItemPrice();
		AvernicItem.setId(0);
		AvernicItem.setName("Avernic defender hilt");
		AvernicItem.setPrice(505050);

		ItemPrice ScytheItem = new ItemPrice();
		ScytheItem.setId(1);
		ScytheItem.setName("Scythe of vitur (uncharged)");
		ScytheItem.setPrice(999999);

		tobList.add(AvernicItem);
		tobList.add(ScytheItem);

		RaidTrackerPlugin.setFw(fw);
		raidTracker.inRaidType = 1;
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 1));
		when(itemManager.search(anyString()))
			.thenAnswer(invocation -> {
				return tobList.stream().filter(e -> e.getName().equalsIgnoreCase(invocation.getArgument(0, String.class))).collect(Collectors.toList());
			});
		raidTracker.setTeamSize(5);

		RaidTrackerPlugin.checkChatMessage( new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Lil\\u0027 Zik", "", 0), raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Avernic defender hilt", "", 0),raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Scythe of vitur (uncharged)", "", 0),raidTracker);
		fw.writeToFile(raidTracker);
	}
	@Test
	public void ToaTest()
	{
		FileReadWriter fw = new FileReadWriter();
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Test_user");
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setRaidComplete(true);
		List<ItemPrice> toalist = new ArrayList<>();
		ItemPrice fangItem = new ItemPrice();
		fangItem.setId(0);
		fangItem.setName("Osmumtuns Fang");
		fangItem.setPrice(505050);

		ItemPrice staffItem = new ItemPrice();
		staffItem.setId(1);
		staffItem.setName("Tumekens Shadow");
		staffItem.setPrice(999999);

		toalist.add(staffItem);
		toalist.add(fangItem);
		RaidTrackerPlugin.setFw(fw);
		raidTracker.inRaidType = 2;
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 2));
		when(itemManager.search(anyString()))
				.thenAnswer(invocation -> {
					return toalist.stream().filter(e -> e.getName().equalsIgnoreCase(invocation.getArgument(0, String.class))).collect(Collectors.toList());
				});
		raidTracker.setTeamSize(5);
		RaidTrackerPlugin.checkChatMessage( new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Tumeken\\u0027s guardian", "", 0), raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Tumekens Shadow", "", 0),raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Osmumtuns Fang", "", 0),raidTracker);
		fw.writeToFile(raidTracker);
	};
	@Test
	public void TestToaTimes()
	{
		RaidTracker raidTracker = new RaidTracker();
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(true, 2));
		String toaRooms[] = {
				"Path of Crondis", "Zebak", "Path of Apmeken", "Ba-Ba", "Path of Het", "Akkha", "Path of Scabaras", "Kephri", "The Wardens"
		};
		int index = 0;
		for (String room : toaRooms)
		{
			int seconds = new Random().nextInt(1000);
			String timeString = seconds / 60 + ":" + (seconds % 60 < 10 ? "0" : "") + seconds % 60;
			String s = "Challenge complete: ";
			s+= room + " ";
			s+= "Duration: ";
			s+= timeString;
			ChatMessage message  = new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", s, "", 0);

			RaidTrackerPlugin.checkChatMessage(message, raidTracker);
			assertEquals(seconds, raidTracker.getRoomTimes()[index]);
			index ++;
		};
		// full raid.
		String message = "Challenge complete: The Wardens. Duration: <col=ef1020>3:53</col><br>Tombs of Amascut: Entry Mode challenge completion time: <col=ef1020>17:22</col>. Personal best: 16:40";
		ChatMessage m  = new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", message, "", 0);
		RaidTrackerPlugin.checkChatMessage(m, raidTracker);
		System.out.println(Arrays.toString(raidTracker.getRoomTimes()));
	};
	/*@Test
	public void TestDuration()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidCox(true);
		raidTracker.setRaidComplete(true);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Congratulations - your raid is complete! Team size: Solo Duration: 1:40:26 Personal best: 31:12", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals(6026, raidTracker.getRaidTime());

		message.setMessage("Congratulations - your raid is complete! Team size: 11-15 Players Duration: 50:26.6 Personal best: 31:12");
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals(3027, raidTracker.getRaidTime());

		//dey0 case
		message.setMessage("Middle level complete! Duration: 7:53 Total: 20:50");
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals(1250, raidTracker.getCoxTimes()[1]);

		//regular case
		message.setMessage("Middle level complete! Duration: 20:50");
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);
		System.out.println(raidTracker);
		assertEquals(1250, raidTracker.getCoxTimes()[1]);

		message.setMessage("Combat room 'Vanguards' complete! Duration: 3:19 Total: 16:16");
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals(199, raidTracker.getCoxTimes()[5]);


	}

	@Test
	public void TestPurple()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidCox(true);
		raidTracker.setRaidComplete(true);

		List<ItemPrice> kodaiTestList = new ArrayList<>();

		ItemPrice kodaiTest = new ItemPrice();

		kodaiTest.setId(0);
		kodaiTest.setName("Kodai Insignia");
		kodaiTest.setPrice(505050);

		kodaiTestList.add(kodaiTest);


		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "K1NG DK - Kodai insignia", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals("K1NG DK", raidTracker.getSpecialLootReceiver());
		assertEquals("Kodai      insignia", raidTracker.getSpecialLoot());
		assertEquals(505050, raidTracker.getSpecialLootValue());
	}

	@Test
	public void TestTobPurple()
	{
		FileReadWriter fw = new FileReadWriter();
		RaidTracker raidTracker = new RaidTracker();
		fw.updateUsername("Test_user");
		RaidTrackerPlugin.setFw(fw);
		raidTracker.setRaidComplete(true);
		raidTracker.setInRaidTob(true);

		List<ItemPrice> avernicTestList = new ArrayList<>();

		ItemPrice avernicTest = new ItemPrice();

		avernicTest.setId(0);
		avernicTest.setName("Avernic defender hilt");
		avernicTest.setPrice(50505050);

		avernicTestList.add(avernicTest);

		when(itemManager.search(anyString())).thenReturn(avernicTestList);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Avernic defender hilt", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Lil\\u0027 Zik", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals("Canvasba", raidTracker.getSpecialLootReceiver());
		assertEquals("Avernic defender hilt", raidTracker.getSpecialLoot());
		assertFalse(raidTracker.petReceiver.isEmpty());
		assertEquals("Canvasba", raidTracker.getPetReceiver());
		assertEquals(50505050, raidTracker.getSpecialLootValue());
	}

	@Test
	public void TestDustAndKitRecipients()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidCox(true);
		raidTracker.setRaidComplete(true);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Dust recipients: Canvasba", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		message.setMessage("Twisted Kit recipients: BallerTom");
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals("Canvasba", raidTracker.getDustReceiver());
		assertEquals("BallerTom", raidTracker.getKitReceiver());
	}

	@Test
	public void TestChallengeModeAndCompletionCount()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidCox(true);
		raidTracker.setRaidComplete(true);

		ChatMessage message  = new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "Your completed Chambers of Xeric Challenge Mode count is: 57.", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertTrue(raidTracker.isChallengeMode());
		assertEquals(57, raidTracker.getCompletionCount());

		message.setMessage("Your completed Chambers of Xeric count is: 443");
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);

		assertEquals(443, raidTracker.getCompletionCount());
	}


	//---------------------------------- onWidgetLoaded tests ------------------------------------------------


	@Test
	public void TestLootListFactory()
	{
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setInRaidCox(true);
		raidTracker.setRaidComplete(true);


		//------------------- general case - 2 stacks of regular items --------------------------
		Item[] items = new Item[2];

		items[0] = new Item(1, 500);
		items[1] = new Item(2, 600);

		ItemComposition comp1 = mock(ItemComposition.class);
		ItemComposition comp2 = mock(ItemComposition.class);

		when(itemManager.getItemComposition(1)).thenReturn(comp1);
		when(itemManager.getItemComposition(2)).thenReturn(comp2);

		when(comp1.getName()).thenReturn("Pure Essence");
		when(comp1.getId()).thenReturn(1);
		when(comp1.getPrice()).thenReturn(5);

		when(comp2.getName()).thenReturn("Teak Planks");
		when(comp2.getId()).thenReturn(2);
		when(comp2.getPrice()).thenReturn(255);

		ArrayList<RaidTrackerItem> lootList = RaidTrackerPlugin.lootListFactory(items);

		assertEquals(2, lootList.size());
		assertEquals(2500, lootList.get(0).getPrice());
		assertEquals(153000, lootList.get(1).getPrice());

		//----------------------------------------- purple ----------------------------------------
		items = new Item[1];
		items[0] = new Item(3, 1);

		ItemComposition comp3 = mock(ItemComposition.class);
		when(itemManager.getItemComposition(3)).thenReturn(comp3);

		when(comp3.getName()).thenReturn("Twisted Bow");
		when(comp3.getId()).thenReturn(3);
		when(comp3.getPrice()).thenReturn(1198653000);

		lootList = RaidTrackerPlugin.lootListFactory(items);

		assertEquals(1, lootList.size());
		assertEquals(1198653000, lootList.get(0).getPrice());
	}
*/
}