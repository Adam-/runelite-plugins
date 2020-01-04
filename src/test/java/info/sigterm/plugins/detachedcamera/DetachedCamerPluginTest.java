package info.sigterm.plugins.detachedcamera;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DetachedCamerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DetachedCameraPlugin.class);
		RuneLite.main(args);
	}
}