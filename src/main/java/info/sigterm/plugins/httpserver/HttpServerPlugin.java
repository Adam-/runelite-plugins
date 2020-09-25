package info.sigterm.plugins.httpserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.http.api.RuneLiteAPI;

@PluginDescriptor(
	name = "HTTP Server"
)
public class HttpServerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private HttpServerConfig config;

	private HttpServer server;

	@Override
	protected void startUp() throws Exception
	{

		String[] ports = config.ports().split(",");
		Exception ex = null;

		for (String port : ports) {
			try {
				server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);
				server.createContext("/stats", new StatsHandler());
				server.setExecutor(Executors.newSingleThreadExecutor());
				server.start();
				return;
			} catch (Exception e) {
				ex = e;
			}
		}

		if (ex != null) {
			throw ex;
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		server.stop(1);
	}

	@Provides
	HttpServerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HttpServerConfig.class);
	}

	class StatsHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange exchange) throws IOException
		{
			JsonArray skills = new JsonArray();
			for (Skill skill : Skill.values())
			{
				if (skill == Skill.OVERALL)
				{
					continue;
				}

				JsonObject object = new JsonObject();
				object.addProperty("stat", skill.getName());
				object.addProperty("level", client.getRealSkillLevel(skill));
				object.addProperty("boostedLevel", client.getBoostedSkillLevel(skill));
				object.addProperty("xp", client.getSkillExperience(skill));
				skills.add(object);
			}

			exchange.sendResponseHeaders(200, 0);
			try (OutputStreamWriter out = new OutputStreamWriter(exchange.getResponseBody()))
			{
				RuneLiteAPI.GSON.toJson(skills, out);
			}
		}
	}
}
