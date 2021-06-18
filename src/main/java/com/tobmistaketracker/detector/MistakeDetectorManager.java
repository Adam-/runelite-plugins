package com.tobmistaketracker.detector;

import com.google.common.annotations.VisibleForTesting;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class MistakeDetectorManager implements TobMistakeDetector {

    private final List<TobMistakeDetector> mistakeDetectors;

    @Getter
    private boolean detectingMistakes;

    @Inject
    public MistakeDetectorManager(List<TobMistakeDetector> mistakeDetectors) {
        this.mistakeDetectors = mistakeDetectors;
        this.detectingMistakes = false;
    }

    @Override
    public void startup() {
        // TODO: We don't need certain detectors running all the time (e.g. Bloat detector during Maiden)
        // TODO: Maybe use NpcSpawned and NpcDespawned
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakeDetector.startup();
        }

        detectingMistakes = true;
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

    @VisibleForTesting
    public void logRunningDetectors() {
        log.info("MistakeDetectorManager running: " + isDetectingMistakes());

        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            log.info(mistakeDetector.getClass() + " running: " + mistakeDetector.isDetectingMistakes());
        }
    }
}
