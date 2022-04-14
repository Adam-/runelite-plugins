/*
 * Copyright (c) 2020, BegOsrs <https://github.com/begosrs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package begosrs.barbarianassault;

import begosrs.barbarianassault.api.BaModelID;
import begosrs.barbarianassault.api.BaObjectID;
import begosrs.barbarianassault.api.widgets.BaWidgetID;
import begosrs.barbarianassault.api.widgets.BaWidgetInfo;
import begosrs.barbarianassault.attackstyle.AttackStyle;
import begosrs.barbarianassault.attackstyle.AttackStyleWidget;
import begosrs.barbarianassault.attackstyle.WeaponType;
import begosrs.barbarianassault.deathtimes.DeathTimeInfoBox;
import begosrs.barbarianassault.deathtimes.DeathTimesMode;
import begosrs.barbarianassault.grounditems.GroundItem;
import begosrs.barbarianassault.grounditems.GroundItemsOverlay;
import begosrs.barbarianassault.grounditems.MenuHighlightMode;
import begosrs.barbarianassault.groundmarkers.BrokenTrapsOverlay;
import begosrs.barbarianassault.hoppers.CollectorEgg;
import begosrs.barbarianassault.hoppers.HoppersOverlay;
import begosrs.barbarianassault.inventory.InventoryOverlay;
import begosrs.barbarianassault.menuentryswapper.MenuEntrySwapper;
import begosrs.barbarianassault.points.DisplayPointsLocationMode;
import begosrs.barbarianassault.points.DisplayPointsMode;
import begosrs.barbarianassault.points.KandarinDiaryBonusMode;
import begosrs.barbarianassault.points.PointsCounterMode;
import begosrs.barbarianassault.points.PointsMode;
import begosrs.barbarianassault.points.RewardsBreakdownMode;
import begosrs.barbarianassault.points.RolePointsInfoBox;
import begosrs.barbarianassault.points.RolePointsOverlay;
import begosrs.barbarianassault.points.RolePointsTrackingMode;
import begosrs.barbarianassault.teamhealthbar.TeamHealthBarOverlay;
import begosrs.barbarianassault.ticktimer.RunnerTickTimer;
import begosrs.barbarianassault.ticktimer.RunnerTickTimerOverlay;
import begosrs.barbarianassault.timer.DurationMode;
import begosrs.barbarianassault.timer.TimeUnits;
import begosrs.barbarianassault.timer.Timer;
import begosrs.barbarianassault.waveinfo.WaveInfoOverlay;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.MessageNode;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.ScriptID;
import net.runelite.api.SpriteID;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetConfig;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
		  name = "Ba Minigame",
		  description = "Includes many features to enhance the barbarian assault minigame gameplay experience",
		  tags = {"overlay", "b.a.", "barbarian assault", "minigame", "attacker", "defender", "collector", "healer", "plugin hub"}
)
public class BaMinigamePlugin extends Plugin
{

	public static final Color RED = new Color(228, 18, 31);
	public static final Color DARK_GREEN = new Color(0, 153, 0);
	public static final Color LIGHT_BLUE = new Color(60, 124, 240);
	public static final Color LIGHT_RED = new Color(255, 35, 35);
	public static final Color DEFAULT_FLASH_COLOR = new Color(255, 255, 255, 126);
	public static final int DEFAULT_ATTACK_STYLE_COLOR = 16750623;
	public static final int WAVE_CHECKMARK_ICON_WIDTH = 13;
	private static final int BA_WAVE_NUM_INDEX = 2;
	private static final String END_ROUND_REWARD_NEEDLE_TEXT = "<br>5";
	private static final int MENU_THIRD_OPTION = MenuAction.GROUND_ITEM_THIRD_OPTION.getId();
	private static final String BA_MINIGAME_CONFIG_GROUP = "baMinigame";
	private static final String GROUND_ITEMS_CONFIG_GROUP = "grounditems";
	private static final String GROUND_ITEMS_CONFIG_HIGHLIGHTED_ITENS = "highlightedItems";
	private static final String GROUND_ITEMS_CONFIG_HIDDEN_ITENS = "hiddenItems";
	private static final String[] GROUND_ITEMS_HIDDEN_LIST = {
			  "Green egg", "Red egg", "Blue egg", "Hammer", "Logs", "Yellow egg", "Crackers", "Tofu", "Worms"
	};
	private static final String BARBARIAN_ASSAULT_CONFIG_GROUP = "barbarianAssault";
	private static final String[] BARBARIAN_ASSAULT_CONFIGS = {
			  "showTimer", "showHealerBars", "waveTimes"
	};
	private static final int BA_UNDERGROUND_REGION_ID = 10322,
			  BA_TILE_START_X = 2522,
			  BA_TILE_END_X = 2538,
			  BA_TILE_START_Y = 3560,
			  BA_TILE_END_Y = 3579;
	private static final int WAVE_ICON_WIDTH = 17;
	private static final BaWidgetInfo[] TEAM_PLAYERS_ROLES_WIDGETS = {
			  BaWidgetInfo.BA_TEAM_PLAYER1_ROLE, BaWidgetInfo.BA_TEAM_PLAYER2_ROLE, BaWidgetInfo.BA_TEAM_PLAYER3_ROLE,
			  BaWidgetInfo.BA_TEAM_PLAYER4_ROLE, BaWidgetInfo.BA_TEAM_PLAYER5_ROLE
	};
	@Getter
	private final List<GameObject> hoppers = new ArrayList<>(2);
	@Getter
	private final List<GroundObject> brokenTraps = new ArrayList<>(2);
	@Getter
	private final Map<CollectorEgg, Integer> cannonEggs = new HashMap<>(4);
	@Getter
	private final Map<GroundItem.Key, GroundItem> groundEggs = new LinkedHashMap<>();
	@Getter
	private final Map<GroundItem.Key, GroundItem> groundBait = new LinkedHashMap<>();
	@Getter
	private final Map<GroundItem.Key, GroundItem> groundLogsHammer = new LinkedHashMap<>(3);
	private final List<DeathTimeInfoBox> deathTimesInfoBoxes = new ArrayList<>();
	private final Map<String, BufferedImage> images = new HashMap<>(8);
	@Getter
	private final Map<Role, Integer> rolePoints = new EnumMap<>(Role.class);
	private final Map<Role, RolePointsInfoBox> rolePointsInfoBoxes = new EnumMap<>(Role.class);
	private final Role[] playerRoles = new Role[5];
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private BaMinigameConfig config;
	@Inject
	private ChatMessageManager chatMessageManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private KeyManager keyManager;
	@Inject
	private MenuEntrySwapper menuEntrySwapper;
	@Inject
	private WaveInfoOverlay waveInfoOverlay;
	@Inject
	private InventoryOverlay inventoryOverlay;
	@Inject
	private GroundItemsOverlay groundItemsOverlay;
	@Inject
	private HoppersOverlay hoppersOverlay;
	@Inject
	private RunnerTickTimerOverlay runnerTickTimerOverlay;
	@Inject
	private TeamHealthBarOverlay teamHealthBarOverlay;
	@Inject
	private BaMinigameInputListener inputListener;
	@Inject
	private RolePointsOverlay rolePointsOverlay;
	@Inject
	private BrokenTrapsOverlay brokenTrapsOverlay;
	@Getter
	private RunnerTickTimer runnerTickTimer;
	@Getter
	private int inGameBit;
	@Getter(AccessLevel.PACKAGE)
	private Timer timer;
	@Getter
	private Session session;
	@Getter
	private Round round;
	@Getter
	private Wave wave;
	@Getter(AccessLevel.PACKAGE)
	private int currentWave;
	@Getter
	private String lastListen;
	@Getter
	private int lastListenItemId;
	private Integer attackStyleTextColor;
	@Getter
	private boolean teammatesHealthHotkeyPressed;
	@Setter
	@Getter
	private boolean teammatesHealthShifted;
	private WorldPoint worldPoint;
	private long lastWaveCompletedTimestamp;
	private int correctedCallCount;
	private boolean containsRoleHorn;
	private boolean loadingPlayerRoles;

	@Provides
	BaMinigameConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BaMinigameConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		images.put("fighters", ImageUtil.loadImageResource(getClass(), "/penance_fighter.png"));
		images.put("rangers", ImageUtil.loadImageResource(getClass(), "/penance_ranger.png"));
		images.put("healers", ImageUtil.loadImageResource(getClass(), "/penance_healer.png"));
		images.put("runners", ImageUtil.loadImageResource(getClass(), "/penance_runner.png"));
		images.put("attacker", ImageUtil.loadImageResource(getClass(), "/attacker.png"));
		images.put("defender", ImageUtil.loadImageResource(getClass(), "/defender.png"));
		images.put("collector", ImageUtil.loadImageResource(getClass(), "/collector.png"));
		images.put("healer", ImageUtil.loadImageResource(getClass(), "/healer.png"));

		overlayManager.add(waveInfoOverlay);
		overlayManager.add(inventoryOverlay);
		overlayManager.add(groundItemsOverlay);
		overlayManager.add(hoppersOverlay);
		overlayManager.add(runnerTickTimerOverlay);
		overlayManager.add(teamHealthBarOverlay);
		overlayManager.add(rolePointsOverlay);
		overlayManager.add(brokenTrapsOverlay);

		menuEntrySwapper.enableSwaps();

		clientThread.invokeLater(() -> updateAttackStyleText(lastListen));

		keyManager.registerKeyListener(inputListener);

		if (config.showGroundItemHighlights())
		{
			setGroundItemsPluginLists();
		}
		disableBarbarianAssaultPluginFeatures();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(waveInfoOverlay);
		overlayManager.remove(inventoryOverlay);
		overlayManager.remove(groundItemsOverlay);
		overlayManager.remove(hoppersOverlay);
		overlayManager.remove(runnerTickTimerOverlay);
		overlayManager.remove(teamHealthBarOverlay);
		overlayManager.remove(rolePointsOverlay);
		overlayManager.remove(brokenTrapsOverlay);

		menuEntrySwapper.disableSwaps();

		keyManager.unregisterKeyListener(inputListener);

		hoppers.clear();
		brokenTraps.clear();
		cannonEggs.clear();
		groundEggs.clear();
		groundBait.clear();
		groundLogsHammer.clear();
		images.clear();
		rolePoints.clear();

		disableRunnerTickTimer(true);
		removeDeathTimesInfoBoxes();
		removeRolePointsInfoBoxes();
		clientThread.invokeLater(this::restoreHealerTeammatesHealth);
		clientThread.invokeLater(this::restoreAttackStyleText);
		if (wave != null)
		{
			clientThread.invokeLater(() ->
			{
				restoreWaveWidget(wave, true);
				setCallFlashColor(wave, DEFAULT_FLASH_COLOR);
				removeWaveWidgets(wave);
				wave = null;
			});
		}

		timer = null;
		round = null;
		session = null;
		currentWave = 0;

		lastListen = null;
		lastListenItemId = 0;

		restoreGroundItemsPluginLists();
		restoreBarbarianAssaultPluginFeatures();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		final String group = event.getGroup();
		final String key = event.getKey();
		final String oldValue = event.getOldValue();
		final String newValue = event.getNewValue();
		switch (group)
		{
			case BARBARIAN_ASSAULT_CONFIG_GROUP:
			{
				config.setBarbarianAssaultConfigs("");
				break;
			}
			case BA_MINIGAME_CONFIG_GROUP:
				switch (key)
				{
					case "callChangeFlashColor":
					{
						if (inGameBit == 1)
						{
							setCallFlashColor(wave, config.callChangeFlashColor());
						}
						break;
					}
					case "showCallChangeTimer":
					case "hideTeammateRole":
					{
						if (wave != null)
						{
							clientThread.invokeLater(() ->
							{
								removeWaveWidgets(wave);
								setWaveWidgets(wave);
							});
						}
						break;
					}
					case "showWaveTimer":
					{
						if (!config.showWaveTimer())
						{
							clientThread.invokeLater(() -> restoreWaveWidget(wave, false));
						}
						break;
					}
					case "showHpCountOverlay":
					{
						if (wave != null && getRole() == Role.HEALER)
						{
							clientThread.invokeLater(() ->
							{
								removeWaveWidgets(wave);
								setWaveWidgets(wave);
							});
						}
						break;
					}
					case "showEggCountOverlay":
					{
						if (wave != null && getRole() == Role.COLLECTOR)
						{
							clientThread.invokeLater(() ->
							{
								removeWaveWidgets(wave);
								setWaveWidgets(wave);
							});
						}
						break;
					}
					case "showRunnerTickTimerAttacker":
					{
						final boolean display = config.showRunnerTickTimerAttacker() && inGameBit == 1 && getRole() == Role.ATTACKER;
						enableRunnerTickTimer(display);
						break;
					}
					case "showRunnerTickTimerDefender":
					{
						final boolean display = config.showRunnerTickTimerDefender() && inGameBit == 1 && getRole() == Role.DEFENDER;
						enableRunnerTickTimer(display);
						break;
					}
					case "showRunnerTickTimerCollector":
					{
						final boolean display = config.showRunnerTickTimerCollector() && inGameBit == 1 && getRole() == Role.COLLECTOR;
						enableRunnerTickTimer(display);
						break;
					}
					case "showRunnerTickTimerHealer":
					{
						final boolean display = config.showRunnerTickTimerHealer() && inGameBit == 1 && getRole() == Role.HEALER;
						enableRunnerTickTimer(display);
						break;
					}
					case "deathTimesMode":
					{
						final DeathTimesMode deathTimesMode = config.deathTimesMode();
						if (deathTimesMode == DeathTimesMode.INFO_BOX || deathTimesMode == DeathTimesMode.INFOBOX_CHAT)
						{
							showDeathTimes();
						}
						else
						{
							hideDeathTimesInfoBoxes();
						}
						break;
					}
					case "displayPointsMode":
					{
						final DisplayPointsMode pointsMode = config.displayPointsMode();
						if (pointsMode == DisplayPointsMode.INFO_BOX)
						{
							clientThread.invokeLater(() -> updateRolePoints(false));
						}
						else
						{
							hideRolePointsInfoBoxes();
						}
						break;
					}
					case "displayPointsLocationMode":
					{
						if (!shouldDisplayRolePoints())
						{
							hideRolePointsInfoBoxes();
						}
						else
						{
							clientThread.invokeLater(() -> updateRolePoints(true));
						}
						break;
					}
					case "highlightAttackStyle":
					case "highlightAttackStyleColor":
					{
						clientThread.invokeLater(() -> updateAttackStyleText(lastListen));
						break;
					}
					case "showGroundItemHighlights":
					{
						if (config.showGroundItemHighlights())
						{
							setGroundItemsPluginLists();
						}
						else
						{
							restoreGroundItemsPluginLists();
						}
						break;
					}
					case "groundItemsPluginHighlightedList":
					{
						if (!oldValue.isEmpty() && newValue.isEmpty())
						{
							config.setGroundItemsPluginHighlightedList(oldValue);
						}
						break;
					}
					case "groundItemsPluginHiddenList":
					{
						if (!oldValue.isEmpty() && newValue.isEmpty())
						{
							config.setGroundItemsPluginHiddenList(oldValue);
						}
						break;
					}
					case "barbarianAssaultConfigs":
					{
						if (!oldValue.isEmpty() && newValue.isEmpty())
						{
							config.setBarbarianAssaultConfigs(oldValue);
						}
						break;
					}
					case "hideHealerTeammatesHealth":
					{
						if (wave != null && wave.getRole() == Role.HEALER)
						{
							setHealerTeammatesHealthDisplay();
						}
						break;
					}
					case "showWaveCompleted":
					{
						if (!config.showWaveCompleted())
						{
							restoreWaveIcon(wave);
						}
						break;
					}
					case "pointsCounterMode":
					case "rolePointsTrackingMode":
					{
						clientThread.invokeLater(() -> updateRolePoints(false));
						break;
					}
					case "resetPoints":
					{
						resetRolePoints();
						break;
					}
					case "kandarinHardDiaryPointsBoost":
					{
						final PointsCounterMode pointsCounterMode = config.pointsCounterMode();
						if (pointsCounterMode != PointsCounterMode.CURRENT_POINTS)
						{
							clientThread.invokeLater(() -> updateRolePoints(false));
						}
					}
				}
				break;
		}
	}


	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		switch (event.getGroupId())
		{
			case BaWidgetID.BA_REWARD_GROUP_ID:
			{
				Widget rewardWidget = client.getWidget(BaWidgetInfo.BA_REWARD_TEXT.getGroupId(), BaWidgetInfo.BA_REWARD_TEXT.getChildId());
				if (rewardWidget == null)
				{
					break;
				}

				if (!rewardWidget.getText().contains(END_ROUND_REWARD_NEEDLE_TEXT))
				{
					onWaveEnded(false);
				}
				else if (round != null)
				{
					onRoundEnded();
				}
				break;
			}
			case BaWidgetID.BA_ATTACKER_GROUP_ID:
			{
				startWave(Role.ATTACKER, config.showRunnerTickTimerAttacker());
				break;
			}
			case BaWidgetID.BA_DEFENDER_GROUP_ID:
			{
				startWave(Role.DEFENDER, config.showRunnerTickTimerDefender());
				break;
			}
			case BaWidgetID.BA_COLLECTOR_GROUP_ID:
			{
				startWave(Role.COLLECTOR, config.showRunnerTickTimerCollector());
				break;
			}
			case BaWidgetID.BA_HEALER_GROUP_ID:
			{
				startWave(Role.HEALER, config.showRunnerTickTimerHealer());
				break;
			}
			case WidgetID.BA_TEAM_GROUP_ID:
			{
				loadingPlayerRoles = true;
				break;
			}
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == WidgetID.BA_TEAM_GROUP_ID)
		{
			loadingPlayerRoles = false;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		// varbit change and widget loading are not reliable enough to start the wave timer

		final ItemContainer container = event.getItemContainer();
		if (container == client.getItemContainer(InventoryID.INVENTORY))
		{
			final Item[] inventory = event.getItemContainer().getItems();
			boolean containsRoleHorn = Arrays.stream(inventory).map(Item::getId).anyMatch(id ->
					  id == ItemID.ATTACKER_HORN
								 || id == ItemID.ATTACKER_HORN_10517
								 || id == ItemID.ATTACKER_HORN_10518
								 || id == ItemID.ATTACKER_HORN_10519
								 || id == ItemID.ATTACKER_HORN_10520
								 || id == ItemID.DEFENDER_HORN
								 || id == ItemID.COLLECTOR_HORN
								 || id == ItemID.HEALER_HORN
								 || id == ItemID.HEALER_HORN_10527
								 || id == ItemID.HEALER_HORN_10528
								 || id == ItemID.HEALER_HORN_10529
								 || id == ItemID.HEALER_HORN_10530
			);
			if (containsRoleHorn && !this.containsRoleHorn)
			{
				startWave(null, false);
			}
			this.containsRoleHorn = containsRoleHorn;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		final ChatMessageType type = chatMessage.getType();
		// for some reason, the 'All of the Penance ... have been killed' are WELCOME messages for Healer/Collector/Defender roles
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.WELCOME)
		{
			return;
		}

		final String message = chatMessage.getMessage();
		if (message.startsWith("---- Wave:"))
		{
			currentWave = Integer.parseInt(message.split(" ")[BA_WAVE_NUM_INDEX]);

			if (round == null || currentWave == Round.STARTING_WAVE)
			{
				timer = new Timer();
				round = new Round(currentWave, timer);
			}

			// wave will be set on onWidgetLoaded which happens after onChatMessage.
			// Reset here in case we are restarting the same wave (no onWidgetLoaded happened).
			wave = null;
		}
		else
		{
			if (wave != null && message.contains("exploded"))
			{
				wave.setWrongEggsCount(wave.getWrongEggsCount() + 1);
				wave.setEggsCount(wave.getEggsCount() - 1);
			}
			else if (wave != null && wave.getNumber() != 10 && message.contains("You healed "))
			{
				final int health = Integer.parseInt(message.split(" ")[2]);
				wave.setHpHealed(wave.getHpHealed() + health);
			}
			else if (config.highlightNotification() && message.contains("the wrong type of poisoned food to use"))
			{
				final MessageNode messageNode = chatMessage.getMessageNode();
				final String nodeValue = Text.removeTags(messageNode.getValue());
				messageNode.setValue(ColorUtil.wrapWithColorTag(nodeValue, config.highlightNotificationColor()));
				chatMessageManager.update(messageNode);
			}
			else if (wave != null && message.startsWith("All of the Penance "))
			{
				final MessageNode node = chatMessage.getMessageNode();
				String nodeValue = Text.removeTags(node.getValue());
				final String npc = message.split(" ")[4];

				Role role = wave.getRole();

				switch (npc)
				{
					case "Fighters":
						wave.setFightersKilled(true);
						break;
					case "Rangers":
						wave.setRangersKilled(true);
						break;
					case "Healers":
						wave.setHealersKilled(true);
						break;
					case "Runners":
						wave.setRunnersKilled(true);
						break;
				}

				if (config.deathMessageColor() != null)
				{
					if (role == Role.ATTACKER && (npc.equals("Fighters") || npc.equals("Rangers"))
							  || role == Role.HEALER && npc.equals("Healers")
							  || role == Role.DEFENDER && npc.equals("Runners"))
					{
						nodeValue = ColorUtil.wrapWithColorTag(nodeValue, config.deathMessageColor());
						node.setValue(nodeValue);
					}
				}

				if (wave.getTimer() != null)
				{
					final TimeUnits units = config.timeUnits();
					final String timeFormat = units.getFormatString();
					final float time = wave.getTimeElapsed(false, units);
					addDeathTimesInfoBoxes(npc, time, timeFormat);

					final DeathTimesMode deathTimesMode = config.deathTimesMode();
					if (deathTimesMode == DeathTimesMode.CHAT || deathTimesMode == DeathTimesMode.INFOBOX_CHAT)
					{
						String timeElapsed = String.format(timeFormat, time) + "s";
						if (config.enableGameChatColors())
						{
							timeElapsed = ColorUtil.wrapWithColorTag(timeElapsed, Color.RED);
						}

						node.setValue(nodeValue + " " + timeElapsed);
					}
				}
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		final int currentInGame = client.getVar(Varbits.IN_GAME_BA);

		if (inGameBit != currentInGame)
		{
			int previousInGameBit = inGameBit;
			inGameBit = currentInGame;
			if (previousInGameBit == 1)
			{
				// Use an instance check to determine if this is exiting a game or a tutorial
				// After exiting tutorials there is a small delay before changing IN_GAME_BA back to
				// 0 whereas when in a real wave it changes while still in the instance.
				final DurationMode durationMode = config.showDurationMode();
				if ((durationMode == DurationMode.WAVE || durationMode == DurationMode.WAVE_ROUND)
						  && wave != null && client.isInInstancedRegion())
				{
					announceWaveTime();
				}

				stopWave();
			}
			else
			{
				startWave(null, false);
			}
		}

		if (inGameBit == 1)
		{
			updateEggsCount();
		}

		updateRolePoints(false);
	}

	private void updateRolePoints(boolean forceDisplay)
	{
		final boolean shouldDisplayRolePoints = forceDisplay || shouldDisplayRolePoints();
		PointsCounterMode pointsCounterMode = config.pointsCounterMode();
		boolean diaryBonus = includeKandarinBonus();
		for (Role role : Role.values())
		{
			int points = 0;
			if (pointsCounterMode == PointsCounterMode.CURRENT_POINTS)
			{
				points = role.getPoints(client);
			}
			else if (round != null && pointsCounterMode == PointsCounterMode.ROUND_POINTS)
			{
				points = round.getRolePoints(role);
				if (diaryBonus)
				{
					points *= 1.1;
				}
			}
			else if (session != null && pointsCounterMode == PointsCounterMode.SESSION_POINTS)
			{
				points = session.getRolePoints(role);
				if (diaryBonus)
				{
					points *= 1.1;
				}
			}

			rolePoints.put(role, points);

			if (shouldDisplayRolePoints)
			{
				addOrUpdateRolePointsInfoBoxes(role, points);
			}
		}
	}

	private boolean includeKandarinBonus()
	{
		return config.kandarinHardDiaryPointsBonus() == KandarinDiaryBonusMode.YES
				  || config.kandarinHardDiaryPointsBonus() == KandarinDiaryBonusMode.IF_COMPLETED &&
				  client.getVar(Varbits.DIARY_KANDARIN_HARD) == 1;
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == ScriptID.COMBAT_INTERFACE_SETUP)
		{
			clientThread.invokeLater(() -> updateAttackStyleText(lastListen));
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		final DisplayPointsLocationMode pointsLocationMode = config.displayPointsLocationMode();
		if (pointsLocationMode == DisplayPointsLocationMode.OUTPOST ||
				  pointsLocationMode == DisplayPointsLocationMode.OUTPOST_INGAME)
		{
			final Player player = client.getLocalPlayer();
			if (player != null)
			{
				final WorldPoint worldPoint = player.getWorldLocation();
				if (worldPoint != this.worldPoint)
				{
					this.worldPoint = worldPoint;
					if (!shouldDisplayRolePoints())
					{
						hideRolePointsInfoBoxes();
					}
				}
			}
		}

		if (timer != null)
		{
			timer.onGameTick();
		}

		if (inGameBit == 1)
		{
			if (wave != null)
			{
				final Role role = wave.getRole();
				if (role != null)
				{
					final String currentListen = role.getListenText(client);
					if (currentListen != null && !currentListen.equals(lastListen))
					{
						clientThread.invokeLater(() -> updateAttackStyleText(currentListen));

						if (config.announceCallCorrection())
						{
							checkCallCorrection(currentListen);
						}
					}

					lastListen = currentListen;
					lastListenItemId = role.getListenItemId(client);
				}
			}
			if (runnerTickTimer != null)
			{
				runnerTickTimer.incrementCount();
			}
		}
		else if (loadingPlayerRoles)
		{
			for (int i = 0; i < TEAM_PLAYERS_ROLES_WIDGETS.length; i++)
			{
				final Widget playerRole = client.getWidget(TEAM_PLAYERS_ROLES_WIDGETS[i].getGroupId(), TEAM_PLAYERS_ROLES_WIDGETS[i].getChildId());
				if (playerRole != null)
				{
					playerRoles[i] = getRoleForModelId(playerRole.getModelId());
				}
				else
				{
					playerRoles[i] = null;
				}
			}
		}

		if (session != null
				  && System.currentTimeMillis() >= lastWaveCompletedTimestamp + TimeUnit.MINUTES.toMillis(config.sessionResetTime()))
		{
			session.reset();
			updateRolePoints(false);
		}
	}

	private Role getRoleForModelId(int modelId)
	{
		for (Role role : Role.values())
		{
			if (role.getRoleModelId() == modelId)
			{
				return role;
			}
		}
		return null;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		final GameObject gameObject = gameObjectSpawned.getGameObject();
		if (isHopperGameObject(gameObject.getId()))
		{
			hoppers.add(gameObject);
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		final GroundObject groundObject = groundObjectSpawned.getGroundObject();
		if (groundObject.getId() == ObjectID.BROKEN_TRAP0)
		{
			brokenTraps.add(groundObject);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		final GameObject gameObject = gameObjectDespawned.getGameObject();
		if (isHopperGameObject(gameObject.getId()))
		{
			hoppers.remove(gameObject);
		}
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned)
	{
		final GroundObject groundObject = groundObjectDespawned.getGroundObject();
		if (groundObject.getId() == ObjectID.BROKEN_TRAP0)
		{
			brokenTraps.remove(groundObject);
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final Role role = getRole();
		if (role == null)
		{
			return;
		}

		final TileItem item = itemSpawned.getItem();
		final Tile tile = itemSpawned.getTile();

		addItemSpawn(item, tile, role);
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		final Role role = getRole();
		if (role == null)
		{
			return;
		}

		TileItem item = itemDespawned.getItem();
		Tile tile = itemDespawned.getTile();

		removeItemSpawn(item, tile, role);
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		menuEntrySwapper.performSwaps();
	}

	public Role getRole()
	{
		return wave == null ? null : wave.getRole();
	}

	private void updateAttackStyleText(String listen)
	{

		restoreAttackStyleText();

		if (!config.highlightAttackStyle() || listen == null || getRole() != Role.ATTACKER)
		{
			return;
		}

		final int var = client.getVar(Varbits.EQUIPPED_WEAPON_TYPE);
		final AttackStyle[] styles = WeaponType.getWeaponType(var).getAttackStyles();
		for (int i = 0; i < styles.length; i++)
		{
			final AttackStyle style = styles[i];
			if (style == null || !listen.startsWith(style.getName()))
			{
				continue;
			}

			final int color = Integer.decode(ColorUtil.toHexColor(config.highlightAttackStyleColor()));

			final AttackStyleWidget attackStyleWidget = AttackStyleWidget.getAttackStyles()[i];

			final BaWidgetInfo attackStyleTextBaWidgetInfo = attackStyleWidget.getTextWidget();
			final Widget attackStyleTextWidget = client.getWidget(attackStyleTextBaWidgetInfo.getGroupId(),
					  attackStyleTextBaWidgetInfo.getChildId());
			if (attackStyleTextWidget != null)
			{
				if (attackStyleTextColor == null)
				{
					attackStyleTextColor = attackStyleTextWidget.getTextColor();
				}
				attackStyleTextWidget.setTextColor(color);
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final Role role = getRole();
		if (role == null)
		{
			return;
		}

		final String listen = lastListen != null ? lastListen : "";

		MenuEntry[] menuEntries = client.getMenuEntries();
		final MenuEntry entry = menuEntries[menuEntries.length - 1];
		String entryOption = Text.removeTags(entry.getOption());
		String entryTarget = Text.removeTags(entry.getTarget());

		final MenuHighlightMode mode = config.menuHighlightMode();

		if (mode != MenuHighlightMode.DISABLED
				  && role == Role.COLLECTOR
				  && entryOption.equals("Take")
				  && event.getType() == MENU_THIRD_OPTION
				  && listen.startsWith(entryTarget))
		{
			Color color = getEggColorFromName(entryTarget.split(" ")[0]);
			if (color != null)
			{

				if (mode == MenuHighlightMode.OPTION_NAME || mode == MenuHighlightMode.OPTION)
				{
					entryOption = ColorUtil.prependColorTag("Take", color);
					entry.setOption(entryOption);
				}

				if (mode == MenuHighlightMode.OPTION_NAME || mode == MenuHighlightMode.NAME)
				{
					entryTarget = ColorUtil.prependColorTag(entryTarget.substring(entryTarget.indexOf('>') + 1), color);
					entry.setTarget(entryTarget);
				}
			}

		}

		client.setMenuEntries(menuEntries);
	}

	public Color getColorForInventoryItemId(int itemId)
	{
		switch (itemId)
		{
			case ItemID.BULLET_ARROW:
			case ItemID.FIELD_ARROW:
			case ItemID.BLUNT_ARROW:
			case ItemID.BARBED_ARROW:
				return config.highlightArrowColor();
			case ItemID.POISONED_TOFU:
			case ItemID.POISONED_WORMS:
			case ItemID.POISONED_MEAT:
				return config.highlightPoisonColor();
			case ItemID.CRACKERS:
			case ItemID.TOFU:
			case ItemID.WORMS:
				return config.highlightBaitColor();
		}

		return null;
	}

	public Color getColorForGroundItemId(int itemId)
	{
		switch (itemId)
		{
			case ItemID.GREEN_EGG:
				return Color.GREEN;
			case ItemID.RED_EGG:
				return BaMinigamePlugin.LIGHT_RED;
			case ItemID.BLUE_EGG:
				return BaMinigamePlugin.LIGHT_BLUE;
			case ItemID.YELLOW_EGG:
				return Color.YELLOW;
			case ItemID.CRACKERS:
			case ItemID.TOFU:
			case ItemID.WORMS:
				return config.highlightGroundBaitColor();
			case ItemID.LOGS:
			case ItemID.HAMMER:
				return config.highlightGroundLogsHammerColor();
		}
		return null;
	}

	private void restoreAttackStyleText()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		final int color = attackStyleTextColor != null ? attackStyleTextColor : DEFAULT_ATTACK_STYLE_COLOR;

		final int var = client.getVar(Varbits.EQUIPPED_WEAPON_TYPE);
		final AttackStyle[] styles = WeaponType.getWeaponType(var).getAttackStyles();
		for (int i = 0; i < styles.length; i++)
		{
			AttackStyle style = styles[i];
			if (style == AttackStyle.CASTING || style == AttackStyle.DEFENSIVE_CASTING)
			{
				// magic attack styles will never be highlighted
				continue;
			}

			final AttackStyleWidget attackStyleWidget = AttackStyleWidget.getAttackStyles()[i];
			final BaWidgetInfo attackStyleTextBaWidgetInfo = attackStyleWidget.getTextWidget();

			final Widget attackStyleTextWidget = client.getWidget(attackStyleTextBaWidgetInfo.getGroupId(),
					  attackStyleTextBaWidgetInfo.getChildId());
			if (attackStyleTextWidget != null)
			{
				attackStyleTextWidget.setTextColor(color);
			}
		}

		attackStyleTextColor = null;
	}

	private void onWaveEnded(boolean roundEnded)
	{
		Wave wave = this.wave;
		if (wave == null)
		{
			wave = new Wave(client, currentWave);
		}

		wave.setPoints(roundEnded);
		if (!roundEnded)
		{
			wave.setAmounts();

			final BaWidgetInfo pointsBaWidgetInfo = BaWidgetInfo.BA_REWARD_TEXT;
			final Widget pointsWidget = client.getWidget(pointsBaWidgetInfo.getGroupId(), pointsBaWidgetInfo.getChildId());
			if (pointsWidget != null)
			{
				final boolean colorful = config.enableGameChatColors();

				final PointsMode pointsMode = config.showRewardPointsMode();
				if (pointsMode == PointsMode.WAVE || pointsMode == PointsMode.WAVE_ROUND)
				{
					ChatMessageBuilder wavePoints = wave.getWavePoints(colorful, includeKandarinBonus());
					announce(wavePoints);
				}

				final RewardsBreakdownMode rewardsBreakdownMode = config.showRewardsBreakdownMode();
				if (rewardsBreakdownMode == RewardsBreakdownMode.WAVE || rewardsBreakdownMode == RewardsBreakdownMode.WAVE_ROUND)
				{
					ChatMessageBuilder waveSummary = wave.getWaveSummary(colorful);
					announce(waveSummary);
				}
			}
		}

		if (round != null)
		{
			round.addWave(wave, config.rolePointsTrackingMode());
		}

		if (session == null)
		{
			session = new Session();
		}
		session.addWave(wave, config.rolePointsTrackingMode());

		PointsCounterMode pointsCounterMode = config.pointsCounterMode();
		if (pointsCounterMode == PointsCounterMode.ROUND_POINTS || pointsCounterMode == PointsCounterMode.SESSION_POINTS)
		{
			updateRolePoints(false);
		}

		this.wave = null;
		currentWave = 0;
		lastWaveCompletedTimestamp = System.currentTimeMillis();
	}

	private void onRoundEnded()
	{
		if (round == null)
		{
			return;
		}

		onWaveEnded(true);

		final DurationMode durationMode = config.showDurationMode();
		if (durationMode == DurationMode.ROUND || durationMode == DurationMode.WAVE_ROUND)
		{
			announceRoundTime();
		}

		if (round.isComplete())
		{
			final boolean colorful = config.enableGameChatColors();

			final PointsMode pointsMode = config.showRewardPointsMode();
			if (pointsMode == PointsMode.ROUND || pointsMode == PointsMode.WAVE_ROUND)
			{
				ChatMessageBuilder roundPoints = round.getRoundPointsMessage(colorful, includeKandarinBonus());
				announce(roundPoints);
			}

			final RewardsBreakdownMode rewardsBreakdownMode = config.showRewardsBreakdownMode();
			if (rewardsBreakdownMode == RewardsBreakdownMode.ROUND || rewardsBreakdownMode == RewardsBreakdownMode.WAVE_ROUND)
			{
				ChatMessageBuilder roundSummary = round.getRoundSummaryMessage(colorful);
				announce(roundSummary);
			}
		}

		timer = null;
	}

	// wave starts when ba ingamebit == 1 (without role set) or when ba widgets are loaded (with role set)
	private void startWave(Role role, boolean displayTickTimer)
	{
		// Prevent changing waves when a wave is already set, as widgets can be
		// loaded multiple times in game from eg. opening and closing the horn
		// of glory.
		if (wave != null)
		{
			if (wave.getRole() == null && role != null)
			{
				// wave has started at ba ingamebit == 1, but role is not set
				wave.setRole(role);
				setCallFlashColor(wave, config.callChangeFlashColor());
				if (role == Role.HEALER)
				{
					setHealerTeammatesHealthDisplay();
				}
				runnerTickTimer.setDisplaying(displayTickTimer);
				clientThread.invokeLater(() -> setWaveWidgets(wave));
			}
			return;
		}

		if (timer == null)
		{
			timer = new Timer();
		}

		log.debug("Starting wave {} with roles 1={} 2={} 3={} 4={} 5={} at {}", currentWave, playerRoles[0],
				  playerRoles[1], playerRoles[2], playerRoles[3], playerRoles[4],
				  timer.getRoundTimeFormatted(false, config.timeUnits()));

		timer.setWaveStartTime();
		wave = new Wave(client, currentWave, playerRoles, timer);
		wave.setRole(role);
		runnerTickTimer = new RunnerTickTimer();
		runnerTickTimer.setDisplaying(displayTickTimer);
		correctedCallCount = 0;

		if (role != null)
		{
			setCallFlashColor(wave, config.callChangeFlashColor());
			if (role == Role.HEALER)
			{
				setHealerTeammatesHealthDisplay();
			}
			clientThread.invokeLater(() -> setWaveWidgets(wave));
		}
	}

	private void setWaveWidgets(Wave wave)
	{
		if (wave == null)
		{
			return;
		}
		Role role = wave.getRole();
		if (role == null)
		{
			return;
		}
		Widget waveInfoWidget = client.getWidget(role.getWaveInfo().getGroupId(), role.getWaveInfo().getChildId());
		if (waveInfoWidget == null)
		{
			return;
		}
		Widget waveTextWidget = client.getWidget(role.getWaveText().getGroupId(), role.getWaveText().getChildId());
		if (waveTextWidget == null)
		{
			return;
		}

		waveInfoWidget.setOriginalHeight(130);
		waveInfoWidget.revalidate();

		boolean showingCounter = role == Role.HEALER && config.showHpCountOverlay()
				  || role == Role.COLLECTOR && config.showEggCountOverlay();
		if (showingCounter)
		{
			Widget roleCounter = waveInfoWidget.createChild(0, WidgetType.TEXT);
			roleCounter.setFontId(waveTextWidget.getFontId());
			roleCounter.setTextColor(waveTextWidget.getTextColor());
			roleCounter.setTextShadowed(true);
			roleCounter.setOriginalX(24);
			roleCounter.setOriginalY(config.hideTeammateRole() ? 57 : 78);
			roleCounter.setOriginalHeight(32);
			roleCounter.setOriginalWidth(75);
			roleCounter.setXTextAlignment(WidgetTextAlignment.RIGHT);
			roleCounter.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
			roleCounter.setYPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
			roleCounter.setClickMask(WidgetConfig.USE_GROUND_ITEM | WidgetConfig.USE_ITEM | WidgetConfig.USE_NPC
					  | WidgetConfig.USE_OBJECT | WidgetConfig.USE_WIDGET);
			roleCounter.setNoClickThrough(false);
			roleCounter.revalidate();

			Widget roleCounterIcon;
			if (role == Role.HEALER)
			{
				roleCounterIcon = waveInfoWidget.createChild(1, WidgetType.GRAPHIC);
				roleCounterIcon.setSpriteId(SpriteID.SPELL_CURE_ME);
				roleCounterIcon.setOriginalHeight(17);
				roleCounterIcon.setOriginalWidth(17);
			}
			else
			{
				roleCounterIcon = waveInfoWidget.createChild(1, WidgetType.MODEL);
				// yellow egg model, from team widget on ba lobby
				roleCounterIcon.setModelId(BaModelID.BA_COLLECTOR_ICON);
				roleCounterIcon.setModelType(1);
				roleCounterIcon.setModelZoom(4112);
				roleCounterIcon.setRotationX(510);
				roleCounterIcon.setRotationZ(2014);
				roleCounterIcon.setOriginalHeight(16);
				roleCounterIcon.setOriginalWidth(16);
			}
			roleCounterIcon.setOriginalX(380);
			roleCounterIcon.setOriginalY(config.hideTeammateRole() ? 64 : 87);
			roleCounterIcon.setClickMask(WidgetConfig.USE_GROUND_ITEM | WidgetConfig.USE_ITEM | WidgetConfig.USE_NPC
					  | WidgetConfig.USE_OBJECT | WidgetConfig.USE_WIDGET);
			roleCounterIcon.setNoClickThrough(false);
			roleCounterIcon.revalidate();
		}

		if (config.showCallChangeTimer())
		{
			if (showingCounter)
			{
				setCallTimerWidget(waveInfoWidget, waveTextWidget, config.hideTeammateRole() ? 80 : 100,
						  config.hideTeammateRole() ? 89 : 109);
			}
			else if (role == Role.ATTACKER)
			{
				setCallTimerWidget(waveInfoWidget, waveTextWidget, config.hideTeammateRole() ? 67 : 87,
						  config.hideTeammateRole() ? 76 : 96);
			}
			else
			{
				setCallTimerWidget(waveInfoWidget, waveTextWidget, config.hideTeammateRole() ? 58 : 78,
						  config.hideTeammateRole() ? 67 : 87);
			}
		}

		final Widget roleText = client.getWidget(role.getRoleText().getGroupId(), role.getRoleText().getChildId());
		if (roleText != null)
		{
			roleText.setHidden(config.hideTeammateRole());
		}
		final Widget roleSprite = client.getWidget(role.getRoleSprite().getGroupId(), role.getRoleSprite().getChildId());
		if (roleSprite != null)
		{
			roleSprite.setHidden(config.hideTeammateRole());
		}
	}

	private void setCallTimerWidget(Widget waveInfoWidget, Widget waveTextWidget, int textOriginalY, int iconOriginalY)
	{
		Widget callTimer = waveInfoWidget.createChild(2, WidgetType.TEXT);
		callTimer.setFontId(waveTextWidget.getFontId());
		callTimer.setTextColor(waveTextWidget.getTextColor());
		callTimer.setTextShadowed(true);
		callTimer.setOriginalX(24);
		callTimer.setOriginalY(textOriginalY);
		callTimer.setOriginalHeight(32);
		callTimer.setOriginalWidth(75);
		callTimer.setXTextAlignment(WidgetTextAlignment.RIGHT);
		callTimer.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		callTimer.setYPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		callTimer.setText("- - -");
		callTimer.setClickMask(WidgetConfig.USE_GROUND_ITEM | WidgetConfig.USE_ITEM | WidgetConfig.USE_NPC
				  | WidgetConfig.USE_OBJECT | WidgetConfig.USE_WIDGET);
		callTimer.setNoClickThrough(false);
		callTimer.revalidate();

		Widget callTimerIcon = waveInfoWidget.createChild(3, WidgetType.GRAPHIC);
		callTimerIcon.setSpriteId(getRole().getHornSpriteId());
		callTimerIcon.setOriginalX(380);
		callTimerIcon.setOriginalY(iconOriginalY);
		callTimerIcon.setOriginalHeight(13);
		callTimerIcon.setOriginalWidth(17);
		callTimerIcon.setClickMask(WidgetConfig.USE_GROUND_ITEM | WidgetConfig.USE_ITEM | WidgetConfig.USE_NPC
				  | WidgetConfig.USE_OBJECT | WidgetConfig.USE_WIDGET);
		callTimerIcon.setNoClickThrough(false);
		callTimerIcon.revalidate();
	}

	private void removeWaveWidgets(Wave wave)
	{
		if (wave == null)
		{
			return;
		}
		Role role = wave.getRole();
		if (role == null)
		{
			return;
		}
		Widget waveInfoWidget = client.getWidget(role.getWaveInfo().getGroupId(), role.getWaveInfo().getChildId());
		if (waveInfoWidget == null)
		{
			return;
		}
		Widget[] children = waveInfoWidget.getChildren();
		if (children == null)
		{
			return;
		}
		if (children.length > 0 && children[0] != null)
		{
			children[0].setHidden(true);
			children[0] = null;
		}
		if (children.length > 1 && children[1] != null)
		{
			children[1].setHidden(true);
			children[1] = null;
		}
		if (children.length > 2 && children[2] != null)
		{
			children[2].setHidden(true);
			children[2] = null;
		}
		if (children.length > 3 && children[3] != null)
		{
			children[3].setHidden(true);
			children[3] = null;
		}
	}

	private void setCallFlashColor(Wave wave, Color flashColor)
	{
		if (wave == null)
		{
			return;
		}
		final Role role = wave.getRole();
		if (role == null)
		{
			return;
		}
		final Widget callFlashWidget = client.getWidget(role.getCallFlash().getGroupId(), role.getCallFlash().getChildId());
		if (callFlashWidget != null)
		{
			final int color = Integer.decode(ColorUtil.toHexColor(new Color(flashColor.getRed(), flashColor.getGreen(), flashColor.getBlue())));
			callFlashWidget.setTextColor(color);
			callFlashWidget.setOpacity(255 - flashColor.getAlpha());
		}
	}

	private void removeCallChangeTimer(Wave wave)
	{
		if (wave == null)
		{
			return;
		}
		Role role = wave.getRole();
		if (role == null)
		{
			return;
		}
		Widget waveInfoWidget = client.getWidget(role.getWaveInfo().getGroupId(), role.getWaveInfo().getChildId());
		if (waveInfoWidget == null)
		{
			return;
		}
		Widget[] children = waveInfoWidget.getChildren();
		if (children != null && children.length >= 4)
		{
			if (children[2] != null)
			{
				children[2].setHidden(true);
				children[2] = null;
			}
			if (children[3] != null)
			{
				children[3].setHidden(true);
				children[3] = null;
			}
		}
	}

	private void restoreWaveWidget(Wave wave, boolean restoreIcon)
	{
		if (wave == null)
		{
			return;
		}
		Role role = wave.getRole();
		if (role == null)
		{
			return;
		}
		final BaWidgetInfo waveInfo = role.getWaveText();
		final Widget waveText = client.getWidget(waveInfo.getGroupId(), waveInfo.getChildId());
		if (waveText != null)
		{
			waveText.setText("Wave " + wave.getNumber());
		}
		if (restoreIcon)
		{
			restoreWaveIcon(wave);
		}
	}

	private void restoreWaveIcon(Wave wave)
	{
		if (wave == null)
		{
			return;
		}
		Role role = wave.getRole();
		if (role == null)
		{
			return;
		}
		final Widget waveSprite = client.getWidget(role.getWaveSprite().getGroupId(), role.getWaveSprite().getChildId());
		if (waveSprite != null)
		{
			waveSprite.setSpriteId(SpriteID.BARBARIAN_ASSAULT_WAVE_ICON);
			waveSprite.setOriginalWidth(WAVE_ICON_WIDTH);
			waveSprite.setOriginalX(3);
		}
	}

	private void stopWave()
	{
		hoppers.clear();
		brokenTraps.clear();
		cannonEggs.clear();
		groundEggs.clear();
		groundBait.clear();
		groundLogsHammer.clear();
		disableRunnerTickTimer(true);
		removeDeathTimesInfoBoxes();
		lastListen = null;
		lastListenItemId = 0;
		clientThread.invokeLater(this::restoreAttackStyleText);
	}

	private void announceWaveTime()
	{
		if (wave == null || wave.getTimer() == null)
		{
			return;
		}

		final String time = wave.getTimer().getWaveTimeFormatted(config.timeUnits());
		final int number = wave.getNumber();

		final StringBuilder message = new StringBuilder();
		message.append("Wave ");
		if (number > 0)
		{
			message.append(number).append(" ");
		}
		message.append("duration: ");

		announceTime(message.toString(), time);
	}

	private void announceRoundTime()
	{
		if (round == null || round.getTimer() == null)
		{
			return;
		}

		final String time = round.getTimer().getRoundTimeFormatted(true, config.timeUnits());
		final int fromWave = round.getStartingWave();
		final StringBuilder message = new StringBuilder();
		if (fromWave == 1)
		{
			message.append("Round duration: ");
		}
		else
		{
			message.append("Round duration from wave ").append(fromWave).append(": ");
		}
		announceTime(message.toString(), time);
	}


	private void announceTime(String preText, String time)
	{
		ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
				  .append(ChatColorType.NORMAL)
				  .append(preText);
		if (config.enableGameChatColors())
		{
			chatMessageBuilder.append(ChatColorType.HIGHLIGHT);
		}
		chatMessageBuilder = chatMessageBuilder.append(time);

		announce(chatMessageBuilder);
	}

	private void announce(final ChatMessageBuilder chatMessage)
	{
		chatMessageManager.queue(QueuedMessage.builder()
				  .type(ChatMessageType.CONSOLE)
				  .runeLiteFormattedMessage(chatMessage.build())
				  .build());
	}

	private Color getEggColorFromName(String eggName)
	{
		switch (eggName.toLowerCase())
		{
			case "green":
				return Color.GREEN;
			case "red":
				return BaMinigamePlugin.LIGHT_RED;
			case "blue":
				return BaMinigamePlugin.LIGHT_BLUE;
		}
		return null;
	}

	private void updateEggsCount()
	{
		for (CollectorEgg collectorEgg : CollectorEgg.values())
		{
			final int eggsCount = client.getVarbitValue(collectorEgg.getVarbits().getId());
			if (eggsCount < 1)
			{
				cannonEggs.remove(collectorEgg);
			}
			else
			{
				cannonEggs.put(collectorEgg, eggsCount);
			}
		}
	}

	private void addItemSpawn(TileItem item, Tile tile, Role role)
	{
		final GroundItem.Key Key = new GroundItem.Key(item.getId(), tile.getWorldLocation());
		final GroundItem groundItem = buildGroundItem(tile, item);

		switch (role)
		{
			case COLLECTOR:
			{
				if (isEggItem(item.getId()))
				{
					addEggSpawn(Key, groundItem);
				}
				break;
			}
			case DEFENDER:
			{
				if (isBaitItem(item.getId()))
				{
					addDefenderBaitSpawn(Key, groundItem);
				}
				else if (isLogsOrHammerItem(item.getId()))
				{
					addLogsHammerSpawn(Key, groundItem);
				}
				break;
			}
		}
	}

	private void removeItemSpawn(TileItem item, Tile tile, Role role)
	{
		switch (role)
		{
			case COLLECTOR:
			{
				if (isEggItem(item.getId()))
				{
					removeEggSpawn(item, tile);
				}
				break;
			}
			case DEFENDER:
			{
				if (isBaitItem(item.getId()))
				{
					removeBaitSpawn(item, tile);
				}
				else if (isLogsOrHammerItem(item.getId()))
				{
					removeLogsHammerSpawn(item, tile);
				}
				break;
			}
		}
	}

	private void addEggSpawn(GroundItem.Key Key, GroundItem groundItem)
	{
		addItemSpawn(Key, groundItem, groundEggs);
	}

	private void addDefenderBaitSpawn(GroundItem.Key Key, GroundItem groundItem)
	{
		addItemSpawn(Key, groundItem, groundBait);
	}

	private void addLogsHammerSpawn(GroundItem.Key Key, GroundItem groundItem)
	{
		addItemSpawn(Key, groundItem, groundLogsHammer);
	}

	private void addItemSpawn(GroundItem.Key Key, GroundItem groundItem, Map<GroundItem.Key, GroundItem> spawnItems)
	{
		GroundItem existing = spawnItems.putIfAbsent(Key, groundItem);
		if (existing != null)
		{
			existing.setQuantity(existing.getQuantity() + groundItem.getQuantity());
		}
	}

	private GroundItem buildGroundItem(final Tile tile, final TileItem item)
	{
		final int itemId = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		final int realItemId = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemId;

		return GroundItem.builder()
				  .id(itemId)
				  .location(tile.getWorldLocation())
				  .itemId(realItemId)
				  .quantity(item.getQuantity())
				  .name(itemComposition.getName())
				  .height(tile.getItemLayer().getHeight())
				  .spawnTime(Instant.now())
				  .build();
	}

	private void removeEggSpawn(TileItem item, Tile tile)
	{
		GroundItem.Key Key = new GroundItem.Key(item.getId(), tile.getWorldLocation());
		GroundItem groundItem = removeItemSpawn(item, Key, groundEggs);
		if (groundItem == null)
		{
			return;
		}

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		Instant spawnTime = groundItem.getSpawnTime();
		Instant now = Instant.now();
		if (wave != null
				  && wave.getNumber() != 10
				  && (spawnTime == null || now.isBefore(spawnTime.plus(Duration.ofMinutes(2))))
				  && groundItem.getLocation().equals(player.getWorldLocation()))
		{
			wave.setEggsCount(wave.getEggsCount() + 1);
		}
	}

	private void removeBaitSpawn(TileItem item, Tile tile)
	{
		GroundItem.Key Key = new GroundItem.Key(item.getId(), tile.getWorldLocation());
		removeItemSpawn(item, Key, groundBait);
	}

	private void removeLogsHammerSpawn(TileItem item, Tile tile)
	{
		GroundItem.Key Key = new GroundItem.Key(item.getId(), tile.getWorldLocation());
		removeItemSpawn(item, Key, groundLogsHammer);
	}

	private GroundItem removeItemSpawn(TileItem item, GroundItem.Key Key, Map<GroundItem.Key, GroundItem> spawnItems)
	{
		GroundItem groundItem = spawnItems.get(Key);
		if (groundItem == null)
		{
			return null;
		}

		if (groundItem.getQuantity() <= item.getQuantity())
		{
			spawnItems.remove(Key);
		}
		else
		{
			groundItem.setQuantity(groundItem.getQuantity() - item.getQuantity());
			// When picking up an item when multiple stacks appear on the ground,
			// it is not known which item is picked up, so we invalidate the spawn
			// time
			groundItem.setSpawnTime(null);
		}

		return groundItem;
	}


	private boolean isEggItem(int itemId)
	{
		return itemId == ItemID.RED_EGG
				  || itemId == ItemID.GREEN_EGG
				  || itemId == ItemID.BLUE_EGG
				  || itemId == ItemID.YELLOW_EGG;
	}

	private boolean isBaitItem(int itemId)
	{
		return itemId == ItemID.TOFU
				  || itemId == ItemID.WORMS
				  || itemId == ItemID.CRACKERS;
	}

	private boolean isLogsOrHammerItem(int itemId)
	{
		return itemId == ItemID.LOGS
				  || itemId == ItemID.HAMMER;
	}

	private boolean isHopperGameObject(int gameObjectId)
	{
		return gameObjectId == ObjectID.EGG_HOPPER
				  || gameObjectId == ObjectID.EGG_HOPPER_20265
				  || gameObjectId == ObjectID.EGG_HOPPER_20266
				  || gameObjectId == BaObjectID.EGG_HOPPER_20267;
	}

	private void enableRunnerTickTimer(boolean display)
	{
		if (runnerTickTimer == null)
		{
			runnerTickTimer = new RunnerTickTimer();
		}
		runnerTickTimer.setDisplaying(display);
	}

	private void disableRunnerTickTimer(boolean remove)
	{
		if (runnerTickTimer != null)
		{
			runnerTickTimer.setDisplaying(false);
		}
		if (remove)
		{
			runnerTickTimer = null;
		}
	}

	private void showDeathTimes()
	{
		List<InfoBox> infoBoxes = infoBoxManager.getInfoBoxes();

		for (InfoBox infoBox : deathTimesInfoBoxes)
		{
			if (!infoBoxes.contains(infoBox))
			{
				infoBoxManager.addInfoBox(infoBox);
			}
		}
	}

	private void showRolePoints()
	{
		List<InfoBox> infoBoxes = infoBoxManager.getInfoBoxes();

		for (InfoBox infoBox : rolePointsInfoBoxes.values())
		{
			if (!infoBoxes.contains(infoBox))
			{
				infoBoxManager.addInfoBox(infoBox);
			}
		}
	}

	private void hideDeathTimesInfoBoxes()
	{
		for (InfoBox infoBox : deathTimesInfoBoxes)
		{
			infoBoxManager.removeInfoBox(infoBox);
		}
	}

	private void hideRolePointsInfoBoxes()
	{
		for (InfoBox infoBox : rolePointsInfoBoxes.values())
		{
			infoBoxManager.removeInfoBox(infoBox);
		}
	}

	private void hideRolePointsInfoBox(Role role)
	{
		infoBoxManager.removeInfoBox(rolePointsInfoBoxes.get(role));
	}

	private void addDeathTimesInfoBoxes(String npc, float time, String format)
	{
		final BufferedImage image = images.get(npc.toLowerCase());
		final DeathTimeInfoBox infoBox = new DeathTimeInfoBox(image, this, time, format);

		deathTimesInfoBoxes.add(infoBox);

		final DeathTimesMode deathTimesMode = config.deathTimesMode();
		if (deathTimesMode == DeathTimesMode.INFO_BOX || deathTimesMode == DeathTimesMode.INFOBOX_CHAT)
		{
			infoBoxManager.addInfoBox(infoBox);
		}
	}

	private void addOrUpdateRolePointsInfoBoxes(Role role, int points)
	{
		final DisplayPointsMode displayPointsMode = config.displayPointsMode();
		final RolePointsTrackingMode rolePointsTrackingMode = config.rolePointsTrackingMode();
		if (displayPointsMode != DisplayPointsMode.INFO_BOX
				  || rolePointsTrackingMode == RolePointsTrackingMode.ATTACKER && role != Role.ATTACKER
				  || rolePointsTrackingMode == RolePointsTrackingMode.DEFENDER && role != Role.DEFENDER
				  || rolePointsTrackingMode == RolePointsTrackingMode.HEALER && role != Role.HEALER
				  || rolePointsTrackingMode == RolePointsTrackingMode.COLLECTOR && role != Role.COLLECTOR)
		{
			hideRolePointsInfoBox(role);
		}
		else
		{
			final RolePointsInfoBox previousInfoBox = rolePointsInfoBoxes.get(role);
			if (previousInfoBox != null)
			{
				previousInfoBox.setPoints(points);
				if (!infoBoxManager.getInfoBoxes().contains(previousInfoBox))
				{
					infoBoxManager.addInfoBox(previousInfoBox);
				}
			}
			else
			{
				final RolePointsInfoBox infoBox = new RolePointsInfoBox(images.get(role.getName().toLowerCase(Locale.ROOT)), this, points);
				rolePointsInfoBoxes.put(role, infoBox);
				infoBoxManager.addInfoBox(infoBox);
			}
		}
	}

	private void removeDeathTimesInfoBoxes()
	{
		hideDeathTimesInfoBoxes();
		deathTimesInfoBoxes.clear();
	}

	private void removeRolePointsInfoBoxes()
	{
		hideRolePointsInfoBoxes();
		rolePointsInfoBoxes.clear();
	}

	private void setGroundItemsPluginLists()
	{
		String highlightedItems = Optional
				  .ofNullable(configManager.getConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIGHLIGHTED_ITENS))
				  .orElse("");
		final List<String> highlightedItemsList = Arrays.stream(highlightedItems.split(","))
				  .map(i -> i.trim().toLowerCase()).collect(Collectors.toList());

		final String hiddenItems = Optional
				  .ofNullable(configManager.getConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIDDEN_ITENS))
				  .orElse("");
		final List<String> hiddenItemsList = Arrays.stream(hiddenItems.split(","))
				  .map(i -> i.trim().toLowerCase()).collect(Collectors.toList());

		final StringBuilder highlightedItemsListBuilder = new StringBuilder();
		final StringBuilder hiddenItemsListBuilder = new StringBuilder();
		for (String item : GROUND_ITEMS_HIDDEN_LIST)
		{
			if (highlightedItemsList.contains(item.toLowerCase()))
			{
				if (highlightedItemsListBuilder.length() > 0)
				{
					highlightedItemsListBuilder.append(",");
				}
				highlightedItemsListBuilder.append(item);

				// regex to replace any white spaces, followed by 0 or more commas, followed by any white spaces,
				// (?i) mode to match case insensitive, followed by any white spaces, followed by 0 or more commas,
				// and finally followed by any white spaces
				highlightedItems = highlightedItems.replaceAll("\\s*,*\\s*(?i)" + Pattern.quote(item) + "\\s*,*\\s*", ",");

				if (highlightedItems.startsWith(","))
				{
					highlightedItems = highlightedItems.substring(1);
				}
				if (highlightedItems.endsWith(","))
				{
					highlightedItems = highlightedItems.substring(0, highlightedItems.length() - 1);
				}
			}

			if (!hiddenItemsList.contains(item.toLowerCase()))
			{
				if (hiddenItemsListBuilder.length() > 0)
				{
					hiddenItemsListBuilder.append(",");
				}
				hiddenItemsListBuilder.append(item);
			}
		}

		final StringBuilder hiddenItemsBuilder = new StringBuilder(hiddenItems);
		if (hiddenItemsListBuilder.length() > 0 && !hiddenItems.endsWith(","))
		{
			hiddenItemsBuilder.append(",");
		}
		hiddenItemsBuilder.append(hiddenItemsListBuilder);

		config.setGroundItemsPluginHighlightedList(highlightedItemsListBuilder.toString());
		config.setGroundItemsPluginHiddenList(hiddenItemsListBuilder.toString());

		configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIGHLIGHTED_ITENS, highlightedItems);
		configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIDDEN_ITENS, hiddenItemsBuilder.toString());
	}

	private void restoreGroundItemsPluginLists()
	{
		String highlightedItems = Optional
				  .ofNullable(configManager.getConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIGHLIGHTED_ITENS))
				  .orElse("");
		StringBuilder highlightedItemsBuilder = new StringBuilder(highlightedItems);
		String[] highlightedItemsArray = config.getGroundItemsPluginHighlightedList().split(",");
		final List<String> highlightedItemsList = Arrays.stream(highlightedItems.split(","))
				  .map(i -> i.trim().toLowerCase()).collect(Collectors.toList());

		for (String s : highlightedItemsArray)
		{
			String item = s.trim();
			if (!highlightedItemsList.contains(item.toLowerCase()))
			{
				if (!highlightedItems.isEmpty() && !highlightedItems.endsWith(","))
				{
					highlightedItemsBuilder.append(",");
				}
				highlightedItemsBuilder.append(item);
			}
		}

		configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIGHLIGHTED_ITENS, highlightedItemsBuilder.toString());
		config.setGroundItemsPluginHighlightedList("");

		String hiddenItems = configManager.getConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIDDEN_ITENS);
		final String[] list = config.getGroundItemsPluginHiddenList().split(",");
		for (String item : list)
		{
			item = item.trim();
			if (item.length() > 0 && StringUtils.containsIgnoreCase(hiddenItems, item))
			{
				// regex to replace any white spaces, followed by 0 or more commas, followed by any white spaces,
				// (?i) mode to match case insensitive, followed by any white spaces, followed by 0 or more commas,
				// and finally followed by any white spaces
				hiddenItems = hiddenItems.replaceAll("\\s*,*\\s*(?i)" + Pattern.quote(item) + "\\s*,*\\s*", ",");

				if (hiddenItems.startsWith(","))
				{
					hiddenItems = hiddenItems.substring(1);
				}
				if (hiddenItems.endsWith(","))
				{
					hiddenItems = hiddenItems.substring(0, hiddenItems.length() - 1);
				}
			}
		}
		configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, GROUND_ITEMS_CONFIG_HIDDEN_ITENS, hiddenItems);
		config.setGroundItemsPluginHiddenList("");
	}

	private void disableBarbarianAssaultPluginFeatures()
	{
		StringBuilder configsBuilder = new StringBuilder();
		for (String config : BARBARIAN_ASSAULT_CONFIGS)
		{
			final String value = configManager.getConfiguration(BARBARIAN_ASSAULT_CONFIG_GROUP, config);
			if (configsBuilder.length() > 0)
			{
				configsBuilder.append(",");
			}
			configsBuilder.append(config).append("=").append(value);
			configManager.setConfiguration(BARBARIAN_ASSAULT_CONFIG_GROUP, config, false);
		}
		if (config.getBarbarianAssaultConfigs().length() == 0)
		{
			config.setBarbarianAssaultConfigs(configsBuilder.toString());
		}
	}

	private void restoreBarbarianAssaultPluginFeatures()
	{
		final String[] configs = config.getBarbarianAssaultConfigs().split(",");
		for (String config : configs)
		{
			final String[] keyValue = config.split("=");
			if (keyValue.length == 2)
			{
				final String key = keyValue[0];
				final String value = keyValue[1];
				configManager.setConfiguration(BARBARIAN_ASSAULT_CONFIG_GROUP, key, value);
			}
		}
		config.setBarbarianAssaultConfigs("");
	}

	public boolean isDisplayingHealerTeammatesHealth()
	{
		return !config.hideHealerTeammatesHealth() || teammatesHealthHotkeyPressed;
	}

	private void setHealerTeammatesHealthDisplay()
	{
		Widget teammatesHealth = client.getWidget(BaWidgetInfo.BA_HEAL_TEAMMATES.getGroupId(),
				  BaWidgetInfo.BA_HEAL_TEAMMATES.getChildId());
		if (teammatesHealth == null)
		{
			return;
		}
		if (config.hideHealerTeammatesHealth())
		{
			teammatesHealth.setHidden(!teammatesHealthHotkeyPressed);
		}
		else
		{
			teammatesHealth.setHidden(false);
		}
	}

	private void restoreHealerTeammatesHealth()
	{
		Widget teammatesHealth = client.getWidget(BaWidgetInfo.BA_HEAL_TEAMMATES.getGroupId(),
				  BaWidgetInfo.BA_HEAL_TEAMMATES.getChildId());
		if (teammatesHealth != null)
		{
			teammatesHealth.setHidden(false);
		}
	}

	void onTeammatesHealthHotkeyChanged(boolean pressed)
	{
		this.teammatesHealthHotkeyPressed = pressed;
		if (wave != null && wave.getRole() == Role.HEALER)
		{
			setHealerTeammatesHealthDisplay();
		}
	}

	public boolean shouldDisplayRolePoints()
	{
		DisplayPointsLocationMode locationMode = config.displayPointsLocationMode();
		if (locationMode == DisplayPointsLocationMode.ALWAYS)
		{
			return true;
		}
		if (locationMode == DisplayPointsLocationMode.NEVER)
		{
			return false;
		}
		if (locationMode == DisplayPointsLocationMode.OUTPOST ||
				  locationMode == DisplayPointsLocationMode.OUTPOST_INGAME)
		{
			if (locationMode == DisplayPointsLocationMode.OUTPOST_INGAME && inGameBit == 1)
			{
				return true;
			}
			if (this.worldPoint == null)
			{
				return false;
			}
			final int regionId = this.worldPoint.getRegionID();
			final int x = this.worldPoint.getX();
			final int y = this.worldPoint.getY();
			return regionId == BA_UNDERGROUND_REGION_ID
					  || x >= BA_TILE_START_X && x <= BA_TILE_END_X && y >= BA_TILE_START_Y && y <= BA_TILE_END_Y;
		}
		return false;
	}

	private void checkCallCorrection(String currentListen)
	{
		final int maxCorrections = 3;

		if (lastListen == null || currentListen == null)
		{
			return;
		}

		if (currentListen.startsWith("-"))
		{
			correctedCallCount = 0;
		}
		else if (!lastListen.startsWith("-") && !currentListen.startsWith("-"))
		{
			// Limit spam in the case your teammate decides to play with their horn :-)
			if (++correctedCallCount > maxCorrections)
			{
				return;
			}

			ChatMessageBuilder message = new ChatMessageBuilder().append("Call correction: ");
			if (config.enableGameChatColors())
			{
				message = message.append(ChatColorType.HIGHLIGHT);
			}
			message.append(currentListen);
			announce(message);
		}
	}

	private void resetRolePoints()
	{
		if (round != null)
		{
			round.resetRolePoints();
		}
		if (session != null)
		{
			session.resetRolePoints();
		}
		PointsCounterMode pointsCounterMode = config.pointsCounterMode();
		if (pointsCounterMode != PointsCounterMode.CURRENT_POINTS)
		{
			clientThread.invokeLater(() -> updateRolePoints(false));
		}
	}
}
