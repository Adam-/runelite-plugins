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
package begosrs.barbarianassault.points;

import begosrs.barbarianassault.BaMinigameConfig;
import begosrs.barbarianassault.BaMinigamePlugin;
import begosrs.barbarianassault.Role;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;


public class RolePointsOverlay extends OverlayPanel
{
	private final BaMinigameConfig config;
	private final BaMinigamePlugin plugin;

	@Inject
	private RolePointsOverlay(BaMinigameConfig config, BaMinigamePlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.MED);
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ba role points overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.displayPointsMode() != DisplayPointsMode.TEXT_BOX || !plugin.shouldDisplayRolePoints())
		{
			return null;
		}

		RolePointsTrackingMode rolePointsTrackingMode = config.rolePointsTrackingMode();
		for (Map.Entry<Role, Integer> rolePoints : plugin.getRolePoints().entrySet())
		{
			final Role role = rolePoints.getKey();
			if (rolePointsTrackingMode == RolePointsTrackingMode.ATTACKER && role != Role.ATTACKER
					  || rolePointsTrackingMode == RolePointsTrackingMode.DEFENDER && role != Role.DEFENDER
					  || rolePointsTrackingMode == RolePointsTrackingMode.HEALER && role != Role.HEALER
					  || rolePointsTrackingMode == RolePointsTrackingMode.COLLECTOR && role != Role.COLLECTOR)
			{
				continue;
			}
			final Integer points = rolePoints.getValue();
			addPanelComponentPoints(role, points);
		}

		return super.render(graphics);
	}

	private void addPanelComponentPoints(Role role, Integer points)
	{
		final String pts = points == null ? "-" : Integer.toString(points);
		final Color strColor = role.getColor();
		final String str = ColorUtil.prependColorTag(pts, strColor);

		panelComponent.getChildren().add(LineComponent.builder()
				  .left(role.getName())
				  .right(str)
				  .rightColor(strColor)
				  .build());
	}

}
