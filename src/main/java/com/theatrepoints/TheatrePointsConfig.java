package com.theatrepoints;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("TheatrePointsConfig")
public interface TheatrePointsConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "showTeamChance",
            name = "Team Chance",
            description = "The probability that your team will see a purple. P(Y)."
    )
    default boolean showTeamChance() {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "showPersonalChance",
            name = "Personal Chance",
            description = "The (estimated) probability that the purple will be in your name. P(X|Y)."
    )
    default boolean showPersonalChance() {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "showYoinkChance",
            name = "Combined Team & Personal Chance",
            description = "The (estimated) joint probability that the team will see a purple in your name. P(X&Y)."
    )
    default boolean showYoinkChance() {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "showPersonalDeathCount",
            name = "Personal Death Count",
            description = "The amount of deaths for yourself."
    )
    default boolean showPersonalDeathCount() {
        return true;
    }

	@ConfigItem(
		position = 4,
		keyName = "showDeathCount",
		name = "Death Count",
		description = "The total amount of deaths on the team."
	)
	default boolean showDeathCount() {
		return true;
	}
}
