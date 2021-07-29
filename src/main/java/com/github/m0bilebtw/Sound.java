package com.github.m0bilebtw;

public enum Sound {
    LEVEL_UP("/LevelUpCompleted_r1.wav"),
    QUEST("/QuestCompleted_r1.wav"),
    COLLECTION_LOG_SLOT("/ColLogSlotCompleted_r1.wav"),
    COMBAT_TASK("/CombatTaskCompleted_r1.wav"),
    ACHIEVEMENT_DIARY("/AchieveDiaryCompleted_r1.wav"),
    DEATH("/DyingHCIMCompleted_r1.wav");

    private final String resourceName;

    Sound(String resNam) {
        resourceName = resNam;
    }

    String getResourceName() {
        return resourceName;
    }
}
