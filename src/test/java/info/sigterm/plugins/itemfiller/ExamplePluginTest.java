package info.sigterm.plugins.itemfiller;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ItemFillerPlugin.class);
		RuneLite.main(args);
	}
}