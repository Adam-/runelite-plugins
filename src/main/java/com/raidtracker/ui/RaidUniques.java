package com.raidtracker.ui;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.Map;

@AllArgsConstructor
public enum RaidUniques {
    DEX("Dexterous Prayer Scroll", ItemID.DEXTEROUS_PRAYER_SCROLL),
    ARCANE("Arcane Prayer Scroll", ItemID.ARCANE_PRAYER_SCROLL),
    TWISTED_BUCKLER("Twisted Buckler", ItemID.TWISTED_BUCKLER),
    DHCB("Dragon Hunter Crossbow", ItemID.DRAGON_HUNTER_CROSSBOW),
    DINNY_B("Dinh's Bulwark", ItemID.DINHS_BULWARK),
    ANCESTRAL_HAT("Ancestral Hat", ItemID.ANCESTRAL_HAT),
    ANCESTRAL_TOP("Ancestral Robe Top", ItemID.ANCESTRAL_ROBE_TOP),
    ANCESTRAL_BOTTOM("Ancestral Robe Bottom", ItemID.ANCESTRAL_ROBE_BOTTOM),
    DRAGON_CLAWS("Dragon Claws", ItemID.DRAGON_CLAWS),
    ELDER_MAUL("Elder Maul", ItemID.ELDER_MAUL),
    KODAI("Kodai Insignia", ItemID.KODAI_INSIGNIA),
    TWISTED_BOW("Twisted Bow", ItemID.TWISTED_BOW),
    DUST("Metamorphatic Dust", ItemID.METAMORPHIC_DUST),
    TWISTED_KIT("Twisted Kit", ItemID.TWISTED_ANCESTRAL_COLOUR_KIT);

    @Getter
    private final String name;

    @Getter
    private final int itemID;

    private static final Map<String, RaidUniques> NAME_MAP;

    static {
        final ImmutableMap.Builder<String, RaidUniques> byName = ImmutableMap.builder();

        for (RaidUniques unique : values())
        {
            byName.put(unique.getName().toLowerCase(), unique);
        }

        NAME_MAP = byName.build();
    }

    public static RaidUniques getByName(final String name) {
        return NAME_MAP.get(name.toLowerCase());
    }

}