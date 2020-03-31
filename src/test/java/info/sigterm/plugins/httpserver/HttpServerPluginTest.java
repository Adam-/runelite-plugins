package info.sigterm.plugins.httpserver;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HttpServerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HttpServerPlugin.class);
		RuneLite.main(args);
	}
}