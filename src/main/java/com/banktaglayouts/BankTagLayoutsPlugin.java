package com.banktaglayouts;

import com.banktaglayouts.invsetupsstuff.InventorySetup;
import com.banktaglayouts.invsetupsstuff.InventorySetupsAdapter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Runnables;
import com.google.gson.Gson;
import com.google.inject.Provides;
import com.jogamp.common.util.VersionNumber;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.MessageNode;
import net.runelite.api.Point;
import net.runelite.api.ScriptEvent;
import net.runelite.api.ScriptID;
import net.runelite.api.Varbits;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.DraggingWidgetChanged;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.MenuShouldLeftClick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.game.RunepouchRune;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.TabInterface;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Bank Tag Layouts",
	description = "Right click a bank tag tabs and click \"Enable layout\", select the tag tab, then drag items in the tag to reposition them.",
	tags = {"bank", "tag", "layout"}
)
@PluginDependency(BankTagsPlugin.class)
public class BankTagLayoutsPlugin extends Plugin implements MouseListener
{
	public static final IntPredicate FILTERED_CHARS = c -> "</>:".indexOf(c) == -1;

	public static final Color itemTooltipColor = new Color(0xFF9040);

	public static final String CONFIG_GROUP = "banktaglayouts";
	public static final String LAYOUT_CONFIG_KEY_PREFIX = "layout_";
	public static final String INVENTORY_SETUPS_LAYOUT_CONFIG_KEY_PREFIX = "inventory_setups_layout_";
	public static final String BANK_TAG_STRING_PREFIX = "banktaglayoutsplugin:";
	public static final String LAYOUT_EXPLICITLY_DISABLED = "DISABLED";

	public static final String ENABLE_LAYOUT = "Enable layout";
	public static final String DISABLE_LAYOUT = "Delete layout";
	public static final String IMPORT_LAYOUT = "Import tag tab with layout";
	public static final String EXPORT_LAYOUT = "Export tag tab with layout";
	public static final String REMOVE_FROM_LAYOUT_MENU_OPTION = "Remove-layout";
	public static final String PREVIEW_AUTO_LAYOUT = "Preview auto layout";
	public static final String DUPLICATE_ITEM = "Duplicate-item";
	public static final String REMOVE_DUPLICATE_ITEM = "Remove-duplicate-item";

	public static final int BANK_ITEM_WIDTH = 36;
	public static final int BANK_ITEM_HEIGHT = 32;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	public ItemManager itemManager;

	@Inject
	public ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	// This is package-private for use in FakeItemOverlay because if it's @Injected there it's a different TabInterface due to some bug I don't understand.
	@Inject
	TabInterface tabInterface;

	@Inject
	private TagManager tagManager;

	@Inject
	private FakeItemOverlay fakeItemOverlay;

	@Inject
	private BankSearch bankSearch;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private BankTagLayoutsConfig config;

	@Inject
	public Gson gson;

	// The current indexes for where each widget should appear in the custom bank layout. Should be ignored if there is not tab active.
	private final Map<Integer, Widget> indexToWidget = new HashMap<>();

	// Copied from the rune pouch plugin
	private static final Varbits[] AMOUNT_VARBITS = {Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3};
	private static final Varbits[] RUNE_VARBITS = {Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3};

	private Widget showLayoutPreviewButton = null;
	private Widget applyLayoutPreviewButton = null;
	private Widget cancelLayoutPreviewButton = null;

	private LayoutableThing lastLayoutable = null;
	private int lastHeight = Integer.MAX_VALUE;

	final AntiDragPluginUtil antiDrag = new AntiDragPluginUtil(this);
	private final LayoutGenerator layoutGenerator = new LayoutGenerator(this);

	private void updateButton() {
		if (showLayoutPreviewButton == null) {
			Widget parent = client.getWidget(WidgetInfo.BANK_CONTENT_CONTAINER);
			showLayoutPreviewButton = parent.createChild(-1, WidgetType.GRAPHIC);

			showLayoutPreviewButton.setOriginalHeight(18);
			showLayoutPreviewButton.setOriginalWidth(18);
			showLayoutPreviewButton.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM);
			showLayoutPreviewButton.setOriginalX(434);
			showLayoutPreviewButton.setOriginalY(45);
			showLayoutPreviewButton.setSpriteId(Sprites.AUTO_LAYOUT.getSpriteId());

			showLayoutPreviewButton.setOnOpListener((JavaScriptCallback) (e) -> showLayoutPreview());
			showLayoutPreviewButton.setHasListener(true);
			showLayoutPreviewButton.revalidate();
			showLayoutPreviewButton.setAction(0, PREVIEW_AUTO_LAYOUT);

			applyLayoutPreviewButton = parent.createChild(-1, WidgetType.GRAPHIC);

			applyLayoutPreviewButton.setOriginalHeight(18);
			applyLayoutPreviewButton.setOriginalWidth(18);
			applyLayoutPreviewButton.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM);
			applyLayoutPreviewButton.setOriginalX(434 - 30);
			applyLayoutPreviewButton.setOriginalY(45);
			applyLayoutPreviewButton.setSpriteId(Sprites.APPLY_PREVIEW.getSpriteId());
			applyLayoutPreviewButton.setNoClickThrough(true);

			applyLayoutPreviewButton.setOnOpListener((JavaScriptCallback) (e) -> applyLayoutPreview());
			applyLayoutPreviewButton.setHasListener(true);
			applyLayoutPreviewButton.revalidate();
			applyLayoutPreviewButton.setAction(0, "Use this layout");

			cancelLayoutPreviewButton = parent.createChild(-1, WidgetType.GRAPHIC);

			cancelLayoutPreviewButton.setOriginalHeight(18);
			cancelLayoutPreviewButton.setOriginalWidth(18);
			cancelLayoutPreviewButton.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM);
			cancelLayoutPreviewButton.setOriginalX(434);
			cancelLayoutPreviewButton.setOriginalY(45);
			cancelLayoutPreviewButton.setSpriteId(Sprites.CANCEL_PREVIEW.getSpriteId());
			cancelLayoutPreviewButton.setNoClickThrough(true);

			cancelLayoutPreviewButton.setOnOpListener((JavaScriptCallback) (e) -> cancelLayoutPreview());
			cancelLayoutPreviewButton.setHasListener(true);
			cancelLayoutPreviewButton.revalidate();
			cancelLayoutPreviewButton.setAction(0, "Cancel preview");
		}

		hideLayoutPreviewButtons(!isShowingPreview());
		showLayoutPreviewButton.setHidden(!(config.showAutoLayoutButton() && getCurrentLayoutableThing() != null && !isShowingPreview()));
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetID.BANK_GROUP_ID) showLayoutPreviewButton = null; // when the bank widget is unloaded or loaded (not sure which) the button is removed from it somehow. So, set it to null so that it will be regenerated.
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(fakeItemOverlay);
		spriteManager.addSpriteOverrides(Sprites.values());
		mouseManager.registerMouseListener(this);
		keyManager.registerKeyListener(antiDrag);

		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN) {
				showLayoutPreviewButton = null;
				updateButton();
				bankSearch.layoutBank();
			}
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(fakeItemOverlay);
		spriteManager.removeSpriteOverrides(Sprites.values());
		mouseManager.unregisterMouseListener(this);
		keyManager.unregisterKeyListener(antiDrag);

		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN) {
				indexToWidget.clear();
				cancelLayoutPreview();
				showLayoutPreviewButton.setHidden(true);

				bankSearch.layoutBank();
			}
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CONFIG_GROUP.equals(event.getGroup())) {
			if ("layoutEnabledByDefault".equals(event.getKey())) {
				clientThread.invokeLater(() -> applyCustomBankTagItemPositions());
			} else if ("showAutoLayoutButton".equals(event.getKey())) {
				clientThread.invokeLater(this::updateButton);
			} else if ("useWithInventorySetups".equals(event.getKey())) {
				clientThread.invokeLater(bankSearch::layoutBank);
			}
		} else if (BankTagsPlugin.CONFIG_GROUP.equals(event.getGroup()) && BankTagsPlugin.TAG_TABS_CONFIG.equals(event.getKey())) {
			handlePotentialTagRename(event);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOGGED_IN) {
			checkVersionUpgrade();
		}
	}

	private void onVersionUpgraded(VersionNumber previousVersion, VersionNumber newVersion) {
		if (previousVersion.compareTo(new VersionNumber(1, 4, 10)) < 0)
		{
			if (config.updateMessages())
			{
				clientThread.invokeLater(() -> {
					chatMessage(ColorUtil.wrapWithColorTag("Bank Tag Layouts ", Color.RED) + "new version: " + "1.4.10");
					chatMessage(" - " + "New Auto-layout mode \"Presets\" shows your gear and inventory in a prettier way. You can switch to it in the plugin's config.");
				});
			}
		}
		if (previousVersion.compareTo(new VersionNumber(1, 4, 11)) < 0)
		{
			String prefix = CONFIG_GROUP + "." + INVENTORY_SETUPS_LAYOUT_CONFIG_KEY_PREFIX;
			for (String key : configManager.getConfigurationKeys(prefix))
			{
				String inventorySetupName = key.substring(prefix.length());
				String layoutString = configManager.getConfiguration(CONFIG_GROUP, INVENTORY_SETUPS_LAYOUT_CONFIG_KEY_PREFIX + inventorySetupName);
				String escapedKey = LayoutableThing.inventorySetup(inventorySetupName).configKey();
				configManager.setConfiguration(CONFIG_GROUP, escapedKey, layoutString);
			}
		}
	}

	private void checkVersionUpgrade() {
		try (InputStream is = BankTagLayoutsPlugin.class.getResourceAsStream("/version.txt"))
		{
			Properties props = new Properties();

			try
			{
				props.load(is);
			}
			catch (IOException e)
			{
				log.error("unable to load version number", e);
				return;
			}

			VersionNumber buildVersion = new VersionNumber(props.getProperty("version"));
			String previousVersionString = configManager.getConfiguration(CONFIG_GROUP, "version");
			// This is a best guess - they could have had the plugin installed previously but if they don't have any layouts set they probably don't use it.
			boolean assumeFreshInstall =
				previousVersionString == null
				&& configManager.getConfigurationKeys(CONFIG_GROUP + "." + LAYOUT_CONFIG_KEY_PREFIX).size() == 0
				&& configManager.getConfigurationKeys(CONFIG_GROUP + "." + INVENTORY_SETUPS_LAYOUT_CONFIG_KEY_PREFIX).size() == 0;
			VersionNumber previousVersion = new VersionNumber(previousVersionString);
			if (buildVersion.compareTo(previousVersion) > 0 && !assumeFreshInstall)
			{
				onVersionUpgraded(previousVersion, buildVersion);
			}
			configManager.setConfiguration(CONFIG_GROUP, "version", buildVersion);
		}
		catch (IOException e) {
			log.error("unable to close version file.", e);
		}
	}

	private void handlePotentialTagRename(ConfigChanged event) {
		Set<String> oldTags = new HashSet<>(Text.fromCSV(event.getOldValue()));
		Set<String> newTags = new HashSet<>(Text.fromCSV(event.getNewValue()));
		// Compute the diff between the two lists.
		Iterator<String> iter = oldTags.iterator();
		while (iter.hasNext()) {
			String oldTag = iter.next();
			if (newTags.remove(oldTag)) {
				iter.remove();
			}
		}

		// Check if it's a rename or something else.
		if (oldTags.size() != 1 || newTags.size() != 1) return;

		LayoutableThing oldName = LayoutableThing.bankTag(oldTags.iterator().next());
		String newName = newTags.iterator().next();

		Layout oldLayout = getBankOrder(oldName);
		if (oldLayout != null) {
			saveLayout(LayoutableThing.bankTag(newName), oldLayout);
			configManager.unsetConfiguration(CONFIG_GROUP, oldName.configKey());
		}
	}

	@Provides
	BankTagLayoutsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankTagLayoutsConfig.class);
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		if (!log.isDebugEnabled()) return;

		if ("clearversion".equals(commandExecuted.getCommand())) {
			configManager.unsetConfiguration(CONFIG_GROUP, "version");
			System.out.println("cleared version number.");
		}
		if ("checkversion".equals(commandExecuted.getCommand())) {
			System.out.println("checking version number");
			checkVersionUpgrade();
		}

		if ("itemname".equals(commandExecuted.getCommand())) {
			String[] arguments = commandExecuted.getArguments();
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "Item name of " + arguments[0], itemName(Integer.valueOf(arguments[0])), "bla");
		}
		if ("placeholder".equals(commandExecuted.getCommand())) {
			String[] arguments = commandExecuted.getArguments();
			int itemId = Integer.parseInt(arguments[0]);
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "" + itemId, itemName(itemId) + " is a " + isPlaceholder(itemId) + " and it's reversed id is " + switchPlaceholderId(itemId) + " and again " + switchPlaceholderId(switchPlaceholderId(itemId)), "bla");
		}
	}

	private void applyLayoutPreview() {
		if (previewLayoutable.isBankTab()) {
			for (Integer itemId : previewLayout.getAllUsedItemIds()) {
				if (!copyPaste.findTag(itemId, previewLayoutable.name)) {
					log.debug("adding item " + itemNameWithId(itemId) + " to tag");
					tagManager.addTag(itemId, previewLayoutable.name, false);
				}
			}
		}

		saveLayoutNonPreview(previewLayoutable, previewLayout);

		cancelLayoutPreview();
		bankSearch.layoutBank();
	}

	private void hideLayoutPreviewButtons(boolean hide) {
		if (applyLayoutPreviewButton != null) applyLayoutPreviewButton.setHidden(hide);
		if (cancelLayoutPreviewButton != null) cancelLayoutPreviewButton.setHidden(hide);
		if (showLayoutPreviewButton != null && config.showAutoLayoutButton() && getCurrentLayoutableThing() != null) showLayoutPreviewButton.setHidden(!hide);
	}

	private void cancelLayoutPreview() {
		previewLayout = null;
		previewLayoutable = null;

		hideLayoutPreviewButtons(true);

		applyCustomBankTagItemPositions();
	}

	/** null indicates that there should not be a preview shown. */
	private Layout previewLayout = null;
	private LayoutableThing previewLayoutable = null;

	private final InventorySetupsAdapter inventorySetupsAdapter = new InventorySetupsAdapter(this);

	private void showLayoutPreview() {

		if (isShowingPreview()) return;
		LayoutableThing currentLayoutableThing = getCurrentLayoutableThing();
		if (currentLayoutableThing == null) {
			chatMessage("Select a tag tab before using this feature.");
			return;
		} else {
			// TODO allow creation of new tab.
		}

		if (currentLayoutableThing.isBankTab()) {
			List<Integer> equippedGear = getEquippedGear();
			List<Integer> inventory = getInventory();
			if (equippedGear.stream().noneMatch(id -> id > 0) && inventory.stream().noneMatch(id -> id > 0)) {
				chatMessage("This feature uses your equipped items and inventory to automatically create a bank tag layout, but you don't have any items equipped or in your inventory.");
				return;
			}

			hideLayoutPreviewButtons(false);

			Layout currentLayout = getBankOrderNonPreview(currentLayoutableThing);
			if (currentLayout == null) currentLayout = Layout.emptyLayout();

			previewLayout = layoutGenerator.generateLayout(equippedGear, inventory, getRunePouchContents(), Collections.emptyList(), currentLayout, getAutoLayoutDuplicateLimit(), config.autoLayoutStyle());
		} else {
			InventorySetup inventorySetup = inventorySetupsAdapter.getInventorySetup(currentLayoutableThing.name);

			Layout currentLayout = getBankOrderNonPreview(currentLayoutableThing);
			if (currentLayout == null) currentLayout = Layout.emptyLayout();

			previewLayout = layoutGenerator.basicInventorySetupsLayout(inventorySetup, currentLayout, getAutoLayoutDuplicateLimit(), config.autoLayoutStyle());
		}

		hideLayoutPreviewButtons(false);

		previewLayoutable = currentLayoutableThing;

		applyCustomBankTagItemPositions();
	}

	private int getAutoLayoutDuplicateLimit() {
		return !config.autoLayoutDuplicatesEnabled() ? 0 : config.autoLayoutDuplicateLimit();
	}

	private List<Integer> getEquippedGear() {
		ItemContainer container = client.getItemContainer(InventoryID.EQUIPMENT);
		if (container == null) return Collections.emptyList();
		return Arrays.stream(container.getItems()).map(Item::getId).collect(Collectors.toList());
	}

	/**
	 * empty spaces before an item are always -1, empty spaces after an item may be -1 or may not be included in the
	 * list at all.
	 */
	private List<Integer> getInventory() {
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		if (container == null) return Collections.emptyList();
		return Arrays.stream(container.getItems()).map(w -> w.getId()).collect(Collectors.toList());
	}

	private boolean isShowingPreview() {
		return previewLayout != null;
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		if (checkInventorySetup + 1 == client.getGameCycle()) {
			updateInventorySetupShown();
		}

		Widget widget = client.getWidget(WidgetInfo.BANK_CONTAINER);
		if (widget == null || widget.isHidden()) {
			return;
		}

		// This fixes a vanilla bug where it's possible open a placeholder's right-click menu while trying to withdraw an item - see https://github.com/geheur/bank-tag-custom-layouts/issues/33 for more info.
		// I would do this in onMenuEntryAdded but client.getDraggedOnWidget() does not feel trustworthy at that point in the client tick - it is often null when it shouldn't bee - consistently and circumventable, but this makes me not trust the return value.
		if (
				config.preventVanillaPlaceholderMenuBug() &&
						client.getDraggedWidget() != null &&
						client.getDraggedOnWidget() != null
		) {
			MenuEntry[] menuEntries = client.getMenuEntries();
			if (menuEntries.length >= 1)
			{
				MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
				if (
						WidgetInfo.TO_GROUP(menuEntry.getParam1()) == WidgetID.BANK_GROUP_ID &&
								menuEntry.getOption().equals("Release")
				) {
					menuEntry.setType(MenuAction.CC_OP);
				}
			}
		}

		sawMenuEntryAddedThisClientTick = false;
	}

	private List<Integer> getRunePouchContents() {
		List<Integer> runes = new ArrayList<>(0);
		for (int i = 0; i < AMOUNT_VARBITS.length; i++)
		{
			int amount = client.getVar(AMOUNT_VARBITS[i]);
			if (amount <= 0) {
				continue;
			}
			int runeId = client.getVar(RUNE_VARBITS[i]);
			RunepouchRune rune = RunepouchRune.getRune(runeId);
			if (rune != null) {
				runes.add(rune.getItemId());
			}
		}
		return runes;
	}

	private String inventorySetup = null;
	private void updateInventorySetupShown() {
		Widget bankTitleBar = client.getWidget(WidgetInfo.BANK_TITLE_BAR);
		String newSetup = null;
		if (bankTitleBar != null)
		{
			String bankTitle = bankTitleBar.getText();
			Matcher matcher = Pattern.compile("Inventory Setup <col=ff0000>(?<setup>.*) - (?<subfilter>.*)</col>.*").matcher(bankTitle);
			if (matcher.matches())
			{
				newSetup = matcher.group("setup");
			}
		}

		inventorySetup = newSetup;
	}

	int checkInventorySetup = 0;

	@Subscribe
	public void onWidgetClosed(WidgetClosed widgetClosed) {
		if (widgetClosed.getGroupId() == WidgetID.BANK_GROUP_ID) {
			checkInventorySetup = client.getGameCycle();
		}
	}

	@Subscribe(priority = -1f) // "Bank Tags" plugin also sets the scroll bar height; run after it. We also need to run after "Inventory Setups" to get the bank title it sets.
	public void onScriptPreFired(ScriptPreFired event) {
		if (event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING) {
			return;
		}

		updateInventorySetupShown();

		LayoutableThing layoutable = getCurrentLayoutableThing();
		if (layoutable == null) {
			return;
		}

		Layout layout = getBankOrder(layoutable);
		if (layout == null) {
			return;
		}

		int maxIndex = layout.getAllUsedIndexes().stream().max(Integer::compare).orElse(0);
		int height = getYForIndex(maxIndex) + BANK_ITEM_HEIGHT;

		// This is prior to bankmain_finishbuilding running, so the arguments are still on the stack. Overwrite
		// argument int12 (7 from the end) which is the height passed to if_setscrollsize
		client.getIntStack()[client.getIntStackSize() - 7] = height;
	}

	@Subscribe(priority = -1f) // I want to run after the Bank Tags plugin does, since it will interfere with the layout-ing if hiding tab separators is enabled.
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == ScriptID.BANKMAIN_BUILD) {
			LayoutableThing layoutable = getCurrentLayoutableThing();
			if (layoutable == null || !layoutable.equals(lastLayoutable)) {
				cancelLayoutPreview();
			}

			applyCustomBankTagItemPositions(false);

			lastLayoutable = layoutable;

			updateButton();
		}
	}

	private void importLayout() {
		final String clipboardData;
		try {
			clipboardData = Toolkit
					.getDefaultToolkit()
					.getSystemClipboard()
					.getData(DataFlavor.stringFlavor)
					.toString()
					.trim();
		} catch (UnsupportedFlavorException | IOException e) {
			chatErrorMessage("import failed: couldn't get an import string from the clipboard");
			return;
		}

		if (!clipboardData.startsWith(BANK_TAG_STRING_PREFIX)) {
			// TODO try to import the tag as a normal tag?.
			chatErrorMessage("import failed: Invalid format. layout-ed tag data starts with \"" + BANK_TAG_STRING_PREFIX + "\"; did you copy the wrong thing?");
			return;
		}

		String[] split = clipboardData.split(",banktag:");
		if (split.length != 2) {
			chatErrorMessage("import failed: invalid format. layout string doesn't include regular bank tag data (It should say \"banktag:\" somewhere in the import string). Maybe you didn't copy the whole thing?");
			return;
		}

		String prefixRemoved = split[0].substring(BANK_TAG_STRING_PREFIX.length());

		String name;
		String layoutString;
		int firstCommaIndex = prefixRemoved.indexOf(",");
		if (firstCommaIndex == -1) { // There are no items in this layout.
			name = prefixRemoved;
			layoutString = "";
		} else {
			name = prefixRemoved.substring(0, firstCommaIndex);
			layoutString = prefixRemoved.substring(name.length() + 1);
		}

		name = validateTagName(name);
		if (name == null) return; // it was invalid.

		Layout layout;
		try {
			layout = Layout.fromString(layoutString);
		} catch (NumberFormatException e) {
			chatErrorMessage("import failed: something in the layout data is not a number");
			return;
		}
		String tagString = split[1];

		log.debug("import string: {}, {}, {}", name, layoutString, split[1]);

		// If the tag has no items in it, it will not trigger the overwrite warning. This is not intuitive, but I don't care enough to fix it.
		if (!tagManager.getItemsForTag(name).isEmpty()) {
			String finalName = name;
			chatboxPanelManager.openTextMenuInput("Tag tab with same name (" + name + ") already exists.")
					.option("Keep both, renaming imported tab", () -> {
						clientThread.invokeLater(() -> { // If the option is selected by a key, this will not be on the client thread.
							String newName = generateUniqueName(finalName);
							if (newName == null) {
								chatErrorMessage("import failed: couldn't find a unique name. do you literally have 100 similarly named tags???????????");
								return;
							}
							importLayout(newName, layout, tagString);
						});
					})
					.option("Overwrite existing tab", () -> {
						clientThread.invokeLater(() -> { // If the option is selected by a key, this will not be on the client thread.
							importLayout(finalName, layout, tagString);
						});
					})
					.option("Cancel", Runnables::doNothing)
					.build();
		} else {
			importLayout(name, layout, tagString);
		}
	}

	private String generateUniqueName(String name) {
		for (int i = 2; i < 100; i++) {
			String newName = "(" + i + ") " + name;
			if (tagManager.getItemsForTag(newName).isEmpty()) {
				return newName;
			}
		}
		return null;
	}

	private void importLayout(String name, Layout layout, String tagString) {
		boolean successful = importBankTag(name, tagString);
		if (!successful) return;

		saveLayout(LayoutableThing.bankTag(name), layout);

		chatMessage("Imported layout-ed tag tab \"" + name + "\"");

		applyCustomBankTagItemPositions();
	}

	void saveLayout(LayoutableThing layoutable, Layout layout) {
		if (isShowingPreview()) {
			previewLayout = layout;
			return;
		}

		saveLayoutNonPreview(layoutable, layout);
	}

	private void saveLayoutNonPreview(LayoutableThing layoutable, Layout layout) {
		configManager.setConfiguration(CONFIG_GROUP, layoutable.configKey(), layout.toString());
	}

	private String validateTagName(String name) {
		StringBuilder sb = new StringBuilder();
		for (char c : name.toCharArray()) {
			if (FILTERED_CHARS.test(c)) {
				sb.append(c);
			}
		}

		if (sb.length() == 0) {
			chatErrorMessage("import failed: tag name does not contain any valid characters.");
			return null;
		}

		return sb.toString();
	}

	private UsedToBeReflection copyPaste = new UsedToBeReflection(this);

	// TODO what is the purpose of the return value.
	private boolean importBankTag(String name, String tagString) {
		log.debug("importing tag data. " + tagString);
		final Iterator<String> dataIter = Text.fromCSV(tagString).iterator();
		dataIter.next(); // skip name.

		final String icon = dataIter.next();

		copyPaste.setIcon(name, icon);

		while (dataIter.hasNext()) {
			int itemId = Integer.parseInt(dataIter.next());
			tagManager.addTag(itemId, name, itemId < 0);
		}

		copyPaste.saveNewTab(name);
		copyPaste.loadTab(name);

		return true;
	}

	public boolean hasLayoutEnabled(LayoutableThing layoutable) {
		if (layoutable == null) return false;
		if (isShowingPreview()) return true;

		String configuration = configManager.getConfiguration(CONFIG_GROUP, layoutable.configKey());
		if (LAYOUT_EXPLICITLY_DISABLED.equals(configuration)) return false;
		return configuration != null || (layoutable.isBankTab() && config.layoutEnabledByDefault()) || (layoutable.isInventorySetup() && config.useWithInventorySetups());
	}

	private void enableLayout(LayoutableThing layoutable) {
		saveLayout(layoutable, Layout.emptyLayout());
		if (layoutable.equals(getCurrentLayoutableThing())) {
			applyCustomBankTagItemPositions();
		}
	}

	private void disableLayout(String bankTagName) {
		chatboxPanelManager.openTextMenuInput("Delete layout for " + bankTagName + "?")
				.option("Yes", () ->
						clientThread.invoke(() ->
						{
							configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, LAYOUT_EXPLICITLY_DISABLED);
							if (tabInterface.getActiveTab() != null && bankTagName.equals(tabInterface.getActiveTab().getTag())) {
								bankSearch.layoutBank();
							}
						})
				)
				.option("No", Runnables::doNothing)
				.build();
	}

	private void applyCustomBankTagItemPositions() {
		applyCustomBankTagItemPositions(true);
	}

	private void applyCustomBankTagItemPositions(boolean setScroll) {
		fakeItems.clear();

		LayoutableThing layoutable = getCurrentLayoutableThing();
		if (layoutable == null) {
			return;
		}

		log.debug("applyCustomBankTagItemPositions: " + layoutable);

		indexToWidget.clear();

		Layout layout = getBankOrder(layoutable);
		if (layout == null) {
			return; // layout not enabled.
		}

		List<Widget> bankItems = Arrays.stream(client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren())
				.filter(bankItem -> !bankItem.isHidden() && bankItem.getItemId() >= 0)
				.collect(Collectors.toList());

		if (!hasLayoutEnabled(layoutable)) {
			for (Widget bankItem : bankItems) {
				bankItem.setOnDragCompleteListener((JavaScriptCallback) (ev) -> {
					boolean tutorialShown = tutorialMessage();

					if (!tutorialShown) bankReorderWarning(ev);
				});
			}
			return;
		}

		if (!isShowingPreview()) { // I don't want to clean layout items when displaying a preview. This could result in some layout placeholders being auto-removed due to not being in the tab.
			cleanItemsNotInBankTag(layout, layoutable);
		}

		indexToWidget.putAll(assignItemPositions(layout, bankItems));
		moveDuplicateItem();
		updateFakeItems(layout);

		for (Widget bankItem : bankItems) {
			bankItem.setOnDragCompleteListener((JavaScriptCallback) (ev) -> customBankTagOrderInsert(layoutable, ev.getSource()));
		}

		setItemPositions(indexToWidget);

		// Necessary as applyCustomBankTagItemPositions can be called after an item's layout position is changed. This doesn't fire BANKMAIN_BUILD, so our PreScriptFired subscriber doesn't change the scrollbar height, and the item's movement can change the height of the layout if it is moved below the last row or if it is the last item in the layout and is alone on its own row and was moved upwards.
		int maxIndex = layout.getAllUsedIndexes().stream().max(Integer::compare).orElse(0);
		int height = getYForIndex(maxIndex) + BANK_ITEM_HEIGHT + 8;
		if (setScroll && layoutable.equals(lastLayoutable) && height != lastHeight)
		{
			resizeBankContainerScrollbar(height, lastHeight);
		}
		lastHeight = height;

		saveLayout(layoutable, layout);
		log.debug("saved tag " + layoutable);
	}

	/**
	 * Generates a map of widgets to the bank indexes where they should show up in the laid-out tag. Does not update fake items.
	 */
	Map<Integer, Widget> assignItemPositions(Layout layout, List<Widget> bankItems)
	{
		Map<Integer, Widget> indexToWidget = new HashMap<>();
		assignVariantItemPositions(layout, bankItems, indexToWidget);
		// TODO check if the existance of this method is just a performance boost.
		assignNonVariantItemPositions(layout, bankItems, indexToWidget);
		return indexToWidget;
	}

	private void updateFakeItems(Layout layout)
	{
		fakeItems = calculateFakeItems(layout, indexToWidget);
	}

	// TODO this is n^2. There are multiple places I think where I do such an operation, so doing something about this would be nice.
	Set<FakeItem> calculateFakeItems(Layout layout, Map<Integer, Widget> indexToWidget)
	{
		Set<FakeItem> fakeItems = new HashSet<>();
		for (Map.Entry<Integer, Integer> entry : layout.allPairs()) {
			Integer index = entry.getKey();
			if (indexToWidget.containsKey(index)) continue;

			int itemId = entry.getValue();
			Optional<Widget> any = layout.allPairs().stream()
					.filter(e -> e.getValue() == itemId)
					.map(e -> indexToWidget.get(e.getKey()))
					.filter(widget -> widget != null)
					.findAny();

			boolean isLayoutPlaceholder = !any.isPresent();
			int quantity = any.isPresent() ? any.get().getItemQuantity() : -1;
			int fakeItemItemId = any.isPresent() ? any.get().getItemId() : itemId;
//			fakeItems.add(new FakeItem(index, getNonPlaceholderId(fakeItemItemId), isLayoutPlaceholder, quantity));
			fakeItems.add(new FakeItem(index, fakeItemItemId, isLayoutPlaceholder, quantity));
		}
		return fakeItems;
	}

	LayoutableThing getCurrentLayoutableThing() {
		boolean isBankTag = tabInterface.isActive();
		if (!isBankTag && !(inventorySetup != null && config.useWithInventorySetups())) {
			return null;
		}
		String name = isBankTag ? tabInterface.getActiveTab().getTag() : inventorySetup;
		return new LayoutableThing(name, isBankTag);
	}

	private void bankReorderWarning(ScriptEvent ev) {
		if (
				config.warnForAccidentalBankReorder()
						&& ev.getSource().getId() == WidgetInfo.BANK_ITEM_CONTAINER.getId() && tabInterface.isActive()
						&& client.getDraggedOnWidget() != null
						&& client.getDraggedOnWidget().getId() == WidgetInfo.BANK_ITEM_CONTAINER.getId() && tabInterface.isActive()
						&& !hasLayoutEnabled(getCurrentLayoutableThing())
						&& !Boolean.parseBoolean(configManager.getConfiguration(BankTagsPlugin.CONFIG_GROUP, "preventTagTabDrags"))
		) {
			chatErrorMessage("You just reordered your actual bank!");
			chatMessage("If you wanted to use a bank tag layout, make sure you enable it for this tab first.");
			chatMessage("You should consider enabling \"Prevent tag tab item dragging\" in the Bank Tags plugin.");
			chatMessage("You can disable this warning in the Bank Tag Layouts config.");
		}
	}

	private boolean tutorialMessageShown = false;
	private boolean tutorialMessage() {
		if (!config.tutorialMessage()) return false;

		for (String key : configManager.getConfigurationKeys(CONFIG_GROUP)) {
			if (key.startsWith(CONFIG_GROUP + "." + LAYOUT_CONFIG_KEY_PREFIX)) { // They probably already know what to do if they have a key like this set.
				return false;
			}
		}

		if (!tutorialMessageShown) {
			tutorialMessageShown = true;
			chatMessage("If you want to use Bank Tag Layouts, enable it for the tab by right clicking the tag tab and clicking \"Enable layout\".");
			chatMessage("To disable this message, to go the Bank Tag Layouts config and disable \"Layout enable tutorial message\".");
			return true;
		}
		return false;
	}

	private void assignNonVariantItemPositions(Layout layout, List<Widget> bankItems, Map<Integer, Widget> indexToWidget) {
		for (Widget bankItem : bankItems) {
			int itemId = bankItem.getItemId();

			int nonPlaceholderId = getNonPlaceholderId(itemId);

			if (!itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
//				log.debug("\tassigning position for " + itemName(itemId) + itemId + ": ");

				Integer indexForItem = layout.getIndexForItem(itemId);
				if (indexForItem == -1) {
					// swap the item with its placeholder (or vice versa) and try again.
					int otherItemId = switchPlaceholderId(itemId);
					indexForItem = layout.getIndexForItem(otherItemId);
				}

				if (indexForItem == -1) {
					// The item is not in the layout.
					indexForItem = layout.getFirstEmptyIndex();
					layout.putItem(itemId, indexForItem);
				}
				indexToWidget.put(indexForItem, bankItem);
			}
		}
	}

	public Set<FakeItem> fakeItems = new HashSet<>();

	@Override
	public MouseEvent mouseClicked(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	public volatile int draggedItemIndex = -1; // Used for fake items only, not real items.
	public int dragStartX = 0;
	public int dragStartY = 0;
	public int dragStartScroll = 0;

	@Override
	public MouseEvent mousePressed(MouseEvent mouseEvent) {
		mouseIsPressed = true;
		if (mouseEvent.getButton() != MouseEvent.BUTTON1 || !hasLayoutEnabled(getCurrentLayoutableThing()) || !config.showLayoutPlaceholders() || client.isMenuOpen()) return mouseEvent;
		if (isShowingPreview() && applyLayoutPreviewButton != null && applyLayoutPreviewButton.contains(client.getMouseCanvasPosition())) {
			return mouseEvent;
		}
		int index = getIndexForMousePosition(true);
		FakeItem fakeItem = fakeItems.stream().filter(fake -> fake.index == index).findAny().orElse(null);
		if (fakeItem != null) {
			draggedItemIndex = fakeItem.index;
			dragStartX = mouseEvent.getX();
			dragStartY = mouseEvent.getY();
			dragStartScroll = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getScrollY();
			antiDrag.startDrag();
			mouseEvent.consume();
		}
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent mouseEvent) {
		mouseIsPressed = false;
		if (mouseEvent.getButton() != MouseEvent.BUTTON1 || !hasLayoutEnabled(getCurrentLayoutableThing())) return mouseEvent;
		if (draggedItemIndex == -1) return mouseEvent;

		if (config.showLayoutPlaceholders()) {
			int draggedOnIndex = getIndexForMousePositionNoLowerLimit();
			clientThread.invokeLater(() -> {
				if (draggedOnIndex != -1 && antiDrag.mayDrag()) {
					customBankTagOrderInsert(getCurrentLayoutableThing(), draggedItemIndex, draggedOnIndex);
				}
				antiDrag.endDrag();
				draggedItemIndex = -1;
			});
		}

		mouseEvent.consume();
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseEntered(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseExited(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseMoved(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Data
	public static class FakeItem {
		public final int index;
		public final int itemId;
		public final boolean layoutPlaceholder;
		public final int quantity;
	}

	/**
	 * Used to run code in onMenuEntryAdded only once per client tick. Client tick events occur after MenuEntryAdded,
	 * so this flag is set in MenuEntryAdded and reset in ClientTick.
	 */
	boolean sawMenuEntryAddedThisClientTick = false;
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		Widget widget = client.getWidget(WidgetInfo.BANK_CONTAINER);
		if (widget == null || widget.isHidden()) {
			return;
		}

		if (!sawMenuEntryAddedThisClientTick) {
			sawMenuEntryAddedThisClientTick = true;

			// If you move the items when you're dragging an item over its duplicates, undesirable behavior occurs.
			if (!mouseIsPressed)
			{
				boolean movedItemWidget = moveDuplicateItem();
				if (movedItemWidget)
				{
					updateFakeItems(getBankOrder(getCurrentLayoutableThing()));
					setItemPositions(indexToWidget);
				}
			}
		}

		addBankTagTabMenuEntries(menuEntryAdded);
		addFakeItemMenuEntries(menuEntryAdded);
		addDuplicateItemMenuEntries(menuEntryAdded);
	}

	private void addBankTagTabMenuEntries(MenuEntryAdded menuEntryAdded)
	{
		if (WidgetInfo.TO_GROUP(menuEntryAdded.getActionParam1()) == WidgetInfo.BANK_CONTENT_CONTAINER.getGroupId()) {
			String bankTagName = Text.removeTags(menuEntryAdded.getTarget()).replace("\u00a0"," ");

			if ("Rename tag tab".equals(menuEntryAdded.getOption())) {
				LayoutableThing layoutable = LayoutableThing.bankTag(bankTagName);
				if (hasLayoutEnabled(layoutable)) {
					addEntry(bankTagName, EXPORT_LAYOUT);
				}

				addEntry(bankTagName, hasLayoutEnabled(layoutable) ? DISABLE_LAYOUT : ENABLE_LAYOUT);
			} else if ("New tag tab".equals(menuEntryAdded.getOption())) {
				if (!config.showAutoLayoutButton()) {
					addEntry(bankTagName, PREVIEW_AUTO_LAYOUT);
				}
				addEntry(bankTagName, IMPORT_LAYOUT);
			}
		}
	}

	private volatile boolean mouseIsPressed = false;

	/**
	 * Makes sure there is a real item under the mouse cursor if the mouse is over or near a duplicated item.
	 * @return true if an item widget was moved, false otherwise. If true, fake items should be updated and
	 * setItemPositions should be called, since this method does not do that.
	 */
	private boolean moveDuplicateItem()
	{
		if (getCurrentLayoutableThing() == null)
		{
			return false;
		}

		int mousePositionIndex = getIndexForMousePosition();
		Layout layout = getBankOrder(getCurrentLayoutableThing());
		if (layout == null) return false;
		int itemId = layout.getItemAtIndex(mousePositionIndex);

		if (itemId == -1)
		{
			return false;
		}

		int count = 0;
		List<Integer> indexes = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : layout.allPairs())
		{
			if (entry.getValue() == itemId) {
				count++;
				indexes.add(entry.getKey());
			}
		}
		if (count > 1) {
			for (Integer index : indexes)
			{
				if (indexToWidget.containsKey(index) && index != mousePositionIndex) {
					Widget widget = indexToWidget.get(index);
					indexToWidget.remove(index);
					indexToWidget.put(mousePositionIndex, widget);
					return true;
				}
			}
		}
		return false;
	}

	private void addDuplicateItemMenuEntries(MenuEntryAdded menuEntryAdded)
	{
		if (config.shiftModifierForExtraBankItemOptions() && !client.isKeyPressed(KeyCode.KC_SHIFT)) return;

		LayoutableThing layoutable = getCurrentLayoutableThing();
		if (layoutable == null) return;
		Layout layout = getBankOrder(layoutable);
		if (layout == null) return;

		int index = getIndexForMousePosition(true);
		if (index == -1) return;
		int itemIdAtIndex = layout.getItemAtIndex(index);

		if (itemIdAtIndex == -1) return;

		boolean isRealItem = indexToWidget.containsKey(index);
		if (!menuEntryAdded.getOption().equals("Examine") && isRealItem) return;

		boolean isLayoutPlaceholder = fakeItems.stream()
				.filter(fakeItem -> fakeItem.getIndex() == index && fakeItem.isLayoutPlaceholder()).findAny().isPresent();

		int itemCount = layout.countItemsWithId(itemIdAtIndex);
		if (itemCount > 1 && !isLayoutPlaceholder) {
			client.createMenuEntry(-1)
					.setOption(REMOVE_DUPLICATE_ITEM)
					.setTarget(ColorUtil.wrapWithColorTag(itemName(itemIdAtIndex), itemTooltipColor))
					.setType(MenuAction.RUNELITE_OVERLAY)
					.setParam0(index);
		}

		client.createMenuEntry(-1)
				.setOption(DUPLICATE_ITEM)
				.setTarget(ColorUtil.wrapWithColorTag(itemName(itemIdAtIndex), itemTooltipColor))
				.setType(MenuAction.RUNELITE_OVERLAY)
				.setParam0(index);

		if (!isRealItem) return; // layout placeholders already have "remove-layout" menu option which does the same thing as remove-duplicate-item.
	}

	private void addEntry(String menuTarget, String menuOption) {
		client.createMenuEntry(-2)
				.setOption(menuOption)
				.setTarget(ColorUtil.wrapWithColorTag(menuTarget, itemTooltipColor))
				.setType(MenuAction.RUNELITE);
	}

	private void addFakeItemMenuEntries(MenuEntryAdded menuEntryAdded) {
		if (!menuEntryAdded.getOption().equalsIgnoreCase("cancel")) return;

		LayoutableThing currentLayoutableThing = getCurrentLayoutableThing();
		if (!config.showLayoutPlaceholders() || !hasLayoutEnabled(currentLayoutableThing)) {
			return;
		}
		Layout layout = getBankOrder(currentLayoutableThing);

		int index = getIndexForMousePosition(true);
		if (index == -1) return;
		int itemIdAtIndex = layout.getItemAtIndex(index);

		if (itemIdAtIndex != -1 && !indexToWidget.containsKey(index)) {
			boolean preventPlaceholderMenuBug =
					config.preventVanillaPlaceholderMenuBug() &&
							client.getDraggedWidget() != null;

			client.createMenuEntry(-1)
					.setOption(REMOVE_FROM_LAYOUT_MENU_OPTION)
					.setType(preventPlaceholderMenuBug ? MenuAction.CC_OP : MenuAction.RUNELITE_OVERLAY)
					.setTarget(ColorUtil.wrapWithColorTag(itemName(itemIdAtIndex), itemTooltipColor))
					.setParam0(index);
		}
	}

	/**
	 * @return -1 if the mouse is not over a location where a bank item can be.
	 */
	int getIndexForMousePositionNoLowerLimit() {
		return getIndexForMousePosition(false, true);
	}

	/**
	 * @return -1 if the mouse is not over a location where a bank item can be.
	 */
	int getIndexForMousePosition() {
		return getIndexForMousePosition(false);
	}

	/**
	 * @param dontEnlargeClickbox If this is false, the clickbox used to calculate the clickbox will be larger (2 larger up and down, 6 larger left to right), so that there are no gaps between clickboxes in the bank interface.
	 * @return -1 if the mouse is not over a location where a bank item can be.
	 */
	int getIndexForMousePosition(boolean dontEnlargeClickbox) {
		return getIndexForMousePosition(dontEnlargeClickbox, false);
	}

	/**
	 * @param dontEnlargeClickbox If this is false, the clickbox used to calculate the clickbox will be larger (2 larger up and down, 6 larger left to right), so that there are no gaps between clickboxes in the bank interface.
	 * @param noLowerLimit Still return indexes when the mouse is below the bank container.
	 * @return -1 if the mouse is not over a location where a bank item can be.
	 */
	int getIndexForMousePosition(boolean dontEnlargeClickbox, boolean noLowerLimit) {
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null) return -1;
		Point mouseCanvasPosition = client.getMouseCanvasPosition();

		int mouseX = mouseCanvasPosition.getX();
		int mouseY = mouseCanvasPosition.getY();
		Rectangle bankBounds = bankItemContainer.getBounds();

		if (
				noLowerLimit && (mouseX < bankBounds.getMinX() || mouseX > bankBounds.getMaxX() || mouseY < bankBounds.getMinY())
						|| !noLowerLimit && !bankBounds.contains(new java.awt.Point(mouseX, mouseY))) {
			return -1;
		}

		Point canvasLocation = bankItemContainer.getCanvasLocation();
		int scrollY = bankItemContainer.getScrollY();
		int row = (mouseY - canvasLocation.getY() + scrollY + 2) / BANK_ITEM_WIDTH;
		int col = (int) Math.floor((mouseX - canvasLocation.getX() - 51 + 6) / 48f);
		int index = row * 8 + col;
		if (row < 0 || col < 0 || col > 7 || index < 0) return -1;
		if (dontEnlargeClickbox) {
			int xDistanceIntoItem = (mouseX - canvasLocation.getX() - 51 + 6) % 48;
			int yDistanceIntoItem = (mouseY - canvasLocation.getY() + scrollY + 2) % BANK_ITEM_WIDTH;
			if (xDistanceIntoItem < 6 || xDistanceIntoItem >= 42 || yDistanceIntoItem < 2 || yDistanceIntoItem >= 34) {
				return -1;
			}
		}
		return index;
	}

	// TODO do I actually want to remove variant items from the tag? What if I'm just removing one of the layout items, and do not actually want to remove it from the tag? That seems very reasonable.
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		Widget widget = client.getWidget(WidgetInfo.BANK_CONTAINER);
		if (widget == null || widget.isHidden()) {
			return;
		}

		// This fixes a vanilla bug where it's possible to release a placeholder without clicking "Release" - see https://github.com/geheur/bank-tag-custom-layouts/issues/33 for more info.
		if (
				config.preventVanillaPlaceholderMenuBug() &&
						!client.isMenuOpen() &&
						WidgetInfo.TO_GROUP(event.getWidgetId()) == WidgetID.BANK_GROUP_ID &&
						event.getMenuOption().equals("Release")
		) {
			event.setConsumed(true);
			return;
		}

		if (!(event.getMenuAction() == MenuAction.RUNELITE_OVERLAY || event.getMenuAction() == MenuAction.RUNELITE)) return;

		String menuTarget = Text.removeTags(event.getMenuTarget()).replace("\u00a0"," ");

		// If this is on a real item, then the bank tags plugin will remove it from the tag, and this plugin only needs
		// to remove it from the layout. If this is on a fake item, this plugin must do both (unless the "Remove-layout"
		// option was clicked, then the tags are not touched).
		String menuOption = event.getMenuOption();
		boolean consume = true;
		if (menuOption.startsWith(REMOVE_FROM_LAYOUT_MENU_OPTION)) {
			removeFromLayout(event.getActionParam());
		} else if (ENABLE_LAYOUT.equals(menuOption)) {
			enableLayout(LayoutableThing.bankTag(menuTarget));
		} else if (DISABLE_LAYOUT.equals(menuOption)) {
			disableLayout(menuTarget);
		} else if (EXPORT_LAYOUT.equals(menuOption)) {
			exportLayout(menuTarget);
		} else if (IMPORT_LAYOUT.equals(menuOption)) {
			importLayout();
		} else if (PREVIEW_AUTO_LAYOUT.equals(menuOption)) {
			showLayoutPreview();
		} else if (DUPLICATE_ITEM.equals(menuOption)) {
			duplicateItem(event.getActionParam());
		} else if (REMOVE_DUPLICATE_ITEM.equals(menuOption)) {
			removeFromLayout(event.getActionParam());
		} else {
			consume = false;
		}
		if (consume) event.consume();
	}

	private void removeFromLayout(int index)
	{
		LayoutableThing layoutable = getCurrentLayoutableThing();
		Layout layout = getBankOrder(layoutable);
		layout.clearIndex(index);
		saveLayout(layoutable, layout);

		applyCustomBankTagItemPositions();
	}

	@VisibleForTesting
	void duplicateItem(int clickedItemIndex)
	{
		LayoutableThing layoutable = getCurrentLayoutableThing();
		Layout layout = getBankOrder(layoutable);

		layout.duplicateItem(clickedItemIndex, getIdForIndexInRealBank(clickedItemIndex));
		saveLayout(layoutable, layout);

		applyCustomBankTagItemPositions();
	}

	// TODO consider using tagManager.getItemsForTag(bankTagName) because unlike findTag it is api.
	private void cleanItemsNotInBankTag(Layout layout, LayoutableThing layoutable) {
		Predicate<Integer> containsId;
		if (layoutable.isBankTab()) {
			containsId = id -> copyPaste.findTag(id, layoutable.name);
		}
		else
		{
			InventorySetup inventorySetup = inventorySetupsAdapter.getInventorySetup(layoutable.name);
			containsId = id -> inventorySetupsAdapter.setupContainsItem(inventorySetup, id);
		}

		Iterator<Map.Entry<Integer, Integer>> iter = layout.allPairsIterator();
		while (iter.hasNext()) {
			int itemId = iter.next().getValue();

			if (!containsId.test(itemId))
			{
				log.debug("removing " + itemNameWithId(itemId) + " because it is no longer in the thing");
				iter.remove();
			}
		}
	}

	// TODO this logic needs looking at re: barrows items.
	private void assignVariantItemPositions(Layout layout, List<Widget> bankItems, Map<Integer, Widget> indexToWidget) {
		// Remove duplicate item id widgets.
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		bankItems = new ArrayList<>(bankItems.stream().filter(widget -> seen.add(widget.getItemId())).collect(Collectors.toList()));

		Multimap<Integer, Widget> variantItemsInBank = LinkedListMultimap.create(); // key is the variant base id; the list contains the item widgets that go in this variant base id;
		for (Widget bankItem : bankItems) {
			int nonPlaceholderId = getNonPlaceholderId(bankItem.getItemId());
			if (itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
				int variationBaseId = getVariationBaseId(nonPlaceholderId);
				variantItemsInBank.put(variationBaseId, bankItem);
			}
		}

		Multimap<Integer, Integer> variantItemsInLayout = LinkedListMultimap.create(); // key is the variant base id; the list contains the item ids;
		for (Map.Entry<Integer, Integer> pair : layout.allPairs()) {
			int nonPlaceholderId = getNonPlaceholderId(pair.getValue());
			if (itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
				int variationBaseId = getVariationBaseId(nonPlaceholderId);
				variantItemsInLayout.put(variationBaseId, pair.getValue());
			}
		}

		for (Integer variationBaseId : variantItemsInBank.keySet()) {
			List<Widget> notYetPositionedWidgets = new ArrayList<>(variantItemsInBank.get(variationBaseId));

			// first, figure out if there is a perfect match.
			assignitemstrashname(indexToWidget, variantItemsInLayout, variationBaseId, notYetPositionedWidgets, (itemIdsInLayoutForVariant, itemId) -> itemIdsInLayoutForVariant.contains(itemId) ? layout.getIndexForItem(itemId) : -1, "pass 1 (exact itemid match)");

			// check matches of placeholders or placeholders matching items.
			assignitemstrashname(indexToWidget, variantItemsInLayout, variationBaseId, notYetPositionedWidgets, (itemIdsInLayoutForVariant, itemId) -> {
				itemId = switchPlaceholderId(itemId);
				return itemIdsInLayoutForVariant.contains(itemId) ? layout.getIndexForItem(itemId) : -1;
			}, "pass 2 (placeholder match)");

			// match any variant item.
			assignitemstrashname(indexToWidget, variantItemsInLayout, variationBaseId, notYetPositionedWidgets, (itemIdsInLayoutForVariant, itemId) -> {
				for (Integer id : itemIdsInLayoutForVariant) {
					int index = layout.getIndexForItem(id);
					if (!indexToWidget.containsKey(index)) {
						return index;
					}
				}
				return -1;
			}, "pass 3 (variant item match)");

			if (!notYetPositionedWidgets.isEmpty()) {
				for (Widget notYetPositionedWidget : notYetPositionedWidgets) {
					int itemId = notYetPositionedWidget.getItemId();
					int layoutIndex = layout.getIndexForItem(itemId);
					if (layoutIndex != -1) continue; // Prevents an issue where items with the same id that take up multiple bank slots, e.g. items that have their charges stored on the item, can be added into two slots during this stage.
					int index = layout.getFirstEmptyIndex();
					layout.putItem(itemId, index);
					log.debug("item " + itemNameWithId(itemId) + " assigned on pass 4 (assign to empty spot) to index " + index);
					indexToWidget.put(index, notYetPositionedWidget);
				}
			}
		}
	}

	private int getVariationBaseId(int nonPlaceholderId)
	{
		int runeliteBaseId = ItemVariationMapping.map(nonPlaceholderId);
		if (runeliteBaseId == 713) {
			ItemComposition itemComposition = itemManager.getItemComposition(nonPlaceholderId);
			int iconId = itemComposition.getInventoryModel();
			if (iconId == 37162) { // beginner
				return nonPlaceholderId; // All share the same id.
			}
			else if (iconId == 37202) { // easy
				return 2677; // Lowest id of this clue type.
			}
			else if (iconId == 37152) { // medium
				return 2801; // Lowest id of this clue type.
			}
			else if (iconId == 37181) { // hard
				return 2722; // Lowest id of this clue type.
			}
			else if (iconId == 37167) { // elite
				return 12073; // Lowest id of this clue type.
			}
			else if (iconId == 37183) { // master
				return nonPlaceholderId; // All share the same id.
			}
			// this is either a (likely unobtainable) pink skirt or a sote quest item. I don't care how either of these items are handled.
		}
		return runeliteBaseId;
	}

	@FunctionalInterface
	private interface functionalinterfacetrashname {
		int getIndex(Collection<Integer> itemIds, int itemId);
	}

	private void assignitemstrashname(Map<Integer, Widget> indexToWidget, Multimap<Integer, Integer> variantItemsInLayout, Integer variationBaseId, List<Widget> notYetPositionedWidgets, functionalinterfacetrashname getIndex, String debugDescription)
	{
		Iterator<Widget> iter = notYetPositionedWidgets.iterator();
		while (iter.hasNext()) {
			Widget widget = iter.next();
			int itemId = widget.getItemId();

			Collection<Integer> itemIds = variantItemsInLayout.get(variationBaseId);
			if (itemIds == null) continue; // this could happen because I removed all the widgets at this key.

			int index = getIndex.getIndex(itemIds, itemId);

			if (index != -1 && !indexToWidget.containsKey(index)) {
				log.debug("item " + itemNameWithId(itemId) + " assigned on " + debugDescription + " to index " + index);
				indexToWidget.put(index, widget);
				iter.remove();
			}
		}
	}

	/**
	 */
	private boolean itemHasVariants(int nonPlaceholderItemId) {
		return ItemVariationMapping.getVariations(ItemVariationMapping.map(nonPlaceholderItemId)).size() > 1;
	}

	/**
	 * Whether this item should be treated as having variants for the purpose of custom bank layouts.
	 * If true, this means that the item should occupy the next available position in the custom layout which matches either its own id or any of its variants.
	 * This includes placeholders for the item.
	 * This does mean that the order that items appear in in the normal bank has an impact on the custom layout. Not something you'd expect from this feature, lol.
	 */
	boolean itemShouldBeTreatedAsHavingVariants(int nonPlaceholderItemId) {
		return itemHasVariants(nonPlaceholderItemId);
	}

	Layout getBankOrder(LayoutableThing layoutable) {
		if (isShowingPreview()) {
			return previewLayout;
		}

		return getBankOrderNonPreview(layoutable);
	}

	/**
	 * unlike getBankOrder, this will not return a preview layout when one is currently being show.
	 */
	private Layout getBankOrderNonPreview(LayoutableThing layoutable) {
		String configuration = configManager.getConfiguration(CONFIG_GROUP, layoutable.configKey());
		if (LAYOUT_EXPLICITLY_DISABLED.equals(configuration)) return null;
		if (configuration == null) {
			if (layoutable.isBankTab() && !config.layoutEnabledByDefault() || layoutable.isInventorySetup() && !config.useWithInventorySetups()) {
				return null;
			} else if (layoutable.isInventorySetup()) {
				// Inventory setups by default have an equipment and inventory order, so lay it out automatically if this
				// is the first time viewing the setup with bank tag layouts.
				InventorySetup inventorySetup = inventorySetupsAdapter.getInventorySetup(layoutable.name);
				return layoutGenerator.basicInventorySetupsLayout(inventorySetup, Layout.emptyLayout(), getAutoLayoutDuplicateLimit(), config.autoLayoutStyle());
			}

			configuration = "";
		}
		return Layout.fromString(configuration, true);
	}

	private void exportLayout(String tagName) {
		String exportString = BANK_TAG_STRING_PREFIX + tagName;
		String layout = getBankOrder(LayoutableThing.bankTag(tagName)).toString();
		if (!layout.isEmpty()) {
			exportString += ",";
		}
		exportString += layout;

		List<String> tabNames = Text.fromCSV(MoreObjects.firstNonNull(configManager.getConfiguration(BankTagsPlugin.CONFIG_GROUP, BankTagsPlugin.TAG_TABS_CONFIG), ""));
		if (!tabNames.contains(tagName)) {
			chatErrorMessage("Couldn't export layout-ed tag tab - tag tab doesn't see to exist?");
		}

		List<String> data = new ArrayList<>();
		data.add(tagName);
		String tagTabIconItemId = configManager.getConfiguration(BankTagsPlugin.CONFIG_GROUP, BankTagsPlugin.ICON_SEARCH + tagName);
		if (tagTabIconItemId == null) {
			tagTabIconItemId = "" + ItemID.SPADE;
		}
		data.add(tagTabIconItemId);

		for (Integer item : tagManager.getItemsForTag(tagName)) {
			data.add(String.valueOf(item));
		}

		exportString += ",banktag:" + Text.toCSV(data);

		putInClipboard(exportString);
		chatMessage("Copied layout-ed tag \"" + tagName + "\" to clipboard");
	}

	private void putInClipboard(String exportString) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(exportString), null);
	}

	private MessageNode chatErrorMessage(String message) {
		return chatMessage(ColorUtil.wrapWithColorTag(message, Color.RED));
	}

	private MessageNode chatMessage(String message) {
		return client.addChatMessage(ChatMessageType.GAMEMESSAGE, "bla", message, "bla");
	}

	static int getXForIndex(int index) {
		return (index % 8) * 48 + 51;
	}

	static int getYForIndex(int index) {
		return (index / 8) * BANK_ITEM_WIDTH;
	}

	private void setItemPositions(Map<Integer, Widget> indexToWidget) {
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		// Hide all widgets not in indexToWidget.
		outer_loop:
		for (Widget child : container.getDynamicChildren()) {
			if (child.isHidden()) continue;
			for (Map.Entry<Integer, Widget> integerWidgetEntry : indexToWidget.entrySet()) {
				if (integerWidgetEntry.getValue().equals(child)) {
					continue outer_loop;
				}
			}

			child.setHidden(true);
			child.revalidate();
		}

		for (Map.Entry<Integer, Widget> entry : indexToWidget.entrySet()) {
			Widget widget = entry.getValue();
			int index = entry.getKey();

			widget.setOriginalX(getXForIndex(index));
			widget.setOriginalY(getYForIndex(index));
			widget.revalidate();
		}
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	static final class LayoutableThing {
		public final String name;
		/** false means it's an inventory setup. */
		public final boolean isBankTab;

		public static LayoutableThing bankTag(String tagName) {
			return new LayoutableThing(tagName, true);
		}

		public static LayoutableThing inventorySetup(String inventorySetupName) {
			return new LayoutableThing(inventorySetupName, false);
		}

		@Override
		public String toString() {
			return name + " " + (isBankTab ? "(bank tab)" : "(inventory setup)");
		}

		public String configKey() {
			if (isBankTab) {
				return LAYOUT_CONFIG_KEY_PREFIX + name;
			} else {
				return INVENTORY_SETUPS_LAYOUT_CONFIG_KEY_PREFIX + escapeColonCharactersInInventorySetupName(name);
			}
		}

		static String escapeColonCharactersInInventorySetupName(String s)
		{
			return s.replaceAll("&", "&amp;").replaceAll(":", "&#58;");
		}

		public boolean isBankTab() {
			return isBankTab;
		}

		public boolean isInventorySetup() {
			return !isBankTab;
		}
	}

	private void resizeBankContainerScrollbar(int height, int lastHeight) {
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

		container.setScrollHeight(height); // This change requires the script below to run to take effect.

		int itemContainerScroll = (height > lastHeight) ? height : container.getScrollY();

		clientThread.invokeLater(() ->
				client.runScript(ScriptID.UPDATE_SCROLLBAR,
						WidgetInfo.BANK_SCROLLBAR.getId(),
						WidgetInfo.BANK_ITEM_CONTAINER.getId(),
						itemContainerScroll)
		);
	}

	public String itemName(Integer itemId) {
		return (itemId == null) ? "null" : itemManager.getItemComposition(itemId).getName();
	}

	public String itemNameWithId(Integer itemId) {
		return ((itemId == null) ? "null" : itemManager.getItemComposition(itemId).getName()) + " (" + itemId + ")";
	}

	private int getPlaceholderId(int id) {
		ItemComposition itemComposition = itemManager.getItemComposition(id);
		return (itemComposition.getPlaceholderTemplateId() == 14401) ? id : itemComposition.getPlaceholderId();
	}

	int getNonPlaceholderId(int id) {
		ItemComposition itemComposition = itemManager.getItemComposition(id);
		return (itemComposition.getPlaceholderTemplateId() == 14401) ? itemComposition.getPlaceholderId() : id;
	}

	int switchPlaceholderId(int id) {
		ItemComposition itemComposition = itemManager.getItemComposition(id);
		return itemComposition.getPlaceholderId();
	}

	public boolean isPlaceholder(int id) {
		ItemComposition itemComposition = itemManager.getItemComposition(id);
		return itemComposition.getPlaceholderTemplateId() == 14401;
	}

	private void customBankTagOrderInsert(LayoutableThing layoutable, Widget draggedItem) {
		int draggedOnItemIndex = getIndexForMousePositionNoLowerLimit();
		if (draggedOnItemIndex == -1) return;

		int draggedItemIndex = -1;
		for (Map.Entry<Integer, Widget> entry : indexToWidget.entrySet()) {
			if (entry.getValue().equals(draggedItem)) {
				draggedItemIndex = entry.getKey();
			}
		}

		customBankTagOrderInsert(layoutable, draggedItemIndex, draggedOnItemIndex);
	}

	private void customBankTagOrderInsert(LayoutableThing layoutable, int draggedItemIndex, int draggedOnItemIndex) {
		Layout layout = getBankOrder(layoutable);
		if (layout == null) return;

		// Currently I'm just spilling the variant items out in bank order, so I don't care exactly what item id was there - although if I ever decide to change this, this section will become much more complicated, since if I drag a (2) charge onto a regular item, but there was supposed to be a (3) charge there then I have to move the (2) but also deal with where the (2)'s saved position is... At least that's how it'll go if I decide to handle jewellery that way.

		Integer currentDraggedItemId = getIdForIndexInRealBank(draggedItemIndex);

		layout.moveItem(draggedItemIndex, draggedOnItemIndex, currentDraggedItemId);

		saveLayout(layoutable, layout);

		applyCustomBankTagItemPositions();
	}

	private Integer getIdForIndexInRealBank(int index) {
		if (index == -1) return -1;
		Widget widget = indexToWidget.get(index);
		if (widget == null) return -1;
		return widget.getItemId();
	}

	// Disable reordering your real bank while any tag tab is active, as if the Bank Tags Plugin's "Prevent tag tab item dragging" was enabled.
	@Subscribe(priority = -1f) // run after bank tags, otherwise you can't drag items into other tabs while a tab is open.
	public void onDraggingWidgetChanged(DraggingWidgetChanged event) {
		Widget widget = client.getWidget(WidgetInfo.BANK_CONTAINER);
		if (widget == null || widget.isHidden()) {
			return;
		}

		Widget draggedWidget = client.getDraggedWidget();

		// Returning early or nulling the drag release listener has no effect. Hence, we need to
		// null the draggedOnWidget instead.
		if (draggedWidget.getId() == WidgetInfo.BANK_ITEM_CONTAINER.getId() && hasLayoutEnabled(getCurrentLayoutableThing())) {
			client.setDraggedOnWidget(null);
		}
	}

	@Subscribe
	public void onFocusChanged(FocusChanged focusChanged)
	{
		antiDrag.focusChanged(focusChanged);
	}

	@Subscribe
	public void onMenuShouldLeftClick(MenuShouldLeftClick event)
	{
		Widget widget = client.getWidget(WidgetInfo.BANK_CONTAINER);
		if (widget == null || widget.isHidden()) {
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();
		for (MenuEntry entry : menuEntries)
		{
			// checking the type is kinda hacky because really both preview auto layout entries should have the runelite id... but it works.
			if (entry.getOption().equals(PREVIEW_AUTO_LAYOUT) && entry.getType() != MenuAction.RUNELITE)
			{
				event.setForceRightClick(true);
				return;
			}
		}
	}
}
