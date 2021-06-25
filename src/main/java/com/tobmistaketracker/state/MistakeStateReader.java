package com.tobmistaketracker.state;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

import static com.tobmistaketracker.state.MistakeStateManager.MISTAKE_STATE_FILE_PATH;

/**
 * Reads MistakeState from disk
 */
@Slf4j
@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MistakeStateReader {

    private static final Gson GSON = RuneLiteAPI.GSON;

    public static MistakeStateManager read() {
        if (Files.exists(MISTAKE_STATE_FILE_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(MISTAKE_STATE_FILE_PATH);
                 JsonReader jsonReader = new JsonReader(reader)) {
                return GSON.fromJson(jsonReader, MistakeStateManager.class);
            } catch (IOException e) {
                log.error("Unable to read mistake state from " + MISTAKE_STATE_FILE_PATH, e);
            }
        }

        return new MistakeStateManager();
    }
}