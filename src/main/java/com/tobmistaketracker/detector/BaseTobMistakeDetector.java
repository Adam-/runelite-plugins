package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import com.tobmistaketracker.TobRaider;
import lombok.Getter;
import lombok.NonNull;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import java.util.List;

/**
 * Interface for detecting mistakes during The Theatre of Blood
 */
public abstract class BaseTobMistakeDetector {
    // When in a tob, register all
    // only detectmistakes wehn startup though.

    @Inject
    protected TobMistakeTrackerPlugin plugin;

    @Inject
    protected Client client;

    @Inject
    protected ClientThread clientThread;

    @Inject
    protected EventBus eventBus;

    @Getter
    public boolean detectingMistakes;

    protected BaseTobMistakeDetector() {
        detectingMistakes = false;
    }

    /**
     * Used to tell a detector to start listening for events.
     */
    public void startup() {
        eventBus.register(this);
        clientThread.invokeLater(this::computeDetectingMistakes);
    }

    /**
     * Shutdown and cleanup state. This is always called when the plugin is shutdown, or when a detector is finished.
     */
    public void shutdown() {
        detectingMistakes = false;
        eventBus.unregister(this);
    }

    /**
     * Compute if the detector should start detecting mistakes. This is always from the client thread called on startup.
     */
    protected abstract void computeDetectingMistakes();

    /**
     * Detects mistakes for the given raider.
     * This is called during handling the {@link net.runelite.api.events.GameTick} event, each tick.
     *
     * @param raider - The raider to detect mistakes for
     * @return The list of {@link TobMistake} detected on this tick
     */
    public abstract List<TobMistake> detectMistakes(@NonNull TobRaider raider);

    /**
     * This optional method allows detectors to handle some logic after all detectMistakes methods have been invoked
     * for this {@link net.runelite.api.events.GameTick}.
     */
    public void afterDetect() {
    }
}
