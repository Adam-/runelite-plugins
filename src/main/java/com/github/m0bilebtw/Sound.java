package com.github.m0bilebtw;

public enum Sound {
    LEVEL_UP("/LevelUpCompleted_r1.mp3"),
    QUEST("/QuestCompleted_r1.mp3"),
    COLLECTION_LOG_SLOT("/ColLogSlotCompleted_r1.mp3"),
    ACHIEVEMENT_DIARY("/AchieveDiaryCompleted_r1.mp3"),
    DEATH("/DyingHCIMCompleted_r1.mp3");

    private final String resourceName;

    Sound(String resNam) {
        resourceName = resNam;
    }

    String getResourceName() {
        return resourceName;
    }
}
