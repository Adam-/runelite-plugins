package com.toofifty.easyblastfurnace.state;

import com.toofifty.easyblastfurnace.EasyBlastFurnaceConfig;
import lombok.Getter;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

@Getter
@Singleton
public class BlastFurnaceState
{
    @Inject
    private Client client;

    @Inject
    private EasyBlastFurnaceConfig config;

    @Inject
    private CoalBagState coalBag;

    @Inject
    private InventoryState inventory;

    @Inject
    private EquipmentState equipment;

    @Inject
    private PlayerState player;

    @Inject
    private FurnaceState furnace;

    @Inject
    private BankState bank;
}
