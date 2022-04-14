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

import begosrs.barbarianassault.deathtimes.DeathTimesMode;
import begosrs.barbarianassault.grounditems.GroundEggsMode;
import begosrs.barbarianassault.grounditems.MenuHighlightMode;
import begosrs.barbarianassault.inventory.InventoryHighlightMode;
import begosrs.barbarianassault.points.DisplayPointsLocationMode;
import begosrs.barbarianassault.points.DisplayPointsMode;
import begosrs.barbarianassault.points.KandarinDiaryBonusMode;
import begosrs.barbarianassault.points.PointsCounterMode;
import begosrs.barbarianassault.points.PointsMode;
import begosrs.barbarianassault.points.RewardsBreakdownMode;
import begosrs.barbarianassault.points.RolePointsTrackingMode;
import begosrs.barbarianassault.timer.DurationMode;
import begosrs.barbarianassault.timer.TimeUnits;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

import java.awt.Color;

@ConfigGroup("baMinigame")
public interface BaMinigameConfig extends Config
{

	@ConfigSection(
			  name = "In-game",
			  description = "Configurations related to features inside a barbarian assault game",
			  position = 50,
			  closedByDefault = true
	)
	String inGameSection = "inGameSection";
	@ConfigSection(
			  name = "Attacker",
			  description = "Options associated to the Attacker role",
			  position = 51,
			  closedByDefault = true
	)
	String attackerSection = "attacker";
	@ConfigSection(
			  name = "Defender",
			  description = "Options associated to the Defender role",
			  position = 52,
			  closedByDefault = true
	)
	String defenderSection = "defender";
	@ConfigSection(
			  name = "Collector",
			  description = "Options associated to the Collector role",
			  position = 53,
			  closedByDefault = true
	)
	String collectorSection = "collector";
	@ConfigSection(
			  name = "Healer",
			  description = "Options associated to the Healer role",
			  position = 54,
			  closedByDefault = true
	)
	String healerSection = "healer";
	@ConfigSection(
			  name = "Post-game",
			  description = "Configurations related to features after a barbarian assault game",
			  position = 55,
			  closedByDefault = true
	)
	String postGameSection = "postGameSection";
	@ConfigSection(
			  name = "Points",
			  description = "Configurations related to role points",
			  position = 66,
			  closedByDefault = true
	)
	String pointsSection = "pointsSection";

	@ConfigItem(
			  keyName = "enableGameChatColors",
			  name = "Chat colors",
			  description = "Enable game chat colors on messages announced by this plugin",
			  position = 0
	)
	default boolean enableGameChatColors()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "swapQuickStart",
			  name = "Swap lobby ladder",
			  description = "Swap Climb-down with Quick-start on lobby ladders",
			  position = 1
	)
	default boolean swapQuickStart()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "swapGetRewards",
			  name = "Swap Commander Connad",
			  description = "Swap Talk-to with Get-rewards for the Commander Connad",
			  position = 2
	)
	default boolean swapGetRewards()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "showWaveCompleted",
			  name = "Display wave completed icon",
			  description = "Replaces the wave icon for a checkmark when role npcs are dead",
			  section = inGameSection,
			  position = 0
	)
	default boolean showWaveCompleted()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "showWaveTimer",
			  name = "Display wave timer",
			  description = "Displays time elapsed inside wave",
			  section = inGameSection,
			  position = 1
	)
	default boolean showWaveTimer()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "showCallChangeTimer",
			  name = "Display call change timer",
			  description = "Displays time to next call change",
			  section = inGameSection,
			  position = 2
	)
	default boolean showCallChangeTimer()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "hideTeammateRole",
			  name = "Hide teammate role",
			  description = "Hide the teammate role on the wave information",
			  section = inGameSection,
			  position = 3
	)
	default boolean hideTeammateRole()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "timeUnits",
			  name = "Time units",
			  description = "Controls time precision for wave/round times and penance deaths",
			  section = inGameSection,
			  position = 4
	)
	default TimeUnits timeUnits()
	{
		return TimeUnits.SECONDS;
	}

	@Alpha
	@ConfigItem(
			  keyName = "callChangeFlashColor",
			  name = "Call change flash color",
			  description = "Select the color to flash the call change",
			  section = inGameSection,
			  position = 5
	)
	default Color callChangeFlashColor()
	{
		return BaMinigamePlugin.DEFAULT_FLASH_COLOR;
	}

	@ConfigItem(
			  keyName = "deathTimesMode",
			  name = "Death times",
			  description = "Shows the time all penance monsters of a certain type are killed in an info box, the chat, or both",
			  section = inGameSection,
			  position = 6
	)
	default DeathTimesMode deathTimesMode()
	{
		return DeathTimesMode.INFOBOX_CHAT;
	}

	@ConfigItem(
			  keyName = "deathMessageColor",
			  name = "Death messages color",
			  description = "Recolors the penance death message relevant to the current role",
			  section = inGameSection,
			  position = 7
	)
	default Color deathMessageColor()
	{
		return null;
	}

	@ConfigItem(
			  keyName = "showEggsOnHopper",
			  name = "Display cannon eggs",
			  description = "Displays the amount of loaded eggs on cannon hoppers",
			  section = inGameSection,
			  position = 8
	)
	default boolean showEggsOnHopper()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "inventoryHighlightMode",
			  name = "Inventory highlight",
			  description = "Define the mode of all inventory highlights",
			  section = inGameSection,
			  position = 9
	)
	default InventoryHighlightMode inventoryHighlightMode()
	{
		return InventoryHighlightMode.DISABLED;
	}

	@ConfigItem(
			  keyName = "showGroundItemHighlights",
			  name = "Ground items highlight",
			  description = "Show ground item highlights",
			  section = inGameSection,
			  position = 10
	)
	default boolean showGroundItemHighlights()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "highlightGroundTiles",
			  name = "Ground tiles highlight",
			  description = "Configures whether or not to highlight tiles containing ground items",
			  section = inGameSection,
			  position = 11
	)
	default boolean highlightGroundTiles()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "callCorrection",
			  name = "Call correction",
			  description = "Write a chat message if your teammate corrects their call",
			  section = inGameSection,
			  position = 12
	)
	default boolean announceCallCorrection()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "highlightArrows",
			  name = "Highlight arrows",
			  description = "Highlights arrows called by your teammate",
			  position = 0,
			  section = attackerSection
	)
	default boolean highlightArrows()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			  keyName = "highlightArrowColor",
			  name = "Highlight arrow color",
			  description = "Configures the color to highlight the called arrows",
			  position = 1,
			  section = attackerSection
	)
	default Color highlightArrowColor()
	{
		return new Color(0, 255, 0, 50);
	}

	@ConfigItem(
			  keyName = "highlightAttackStyle",
			  name = "Highlight attack style",
			  description = "Highlights the attack style called by your teammate",
			  position = 2,
			  section = attackerSection
	)
	default boolean highlightAttackStyle()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "highlightAttackStyleColor",
			  name = "Highlight attack style color",
			  description = "Configures the color to highlight the attack style",
			  position = 3,
			  section = attackerSection
	)
	default Color highlightAttackStyleColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			  keyName = "showRunnerTickTimerAttacker",
			  name = "Show runner tick timer",
			  description = "Shows the current cycle tick of runners when performing the attacker role",
			  position = 4,
			  section = attackerSection
	)
	default boolean showRunnerTickTimerAttacker()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "highlightBait",
			  name = "Highlight called bait",
			  description = "Highlights bait called by your teammate",
			  position = 0,
			  section = defenderSection
	)
	default boolean highlightBait()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
			  keyName = "highlightBaitColor",
			  name = "Called bait color",
			  description = "Color to highlight the bait called by your teammate",
			  position = 1,
			  section = defenderSection
	)
	default Color highlightBaitColor()
	{
		return new Color(0, 255, 0, 50);
	}

	@ConfigItem(
			  keyName = "highlightGroundBait",
			  name = "Highlight ground bait",
			  description = "Highlight bait dropped on the ground",
			  position = 2,
			  section = defenderSection
	)
	default boolean highlightGroundBait()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "highlightGroundBaitColor",
			  name = "Ground bait color",
			  description = "Color to highlight the bait dropped on the ground",
			  position = 3,
			  section = defenderSection
	)
	default Color highlightGroundBaitColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
			  keyName = "highlightGroundLogsHammer",
			  name = "Highlight ground logs/hammer",
			  description = "Highlight logs and hammer on the ground",
			  position = 4,
			  section = defenderSection
	)
	default boolean highlightGroundLogsHammer()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "highlightGroundLogsHammerColor",
			  name = "Ground logs/hammer color",
			  description = "Color to highlight the logs and hammer on the ground",
			  position = 5,
			  section = defenderSection
	)
	default Color highlightGroundLogsHammerColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
			  keyName = "showRunnerTickTimerDefender",
			  name = "Show runner tick timer",
			  description = "Shows the current cycle tick of runners when performing the defender role",
			  position = 6,
			  section = defenderSection
	)
	default boolean showRunnerTickTimerDefender()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "highlightBrokenTraps",
			  name = "Highlight broken traps",
			  description = "Highlights broken traps when all penance runners are not dead",
			  position = 7,
			  section = defenderSection
	)
	default boolean highlightBrokenTraps()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "highlightBrokenTrapsColor",
			  name = "Broken traps color",
			  description = "Set color of the highlighted broken traps",
			  position = 8,
			  section = defenderSection
	)
	default Color highlightBrokenTrapsColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			  keyName = "highlightBrokenTrapsOpacity",
			  name = "Broken traps opacity",
			  description = "Set opacity of the highlighted broken traps",
			  position = 9,
			  section = defenderSection
	)
	default int highlightBrokenTrapsOpacity()
	{
		return 50;
	}

	@ConfigItem(
			  keyName = "highlightBrokenTrapsBorderWidth",
			  name = "Broken traps border",
			  description = "Set width of the highlighted broken traps",
			  position = 10,
			  section = defenderSection
	)
	default double highlightBrokenTrapsBorderWidth()
	{
		return 2;
	}

	@ConfigItem(
			  keyName = "showEggCountOverlay",
			  name = "Show number of eggs collected",
			  description = "Displays current number of eggs collected",
			  position = 0,
			  section = collectorSection
	)
	default boolean showEggCountOverlay()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "highlightGroundEggsMode",
			  name = "Highlight eggs",
			  description = "Highlight egg colors on the ground",
			  position = 1,
			  section = collectorSection
	)
	default GroundEggsMode highlightGroundEggsMode()
	{
		return GroundEggsMode.CALLED;
	}

	@ConfigItem(
			  keyName = "menuHighlightMode",
			  name = "Menu highlight mode",
			  description = "Configures what to highlight in right-click menu",
			  section = collectorSection,
			  position = 2
	)
	default MenuHighlightMode menuHighlightMode()
	{
		return MenuHighlightMode.NAME;
	}

	@ConfigItem(
			  keyName = "swapCollectionBag",
			  name = "Swap collection bag",
			  description = "Swap Look-in with Empty on the collection bag",
			  position = 3,
			  section = collectorSection
	)
	default boolean swapCollectionBag()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "swapCollectorHorn",
			  name = "Swap collector horn",
			  description = "Swap Use with Tell-defensive on the collector horn",
			  position = 4,
			  section = collectorSection
	)
	default boolean swapCollectorHorn()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "swapDestroyEggs",
			  name = "Swap collector eggs",
			  description = "Swap Use with Destroy on red/green/blue eggs",
			  position = 5,
			  section = collectorSection
	)
	default boolean swapDestroyEggs()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "showRunnerTickTimerCollector",
			  name = "Show runner tick timer",
			  description = "Shows the current cycle tick of runners when performing the collector role",
			  position = 6,
			  section = collectorSection
	)
	default boolean showRunnerTickTimerCollector()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "highlightPoison",
			  name = "Highlight called poison",
			  description = "Highlights poison food called by your teammate",
			  position = 0,
			  section = healerSection
	)
	default boolean highlightPoison()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			  keyName = "highlightPoisonColor",
			  name = "Called poison color",
			  description = "Configures the color to highlight the correct poison food",
			  position = 1,
			  section = healerSection
	)
	default Color highlightPoisonColor()
	{
		return new Color(0, 255, 0, 50);
	}

	@ConfigItem(
			  keyName = "highlightNotification",
			  name = "Highlight incorrect notification",
			  description = "Highlights incorrect poison chat notification",
			  position = 2,
			  section = healerSection
	)
	default boolean highlightNotification()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "highlightNotificationColor",
			  name = "Notification color",
			  description = "Configures the color to highlight the notification text",
			  position = 3,
			  section = healerSection
	)
	default Color highlightNotificationColor()
	{
		return new Color(228, 18, 31);
	}

	@ConfigItem(
			  keyName = "showHpCountOverlay",
			  name = "Show number of hitpoints healed",
			  description = "Displays current number of hitpoints healed",
			  position = 4,
			  section = healerSection
	)
	default boolean showHpCountOverlay()
	{
		return true;
	}

	@ConfigItem(
			  keyName = "showTeammateHealthBars",
			  name = "Show teammate health bars",
			  description = "Displays a health bar where a teammate's remaining health is located",
			  position = 5,
			  section = healerSection
	)
	default boolean showTeammateHealthBars()
	{
		return true;
	}

	@Range(max = 255)
	@ConfigItem(
			  keyName = "teammateHealthBarTransparency",
			  name = "Health bars transparency",
			  description = "Configures the amount of transparency on the teammate health bar",
			  position = 6,
			  section = healerSection
	)
	default int teammateHealthBarTransparency()
	{
		return 200;
	}

	@ConfigItem(
			  keyName = "hideHealerTeammatesHealth",
			  name = "Hide teammates health",
			  description = "Hides teammates health information",
			  position = 7,
			  section = healerSection
	)
	default boolean hideHealerTeammatesHealth()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "healerTeammatesHealthHotkey",
			  name = "Teammates health hotkey",
			  description = "Hotkey to show teammates health information when hidden",
			  position = 8,
			  section = healerSection
	)
	default Keybind healerTeammatesHealthHotkey()
	{
		return Keybind.CTRL;
	}

	@ConfigItem(
			  keyName = "showRunnerTickTimerHealer",
			  name = "Show runner tick timer",
			  description = "Shows the current cycle tick of runners when performing the healer role",
			  position = 9,
			  section = healerSection
	)
	default boolean showRunnerTickTimerHealer()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "swapHealerSpring",
			  name = "Swap healer spring",
			  description = "Swap Drink-from with Take-from on healer spring",
			  position = 10,
			  section = healerSection
	)
	default boolean swapHealerSpring()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "showDurationMode",
			  name = "Duration",
			  description = "Displays duration after each wave and/or round",
			  section = postGameSection,
			  position = 0
	)
	default DurationMode showDurationMode()
	{
		return DurationMode.WAVE_ROUND;
	}

	@ConfigItem(
			  keyName = "showRewardPointsMode",
			  name = "Reward points",
			  description = "Gives summary of reward points in the chat after each wave and/or round",
			  section = postGameSection,
			  position = 1
	)
	default PointsMode showRewardPointsMode()
	{
		return PointsMode.WAVE_ROUND;
	}

	@ConfigItem(
			  keyName = "showRewardsBreakdownMode",
			  name = "Rewards breakdown",
			  description = "Gives summary of advanced points breakdown in the chat after each wave and/or round",
			  section = postGameSection,
			  position = 2
	)
	default RewardsBreakdownMode showRewardsBreakdownMode()
	{
		return RewardsBreakdownMode.ROUND;
	}

	@ConfigItem(
			  keyName = "displayPointsLocationMode",
			  name = "Display at",
			  description = "Set when to display role points",
			  section = pointsSection,
			  position = 3
	)
	default DisplayPointsLocationMode displayPointsLocationMode()
	{
		return DisplayPointsLocationMode.OUTPOST;
	}

	@ConfigItem(
			  keyName = "displayPointsMode",
			  name = "Points mode",
			  description = "Set how to display role points",
			  section = pointsSection,
			  position = 4
	)
	default DisplayPointsMode displayPointsMode()
	{
		return DisplayPointsMode.TEXT_BOX;
	}

	@ConfigItem(
			  keyName = "pointsCounterMode",
			  name = "Points counter",
			  description = "Set which role points counter to display",
			  section = pointsSection,
			  position = 5
	)
	default PointsCounterMode pointsCounterMode()
	{
		return PointsCounterMode.CURRENT_POINTS;
	}

	@ConfigItem(
			  keyName = "rolePointsTrackingMode",
			  name = "Points tracking",
			  description = "Define how points will be tracked for rounds and sessions",
			  section = pointsSection,
			  position = 6
	)
	default RolePointsTrackingMode rolePointsTrackingMode()
	{
		return RolePointsTrackingMode.MINE;
	}

	@ConfigItem(
			  keyName = "resetPoints",
			  name = "Reset round and session points",
			  description = "Turn on/off to reset round and session points",
			  section = pointsSection,
			  position = 7
	)
	default boolean resetPoints()
	{
		return false;
	}

	@ConfigItem(
			  keyName = "sessionResetTime",
			  name = "Session reset time",
			  description = "Set how long to wait from the last wave completed for the session to reset",
			  section = pointsSection,
			  position = 8
	)
	@Units(Units.MINUTES)
	default int sessionResetTime()
	{
		return 10;
	}

	@ConfigItem(
			  keyName = "kandarinHardDiaryPointsBoost",
			  name = "Diary bonus",
			  description = "Include Kandarin hard diary +10% point bonus when displaying role points",
			  section = pointsSection,
			  position = 9
	)
	default KandarinDiaryBonusMode kandarinHardDiaryPointsBonus()
	{
		return KandarinDiaryBonusMode.IF_COMPLETED;
	}

	@ConfigItem(
			  keyName = "groundItemsPluginHighlightedList",
			  name = "Ground items highlighted list",
			  description = "Stores all the items automatically removed from the ground items plugin highlighted list",
			  hidden = true
	)
	default String getGroundItemsPluginHighlightedList()
	{
		return "";
	}

	@ConfigItem(
			  keyName = "groundItemsPluginHighlightedList",
			  name = "",
			  description = "",
			  hidden = true
	)
	void setGroundItemsPluginHighlightedList(String list);

	@ConfigItem(
			  keyName = "groundItemsPluginHiddenList",
			  name = "Ground Items Hidden List",
			  description = "Stores all the items automatically added to the ground items plugin hidden list",
			  hidden = true
	)
	default String getGroundItemsPluginHiddenList()
	{
		return "";
	}

	@ConfigItem(
			  keyName = "groundItemsPluginHiddenList",
			  name = "",
			  description = "",
			  hidden = true
	)
	void setGroundItemsPluginHiddenList(String list);

	@ConfigItem(
			  keyName = "barbarianAssaultConfigs",
			  name = "Barbarian Assault Configs",
			  description = "Stores all the configs previously set on the barbarian assault plugin",
			  hidden = true
	)
	default String getBarbarianAssaultConfigs()
	{
		return "";
	}

	@ConfigItem(
			  keyName = "barbarianAssaultConfigs",
			  name = "",
			  description = "",
			  hidden = true
	)
	void setBarbarianAssaultConfigs(String configs);
}
