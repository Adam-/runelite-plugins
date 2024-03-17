package info.sigterm.plugins.discordlootlogger;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
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
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import net.runelite.http.api.loottracker.LootRecordType;
import okhttp3.*;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.http.api.RuneLiteAPI.GSON;

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

	private LoadingCache<Integer, Boolean> hiddenItemsCache;
	private Collection<String> hiddenItemNames;

	private static String itemImageUrl(int itemId)
	{
		return "https://static.runelite.net/cache/item/icon/" + itemId + ".png";
	}

	@Inject
	private Client client;

	@Override
	protected void startUp()
	{
		lootNpcs = Collections.emptyList();
		String s = config.hiddenItems();
		hiddenItemNames = s != null ? Text.fromCSV(s) : Collections.emptyList();
		hiddenItemsCache = CacheBuilder.newBuilder()
				.maximumSize(512L)
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.build(new CacheLoader<Integer, Boolean>() {
					@Override
					public Boolean load(Integer itemId) throws Exception {
						ItemComposition itemComp = itemManager.getItemComposition(itemId);
						return hiddenItemNames.stream()
								.noneMatch(hiddenItem -> WildcardMatcher.matches(hiddenItem, itemComp.getName()));
					}
				});
	}

	@Override
	protected void shutDown()
	{
		hiddenItemsCache.invalidateAll();
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
			s = config.hiddenItems();
			hiddenItemNames = s != null ? Text.fromCSV(s) : Collections.emptyList();
			hiddenItemsCache.invalidateAll();
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
					processLoot(npc.getName(), items);
					return;
				}
			}
		}
		else
		{
			processLoot(npc.getName(), items);
		}
	}

	@Subscribe
	public void onPlayerLootReceived(PlayerLootReceived playerLootReceived)
	{
		Collection<ItemStack> items = playerLootReceived.getItems();
		processLoot(playerLootReceived.getPlayer().getName(), items);
	}

	@Subscribe
	public void onLootReceived(LootReceived lootReceived)
	{
		if (lootReceived.getType() != LootRecordType.EVENT && lootReceived.getType() != LootRecordType.PICKPOCKET)
		{
			return;
		}

		processLoot(lootReceived.getName(), lootReceived.getItems());
	}

	private String getPlayerName()
	{
		return client.getLocalPlayer().getName();
	}

	private Collection<ItemStack> filterHiddenItems(Collection<ItemStack> items){
		return items.stream()
				.filter(item -> hiddenItemsCache.getUnchecked(item.getId()))
				.collect(Collectors.toList());
	}

	private void processLoot(String name, Collection<ItemStack> items)
	{
		items = filterHiddenItems(items);

		WebhookBody webhookBody = new WebhookBody();

		boolean sendMessage = false;
		StringBuilder stringBuilder = new StringBuilder();
		if (config.includeUsername())
		{
			stringBuilder.append("\n**").append(getPlayerName()).append("**").append(":\n\n");
		}
		stringBuilder.append("***").append(name).append("***").append(":\n");
		final int targetValue = config.lootValue();
		for (ItemStack item : stack(items))
		{
			int itemId = item.getId();
			int qty = item.getQuantity();

			int price = itemManager.getItemPrice(itemId);
			long total = (long) price * qty;

			if (config.includeLowValueItems() || total >= targetValue)
			{
				sendMessage = true;
				ItemComposition itemComposition = itemManager.getItemComposition(itemId);
				stringBuilder.append("*").append(qty).append(" x ").append(itemComposition.getName()).append("*");
				if (config.stackValue())
				{
					stringBuilder.append(" (").append(QuantityFormatter.quantityToStackSize(total)).append(")");
				}
				stringBuilder.append("\n");
				webhookBody.getEmbeds().add(new WebhookBody.Embed(new WebhookBody.UrlEmbed(itemImageUrl(itemId))));
			}
		}

		if (sendMessage)
		{
			webhookBody.setContent(stringBuilder.toString());
			sendWebhook(webhookBody);
		}
	}

	private void sendWebhook(WebhookBody webhookBody)
	{
		String configUrls = config.webhook();
		if (Strings.isNullOrEmpty(configUrls))
		{
			return;
		}

		if (config.sendScreenshot())
		{
			drawManager.requestNextFrameListener(image ->
			{
				BufferedImage bufferedImage = (BufferedImage) image;
				byte[] imageBytes = null;
				try
				{
					imageBytes = convertImageToByteArray(bufferedImage);
				}
				catch (IOException e)
				{
					log.error("Error converting image to byte array", e);
				}
				sendWebhook(webhookBody, imageBytes);
			});
		}
		else
		{
			sendWebhook(webhookBody, null);
		}
	}

	private void sendWebhook(WebhookBody webhookBody, byte[] screenshot)
	{
		MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
			.setType(MultipartBody.FORM)
			.addFormDataPart("payload_json", GSON.toJson(webhookBody));

		if (screenshot != null)
		{
			requestBodyBuilder.addFormDataPart("file", "image.png",
				RequestBody.create(MediaType.parse("image/png"), screenshot));
		}

		MultipartBody requestBody = requestBodyBuilder.build();

		List<String> urls = Splitter.on("\n")
			.omitEmptyStrings()
			.trimResults()
			.splitToList(config.webhook());
		for (String url : urls)
		{
			HttpUrl u = HttpUrl.parse(url);
			if (u == null)
			{
				log.info("Malformed webhook url {}", url);
				continue;
			}

			Request request = new Request.Builder()
				.url(url)
				.post(requestBody)
				.build();
			okHttpClient.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					log.debug("Error submitting webhook", e);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException
				{
					response.close();
				}
			});
		}
	}

	private static byte[] convertImageToByteArray(BufferedImage bufferedImage) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	private static Collection<ItemStack> stack(Collection<ItemStack> items)
	{
		final List<ItemStack> list = new ArrayList<>();

		for (final ItemStack item : items)
		{
			int quantity = 0;
			for (final ItemStack i : list)
			{
				if (i.getId() == item.getId())
				{
					quantity = i.getQuantity();
					list.remove(i);
					break;
				}
			}
			if (quantity > 0)
			{
				list.add(new ItemStack(item.getId(), item.getQuantity() + quantity, item.getLocation()));
			}
			else
			{
				list.add(item);
			}
		}

		return list;
	}
}
