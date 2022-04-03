package com.toofifty.easyblastfurnace.state;

import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import net.runelite.api.Client;
import net.runelite.api.Varbits;

import javax.inject.Inject;

public class PlayerState
{
    @Inject
    private Client client;

    @Inject
    private EasyBlastFurnaceConfig config;

    public int getRunEnergy()
    {
        return client.getEnergy();
    }

    public boolean hasStamina()
    {
        return client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0;
    }

    public boolean needsStamina()
    {
        return config.requireStaminaThreshold() != 0 && !hasStamina() && getRunEnergy() <= config.requireStaminaThreshold();
    }
}
