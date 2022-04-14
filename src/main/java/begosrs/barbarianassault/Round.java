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

import begosrs.barbarianassault.points.RolePointsTrackingMode;
import begosrs.barbarianassault.timer.Timer;
import lombok.Getter;
import net.runelite.client.chat.ChatMessageBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Round
{
	static final int STARTING_WAVE = 1;
	public static final int ENDING_WAVE = 10;

	private static final String[] SUMMARY_DESCRIPTIONS = {
			  "Wrong att.",
			  "Runners passed",
			  "Eggs",
			  "Heal",
			  "Wrong pois.",
	};

	@Getter
	private final int startingWave;
	@Getter
	private final Timer timer;
	@Getter
	private final List<Wave> waves;

	// sum of all 4 role points
	private final int[] rolesPoints;

	// sum of amounts and points for each of the events happening during the game:
	// wrong attacks, runners passed, eggs collected, hp healed, wrong poisons used
	private final int[] summaryAmounts;
	private final int[] summaryPoints;

	Round(int startingWave, Timer timer)
	{
		this.startingWave = startingWave;
		this.timer = timer;
		this.waves = new ArrayList<>();
		this.rolesPoints = new int[4];
		this.summaryPoints = new int[5];
		this.summaryAmounts = new int[5];
	}

	void addWave(Wave wave, RolePointsTrackingMode trackingMode)
	{
		int[] waveRolesPoints = wave.getRolesPoints();
		Role[] playerRoles = wave.getPlayerRoles();
		Role myRole = wave.getRole();
		for (int i = 0; i < rolesPoints.length; i++)
		{
			int playerRoleIndex;
			switch (trackingMode)
			{
				case PLAYER_ONE:
					playerRoleIndex = 0;
					break;
				case PLAYER_TWO:
					playerRoleIndex = 1;
					break;
				case PLAYER_THREE:
					playerRoleIndex = 2;
					break;
				case PLAYER_FOUR:
					playerRoleIndex = 3;
					break;
				case PLAYER_FIVE:
					playerRoleIndex = 4;
					break;
				default:
					playerRoleIndex = -1;
					break;
			}
			boolean isPlayerTracking = playerRoleIndex >= 0
					  && playerRoles[playerRoleIndex] != null;
			if (trackingMode == RolePointsTrackingMode.ALL
					  || trackingMode == RolePointsTrackingMode.MINE && myRole != null && myRole.ordinal() == i
					  || trackingMode == RolePointsTrackingMode.ATTACKER && Role.ATTACKER.ordinal() == i
					  || trackingMode == RolePointsTrackingMode.DEFENDER && Role.DEFENDER.ordinal() == i
					  || trackingMode == RolePointsTrackingMode.HEALER && Role.HEALER.ordinal() == i
					  || trackingMode == RolePointsTrackingMode.COLLECTOR && Role.COLLECTOR.ordinal() == i
					  || isPlayerTracking && playerRoles[playerRoleIndex].ordinal() == i)
			{
				rolesPoints[i] += waveRolesPoints[i];
			}
			if (wave.getNumber() == Round.ENDING_WAVE)
			{
				if (trackingMode == RolePointsTrackingMode.MINE && myRole != null && myRole.ordinal() != i
						  || isPlayerTracking && playerRoles[playerRoleIndex].ordinal() != i)
				{
					rolesPoints[i] += 5;
				}
			}
		}
		int[] waveAmounts = wave.getAmounts();
		for (int i = 0; i < summaryAmounts.length; i++)
		{
			summaryAmounts[i] += waveAmounts[i];
		}
		int[] wavePoints = wave.getPoints();
		for (int i = 0; i < summaryPoints.length; i++)
		{
			summaryPoints[i] += wavePoints[i];
		}
		waves.add(wave);
	}

	public int getCollectedEggsCount(int currentWaveCollectedEggs)
	{
		return currentWaveCollectedEggs + waves.stream().map(Wave::getCollectedEggsCount).reduce(0, Integer::sum);
	}

	public int getHpHealed(int currentWaveHpHealed)
	{
		return currentWaveHpHealed + waves.stream().map(Wave::getHpHealed).reduce(0, Integer::sum);
	}

	int getNumberOfWaves()
	{
		return waves.size();
	}

	boolean isComplete()
	{
		return startingWave - 1 + getNumberOfWaves() == Round.ENDING_WAVE;
	}

	ChatMessageBuilder getRoundPointsMessage(boolean colorful, boolean bonus)
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

	ChatMessageBuilder getRoundSummaryMessage(boolean colorful)
	{
		ChatMessageBuilder message = new ChatMessageBuilder();
		for (int i = 0; i < SUMMARY_DESCRIPTIONS.length; i++)
		{
			if (i != 0)
			{
				message.append(" / ");
			}
			message.append(SUMMARY_DESCRIPTIONS[i]).append(": ").append(String.valueOf(summaryAmounts[i]));
			int pointsCount = summaryPoints[i];
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

	public int getRolePoints(Role role)
	{
		return rolesPoints[role.ordinal()];
	}

	public void resetRolePoints()
	{
		for (int i = 0; i < Role.values().length; i++)
		{
			rolesPoints[i] = 0;
		}
	}
}
