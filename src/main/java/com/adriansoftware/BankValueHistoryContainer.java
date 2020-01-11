package com.adriansoftware;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Wraps a collection of prices mapped to the time recorded.
 */
@Data
@RequiredArgsConstructor
public class BankValueHistoryContainer
{
	private final Map<LocalDateTime, BankValue> pricesMap;

	public BankValueHistoryContainer()
	{
		this(new HashMap<>());
	}

	public void addPrice(BankValue bankValue)
	{
		pricesMap.put(LocalDateTime.now(), bankValue);
	}
}
