package com.larsvansoest.runelite.clueitems.progress;

import com.larsvansoest.runelite.clueitems.data.StashUnit;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.ConfigManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides utility to write and read stash fill status to Runelite's {@link net.runelite.client.config.ConfigManager}.
 *
 * @author Lars van Soest
 * @since 3.0.0
 */
@RequiredArgsConstructor
class StashMonitor
{
	private static final int[] STASH_IDS_ORDERED = Arrays.stream(StashUnit.values()).mapToInt(stashUnit -> stashUnit.getStashUnit().getObjectId()).sorted().toArray();
	private static final String STASH_IDS_ORDERED_FINGERPRINT = Arrays.stream(STASH_IDS_ORDERED).mapToObj(String::valueOf).collect(Collectors.joining(","));
	private static final String FINGERPRINT_KEY = "_fingerprint";

	private final String group;
	private final String key;
	private final ConfigManager config;

	public void setStashFilled(final StashUnit stashUnit, final boolean filled)
	{
		final StringBuilder stashesBuilder = new StringBuilder(this.config.getRSProfileConfiguration(this.group, this.key));
		stashesBuilder.setCharAt(ArrayUtils.indexOf(STASH_IDS_ORDERED, stashUnit.getStashUnit().getObjectId()), filled ? '1' : '0');
		this.config.setRSProfileConfiguration(this.group, this.key, stashesBuilder.toString());
	}

	public boolean getStashFilled(final StashUnit stashUnit)
	{
		final String stashes = this.config.getRSProfileConfiguration(this.group, this.key);
		return stashes.charAt(ArrayUtils.indexOf(STASH_IDS_ORDERED, stashUnit.getStashUnit().getObjectId())) == '1';
	}

	public void validate()
	{
		final String stashes = this.config.getRSProfileConfiguration(this.group, this.key);
		final String fingerPrint = this.config.getRSProfileConfiguration(this.group, FINGERPRINT_KEY);
		if (Objects.isNull(stashes) || Objects.isNull(fingerPrint) || !fingerPrint.equals(STASH_IDS_ORDERED_FINGERPRINT) || stashes.length() != STASH_IDS_ORDERED.length)
		{
			this.config.setRSProfileConfiguration(this.group, this.key, StringUtils.repeat('0', STASH_IDS_ORDERED.length));
			this.config.setRSProfileConfiguration(this.group, FINGERPRINT_KEY, STASH_IDS_ORDERED_FINGERPRINT);
		}
	}
}