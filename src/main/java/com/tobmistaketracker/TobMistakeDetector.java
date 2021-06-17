package com.tobmistaketracker;

import lombok.NonNull;

import java.util.List;

public interface TobMistakeDetector {

    List<TobMistake> detectMistakes(@NonNull TobRaider raider);

    void cleanup();
}
