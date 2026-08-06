package info.sigterm.plugins.httpserver;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("httpserver")
public interface HttpServerConfig extends Config {
    @ConfigItem(
            keyName = "port",
            name = "Server port",
            description = "Port to host the HTTP server on. Requires a plugin restart after changing."
    )
    @Range(
            min = 1,
            max = 65535
    )
    default int port() {
        return 8080;
    }
}