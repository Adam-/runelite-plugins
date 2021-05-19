package com.herblorerecipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum Potion
{
    IMP_REPELLENT("Anchovy oil", "Imp repellent", 3),
    ATTACK_POTION("Guam leaf", "Attack potion", 3),
    ANTIPOISON("Marrentill", "Antipoison", 5),
    RELICYMS_BALM("Rogue's purse", "Relicym's balm", 8),
    STRENGTH_POTION("Tarromin", "Strength potion", 12),
    SERUM_207("Tarromin", "Serum 207", 15),
    GUTHIX_REST_TEA("Harralander", "Guthix rest tea", 18),
    COMPOST_POTION("Harralander", "Compost potion", 22),
    RESTORE_POTION("Harralander", "Restore potion", 22),
    GUTHIX_BALANCE("Garlic", "Guthix balance", 22),
    BLAMISH_OIL("Harralander", "Blamish oil", 25),
    ENERGY_POTION("Harralander", "Energy potion", 26),
    DEFENCE_POTION("Ranarr weed", "Defence potion", 30),
    AGILITY_POTION("Toadflax", "Agility potion", 34),
    COMBAT_POTION("Harralander", "Combat potion", 36),
    PRAYER_POTION("Ranarr weed", "Prayer potion", 38),
    SUPER_ATTACK("Irit leaf", "Super attack", 45),
    SUPERANTIPOISON("Irit leaf", "Super antipoison", 48),
    FISHING_POTION("Avantoe", "Fishing potion", 50),
    SUPER_ENERGY("Avantoe", "Super energy", 52),
    SHRINK_ME_QUICK("Tarromin", "Shrink-me-quick", 52),
    HUNTER_POTION("Avantoe", "Hunter potion", 53),
    SUPER_STRENGTH("Kwuarm", "Super strength", 55),
    MAGIC_ESSENCE("Star flower", "Magic essence", 57),
    WEAPON_POISON("Kwuarm", "Weapon poison", 60),
    SUPER_RESTORE("Snapdragon", "Super restore", 63),
    SANFEW_SERUM("Unicorn horn dust", "Sanfew serum", 65),
    SUPER_DEFENCE("Cadantine", "Super defence", 66),
    ANTIDOTE_PLUS("Toadflax", "Antidote+", 68),
    ANTIFIRE_POTION("Lantadyme", "Antifire potion", 69),
    DIVINE_SUPER_ATTACK("Crystal dust", "Divine super attack", 70),
    DIVINE_SUPER_DEFENCE("Crystal dust", "Divine super defence", 70),
    DIVINE_SUPER_STRENGTH("Crystal dust", "Divine super strength", 70),
    RANGING_POTION("Dwarf weed", "Ranging potion", 72),
    WEAPON_POISON_PLUS("Cactus spine", "Weapon Poison+", 73),
    DIVINE_RANGING_POTION("Crystal dust", "Divine ranging potion", 74),
    MAGIC_POTION("Lantadyme", "Magic potion", 76),
    STAMINA_POTION("Amylase crystal", "Stamina potion", 77),
    ZAMORAK_BREW("Torstol", "Zamorak brew", 78),
    DIVINE_MAGIC_POTION("Crystal dust", "Divine magic potion", 78),
    ANTIDOTE_PLUS_PLUS("Irit leaf", "Antidote++", 79),
    BASTION_POTION("Cadantine", "Bastion potion", 80),
    BATTLEMAGE_POTION("Cadantine", "Battlemage potion", 80),
    SARADOMIN_BREW("Toadflax", "Saradomin brew", 81),
    WEAPON_POISON_PLUS_PLUS("Cave nightshade", "Weapon poison++", 82),
    EXTENDED_ANTIFIRE("Lava scale shard", "Extended antifire", 84),
    DIVINE_BASTION_POTION("Crystal dust", "Divine bastion potion", 86),
    DIVINE_BATTLEMAGE_POTION("Crystal dust", "Divine battlemage potion", 86),
    ANTI_VENOM("Zulrah's scales", "Anti-venom", 87),
    SUPER_COMBAT_POTION("Torstol", "Super combat potion", 90),
    SUPER_ANTIFIRE_POTION("Crushed superior dragon bones", "Super antifire potion", 92),
    ANTI_VENOM_PLUS("Torstol", "Anti-venom+", 94),
    DIVINE_SUPER_COMBAT("Crystal dust", "Divine super combat", 97),
    EXTENDED_SUPER_ANTIFIRE("Lava scale shard", "Extended super antifire", 98);

    private final String primaryIngredient;
    private final String potionName;
    private final int level;

    private static final Set<String> primaryIngredients = new HashSet<>();

    private static final Map<String, List<Potion>> lookup = new HashMap<>();

    static
    {
        Arrays.stream(values()).forEach(potion -> lookup.computeIfAbsent(potion.primaryIngredient, s -> new ArrayList<>()).add(potion));
        Arrays.stream(values()).forEach(potion -> primaryIngredients.add(potion.primaryIngredient));
    }

    Potion(String primaryIngredient, String potionName, int level)
    {
        this.primaryIngredient = primaryIngredient;
        this.potionName = potionName;
        this.level = level;
    }

    public static Set<String> getPrimaryIngredients()
    {
        return primaryIngredients;
    }

    public static List<Potion> getPotionsByPrimaryIngredient(String primaryIngredient)
    {
        return lookup.get(primaryIngredient);
    }

    public String getPotionName()
    {
        return potionName;
    }

    public int getLevel()
    {
        return level;
    }
}
