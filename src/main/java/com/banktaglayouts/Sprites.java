package com.banktaglayouts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteOverride;

@RequiredArgsConstructor
public enum Sprites implements SpriteOverride
{
    APPLY_PREVIEW(-3192, "confirm_icon.png"), // 3192 is definitely not my bank pin.
    CANCEL_PREVIEW(-3193, "delete.png"),
    ;

    @Getter
    private final int spriteId;

    @Getter
    private final String fileName;
}
