package com.github.m0bilebtw;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.Map;

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

	private final Map<Skill, Integer> oldExperience = new EnumMap<>(Skill.class);

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invoke(this::setupOldExperienceHashmap);
	}

	@Override
	protected void shutDown() throws Exception
	{
		oldExperience.clear();
	}

	private void setupOldExperienceHashmap() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			oldExperience.clear();
		} else {
			for (final Skill skill : Skill.values()) {
				oldExperience.put(skill, client.getSkillExperience(skill));
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
			soundEngine.playClip(Sound.TEST);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		if (config.announceQuestCompletion() && WidgetID.QUEST_COMPLETED_GROUP_ID == widgetLoaded.getGroupId()) {
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "C Engineer", "Quest: completed.", null); // TODO remove, for testing before sounds present
			soundEngine.playClip(Sound.TEST);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath) {
		if (config.announceDeath() && actorDeath.getActor() == client.getLocalPlayer()) {
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "C Engineer", "Dying on my HCIM: completed.", null); // TODO remove, for testing before sounds present
			soundEngine.playClip(Sound.DEATH);
		}
	}

	@Provides
	CEngineerCompletedConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CEngineerCompletedConfig.class);
	}
}
