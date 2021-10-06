package tictac7x.TODO_PLUGIN_NAME;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TODO_PLUGIN_NAME_PluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(tictac7x.TODO_PLUGIN_NAME.TODO_PLUGIN_NAME_Plugin.class);
		RuneLite.main(args);
	}
}