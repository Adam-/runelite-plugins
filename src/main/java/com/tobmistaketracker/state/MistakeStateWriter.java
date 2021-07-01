package com.tobmistaketracker.state;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.tobmistaketracker.state.MistakeStateUtil.getMistakeStateDir;
import static com.tobmistaketracker.state.MistakeStateUtil.getMistakeStateFilePath;

/**
 * Writes MistakeState to disk
 */
@Slf4j
@Singleton
public class MistakeStateWriter {

    private static final Gson GSON = RuneLiteAPI.GSON;

    private final Path mistakeStateDir;
    private final Path mistakeStateFilePath;

    @Inject
    public MistakeStateWriter(@Named("developerMode") boolean developerMode) {
        this.mistakeStateDir = getMistakeStateDir();
        this.mistakeStateFilePath = getMistakeStateFilePath(developerMode);
    }

    public void write(MistakeStateManager mistakeStateManager) {
        try {
            Files.createDirectories(mistakeStateDir);
        } catch (IOException e) {
            log.error("Unable to create directories " + mistakeStateDir, e);
            return;
        }

        final Path filepath = mistakeStateFilePath;
        try (BufferedWriter writer = Files.newBufferedWriter(filepath)) {
            writer.write(GSON.toJson(mistakeStateManager));
        } catch (IOException e) {
            log.error("Unable to write mistake state to " + filepath, e);
        }
    }
}