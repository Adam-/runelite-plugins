package com.herblorerecipes.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.herblorerecipes.HerbloreRecipesConfig;
import com.herblorerecipes.model.Potion;
import net.runelite.client.util.ColorUtil;

import java.awt.Color;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.herblorerecipes.util.Utils.KEY_PRIMARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SECONDARY_IDENTIFIER;

public class HerbloreRecipesCacheLoader extends CacheLoader<String, String>
{
    private static final Color GREY_COLOR = new Color(238, 238, 238);
    private static LoadingCache<String, String> tooltipTextCache;

    private final HerbloreRecipesConfig config;

    public HerbloreRecipesCacheLoader(HerbloreRecipesConfig config)
    {
        this.config = config;
        tooltipTextCache = CacheBuilder.newBuilder()
                .maximumSize(Potion.getPrimaryIngredients().size() + Potion.getSecondaryIngredients().size())
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(this);
    }

    @Override
    public String load(String key) throws Exception
    {
        if (key.charAt(0) == KEY_PRIMARY_IDENTIFIER.charAt(0))
        {
            // Primary ingredients
            return Potion.getPotionsByPrimaryIngredient(key).stream()
                    .map(this::makeTooltipText)
                    .collect(Collectors.joining("</br>", "</br>", "</br>"));
        }
        if (key.charAt(0) == KEY_SECONDARY_IDENTIFIER.charAt(0))
        {
            return Potion.getPotionsBySecondaryIngredient(key).stream()
                    .map(this::makeTooltipText)
                    .collect(Collectors.joining("</br>", "</br>", "</br>"));
        }
        return null;
    }

    private String makeTooltipText(Potion potion)
    {
        String text = config.showLevelReqs() ?
                String.format("lvl %d: %s", potion.getLevel(), potion.getPotionName()) :
                potion.getPotionName();
        text += (config.showSecondaryIngredientsAlongsidePrimaries() && potion.getSecondaryIngredient() != null) ? String.format(" (2nd: %s)", potion.getSecondaryIngredient()) : "";
        return ColorUtil.wrapWithColorTag(text, GREY_COLOR);
    }

    public String get(String item) throws ExecutionException
    {
        return tooltipTextCache.get(item);
    }

    public static void clearCache()
    {
        tooltipTextCache.invalidateAll();
    }
}
