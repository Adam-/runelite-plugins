package com.theatrepoints;

import net.runelite.api.Varbits;

import java.util.regex.Pattern;

public class TheatrePointsConstant {

    public static final Varbits THEATRE_STATUS_VARP = Varbits.THEATRE_OF_BLOOD;

    public static final int THEATRE_RAIDERS_VARP = 330;
    public static final int MAX_RAIDERS = 5;

    public static final int STATE_NO_PARTY = 0;
    public static final int STATE_IN_PARTY = 1;

    public static final Pattern DEATH_SELF = Pattern.compile("You have died. Death count: (\\d+).");
    public static final Pattern DEATH_OTHER = Pattern.compile("([0-9A-Za-z -_]+) has died. Death count: (\\d+).");

    public static final double POINTS_MVP = 14.0;
    public static final double POINTS_ENCOUNTER = 18.0;
    public static final double POINTS_DEATH = 4.0;

    public static final double BASE_RATE = 11.0;


}
