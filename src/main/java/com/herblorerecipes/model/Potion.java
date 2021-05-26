package com.herblorerecipes.model;

import static com.herblorerecipes.util.Utils.KEY_POTION_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_PRIMARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SECONDARY_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_SEED_IDENTIFIER;
import static com.herblorerecipes.util.Utils.KEY_UNF_IDENTIFIER;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum Potion
{
	IMP_REPELLENT("Imp repellent", "Anchovy oil", "Marigolds, Rosemary, Nasturtiums, Woad leaf, Limpwurt root, White lily", null, null, 3),
	ATTACK_POTION("Attack potion", "Guam leaf", "Eye of newt", "Guam potion (unf)", "Guam seed", 3),
	ANTIPOISON("Antipoison", "Marrentill", "Unicorn horn dust", "Marrentill potion (unf)", "Marrentill seed", 5),
	RELICYMS_BALM("Relicym's balm", "Rogue's purse", "Snake weed", "Unfinished potion (Rogue's Purse)", null, 8),
	STRENGTH_POTION("Strength potion", "Tarromin", "Limpwurt root", "Tarromin potion (unf)", "Tarromin seed", 12),
	SERUM_207("Serum 207", "Tarromin", "Ashes", "Tarromin potion (unf)", "Tarromin seed", 15),
	GUTHIX_REST_TEA("Guthix rest tea", "Harralander", "Guam leaf x2, Marrentill", null, "Harralander seed", 18),
	COMPOST_POTION("Compost potion", "Harralander", "Volcanic ash", "Harralander potion (unf)", "Harralander seed", 22),
	RESTORE_POTION("Restore potion", "Harralander", "Red spider's eggs", "Harralander potion (unf)", "Harralander seed", 22),
	GUTHIX_BALANCE("Guthix balance", "Garlic", "Silver dust", null, null, 22),
	BLAMISH_OIL("Blamish oil", "Harralander", "Blamish snail slime", "Harralander potion (unf)", "Harralander seed", 25),
	ENERGY_POTION("Energy potion", "Harralander", "Chocolate dust", "Harralander potion (unf)", "Harralander seed", 26),
	DEFENCE_POTION("Defence potion", "Ranarr weed", "White berries", "Ranarr potion (unf)", "Ranarr seed", 30),
	AGILITY_POTION("Agility potion", "Toadflax", "Toad's legs", "Toadflax potion (unf)", "Toadflax seed", 34),
	COMBAT_POTION("Combat potion", "Harralander", "Goat horn dust", "Harralander potion (unf)", "Harralander seed", 36),
	PRAYER_POTION("Prayer potion", "Ranarr weed", "Snape grass", "Ranarr potion (unf)", "Ranarr seed", 38),
	SUPER_ATTACK("Super attack", "Irit leaf", "Eye of newt", "Irit potion (unf)", "Irit seed", 45),
	SUPERANTIPOISON("Superantipoison", "Irit leaf", "Unicorn horn dust", "Irit potion (unf)", "Irit seed", 48),
	FISHING_POTION("Fishing potion", "Avantoe", "Snape grass", "Avantoe potion (unf)", "Avantoe seed", 50),
	SUPER_ENERGY("Super energy", "Avantoe", "Mort myre fungus", "Avantoe potion (unf)", "Avantoe seed", 52),
	SHRINK_ME_QUICK("Shrink-me-quick", "Tarromin", "Shrunk ogleroot", "Tarromin potion (unf)", "Tarromin seed", 52),
	HUNTER_POTION("Hunter potion", "Avantoe", "Kebbit teeth dust", "Avantoe potion (unf)", "Avantoe seed", 53),
	SUPER_STRENGTH("Super strength", "Kwuarm", "Limpwurt root", "Kwuarm potion (unf)", "Kwuarm seed", 55),
	MAGIC_ESSENCE("Magic essence", "Star flower", "Gorak claw powder", "Magic essence (unf)", null, 57),
	WEAPON_POISON("Weapon poison", "Kwuarm", "Dragon scale dust", "Kwuarm potion (unf)", "Kwuarm seed", 60),
	SUPER_RESTORE("Super restore", "Snapdragon", "Red spider's eggs", "Snapdragon potion (unf)", "Snapdragon seed", 63),
	SANFEW_SERUM("Sanfew serum", "Unicorn horn dust", "Snake weed, Nail beast nails", "Mixture - step 2(4)", null, 65),
	SUPER_DEFENCE("Super defence", "Cadantine", "White berries", "Cadantine potion (unf)", "Cadantine seed", 66),
	ANTIDOTE_PLUS("Antidote+", "Toadflax", "Yew roots", "Toadflax potion (unf)", "Toadflax seed", 68),
	ANTIFIRE_POTION("Antifire potion", "Lantadyme", "Dragon scale dust", "Lantadyme potion (unf)", "Lantadyme seed", 69),
	DIVINE_SUPER_ATTACK("Divine super attack", "Super attack", "Crystal dust", null, null, 70),
	DIVINE_SUPER_DEFENCE("Divine super defence", "Super defence", "Crystal dust", null, null, 70),
	DIVINE_SUPER_STRENGTH("Divine super strength", "Super strength", "Crystal dust", null, null, 70),
	RANGING_POTION("Ranging potion", "Dwarf weed", "Wine of zamorak", "Dwarf weed potion (unf)", "Dwarf weed seed", 72),
	WEAPON_POISON_PLUS("Weapon Poison+", "Cactus spine", "Red spider's eggs", "Weapon poison+ (unf)", null, 73),
	DIVINE_RANGING_POTION("Divine ranging potion", "Ranging potion", "Crystal dust", null, null, 74),
	MAGIC_POTION("Magic potion", "Lantadyme", "Potato cactus", "Lantadyme potion (unf)", "Lantadyme seed", 76),
	STAMINA_POTION("Stamina potion", "Super energy", "Amylase crystal", null, null, 77),
	ZAMORAK_BREW("Zamorak brew", "Torstol", "Jangerberries", "Torstol potion (unf)", "Torstol seed", 78),
	DIVINE_MAGIC_POTION("Divine magic potion", "Magic potion", "Crystal dust", null, null, 78),
	ANTIDOTE_PLUS_PLUS("Antidote++", "Irit leaf", "Magic roots", "Antidote++ (unf)", "Irit seed", 79),
	BASTION_POTION("Bastion potion", "Cadantine", "Wine of zamorak", "Cadantine blood potion (unf)", "Cadantine seed", 80),
	BATTLEMAGE_POTION("Battlemage potion", "Cadantine", "Potato cactus", "Cadantine blood potion (unf)", "Cadantine seed", 80),
	SARADOMIN_BREW("Saradomin brew", "Toadflax", "Crushed nest", "Toadflax potion (unf)", "Toadflax seed", 81),
	WEAPON_POISON_PLUS_PLUS("Weapon poison++", "Cave nightshade", "Poison ivy berries", "Weapon poison++ (unf)", null, 82),
	EXTENDED_ANTIFIRE("Extended antifire", "Antifire potion", "Lava scale shard", null, null, 84),
	DIVINE_BASTION_POTION("Divine bastion potion", "Bastion potion", "Crystal dust", null, null, 86),
	DIVINE_BATTLEMAGE_POTION("Divine battlemage potion", "Battlemage potion", "Crystal dust", null, null, 86),
	ANTI_VENOM("Anti-venom", "Antidote++", "Zulrah's scales", null, null, 87),
	SUPER_COMBAT_POTION("Super combat potion", "Torstol", "Super attack, Super strength, Super defence", "Torstol potion (unf)", "Torstol seed", 90),
	SUPER_ANTIFIRE_POTION("Super antifire potion", "Antifire potion", "Crushed superior dragon bones", null, null, 92),
	ANTI_VENOM_PLUS("Anti-venom+", "Anti-venom", "Torstol", null, "Torstol seed", 94),
	DIVINE_SUPER_COMBAT("Divine super combat", "Super combat potion", "Crystal dust", null, null, 97),
	EXTENDED_SUPER_ANTIFIRE("Extended super antifire", "Super antifire potion", "Lava scale shard", null, null, 98),
	GUAM_TAR("Guam tar", "Guam leaf", "Swamp tar", null, "Guam seed", 19),
	MARRENTILL_TAR("Marrentill tar", "Marrentill", "Swamp tar", null, "Marrentill seed", 31),
	TARROMIN_TAR("Tarromin tar", "Tarromin", "Swamp tar", null, "Tarromin seed", 39),
	HARRALANDER_TAR("Harralander tar", "Harralander", "Swamp tar", null, "Harralander seed", 44);

	private static final Set<String> potionNames = new HashSet<>();
	private static final Set<String> primaries = new HashSet<>();
	private static final Set<String> secondariesSet = new HashSet<>();
	private static final Set<String> unfinishedPotions = new HashSet<>();
	private static final Set<String> seeds = new HashSet<>();
	private static final Map<String, Potion> potionNameToPotionMap = new HashMap<>();
	private static final Map<String, List<Potion>> primaryIngredientToPotion = new HashMap<>();
	private static final Map<String, List<Potion>> secondaryIngredientToPotion = new HashMap<>();
	private static final Map<String, List<Potion>> unfinishedPotionsToPotion = new HashMap<>();
	private static final Map<String, List<Potion>> seedToPotions = new HashMap<>();

	static
	{
		// Prepare the lookup HashMaps
		Arrays.stream(values()).forEach(potion -> {
			potionNameToPotionMap.put(KEY_POTION_IDENTIFIER + potion.potionName, potion);
			primaryIngredientToPotion.computeIfAbsent(KEY_PRIMARY_IDENTIFIER + potion.primary, s -> new ArrayList<>()).add(potion);
			stripSecondIngredientName(potion.secondaries).forEach(secondIngredient ->
				secondaryIngredientToPotion.computeIfAbsent(KEY_SECONDARY_IDENTIFIER + secondIngredient, s -> new ArrayList<>()).add(potion));
		});
		Arrays.stream(values()).filter(potion -> potion.unfinishedPotion != null).forEach(potion -> unfinishedPotionsToPotion.computeIfAbsent(KEY_UNF_IDENTIFIER + potion.unfinishedPotion, s -> new ArrayList<>()).add(potion));
		Arrays.stream(values()).filter(potion -> potion.seed != null).forEach(potion -> seedToPotions.computeIfAbsent(KEY_SEED_IDENTIFIER + potion.seed, s -> new ArrayList<>()).add(potion));

		// Prepare the HashSets
		Arrays.stream(values()).forEach(potion -> {
			potionNames.add(potion.potionName);
			primaries.add(potion.primary);
			secondariesSet.add(potion.secondaries);
		});
		Arrays.stream(values()).filter(potion -> potion.unfinishedPotion != null).forEach(potion -> unfinishedPotions.add(potion.unfinishedPotion));
		Arrays.stream(values()).filter(potion -> potion.seed != null).forEach(potion -> seeds.add(potion.seed));
	}

	private final String primary;
	private final String secondaries;
	private final String unfinishedPotion;
	private final String potionName;
	private final String seed;
	private final int level;

	Potion(String potionName, String primary, String secondaries, String unfinishedPotion, String seed, int level)
	{
		this.primary = primary;
		this.secondaries = secondaries;
		this.unfinishedPotion = unfinishedPotion;
		this.potionName = potionName;
		this.seed = seed;
		this.level = level;
	}

	public static Set<String> getPotionNames()
	{
		return potionNames;
	}

	public static Set<String> getPrimaries()
	{
		return primaries;
	}

	public static Set<String> getSecondariesSet()
	{
		return secondariesSet;
	}

	public static Set<String> getUnfinishedPotions()
	{
		return unfinishedPotions;
	}

	public static Set<String> getSeeds()
	{
		return seeds;
	}

	public static List<Potion> getPotionsBySeed(String seed)
	{
		return seedToPotions.get(seed);
	}

	public static Potion getPotionByName(String name)
	{
		return potionNameToPotionMap.get(name);
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

	private static List<String> stripSecondIngredientName(String name)
	{
		// There can be multiple second ingredients (e.g. "Grimy guam leaf x2,Marrentill")
		// This will return ["Grimy guam leaf", "Marrentill"] as List
		return Arrays.stream(name.replaceAll("\\s?x\\d", "")
			.split(","))
			.map(String::trim)
			.collect(Collectors.toList());
	}

	public String getPotionName()
	{
		return potionName;
	}

	public int getLevel()
	{
		return level;
	}

	public String getSecondaries()
	{
		return secondaries;
	}

	public String getPrimary()
	{
		return primary;
	}
}
