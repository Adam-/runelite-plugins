package info.sigterm.plugins.detachedcamera;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("detachedcamera")
public interface DetachedCameraConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "detachedCameraHotkey",
		name = "Toggle hotkey",
		description = "Toggle detached camera on/off with a hotkey (unset to disable)"
	)
	default Keybind detachedCameraHotkey()
	{
		return Keybind.NOT_SET;
	}
}
