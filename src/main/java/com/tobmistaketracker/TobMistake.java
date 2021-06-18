package com.tobmistaketracker;

import lombok.Getter;
import lombok.NonNull;

public enum TobMistake {
    DEATH("Death", "death"),
    MAIDEN_BLOOD("Maiden Blood", "blood");

    @Getter
    @NonNull
    private final String mistakeName;
    @Getter
    @NonNull
    private final String mistakeIcon;

    TobMistake(@NonNull String mistakeName, @NonNull String mistakeIcon) {
        this.mistakeName = mistakeName;
        this.mistakeIcon = mistakeIcon;
    }
}
