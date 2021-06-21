package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class DeathMistakeDetector extends BaseTobMistakeDetector {

    private final Set<String> playerDeaths;

    @Inject
    public DeathMistakeDetector() {
        this.playerDeaths = new HashSet<>();
    }

    @Override
    public void startup() {
        super.startup();

        // Always detect deaths throughout the raid
        detectingMistakes = true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        playerDeaths.clear();
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

            if (plugin.isLoadedRaider(player.getName())) {
                playerDeaths.add(player.getName());
            }
        }
        // TODO: If verzik death call shutdown()
    }
}
