package com.adriansoftware;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Deserializes a {@link BankValueHistoryContainer}.
 */
public class BankValueHistoryDeserializer implements JsonDeserializer<BankValueHistoryContainer>
{
	@Override
	public BankValueHistoryContainer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
	{
		JsonObject jsonObject = jsonElement.getAsJsonObject();

		JsonObject map = jsonObject.getAsJsonObject("pricesMap");
		Map<LocalDateTime, BankValue> bankValue = new HashMap<>();
		for (Map.Entry<String, JsonElement> elems : map.entrySet())
		{
			JsonElement element = elems.getValue();
			JsonObject containerPrices = element.getAsJsonObject();

			bankValue.put(LocalDateTime.parse(elems.getKey()),
				BankValue
					.builder()
					.tab(containerPrices.get("tab").getAsInt())
					.bankValue(containerPrices.get("bankValue").getAsLong())
					.build());
		}

		return new BankValueHistoryContainer(bankValue);
	}
}