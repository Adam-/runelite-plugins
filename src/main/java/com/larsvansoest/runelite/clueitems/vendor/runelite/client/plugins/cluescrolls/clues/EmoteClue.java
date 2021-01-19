/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.larsvansoest.runelite.clueitems.vendor.runelite.client.plugins.cluescrolls.clues;

import com.google.common.collect.ImmutableSet;
import com.larsvansoest.runelite.clueitems.EmoteClueItemsPlugin;
import com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty;
import static com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty.Beginner;
import static com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty.Easy;
import static com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty.Elite;
import static com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty.Hard;
import static com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty.Master;
import static com.larsvansoest.runelite.clueitems.data.EmoteClueDifficulty.Medium;
import com.larsvansoest.runelite.clueitems.data.EmoteClueItem;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import static net.runelite.api.EquipmentInventorySlot.*;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.cluescrolls.clues.Enemy;
import static net.runelite.client.plugins.cluescrolls.clues.Enemy.DOUBLE_AGENT_108;
import static net.runelite.client.plugins.cluescrolls.clues.Enemy.DOUBLE_AGENT_141;
import static net.runelite.client.plugins.cluescrolls.clues.Enemy.DOUBLE_AGENT_65;
import net.runelite.client.plugins.cluescrolls.clues.LocationClueScroll;
import net.runelite.client.plugins.cluescrolls.clues.TextClueScroll;
import net.runelite.client.plugins.cluescrolls.clues.emote.Emote;
import static net.runelite.client.plugins.cluescrolls.clues.emote.Emote.*;
import net.runelite.client.plugins.cluescrolls.clues.emote.STASHUnit;
import static net.runelite.client.plugins.cluescrolls.clues.emote.STASHUnit.*;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;
import static net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirements.emptySlot;

/**
 * Data set vendored from RuneLite source. Replaces original {@link ItemRequirement} objects with {@link EmoteClueItem} entries to use in the {@link EmoteClueItemsPlugin}.
 * <p>
 * Source: https://github.com/runelite/runelite/tree/master/runelite-client/src/main/java/net/runelite/client/plugins/cluescrolls/clues
 */
@Getter
public class EmoteClue extends ClueScroll implements TextClueScroll, LocationClueScroll
{
	public static final Set<EmoteClue> CLUES = ImmutableSet.of(
		new EmoteClue(Hard, "Beckon on the east coast of the Kharazi Jungle. Beware of double agents! Equip any vestment stole and a heraldic rune shield.", "Kharazi Jungle", NORTHEAST_CORNER_OF_THE_KHARAZI_JUNGLE, new WorldPoint(2954, 2933, 0), DOUBLE_AGENT_108, BECKON, EmoteClueItem.ANY_STOLE, EmoteClueItem.ANY_HERALDIC_RUNE_SHIELD),
		new EmoteClue(Medium, "Cheer in the Barbarian Agility Arena. Headbang before you talk to me. Equip a steel platebody, maple shortbow and a Wilderness cape.", "Barbarian Outpost", BARBARIAN_OUTPOST_OBSTACLE_COURSE, new WorldPoint(2552, 3556, 0), CHEER, HEADBANG, EmoteClueItem.STEEL_PLATEBODY, EmoteClueItem.MAPLE_SHORTBOW, EmoteClueItem.ANY_TEAM_CAPE),
		new EmoteClue(Elite, "Bow upstairs in the Edgeville Monastery. Equip a completed prayer book.", "Edgeville Monastery", SOUTHEAST_CORNER_OF_THE_MONASTERY, new WorldPoint(3056, 3484, 1), BOW, EmoteClueItem.ANY_GOD_BOOK),
		new EmoteClue(Elite, "Cheer in the Shadow dungeon. Equip a rune crossbow, climbing boots and any mitre.", "Shadow dungeon", ENTRANCE_OF_THE_CAVE_OF_DAMIS, new WorldPoint(2629, 5071, 0), CHEER, EmoteClueItem.ANY_MITRE, EmoteClueItem.RUNE_CROSSBOW, EmoteClueItem.CLIMBING_BOOTS, EmoteClueItem.RING_OF_VISIBILITY),
		new EmoteClue(Hard, "Cheer at the top of the agility pyramid. Beware of double agents! Equip a blue mystic robe top, and any rune heraldic shield.", "Agility Pyramid", AGILITY_PYRAMID, new WorldPoint(3043, 4697, 3), DOUBLE_AGENT_108, CHEER, EmoteClueItem.MYSTIC_ROBE_TOP, EmoteClueItem.ANY_HERALDIC_RUNE_SHIELD),
		new EmoteClue(Master, "Dance in Iban's temple. Beware of double agents! Equip Iban's staff, a black mystic top and a black mystic bottom.", "Iban's temple", WELL_OF_VOYAGE, new WorldPoint(2011, 4712, 0), DOUBLE_AGENT_141, DANCE, EmoteClueItem.ANY_IBANS_STAFF, EmoteClueItem.MYSTIC_ROBE_TOP_DARK, EmoteClueItem.MYSTIC_ROBE_BOTTOM_DARK),
		new EmoteClue(Elite, "Dance on the Fishing Platform. Equip barrows gloves, an amulet of glory and a dragon med helm.", "Fishing Platform", SOUTHEAST_CORNER_OF_THE_FISHING_PLATFORM, new WorldPoint(2782, 3273, 0), DANCE, EmoteClueItem.ANY_AMULET_OF_GLORY, EmoteClueItem.BARROWS_GLOVES, EmoteClueItem.DRAGON_MED_HELM),
		new EmoteClue(Master, "Flap at the death altar. Beware of double agents! Equip a death tiara, a legend's cape and any ring of wealth.", "Death altar", DEATH_ALTAR, new WorldPoint(2205, 4838, 0), DOUBLE_AGENT_141, FLAP, EmoteClueItem.ANY_RING_OF_WEALTH, EmoteClueItem.DEATH_TIARA, EmoteClueItem.CAPE_OF_LEGENDS),
		new EmoteClue(Elite, "Headbang in the Fight Arena pub. Equip a pirate bandana, a dragonstone necklace and and a magic longbow.", "Fight Arena pub", OUTSIDE_THE_BAR_BY_THE_FIGHT_ARENA, new WorldPoint(2568, 3149, 0), HEADBANG, EmoteClueItem.ANY_PIRATE_BANDANA, EmoteClueItem.DRAGON_NECKLACE, EmoteClueItem.MAGIC_LONGBOW),
		new EmoteClue(Master, "Do a jig at the barrows chest. Beware of double agents! Equip any full barrows set.", "Barrows chest", BARROWS_CHEST, new WorldPoint(3551, 9694, 0), DOUBLE_AGENT_141, JIG, EmoteClueItem.ANY_BARROWS_SET),
		new EmoteClue(Hard, "Jig at Jiggig. Beware of double agents! Equip a Rune spear, rune platelegs and any rune heraldic helm.", "Jiggig", IN_THE_MIDDLE_OF_JIGGIG, new WorldPoint(2477, 3047, 0), DOUBLE_AGENT_108, JIG, EmoteClueItem.ANY_HERALDIC_RUNE_HELM, EmoteClueItem.RUNE_SPEAR, EmoteClueItem.RUNE_PLATELEGS),
		new EmoteClue(Easy, "Cheer at the games room. Have nothing equipped at all when you do.", "Games room", null, new WorldPoint(2207, 4952, 0), CHEER, emptySlot("Nothing at all", HEAD, CAPE, AMULET, WEAPON, BODY, SHIELD, LEGS, GLOVES, BOOTS, RING, AMMO)),
		new EmoteClue(Easy, "Panic on the pier where you catch the Fishing trawler. Have nothing equipped at all when you do.", "Fishing trawler", null, new WorldPoint(2676, 3169, 0), PANIC, emptySlot("Nothing at all", HEAD, CAPE, AMULET, WEAPON, BODY, SHIELD, LEGS, GLOVES, BOOTS, RING, AMMO)),
		new EmoteClue(Hard, "Panic in the heart of the Haunted Woods. Beware of double agents! Have no items equipped when you do.", "Haunted Woods (ALQ)", null, new WorldPoint(3611, 3492, 0), DOUBLE_AGENT_108, PANIC, emptySlot("Nothing at all", HEAD, CAPE, AMULET, WEAPON, BODY, SHIELD, LEGS, GLOVES, BOOTS, RING, AMMO)),
		new EmoteClue(Master, "Show your anger towards the Statue of Saradomin in Ellamaria's garden. Beware of double agents! Equip a zamorak godsword.", "Varrock Castle", BY_THE_BEAR_CAGE_IN_VARROCK_PALACE_GARDENS, new WorldPoint(3230, 3478, 0), DOUBLE_AGENT_141, ANGRY, EmoteClueItem.ANY_ZAMORAK_GODSWORD),
		new EmoteClue(Master, "Show your anger at the Wise old man. Beware of double agents! Equip an abyssal whip, a legend's cape and some spined chaps.", "Draynor Village", BEHIND_MISS_SCHISM_IN_DRAYNOR_VILLAGE, new WorldPoint(3088, 3254, 0), DOUBLE_AGENT_141, ANGRY, EmoteClueItem.ANY_ABYSSAL_WHIP, EmoteClueItem.CAPE_OF_LEGENDS, EmoteClueItem.SPINED_CHAPS),
		new EmoteClue(Master, "Beckon by a collection of crystalline maple trees. Beware of double agents! Equip Bryophyta's staff and a nature tiara.", "North of Prifddinas", CRYSTALLINE_MAPLE_TREES, new WorldPoint(2211, 3427, 0), DOUBLE_AGENT_141, BECKON, EmoteClueItem.ANY_BRYOPHYTAS_STAFF, EmoteClueItem.NATURE_TIARA),
		new EmoteClue(Medium, "Beckon in the Digsite, near the eastern winch. Bow before you talk to me. Equip a green gnome hat, snakeskin boots and an iron pickaxe.", "Digsite", DIGSITE, new WorldPoint(3370, 3425, 0), BECKON, BOW, EmoteClueItem.GREEN_HAT, EmoteClueItem.SNAKESKIN_BOOTS, EmoteClueItem.IRON_PICKAXE),
		new EmoteClue(Medium, "Beckon in Tai Bwo Wannai. Clap before you talk to me. Equip green dragonhide chaps, a ring of dueling and a mithril medium helmet.", "Tai Bwo Wannai", SOUTH_OF_THE_SHRINE_IN_TAI_BWO_WANNAI_VILLAGE, new WorldPoint(2803, 3073, 0), BECKON, CLAP, EmoteClueItem.GREEN_DHIDE_CHAPS, EmoteClueItem.ANY_RING_OF_DUELING, EmoteClueItem.MITHRIL_MED_HELM),
		new EmoteClue(Medium, "Beckon in the combat ring of Shayzien. Show your anger before you talk to me. Equip an adamant platebody, adamant full helm and adamant platelegs.", "Shayzien combat ring", WEST_OF_THE_SHAYZIEN_COMBAT_RING, new WorldPoint(1545, 3594, 0), BECKON, ANGRY, EmoteClueItem.ADAMANT_PLATELEGS, EmoteClueItem.ADAMANT_PLATEBODY, EmoteClueItem.ADAMANT_FULL_HELM),
		new EmoteClue(Master, "Bow near Lord Iorwerth. Beware of double agents! Equip a charged crystal bow.", "Lord Iorwerth's camp", TENT_IN_LORD_IORWERTHS_ENCAMPMENT, new WorldPoint(2205, 3252, 0), DOUBLE_AGENT_141, BOW, EmoteClueItem.ANY_CRYSTAL_BOW),
		new EmoteClue(Master, "Bow in the Iorwerth Camp. Beware of double agents! Equip a charged crystal bow.", "Lord Iorwerth's camp", TENT_IN_LORD_IORWERTHS_ENCAMPMENT, new WorldPoint(2205, 3252, 0), DOUBLE_AGENT_141, BOW, EmoteClueItem.ANY_CRYSTAL_BOW),
		new EmoteClue(Easy, "Bow outside the entrance to the Legends' Guild. Equip iron platelegs, an emerald amulet and an oak longbow.", "Legend's Guild", OUTSIDE_THE_LEGENDS_GUILD_GATES, new WorldPoint(2729, 3349, 0), BOW, EmoteClueItem.IRON_PLATELEGS, EmoteClueItem.OAK_LONGBOW, EmoteClueItem.EMERALD_AMULET),
		new EmoteClue(Elite, "Bow on the ground floor of the Legend's guild. Equip Legend's cape, a dragon battleaxe and an amulet of glory.", "Legend's Guild", OUTSIDE_THE_LEGENDS_GUILD_DOOR, new WorldPoint(2728, 3377, 0), BOW, EmoteClueItem.CAPE_OF_LEGENDS, EmoteClueItem.DRAGON_BATTLEAXE, EmoteClueItem.ANY_AMULET_OF_GLORY),
		new EmoteClue(Easy, "Bow in the ticket office of the Duel Arena. Equip an iron chain body, leather chaps and coif.", "Duel Arena", MUBARIZS_ROOM_AT_THE_DUEL_ARENA, new WorldPoint(3314, 3241, 0), BOW, EmoteClueItem.IRON_CHAINBODY, EmoteClueItem.LEATHER_CHAPS, EmoteClueItem.COIF),
		new EmoteClue(Hard, "Bow at the top of the lighthouse. Beware of double agents! Equip a blue dragonhide body, blue dragonhide vambraces and no jewelry.", "Lighthouse", TOP_FLOOR_OF_THE_LIGHTHOUSE, new WorldPoint(2511, 3641, 2), DOUBLE_AGENT_108, BOW, EmoteClueItem.BLUE_DHIDE_BODY, EmoteClueItem.BLUE_DHIDE_VAMBRACES, emptySlot("No jewellery", AMULET, RING)),
		new EmoteClue(Hard, "Blow a kiss between the tables in Shilo Village bank. Beware of double agents! Equip a blue mystic hat, bone spear and rune platebody.", "Shilo Village", SHILO_VILLAGE_BANK, new WorldPoint(2851, 2954, 0), DOUBLE_AGENT_108, BLOW_KISS, EmoteClueItem.MYSTIC_HAT, EmoteClueItem.BONE_SPEAR, EmoteClueItem.RUNE_PLATEBODY),
		new EmoteClue(Elite, "Blow a kiss in the heart of the lava maze. Equip black dragonhide chaps, a spotted cape and a rolling pin.", "Lava maze", NEAR_A_LADDER_IN_THE_WILDERNESS_LAVA_MAZE, new WorldPoint(3069, 3861, 0), BLOW_KISS, EmoteClueItem.BLACK_DHIDE_CHAPS, EmoteClueItem.ANY_SPOTTED_CAPE, EmoteClueItem.ROLLING_PIN),
		new EmoteClue(Master, "Blow a kiss outside K'ril Tsutsaroth's chamber. Beware of double agents! Equip a zamorak full helm and the shadow sword.", "K'ril's chamber", OUTSIDE_KRIL_TSUTSAROTHS_ROOM, new WorldPoint(2925, 5333, 2), DOUBLE_AGENT_141, BLOW_KISS, EmoteClueItem.ZAMORAK_FULL_HELM, EmoteClueItem.SHADOW_SWORD),
		new EmoteClue(Easy, "Cheer at the Druids' Circle. Equip a blue wizard hat, a bronze two-handed sword and HAM boots.", "Taverley stone circle", TAVERLEY_STONE_CIRCLE, new WorldPoint(2924, 3478, 0), CHEER, EmoteClueItem.BLUE_WIZARD_HAT, EmoteClueItem.BRONZE_2H_SWORD, EmoteClueItem.HAM_BOOTS),
		new EmoteClue(Medium, "Cheer in the Edgeville general store. Dance before you talk to me. Equip a brown apron, leather boots and leather gloves.", "Edgeville", NORTH_OF_EVIL_DAVES_HOUSE_IN_EDGEVILLE, new WorldPoint(3080, 3509, 0), CHEER, DANCE, EmoteClueItem.BROWN_APRON, EmoteClueItem.LEATHER_BOOTS, EmoteClueItem.LEATHER_GLOVES),
		new EmoteClue(Medium, "Cheer in the Ogre Pen in the Training Camp. Show you are angry before you talk to me. Equip a green dragonhide body and chaps and a steel square shield.", "King Lathas' camp", OGRE_CAGE_IN_KING_LATHAS_TRAINING_CAMP, new WorldPoint(2527, 3375, 0), CHEER, ANGRY, EmoteClueItem.GREEN_DHIDE_BODY, EmoteClueItem.GREEN_DHIDE_CHAPS, EmoteClueItem.STEEL_SQ_SHIELD),
		new EmoteClue(Master, "Cheer in the Entrana church. Beware of double agents! Equip a full set of black dragonhide armour.", "Entrana church", ENTRANA_CHAPEL, new WorldPoint(2852, 3349, 0), DOUBLE_AGENT_141, CHEER, EmoteClueItem.BLACK_DHIDE_VAMBRACES, EmoteClueItem.BLACK_DHIDE_CHAPS, EmoteClueItem.BLACK_DHIDE_BODY),
		new EmoteClue(Easy, "Cheer for the monks at Port Sarim. Equip a coif, steel plateskirt and a sapphire necklace.", "Port Sarim", NEAR_THE_ENTRANA_FERRY_IN_PORT_SARIM, new WorldPoint(3047, 3237, 0), CHEER, EmoteClueItem.COIF, EmoteClueItem.STEEL_PLATESKIRT, EmoteClueItem.SAPPHIRE_NECKLACE),
		new EmoteClue(Easy, "Clap in the main exam room in the Exam Centre. Equip a white apron, green gnome boots and leather gloves.", "Exam Centre", OUTSIDE_THE_DIGSITE_EXAM_CENTRE, new WorldPoint(3361, 3339, 0), CLAP, EmoteClueItem.WHITE_APRON, EmoteClueItem.GREEN_BOOTS, EmoteClueItem.LEATHER_GLOVES),
		new EmoteClue(Easy, "Clap on the causeway to the Wizards' Tower. Equip an iron medium helmet, emerald ring and a white apron.", "Wizards' Tower", ON_THE_BRIDGE_TO_THE_MISTHALIN_WIZARDS_TOWER, new WorldPoint(3113, 3196, 0), CLAP, EmoteClueItem.IRON_MED_HELM, EmoteClueItem.EMERALD_RING, EmoteClueItem.WHITE_APRON),
		new EmoteClue(Easy, "Clap on the top level of the mill, north of East Ardougne. Equip a blue gnome robe top, HAM robe bottom and an unenchanted tiara.", "East Ardougne", UPSTAIRS_IN_THE_ARDOUGNE_WINDMILL, new WorldPoint(2635, 3385, 3), CLAP, EmoteClueItem.BLUE_ROBE_TOP, EmoteClueItem.HAM_ROBE, EmoteClueItem.TIARA),
		new EmoteClue(Medium, "Clap in Seers court house. Spin before you talk to me. Equip an adamant halberd, blue mystic robe bottom and a diamond ring.", "Seers Village", OUTSIDE_THE_SEERS_VILLAGE_COURTHOUSE, new WorldPoint(2735, 3469, 0), CLAP, SPIN, EmoteClueItem.ADAMANT_HALBERD, EmoteClueItem.MYSTIC_ROBE_BOTTOM, EmoteClueItem.MYSTIC_ROBE_BOTTOM, EmoteClueItem.DIAMOND_RING),
		new EmoteClue(Master, "Clap in the magic axe hut. Beware of double agents! Equip only some flared trousers.", "Magic axe hut", OUTSIDE_THE_WILDERNESS_AXE_HUT, new WorldPoint(3191, 3960, 0), DOUBLE_AGENT_141, CLAP, EmoteClueItem.FLARED_TROUSERS, EmoteClueItem.LOCKPICK, emptySlot("Nothing else", HEAD, CAPE, AMULET, WEAPON, BODY, SHIELD, GLOVES, BOOTS, RING, AMMO)),
		new EmoteClue(Medium, "Clap your hands north of Mount Karuulm Spin before you talk to me. Equip an adamant warhammer, a ring of life and a pair of mithril boots.", "Mount Karuulm", NORTH_OF_MOUNT_KARUULM, new WorldPoint(1306, 3839, 0), CLAP, SPIN, EmoteClueItem.ADAMANT_WARHAMMER, EmoteClueItem.RING_OF_LIFE, EmoteClueItem.MITHRIL_BOOTS),
		new EmoteClue(Medium, "Cry in the Catherby Ranging shop. Bow before you talk to me. Equip blue gnome boots, a hard leather body and an unblessed silver sickle.", "Catherby", HICKTONS_ARCHERY_EMPORIUM, new WorldPoint(2823, 3443, 0), CRY, BOW, EmoteClueItem.BLUE_BOOTS, EmoteClueItem.HARDLEATHER_BODY, EmoteClueItem.SILVER_SICKLE),
		new EmoteClue(Medium, "Cry on the shore of Catherby beach. Laugh before you talk to me, equip an adamant sq shield, a bone dagger and mithril platebody.", "Catherby", OUTSIDE_HARRYS_FISHING_SHOP_IN_CATHERBY, new WorldPoint(2852, 3429, 0), CRY, LAUGH, EmoteClueItem.ADAMANT_SQ_SHIELD, EmoteClueItem.BONE_DAGGER, EmoteClueItem.MITHRIL_PLATEBODY),
		new EmoteClue(Medium, "Cry on top of the western tree in the Gnome Agility Arena. Indicate 'no' before you talk to me. Equip a steel kiteshield, ring of forging and green dragonhide chaps.", "Gnome Stronghold", GNOME_STRONGHOLD_BALANCING_ROPE, new WorldPoint(2473, 3420, 2), CRY, NO, EmoteClueItem.STEEL_KITESHIELD, EmoteClueItem.RING_OF_FORGING, EmoteClueItem.GREEN_DHIDE_CHAPS),
		new EmoteClue(Master, "Cry in the TzHaar gem store. Beware of double agents! Equip a fire cape and TokTz-Xil-Ul.", "Tzhaar gem store", TZHAAR_GEM_STORE, new WorldPoint(2463, 5149, 0), DOUBLE_AGENT_141, CRY, EmoteClueItem.ANY_FIRE_CAPE, EmoteClueItem.TOKTZXILUL),
		new EmoteClue(Medium, "Cry in the Draynor Village jail. Jump for joy before you talk to me. Equip an adamant sword, a sapphire amulet and an adamant plateskirt.", "Draynor Village jail", OUTSIDE_DRAYNOR_VILLAGE_JAIL, new WorldPoint(3128, 3245, 0), CRY, JUMP_FOR_JOY, EmoteClueItem.ADAMANT_SWORD, EmoteClueItem.SAPPHIRE_AMULET, EmoteClueItem.ADAMANT_PLATESKIRT),
		new EmoteClue(Easy, "Dance at the crossroads north of Draynor. Equip an iron chain body, a sapphire ring and a longbow.", "Draynor Village", CROSSROADS_NORTH_OF_DRAYNOR_VILLAGE, new WorldPoint(3109, 3294, 0), DANCE, EmoteClueItem.IRON_CHAINBODY, EmoteClueItem.SAPPHIRE_RING, EmoteClueItem.LONGBOW),
		new EmoteClue(Easy, "Dance in the Party Room. Equip a steel full helmet, steel platebody and an iron plateskirt.", "Falador Party Room", OUTSIDE_THE_FALADOR_PARTY_ROOM, new WorldPoint(3045, 3376, 0), DANCE, EmoteClueItem.STEEL_FULL_HELM, EmoteClueItem.STEEL_PLATEBODY, EmoteClueItem.IRON_PLATESKIRT),
		new EmoteClue(Easy, "Dance in the shack in Lumbridge Swamp. Equip a bronze dagger, iron full helmet and a gold ring.", "Lumbridge swamp", NEAR_A_SHED_IN_LUMBRIDGE_SWAMP, new WorldPoint(3203, 3169, 0), DANCE, EmoteClueItem.BRONZE_DAGGER, EmoteClueItem.IRON_FULL_HELM, EmoteClueItem.GOLD_RING),
		new EmoteClue(Medium, "Dance in the dark caves beneath Lumbridge Swamp. Blow a kiss before you talk to me. Equip an air staff, Bronze full helm and an amulet of power.", "Lumbridge swamp caves", LUMBRIDGE_SWAMP_CAVES, new WorldPoint(3168, 9571, 0), DANCE, BLOW_KISS, Varbits.FIRE_PIT_LUMBRIDGE_SWAMP, EmoteClueItem.STAFF_OF_AIR, EmoteClueItem.BRONZE_FULL_HELM, EmoteClueItem.AMULET_OF_POWER),
		new EmoteClue(Hard, "Dance at the cat-doored pyramid in Sophanem. Beware of double agents! Equip a ring of life, an uncharged amulet of glory and an adamant two-handed sword.", "Pyramid Of Sophanem", OUTSIDE_THE_GREAT_PYRAMID_OF_SOPHANEM, new WorldPoint(3294, 2781, 0), DOUBLE_AGENT_108, DANCE, EmoteClueItem.RING_OF_LIFE, EmoteClueItem.AMULET_OF_GLORY, EmoteClueItem.ADAMANT_2H_SWORD),
		new EmoteClue(Medium, "Dance in the centre of Canifis. Bow before you talk to me. Equip a green gnome robe top, mithril plate legs and an iron two-handed sword.", "Canifis", CENTRE_OF_CANIFIS, new WorldPoint(3492, 3488, 0), DANCE, BOW, EmoteClueItem.GREEN_ROBE_TOP, EmoteClueItem.MITHRIL_PLATELEGS, EmoteClueItem.IRON_2H_SWORD),
		new EmoteClue(Master, "Dance in the King Black Dragon's lair. Beware of double agents! Equip a black dragonhide body, black dragonhide vambraces and a black dragon mask.", "King black dragon's lair", KING_BLACK_DRAGONS_LAIR, new WorldPoint(2271, 4680, 0), DOUBLE_AGENT_141, DANCE, EmoteClueItem.BLACK_DHIDE_BODY, EmoteClueItem.BLACK_DHIDE_VAMBRACES, EmoteClueItem.BLACK_DRAGON_MASK),
		new EmoteClue(Easy, "Dance at the entrance to the Grand Exchange. Equip a pink skirt, pink robe top and a body tiara.", "Grand Exchange", SOUTH_OF_THE_GRAND_EXCHANGE, new WorldPoint(3165, 3467, 0), DANCE, EmoteClueItem.PINK_SKIRT, EmoteClueItem.PINK_ROBE_TOP, EmoteClueItem.BODY_TIARA),
		new EmoteClue(Master, "Goblin Salute in the Goblin Village. Beware of double agents! Equip a bandos godsword, a bandos cloak and a bandos platebody.", "Goblin Village", OUTSIDE_MUDKNUCKLES_HUT, new WorldPoint(2956, 3505, 0), DOUBLE_AGENT_141, GOBLIN_SALUTE, EmoteClueItem.BANDOS_PLATEBODY, EmoteClueItem.BANDOS_CLOAK, EmoteClueItem.ANY_BANDOS_GODSWORD),
		new EmoteClue(Easy, "Headbang in the mine north of Al Kharid. Equip a desert shirt, leather gloves and leather boots.", "Al Kharid mine", AL_KHARID_SCORPION_MINE, new WorldPoint(3299, 3289, 0), HEADBANG, EmoteClueItem.DESERT_SHIRT, EmoteClueItem.LEATHER_GLOVES, EmoteClueItem.LEATHER_BOOTS),
		new EmoteClue(Hard, "Headbang at the exam centre. Beware of double agents! Equip a mystic fire staff, a diamond bracelet and rune boots.", "Exam Centre", INSIDE_THE_DIGSITE_EXAM_CENTRE, new WorldPoint(3362, 3340, 0), DOUBLE_AGENT_108, HEADBANG, EmoteClueItem.MYSTIC_FIRE_STAFF, EmoteClueItem.DIAMOND_BRACELET, EmoteClueItem.RUNE_BOOTS),
		new EmoteClue(Elite, "Headbang at the top of Slayer Tower. Equip a seercull, a combat bracelet and helm of Neitiznot.", "Slayer Tower", OUTSIDE_THE_SLAYER_TOWER_GARGOYLE_ROOM, new WorldPoint(3421, 3537, 2), HEADBANG, EmoteClueItem.SEERCULL, EmoteClueItem.ANY_COMBAT_BRACELET, EmoteClueItem.HELM_OF_NEITIZNOT),
		new EmoteClue(Easy, "Dance a jig by the entrance to the Fishing Guild. Equip an emerald ring, a sapphire amulet, and a bronze chain body.", "Fishing Guild", OUTSIDE_THE_FISHING_GUILD, new WorldPoint(2610, 3391, 0), JIG, EmoteClueItem.EMERALD_RING, EmoteClueItem.SAPPHIRE_AMULET, EmoteClueItem.BRONZE_CHAINBODY),
		new EmoteClue(Medium, "Dance a jig under Shantay's Awning. Bow before you talk to me. Equip a pointed blue snail helmet, an air staff and a bronze square shield.", "Shantay Pass", SHANTAY_PASS, new WorldPoint(3304, 3124, 0), JIG, BOW, EmoteClueItem.BRUISE_BLUE_SNELM_POINTED, EmoteClueItem.STAFF_OF_AIR, EmoteClueItem.BRONZE_SQ_SHIELD),
		new EmoteClue(Easy, "Do a jig in Varrock's rune store. Equip an air tiara and a staff of water.", "Varrock rune store", AUBURYS_SHOP_IN_VARROCK, new WorldPoint(3253, 3401, 0), JIG, EmoteClueItem.AIR_TIARA, EmoteClueItem.STAFF_OF_WATER),
		new EmoteClue(Easy, "Jump for joy at the beehives. Equip a desert shirt, green gnome robe bottoms and a steel axe.", "Catherby", CATHERBY_BEEHIVE_FIELD, new WorldPoint(2759, 3445, 0), JUMP_FOR_JOY, EmoteClueItem.DESERT_SHIRT, EmoteClueItem.GREEN_ROBE_BOTTOMS, EmoteClueItem.STEEL_AXE),
		new EmoteClue(Medium, "Jump for joy in Yanille bank. Dance a jig before you talk to me. Equip a brown apron, adamantite medium helmet and snakeskin chaps.", "Yanille", OUTSIDE_YANILLE_BANK, new WorldPoint(2610, 3092, 0), JUMP_FOR_JOY, JIG, EmoteClueItem.BROWN_APRON, EmoteClueItem.ADAMANT_MED_HELM, EmoteClueItem.SNAKESKIN_CHAPS),
		new EmoteClue(Medium, "Jump for joy in the TzHaar sword shop. Shrug before you talk to me. Equip a Steel longsword, Blue D'hide body and blue mystic gloves.", "Tzhaar weapon store", TZHAAR_WEAPONS_STORE, new WorldPoint(2477, 5146, 0), JUMP_FOR_JOY, SHRUG, EmoteClueItem.STEEL_LONGSWORD, EmoteClueItem.BLUE_DHIDE_BODY, EmoteClueItem.MYSTIC_GLOVES),
		new EmoteClue(Elite, "Jump for joy in the Ancient Cavern. Equip a granite shield, splitbark body and any rune heraldic helm.", "Ancient cavern", ENTRANCE_OF_THE_CAVERN_UNDER_THE_WHIRLPOOL, new WorldPoint(1768, 5366, 1), JUMP_FOR_JOY, EmoteClueItem.GRANITE_SHIELD, EmoteClueItem.SPLITBARK_BODY, EmoteClueItem.ANY_HERALDIC_RUNE_HELM),
		new EmoteClue(Elite, "Jump for joy at the Neitiznot rune rock. Equip Rune boots, a proselyte hauberk and a dragonstone ring.", "Fremennik Isles", NEAR_A_RUNITE_ROCK_IN_THE_FREMENNIK_ISLES, new WorldPoint(2375, 3850, 0), JUMP_FOR_JOY, EmoteClueItem.RUNE_BOOTS, EmoteClueItem.PROSELYTE_HAUBERK, EmoteClueItem.DRAGONSTONE_RING),
		new EmoteClue(Master, "Jump for joy in the centre of Zul-Andra. Beware of double agents! Equip a dragon 2h sword, bandos boots and an obsidian cape.", "Zul-Andra", NEAR_THE_PIER_IN_ZULANDRA, new WorldPoint(2199, 3056, 0), DOUBLE_AGENT_141, JUMP_FOR_JOY, EmoteClueItem.DRAGON_2H_SWORD, EmoteClueItem.BANDOS_BOOTS, EmoteClueItem.OBSIDIAN_CAPE),
		new EmoteClue(Elite, "Laugh by the fountain of heroes. Equip splitbark legs, dragon boots and a Rune longsword.", "Fountain of heroes", FOUNTAIN_OF_HEROES, new WorldPoint(2920, 9893, 0), LAUGH, EmoteClueItem.SPLITBARK_LEGS, EmoteClueItem.ANY_DRAGON_BOOTS, EmoteClueItem.RUNE_LONGSWORD),
		new EmoteClue(Hard, "Laugh in Jokul's tent in the Mountain Camp. Beware of double agents! Equip a rune full helmet, blue dragonhide chaps and a fire battlestaff.", "Mountain Camp", MOUNTAIN_CAMP_GOAT_ENCLOSURE, new WorldPoint(2812, 3681, 0), DOUBLE_AGENT_108, LAUGH, EmoteClueItem.RUNE_FULL_HELM, EmoteClueItem.BLUE_DHIDE_CHAPS, EmoteClueItem.FIRE_BATTLESTAFF),
		new EmoteClue(Easy, "Laugh at the crossroads south of the Sinclair Mansion. Equip a cowl, a blue wizard robe top and an iron scimitar.", "Sinclair Mansion", ROAD_JUNCTION_SOUTH_OF_SINCLAIR_MANSION, new WorldPoint(2741, 3536, 0), LAUGH, EmoteClueItem.LEATHER_COWL, EmoteClueItem.BLUE_WIZARD_ROBE, EmoteClueItem.IRON_SCIMITAR),
		new EmoteClue(Elite, "Laugh in front of the gem store in Ardougne market. Equip a Castlewars bracelet, a dragonstone amulet and a ring of forging.", "Ardougne", NEAR_THE_GEM_STALL_IN_ARDOUGNE_MARKET, new WorldPoint(2666, 3304, 0), LAUGH, EmoteClueItem.ANY_CASTLE_WARS_BRACELET, EmoteClueItem.DRAGONSTONE_AMULET, EmoteClueItem.RING_OF_FORGING),
		new EmoteClue(Easy, "Panic in the Limestone Mine. Equip bronze platelegs, a steel pickaxe and a steel medium helmet.", "Limestone Mine", LIMESTONE_MINE, new WorldPoint(3372, 3498, 0), PANIC, EmoteClueItem.BRONZE_PLATELEGS, EmoteClueItem.STEEL_PICKAXE, EmoteClueItem.STEEL_MED_HELM),
		new EmoteClue(Medium, "Panic by the mausoleum in Morytania. Wave before you speak to me. Equip a mithril plate skirt, a maple longbow and no boots.", "Morytania mausoleum, access via the experiments cave", MAUSOLEUM_OFF_THE_MORYTANIA_COAST, new WorldPoint(3504, 3576, 0), PANIC, WAVE, EmoteClueItem.MITHRIL_PLATESKIRT, EmoteClueItem.MAPLE_LONGBOW, emptySlot("No boots", BOOTS)),
		new EmoteClue(Hard, "Panic on the Wilderness volcano bridge. Beware of double agents! Equip any headband and crozier.", "Wilderness volcano", VOLCANO_IN_THE_NORTHEASTERN_WILDERNESS, new WorldPoint(3368, 3935, 0), DOUBLE_AGENT_65, PANIC, EmoteClueItem.ANY_HEADBAND, EmoteClueItem.ANY_CROZIER),
		new EmoteClue(Hard, "Panic by the pilot on White Wolf Mountain. Beware of double agents! Equip mithril platelegs, a ring of life and a rune axe.", "White Wolf Mountain", GNOME_GLIDER_ON_WHITE_WOLF_MOUNTAIN, new WorldPoint(2847, 3499, 0), DOUBLE_AGENT_108, PANIC, EmoteClueItem.MITHRIL_PLATELEGS, EmoteClueItem.RING_OF_LIFE, EmoteClueItem.RUNE_AXE),
		new EmoteClue(Master, "Panic by the big egg where no one dare goes and the ground is burnt. Beware of double agents! Equip a dragon med helm, a TokTz-Ket-Xil, a brine sabre, rune platebody and an uncharged amulet of glory.", "Lava dragon isle", SOUTHEAST_CORNER_OF_LAVA_DRAGON_ISLE, new WorldPoint(3227, 3831, 0), DOUBLE_AGENT_141, PANIC, EmoteClueItem.DRAGON_MED_HELM, EmoteClueItem.TOKTZKETXIL, EmoteClueItem.BRINE_SABRE, EmoteClueItem.RUNE_PLATEBODY, EmoteClueItem.AMULET_OF_GLORY),
		new EmoteClue(Elite, "Panic at the area flowers meet snow. Equip Blue D'hide vambraces, a dragon spear and a rune plateskirt.", "Trollweiss mountain", HALFWAY_DOWN_TROLLWEISS_MOUNTAIN, new WorldPoint(2776, 3781, 0), PANIC, EmoteClueItem.BLUE_DHIDE_VAMBRACES, EmoteClueItem.DRAGON_SPEAR, EmoteClueItem.RUNE_PLATESKIRT, EmoteClueItem.SLED_4084),
		new EmoteClue(Master, "Do a push up at the bank of the Warrior's guild. Beware of double agents! Equip a dragon battleaxe, a dragon defender and a slayer helm of any kind.", "Warriors' guild", WARRIORS_GUILD_BANK_29047, new WorldPoint(2843, 3543, 0), DOUBLE_AGENT_141, PUSH_UP, EmoteClueItem.DRAGON_BATTLEAXE, EmoteClueItem.ANY_DRAGON_DEFENDER, EmoteClueItem.ANY_SLAYER_HELMET),
		new EmoteClue(Master, "Blow a raspberry in the bank of the Warriors' Guild. Beware of double agents! Equip a dragon battleaxe, a slayer helm of any kind and a dragon defender or avernic defender.", "Warriors' guild", WARRIORS_GUILD_BANK_29047, new WorldPoint(2843, 3543, 0), DOUBLE_AGENT_141, RASPBERRY, EmoteClueItem.DRAGON_BATTLEAXE, EmoteClueItem.ANY_DRAGON_OR_AVERNIC_DEFENDER, EmoteClueItem.ANY_SLAYER_HELMET),
		new EmoteClue(Easy, "Blow a raspberry at the monkey cage in Ardougne Zoo. Equip a studded leather body, bronze platelegs and a normal staff with no orb.", "Ardougne Zoo", NEAR_THE_PARROTS_IN_ARDOUGNE_ZOO, new WorldPoint(2607, 3282, 0), RASPBERRY, EmoteClueItem.STUDDED_BODY, EmoteClueItem.BRONZE_PLATELEGS, EmoteClueItem.STAFF),
		new EmoteClue(Easy, "Blow raspberries outside the entrance to Keep Le Faye. Equip a coif, an iron platebody and leather gloves.", "Keep Le Faye", OUTSIDE_KEEP_LE_FAYE, new WorldPoint(2757, 3401, 0), RASPBERRY, EmoteClueItem.COIF, EmoteClueItem.IRON_PLATEBODY, EmoteClueItem.LEATHER_GLOVES),
		new EmoteClue(Hard, "Blow a raspberry in the Fishing Guild bank. Beware of double agents! Equip an elemental shield, blue dragonhide chaps and a rune warhammer.", "Fishing Guild", FISHING_GUILD_BANK, new WorldPoint(2588, 3419, 0), DOUBLE_AGENT_108, RASPBERRY, EmoteClueItem.ELEMENTAL_SHIELD, EmoteClueItem.BLUE_DHIDE_CHAPS, EmoteClueItem.RUNE_WARHAMMER),
		new EmoteClue(Hard, "Salute in the banana plantation. Beware of double agents! Equip a diamond ring, amulet of power, and nothing on your chest and legs.", "Karamja", WEST_SIDE_OF_THE_KARAMJA_BANANA_PLANTATION, new WorldPoint(2914, 3168, 0), DOUBLE_AGENT_108, SALUTE, EmoteClueItem.DIAMOND_RING, EmoteClueItem.AMULET_OF_POWER, emptySlot("Nothing on chest & legs", BODY, LEGS)),
		new EmoteClue(Elite, "Salute in the Warriors' guild bank. Equip only a black salamander.", "Warriors' guild", WARRIORS_GUILD_BANK, new WorldPoint(2844, 3542, 0), SALUTE, EmoteClueItem.BLACK_SALAMANDER, emptySlot("Nothing else", HEAD, CAPE, AMULET, BODY, SHIELD, LEGS, GLOVES, BOOTS, RING, AMMO)),
		new EmoteClue(Hard, "Salute in the centre of the mess hall. Beware of double agents! Equip a rune halberd rune platebody, and an amulet of strength.", "Hosidius mess hall", HOSIDIUS_MESS, new WorldPoint(1646, 3631, 0), DOUBLE_AGENT_108, SALUTE, EmoteClueItem.RUNE_HALBERD, EmoteClueItem.RUNE_PLATEBODY, EmoteClueItem.AMULET_OF_STRENGTH),
		new EmoteClue(Easy, "Shrug in the mine near Rimmington. Equip a gold necklace, a gold ring and a bronze spear.", "Rimmington mine", RIMMINGTON_MINE, new WorldPoint(2976, 3238, 0), SHRUG, EmoteClueItem.GOLD_NECKLACE, EmoteClueItem.GOLD_RING, EmoteClueItem.BRONZE_SPEAR),
		new EmoteClue(Medium, "Shrug in Catherby bank. Yawn before you talk to me. Equip a maple longbow, green d'hide chaps and an iron med helm.", "Catherby", OUTSIDE_CATHERBY_BANK, new WorldPoint(2808, 3440, 0), SHRUG, YAWN, EmoteClueItem.MAPLE_LONGBOW, EmoteClueItem.GREEN_DHIDE_CHAPS, EmoteClueItem.IRON_MED_HELM),
		new EmoteClue(Hard, "Shrug in the Zamorak temple found in the Eastern Wilderness. Beware of double agents! Equip rune platelegs, an iron platebody and blue dragonhide vambraces.", "Chaos temple", CHAOS_TEMPLE_IN_THE_SOUTHEASTERN_WILDERNESS, new WorldPoint(3239, 3611, 0), DOUBLE_AGENT_65, SHRUG, EmoteClueItem.RUNE_PLATELEGS, EmoteClueItem.IRON_PLATEBODY, EmoteClueItem.BLUE_DHIDE_VAMBRACES),
		new EmoteClue(Elite, "Shrug in the Shayzien command tent. Equip a blue mystic robe bottom, a rune kiteshield and any bob shirt.", "Shayzien command tent", SHAYZIEN_WAR_TENT, new WorldPoint(1555, 3537, 0), SHRUG, EmoteClueItem.MYSTIC_ROBE_BOTTOM, EmoteClueItem.RUNE_KITESHIELD, EmoteClueItem.ANY_BOBS_SHIRT),
		new EmoteClue(Master, "Slap your head in the centre of the Kourend catacombs. Beware of double agents! Equip the arclight and the amulet of the damned.", "Kourend catacombs", CENTRE_OF_THE_CATACOMBS_OF_KOUREND, new WorldPoint(1663, 10045, 0), DOUBLE_AGENT_141, SLAP_HEAD, EmoteClueItem.ARCLIGHT, EmoteClueItem.ANY_AMULET_OF_THE_DAMNED),
		new EmoteClue(Easy, "Spin at the crossroads north of Rimmington. Equip a green gnome hat, cream gnome top and leather chaps.", "Rimmington", ROAD_JUNCTION_NORTH_OF_RIMMINGTON, new WorldPoint(2981, 3276, 0), SPIN, EmoteClueItem.GREEN_HAT, EmoteClueItem.CREAM_ROBE_TOP, EmoteClueItem.LEATHER_CHAPS),
		new EmoteClue(Easy, "Spin in Draynor Manor by the fountain. Equip an iron platebody, studded leather chaps and a bronze full helmet.", "Draynor Manor", DRAYNOR_MANOR_BY_THE_FOUNTAIN, new WorldPoint(3088, 3336, 0), SPIN, EmoteClueItem.IRON_PLATEBODY, EmoteClueItem.STUDDED_CHAPS, EmoteClueItem.BRONZE_FULL_HELM),
		new EmoteClue(Master, "Spin in front of the Soul altar. Beware of double agents! Equip a dragon pickaxe, helm of neitiznot and a pair of rune boots.", "Soul altar", SOUL_ALTAR, new WorldPoint(1815, 3856, 0), DOUBLE_AGENT_141, SPIN, EmoteClueItem.ANY_DRAGON_OR_CRYSTAL_PICKAXE, EmoteClueItem.HELM_OF_NEITIZNOT, EmoteClueItem.RUNE_BOOTS),
		new EmoteClue(Easy, "Spin in the Varrock Castle courtyard. Equip a black axe, a coif and a ruby ring.", "Varrock Castle", OUTSIDE_VARROCK_PALACE_COURTYARD, new WorldPoint(3213, 3463, 0), SPIN, EmoteClueItem.BLACK_AXE, EmoteClueItem.COIF, EmoteClueItem.RUBY_RING),
		new EmoteClue(Elite, "Spin in West Ardougne Church. Equip a dragon spear and red dragonhide chaps.", "West Ardougne Church", CHAPEL_IN_WEST_ARDOUGNE, new WorldPoint(2530, 3290, 0), SPIN, EmoteClueItem.DRAGON_SPEAR, EmoteClueItem.RED_DHIDE_CHAPS),
		new EmoteClue(Medium, "Spin on the bridge by the Barbarian Village. Salute before you talk to me. Equip purple gloves, a steel kiteshield and a mithril full helmet.", "Barbarian Village", EAST_OF_THE_BARBARIAN_VILLAGE_BRIDGE, new WorldPoint(3105, 3420, 0), SPIN, SALUTE, EmoteClueItem.PURPLE_GLOVES, EmoteClueItem.STEEL_KITESHIELD, EmoteClueItem.MITHRIL_FULL_HELM),
		new EmoteClue(Master, "Stamp in the Enchanted valley west of the waterfall. Beware of double agents! Equip a dragon axe.", "Enchanted Valley (BKQ)", NORTHWESTERN_CORNER_OF_THE_ENCHANTED_VALLEY, new WorldPoint(3030, 4522, 0), DOUBLE_AGENT_141, STAMP, EmoteClueItem.ANY_DRAGON_OR_CRYSTAL_AXE),
		new EmoteClue(Easy, "Think in middle of the wheat field by the Lumbridge mill. Equip a blue gnome robetop, a turquoise gnome robe bottom and an oak shortbow.", "Lumbridge mill", WHEAT_FIELD_NEAR_THE_LUMBRIDGE_WINDMILL, new WorldPoint(3159, 3298, 0), THINK, EmoteClueItem.BLUE_ROBE_TOP, EmoteClueItem.TURQUOISE_ROBE_BOTTOMS, EmoteClueItem.OAK_SHORTBOW),
		new EmoteClue(Medium, "Think in the centre of the Observatory. Spin before you talk to me. Equip a mithril chain body, green dragonhide chaps and a ruby amulet.", "Observatory", OBSERVATORY, new WorldPoint(2439, 3161, 0), THINK, SPIN, EmoteClueItem.MITHRIL_CHAINBODY, EmoteClueItem.GREEN_DHIDE_CHAPS, EmoteClueItem.RUBY_AMULET),
		new EmoteClue(Easy, "Wave along the south fence of the Lumber Yard. Equip a hard leather body, leather chaps and a bronze axe.", "Lumber Yard", NEAR_THE_SAWMILL_OPERATORS_BOOTH, new WorldPoint(3307, 3491, 0), WAVE, EmoteClueItem.HARDLEATHER_BODY, EmoteClueItem.LEATHER_CHAPS, EmoteClueItem.BRONZE_AXE),
		new EmoteClue(Easy, "Wave in the Falador gem store. Equip a Mithril pickaxe, Black platebody and an Iron Kiteshield.", "Falador", NEAR_HERQUINS_SHOP_IN_FALADOR, new WorldPoint(2945, 3335, 0), WAVE, EmoteClueItem.MITHRIL_PICKAXE, EmoteClueItem.BLACK_PLATEBODY, EmoteClueItem.IRON_KITESHIELD),
		new EmoteClue(Easy, "Wave on Mudskipper Point. Equip a black cape, leather chaps and a steel mace.", "Mudskipper Point", MUDSKIPPER_POINT, new WorldPoint(2989, 3110, 0), WAVE, EmoteClueItem.BLACK_CAPE, EmoteClueItem.LEATHER_CHAPS, EmoteClueItem.STEEL_MACE),
		new EmoteClue(Master, "Wave on the northern wall of Castle Drakan. Beware of double agents! Wear a dragon sq shield, splitbark body and any boater.", "Castle Drakan", NORTHERN_WALL_OF_CASTLE_DRAKAN, new WorldPoint(3562, 3379, 0), DOUBLE_AGENT_141, WAVE, EmoteClueItem.ANY_DRAGON_SQ_SHIELD, EmoteClueItem.SPLITBARK_BODY, EmoteClueItem.ANY_BOATER),
		new EmoteClue(Master, "Yawn in the 7th room of Pyramid Plunder. Beware of double agents! Equip a pharaoh sceptre and a full set of menaphite robes.", "Pyramid Plunder", _7TH_CHAMBER_OF_JALSAVRAH, new WorldPoint(1944, 4427, 0), DOUBLE_AGENT_141, YAWN, EmoteClueItem.ANY_PHARAOHS_SCEPTRE, EmoteClueItem.ANY_MENAPHITE_SET),
		new EmoteClue(Easy, "Yawn in the Varrock library. Equip a green gnome robe top, HAM robe bottom and an iron warhammer.", "Varrock Castle", VARROCK_PALACE_LIBRARY, new WorldPoint(3209, 3492, 0), YAWN, EmoteClueItem.GREEN_ROBE_TOP, EmoteClueItem.HAM_ROBE, EmoteClueItem.IRON_WARHAMMER),
		new EmoteClue(Easy, "Yawn in Draynor Marketplace. Equip studded leather chaps, an iron kiteshield and a steel longsword.", "Draynor", DRAYNOR_VILLAGE_MARKET, new WorldPoint(3083, 3253, 0), YAWN, EmoteClueItem.STUDDED_CHAPS, EmoteClueItem.IRON_KITESHIELD, EmoteClueItem.STEEL_LONGSWORD),
		new EmoteClue(Medium, "Yawn in the Castle Wars lobby. Shrug before you talk to me. Equip a ruby amulet, a mithril scimitar and a Wilderness cape.", "Castle Wars", CASTLE_WARS_BANK, new WorldPoint(2440, 3092, 0), YAWN, SHRUG, EmoteClueItem.RUBY_AMULET, EmoteClueItem.MITHRIL_SCIMITAR, EmoteClueItem.ANY_TEAM_CAPE),
		new EmoteClue(Hard, "Yawn in the rogues' general store. Beware of double agents! Equip an adamant square shield, blue dragon vambraces and a rune pickaxe.", "Rogues general store", NOTERAZZOS_SHOP_IN_THE_WILDERNESS, new WorldPoint(3026, 3701, 0), DOUBLE_AGENT_65, YAWN, EmoteClueItem.ADAMANT_SQ_SHIELD, EmoteClueItem.BLUE_DHIDE_VAMBRACES, EmoteClueItem.RUNE_PICKAXE),
		new EmoteClue(Elite, "Yawn at the top of Trollheim. Equip a lava battlestaff, black dragonhide vambraces and a mind shield.", "Trollheim Mountain", ON_TOP_OF_TROLLHEIM_MOUNTAIN, new WorldPoint(2887, 3676, 0), YAWN, EmoteClueItem.ANY_LAVA_BATTLESTAFF, EmoteClueItem.BLACK_DHIDE_VAMBRACES, EmoteClueItem.MIND_SHIELD),
		new EmoteClue(Medium, "Yawn in the centre of Arceuus library. Nod your head before you talk to me. Equip blue dragonhide vambraces, adamant boots and an adamant dagger.", "Arceuus library", ENTRANCE_OF_THE_ARCEUUS_LIBRARY, new WorldPoint(1632, 3807, 0), YAWN, YES, EmoteClueItem.BLUE_DHIDE_VAMBRACES, EmoteClueItem.ADAMANT_BOOTS, EmoteClueItem.ADAMANT_DAGGER),
		new EmoteClue(Master, "Swing a bullroarer at the top of the Watchtower. Beware of double agents! Equip a dragon plateskirt, climbing boots and a dragon chainbody.", "Yanille Watchtower", TOP_FLOOR_OF_THE_YANILLE_WATCHTOWER, new WorldPoint(2930, 4717, 2), DOUBLE_AGENT_141, BULL_ROARER, EmoteClueItem.ANY_DRAGON_PLATESKIRT, EmoteClueItem.CLIMBING_BOOTS, EmoteClueItem.ANY_DRAGON_CHAINBODY, EmoteClueItem.BULL_ROARER),
		new EmoteClue(Beginner, "Blow a raspberry at Gypsy Aris in her tent. Equip a gold ring and a gold necklace.", "Varrock", GYPSY_TENT_ENTRANCE, new WorldPoint(3203, 3424, 0), RASPBERRY, EmoteClueItem.GOLD_RING, EmoteClueItem.GOLD_NECKLACE),
		new EmoteClue(Beginner, "Bow to Brugsen Bursen at the Grand Exchange.", "Grand Exchange", null, new WorldPoint(3164, 3477, 0), BOW),
		new EmoteClue(Beginner, "Cheer at Iffie Nitter. Equip a chef hat and a red cape.", "Varrock", FINE_CLOTHES_ENTRANCE, new WorldPoint(3205, 3416, 0), CHEER, EmoteClueItem.CHEFS_HAT, EmoteClueItem.RED_CAPE),
		new EmoteClue(Beginner, "Clap at Bob's Brilliant Axes. Equip a bronze axe and leather boots.", "Lumbridge", BOB_AXES_ENTRANCE, new WorldPoint(3231, 3203, 0), CLAP, EmoteClueItem.BRONZE_AXE, EmoteClueItem.LEATHER_BOOTS),
		new EmoteClue(Beginner, "Panic at Al Kharid mine.", "Al Kharid mine", null, new WorldPoint(3300, 3314, 0), PANIC),
		new EmoteClue(Beginner, "Spin at Flynn's Mace Shop.", "Falador", null, new WorldPoint(2950, 3387, 0), SPIN));

	private static final String UNICODE_CHECK_MARK = "\u2713";
	private static final String UNICODE_BALLOT_X = "\u2717";

	private final String text;
	private final String locationName;
	private final EmoteClueDifficulty emoteClueDifficulty;

	@Nullable
	private final STASHUnit stashUnit;
	private final WorldPoint location;
	private final Emote firstEmote;
	private final Emote secondEmote;
	private final ItemRequirement[] itemRequirements;

	private EmoteClue(EmoteClueDifficulty emoteClueDifficulty, String text, String locationName, STASHUnit stashUnit, WorldPoint location, Emote firstEmote, @Nonnull ItemRequirement... itemRequirements)
	{
		this(emoteClueDifficulty, text, locationName, stashUnit, location, firstEmote, null, itemRequirements);
	}

	private EmoteClue(EmoteClueDifficulty emoteClueDifficulty, String text, String locationName, STASHUnit stashUnit, WorldPoint location, Enemy enemy, Emote firstEmote, @Nonnull ItemRequirement... itemRequirements)
	{
		this(emoteClueDifficulty, text, locationName, stashUnit, location, firstEmote, null, itemRequirements);
		setEnemy(enemy);
	}

	private EmoteClue(EmoteClueDifficulty emoteClueDifficulty, String text, String locationName, STASHUnit stashUnit, WorldPoint location, Emote firstEmote, Emote secondEmote, @Nonnull ItemRequirement... itemRequirements)
	{
		this.emoteClueDifficulty = emoteClueDifficulty;
		this.text = text;
		this.locationName = locationName;
		this.stashUnit = stashUnit;
		this.location = location;
		this.firstEmote = firstEmote;
		this.secondEmote = secondEmote;
		this.itemRequirements = itemRequirements;
	}

	private EmoteClue(EmoteClueDifficulty emoteClueDifficulty, String text, String locationName, @Nullable STASHUnit stashUnit, WorldPoint location, Emote firstEmote, Emote secondEmote, @Nonnull Varbits firePit, @Nonnull ItemRequirement... itemRequirements)
	{
		this(emoteClueDifficulty, text, locationName, stashUnit, location, firstEmote, secondEmote, itemRequirements);
		setRequiresLight(true);
		setHasFirePit(firePit);
	}
}
