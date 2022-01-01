package com.github.m0bilebtw;

public enum Sound {
    LEVEL_UP("/LevelUpCompleted_r1.wav"),
    QUEST("/QuestCompleted_r1.wav"),
    COLLECTION_LOG_SLOT("/ColLogSlotCompleted_r1.wav"),
    COMBAT_TASK("/CombatTaskCompleted_r1.wav"),
    ACHIEVEMENT_DIARY("/AchieveDiaryCompleted_r1.wav"),
    DEATH("/DyingHCIMCompleted_r1.wav"),
    EASTER_EGG_STAIRCASE("/Staircase_r1.wav"),
    EASTER_EGG_STRAYDOG_BONE("/ILoveYou_r1.wav"),
    EASTER_EGG_TWISTED_BOW_1GP("/TwistedBow1GP_r1.wav"),
    EASTER_EGG_ZULRAH_PB("/ZulrahPB_r1.wav");

    private final String resourceName;

    Sound(String resNam) {
        resourceName = resNam;
    }

    String getResourceName() {
        return resourceName;
    }
}
