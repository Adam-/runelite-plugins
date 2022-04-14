/*
 * Copyright (c) 2018, Jacob M <https://github.com/jacoblairm>
 * Copyright (c) 2020, BegOsrs <https://github.com/begosrs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package begosrs.barbarianassault.timer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static net.runelite.client.util.RSTimeUnit.GAME_TICKS;

@Slf4j
@Getter
public class Timer
{
	private final Instant roundStartTime;
	private Instant waveStartTime;
	private int roundTicks;
	private int waveTickStart;

	public Timer()
	{
		this.roundStartTime = Instant.now();
		this.waveStartTime = roundStartTime;
		this.roundTicks = 0;
		this.waveTickStart = 0;
	}

	private static LocalTime timeFromMillis(int millis)
	{
		return LocalTime.ofSecondOfDay(millis / 1000)
				  .plus(millis % 1000, ChronoUnit.MILLIS);
	}

	private static LocalTime timeFromDuration(Duration duration)
	{
		return LocalTime.ofSecondOfDay(duration.getSeconds())
				  .plusNanos(duration.getNano());
	}

	private static String formatTime(LocalTime time, TimeUnits units)
	{
		if (time.getHour() > 0)
		{
			return time.format(DateTimeFormatter.ofPattern("HH:mm"));
		}
		else
		{
			final String minsFormat = time.getMinute() > 9 ? "mm" : "m";
			final String fractional = units.equals(TimeUnits.SECONDS) ?
					  "" :
					  "." + time.get(ChronoField.MILLI_OF_SECOND) / 100;
			return time.format(DateTimeFormatter.ofPattern(minsFormat + ":ss")) + fractional;
		}
	}

	public Duration getRoundTime(boolean delayed)
	{
		Duration duration = Duration.between(roundStartTime, Instant.now());
		if (delayed)
		{
			duration = duration.minus(Duration.of(1, GAME_TICKS));
		}

		return duration;
	}

	public int getRoundTicks(boolean delayed)
	{
		return delayed ? roundTicks - 1 : roundTicks;
	}

	public Duration getWaveTime()
	{
		return Duration.between(waveStartTime, Instant.now());
	}

	// delayed is true for in-wave timer and false for end-time displays
	public int getCurrentWaveTick(boolean delayed)
	{
		return delayed ? roundTicks - waveTickStart - 1 : roundTicks - waveTickStart;
	}

	public int getWaveTimeSecondsRounded()
	{
		final Duration elapsed = getWaveTime();
		final long millis = elapsed.toMillis();
		final int seconds = roundToNearestSecond(millis);
		log.debug("Wave duration millis: {}", millis);
		log.debug("Wave duration seconds: {}", seconds);
		return seconds;
	}

	public int getRoundTimeSecondsRounded()
	{
		final Duration elapsed = getRoundTime(true);
		final long millis = elapsed.toMillis();
		final int seconds = roundToNearestSecond(millis);
		log.debug("Round duration millis: {}", millis);
		log.debug("Round duration seconds: {}", seconds);
		return seconds;
	}

	public String getRoundTimeFormatted(boolean delayed, TimeUnits units)
	{
		final LocalTime time = units.equals(TimeUnits.TICKS) ?
				  timeFromMillis(getRoundTicks(delayed) * Constants.GAME_TICK_LENGTH) :
				  timeFromDuration(getRoundTime(delayed));
		return formatTime(time, units);
	}

	public String getWaveTimeFormatted(TimeUnits units)
	{
		final LocalTime time = units.equals(TimeUnits.TICKS) ?
				  timeFromMillis(getCurrentWaveTick(false) * Constants.GAME_TICK_LENGTH) :
				  timeFromDuration(getWaveTime());
		return formatTime(time, units);
	}

	public void setWaveStartTime()
	{
		waveStartTime = Instant.now();
		waveTickStart = roundTicks;
	}

	public void onGameTick()
	{
		roundTicks++;
	}

	private int roundToNearestSecond(long millis)
	{
		return (int) (1000 * ((millis + 500) / 1000)) / 1000;
	}
}
