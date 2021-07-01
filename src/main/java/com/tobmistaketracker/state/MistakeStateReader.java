package com.tobmistaketracker.state;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;

import javax.inject.Inject;
import javax.inject.Named;
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
public class MistakeStateReader {

    private static final Gson GSON = RuneLiteAPI.GSON;

    private final MistakeStateWriter mistakeStateWriter;
    private final Path mistakeStateFilePath;

    @Inject
    public MistakeStateReader(MistakeStateWriter mistakeStateWriter, @Named("developerMode") boolean developerMode) {
        this.mistakeStateWriter = mistakeStateWriter;
        this.mistakeStateFilePath = getMistakeStateFilePath(developerMode);
    }

    public MistakeStateManager read() {
        if (Files.exists(mistakeStateFilePath)) {
            try (BufferedReader reader = Files.newBufferedReader(mistakeStateFilePath);
                 JsonReader jsonReader = new JsonReader(reader)) {
                MistakeStateManager mistakeStateManager = GSON.fromJson(jsonReader, MistakeStateManager.class);
                mistakeStateManager.setMistakeStateWriter(mistakeStateWriter);
                return mistakeStateManager;
            } catch (IOException e) {
                log.error("Unable to read mistake state from " + mistakeStateFilePath, e);
            }
        }

        return new MistakeStateManager(mistakeStateWriter);
    }
}