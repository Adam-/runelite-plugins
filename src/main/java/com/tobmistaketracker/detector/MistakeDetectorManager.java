package com.tobmistaketracker.detector;

import com.google.common.annotations.VisibleForTesting;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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

    public <T> void onEvent(String methodName, T event) {
        // TODO: Use eventBus.register instead and remove all this reflection code.
        if (!isDetectingMistakes()) return;

        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            if (!mistakeDetector.isDetectingMistakes()) {
                continue;
            }

            final Method method;
            try {
                method = mistakeDetector.getClass().getMethod(methodName, event.getClass());
            } catch (NoSuchMethodException e) {
                // The mistake detector doesn't have an implementation for receiving these events -- Skip over...
                continue;
            }

            try {
                method.invoke(mistakeDetector, event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(String.format("Error while calling %s.%s", mistakeDetector.getClass(), methodName), e);
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
