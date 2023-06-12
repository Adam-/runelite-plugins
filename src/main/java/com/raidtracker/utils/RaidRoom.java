package com.raidtracker.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RaidRoom {
    AKKHA(new int[]{14676}),
    APMEKEN(new int[]{15186}),
    BABA(new int[]{15188}),
    CHESTROOM(new int[]{14672}),
    CRONDIS(new int[]{15698}),
    HET(new int[]{14674}),
    KEPHRI(new int[]{14164}),
    NEXUS(new int[]{14160}),
    SCABARAS(new int[]{14162}),
    WARDENS(new int[]{15184, 15696}),
    ZEBAK(new int[]{15700});

    private final int[] regionIds;

    public static RaidRoom forRegionId(int region) {
        for (RaidRoom r : RaidRoom.values()) {
            for (int regionId : r.regionIds) {
                if (regionId == region) {
                    return r;
                }
            }
        }

        return null;
    }

}