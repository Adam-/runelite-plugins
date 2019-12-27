package info.sigterm.plugins.esspouch;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class Runner
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EssPouchPlugin.class);
		RuneLite.main(args);
	}
}
