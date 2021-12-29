package info.sigterm.plugins.playtime;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PlayTimePlugin.class);
		RuneLite.main(args);
	}
}