package com.raidtracker.utils;

import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RaidStateTracker
{
    private final EventBus eventBus;
    private static final int REGION_LOBBY = 13454;
    private static final int WIDGET_PARENT_ID = 481;
    private static final int WIDGET_CHILD_ID = 40;

    private final Client client;

    private RaidState currentState = new RaidState(false, -1);


    public void onPluginStart()
    {
        eventBus.register(this);
    }

    public void onPluginStop()
    {
        eventBus.unregister(this);
    }
    @Subscribe(priority = 5)
    public void onGameTick(GameTick e)
    {
        LocalPoint lp = client.getLocalPlayer().getLocalLocation();
        int region = lp == null ? -1 : WorldPoint.fromLocalInstance(client, lp).getRegionID();

        Widget w = client.getWidget(WIDGET_PARENT_ID, WIDGET_CHILD_ID);

        boolean inLobby = region == REGION_LOBBY;
        RaidRoom currentRoom = RaidRoom.forRegionId(region);
        boolean inRaid = currentRoom != null || (
                (w != null && !w.isHidden()) || //toa
                (client.getVarbitValue(Varbits.IN_RAID) == 1) ||
                (client.getVarbitValue(Varbits.THEATRE_OF_BLOOD) > 1)
        );

        Boolean[] RaidChecks = {
                (client.getVarbitValue(Varbits.IN_RAID) == 1),
                (client.getVarbitValue(Varbits.THEATRE_OF_BLOOD) > 1),
                ((w != null && !w.isHidden()) || currentRoom != null)
        };

        int RaidType = -1;
        for (int i = 0; i < RaidChecks.length; i++)
        {
            if (RaidChecks[i])
            {
                RaidType = i;
            };
        };
        RaidState previousState = this.currentState;
        RaidState newState = new RaidState(inRaid, RaidType);
        if (!previousState.equals(newState))
        {
            System.out.println("Raid State Changed " + newState);
            this.currentState = newState;
            eventBus.post(new RaidStateChanged(previousState, newState));
        }
    }


    public boolean isInRaid()
    {
        return this.currentState.isInRaid();
    }

    public RaidState getCurrentState()
    {
        return this.currentState;
    }
}