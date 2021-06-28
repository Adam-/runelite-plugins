package com.tobmistaketracker;

import lombok.Getter;
import lombok.NonNull;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

public enum TobMistake {
    DEATH("Death", "death.png", "I'm planking!"),
    MAIDEN_BLOOD("Maiden Blood", "maiden_blood.png", "I'm drowning in Maiden's blood!"),
    BLOAT_HAND("Bloat Hand", "bloat_hand.png", "I'm stunned!"),
    VERZIK_P2_ACID("Verzik P2 Acid", "verzik_p2_acid.png", "I can't count to four!"),
    VERZIK_P2_BOMB("Verzik P2 Bomb", "verzik_p2_bomb.png", "I'm eating cabbages!"),
    VERZIK_P2_BOUNCE("Verzik P2 Bounce", "verzik_p2_bounce.png", "Bye!"),
    VERZIK_P3_MELEE("Verzik P3 Melee", "verzik_p3_melee.png", "Sorry, I can't tank!"),
    VERZIK_P3_PURPLE("Verzik P3 Purple Tornado", "verzik_p3_purple.png", "I'm healing Verzik!");

    @Getter
    @NonNull
    private final String mistakeName;

    @Getter
    @NonNull
    // TODO: Maybe have a list of messages to say after reaching a certain amount of mistakes
    // TODO: Maybe have these be configurable for each mistake in the config?
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
