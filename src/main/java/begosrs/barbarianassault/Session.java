/*
 * Copyright (c) 2022, BegOsrs <https://github.com/begosrs>
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
import lombok.Getter;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Session
{
	@Getter
	private final List<Wave> waves;
	@Getter
	private final Map<Role, Integer> rolePoints;

	public Session()
	{
		this.waves = new LinkedList<>();
		this.rolePoints = new EnumMap<>(Role.class);
	}

	public void addWave(Wave wave, RolePointsTrackingMode trackingMode)
	{
		int[] points = wave.getRolesPoints();
		Role[] playerRoles = wave.getPlayerRoles();
		Role myRole = wave.getRole();
		for (Role role : Role.values())
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
					  || trackingMode == RolePointsTrackingMode.MINE && myRole == role
					  || trackingMode == RolePointsTrackingMode.ATTACKER && Role.ATTACKER == role
					  || trackingMode == RolePointsTrackingMode.DEFENDER && Role.DEFENDER == role
					  || trackingMode == RolePointsTrackingMode.HEALER && Role.HEALER == role
					  || trackingMode == RolePointsTrackingMode.COLLECTOR && Role.COLLECTOR == role
					  || isPlayerTracking && playerRoles[playerRoleIndex] == role)
			{
				rolePoints.merge(role, points[role.ordinal()], Integer::sum);
			}
			if (wave.getNumber() == Round.ENDING_WAVE)
			{
				if (trackingMode == RolePointsTrackingMode.MINE && role != myRole
						  || isPlayerTracking && playerRoles[playerRoleIndex] != role)
				{
					rolePoints.merge(role, 5, Integer::sum);
				}
			}

		}
		waves.add(wave);
	}

	public void reset()
	{
		waves.clear();
		rolePoints.clear();
	}

	public int getRolePoints(Role role)
	{
		return rolePoints.getOrDefault(role, 0);
	}

	public void resetRolePoints()
	{
		for (Role role : Role.values())
		{
			rolePoints.put(role, 0);
		}
	}
}
