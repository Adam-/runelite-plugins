package com.theatrepoints;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static com.theatrepoints.TheatrePointsConstant.*;

@Slf4j
@PluginDescriptor(
	name = "ToB Drop Chance",
	description = "Displays estimated team and personal drop chance information at ToB"
)
public class TheatrePointsPlugin extends Plugin {

    @Inject
    private Client client;

	@Inject
	private TheatrePointsConfig config;

	@Provides
	TheatrePointsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TheatrePointsConfig.class);
	}

	@Inject
    private OverlayManager overlayManager;

    @Inject
    private TheatrePointsOverlay overlay;

    boolean inRaid;

    int raidState;

	@Getter
    Map<String, Integer> deathCounter;

    boolean loadedPlayers;

    @Override
    protected void startUp() {
        deathCounter = new HashMap<>();
        reset();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
    }

    private void reset() {
        inRaid = false;
        raidState = 0;
        loadedPlayers = false;
        deathCounter.clear();
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.GAMEMESSAGE && event.getSender() == null) {
            String msg = Text.sanitize(Text.removeTags(event.getMessage()));
            if (msg == null)
                return;

            String target = null;

            Matcher self = DEATH_SELF.matcher(msg);
            if (self.matches())
                target = Text.sanitize(client.getLocalPlayer().getName());

            Matcher other = DEATH_OTHER.matcher(msg);
            if (other.matches())
                target = other.group(1);

            if (target != null && deathCounter.containsKey(target)) {
                int count = deathCounter.get(target);
                deathCounter.put(target, count + 1);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (inRaid && !loadedPlayers) {
            deathCounter.clear();

            Map<Integer, Object> varcmap = client.getVarcMap();
            for (int i = 0; i < MAX_RAIDERS; i++) {
                Integer playervarp = THEATRE_RAIDERS_VARP + i;
                if (varcmap.containsKey(playervarp)) {
                    String tName = Text.sanitize(varcmap.get(playervarp).toString());
                    if (tName != null && !tName.equals("")) {
                        deathCounter.put(tName, 0);
                    }
                }
            }

            loadedPlayers = true;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        int nextState = client.getVar(Varbits.THEATRE_OF_BLOOD);
        if (this.raidState != nextState) {
            if (nextState == STATE_NO_PARTY || nextState == STATE_IN_PARTY) { // Player is not in a raid.
                reset();
                raidState = nextState;
            } else { // Player has entered the theatre.
                if (raidState == STATE_IN_PARTY) { // Player was in a party. They are a raider.
                    reset();
                    inRaid = true;
                }

                raidState = nextState;
            }
        }
    }
}
