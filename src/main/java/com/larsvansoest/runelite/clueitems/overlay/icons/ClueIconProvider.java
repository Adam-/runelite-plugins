package com.larsvansoest.runelite.clueitems.overlay.icons;

import net.runelite.client.ui.overlay.components.ImageComponent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

public class ClueIconProvider {

    public ClueRibbonCollection getRibbons() {
        return ribbons;
    }

    private ClueRibbonCollection ribbons;

    public void fetchBuffers() throws IOException {
        this.ribbons = new ClueRibbonCollection(fetchBuffer(ClueIconResource.BEGINNER_RIBBON), fetchBuffer(ClueIconResource.EASY_RIBBON), fetchBuffer(ClueIconResource.MEDIUM_RIBBON), fetchBuffer(ClueIconResource.HARD_RIBBON), fetchBuffer(ClueIconResource.ELITE_RIBBON), fetchBuffer(ClueIconResource.MASTER_RIBBON));
    }

    private ImageComponent fetchBuffer(String iconSource) throws IOException {
        return new ImageComponent(ImageIO.read(Objects.requireNonNull(this.getClass().getResource(iconSource))));
    }
}
