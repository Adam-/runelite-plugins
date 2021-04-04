package com.banktaglayouts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import inventorysetups.InventorySetupsItem;
import inventorysetups.InventorySetupsSlotID;
import inventorysetups.InventorySetupsStackCompareID;
import joptsimple.internal.Strings;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.runepouch.Runes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class InventorySetupsAdapter {

    public static final String CONFIG_GROUP = "inventorysetups";
    public static final String CONFIG_KEY = "setups";

    private final BankTagLayoutsPlugin plugin;

    public InventorySetup getInventorySetup(String name)
    {
        final String storedSetups = plugin.configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
        if (Strings.isNullOrEmpty(storedSetups))
        {
            return null;
        }
        try
        {
            final Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<InventorySetup>>()
            {

            }.getType();

            return ((List<InventorySetup>) gson.fromJson(storedSetups, type)).stream().filter(s -> s.getName().equals(name)).findAny().orElse(null);
        }
        catch (Exception e)
        {
            return null;
        }
    }

}
