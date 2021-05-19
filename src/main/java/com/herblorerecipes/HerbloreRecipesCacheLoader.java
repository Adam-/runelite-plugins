package com.herblorerecipes;

import com.google.common.cache.CacheLoader;
import net.runelite.client.util.ColorUtil;

import java.awt.Color;
import java.util.stream.Collectors;

public class HerbloreRecipesCacheLoader extends CacheLoader<String, String>
{
    private static final Color GREY_COLOR = new Color(238, 238, 238);

    private final HerbloreRecipesConfig config;

    HerbloreRecipesCacheLoader(HerbloreRecipesConfig config)
    {
        this.config = config;
    }

    @Override
    public String load(String key) throws Exception
    {
        if (key.charAt(0) == '1')
        {
            // Primary ingredients
            return Potion.getPotionsByPrimaryIngredient(key).stream()
                    .map(this::makeTooltipText)
                    .collect(Collectors.joining("</br>", "</br>", "</br>"));
        }
        if (key.charAt(0) == '2')
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
        text += (config.showSecondaryIngredients() && potion.getSecondaryIngredient() != null) ? String.format(" (2nd: %s)", potion.getSecondaryIngredient()) : "";
        return ColorUtil.wrapWithColorTag(text, GREY_COLOR);
    }
}
