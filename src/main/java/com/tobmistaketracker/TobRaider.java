package com.tobmistaketracker;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/**
 * Encapsulating class for a {@link Player} and other relevant metadata in a Tob raid.
 */
public class TobRaider {

    @Getter
    @NonNull
    private final Player player;

    @Getter
    @Setter
    private WorldPoint previousWorldLocation;

    TobRaider(@NonNull Player player) {
        this.player = player;
    }

    public String getName() {
        return player.getName();
    }

    public WorldPoint getCurrentWorldLocation() {
        return player.getWorldLocation();
    }

    public void setOverheadText(String overheadText) {
        player.setOverheadText(overheadText);
    }
}
