package com.raidtracker;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;


@RunWith(MockitoJUnitRunner.class)
public class GenericTests
{
    @Inject
    public Gson gson;

    @Inject
    private RaidTracker raidTracker;
    @Before
    public void before()
    {
        Gson gson = new Gson();
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
        String message = "Challenge complete: The Wardens. Duration: <col=ef1020>3:53</col><br>Tombs of Amascut: Entry Mode challenge completion time: <col=ef1020>17:22</col>. Personal best: 16:40";
        System.out.println(message.split("<br>")[0]);
        System.out.println(message.split("<br>")[1]);
    };
    @Test
    public void TestJsonToStringWithTracker()
    {
        ArrayList<RaidTrackerItem> lootList = new ArrayList<>();
        lootList.add(new RaidTrackerItem("item", 1, 1, 1));
        lootList.add(new RaidTrackerItem("item", 1, 1, 1));
        lootList.add(new RaidTrackerItem("item", 1, 1, 1));
        raidTracker.setLootList(lootList);
        System.out.println("here: " + gson.toJson(raidTracker.lootList));
    };
}
