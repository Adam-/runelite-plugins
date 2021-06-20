package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manager for all the {@link TobMistakeDetector}. It keeps all the detectors in memory in order to manage events.
 */
@Slf4j
@Singleton
public class MistakeDetectorManager implements TobMistakeDetector {

    private final Client client;
    private final List<TobMistakeDetector> mistakeDetectors;

    private final DeathMistakeDetector deathMistakeDetector;

    @Getter
    private boolean detectingMistakes;

    @Inject
    public MistakeDetectorManager(Client client,
                                  DeathMistakeDetector deathMistakeDetector,
                                  MaidenMistakeDetector maidenMistakeDetector,
                                  BloatMistakeDetector bloatMistakeDetector) {
        this.client = client;
        this.mistakeDetectors = Arrays.asList(deathMistakeDetector, maidenMistakeDetector, bloatMistakeDetector);
        this.deathMistakeDetector = deathMistakeDetector;
        this.detectingMistakes = false;
    }

    @Override
    public void startup() {
        detectingMistakes = true;
        // Always detect deaths throughout the raid
        deathMistakeDetector.startup();
    }

    @Override
    public void shutdown() {
        reset();
        mistakeDetectors.clear();
    }

    /**
     * Shutdown all other detectors and stop detecting mistakes until startup() is called again. This *keeps* all
     * currently installed detectors around, and just calls shutdown() on them.
     */
    public void reset() {
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakeDetector.shutdown();
        }

        detectingMistakes = false;
    }

    /**
     * Called when the plugin is started
     */
    public void registerToEventBus(EventBus eventBus) {
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            eventBus.register(mistakeDetector);
        }

        eventBus.register(this);
    }

    /**
     * Called when the plugin is shutdown
     */
    public void unregisterFromEventBus(EventBus eventBus) {
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            eventBus.unregister(mistakeDetector);
        }

        eventBus.unregister(this);
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        List<TobMistake> mistakes = new ArrayList<>();

        if (!isDetectingMistakes()) {
            return mistakes;
        }

        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakes.addAll(mistakeDetector.detectMistakes(raider));
            }
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        if (!isDetectingMistakes()) return;

        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakeDetector.afterDetect();
            }
        }
    }

    public List<TobMistakeDetector> getMistakeDetectors() {
        return Collections.unmodifiableList(mistakeDetectors);
    }
}
