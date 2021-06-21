package com.tobmistaketracker;

import lombok.Getter;
import lombok.NonNull;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

public enum TobMistake {
    DEATH("Death", "death.png", "Oh no, I've died! :("),
    MAIDEN_BLOOD("Maiden Blood", "maiden_blood.png", "I've stood in Maiden blood!"),
    BLOAT_HAND("Bloat Hand", "bloat_hand.png", "I got handed by Bloat!");

    @Getter
    @NonNull
    private final String mistakeName;

    @Getter
    @NonNull
    // TODO: Maybe have a list of messages to say after reaching a certain amount of mistakes
    private final String chatMessage;

    @Getter
    @NonNull
    private final BufferedImage mistakeImage;

    TobMistake(@NonNull String mistakeName, @NonNull String mistakeImagePath, @NonNull String chatMessage) {
        this.mistakeName = mistakeName;
        this.chatMessage = chatMessage;

        this.mistakeImage = ImageUtil.loadImageResource(getClass(), mistakeImagePath);
    }
}
