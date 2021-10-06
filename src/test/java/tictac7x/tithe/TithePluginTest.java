package tictac7x.tithe;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TithePluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(tictac7x.tithe.TithePlugin.class);
		RuneLite.main(args);
	}
}