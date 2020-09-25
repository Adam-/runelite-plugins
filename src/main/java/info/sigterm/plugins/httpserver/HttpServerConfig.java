package info.sigterm.plugins.httpserver;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("httpserver")
public interface HttpServerConfig extends Config
{

    @ConfigItem(
            keyName = "ports",
            name = "Server Port",
            description = "Comma separated list of ports. The first free port will be used."
    )
    default String ports()
    {
        return "8080";
    }

}