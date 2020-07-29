package info.sigterm.plugins.discordlootlogger;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
class WebhookBody
{
	private String content;
	private List<Embed> embeds = new ArrayList<>();

	@Data
	static class Embed
	{
		final UrlEmbed image;
	}

	@Data
	static class UrlEmbed
	{
		final String url;
	}
}
