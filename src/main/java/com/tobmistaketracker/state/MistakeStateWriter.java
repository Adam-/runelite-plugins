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

import static com.tobmistaketracker.state.MistakeStateManager.MISTAKE_STATE_DIR;
import static com.tobmistaketracker.state.MistakeStateManager.MISTAKE_STATE_FILE_PATH;

/**
 * Writes MistakeState to disk
 */
@Slf4j
@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MistakeStateWriter {

    private static final Gson GSON = RuneLiteAPI.GSON;

    public static void write(MistakeStateManager mistakeStateManager) {
        try {
            Files.createDirectories(MISTAKE_STATE_DIR);
        } catch (IOException e) {
            log.error("Unable to create directories " + MISTAKE_STATE_DIR, e);
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(MISTAKE_STATE_FILE_PATH)) {
            writer.write(GSON.toJson(mistakeStateManager));
        } catch (IOException e) {
            log.error("Unable to write mistake state to " + MISTAKE_STATE_FILE_PATH, e);
        }
    }
}