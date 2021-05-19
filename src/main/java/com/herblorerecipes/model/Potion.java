package com.herblorerecipes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.herblorerecipes.util.Utils.KEY_PRIMARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SECONDARY_IDENTIFIER;

public enum Potion
{
    IMP_REPELLENT("Anchovy oil", null, "Imp repellent", 3),
    ATTACK_POTION("Guam leaf", "Eye of newt", "Attack potion", 3),
    ANTIPOISON("Marrentill", "Unicorn horn dust", "Antipoison", 5),
    RELICYMS_BALM("Rogue's purse", "Snake weed", "Relicym's balm", 8),
    STRENGTH_POTION("Tarromin", "Limpwurt root", "Strength potion", 12),
    SERUM_207("Tarromin", "Ashes", "Serum 207", 15),
    GUTHIX_REST_TEA("Harralander", "Guam leaf x2,Marrentill", "Guthix rest tea", 18),
    COMPOST_POTION("Harralander", "Volcanic ash", "Compost potion", 22),
    RESTORE_POTION("Harralander", "Red spider's eggs", "Restore potion", 22),
    GUTHIX_BALANCE("Garlic", "Silver dust", "Guthix balance", 22),
    BLAMISH_OIL("Harralander", "Blamish snail slime", "Blamish oil", 25),
    ENERGY_POTION("Harralander", "Chocolate dust", "Energy potion", 26),
    DEFENCE_POTION("Ranarr weed", "White berries", "Defence potion", 30),
    AGILITY_POTION("Toadflax", "Toad's legs", "Agility potion", 34),
    COMBAT_POTION("Harralander", "Goat horn dust", "Combat potion", 36),
    PRAYER_POTION("Ranarr weed", "Snape grass", "Prayer potion", 38),
    SUPER_ATTACK("Irit leaf", "Eye of newt", "Super attack", 45),
    SUPERANTIPOISON("Irit leaf", "Unicorn horn dust", "Super antipoison", 48),
    FISHING_POTION("Avantoe", "Snape grass", "Fishing potion", 50),
    SUPER_ENERGY("Avantoe", "Mort myre fungus", "Super energy", 52),
    SHRINK_ME_QUICK("Tarromin", "Shrunk ogleroot", "Shrink-me-quick", 52),
    HUNTER_POTION("Avantoe", "Kebbit teeth dust", "Hunter potion", 53),
    SUPER_STRENGTH("Kwuarm", "Limpwurt root", "Super strength", 55),
    MAGIC_ESSENCE("Star flower", "Gorak claw powder", "Magic essence", 57),
    WEAPON_POISON("Kwuarm", "Dragon scale dust", "Weapon poison", 60),
    SUPER_RESTORE("Snapdragon", "Red spider's eggs", "Super restore", 63),
    SANFEW_SERUM("Unicorn horn dust", "Snake weed,Nail beast nails", "Sanfew serum", 65),
    SUPER_DEFENCE("Cadantine", "White berries", "Super defence", 66),
    ANTIDOTE_PLUS("Toadflax", "Yew roots", "Antidote+", 68),
    ANTIFIRE_POTION("Lantadyme", "Dragon scale dust", "Antifire potion", 69),
    DIVINE_SUPER_ATTACK("Crystal dust", null, "Divine super attack", 70),
    DIVINE_SUPER_DEFENCE("Crystal dust", null, "Divine super defence", 70),
    DIVINE_SUPER_STRENGTH("Crystal dust", null, "Divine super strength", 70),
    RANGING_POTION("Dwarf weed", "Wine of zamorak", "Ranging potion", 72),
    WEAPON_POISON_PLUS("Cactus spine", "Red spider's eggs", "Weapon Poison+", 73),
    DIVINE_RANGING_POTION("Crystal dust", null, "Divine ranging potion", 74),
    MAGIC_POTION("Lantadyme", "Potato cactus", "Magic potion", 76),
    STAMINA_POTION("Amylase crystal", null, "Stamina potion", 77),
    ZAMORAK_BREW("Torstol", "Jangerberries", "Zamorak brew", 78),
    DIVINE_MAGIC_POTION("Crystal dust", null, "Divine magic potion", 78),
    ANTIDOTE_PLUS_PLUS("Irit leaf", "Magic roots", "Antidote++", 79),
    BASTION_POTION("Cadantine", "Wine of zamorak", "Bastion potion", 80),
    BATTLEMAGE_POTION("Cadantine", "Potato cactus", "Battlemage potion", 80),
    SARADOMIN_BREW("Toadflax", "Crushed nest", "Saradomin brew", 81),
    WEAPON_POISON_PLUS_PLUS("Cave nightshade", "Poison ivy berries", "Weapon poison++", 82),
    EXTENDED_ANTIFIRE("Lava scale shard", null, "Extended antifire", 84),
    DIVINE_BASTION_POTION("Crystal dust", null, "Divine bastion potion", 86),
    DIVINE_BATTLEMAGE_POTION("Crystal dust", null, "Divine battlemage potion", 86),
    ANTI_VENOM("Zulrah's scales", null, "Anti-venom", 87),
    SUPER_COMBAT_POTION("Torstol", null, "Super combat potion", 90),
    SUPER_ANTIFIRE_POTION("Crushed superior dragon bones", null, "Super antifire potion", 92),
    ANTI_VENOM_PLUS("Torstol", "Anti-venom+", null, 94),
    DIVINE_SUPER_COMBAT("Crystal dust", null, "Divine super combat", 97),
    EXTENDED_SUPER_ANTIFIRE("Lava scale shard", null, "Extended super antifire", 98),
    GUAM_TAR("Guam leaf", null, "Guam tar", 19),
    MARRENTILL_TAR("Marrentill", null, "Marrentill tar", 31),
    TARROMIN_TAR("Tarromin", null, "Tarromin tar", 39),
    HARRALANDER_TAR("Harralander", null, "Harralander tar", 44);

    private static final Set<String> primaryIngredients = new HashSet<>();
    private static final Set<String> secondaryIngredients = new HashSet<>();
    private static final Map<String, List<Potion>> primaryIngredientToPotion = new HashMap<>();
    private static final Map<String, List<Potion>> secondaryIngredientToPotion = new HashMap<>();

    static
    {
        Arrays.stream(values()).forEach(potion -> primaryIngredientToPotion.computeIfAbsent(KEY_PRIMARY_IDENTIFIER + potion.primaryIngredient, s -> new ArrayList<>()).add(potion));
        Arrays.stream(values()).filter(potion -> potion.getSecondaryIngredient() != null).forEach(potion ->
                stripSecondIngredientName(potion.getSecondaryIngredient()).forEach(secondIngredient ->
                        secondaryIngredientToPotion.computeIfAbsent(KEY_SECONDARY_IDENTIFIER + secondIngredient, s -> new ArrayList<>()).add(potion))
        );
        Arrays.stream(values()).forEach(potion -> primaryIngredients.add(potion.primaryIngredient));
        Arrays.stream(values()).filter(potion -> potion.getSecondaryIngredient() != null).forEach(potion -> secondaryIngredients.add(potion.secondaryIngredient));
    }

    private final String primaryIngredient;
    private final String secondaryIngredient;
    private final String potionName;
    private final int level;

    Potion(String primaryIngredient, String secondaryIngredient, String potionName, int level)
    {
        this.primaryIngredient = primaryIngredient;
        this.secondaryIngredient = secondaryIngredient;
        this.potionName = potionName;
        this.level = level;
    }

    public static Set<String> getPrimaryIngredients()
    {
        return primaryIngredients;
    }

    public static Set<String> getSecondaryIngredients()
    {
        return secondaryIngredients;
    }

    public static List<Potion> getPotionsByPrimaryIngredient(String primaryIngredient)
    {
        return primaryIngredientToPotion.get(primaryIngredient);
    }

    public static List<Potion> getPotionsBySecondaryIngredient(String secondaryIngredient)
    {
        return secondaryIngredientToPotion.get(secondaryIngredient);
    }

    private static List<String> stripSecondIngredientName(String name)
    {
        // There can be multiple second ingredients (e.g. "Grimy guam leaf x2,Marrentill")
        // This will return ["Grimy guam leaf", "Marrentill"] as List
        return Arrays.asList(name.replaceAll(" x\\d", "").split(","));
    }

    public String getPotionName()
    {
        return potionName;
    }

    public int getLevel()
    {
        return level;
    }

    public String getSecondaryIngredient()
    {
        return secondaryIngredient;
    }
}
