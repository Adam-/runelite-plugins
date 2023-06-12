package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.filereadwriter.FileReadWriter;
import com.raidtracker.ui.RaidTrackerPanel;
import com.raidtracker.ui.RaidUniques;
import junit.framework.TestCase;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;

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
    public void TestFilter() throws ExecutionException, InterruptedException {
        fw.updateUsername("Canvasba");

        ArrayList<RaidTracker> l = fw.readFromFile(1);

        assertEquals(3, l.size());
        System.out.print(l);
        assertEquals("Adamantite ore", l.get(0).getLootList().get(0).getName());

        RaidTrackerPanel panel = mock(RaidTrackerPanel.class, CALLS_REAL_METHODS);
        panel.setLoaded(true);
        panel.setRTList(l);
        panel.setCmFilter("CM & Normal");
        panel.setDateFilter("All Time");
        panel.setMvpFilter("Both");
        panel.setTeamSizeFilter("All sizes");

        when(panel.getUniquesList()).thenReturn(EnumSet.of(
                RaidUniques.DEX,
                RaidUniques.ARCANE,
                RaidUniques.TWISTED_BUCKLER,
                RaidUniques.DHCB,
                RaidUniques.DINNY_B,
                RaidUniques.ANCESTRAL_HAT,
                RaidUniques.ANCESTRAL_TOP,
                RaidUniques.ANCESTRAL_BOTTOM,
                RaidUniques.DRAGON_CLAWS,
                RaidUniques.ELDER_MAUL,
                RaidUniques.KODAI,
                RaidUniques.TWISTED_BOW,
                RaidUniques.DUST,
                RaidUniques.TWISTED_KIT,
                RaidUniques.OLMLET
        ));

        ItemManager IM = mock(ItemManager.class);

        panel.setItemManager(IM);

        /*ArrayList<RaidTracker> arcanes = panel.filterRTListByName("Arcane Prayer Scroll");
        ArrayList<RaidTracker> dexes = panel.filterRTListByName("Dexterous Prayer Scroll");
        ArrayList<RaidTracker> dusts = panel.filterDustReceivers();
        ArrayList<RaidTracker> kits = panel.filterKitReceivers();
        ArrayList<RaidTracker> pets = panel.filterPetReceivers();
        ArrayList<RaidTracker> ownArcanes = panel.filterOwnDrops(arcanes);
        ArrayList<RaidTracker> ownDexes = panel.filterOwnDrops(dexes);
        ArrayList<RaidTracker> ownDusts = panel.filterOwnDusts(dusts);
        ArrayList<RaidTracker> ownKits = panel.filterOwnKits(kits);
        ArrayList<RaidTracker> ownPets = panel.filterOwnPets(pets);


        assertEquals(2, arcanes.size());
        assertEquals(1, ownArcanes.size());
        assertEquals(1, dexes.size());
        assertEquals(0, ownDexes.size());
        assertEquals(2, dusts.size());
        assertEquals(1, ownDusts.size());
        assertEquals(4, kits.size());
        assertEquals(2, ownKits.size());
        assertEquals(4, pets.size());
        assertEquals(2, ownPets.size());*/

        assertEquals(4, panel.getDistinctKills(l).size());

    }
}
