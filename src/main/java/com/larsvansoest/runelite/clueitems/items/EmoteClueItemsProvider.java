package com.larsvansoest.runelite.clueitems.items;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class EmoteClueItemsProvider
{
    private final HashSet<Integer> beginnerItems;
    private final HashSet<Integer> easyItems;
    private final HashSet<Integer> mediumItems;
    private final HashSet<Integer> hardItems;
    private final HashSet<Integer> eliteItems;
    private final HashSet<Integer> masterItems;

    public EmoteClueItemsProvider()
    {
        this.beginnerItems = this.toHashSet(EmoteClueItems.Beginner.ids);
        this.easyItems = this.toHashSet(EmoteClueItems.Easy.ids);
        this.mediumItems = this.toHashSet(EmoteClueItems.Medium.ids);
        this.hardItems = this.toHashSet(EmoteClueItems.Hard.ids);
        this.eliteItems = this.toHashSet(EmoteClueItems.Elite.ids);
        this.masterItems = this.toHashSet(EmoteClueItems.Master.ids);
    }

    private HashSet<Integer> toHashSet(int[] ids)
    {
        return Arrays.stream(ArrayUtils.toObject(ids)).collect(Collectors.toCollection(HashSet::new));
    }

    public HashSet<Integer> getBeginnerItems() {
        return beginnerItems;
    }

    public HashSet<Integer> getEasyItems() {
        return easyItems;
    }

    public HashSet<Integer> getMediumItems() {
        return mediumItems;
    }

    public HashSet<Integer> getHardItems() {
        return hardItems;
    }

    public HashSet<Integer> getEliteItems() {
        return eliteItems;
    }

    public HashSet<Integer> getMasterItems() {
        return masterItems;
    }
}
