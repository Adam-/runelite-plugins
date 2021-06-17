package com.tobmistaketracker;

import lombok.NonNull;
import net.runelite.api.events.GameTick;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MistakeDetectorManager {

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

    public void cleanup() {
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakeDetector.cleanup();
        }

        mistakeDetectors.clear();
    }

    public List<TobMistake> detectMistakes(@NonNull TobRaider raider) {
        List<TobMistake> mistakes = new ArrayList<>();
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
            mistakes.addAll(mistakeDetector.detectMistakes(raider));
        }

        return mistakes;
    }

    public  <T> void onEvent(String methodName, T event) {
        for (TobMistakeDetector mistakeDetector : mistakeDetectors) {
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
