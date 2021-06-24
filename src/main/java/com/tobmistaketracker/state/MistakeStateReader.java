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
import java.nio.file.Path;

import static com.tobmistaketracker.state.MistakeStateUtil.getMistakeStateFilePath;

/**
 * Reads MistakeState from disk
 */
@Slf4j
@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MistakeStateReader {

    private static final Gson GSON = RuneLiteAPI.GSON;

    public static MistakeStateManager read(boolean developerMode) {
        final Path filepath = getMistakeStateFilePath(developerMode);

        if (Files.exists(filepath)) {
            try (BufferedReader reader = Files.newBufferedReader(filepath);
                 JsonReader jsonReader = new JsonReader(reader)) {
                return GSON.fromJson(jsonReader, MistakeStateManager.class);
            } catch (IOException e) {
                log.error("Unable to read mistake state from " + filepath, e);
            }
        }

        return new MistakeStateManager(developerMode);
    }
}