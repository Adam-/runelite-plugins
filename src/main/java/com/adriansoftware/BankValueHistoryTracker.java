package com.adriansoftware;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.http.api.RuneLiteAPI;

/**
 * Track the change in bank value overtime. Caches values locally.
 */
@Slf4j
public class BankValueHistoryTracker
{
	private static final File HISTORY_CACHE;
	private static final Gson GSON =
		RuneLiteAPI.GSON.newBuilder().registerTypeAdapter(BankValueHistoryContainer.class,
			new BankValueHistoryDeserializer()).create();
	private static final String EXTENTION = ".json";

	static
	{
		HISTORY_CACHE = new File(RuneLite.CACHE_DIR, "/bank");
		HISTORY_CACHE.mkdir();
	}

	/**
	 * Adds a group of prices to a local file cache. Uses a configuration
	 * to determine update frequency.
	 *
	 * @param bankValue current bank value
	 */
	public void add(String username, BankValue bankValue)
	{
		BankValueHistoryContainer valueHistory = getBankValueHistory(username);
		if (valueHistory != null)
		{
			valueHistory.addPrice(bankValue);
			try (FileWriter writer = new FileWriter(getFileForUser(username)))
			{
				GSON.toJson(valueHistory, writer);
			}
			catch (IOException e)
			{
				log.error("Unable to write to price value cache", e);
			}

			log.debug("Adding bank value history entry {}", valueHistory);
		}
	}

	/**
	 * Gets the current bank value history from disk.
	 *
	 * @return the retrieved bank value
	 * @param username username associated with the bank value.
	 */
	public BankValueHistoryContainer getBankValueHistory(String username)
	{
		File playerHistoryFile = getFileForUser(username);
		if (playerHistoryFile == null)
		{
			return null;
		}

		try (FileReader reader = new FileReader(playerHistoryFile))
		{
			playerHistoryFile.createNewFile();
			log.debug("Creating bank history cache file at {}", playerHistoryFile.getAbsolutePath());
			BankValueHistoryContainer container = GSON.fromJson(reader, BankValueHistoryContainer.class);
			if (container == null)
			{
				return new BankValueHistoryContainer();
			}

			return container;
		}
		catch (IOException e)
		{
			log.error("Error reading/writing to the cache files", e);
		}

		return null;
	}

	/**
	 * Get the data file for a specific user
	 * @param username user to get the data for
	 * @return data file for user
	 */
	public File getFileForUser(String username)
	{
		return new File(HISTORY_CACHE, username + EXTENTION);
	}



	/**
	 * Get all accounts that have tracking data from the local file cache.
	 * @return available accounts with data
	 */
	public List<String> getAvailableUsers()
	{
		List<String> result = new ArrayList<>();
		File[] accountFiles = HISTORY_CACHE.listFiles();
		if (accountFiles == null)
		{
			return result;
		}

		for (File file : accountFiles)
		{
			result.add(file.getName().replace(EXTENTION, ""));
		}

		return result;
	}

	/**
	 * Gets the last time something was added to the file cache.
	 * @param username
	 * @return
	 */
	public LocalDateTime getLastDataEntry(String username)
	{
		BankValueHistoryContainer container = getBankValueHistory(username);
		Set<LocalDateTime> times = container.getPricesMap().keySet();
		if (times == null || times.isEmpty()) {
			return null;
		}

		return Collections.max(times);
	}
}
