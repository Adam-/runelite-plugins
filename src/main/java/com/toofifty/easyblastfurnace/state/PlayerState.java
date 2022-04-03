package com.toofifty.easyblastfurnace.state;

import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;

public class PlayerState
{
    private static final WorldPoint LOAD_POSITION = new WorldPoint(1942, 4967, 0);

    @Accessors(fluent = true)
    @Getter
    @Setter
    private boolean hasLoadedOres = false;

    @Inject
    private Client client;

    @Inject
    private EasyBlastFurnaceConfig config;

    public int getRunEnergy()
    {
        return client.getEnergy();
    }

    public boolean isAtConveyorBelt()
    {
        Player player = client.getLocalPlayer();
        assert player != null;

        WorldPoint location = player.getWorldLocation();
        return location.distanceTo(LOAD_POSITION) < 2;
    }

    public boolean hasStamina()
    {
        return client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0;
    }

    public boolean needsStamina()
    {
        return config.requireStaminaThreshold() != 0 &&
            !hasStamina() &&
            getRunEnergy() <= config.requireStaminaThreshold();
    }
}
