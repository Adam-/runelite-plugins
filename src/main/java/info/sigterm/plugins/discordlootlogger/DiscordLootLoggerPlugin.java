package info.sigterm.plugins.discordlootlogger;

import com.google.common.base.Strings;
import com.google.inject.Provides;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import static net.runelite.http.api.RuneLiteAPI.GSON;
import static net.runelite.http.api.RuneLiteAPI.JSON;

import okhttp3.*;

@Slf4j
@PluginDescriptor(
	name = "Discord Loot Logger"
)
public class DiscordLootLoggerPlugin extends Plugin
{
	@Inject
	private DiscordLootLoggerConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private ImageCapture imageCapture;

	@Inject
	private DrawManager drawManager;

	private List<String> lootNpcs;

	private static String itemImageUrl(int itemId)
	{
		return "https://static.runelite.net/cache/item/icon/" + itemId + ".png";
	}

	@Override
	protected void startUp()
	{
		lootNpcs = Collections.emptyList();
	}

	@Override
	protected void shutDown()
	{
	}

	@Provides
	DiscordLootLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DiscordLootLoggerConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equalsIgnoreCase(DiscordLootLoggerConfig.GROUP))
		{
			String s = config.lootNpcs();
			lootNpcs = s != null ? Text.fromCSV(s) : Collections.emptyList();
		}
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived npcLootReceived)
	{
		NPC npc = npcLootReceived.getNpc();
		Collection<ItemStack> items = npcLootReceived.getItems();

		if (!lootNpcs.isEmpty())
		{
			for (String npcName : lootNpcs)
			{
				if (WildcardMatcher.matches(npcName, npc.getName()))
				{
					processLoot(npc, items);
					return;
				}
			}
		}
		else
		{
			processLoot(npc, items);
		}
	}

	@Subscribe
	public void onPlayerLootReceived(PlayerLootReceived playerLootReceived)
	{
		Collection<ItemStack> items = playerLootReceived.getItems();
		processLoot(playerLootReceived.getPlayer(), items);
	}

	private void processLoot(Actor from, Collection<ItemStack> items)
	{
		WebhookBody webhookBody = new WebhookBody();

		long totalValue = 0;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(from.getName()).append(":\n");
		for (ItemStack item : items)
		{
			int itemId = item.getId();
			int qty = item.getQuantity();

			int price = itemManager.getItemPrice(itemId);
			long total = (long) price * qty;

			totalValue += total;

			ItemComposition itemComposition = itemManager.getItemComposition(itemId);
			stringBuilder.append(qty).append(" x ").append(itemComposition.getName()).append("\n");
			webhookBody.getEmbeds().add(new WebhookBody.Embed(new WebhookBody.UrlEmbed(itemImageUrl(itemId))));
		}

		final int targetValue = config.lootValue();
		if (targetValue == 0 || totalValue >= targetValue)
		{
			webhookBody.setContent(stringBuilder.toString());
            sendWebhooks(webhookBody);
        }
	}

    private void sendWebhooks(WebhookBody webhookBody) {
        String configUrl = config.webhook();
        if (Strings.isNullOrEmpty(configUrl))
        {
            return;
        }
        HttpUrl url = HttpUrl.parse(configUrl);
        sendLootWebhook(webhookBody, url);
        if (config.sendScreenshot())
        {
			sendScreenShotWebhook(url);
		}
    }

    private void sendScreenShotWebhook(HttpUrl url)
	{
		drawManager.requestNextFrameListener(image -> {
			BufferedImage bufferedImage = getBufferedImageScreenshot(image);
			byte[] imageBytes;
			try {
                 imageBytes = convertImageToByteArray(bufferedImage);
            } catch (IOException e) {
			    log.debug("Error converting image to byte array");
			    return;
            }
			RequestBody requestBody = new MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("file", "image.png",
							RequestBody.create(MediaType.parse("image/png"), imageBytes))
					.build();
			Request request = new Request.Builder()
					.url(url)
					.post(requestBody)
					.build();
			sendRequest(request);
		});
	}

	private BufferedImage getBufferedImageScreenshot(Image image) {
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bufferedImage.createGraphics();
		bGr.drawImage(image, 0, 0, null);
		bGr.dispose();
		return bufferedImage;
	}

	private byte[] convertImageToByteArray(BufferedImage bufferedImage) throws IOException {
		byte[] imageBytes;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        imageBytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
		return imageBytes;
	}

	private void sendLootWebhook(WebhookBody webhookBody, HttpUrl url)
	{

		Request request = new Request.Builder()
			.url(url)
			.post(RequestBody.create(JSON, GSON.toJson(webhookBody)))
			.build();

		sendRequest(request);
	}

	private void sendRequest(Request request) {
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("Error submitting loot webhook", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				response.close();
			}
		});
	}
}
