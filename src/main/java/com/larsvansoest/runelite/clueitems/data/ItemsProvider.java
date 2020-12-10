package com.larsvansoest.runelite.clueitems.data;

import com.larsvansoest.runelite.clueitems.support.Util;

import java.util.HashSet;

public class ItemsProvider
{
	private HashSet<Integer> beginnerItems;
	private HashSet<Integer> easyItems;
	private HashSet<Integer> mediumItems;
	private HashSet<Integer> hardItems;
	private HashSet<Integer> eliteItems;
	private HashSet<Integer> masterItems;

	public void loadItems()
	{
		this.beginnerItems = this.toHashSet(Items.Beginner.ids);
		this.easyItems = this.toHashSet(Items.Easy.ids);
		this.mediumItems = this.toHashSet(Items.Medium.ids);
		this.hardItems = this.toHashSet(Items.Hard.ids);
		this.eliteItems = this.toHashSet(Items.Elite.ids);
		this.masterItems = this.toHashSet(Items.Master.ids);
	}

	private HashSet<Integer> toHashSet(int[] ids)
	{
		return Util.toHashSet(ids);
	}

	public HashSet<Integer> getBeginnerItems()
	{
		return beginnerItems;
	}

	public HashSet<Integer> getEasyItems()
	{
		return easyItems;
	}

	public HashSet<Integer> getMediumItems()
	{
		return mediumItems;
	}

	public HashSet<Integer> getHardItems()
	{
		return hardItems;
	}

	public HashSet<Integer> getEliteItems()
	{
		return eliteItems;
	}

	public HashSet<Integer> getMasterItems()
	{
		return masterItems;
	}
}
