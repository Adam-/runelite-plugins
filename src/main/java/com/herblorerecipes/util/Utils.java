package com.herblorerecipes.util;

public class Utils
{
	// These identifiers are only required to differentiate between ingredients that can be primaries, secondaries, unfinished potions...
	// It's not required for seeds, really, since seeds will never be considered primary or secondary.
	// But seeds are gonna have their own identifier anyway :)
	public static final char KEY_PRIMARY_IDENTIFIER = '1';
	public static final char KEY_SECONDARY_IDENTIFIER = '2';
	public static final char KEY_UNF_IDENTIFIER = '3';
	public static final char KEY_POTION_IDENTIFIER = '4';
	public static final char KEY_SEED_IDENTIFIER = '5';
}
