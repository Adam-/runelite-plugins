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
package begosrs.barbarianassault.waveinfo;

import begosrs.barbarianassault.BaMinigameConfig;
import begosrs.barbarianassault.BaMinigamePlugin;
import begosrs.barbarianassault.Role;
import begosrs.barbarianassault.Round;
import begosrs.barbarianassault.Wave;
import begosrs.barbarianassault.timer.TimeUnits;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.Graphics2D;

@Slf4j
@Singleton
public class WaveInfoOverlay extends Overlay
{
	private static final int WAVE_CHECKMARK_ORIGINAL_X = 6;
	private static final int WAVE_ICON_ORIGINAL_X = 3;

	private final Client client;
	private final BaMinigamePlugin plugin;
	private final BaMinigameConfig config;

	@Inject
	private WaveInfoOverlay(Client client, BaMinigamePlugin plugin, BaMinigameConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Wave wave = plugin.getWave();
		final Round round = plugin.getRound();
		if (wave == null || round == null)
		{
			return null;
		}
		final Role role = wave.getRole();
		if (role == null)
		{
			return null;
		}

		if (config.showWaveTimer())
		{
			final Widget waveText = client.getWidget(role.getWaveText().getGroupId(), role.getWaveText().getChildId());
			if (waveText != null)
			{
				final TimeUnits units = config.timeUnits();
				final String waveTime = String.format(units.getFormatString(), wave.getTimeElapsed(true, units));
				waveText.setText("Wave " + wave.getNumber() + " / " + waveTime);
			}
		}

		if (config.showWaveCompleted())
		{
			// icon is replaced back on call changes, so must be constantly set here
			final Widget waveSprite = client.getWidget(role.getWaveSprite().getGroupId(), role.getWaveSprite().getChildId());
			if (waveSprite != null)
			{
				waveSprite.setSpriteId(wave.isComplete() ? SpriteID.OPTIONS_ROUND_CHECK_BOX_CHECKED : SpriteID.OPTIONS_ROUND_CHECK_BOX);
				waveSprite.setOriginalWidth(BaMinigamePlugin.WAVE_CHECKMARK_ICON_WIDTH);
				waveSprite.setOriginalX(WAVE_CHECKMARK_ORIGINAL_X);
			}
		}

		final Widget waveInfo = client.getWidget(role.getWaveInfo().getGroupId(), role.getWaveInfo().getChildId());

		if (waveInfo != null)
		{
			final Widget roleCounter = waveInfo.getChild(0);
			if (roleCounter != null)
			{
				String counter = null;
				if (config.showEggCountOverlay() && role == Role.COLLECTOR)
				{
					final int currentWave = wave.getNumber();
					final int waveCollectedEggs = currentWave == 10 ? 0 : wave.getCollectedEggsCount();
					final int roundCollectedEggs = round.getCollectedEggsCount(waveCollectedEggs);
					if (wave.getNumber() != 10)
					{
						counter = waveCollectedEggs + " / " + roundCollectedEggs;
					}
					else
					{
						counter = String.valueOf(roundCollectedEggs);
					}
				}
				else if (config.showHpCountOverlay() && role == Role.HEALER)
				{
					final int currentWave = wave.getNumber();
					final int waveHpHealed = currentWave == 10 ? 0 : wave.getHpHealed();
					final int roundHpHealed = round.getHpHealed(waveHpHealed);
					if (wave.getNumber() != 10)
					{
						counter = waveHpHealed + " / " + roundHpHealed;
					}
					else
					{
						counter = String.valueOf(roundHpHealed);
					}
				}

				if (counter != null)
				{
					roleCounter.setText(counter);
				}
			}
		}

		if (config.showCallChangeTimer() && waveInfo != null)
		{
			final Widget waveTimer = waveInfo.getChild(2);
			if (waveTimer != null)
			{
				waveTimer.setText(String.format("00:%02d", wave.getTimeUntilCallChange()));
			}
		}

		return null;
	}
}
