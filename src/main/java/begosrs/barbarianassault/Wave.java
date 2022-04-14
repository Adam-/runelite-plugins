/*
 * Copyright (c) 2018, Cameron <https://github.com/noremac201>
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

import begosrs.barbarianassault.api.widgets.BaWidgetInfo;
import begosrs.barbarianassault.timer.TimeUnits;
import begosrs.barbarianassault.timer.Timer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.widgets.Widget;
import net.runelite.client.chat.ChatMessageBuilder;

import javax.annotation.Nullable;
import java.awt.Color;

@Slf4j
public class Wave
{
	static final int AMOUNT_EGGS_COLLECTED_INDEX = 2;
	static final int AMOUNT_HP_REPLENISHED_INDEX = 3;
	private static final String[] SUMMARY_DESCRIPTIONS = {
			  "Wrong att.",
			  "Runners passed",
			  "Eggs",
			  "Heal",
			  "Wrong pois.",
	};
	private static final BaWidgetInfo[] AMOUNTS_WIDGETS = {
			  BaWidgetInfo.BA_FAILED_ATTACKS,
			  BaWidgetInfo.BA_RUNNERS_PASSED,
			  BaWidgetInfo.BA_EGGS_COLLECTED,
			  BaWidgetInfo.BA_HITPOINTS_REPLENISHED,
			  BaWidgetInfo.BA_WRONG_POISON_PACKS,
	};
	private static final int FAILED_ATTACKS_INDEX = 0;
	private static final BaWidgetInfo[] ATTACKER_POINTS_WIDGETS = {
			  BaWidgetInfo.BA_FAILED_ATTACKS_POINTS,
			  BaWidgetInfo.BA_RANGERS_KILLED,
			  BaWidgetInfo.BA_FIGHTERS_KILLED,
	};
	private static final int RUNNERS_PASSED_INDEX = 0;
	private static final BaWidgetInfo[] DEFENDER_POINTS_WIDGETS = {
			  BaWidgetInfo.BA_RUNNERS_PASSED_POINTS,
			  BaWidgetInfo.BA_RUNNERS_KILLED,
	};
	private static final int EGGS_COLLECTED_INDEX = 0;
	private static final BaWidgetInfo[] COLLECTOR_POINTS_WIDGETS = {
			  BaWidgetInfo.BA_EGGS_COLLECTED_POINTS,
	};
	private static final int HITPOINTS_REPLENISHED_INDEX = 0;
	private static final int WRONG_POISON_PACKS_INDEX = 1;
	private static final BaWidgetInfo[] HEALER_POINTS_WIDGETS = {
			  BaWidgetInfo.BA_HITPOINTS_REPLENISHED_POINTS,
			  BaWidgetInfo.BA_WRONG_POISON_PACKS_POINTS,
			  BaWidgetInfo.BA_HEALERS_KILLED,
	};
	private static final int MAXIMUM_COLLECTED_EGGS = 60;
	private static final int MAXIMUM_HP_HEALED = 504;
	private final Client client;

	@Getter
	private final int number;
	@Nullable
	@Getter
	private final Timer timer;
	@Getter
	private final int[] rolesPoints;
	@Getter
	private final int[] amounts;
	@Getter
	private final int[] points;
	@Getter
	private final Role[] playerRoles;
	@Nullable
	@Getter
	@Setter
	private Role role;
	@Getter
	@Setter
	private int wrongEggsCount;
	@Getter
	@Setter
	private int eggsCount;
	@Setter
	private int hpHealed;
	@Setter
	private boolean fightersKilled, rangersKilled, healersKilled, runnersKilled;
	private boolean ended;

	Wave(Client client, int number)
	{
		this(client, number, new Role[5], null);
	}

	Wave(Client client, int number, Role[] playerRoles, Timer timer)
	{
		this.client = client;
		this.number = number;
		this.timer = timer;
		this.rolesPoints = new int[4];
		this.amounts = new int[5];
		this.points = new int[5];
		this.playerRoles = playerRoles;
	}

	public int getTimeUntilCallChange()
	{
		return 30 - getSecondsElapsed(true, TimeUnits.SECONDS) % 30;
	}

	public float getTimeElapsed(boolean delayTicks, TimeUnits units)
	{
		return getSecondsElapsed(delayTicks, units) + getMillisOfSecondElapsed(delayTicks, units) / 1000f;
	}

	public int getCollectedEggsCount()
	{
		if (ended)
		{
			return Math.max(0, amounts[AMOUNT_EGGS_COLLECTED_INDEX]);
		}
		// limit to 60, based off formula: y=x/4.35, X is capped at 60
		// https://oldschool.runescape.wiki/w/Barbarian_Assault/Rewards#Points_calculation
		return Math.min(eggsCount - wrongEggsCount, MAXIMUM_COLLECTED_EGGS);
	}

	public int getHpHealed()
	{
		if (ended)
		{
			return Math.max(0, amounts[AMOUNT_HP_REPLENISHED_INDEX]);
		}
		// upper bound 504, based off formula: y=x/18, Y is capped at 28
		// https://oldschool.runescape.wiki/w/Barbarian_Assault/Rewards#Points_calculation
		return Math.min(hpHealed, MAXIMUM_HP_HEALED);
	}

	void setAmounts()
	{
		for (int i = 0; i < AMOUNTS_WIDGETS.length; i++)
		{
			final int amountsCount = getBaWidgetValue(AMOUNTS_WIDGETS[i]);
			amounts[i] = amountsCount;
		}
	}

	void setPoints(boolean roundEnded)
	{
		if (roundEnded)
		{
			for (int i = 0; i < rolesPoints.length; i++)
			{
				rolesPoints[i] += 80;
			}
		}
		else
		{
			points[0] = getBaWidgetValue(ATTACKER_POINTS_WIDGETS[FAILED_ATTACKS_INDEX]);
			points[1] = getBaWidgetValue(DEFENDER_POINTS_WIDGETS[RUNNERS_PASSED_INDEX]);
			points[2] = getBaWidgetValue(COLLECTOR_POINTS_WIDGETS[EGGS_COLLECTED_INDEX]);
			points[3] = getBaWidgetValue(HEALER_POINTS_WIDGETS[HITPOINTS_REPLENISHED_INDEX]);
			points[4] = getBaWidgetValue(HEALER_POINTS_WIDGETS[WRONG_POISON_PACKS_INDEX]);

			final int basePoints = getBaWidgetValue(BaWidgetInfo.BA_BASE_POINTS);
			for (Role role : Role.values())
			{
				rolesPoints[role.ordinal()] += Math.max(0, basePoints);
			}
			for (BaWidgetInfo baWidgetInfo : ATTACKER_POINTS_WIDGETS)
			{
				final int points = getBaWidgetValue(baWidgetInfo);
				rolesPoints[Role.ATTACKER.ordinal()] += Math.max(0, points);
			}
			for (BaWidgetInfo baWidgetInfo : DEFENDER_POINTS_WIDGETS)
			{
				final int points = getBaWidgetValue(baWidgetInfo);
				rolesPoints[Role.DEFENDER.ordinal()] += Math.max(0, points);
			}
			for (BaWidgetInfo baWidgetInfo : COLLECTOR_POINTS_WIDGETS)
			{
				final int points = getBaWidgetValue(baWidgetInfo);
				rolesPoints[Role.COLLECTOR.ordinal()] += Math.max(0, points);
			}
			for (BaWidgetInfo baWidgetInfo : HEALER_POINTS_WIDGETS)
			{
				final int points = getBaWidgetValue(baWidgetInfo);
				rolesPoints[Role.HEALER.ordinal()] += Math.max(0, points);
			}
		}

		ended = true;
	}

	ChatMessageBuilder getWavePoints(boolean colorful, boolean bonus)
	{
		ChatMessageBuilder message = new ChatMessageBuilder();
		for (int i = 0; i < Role.values().length; i++)
		{
			if (i != 0)
			{
				message.append(" / ");
			}
			Role role = Role.values()[i];
			String roleName = role.getName();
			Color roleColor = role.getColor();
			int pts = Math.max(0, rolesPoints[i]);
			if (bonus)
			{
				pts *= 1.1;
			}
			String points = String.valueOf(pts);
			message.append(roleName + ": ");
			if (colorful)
			{
				message.append(roleColor, points);
			}
			else
			{
				message.append(points);
			}
		}
		return message;
	}

	ChatMessageBuilder getWaveSummary(boolean colorful)
	{
		ChatMessageBuilder message = new ChatMessageBuilder();
		for (int i = 0; i < SUMMARY_DESCRIPTIONS.length; i++)
		{
			if (i != 0)
			{
				message.append(" / ");
			}

			message.append(SUMMARY_DESCRIPTIONS[i]).append(": ").append(String.valueOf(amounts[i]));

			int pointsCount = points[i];
			if (pointsCount != 0)
			{
				message.append(" (");
				if (colorful)
				{
					Color color = pointsCount < 0 ? BaMinigamePlugin.RED : BaMinigamePlugin.DARK_GREEN;
					message.append(color, (pointsCount > 0 ? "+" : "") + pointsCount);
				}
				else
				{
					message.append((pointsCount > 0 ? "+" : "") + pointsCount);
				}
				message.append(")");
			}
		}
		return message;
	}

	private int getSecondsElapsed(boolean delayTicks, TimeUnits units)
	{
		if (units == TimeUnits.TICKS)
		{
			return getTicksInMillis(delayTicks) / 1000;
		}
		return timer == null ? 0 : (int) timer.getWaveTime().getSeconds();
	}

	private int getMillisOfSecondElapsed(boolean delayTicks, TimeUnits units)
	{
		if (units == TimeUnits.TICKS)
		{
			return getTicksInMillis(delayTicks) % 1000;
		}
		return timer == null ? 0 : timer.getWaveTime().getNano() / 1_000_000;
	}

	private int getTicksInMillis(boolean delayed)
	{
		return timer == null ? 0 : timer.getCurrentWaveTick(delayed) * Constants.GAME_TICK_LENGTH;
	}

	private int getBaWidgetValue(BaWidgetInfo baWidgetInfo)
	{
		final Widget widget = client.getWidget(baWidgetInfo.getGroupId(), baWidgetInfo.getChildId());
		return widget == null ? 0 : Integer.parseInt(widget.getText());
	}

	public boolean isComplete()
	{
		return (role == Role.ATTACKER || role == Role.COLLECTOR) && fightersKilled && rangersKilled
				  || role == Role.DEFENDER && runnersKilled
				  || role == Role.HEALER && healersKilled;
	}
}
