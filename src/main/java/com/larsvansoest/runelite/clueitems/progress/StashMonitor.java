package com.larsvansoest.runelite.clueitems.progress;

import lombok.RequiredArgsConstructor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cluescrolls.clues.emote.STASHUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@RequiredArgsConstructor
public class StashMonitor
{
	private static final String GROUP = "[EmoteClueItems] Stash status";
	private static final int[] STASH_UNITS = Arrays.stream(STASHUnit.values()).mapToInt(STASHUnit::getObjectId).sorted().toArray();

	private final ConfigManager config;

	public void setStashFilled(final String key, final STASHUnit stashUnit, final boolean filled)
	{
		final String stashes = this.config.getRSProfileConfiguration(GROUP, key);
		final StringBuilder stashesBuilder = stashes.length() == STASH_UNITS.length ? new StringBuilder(stashes) : new StringBuilder(StringUtils.repeat('0', STASH_UNITS.length));
		stashesBuilder.setCharAt(ArrayUtils.indexOf(STASH_UNITS, stashUnit.getObjectId()), filled ? '1' : '0');
		this.config.setRSProfileConfiguration(GROUP, key, stashesBuilder.toString());
	}

	public boolean getStashFilled(final String key, final STASHUnit stashUnit)
	{
		final String stashes = this.config.getRSProfileConfiguration(GROUP, key);
		return stashes.length() == STASH_UNITS.length && stashes.charAt(ArrayUtils.indexOf(STASH_UNITS, stashUnit.getObjectId())) == '1';
	}
}