package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import com.tobmistaketracker.TobRaider;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class DeathMistakeDetector implements TobMistakeDetector {

    private final Set<String> playerDeaths;

    private final TobMistakeTrackerPlugin plugin;

    private final Client client;

    @Getter
    private boolean detectingMistakes;

    @Inject
    public DeathMistakeDetector(TobMistakeTrackerPlugin plugin, Client client) {
        this.plugin = plugin;
        this.client = client;

        this.playerDeaths = new HashSet<>();
    }

    @Override
    public void startup() {
        detectingMistakes = true;
    }

    @Override
    public void shutdown() {
        playerDeaths.clear();
        detectingMistakes = false;
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        if (playerDeaths.contains(raider.getName())) {
            return Collections.singletonList(TobMistake.DEATH);
        }

        return Collections.emptyList();
    }

    @Override
    public void afterDetect() {
        playerDeaths.clear();
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        Actor actor = event.getActor();
        if (actor instanceof Player) {
            Player player = (Player) actor;

            if (plugin.isPlayerInRaid(player.getName())) {
                playerDeaths.add(player.getName());
            }
        }
    }
}
