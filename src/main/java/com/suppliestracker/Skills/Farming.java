package com.suppliestracker.Skills;

import com.suppliestracker.SuppliesTrackerPlugin;
import static net.runelite.api.ItemID.*;

import javax.inject.Singleton;

@Singleton
public class Farming
{
	private SuppliesTrackerPlugin plugin;

	/***	Will be switching all to onMenuOptionClicked similar to how
	 *		hardwoods work next update so its automated just pushing this for now
	 ***/
	public Farming(SuppliesTrackerPlugin plugin)
	{
		this.plugin = plugin;
	}

	public void OnChat(String message)
	{
		if (message.contains("you plant "))
		{
			//Fruit Trees
			if (message.contains("fruit tree patch"))
			{
				//apple
				if (message.contains(" apple"))
				{
					plugin.buildEntries(APPLE_SAPLING);
				}
				//banana
				else if (message.contains("banana"))
				{
					plugin.buildEntries(BANANA_SAPLING);
				}
				//orange
				else if (message.contains("orange"))
				{
					plugin.buildEntries(ORANGE_SAPLING);
				}
				//curry
				else if (message.contains("curry"))
				{
					plugin.buildEntries(CURRY_SAPLING);
				}
				//pineapple
				else if (message.contains("pineapple"))
				{
					plugin.buildEntries(PINEAPPLE_SAPLING);
				}
				//papaya
				else if (message.contains("papaya"))
				{
					plugin.buildEntries(PAPAYA_SAPLING);
				}
				//palm
				else if (message.contains("palm"))
				{
					plugin.buildEntries(PALM_SAPLING);
				}
				//dragonfruit
				else if (message.contains("dragonfruit"))
				{
					plugin.buildEntries(DRAGONFRUIT_SAPLING);
				}
			}
			//Special trees
			if (message.contains("hardwood tree patch"))
			{
				String hardwood = plugin.getHardwoodTree();
				//teak
				if (!hardwood.equals("") && hardwood.contains("mahogany"))
				{
					plugin.buildEntries(MAHOGANY_SAPLING);
				}
				//mahogany
				else if (!hardwood.equals("") && hardwood.contains("teak"))
				{
					plugin.buildEntries(TEAK_SAPLING);
				}
			}
			if (message.contains("celastrus patch"))
			{
				plugin.buildEntries(CELASTRUS_SAPLING);
			}
			if (message.contains("redwood patch"))
			{
				plugin.buildEntries(REDWOOD_SAPLING);
			}
			if (message.contains("calquat patch"))
			{
				plugin.buildEntries(CALQUAT_SAPLING);
			}
			//Trees
			else if (message.contains("tree patch"))
			{
				//oak
				if (message.contains("oak sapling"))
				{
					plugin.buildEntries(OAK_SAPLING);
				}
				//willow
				else if (message.contains("willow sapling"))
				{
					plugin.buildEntries(WILLOW_SAPLING);
				}
				//maple
				else if (message.contains("maple "))
				{
					plugin.buildEntries(MAPLE_SAPLING);
				}
				//yew
				else if (message.contains("yew "))
				{
					plugin.buildEntries(YEW_SAPLING);
				}
				//magic
				else if (message.contains("magic "))
				{
					plugin.buildEntries(MAGIC_SAPLING);
				}
			}
			//allotment
			else if (message.contains(" allotment."))
			{
				if (message.contains("potato"))
				{
					plugin.buildEntries(POTATO_SEED, 3);
				}
				else if (message.contains("onion"))
				{
					plugin.buildEntries(ONION_SEED, 3);
				}
				else if (message.contains("cabbage"))
				{
					plugin.buildEntries(CABBAGE_SEED, 3);
				}
				else if (message.contains("tomato"))
				{
					plugin.buildEntries(TOMATO_SEED, 3);
				}
				else if (message.contains("sweetcorn"))
				{
					plugin.buildEntries(SWEETCORN_SEED, 3);
				}
				else if (message.contains("strawberry"))
				{
					plugin.buildEntries(STRAWBERRY_SEED, 3);
				}
				else if (message.contains("watermelon"))
				{
					plugin.buildEntries(WATERMELON_SEED, 3);
				}
				else if (message.contains("snape grass"))
				{
					plugin.buildEntries(SNAPE_GRASS_SEED, 3);
				}
			}
			else if (message.contains("herb patch"))
			{
				if (message.contains("guam seed"))
				{
					plugin.buildEntries(GUAM_SEED);
				}
				else if (message.contains("marrentill seed"))
				{
					plugin.buildEntries(MARRENTILL_SEED);
				}
				else if (message.contains("tarromin seed"))
				{
					plugin.buildEntries(TARROMIN_SEED);
				}
				else if (message.contains("harralander seed"))
				{
					plugin.buildEntries(HARRALANDER_SEED);
				}
				else if (message.contains("ranarr seed"))
				{
					plugin.buildEntries(RANARR_SEED);
				}
				else if (message.contains("toadflax seed"))
				{
					plugin.buildEntries(TOADFLAX_SEED);
				}
				else if (message.contains("irit seed"))
				{
					plugin.buildEntries(IRIT_SEED);
				}
				else if (message.contains("avantoe seed"))
				{
					plugin.buildEntries(AVANTOE_SEED);
				}
				else if (message.contains("kwuarm seed"))
				{
					plugin.buildEntries(KWUARM_SEED);
				}
				else if (message.contains("snapdragon seed"))
				{
					plugin.buildEntries(SNAPDRAGON_SEED);
				}
				else if (message.contains("cadantine seed"))
				{
					plugin.buildEntries(CADANTINE_SEED);
				}
				else if (message.contains("lantadyme seed"))
				{
					plugin.buildEntries(LANTADYME_SEED);
				}
				else if (message.contains("dwarf weed seed"))
				{
					plugin.buildEntries(DWARF_WEED_SEED);
				}
				else if (message.contains("torstol seed"))
				{
					plugin.buildEntries(TORSTOL_SEED);
				}
			}
			//FLOWERS
			if (message.contains("flower patch"))
			{
				if (message.contains("marigold"))
				{
					plugin.buildEntries(MARIGOLD_SEED);
				}
				else if (message.contains("rosemary"))
				{
					plugin.buildEntries(ROSEMARY_SEED);
				}
				else if (message.contains("nasturtium"))
				{
					plugin.buildEntries(NASTURTIUM_SEED);
				}
				else if (message.contains("woad"))
				{
					plugin.buildEntries(WOAD_SEED);
				}
				else if (message.contains("limpwurt"))
				{
					plugin.buildEntries(LIMPWURT_SEED);
				}
				else if (message.contains("white lil"))
				{
					plugin.buildEntries(WHITE_LILY_SEED);
				}
			}
			//BUSHES
			if (message.contains("bush patch"))
			{
				if (message.contains("redberry"))
				{
					plugin.buildEntries(REDBERRY_SEED);
				}
				else if (message.contains("cadavaberry"))
				{
					plugin.buildEntries(CADAVABERRY_SEED);
				}
				else if (message.contains("dwellberry"))
				{
					plugin.buildEntries(DWELLBERRY_SEED);
				}
				else if (message.contains("jangerberry"))
				{
					plugin.buildEntries(JANGERBERRY_SEED);
				}
				else if (message.contains("whiteberry"))
				{
					plugin.buildEntries(WHITEBERRY_SEED);
				}
				else if (message.contains("poison ivy"))
				{
					plugin.buildEntries(POISON_IVY_SEED);
				}
			}
			//giant seaweed
			if (message.contains("seaweed patch"))
			{
				plugin.buildEntries(SEAWEED_SPORE);
			}
			//cacti
			if (message.contains("cactus patch"))
			{
				if (message.contains("potato cactus"))
				{
					plugin.buildEntries(POTATO_CACTUS_SEED);
				}
				else
				{
					plugin.buildEntries(CACTUS_SEED);
				}
			}
		}
		else if (message.contains("you treat"))
		{
			if (message.contains("with compost"))
			{
				plugin.buildEntries(COMPOST);
			}
			else if (message.contains("with supercompost"))
			{
				plugin.buildEntries(SUPERCOMPOST);
			}
			else if (message.contains("with ultracompost"))
			{
				plugin.buildEntries(ULTRACOMPOST);
			}
			else if (message.contains("with the plant cure"))
			{
				plugin.buildEntries(PLANT_CURE);
			}
		}
	}
}
