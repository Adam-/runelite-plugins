package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.NonNull;

import java.util.List;

public interface TobMistakeDetector {

    /**
     * Detects mistakes for the given raider.
     * This is called during handling the {@link net.runelite.api.events.GameTick} event, each tick.
     *
     * @param raider - The raider to detect mistakes for
     * @return The list of {@link TobMistake} detected on this tick
     */
    List<TobMistake> detectMistakes(@NonNull TobRaider raider);

    /**
     * This optional method allows detectors to handle some logic after all detectMistakes methods have been invoked
     * for this {@link net.runelite.api.events.GameTick}.
     */
    default void afterDetect() {
    }

    /**
     * Used to tell a detector to initialize state and start detecting mistakes.
     */
    void startup();

    /**
     * Shutdown and cleanup state. This is always called when the plugin is shutdown, or when a detector is finished.
     */
    void shutdown();

    /**
     * Whether or not the detector is actively detecting mistakes
     *
     * @return true if detecting mistakes, false otherwise.
     */
    boolean isDetectingMistakes();
}
