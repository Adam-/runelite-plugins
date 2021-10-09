package com.herblorerecipes;

import java.awt.event.KeyEvent;
import javax.inject.Inject;
import net.runelite.client.input.KeyListener;

public class HerbloreRecipesKeyListener implements KeyListener
{

	@Inject
	private HerbloreRecipesPlugin plugin;

	@Inject
	private HerbloreRecipesConfig config;

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (config.useModifierKey() && config.modifierKey().matches(e))
		{
			plugin.setModifierKeyPressed(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (config.useModifierKey() && config.modifierKey().matches(e))
		{
			plugin.setModifierKeyPressed(false);
		}
	}
}
