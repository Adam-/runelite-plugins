package begosrs.barbarianassault;

import begosrs.barbarianassault.timer.TimeUnits;
import begosrs.barbarianassault.timer.Timer;

import java.util.regex.Pattern;

import net.runelite.api.Constants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Test {

	@org.junit.Test
    public void testRegex()
    {
        String text = "(50) Defender";
        text = text.replaceAll("\\(.*\\) ", "");
        assertEquals("Defender", text);

        text = "(5) 00:00";
        text = text.replaceAll("\\(.*\\) ", "");
        assertEquals("00:00", text);

        text = " , Red egg  ,     Green egg  ,  Blue egg  ,Yellow egg,";
        text = text.replaceAll("\\s*,*\\s*(?i)" + Pattern.quote("red egg") + "\\s*,*\\s*", ",");
        text = trimComma(text);
        assertEquals("Green egg  ,  Blue egg  ,Yellow egg", text);

        text = text.replaceAll("\\s*,*\\s*(?i)" + Pattern.quote("blue egg") + "\\s*,*\\s*", ",");
        text = trimComma(text);
        assertEquals("Green egg,Yellow egg", text);

        text = text.replaceAll("\\s*,*\\s*(?i)" + Pattern.quote("yellow egg") + "\\s*,*\\s*", ",");
        text = trimComma(text);
        assertEquals("Green egg", text);
    }

    private String trimComma(String text)
    {
        if (text.startsWith(","))
        {
            text = text.substring(1);
        }
        if (text.endsWith(","))
        {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    @org.junit.Test
    public void testTimer()
    {
		final Timer timer = new Timer();

		final Round round = new Round(1, timer);
		assertTrue(round.getTimer().getRoundTime(false).toMillis() <= 1000);

		// 1st wave starts
		Wave wave = new Wave(null, 1, new Role[5], timer);
		timer.onGameTick();
		assertTrue(wave.getTimeUntilCallChange() >= 29);
		assertTrue(wave.getTimer().getWaveTime().toMillis() <= 1000);
		assertEquals(0.0, wave.getTimeElapsed(true, TimeUnits.TENTHS_OF_SECOND), 0.05);
		assertEquals(0.0, wave.getTimeElapsed(true, TimeUnits.TICKS), 0.05);

		sleep(3000);
		for (int i = 0; i < 3000 / Constants.GAME_TICK_LENGTH; i++)
		{
			timer.onGameTick();
		}

		assertTrue(wave.getTimeUntilCallChange() <= 27);
		assertTrue(wave.getTimer().getWaveTime().toMillis() >= 3000);
		assertEquals(3.0, wave.getTimeElapsed(true, TimeUnits.TENTHS_OF_SECOND), 0.05);
		assertEquals(3.0, wave.getTimeElapsed(true, TimeUnits.TICKS), 0.05);

		// 2nd wave starts
		timer.setWaveStartTime();
		wave = new Wave(null, 2, new Role[5], timer);
		timer.onGameTick();
		assertTrue(wave.getTimeUntilCallChange() >= 29);
		assertTrue(wave.getTimer().getWaveTime().toMillis() <= 1000);
		assertEquals(0.0, wave.getTimeElapsed(true, TimeUnits.TENTHS_OF_SECOND), 0.05);
		assertEquals(0.0, wave.getTimeElapsed(true, TimeUnits.TICKS), 0.05);
	}

    private void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException ignored)
        {
        }
    }

}
