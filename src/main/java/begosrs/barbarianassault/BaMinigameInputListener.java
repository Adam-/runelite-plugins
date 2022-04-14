package begosrs.barbarianassault;

import net.runelite.client.input.KeyListener;

import javax.inject.Inject;
import java.awt.event.KeyEvent;

public class BaMinigameInputListener implements KeyListener
{

	@Inject
	private BaMinigamePlugin plugin;

	@Inject
	private BaMinigameConfig config;

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (config.healerTeammatesHealthHotkey().matches(e))
		{
			plugin.onTeammatesHealthHotkeyChanged(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (config.healerTeammatesHealthHotkey().matches(e))
		{
			plugin.onTeammatesHealthHotkeyChanged(false);
		}
	}
}
