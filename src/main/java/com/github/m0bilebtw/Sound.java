package com.github.m0bilebtw;

public enum Sound {
    TEST("/notifier.wav"), /* TODO remove Sound.TEST replacing each use with respective actual sound clip */
    DEATH("/dying_on_my_hcim_completed.wav");

    private final String resourceName;

    Sound(String resNam) {
        resourceName = resNam;
    }

    String getResourceName() {
        return resourceName;
    }
}
