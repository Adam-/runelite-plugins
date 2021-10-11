/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, Lars van Soest
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.larsvansoest.runelite.clueitems.data;

import org.apache.commons.lang3.ArrayUtils;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides static predicate mappings over {@link EmoteClueItem} data set.
 * <p>
 * Use-case examples are requirement progression inference by {@link com.larsvansoest.runelite.clueitems.progress.ProgressManager} class, nested requirement visualisation by {@link com.larsvansoest.runelite.clueitems.ui.EmoteClueItemsPanel}.
 *
 * @author Lars van Soest
 * @since 2.0.0
 */
public abstract class EmoteClueAssociations
{
	/**
	 * Maps {@link net.runelite.api.ItemID} to corresponding {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem}.
	 * <p>
	 * Only contains items used for emote clues.
	 */
	public static final Map<Integer, EmoteClueItem> ItemIdToEmoteClueItem = Arrays
			.stream(EmoteClueItem.values())
			.filter(emoteClueItem -> emoteClueItem.getItemId() != null)
			.collect(Collectors.toMap(EmoteClueItem::getItemId, Function.identity()));

	/**
	 * Maps {@link com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty} to all {@link com.larsvansoest.runelite.clueitems.data.EmoteClue} of that difficulty.
	 */
	public static final Map<EmoteClueDifficulty, EmoteClue[]> DifficultyToEmoteClues = EmoteClue.CLUES
			.stream()
			.map(emoteClue -> new AbstractMap.SimpleImmutableEntry<>(emoteClue, emoteClue.getEmoteClueDifficulty()))
			.collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getValue, entry -> new EmoteClue[]{entry.getKey()}, ArrayUtils::addAll));

	/**
	 * Maps {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} parents to all {@link com.larsvansoest.runelite.clueitems.data.EmoteClue} that use it.
	 * <p>
	 * Map does not contain {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem}'s children which are indirectly related to the {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem}.
	 */
	public static final Map<EmoteClueItem, EmoteClue[]> EmoteClueItemParentToEmoteClues = EmoteClue.CLUES
			.stream()
			.flatMap(emoteClue -> Arrays
					.stream(emoteClue.getItemRequirements())
					.filter(itemRequirement -> (itemRequirement instanceof EmoteClueItem))
					.map(EmoteClueItem.class::cast)
					.map(emoteClueItem -> new AbstractMap.SimpleImmutableEntry<>(emoteClue, emoteClueItem)))
			.collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getValue, entry -> new EmoteClue[]{entry.getKey()}, ArrayUtils::addAll));

	/**
	 * Maps {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} to all {@link com.larsvansoest.runelite.clueitems.data.EmoteClue} that use it.
	 * <p>
	 * Map does not contain {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem}'s children which are indirectly related to the {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem}.
	 */
	public static final Map<EmoteClueItem, EmoteClue[]> EmoteClueItemToEmoteClues = EmoteClueItemParentToEmoteClues
			.entrySet()
			.stream()
			.flatMap(EmoteClueAssociations::flatMapEmoteClueItemParentToEmoteClues)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, ArrayUtils::addAll));
	/**
	 * Maps {@link com.larsvansoest.runelite.clueitems.data.EmoteClue} to all {@link com.larsvansoest.runelite.clueitems.data.EmoteClueItem} that it uses.
	 */
	public static final Map<EmoteClue, EmoteClueItem[]> EmoteClueToEmoteClueItems = EmoteClueItemToEmoteClues
			.entrySet()
			.stream()
			.flatMap(entry -> Arrays.stream(entry.getValue()).map(emoteClue -> new AbstractMap.SimpleImmutableEntry<EmoteClue, EmoteClueItem[]>(emoteClue, new EmoteClueItem[]{entry.getKey()})))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, ArrayUtils::addAll));

	/**
	 * Maps {@link com.larsvansoest.runelite.clueitems.data.StashUnit} to all {@link com.larsvansoest.runelite.clueitems.data.EmoteClue} that use it.
	 */
	public static final Map<StashUnit, EmoteClue[]> STASHUnitToEmoteClues = EmoteClue.CLUES
			.stream()
			.collect(Collectors.toMap(EmoteClue::getStashUnit, emoteClue -> new EmoteClue[]{emoteClue}, ArrayUtils::addAll));

	private static Stream<Map.Entry<EmoteClueItem, EmoteClue[]>> flatMapEmoteClueItemParentToEmoteClues(final Map.Entry<EmoteClueItem, EmoteClue[]> entry)
	{
		return Stream.concat(Stream.of(entry),
				entry.getKey().getChildren().stream().flatMap(child -> flatMapEmoteClueItemParentToEmoteClues(new AbstractMap.SimpleImmutableEntry<>(child, entry.getValue())))
		);
	}
}