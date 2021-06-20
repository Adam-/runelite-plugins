package com.tobmistaketracker;

import lombok.Getter;
import lombok.NonNull;

public enum TobMistake {
    DEATH("Death", "death", "Oh no, I've died! :("),
    MAIDEN_BLOOD("Maiden Blood", "blood", "I've stood in Maiden blood!");

    @Getter
    @NonNull
    private final String mistakeName;

    @Getter
    @NonNull
    private final String mistakeIcon;

    @Getter
    @NonNull
    // TODO: Maybe have a list of messages to say after reaching a certain amount of mistakes
    private final String chatMessage;

    TobMistake(@NonNull String mistakeName, @NonNull String mistakeIcon, @NonNull String chatMessage) {
        this.mistakeName = mistakeName;
        this.mistakeIcon = mistakeIcon;
        this.chatMessage = chatMessage;
    }
}
