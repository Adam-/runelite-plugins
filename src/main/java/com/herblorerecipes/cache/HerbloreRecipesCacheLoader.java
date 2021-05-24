package com.herblorerecipes.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.herblorerecipes.HerbloreRecipesConfig;
import com.herblorerecipes.model.Potion;
import static com.herblorerecipes.util.Utils.KEY_POTION_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_PRIMARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SECONDARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_UNF_IDENTIFIER;
import java.awt.Color;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.runelite.client.util.ColorUtil;

public class HerbloreRecipesCacheLoader extends CacheLoader<String, String>
{
	private static final Color GREY_COLOR = new Color(238, 238, 238);
	private static LoadingCache<String, String> tooltipTextCache;

	private final HerbloreRecipesConfig config;

	public HerbloreRecipesCacheLoader(HerbloreRecipesConfig config)
	{
		this.config = config;
		tooltipTextCache = CacheBuilder.newBuilder()
			.maximumSize(Potion.getPrimaryIngredients().size() + Potion.getSecondaryIngredients().size() + Potion.getUnfinishedPotions().size())
			.expireAfterAccess(30, TimeUnit.MINUTES)
			.build(this);
	}

	public static void clearCache()
	{
		tooltipTextCache.invalidateAll();
	}

	@Override
	public String load(String key) throws Exception
	{
		if (key.charAt(0) == KEY_PRIMARY_IDENTIFIER)
		{
			// Primary ingredients
			return Potion.getPotionsByPrimaryIngredient(key).stream()
				.map(potion -> makeTooltipText(potion, true))
				.collect(Collectors.joining("</br>", "</br>", "</br>"));
		}
		if (key.charAt(0) == KEY_SECONDARY_IDENTIFIER)
		{
			return Potion.getPotionsBySecondaryIngredient(key).stream()
				.map(potion -> makeTooltipText(potion, false))
				.collect(Collectors.joining("</br>", "</br>", "</br>"));
		}
		if (key.charAt(0) == KEY_UNF_IDENTIFIER)
		{
			return Potion.getPotionsByUnfinishedPotion(key).stream()
				.map(potion -> makeTooltipText(potion, true))
				.collect(Collectors.joining("</br>", "</br>", "</br>"));
		}
		if (key.charAt(0) == KEY_POTION_IDENTIFIER)
		{
			return Stream.of(Potion.getPotionByName(key))
				.map(this::makeTooltipForPotion)
				.collect(Collectors.joining("</br>", "</br>", "</br>"));
		}
		return null;
	}

	private String makeTooltipText(Potion potion, boolean isPrimaryOrUnfinished)
	{
		StringBuilder tooltipBuilder = new StringBuilder();

		tooltipBuilder.append(config.showLevelReqs() ?
			String.format("lvl %d: %s", potion.getLevel(), potion.getPotionName()) :
			potion.getPotionName());

		boolean hasMultipleSecondaries = potion.getSecondaryIngredient() != null && potion.getSecondaryIngredient().split(",").length > 1;

		if (config.showSecondaryIngredientsAlongsidePrimaries() && potion.getSecondaryIngredient() != null && isPrimaryOrUnfinished)
		{
			tooltipBuilder.append(String.format(" (2nd: %s)", potion.getSecondaryIngredient()));
		}

		if (config.showPrimaryIngredientsAlongsideSecondaries() && !isPrimaryOrUnfinished)
		{
			tooltipBuilder.append(String.format(" (1st: %s", potion.getPrimaryIngredient()));
			tooltipBuilder.append(hasMultipleSecondaries && config.showSecondaryIngredientsAlongsidePrimaries() ?
				String.format(", 2nd: %s)", potion.getSecondaryIngredient()) :
				")");
		}

		return ColorUtil.wrapWithColorTag(tooltipBuilder.toString(), GREY_COLOR);
	}

	private String makeTooltipForPotion(Potion potion)
	{
		return
			(config.showLevelReqs() ?
				String.format("lvl %d: %s", potion.getLevel(), potion.getPotionName()) :
				potion.getPotionName()) +
				String.format(" (1st: %s, 2nd: %s)", potion.getPrimaryIngredient(), potion.getSecondaryIngredient());
	}

	public String get(String item) throws ExecutionException
	{
		return tooltipTextCache.get(item);
	}
}
