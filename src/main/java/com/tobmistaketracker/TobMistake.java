package com.tobmistaketracker;

import lombok.Getter;
import lombok.NonNull;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Set;

public enum TobMistake {
    // All death chat messages will be handled by the corresponding specific DEATH enum
    DEATH("Death", "death.png", ""),
    DEATH_MAIDEN("Maiden Death", "death.png", "I'm planking!"),
    DEATH_BLOAT("Bloat Death", "death.png", "I'm planking!"),
    DEATH_NYLOCAS("Nylocas Death", "death.png", "I'm planking!"),
    DEATH_SOTETSEG("Sotetseg Death", "death.png", "I'm planking!"),
    DEATH_XARPUS("Xarpus Death", "death.png", "I'm planking!"),
    DEATH_VERZIK("Verzik Death", "death.png", "I'm planking!"),
    MAIDEN_BLOOD("Maiden Blood", "maiden_blood.png", "I'm drowning in Maiden's blood!"),
    BLOAT_HAND("Bloat Hand", "bloat_hand.png", "I'm stunned!"),
    VERZIK_P2_BOUNCE("Verzik P2 Bounce", "verzik_p2_bounce.png", "Bye!"),
    VERZIK_P2_BOMB("Verzik P2 Bomb", "verzik_p2_bomb.png", "I'm eating cabbages!"),
    VERZIK_P2_ACID("Verzik P2 Acid", "verzik_p2_acid.png", "I can't count to four!"),
    VERZIK_P3_MELEE("Verzik P3 Melee", "verzik_p3_melee.png", "I'm PKing my team!"),
    VERZIK_P3_WEB("Verzik P3 Web", "verzik_p3_web.png", "I was stuck in a web!"),
    VERZIK_P3_PURPLE("Verzik P3 Purple Tornado", "verzik_p3_purple.png", "I'm healing Verzik!");

    private static final Set<TobMistake> ROOM_DEATH_ENUMS = EnumSet.of(
            DEATH_MAIDEN, DEATH_BLOAT, DEATH_NYLOCAS, DEATH_SOTETSEG, DEATH_XARPUS, DEATH_VERZIK);

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

    public static boolean isRoomDeath(TobMistake mistake) {
        return ROOM_DEATH_ENUMS.contains(mistake);
    }
}
