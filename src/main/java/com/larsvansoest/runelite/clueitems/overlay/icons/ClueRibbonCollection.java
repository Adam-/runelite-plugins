package com.larsvansoest.runelite.clueitems.overlay.icons;

import net.runelite.client.ui.overlay.components.ImageComponent;

import java.awt.image.BufferedImage;

public final class ClueRibbonCollection {

    public final ImageComponent getBeginnerRibbon() {
        return beginnerRibbon;
    }

    public final ImageComponent getEasyRibbon() {
        return easyRibbon;
    }

    public final ImageComponent getMediumRibbon() {
        return mediumRibbon;
    }

    public final ImageComponent getHardRibbon() {
        return hardRibbon;
    }

    public final ImageComponent getEliteRibbon() {
        return eliteRibbon;
    }

    public final ImageComponent getMasterRibbon() {
        return masterRibbon;
    }

    private final ImageComponent beginnerRibbon;
    private final ImageComponent easyRibbon;
    private final ImageComponent mediumRibbon;
    private final ImageComponent hardRibbon;
    private final ImageComponent eliteRibbon;
    private final ImageComponent masterRibbon;

    public ClueRibbonCollection(ImageComponent beginnerRibbon, ImageComponent easyRibbon, ImageComponent mediumRibbon, ImageComponent hardRibbon, ImageComponent eliteRibbon, ImageComponent masterRibbon)
    {
        this.beginnerRibbon = beginnerRibbon;
        this.easyRibbon = easyRibbon;
        this.mediumRibbon = mediumRibbon;
        this.hardRibbon = hardRibbon;
        this.eliteRibbon = eliteRibbon;
        this.masterRibbon = masterRibbon;
    }
}
