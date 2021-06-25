package com.github.m0bilebtw;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Singleton
@Slf4j
public class SoundEngine {

    private Clip clip = null;

    private boolean loadClip(Sound sound) {
        try {
            InputStream resourceStream = SoundEngine.class.getResourceAsStream(sound.getResourceName());
            if (resourceStream == null) {
                log.error("Failed to load C Engineer sound " + sound + " as resource stream was null!");
            } else {
                InputStream fileStream = new BufferedInputStream(resourceStream);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileStream);
                clip.open(audioInputStream); // TODO currently erroring with "Invalid format" on both my own wav file, and the idle notifier default wav file
                return true;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            log.warn("Failed to load C Engineer sound " + sound, e);
        }
        return false;
    }

    public void playClip(Sound sound) {
        if (clip == null || !clip.isOpen()) {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }

            try {
                clip = AudioSystem.getClip();
            } catch (LineUnavailableException e) {
                log.warn("Failed to get clip for C Engineer sound " + sound, e);
                return;
            }

            if (!loadClip(sound)) {
                return;
            }
        }

        // From RuneLite base client Notifier class:
        // Using loop instead of start + setFramePosition prevents the clip
        // from not being played sometimes, presumably a race condition in the
        // underlying line driver
        clip.loop(1);
    }
}
