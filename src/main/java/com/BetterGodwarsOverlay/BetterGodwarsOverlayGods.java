package com.BetterGodwarsOverlay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BetterGodwarsOverlayGods
{

	ARMADYL("Armadyl", BetterGodwarsOverlayVarbits.GWD_ARMADYL_KC),
	BANDOS("Bandos", BetterGodwarsOverlayVarbits.GWD_BANDOS_KC),
	SARADOMIN("Saradomin", BetterGodwarsOverlayVarbits.GWD_SARADOMIN_KC),
	ZAMORAK("Zamorak", BetterGodwarsOverlayVarbits.GWD_ZAMORAK_KC),
	ZAROS("Ancient", BetterGodwarsOverlayVarbits.GWD_ZAROS_KC);

	@Getter
	private final String name;

	@Getter
	private final BetterGodwarsOverlayVarbits killCountVarbit;

}
