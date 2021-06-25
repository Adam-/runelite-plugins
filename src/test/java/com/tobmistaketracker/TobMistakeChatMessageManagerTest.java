package com.tobmistaketracker;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.OverheadTextChanged;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.tobmistaketracker.TobMistakeChatMessageManager.OVERHEAD_TEXT_TICK_TIMEOUT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TobMistakeChatMessageManagerTest {

    private static final String PLAYER_NAME = "Test Player";
    private static final TobMistake MISTAKE = TobMistake.DEATH;

    @Mock
    private TobMistakeTrackerConfig config;

    @Mock
    private Client client;

    @Mock
    private Player mockPlayer;

    private TobMistakeChatMessageManager chatMessageManager;

    @Before
    public void before() {
        when(mockPlayer.getName()).thenReturn(PLAYER_NAME);

        chatMessageManager = new TobMistakeChatMessageManager(config, client, false);
    }

    @Test
    public void playerMadeMistakeSetsOverheadText() {
        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);

        verify(mockPlayer).setOverheadText(MISTAKE.getChatMessage());
    }

    @Test
    public void playerMadeMistakeAddsChatMessageWhenConfigEnabled() {
        when(config.showMistakesInChat()).thenReturn(true);

        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);

        verify(client).addChatMessage(
                eq(ChatMessageType.PUBLICCHAT), eq(PLAYER_NAME), eq(MISTAKE.getChatMessage()), eq(null));
    }

    @Test
    public void playerMadeMistakeDoesNotAddChatMessageWhenConfigDisabled() {
        when(config.showMistakesInChat()).thenReturn(false);

        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);

        verify(client, never()).addChatMessage(
                eq(ChatMessageType.PUBLICCHAT), eq(PLAYER_NAME), eq(MISTAKE.getChatMessage()), eq(null));
    }

    @Test
    public void onGameTickDoesNotRemoveOverheadTextWhenNotTimeout() {
        // Set initial overhead text
        when(client.getTickCount()).thenReturn(0);
        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);
        clearInvocations(mockPlayer);

        when(client.getTickCount()).thenReturn(OVERHEAD_TEXT_TICK_TIMEOUT - 1);

        chatMessageManager.onGameTick(new GameTick());

        verify(mockPlayer, never()).setOverheadText(eq(null));
    }

    @Test
    public void onGameTickDoesRemoveOverheadTextWhenTimeout() {
        // Set initial overhead text
        when(client.getTickCount()).thenReturn(0);
        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);
        clearInvocations(mockPlayer);

        when(client.getTickCount()).thenReturn(OVERHEAD_TEXT_TICK_TIMEOUT);

        chatMessageManager.onGameTick(new GameTick());

        verify(mockPlayer).setOverheadText(eq(null));
    }

    @Test
    public void onOverheadTextChangedNoLongerMakesOnGameTickRemoveOverheadTextWhenPlayer() {
        // Set initial overhead text
        when(client.getTickCount()).thenReturn(0);
        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);
        clearInvocations(mockPlayer);

        // Receive OverheadTextChanged for the player
        chatMessageManager.onOverheadTextChanged(new OverheadTextChanged(mockPlayer, "does not matter"));

        // Does not remove our player's overhead text anymore
        when(client.getTickCount()).thenReturn(OVERHEAD_TEXT_TICK_TIMEOUT);
        chatMessageManager.onGameTick(new GameTick());
        verify(mockPlayer, never()).setOverheadText(eq(null));
    }

    @Test
    public void onOverheadTextChangedStillMakesOnGameTickRemoveOverheadTextWhenNotPlayer() {
        // Set initial overhead text
        when(client.getTickCount()).thenReturn(0);
        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);
        clearInvocations(mockPlayer);

        // Receive OverheadTextChanged for a different player
        Player otherPlayer = mock(Player.class);
        when(otherPlayer.getName()).thenReturn("Some other player");
        chatMessageManager.onOverheadTextChanged(new OverheadTextChanged(otherPlayer, "does not matter"));

        // Still removes our player's overhead text
        when(client.getTickCount()).thenReturn(OVERHEAD_TEXT_TICK_TIMEOUT);
        chatMessageManager.onGameTick(new GameTick());
        verify(mockPlayer).setOverheadText(eq(null));
    }

    @Test
    public void shutdownRemovesOverheadText() {
        // Set initial overhead text
        when(client.getTickCount()).thenReturn(0);
        chatMessageManager.playerMadeMistake(mockPlayer, MISTAKE);
        clearInvocations(mockPlayer);

        chatMessageManager.shutdown();

        verify(mockPlayer).setOverheadText(eq(null));
    }
}
