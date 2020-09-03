package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.filereadwriter.FileReadWriter;
import com.raidtracker.ui.RaidTrackerPanel;
import junit.framework.TestCase;
import net.runelite.api.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class TestRaidTrackerPanel extends TestCase
{
    @Mock
    @Bind
    private Client client;

    @Inject
    private FileReadWriter fw;

    @Before
    public void setUp()
    {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
    }

    @Test
    public void TestFilter()
    {
        fw.updateUsername("Canvasba");

        ArrayList<RaidTracker> l = fw.readFromFile();

        assertEquals(19, l.size());
        assertEquals("Grimy irit leaf", l.get(0).getLootList().get(0).getName());

        RaidTrackerPanel panel = mock(RaidTrackerPanel.class, CALLS_REAL_METHODS);
        panel.setLoaded(true);
        panel.setRTList(l);

        ArrayList<RaidTracker> arcanes = panel.filterRTListByName("Arcane Prayer Scroll");
        ArrayList<RaidTracker> dexes = panel.filterRTListByName("Dexterous Prayer Scroll");
        ArrayList<RaidTracker> dusts = panel.filterDustReceivers();
        ArrayList<RaidTracker> kits = panel.filterKitReceivers();
        ArrayList<RaidTracker> ownArcanes = panel.filterOwnDrops(arcanes);
        ArrayList<RaidTracker> ownDexes = panel.filterOwnDrops(dexes);
        ArrayList<RaidTracker> ownDusts = panel.filterOwnDusts(dusts);
        ArrayList<RaidTracker> ownKits = panel.filterOwnKits(kits);


        assertEquals(2, arcanes.size());
        assertEquals(1, ownArcanes.size());
        assertEquals(0, dexes.size());
        assertEquals(0, ownDexes.size());
        assertEquals(2, dusts.size());
        assertEquals(1, ownDusts.size());
        assertEquals(3, kits.size());
        assertEquals(2, ownKits.size());


    }

}
