package com.github.m0bilebtw;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "C Engineer: Completed",
	description = "C Engineer announces when you complete an achievement",
	tags = {"skills", "stats", "levels", "progress", "bars"}
)

public class CEngineerCompletedPlugin extends Plugin
{
	@Inject
	private Client client;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private ClientThread clientThread;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private CEngineerCompletedConfig config;

	private final Varbits[] varbitsAchievementDiaries = {
			Varbits.DIARY_ARDOUGNE_EASY, Varbits.DIARY_ARDOUGNE_MEDIUM, Varbits.DIARY_ARDOUGNE_HARD, Varbits.DIARY_ARDOUGNE_ELITE,
			Varbits.DIARY_DESERT_EASY, Varbits.DIARY_DESERT_MEDIUM, Varbits.DIARY_DESERT_HARD, Varbits.DIARY_DESERT_ELITE,
			Varbits.DIARY_FALADOR_EASY, Varbits.DIARY_FALADOR_MEDIUM, Varbits.DIARY_FALADOR_HARD, Varbits.DIARY_FALADOR_ELITE,
			Varbits.DIARY_KANDARIN_EASY, Varbits.DIARY_KANDARIN_MEDIUM, Varbits.DIARY_KANDARIN_HARD, Varbits.DIARY_KANDARIN_ELITE,
			Varbits.DIARY_KARAMJA_EASY, Varbits.DIARY_KARAMJA_MEDIUM, Varbits.DIARY_KARAMJA_HARD, Varbits.DIARY_KARAMJA_ELITE,
			Varbits.DIARY_KOUREND_EASY, Varbits.DIARY_KOUREND_MEDIUM, Varbits.DIARY_KOUREND_HARD, Varbits.DIARY_KOUREND_ELITE,
			Varbits.DIARY_LUMBRIDGE_EASY, Varbits.DIARY_LUMBRIDGE_MEDIUM, Varbits.DIARY_LUMBRIDGE_HARD, Varbits.DIARY_LUMBRIDGE_ELITE,
			Varbits.DIARY_MORYTANIA_EASY, Varbits.DIARY_MORYTANIA_MEDIUM, Varbits.DIARY_MORYTANIA_HARD, Varbits.DIARY_MORYTANIA_ELITE,
			Varbits.DIARY_VARROCK_EASY, Varbits.DIARY_VARROCK_MEDIUM, Varbits.DIARY_VARROCK_HARD, Varbits.DIARY_VARROCK_ELITE,
			Varbits.DIARY_WESTERN_EASY, Varbits.DIARY_WESTERN_MEDIUM, Varbits.DIARY_WESTERN_HARD, Varbits.DIARY_WESTERN_ELITE,
			Varbits.DIARY_WILDERNESS_EASY, Varbits.DIARY_WILDERNESS_MEDIUM, Varbits.DIARY_WILDERNESS_HARD, Varbits.DIARY_WILDERNESS_ELITE
	};
	private static final Pattern COLLECTION_LOG_ITEM_REGEX = Pattern.compile("New item added to your collection log: .*");

	private final Map<Skill, Integer> oldExperience = new EnumMap<>(Skill.class);
	private final Map<Varbits, Integer> oldAchievementDiaries = new EnumMap<>(Varbits.class);

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invoke(this::setupOldMaps);
	}

	@Override
	protected void shutDown() throws Exception
	{
		oldExperience.clear();
		oldAchievementDiaries.clear();
	}

	private void setupOldMaps() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			oldExperience.clear();
			oldAchievementDiaries.clear();
		} else {
			for (final Skill skill : Skill.values()) {
				oldExperience.put(skill, client.getSkillExperience(skill));
			}
			for (Varbits v : varbitsAchievementDiaries) {
				int var = client.getVar(v);
				oldAchievementDiaries.put(v, var);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch(event.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
			case LOGGING_IN:
			case LOGIN_SCREEN_AUTHENTICATOR:
				oldExperience.clear();
				oldAchievementDiaries.clear();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		final Skill skill = statChanged.getSkill();

		// Modified from Nightfirecat's virtual level ups plugin as this info isn't (yet?) built in to statChanged event
		final int xpAfter = client.getSkillExperience(skill);
		final int levelAfter = Experience.getLevelForXp(xpAfter);
		final int xpBefore = oldExperience.getOrDefault(skill, -1);
		final int levelBefore = xpBefore == -1 ? -1 : Experience.getLevelForXp(xpBefore);

		oldExperience.put(skill, xpAfter);

		// Do not proceed if any of the following are true:
		//  * xpBefore == -1              (don't fire when first setting new known value)
		//  * levelAfter > MAX_REAL_LEVEL (we only care about real level ups)
		//  * xpAfter <= xpBefore         (do not allow 200m -> 200m exp drops)
		//  * levelBefore >= levelAfter   (stop if if we're not actually reaching a new level)
		if (xpBefore == -1 || levelAfter > Experience.MAX_REAL_LEVEL || xpAfter <= xpBefore || levelBefore >= levelAfter) {
			return;
		}

		// If we get here, 'skill' was leveled up!
		if (config.announceLevelUp()) {
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "C Engineer", "" + skill + " level up: completed.", null); // TODO remove, for testing before sounds present
			soundEngine.playClip(Sound.LEVEL_UP);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		if (config.announceQuestCompletion() && WidgetID.QUEST_COMPLETED_GROUP_ID == widgetLoaded.getGroupId()) {
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "C Engineer", "Quest: completed.", null); // TODO remove, for testing before sounds present
			soundEngine.playClip(Sound.QUEST);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath) {
		if (config.announceDeath() && actorDeath.getActor() == client.getLocalPlayer()) {
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "C Engineer", "Dying on my HCIM: completed.", null); // TODO remove, for testing before sounds present
			soundEngine.playClip(Sound.DEATH);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM) {
			return;
		}

		if (config.announceCollectionLog() && COLLECTION_LOG_ITEM_REGEX.matcher(chatMessage.getMessage()).matches()) {
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "C Engineer", "Collection log slot: completed.", null); // TODO remove, for testing before sounds present
			soundEngine.playClip(Sound.COLLECTION_LOG_SLOT);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged) {
		// Apparently I can't check if it's a particular varbit using the names from Varbits enum, so this is the way
		for (Varbits v : varbitsAchievementDiaries) {
			int var = client.getVar(v);
			int previousValue = oldAchievementDiaries.getOrDefault(v, -1);
			if (previousValue != -1 && previousValue != var) {
				// Doesn't matter what the value is, as long as it's not -1 (just discovering value exists) and has changed (diaries don't un-unlock so direction doesn't matter)
				client.addChatMessage(ChatMessageType.PUBLICCHAT, "C Engineer", "Achievement diary: completed.", null); // TODO remove, for testing before sounds present
				// TODO this route has not yet been tested in-game
				soundEngine.playClip(Sound.ACHIEVEMENT_DIARY);
			}
			oldAchievementDiaries.put(v, var);
		}
	}

	@Provides
	CEngineerCompletedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CEngineerCompletedConfig.class);
	}
}
