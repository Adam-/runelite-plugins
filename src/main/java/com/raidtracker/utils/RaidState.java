package com.raidtracker.utils;
import lombok.Value;

@Value
public class RaidState
{
    private final boolean inRaid;
    private final int raidType;
}