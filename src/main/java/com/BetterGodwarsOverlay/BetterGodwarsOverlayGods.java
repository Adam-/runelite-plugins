package com.BetterGodwarsOverlay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
public enum BetterGodwarsOverlayGods
{

	ARMADYL("Armadyl", new WorldPoint(2923, 5351, 2), BetterGodwarsOverlayVarbits.GWD_ARMADYL_KC),
	BANDOS("Bandos", new WorldPoint(2923, 5351, 2), BetterGodwarsOverlayVarbits.GWD_BANDOS_KC),
	SARADOMIN("Saradomin", new WorldPoint(2923, 5351, 2), BetterGodwarsOverlayVarbits.GWD_SARADOMIN_KC),
	ZAMORAK("Zamorak", new WorldPoint(2922, 5350, 2), BetterGodwarsOverlayVarbits.GWD_ZAMORAK_KC);

	@Getter
	private final String name;
	@Getter
	private final WorldPoint location;
	@Getter
	private final BetterGodwarsOverlayVarbits killCountVarbit;

}
