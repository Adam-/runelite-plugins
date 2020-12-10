package com.larsvansoest.runelite.clueitems.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;

public abstract class Util
{
	public static HashSet<Integer> toHashSet(int[] ids)
	{
		return Arrays.stream(ArrayUtils.toObject(ids)).collect(Collectors.toCollection(HashSet::new));
	}
}
