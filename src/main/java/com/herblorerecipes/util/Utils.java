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
	public static final String SHOW_TOOLTIP_ON_POTIONS = "showPotionRecipes";
	public static final String SHOW_TOOLTIP_ON_PRIMARIES = "showPrimaryIngredients";
	public static final String SHOW_TOOLTIP_ON_SECONDARIES = "showSecondaryIngredients";
	public static final String SHOW_TOOLTIP_ON_UNFINISHED = "showUnfinishedPotions";
	public static final String SHOW_TOOLTIP_ON_SEEDS = "showTooltipOnSeeds";
	public static final String SHOW_PRIMARIES_IN_TOOLTIP = "showPrimaryIngredientsAlongsidePrimaries";
	public static final String SHOW_SECONDARIES_IN_TOOLTIP = "showSecondaryIngredientsAlongsidePrimaries";
	public static final String SHOW_LEVEL_REQS_IN_TOOLTIP = "showLevelReqs";
}
