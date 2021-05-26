package com.herblorerecipes.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.herblorerecipes.HerbloreRecipesConfig;
import com.herblorerecipes.model.Potion;
import static com.herblorerecipes.util.Utils.KEY_POTION_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_PRIMARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SECONDARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SEED_IDENTIFIER;
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
			.maximumSize(Potion.getPrimaries().size() + Potion.getSecondariesSet().size() + Potion.getUnfinishedPotions().size())
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
		switch (key.charAt(0))
		{
			case KEY_PRIMARY_IDENTIFIER:
				return Potion.getPotionsByPrimaryIngredient(key).stream()
					.map(this::makeTooltipWithoutPrimaries)
					.collect(Collectors.joining("</br>", "</br>", "</br>"));
			case KEY_SECONDARY_IDENTIFIER:
				return Potion.getPotionsBySecondaryIngredient(key).stream()
					.map(this::makeBasicTooltip)
					.collect(Collectors.joining("</br>", "</br>", "</br>"));
			case KEY_UNF_IDENTIFIER:
				return Potion.getPotionsByUnfinishedPotion(key).stream()
					.map(this::makeTooltipWithoutPrimaries)
					.collect(Collectors.joining("</br>", "</br>", "</br>"));
			case KEY_POTION_IDENTIFIER:
				return Stream.of(Potion.getPotionByName(key))
					.map(this::makeBasicTooltip)
					.collect(Collectors.joining("</br>", "</br>", "</br>"));
			case KEY_SEED_IDENTIFIER:
				return Potion.getPotionsBySeed(key).stream()
					.map(this::makeBasicTooltip)
					.collect(Collectors.joining("</br>", "</br>", "</br>"));
		}
		return null;
	}

	private String makeTooltipWithoutPrimaries(Potion potion)
	{
		String tooltipLine = (config.showLevelReqsInTooltip() ?
			String.format("lvl %d: %s", potion.getLevel(), potion.getPotionName()) :
			potion.getPotionName()) +
			(config.showSecondariesInTooltip() ? String.format(" (2nd: %s)", potion.getSecondaries()) : "");

		return ColorUtil.wrapWithColorTag(tooltipLine, GREY_COLOR);
	}

	private String makeBasicTooltip(Potion potion)
	{
		StringBuilder tooltipLine = new StringBuilder(config.showLevelReqsInTooltip() ?
			String.format("lvl %d: %s", potion.getLevel(), potion.getPotionName()) :
			potion.getPotionName());

		String primaryIngredientText = config.showPrimariesInTooltip() ? String.format("1st: %s", potion.getPrimary()) : "";
		String secondaryIngredientText = config.showSecondariesInTooltip() ? String.format("2nd: %s", potion.getSecondaries()) : ")";

		if (config.showPrimariesInTooltip() && config.showSecondariesInTooltip())
		{
			return tooltipLine.append(String.format(" (%s, %s)", primaryIngredientText, secondaryIngredientText)).toString();
		}
		else if (config.showPrimariesInTooltip() && !config.showSecondariesInTooltip())
		{
			return tooltipLine.append(String.format(" (%s) ", primaryIngredientText)).toString();
		}
		else if (!config.showPrimariesInTooltip() && config.showSecondariesInTooltip())
		{
			return tooltipLine.append(String.format(" (%s) ", secondaryIngredientText)).toString();
		}

		return ColorUtil.wrapWithColorTag(tooltipLine.toString(), GREY_COLOR);
	}

	public String get(String item) throws ExecutionException
	{
		return tooltipTextCache.get(item);
	}
}
