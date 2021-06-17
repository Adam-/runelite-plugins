package com.tobmistaketracker;

import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MistakeDetectorManager implements TobMistakeDetector {

    @NonNull
    private final List<TobMistakeDetector> mistakeDetectors;

    @NonNull
    private final TobMistakeTrackerPlugin plugin;

    public MistakeDetectorManager(@NonNull TobMistakeTrackerPlugin plugin) {
        this.mistakeDetectors = new ArrayList<>();
        this.plugin = plugin;
    }

    public <T extends TobMistakeDetector> void installMistakeDetector(Class<T> mistakeDetectorClass) throws Exception {
        Constructor<T> constructor = mistakeDetectorClass.getConstructor(TobMistakeTrackerPlugin.class);
        mistakeDetectors.add(constructor.newInstance(plugin));
    }

    @Override
    public void startup() {
        // TODO: We don't need certain detectors running all the time (e.g. Bloat detector during Maiden)
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakeDetector.startup();
        }
    }

    @Override
    public void shutdown() {
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakeDetector.shutdown();
        }

        mistakeDetectors.clear();
    }

    @Override
    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        List<TobMistake> mistakes = new ArrayList<>();
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakes.addAll(mistakeDetector.detectMistakes(raider));
            }
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            if (mistakeDetector.isDetectingMistakes()) {
                mistakeDetector.afterDetect();
            }
        }
    }

    @Override
    public boolean isDetectingMistakes() {
        return true;
    }

    public  <T> void onEvent(String methodName, T event) {
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
}
