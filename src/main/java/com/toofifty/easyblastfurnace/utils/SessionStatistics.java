package com.toofifty.easyblastfurnace.utils;

import com.toofifty.easyblastfurnace.methods.GoldBarMethod;
import com.toofifty.easyblastfurnace.methods.GoldHybridMethod;
import com.toofifty.easyblastfurnace.methods.MetalBarMethod;
import com.toofifty.easyblastfurnace.methods.Method;
import com.toofifty.easyblastfurnace.state.BlastFurnaceState;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SessionStatistics
{
    @Inject
    private Client client;

    @Inject
    private MethodHandler methodHandler;

    @Inject
    private BlastFurnaceState state;

    @Getter
    private int staminaDoses = 0;

    private ItemContainer cachedBank;

    private final Map<Integer, Integer> outputs = new HashMap<>();

    public void clear()
    {
        outputs.clear();
        staminaDoses = 0;
    }

    public void drinkStamina()
    {
        staminaDoses++;
    }

    public int getTotalActionsDone()
    {
        int actions = 0;
        for (int itemId : outputs.keySet()) {
            actions += outputs.getOrDefault(itemId, 0);
        }
        return actions;
    }

    public double getTotalXpGained()
    {
        double xp = 0;
        for (int itemId : outputs.keySet()) {
            int quantity = outputs.getOrDefault(itemId, 0);
            xp += XpRecord.get(itemId).getXp() * quantity;
        }
        return xp;
    }

    private int getActionsBanked(int itemId)
    {
        return getActionsBanked(XpRecord.get(itemId));
    }

    private ItemContainer getBank()
    {
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);

        if (bank != null) {
            return cachedBank = bank;
        }

        return cachedBank;
    }

    private int getActionsBanked(XpRecord xpRecord)
    {
        ItemContainer bank = getBank();
        if (bank == null) return 0;

        int ores = bank.count(xpRecord.getOreId());

        if (xpRecord.getCoalPer() == 0) {
            return ores;
        }

        int coal = bank.count(ItemID.COAL);

        return Math.min(ores, coal / xpRecord.getCoalPer());
    }

    private double getXpBanked(int itemId)
    {
        return getXpBanked(XpRecord.get(itemId));
    }

    private double getXpBanked(XpRecord xpRecord)
    {
        return getActionsBanked(xpRecord) * xpRecord.getXp();
    }

    public int getTotalActionsBanked()
    {
        Method method = methodHandler.getMethod();

        if (method instanceof GoldHybridMethod) {
            return getActionsBanked(ItemID.GOLD_ORE) +
                getActionsBanked(((GoldHybridMethod) method).oreItem());
        }

        if (method instanceof MetalBarMethod) {
            return getActionsBanked(((MetalBarMethod) method).oreItem());
        }

        if (method instanceof GoldBarMethod) {
            return getActionsBanked(ItemID.GOLD_ORE);
        }

        return 0;
    }

    public double getTotalXpBanked()
    {
        Method method = methodHandler.getMethod();

        if (method instanceof GoldHybridMethod) {
            return getXpBanked(ItemID.GOLD_ORE) +
                getXpBanked(((GoldHybridMethod) method).oreItem());
        }

        if (method instanceof MetalBarMethod) {
            return getXpBanked(((MetalBarMethod) method).oreItem());
        }

        if (method instanceof GoldBarMethod) {
            return getXpBanked(ItemID.GOLD_ORE);
        }

        return 0;
    }

    public void onFurnaceUpdate()
    {
        int[] bars = new int[]{
            ItemID.GOLD_BAR, ItemID.STEEL_BAR, ItemID.MITHRIL_BAR, ItemID.ADAMANTITE_BAR, ItemID.RUNITE_BAR
        };

        for (int barId : bars) {
            int diff = state.getFurnace().getChange(barId);
            if (diff > 0) {
                outputs.put(barId, outputs.getOrDefault(barId, 0) + diff);
            }
        }
    }
}
