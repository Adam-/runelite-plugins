package com.larsvansoest.runelite.clueitems.overlay.icon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ClueIconProvider {

    public BufferedImage getBeginner() {
        return beginner;
    }

    public BufferedImage getEasy() {
        return easy;
    }

    public BufferedImage getMedium() {
        return medium;
    }

    public BufferedImage getHard() {
        return hard;
    }

    public BufferedImage getElite() {
        return elite;
    }

    public BufferedImage getMaster() {
        return master;
    }

    private BufferedImage beginner;
    private BufferedImage easy;
    private BufferedImage medium;
    private BufferedImage hard;
    private BufferedImage elite;
    private BufferedImage master;

    public void fetchBuffers() throws IOException {
        this.beginner = fetchBuffer(ClueIconSource.BEGINNER);
        this.easy = fetchBuffer(ClueIconSource.EASY);
        this.medium = fetchBuffer(ClueIconSource.MEDIUM);
        this.hard = fetchBuffer(ClueIconSource.HARD);
        this.elite = fetchBuffer(ClueIconSource.ELITE);
        this.master = fetchBuffer(ClueIconSource.MASTER);
    }

    private BufferedImage fetchBuffer(String iconSource) throws IOException {
        return ImageIO.read(new URL(iconSource));
    }
}
