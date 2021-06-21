package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manager for all the {@link BaseTobMistakeDetector}. It keeps all the detectors in memory in order to manage events.
 * <p>
 * All detectors initialized in the manager are responsible for determining when to start detecting mistakes.
 * The manager may call the startup() or shutdown() method on a detector at any time.
 *
 * When the manager is on (detectingMistakes = true), then all other detectors are subscribed to the EventBus and
 * listening for events on when to turn themselves on/off. This will only be true while the player is in Tob.
 */
@Slf4j
@Singleton
public class MistakeDetectorManager extends BaseTobMistakeDetector {

    private final List<BaseTobMistakeDetector> mistakeDetectors;

    @Inject
    public MistakeDetectorManager(DeathMistakeDetector deathMistakeDetector,
                                  MaidenMistakeDetector maidenMistakeDetector,
                                  BloatMistakeDetector bloatMistakeDetector) {
        this.mistakeDetectors = new ArrayList<>(Arrays.asList(
                deathMistakeDetector, maidenMistakeDetector, bloatMistakeDetector));
    }

    @Override
    public void startup() {
        super.startup();

        for (BaseTobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakeDetector.startup();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        for (BaseTobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakeDetector.shutdown();
        }
        // Don't clear mistakeDetectors or else we can't get them back.
    }

    @Override
    protected void computeDetectingMistakes() {
        // Always run the manager throughout the raid
        detectingMistakes = true;
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        List<TobMistake> mistakes = new ArrayList<>();

        if (!isDetectingMistakes()) {
            return mistakes;
        }

        for (BaseTobMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakes.addAll(mistakeDetector.detectMistakes(raider));
            }
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        if (!isDetectingMistakes()) return;

        for (BaseTobMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakeDetector.afterDetect();
            }
        }
    }

    public List<BaseTobMistakeDetector> getMistakeDetectors() {
        return Collections.unmodifiableList(mistakeDetectors);
    }
}
