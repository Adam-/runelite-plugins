package com.tobmistaketracker.state;

import java.nio.file.Path;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class MistakeStateUtil {

    private static final Path MISTAKE_STATE_DIR = RUNELITE_DIR.toPath().resolve("tob-mistake-tracker");

    // Use the same mistake state file regardless of user
    private static final String MISTAKE_STATE_FILE_NAME = "mistake-state.txt";

    private static final String DEVELOPER_MODE_PREFIX = "dev-";

    static Path getMistakeStateDir() {
        return MISTAKE_STATE_DIR;
    }

    static Path getMistakeStateFilePath(boolean developerMode) {
        final String filename;
        if (developerMode) {
            filename = DEVELOPER_MODE_PREFIX + MISTAKE_STATE_FILE_NAME;
        } else {
            filename = MISTAKE_STATE_FILE_NAME;
        }

        return getMistakeStateDir().resolve(filename);
    }
}
