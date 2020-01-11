package com.adriansoftware;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankValue
{
	private final int tab;
	private final long bankValue;
}
