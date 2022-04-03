package com.toofifty.easyblastfurnace;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.toofifty.easyblastfurnace.overlays.*;
import com.toofifty.easyblastfurnace.utils.BlastFurnaceState;
import com.toofifty.easyblastfurnace.utils.MethodHandler;
import com.toofifty.easyblastfurnace.utils.ObjectManager;
import com.toofifty.easyblastfurnace.utils.SessionStatistics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
    name = "Easy Blast Furnace",
    description = "Helps you train at the blast furnace more efficiently"
)
public class EasyBlastFurnacePlugin extends Plugin
{
    public static final int CONVEYOR_BELT = ObjectID.CONVEYOR_BELT;
    public static final int BAR_DISPENSER = NullObjectID.NULL_9092;
    public static final int BANK_CHEST = ObjectID.BANK_CHEST_26707;

    private static final Pattern COAL_FULL_MESSAGE = Pattern.compile("^The coal bag contains 27 pieces of coal.$");
    private static final Pattern COAL_EMPTY_MESSAGE = Pattern.compile("^The coal bag is now empty.$");

    private static final String FILL_ACTION = "Fill";
    private static final String EMPTY_ACTION = "Empty";
    private static final String DRINK_ACTION = "Drink";

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BlastFurnaceState state;

    @Inject
    private ObjectManager objectManager;

    @Inject
    private EasyBlastFurnaceInstructionOverlay instructionOverlay;

    @Inject
    private EasyBlastFurnaceStatisticsOverlay statisticsOverlay;

    @Inject
    private EasyBlastFurnaceItemStepOverlay itemStepOverlay;

    @Inject
    private EasyBlastFurnaceObjectStepOverlay objectStepOverlay;

    @Inject
    private EasyBlastFurnaceWidgetStepOverlay widgetStepOverlay;

    @Inject
    private EasyBlastFurnaceCoalBagOverlay coalBagOverlay;

    @Inject
    private MethodHandler methodHandler;

    @Inject
    private SessionStatistics statistics;

    @Getter
    private boolean isEnabled = false;

    @Override
    protected void startUp()
    {
        overlayManager.add(instructionOverlay);
        overlayManager.add(statisticsOverlay);
        overlayManager.add(coalBagOverlay);
        overlayManager.add(itemStepOverlay);
        overlayManager.add(objectStepOverlay);
        overlayManager.add(widgetStepOverlay);
    }

    @Override
    protected void shutDown()
    {
        statistics.clear();
        methodHandler.clear();

        overlayManager.remove(instructionOverlay);
        overlayManager.remove(statisticsOverlay);
        overlayManager.remove(coalBagOverlay);
        overlayManager.remove(itemStepOverlay);
        overlayManager.remove(objectStepOverlay);
        overlayManager.remove(widgetStepOverlay);
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        GameObject gameObject = event.getGameObject();

        switch (gameObject.getId()) {
            case CONVEYOR_BELT:
            case BAR_DISPENSER:
            case BANK_CHEST:
                objectManager.add(gameObject);
                isEnabled = true;
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        GameObject gameObject = event.getGameObject();

        switch (gameObject.getId()) {
            case CONVEYOR_BELT:
            case BAR_DISPENSER:
            case BANK_CHEST:
                statistics.clear();
                methodHandler.clear();
                isEnabled = false;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() != GameState.LOGGED_IN) {
            methodHandler.clear();
            isEnabled = false;
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (!isEnabled) return;

        if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
            methodHandler.setMethodFromInventory();
        }

        // handle any inventory or bank changes
        methodHandler.next();
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (!isEnabled) return;

        statistics.onFurnaceUpdate();
        state.updatePreviousFurnaceQuantity();

        // handle furnace ore/bar quantity changes
        methodHandler.next();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (!isEnabled) return;
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        Matcher emptyMatcher = COAL_EMPTY_MESSAGE.matcher(event.getMessage());
        Matcher filledMatcher = COAL_FULL_MESSAGE.matcher(event.getMessage());

        if (emptyMatcher.matches()) {
            state.emptyCoalBag();

        } else if (filledMatcher.matches()) {
            state.fillCoalBag();
        }

        // handle coal bag changes
        methodHandler.next();
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!isEnabled) return;

        if (event.getMenuOption().equals(FILL_ACTION)) state.fillCoalBag();
        if (event.getMenuOption().equals(EMPTY_ACTION)) state.emptyCoalBag();
        if (event.getMenuOption().equals(DRINK_ACTION)) statistics.drinkStamina();

        // handle coal bag changes
        methodHandler.next();
    }

    @Subscribe
    public void onOverlayMenuClicked(OverlayMenuClicked event)
    {
        if (event.getOverlay() == instructionOverlay &&
            event.getEntry().getOption().equals(EasyBlastFurnaceInstructionOverlay.RESET_ACTION)) {
            methodHandler.clear();
        }
        if (event.getOverlay() == statisticsOverlay &&
            event.getEntry().getOption().equals(EasyBlastFurnaceStatisticsOverlay.CLEAR_ACTION)) {
            statistics.clear();
        }
    }

    @Provides
    EasyBlastFurnaceConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(EasyBlastFurnaceConfig.class);
    }
}
