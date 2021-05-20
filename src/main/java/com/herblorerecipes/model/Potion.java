package com.herblorerecipes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.herblorerecipes.util.Utils.KEY_PRIMARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SECONDARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_UNF_IDENTIFIER;

public enum Potion
{
    IMP_REPELLENT("Anchovy oil", "Marigolds, Rosemary, Nasturtiums, Woad leaf, Limpwurt root, White lily", null, "Imp repellent", 3),
    ATTACK_POTION("Guam leaf", "Eye of newt", "Guam potion (unf)", "Attack potion", 3),
    ANTIPOISON("Marrentill", "Unicorn horn dust", "Marrentill potion (unf)", "Antipoison", 5),
    RELICYMS_BALM("Rogue's purse", "Snake weed", "Unfinished potion (Rogue's Purse)", "Relicym's balm", 8),
    STRENGTH_POTION("Tarromin", "Limpwurt root", "Tarromin potion (unf)", "Strength potion", 12),
    SERUM_207("Tarromin", "Ashes", "Tarromin potion (unf)", "Serum 207", 15),
    GUTHIX_REST_TEA("Harralander", "Guam leaf x2, Marrentill", null, "Guthix rest tea", 18),
    COMPOST_POTION("Harralander", "Volcanic ash", "Harralander potion (unf)", "Compost potion", 22),
    RESTORE_POTION("Harralander", "Red spider's eggs", "Harralander potion (unf)", "Restore potion", 22),
    GUTHIX_BALANCE("Garlic", "Silver dust", null, "Guthix balance", 22),
    BLAMISH_OIL("Harralander", "Blamish snail slime", "Harralander potion (unf)", "Blamish oil", 25),
    ENERGY_POTION("Harralander", "Chocolate dust", "Harralander potion (unf)", "Energy potion", 26),
    DEFENCE_POTION("Ranarr weed", "White berries", "Ranarr potion (unf)", "Defence potion", 30),
    AGILITY_POTION("Toadflax", "Toad's legs", "Toadflax potion (unf)", "Agility potion", 34),
    COMBAT_POTION("Harralander", "Goat horn dust", "Harralander potion (unf)", "Combat potion", 36),
    PRAYER_POTION("Ranarr weed", "Snape grass", "Ranarr potion (unf)", "Prayer potion", 38),
    SUPER_ATTACK("Irit leaf", "Eye of newt", "Irit potion (unf)", "Super attack", 45),
    SUPERANTIPOISON("Irit leaf", "Unicorn horn dust", "Irit potion (unf)", "Super antipoison", 48),
    FISHING_POTION("Avantoe", "Snape grass", "Avantoe potion (unf)", "Fishing potion", 50),
    SUPER_ENERGY("Avantoe", "Mort myre fungus", "Avantoe potion (unf)", "Super energy", 52),
    SHRINK_ME_QUICK("Tarromin", "Shrunk ogleroot", "Tarromin potion (unf)", "Shrink-me-quick", 52),
    HUNTER_POTION("Avantoe", "Kebbit teeth dust", "Avantoe potion (unf)", "Hunter potion", 53),
    SUPER_STRENGTH("Kwuarm", "Limpwurt root", "Kwuarm potion (unf)", "Super strength", 55),
    MAGIC_ESSENCE("Star flower", "Gorak claw powder", "Magic essence (unf)", "Magic essence", 57),
    WEAPON_POISON("Kwuarm", "Dragon scale dust", "Kwuarm potion (unf)", "Weapon poison", 60),
    SUPER_RESTORE("Snapdragon", "Red spider's eggs", "Snapdragon potion (unf)", "Super restore", 63),
    SANFEW_SERUM("Unicorn horn dust", "Snake weed, Nail beast nails", "Mixture - step 2(4)", "Sanfew serum", 65),
    SUPER_DEFENCE("Cadantine", "White berries", "Cadantine potion (unf)", "Super defence", 66),
    ANTIDOTE_PLUS("Toadflax", "Yew roots", "Toadflax potion (unf)", "Antidote+", 68),
    ANTIFIRE_POTION("Lantadyme", "Dragon scale dust", "Lantadyme potion (unf)", "Antifire potion", 69),
    DIVINE_SUPER_ATTACK("Super attack", "Crystal dust", null, "Divine super attack", 70),
    DIVINE_SUPER_DEFENCE("Super defence", "Crystal dust", null, "Divine super defence", 70),
    DIVINE_SUPER_STRENGTH("Super strength", "Crystal dust", null, "Divine super strength", 70),
    RANGING_POTION("Dwarf weed", "Wine of zamorak", "Dwarf weed potion (unf)", "Ranging potion", 72),
    WEAPON_POISON_PLUS("Cactus spine", "Red spider's eggs", "Weapon poison+ (unf)", "Weapon Poison+", 73),
    DIVINE_RANGING_POTION("Ranging potion", "Crystal dust", null, "Divine ranging potion", 74),
    MAGIC_POTION("Lantadyme", "Potato cactus", "Lantadyme potion (unf)", "Magic potion", 76),
    STAMINA_POTION("Super energy", "Amylase crystal", null, "Stamina potion", 77),
    ZAMORAK_BREW("Torstol", "Jangerberries", "Torstol potion (unf)", "Zamorak brew", 78),
    DIVINE_MAGIC_POTION("Magic potion", "Crystal dust", null, "Divine magic potion", 78),
    ANTIDOTE_PLUS_PLUS("Irit leaf", "Magic roots", "Antidote++ (unf)", "Antidote++", 79),
    BASTION_POTION("Cadantine", "Wine of zamorak", "Cadantine blood potion (unf)", "Bastion potion", 80),
    BATTLEMAGE_POTION("Cadantine", "Potato cactus", "Cadantine blood potion (unf)", "Battlemage potion", 80),
    SARADOMIN_BREW("Toadflax", "Crushed nest", "Toadflax potion (unf)", "Saradomin brew", 81),
    WEAPON_POISON_PLUS_PLUS("Cave nightshade", "Poison ivy berries", "Weapon poison++ (unf)", "Weapon poison++", 82),
    EXTENDED_ANTIFIRE("Antifire potion", "Lava scale shard", null, "Extended antifire", 84),
    DIVINE_BASTION_POTION("Bastion potion", "Crystal dust", null, "Divine bastion potion", 86),
    DIVINE_BATTLEMAGE_POTION("Battlemage potion", "Crystal dust", null, "Divine battlemage potion", 86),
    ANTI_VENOM("Antidote++", "Zulrah's scales", null, "Anti-venom", 87),
    SUPER_COMBAT_POTION("Torstol", "Super attack, Super strength, Super defence", "Torstol potion (unf)", "Super combat potion", 90),
    SUPER_ANTIFIRE_POTION("Antifire potion", "Crushed superior dragon bones", null, "Super antifire potion", 92),
    ANTI_VENOM_PLUS("Anti-venom", "Torstol", null, "Anti-venom+", 94),
    DIVINE_SUPER_COMBAT("Super combat potion", "Crystal dust", null, "Divine super combat", 97),
    EXTENDED_SUPER_ANTIFIRE("Super antifire potion", "Lava scale shard", null, "Extended super antifire", 98),
    GUAM_TAR("Guam leaf", null, null, "Guam tar", 19),
    MARRENTILL_TAR("Marrentill", null, null, "Marrentill tar", 31),
    TARROMIN_TAR("Tarromin", null, null, "Tarromin tar", 39),
    HARRALANDER_TAR("Harralander", null, null, "Harralander tar", 44);

    private static final Set<String> primaryIngredients = new HashSet<>();
    private static final Set<String> secondaryIngredients = new HashSet<>();
    private static final Set<String> unfinishedPotions = new HashSet<>();
    private static final Map<String, List<Potion>> primaryIngredientToPotion = new HashMap<>();
    private static final Map<String, List<Potion>> secondaryIngredientToPotion = new HashMap<>();
    private static final Map<String, List<Potion>> unfinishedPotionsToPotion = new HashMap<>();

    static
    {
        // Prepare the lookup HashMaps
        Arrays.stream(values()).forEach(potion -> primaryIngredientToPotion.computeIfAbsent(KEY_PRIMARY_IDENTIFIER + potion.primaryIngredient, s -> new ArrayList<>()).add(potion));
        Arrays.stream(values()).filter(potion -> potion.unfinishedPotion != null).forEach(potion -> unfinishedPotionsToPotion.computeIfAbsent(KEY_UNF_IDENTIFIER + potion.unfinishedPotion, s -> new ArrayList<>()).add(potion));
        Arrays.stream(values()).filter(potion -> potion.secondaryIngredient != null).forEach(potion ->
                stripSecondIngredientName(potion.secondaryIngredient).forEach(secondIngredient ->
                        secondaryIngredientToPotion.computeIfAbsent(KEY_SECONDARY_IDENTIFIER + secondIngredient, s -> new ArrayList<>()).add(potion)));

        // Prepare the HashSets
        Arrays.stream(values()).forEach(potion -> primaryIngredients.add(potion.primaryIngredient));
        Arrays.stream(values()).filter(potion -> potion.secondaryIngredient != null).forEach(potion -> secondaryIngredients.add(potion.secondaryIngredient));
        Arrays.stream(values()).filter(potion -> potion.unfinishedPotion != null).forEach(potion -> unfinishedPotions.add(potion.unfinishedPotion));
    }

    private final String primaryIngredient;
    private final String secondaryIngredient;
    private final String unfinishedPotion;
    private final String potionName;
    private final int level;

    Potion(String primaryIngredient, String secondaryIngredient, String unfinishedPotion, String potionName, int level)
    {
        this.primaryIngredient = primaryIngredient;
        this.secondaryIngredient = secondaryIngredient;
        this.unfinishedPotion = unfinishedPotion;
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

    public static Set<String> getUnfinishedPotions()
    {
        return unfinishedPotions;
    }

    public static List<Potion> getPotionsByPrimaryIngredient(String primaryIngredient)
    {
        return primaryIngredientToPotion.get(primaryIngredient);
    }

    public static List<Potion> getPotionsBySecondaryIngredient(String secondaryIngredient)
    {
        return secondaryIngredientToPotion.get(secondaryIngredient);
    }

    public static List<Potion> getPotionsByUnfinishedPotion(String name)
    {
        return unfinishedPotionsToPotion.get(name);
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

    public String getPrimaryIngredient()
    {
        return primaryIngredient;
    }

    public String getUnfinishedPotion()
    {
        return unfinishedPotion;
    }

    private static List<String> stripSecondIngredientName(String name)
    {
        // There can be multiple second ingredients (e.g. "Grimy guam leaf x2,Marrentill")
        // This will return ["Grimy guam leaf", "Marrentill"] as List
        return Arrays.stream(name.replaceAll("\\s?x\\d", "")
                                 .split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
