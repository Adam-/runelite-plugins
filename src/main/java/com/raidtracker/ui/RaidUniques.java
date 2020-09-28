package com.raidtracker.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

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
    DUST("Metamorphic Dust", ItemID.METAMORPHIC_DUST),
    TWISTED_KIT("Twisted Kit", ItemID.TWISTED_ANCESTRAL_COLOUR_KIT),
    OLMLET("Olmlet", ItemID.OLMLET),
    AVERNIC("Avernic defender hilt", ItemID.AVERNIC_DEFENDER_HILT),
    RAPIER("Ghrazi rapier", ItemID.GHRAZI_RAPIER),
    SANGSTAFF("Sanguinesti staff (uncharged)", ItemID.SANGUINESTI_STAFF_UNCHARGED),
    JUSTI_FACEGUARD("Justiciar faceguard", ItemID.JUSTICIAR_FACEGUARD),
    JUSTI_CHESTGUARD("Justiciar chestguard", ItemID.JUSTICIAR_CHESTGUARD),
    JUSTI_LEGGUARDS("Justiciar legguards", ItemID.JUSTICIAR_LEGGUARDS),
    SCYTHE("Scythe of vitur (uncharged)", ItemID.SCYTHE_OF_VITUR_UNCHARGED),
    LILZIK("Lil' Zik", 22473);

    @Getter
    private final String name;

    @Getter
    private final int itemID;
}