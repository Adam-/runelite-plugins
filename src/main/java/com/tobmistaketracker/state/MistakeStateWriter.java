package com.tobmistaketracker.state;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MistakeStateWriter {

    private static final Gson GSON = RuneLiteAPI.GSON;

    public static void write(MistakeStateManager mistakeStateManager, boolean developerMode) {
        try {
            Files.createDirectories(getMistakeStateDir());
        } catch (IOException e) {
            log.error("Unable to create directories " + getMistakeStateDir(), e);
            return;
        }

        final Path filepath = getMistakeStateFilePath(developerMode);
        try (BufferedWriter writer = Files.newBufferedWriter(filepath)) {
            writer.write(GSON.toJson(mistakeStateManager));
        } catch (IOException e) {
            log.error("Unable to write mistake state to " + filepath, e);
        }
    }
}