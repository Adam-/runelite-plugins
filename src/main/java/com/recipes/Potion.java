package com.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum Potion {
    IMP_REPELLENT("Anchovy oil", "Imp repellent"),
    ATTACK_POTION("Guam leaf", "Attack potion"),
    ANTIPOISON("Marrentill", "Antipoison"),
    RELICYMS_BALM("Rogue's purse", "Relicym's balm"),
    STRENGTH_POTION("Tarromin", "Strength potion"),
    SERUM_207("Tarromin", "Serum 207"),
    GUTHIX_REST_TEA("Harralander", "Guthix rest tea"),
    COMPOST_POTION("Harralander", "Compost potion"),
    RESTORE_POTION("Harralander", "Restore potion"),
    GUTHIX_BALANCE("Garlic", "Guthix balance"),
    BLAMISH_OIL("Harralander", "Blamish oil"),
    ENERGY_POTION("Harralander", "Energy potion"),
    DEFENCE_POTION("Ranarr weed", "Defence potion"),
    AGILITY_POTION("Toadflax", "Agility potion"),
    COMBAT_POTION("Harralander", "Combat potion"),
    PRAYER_POTION("Ranarr weed", "Prayer potion"),
    SUPER_ATTACK("Irit leaf", "Super attack"),
    SUPERANTIPOISON("Irit leaf", "Super antipoison"),
    FISHING_POTION("Avantoe", "Fishing potion"),
    SUPER_ENERGY("Avantoe", "Super energy"),
    SHRINK_ME_QUICK("Tarromin", "Shrink-me-quick"),
    HUNTER_POTION("Avantoe", "Hunter potion"),
    SUPER_STRENGTH("Kwuarm", "Super strength"),
    MAGIC_ESSENCE("Star flower", "Magic essence"),
    WEAPON_POISON("Kwuarm", "Weapon poison"),
    SUPER_RESTORE("Snapdragon", "Super restore"),
    SANFEW_SERUM("Unicorn horn dust", "Sanfew serum"),
    SUPER_DEFENCE("Cadantine", "Super defence"),
    ANTIDOTE_PLUS("Toadflax", "Antidote+"),
    ANTIFIRE_POTION("Lantadyme", "Antifire potion"),
    DIVINE_SUPER_ATTACK("Crystal dust", "Divine super attack"),
    DIVINE_SUPER_DEFENCE("Crystal dust", "Divine super defence"),
    DIVINE_SUPER_STRENGTH("Crystal dust", "Divine super strength"),
    RANGING_POTION("Dwarf weed", "Ranging potion"),
    WEAPON_POISON_PLUS("Cactus spine", "Weapon Poison+"),
    DIVINE_RANGING_POTION("Crystal dust", "Divine ranging potion"),
    MAGIC_POTION("Lantadyme", "Magic potion"),
    STAMINA_POTION("Amylase crystal", "Stamina potion"),
    ZAMORAK_BREW("Torstol", "Zamorak brew"),
    DIVINE_MAGIC_POTION("Crystal dust", "Divine magic potion"),
    ANTIDOTE_PLUS_PLUS("Irit leaf", "Antidote++"),
    BASTION_POTION("Cadantine", "Bastion potion"),
    BATTLEMAGE_POTION("Cadantine", "Battlemage potion"),
    SARADOMIN_BREW("Toadflax", "Saradomin brew"),
    WEAPON_POISON_PLUS_PLUS("Cave nightshade", "Weapon poison++"),
    EXTENDED_ANTIFIRE("Lava scale shard", "Extended antifire"),
    DIVINE_BASTION_POTION("Crystal dust", "Divine bastion potion"),
    DIVINE_BATTLEMAGE_POTION("Crystal dust", "Divine battlemage potion"),
    ANTI_VENOM("Zulrah's scales", "Anti-venom"),
    SUPER_COMBAT_POTION("Torstol", "Super combat potion"),
    SUPER_ANTIFIRE_POTION("Crushed superior dragon bones", "Super antifire potion"),
    ANTI_VENOM_PLUS("Torstol", "Anti-venom+"),
    DIVINE_SUPER_COMBAT("Crystal dust", "Divine super combat"),
    EXTENDED_SUPER_ANTIFIRE("Lava scale shard", "Extended super antifire");
    
    private final String primaryIngredient;
    private final String potionName;

    private static final Set<String> primaryIngredients = new HashSet<>();

    private static final Map<String, List<Potion>> lookup = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(potion -> lookup.computeIfAbsent(potion.primaryIngredient, s -> new ArrayList<>()).add(potion));
        Arrays.stream(values()).forEach(potion -> primaryIngredients.add(potion.primaryIngredient));
    }
    
    Potion(String primaryIngredient, String potionName) {
        this.primaryIngredient = primaryIngredient;
        this.potionName = potionName;
    }

    public static Set<String> getPrimaryIngredients() {
        return primaryIngredients;
    }

    public static List<Potion> getPotionsByPrimaryIngredient(String primaryIngredient) {
        return lookup.get(primaryIngredient);
    }

    public String getPotionName() {
        return potionName;
    }
}
