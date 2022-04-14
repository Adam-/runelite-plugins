package begosrs.barbarianassault;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BaMinigamePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BaMinigamePlugin.class);
		RuneLite.main(args);
	}
}
