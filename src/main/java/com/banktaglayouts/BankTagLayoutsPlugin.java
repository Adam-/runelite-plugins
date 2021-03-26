package com.banktaglayouts;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ObjectArrays;
import com.google.common.util.concurrent.Runnables;
import com.google.inject.Provides;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.MessageNode;
import net.runelite.api.Point;
import net.runelite.api.ScriptEvent;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.DraggingWidgetChanged;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.MenuShouldLeftClick;
import net.runelite.api.events.ScriptPostFired;
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
import net.runelite.client.plugins.banktags.tabs.TagTab;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.banktags.BankTagsPlugin.ICON_SEARCH;
import static net.runelite.client.plugins.banktags.BankTagsPlugin.TAG_TABS_CONFIG;

@Slf4j
/* TODO
    inventory setups.
    autolayout.

	Bank tags bug:
		Why are bank tag tabs not using exact matches for tag names but instead checking with something like startsWith?
		Weird ids added for scb, possibly all variant items.
			Why did 1,2,3 dose scb and stamina potions suddenly appear in my cox tab?

	Issues I don't care about:
		Tag import does not delete existing tags in the tab.
		duplicate tab.
			Can easily be done with an export then an import, but this would be nice for convenience I guess.

	Minor features:
		Tag editing:
			Option to show fake items for things that are in the tag but not in the layout. This would be nice for
				"cleaning out" a bank tag, for example for publishing to other people.
				Also allow people to remove the item from the actual tag.

	Handling of barrows items still questionable. What happens if you put in a degraded one where previously there was something of a different degradation in the tab?

	// TODO items with same id that do not stack in bank, e.g. bank placeholders. I thin currently only one of these
	    is shown, which is fine for all use-cases I'm aware of.

	low:
	// TODO treat variant items which have a placeholder for each variant (e.g. potions) as non-variant items. Probably need to build a list by iterating all items in the game.
		// What is the point of this???? What would it accomplish? Idk why i wrote this weeks ago.
 */
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
	public static final String BANK_TAG_STRING_PREFIX = "banktaglayoutsplugin:";
	public static final String LAYOUT_EXPLICITLY_DISABLED = "DISABLED";

	public static final String ENABLE_LAYOUT = "Enable layout";
	public static final String DISABLE_LAYOUT = "Delete layout";
	public static final String IMPORT_LAYOUT = "Import tag tab with layout";
	public static final String EXPORT_LAYOUT = "Export tag tab with layout";
	public static final String REMOVE_FROM_LAYOUT_MENU_OPTION = "Remove-layout";
	//	public static final String REMOVE_FROM_TAG_MENU_OPTION = "Remove-tag";
	public static final String PREVIEW_AUTO_LAYOUT = "Preview auto layout";

	private static final int BANK_ITEM_WIDTH = 36;
	private static final int BANK_ITEM_HEIGHT = 32;

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
	ItemManager itemManager;

	@Inject
	ConfigManager configManager;

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

	// The current indexes for where each widget should appear in the custom bank layout. Should be ignored if there is not tab active.
	private final Map<Integer, Widget> indexToWidget = new HashMap<>();

	private Widget showLayoutPreviewButton = null;
	private Widget applyLayoutPreviewButton = null;
    private Widget cancelLayoutPreviewButton = null;

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

			showLayoutPreviewButton.setOnOpListener((JavaScriptCallback) (e) -> {
			    showLayoutPreview();
			});
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

			applyLayoutPreviewButton.setOnOpListener((JavaScriptCallback) (e) -> {
				applyLayoutPreview();
			});
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

			cancelLayoutPreviewButton.setOnOpListener((JavaScriptCallback) (e) -> {
				cancelLayoutPreview();
			});
			cancelLayoutPreviewButton.setHasListener(true);
			cancelLayoutPreviewButton.revalidate();
			cancelLayoutPreviewButton.setAction(0, "Cancel preview");
		}

		hideLayoutPreviewButtons(!isShowingPreview());
		showLayoutPreviewButton.setHidden(!(config.showAutoLayoutButton() && tabInterface.isActive() && !isShowingPreview()));
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
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(fakeItemOverlay);
		spriteManager.removeSpriteOverrides(Sprites.values());
		mouseManager.unregisterMouseListener(this);
		keyManager.unregisterKeyListener(antiDrag);
		indexToWidget.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
	    if (CONFIG_GROUP.equals(event.getGroup())) {
			if ("layoutEnabledByDefault".equals(event.getKey())) {
				clientThread.invokeLater(this::applyCustomBankTagItemPositions);
			} else if ("showAutoLayoutButton".equals(event.getKey())) {
				clientThread.invokeLater(this::updateButton);
			}
		} else if (BankTagsPlugin.CONFIG_GROUP.equals(event.getGroup()) && BankTagsPlugin.TAG_TABS_CONFIG.equals(event.getKey())) {
			handlePotentialTagChange(event);
		}
	}

	private void handlePotentialTagChange(ConfigChanged event) {
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

		String oldName = oldTags.iterator().next();
		String newName = newTags.iterator().next();

		Map<Integer, Integer> oldLayout = getBankOrder(oldName);
		if (oldLayout != null) {
		    saveLayout(newName, oldLayout);
			configManager.unsetConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + oldName);
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
		for (Integer itemId : previewLayout.keySet()) {
			if (!copyPaste.findTag(itemId, previewLayoutTagName)) {
			    log.debug("adding item " + itemName(itemId) + " (" + itemId + ") to tag");
				tagManager.addTag(itemId, previewLayoutTagName, false);
			}
		}

		saveLayoutNonPreview(previewLayoutTagName, previewLayout);

		cancelLayoutPreview();
		bankSearch.layoutBank();
	}

	private void hideLayoutPreviewButtons(boolean hide) {
		if (applyLayoutPreviewButton != null) applyLayoutPreviewButton.setHidden(hide);
		if (cancelLayoutPreviewButton != null) cancelLayoutPreviewButton.setHidden(hide);
		if (config.showAutoLayoutButton() && tabInterface.isActive()) showLayoutPreviewButton.setHidden(!hide);
	}

	private void cancelLayoutPreview() {
		previewLayout = null;
		previewLayoutTagName = null;

		hideLayoutPreviewButtons(true);

		applyCustomBankTagItemPositions();
	}

	/** null indicates that there should not be a preview shown. */
	private Map<Integer, Integer> previewLayout = null;
	private String previewLayoutTagName = null;

	private void showLayoutPreview() {

	    if (isShowingPreview()) return;
		TagTab activeTab = tabInterface.getActiveTab();
		if (activeTab == null) {
			chatMessage("Select a tab tag before using this feature.");
			return;
		} else {
			// TODO allow creation of new tab.
		}

		List<Integer> equippedGear = getEquippedGear();
		List<Integer> inventory = getInventory();
		if (equippedGear.stream().filter(id -> id > 0).count() == 0 && inventory.stream().filter(id -> id > 0).count() == 0) {
			chatMessage("This feature uses your equipped items and inventory to automatically create a bank tag layout, but you don't have any items equipped or in your inventory.");
			return;
		}

		hideLayoutPreviewButtons(false);

		String bankTagName = tabInterface.getActiveTab().getTag();
		Map<Integer, Integer> currentLayout = getBankOrderNonPreview(bankTagName);
		if (currentLayout == null) currentLayout = new HashMap<>();

		previewLayout = layoutGenerator.basicLayout(equippedGear, inventory, currentLayout);
		previewLayoutTagName = activeTab.getTag();

		applyCustomBankTagItemPositions();
	}

	private List<Integer> getEquippedGear() {
		ItemContainer container = client.getItemContainer(InventoryID.EQUIPMENT);
		if (container == null) return Collections.emptyList();
		return Arrays.stream(container.getItems()).map(w -> w.getId()).collect(Collectors.toList());
	}

	private List<Integer> getInventory() {
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		if (container == null) return Collections.emptyList();
		return Arrays.stream(container.getItems()).map(w -> w.getId()).collect(Collectors.toList());
	}

	private boolean isShowingPreview() {
		return previewLayout != null;
	}

	int lastScrollY = -1;
	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (container != null) lastScrollY = container.getScrollY();
	}

	@Subscribe(priority = -1f) // I want to run after the Bank Tags plugin does, since it will interfere with the layout-ing if hiding tab separators is enabled.
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == ScriptID.BANKMAIN_BUILD) {
			if (
					!tabInterface.isActive()
					|| (isShowingPreview() && !Objects.equals(previewLayoutTagName, tabInterface.getActiveTab().getTag()))) {
				cancelLayoutPreview();
			} else {
				applyCustomBankTagItemPositions();
			}

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

		Map<Integer, Integer> layout;
		try {
			layout = bankTagOrderStringToMap(layoutString);
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

	private void importLayout(String name, Map<Integer, Integer> layout, String tagString) {
		boolean successful = importBankTag(name, tagString);
		if (!successful) return;

		saveLayout(name, layout);

		chatMessage("Imported layout-ed tag tab \"" + name + "\"");

		applyCustomBankTagItemPositions();
	}

	private void saveLayout(String name, Map<Integer, Integer> layout) {
		if (isShowingPreview()) {
			previewLayout = layout;
			return;
		}

		saveLayoutNonPreview(name, layout);
	}

	private void saveLayoutNonPreview(String name, Map<Integer, Integer> layout) {
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + name, bankTagOrderMapToString(layout));
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
//		copyPaste.scrollTab(0); // TODO what was this supposed to do?

//		if (tabInterface.getActiveTab() != null && name.equals(tabInterface.getActiveTab().getTag())) {
//			copyPaste.openTag(tabInterface.getActiveTab().getTag());
//		} else if (tabInterface.isTagTabActive()) {
//			copyPaste.openTag("tagtabs");
//		}
		return true;
	}

	public boolean hasLayoutEnabled(String bankTagName) {
		if (isShowingPreview()) return true;

		String configuration = configManager.getConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName);
		if (LAYOUT_EXPLICITLY_DISABLED.equals(configuration)) return false;
		return config.layoutEnabledByDefault() || configuration != null;
	}

	private void enableLayout(String bankTagName) {
		saveLayout(bankTagName, new HashMap<>());
		if (tabInterface.getActiveTab() != null && bankTagName.equals(tabInterface.getActiveTab().getTag())) {
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

	private boolean tutorialMessageShown = false;
	private void applyCustomBankTagItemPositions() {
		log.debug("applying layout");
        fakeItems.clear();

		if (!tabInterface.isActive()) {
			return;
		}

		String bankTagName = tabInterface.getActiveTab().getTag();

		List<Widget> bankItems = Arrays.stream(client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren())
				.filter(bankItem -> !bankItem.isHidden() && bankItem.getItemId() >= 0)
				.collect(Collectors.toList());

		if (!hasLayoutEnabled(bankTagName)) {
			for (Widget bankItem : bankItems) {
				bankItem.setOnDragCompleteListener((JavaScriptCallback) (ev) -> {
					boolean tutorialShown = tutorialMessage();

					if (!tutorialShown) bankReorderWarning(ev);
				});
			}
			return;
		}

		log.debug("applyCustomBankTagItemPositions " + bankTagName);

		indexToWidget.clear();
		Map<Integer, Integer> itemPositionIndexes = getBankOrder(bankTagName);
		if (itemPositionIndexes == null) {
			return; // layout not enabled.
		}

		if (!isShowingPreview()) { // I don't want to clean layout items when displaying a preview. This could result in some layout placeholders being auto-removed due to not being in the tab.
			cleanItemsNotInBankTag(itemPositionIndexes, bankTagName);
			cleanDuplicateIndexes(itemPositionIndexes);
		}

		assignVariantItemPositions(itemPositionIndexes);
		assignNonVariantItemPositions(itemPositionIndexes, bankItems);

		for (Widget bankItem : bankItems) {
			bankItem.setOnDragCompleteListener((JavaScriptCallback) (ev) -> {
				customBankTagOrderInsert(bankTagName, ev.getSource());
			});
		}

		for (Map.Entry<Integer, Integer> entry : itemPositionIndexes.entrySet()) {
			Integer index = entry.getValue();
			if (indexToWidget.containsKey(index)) {
				continue;
			}

			fakeItems.add(new FakeItem(index, getNonPlaceholderId(entry.getKey())));
		}

		// What the fuck? It appears that despite my priority setting on the @Subscribe, Bank Tags can still sometimes run after me and potentially run its remove tag separators code, messing up my layout.
        // invokeLater solves this issue though.
		clientThread.invokeLater(() -> {
			setItemPositions(indexToWidget);
		});
		setContainerHeight(bankTagName, itemPositionIndexes);
		saveLayout(bankTagName, itemPositionIndexes);
		log.debug("saved tag " + bankTagName);
	}

	private void bankReorderWarning(ScriptEvent ev) {
		if (
				config.warnForAccidentalBankReorder()
				&& ev.getSource().getId() == WidgetInfo.BANK_ITEM_CONTAINER.getId() && tabInterface.isActive()
				&& client.getDraggedOnWidget() != null
				&& client.getDraggedOnWidget().getId() == WidgetInfo.BANK_ITEM_CONTAINER.getId() && tabInterface.isActive()
				&& !hasLayoutEnabled(tabInterface.getActiveTab().getTag())
				&& !Boolean.parseBoolean(configManager.getConfiguration(BankTagsPlugin.CONFIG_GROUP, "preventTagTabDrags"))
		) {
			chatErrorMessage("You just reordered your actual bank!");
			chatMessage("If you wanted to use a bank tag layout, make sure you enable it for this tab first.");
			chatMessage("You should consider enabling \"Prevent tag tab item dragging\" in the Bank Tags plugin.");
			chatMessage("You can disable this warning in the Bank Tag Layouts config.");
		}
	}

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

	private void assignNonVariantItemPositions(Map<Integer, Integer> itemPositionIndexes, List<Widget> bankItems) {
		for (Widget bankItem : bankItems) {
			int itemId = bankItem.getItemId();

			int nonPlaceholderId = getNonPlaceholderId(itemId);

			if (!itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
//				log.debug("\tassigning position for " + itemName(itemId) + itemId + ": ");
				int indexForItem = getIndexForItem(itemId, itemPositionIndexes);
				if (indexForItem != -1) {
//					log.debug("\t\texisting position: " + indexForItem);
					indexToWidget.put(indexForItem, bankItem);
				} else {
					int newIndex = assignPosition(itemPositionIndexes);
					itemPositionIndexes.put(itemId, newIndex);
//					log.debug("\t\tnew position: " + newIndex);
					indexToWidget.put(newIndex, bankItem);
				}
			}
		}
	}

	public final Set<FakeItem> fakeItems = new HashSet<>();

	@Override
	public MouseEvent mouseClicked(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	public volatile int draggedItemIndex = -1; // Used for fake items only, not real items.
	public int dragStartX = 0;
	public int dragStartY = 0;

	@Override
	public MouseEvent mousePressed(MouseEvent mouseEvent) {
	    if (mouseEvent.getButton() != MouseEvent.BUTTON1 || isHidden() || !config.showLayoutPlaceholders()) return mouseEvent;
		int index = getIndexForMousePosition(true);
		FakeItem fakeItem = fakeItems.stream().filter(fake -> fake.index == index).findAny().orElse(null);
		if (fakeItem != null) {
			draggedItemIndex = fakeItem.index;
			dragStartX = mouseEvent.getX();
			dragStartY = mouseEvent.getY();
			antiDrag.startDrag();
			mouseEvent.consume();
		}
		return mouseEvent;
	}

	private boolean isHidden() {
		return !tabInterface.isActive();
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() != MouseEvent.BUTTON1 || isHidden()) return mouseEvent;
		if (draggedItemIndex == -1) return mouseEvent;

		if (config.showLayoutPlaceholders()) {
			int draggedOnIndex = getIndexForMousePositionNoLowerLimit();
			clientThread.invokeLater(() -> {
				if (draggedOnIndex != -1 && antiDrag.mayDrag()) {
					customBankTagOrderInsert(tabInterface.getActiveTab().getTag(), draggedItemIndex, draggedOnIndex);
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
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
	    if (WidgetInfo.TO_GROUP(menuEntryAdded.getActionParam1()) == WidgetInfo.BANK_CONTENT_CONTAINER.getGroupId()) {
			String bankTagName = Text.removeTags(menuEntryAdded.getTarget()).replace("\u00a0"," ");

			if ("Rename tag tab".equals(menuEntryAdded.getOption())) {
				if (hasLayoutEnabled(bankTagName)) {
					addEntry(bankTagName, EXPORT_LAYOUT);
				}

				addEntry(bankTagName, hasLayoutEnabled(bankTagName) ? DISABLE_LAYOUT : ENABLE_LAYOUT);
			} else if ("New tag tab".equals(menuEntryAdded.getOption())) {
				if (!config.showAutoLayoutButton()) {
					addEntry(bankTagName, PREVIEW_AUTO_LAYOUT);
				}
				addEntry(bankTagName, IMPORT_LAYOUT);
			}
		}

		addFakeItemMenuEntries(menuEntryAdded);
	}

	private void addEntry(String menuTarget, String menuOption) {
		MenuEntry newEntry;
		newEntry = new MenuEntry();
		newEntry.setOption(menuOption);
		newEntry.setTarget(ColorUtil.wrapWithColorTag(menuTarget, itemTooltipColor));
		newEntry.setType(MenuAction.RUNELITE.getId());
		insertMenuEntry(newEntry, client.getMenuEntries(), true);
	}

	private void addFakeItemMenuEntries(MenuEntryAdded menuEntryAdded) {
		if (!menuEntryAdded.getOption().equalsIgnoreCase("cancel")) return;

		if (!tabInterface.isActive() || !config.showLayoutPlaceholders()) {
			return;
		}
		Map<Integer, Integer> itemIdToIndexes = getBankOrder(tabInterface.getActiveTab().getTag());
		if (itemIdToIndexes == null) return;

		int index = getIndexForMousePosition(true);
		if (index == -1) return;
		Map.Entry<Integer, Integer> entry = itemIdToIndexes.entrySet().stream()
				.filter(e -> e.getValue() == index)
				.findAny().orElse(null);

		if (entry != null && !indexToWidget.containsKey(entry.getValue())) {
			MenuEntry newEntry;
//			newEntry = new MenuEntry();
//			newEntry.setOption(REMOVE_FROM_TAG_MENU_OPTION + " (" + tabInterface.getActiveTab().getTag() + ")");
//			newEntry.setType(MenuAction.CC_OP_LOW_PRIORITY.getId());
//			newEntry.setTarget(ColorUtil.wrapWithColorTag(itemName(entry.getKey()), itemTooltipColor));
//			newEntry.setType(MenuAction.RUNELITE.getId());
//			newEntry.setParam0(entry.getKey());
//			insertMenuEntry(newEntry, client.getMenuEntries(), true);

			newEntry = new MenuEntry();
			newEntry.setOption(REMOVE_FROM_LAYOUT_MENU_OPTION);
			newEntry.setType(MenuAction.RUNELITE_OVERLAY.getId());
			newEntry.setTarget(ColorUtil.wrapWithColorTag(itemName(entry.getKey()), itemTooltipColor));
			newEntry.setParam0(entry.getKey());
			insertMenuEntry(newEntry, client.getMenuEntries(), false);
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
		int row = (mouseY - canvasLocation.getY() + scrollY + 2) / 36;
		int col = (int) Math.floor((mouseX - canvasLocation.getX() - 51 + 6) / 48f);
		int index = row * 8 + col;
		if (row < 0 || col < 0 || col > 7 || index < 0) return -1;
		if (dontEnlargeClickbox) {
			int xDistanceIntoItem = (mouseX - canvasLocation.getX() - 51 + 6) % 48;
			int yDistanceIntoItem = (mouseY - canvasLocation.getY() + scrollY + 2) % 36;
			if (xDistanceIntoItem < 6 || xDistanceIntoItem >= 42 || yDistanceIntoItem < 2 || yDistanceIntoItem >= 34) {
				return -1;
			}
		}
		return index;
	}

	private void insertMenuEntry(MenuEntry newEntry, MenuEntry[] entries, boolean after)
	{
		MenuEntry[] newMenu = ObjectArrays.concat(entries, newEntry);

		if (after)
		{
			int menuEntryCount = newMenu.length;
			ArrayUtils.swap(newMenu, menuEntryCount - 1, menuEntryCount - 2);
		}

		client.setMenuEntries(newMenu);
	}

	// TODO do I actually want to remove variant items from the tag? What if I'm just removing one of the layout items, and do not actually want to remove it from the tag? That seems very reasonable.
    // TODO remove code related to removing stuff from bank tags.
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!(event.getMenuAction() == MenuAction.RUNELITE_OVERLAY || event.getMenuAction() == MenuAction.RUNELITE)) return;

		String menuTarget = Text.removeTags(event.getMenuTarget()).replace("\u00a0"," ");

		// If this is on a real item, then the bank tags plugin will remove it from the tag, and this plugin only needs
		// to remove it from the layout. If this is on a fake item, this plugin must do both (unless the "Remove-layout"
		// option was clicked, then the tags are not touched).
		String menuOption = event.getMenuOption();
		if (
				menuOption.startsWith(REMOVE_FROM_LAYOUT_MENU_OPTION)
//						|| event.getMenuOption().startsWith(REMOVE_FROM_TAG_MENU_OPTION)
		) {
			boolean isOnFakeItem = event.getWidgetId() != WidgetInfo.BANK_ITEM_CONTAINER.getId();
			boolean layoutOnly = menuOption.startsWith(REMOVE_FROM_LAYOUT_MENU_OPTION);
			event.consume();

			int itemId;
			if (isOnFakeItem) {
				itemId = event.getActionParam();
            } else {
			    // TODO is all this actually necessary? I got it from the Bank Tags plugin.
				ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
				Item item = bankContainer.getItem(event.getActionParam());
				ItemComposition itemComposition = itemManager.getItemComposition(item.getId());
				itemId = itemComposition.getId();
			}

			String bankTagName = tabInterface.getActiveTab().getTag();

			int menuEntryItemVariationBaseId = itemId;
			if (!layoutOnly) {
				int menuEntryItemNonPlaceholderId = getNonPlaceholderId(itemId);
				menuEntryItemVariationBaseId = itemShouldBeTreatedAsHavingVariants(menuEntryItemNonPlaceholderId) ? ItemVariationMapping.map(menuEntryItemNonPlaceholderId) : menuEntryItemNonPlaceholderId;
			}

			Map<Integer, Integer> bankOrder = getBankOrder(bankTagName);
			Iterator<Map.Entry<Integer, Integer>> iter = bankOrder.entrySet().iterator();
			while (iter.hasNext()) {
				Integer entryItemId = iter.next().getKey();
				int variationBaseId = entryItemId;
				if (!layoutOnly) {
					int nonPlaceholderId = getNonPlaceholderId(entryItemId);
					variationBaseId = itemShouldBeTreatedAsHavingVariants(nonPlaceholderId) ? ItemVariationMapping.map(nonPlaceholderId) : nonPlaceholderId;
				}
				if (menuEntryItemVariationBaseId == variationBaseId) {
					iter.remove();
				}
			}
			saveLayout(bankTagName, bankOrder);

			if (isOnFakeItem && !layoutOnly) {
				ItemComposition itemComposition = itemManager.getItemComposition(itemId);
				tagManager.removeTag(itemComposition.getId(), bankTagName);

				// This will trigger applyCustomBankTagItemPositions via the bankmain_build script, in addition to removing the item from the tag.
				bankSearch.layoutBank();
			} else if (layoutOnly) {
				applyCustomBankTagItemPositions();
			}
		} else if (ENABLE_LAYOUT.equals(menuOption)) {
			enableLayout(menuTarget);
			event.consume();
		} else if (DISABLE_LAYOUT.equals(menuOption)) {
			disableLayout(menuTarget);
			event.consume();
		} else if (EXPORT_LAYOUT.equals(menuOption)) {
			exportLayout(menuTarget);
			event.consume();
		} else if (IMPORT_LAYOUT.equals(menuOption)) {
			importLayout();
			event.consume();
		} else if (PREVIEW_AUTO_LAYOUT.equals(menuOption)) {
			showLayoutPreview();
			event.consume();
		}
	}

	// TODO consider using tagManager.getItemsForTag(bankTagName) because unlike findTag it is api.
	private void cleanItemsNotInBankTag(Map<Integer, Integer> itemPositionIndexes, String bankTagName) {
		Iterator<Map.Entry<Integer, Integer>> iter = itemPositionIndexes.entrySet().iterator();
		while (iter.hasNext()) {
			int itemId = iter.next().getKey();

			if (!copyPaste.findTag(itemId, bankTagName)) {
				log.debug("removing " + itemName(itemId) + " (" + itemId + ") because it is no longer in the bank tag");
				iter.remove();
			}
		}
	}

	// TODO this logic needs looking at re: barrows items.
	private void assignVariantItemPositions(Map<Integer, Integer> itemPositionIndexes) {
		Map<Integer, List<Widget>> variantItemsInBank = new HashMap<>(); // key is the variant base id; the list contains the item widgets that go in this variant base id;
		for (Widget bankItem : client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren()) {
			if (bankItem.isHidden()) continue;

			int itemId = bankItem.getItemId();
			if (itemId < 0) {
				continue; // idk what an item id < 0 means but I'm not interested in it.
			}

			int nonPlaceholderId = getNonPlaceholderId(itemId);

			if (itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
				int variationBaseId = ItemVariationMapping.map(nonPlaceholderId);
				List<Widget> l = variantItemsInBank.getOrDefault(variationBaseId, new ArrayList<>());
				l.add(bankItem);
				variantItemsInBank.put(variationBaseId, l);
			}
		}
//		log.debug("variant items in bank: " + variantItemsInBank);

		// TODO what if people remove the sidebar.

		Map<Integer, List<Integer>> variantItemsInLayout = new HashMap<>(); // key is the variant base id; the list contains the item id of the items.
		ArrayList<Map.Entry<Integer, Integer>> entries = new ArrayList<>(itemPositionIndexes.entrySet());
		entries.sort(Comparator.comparingInt(Map.Entry::getValue));
		for (Map.Entry<Integer, Integer> integerIntegerEntry : entries) {
			int itemId = integerIntegerEntry.getKey();

			int nonPlaceholderId = getNonPlaceholderId(itemId);

			if (itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
				int variationBaseId = ItemVariationMapping.map(nonPlaceholderId);
				List<Integer> l = variantItemsInLayout.getOrDefault(variationBaseId, new ArrayList<>());
				l.add(integerIntegerEntry.getKey());
				variantItemsInLayout.put(variationBaseId, l);
			}
		}
//		log.debug("variant items in layout: " + variantItemsInLayout);

		for (Map.Entry<Integer, List<Widget>> integerListEntry : variantItemsInBank.entrySet()) {
			int variationBaseId = integerListEntry.getKey();
			List<Widget> notYetPositionedWidgets = new ArrayList<>(integerListEntry.getValue());
			// first, figure out if there is a perfect match.
			Iterator<Widget> iter = notYetPositionedWidgets.iterator();
			while (iter.hasNext()) {
				Widget widget = iter.next();
				int itemId = widget.getItemId();

//				log.debug("variationBaseId is " + variationBaseId);
				List<Integer> itemIds = variantItemsInLayout.get(variationBaseId);
				if (itemIds == null) continue; // TODO do I need this line?

				if (!itemIds.contains(itemId)) itemId = switchPlaceholderId(widget.getItemId());
				if (!itemIds.contains(itemId)) continue;

				int index = itemPositionIndexes.get(itemId);
//				log.debug("item " + itemName(itemId) + " (" + itemId + ") assigned on pass 1 to index " + index);
				indexToWidget.put(index, widget);
				iter.remove();
			}

			// If there was no perfect match, put the items in any unoccupied slot.
			iter = notYetPositionedWidgets.iterator();
			while (iter.hasNext()) {
				Widget widget = iter.next();

				List<Integer> itemIds = variantItemsInLayout.get(variationBaseId);
				if (itemIds == null) continue; // TODO do I need this line?
				for (Integer id : itemIds) {
					int index = itemPositionIndexes.get(id);
					if (!indexToWidget.containsKey(index)) {
//						log.debug("item " + itemName(switchPlaceholderId(widget.getItemId())) + switchPlaceholderId(widget.getItemId()) + " assigned on pass 3 to index " + index);
						indexToWidget.put(index, widget);
						iter.remove();
						break;
					}
				}
			}

			if (!notYetPositionedWidgets.isEmpty()) {
				for (Widget notYetPositionedWidget : notYetPositionedWidgets) {
					int itemId = notYetPositionedWidget.getItemId();
					int index = assignPosition(itemPositionIndexes);
					itemPositionIndexes.put(itemId, index);
//					log.debug("\t\tnew position: " + index + notYetPositionedWidget.getItemId());
					indexToWidget.put(index, notYetPositionedWidget);
				}
			}
		}
	}

	/**
	 * Doesn't handle variation items.
	 */
	private int getIndexForItem(int itemId, Map<Integer, Integer> itemPositionIndexes) {
		Integer index = itemPositionIndexes.get(itemId);
		if (index != null) return index;

		// swap the item with its placeholder (or vice versa) and try again.
		int otherItemId = switchPlaceholderId(itemId);
		index = itemPositionIndexes.get(otherItemId);
		if (index != null) return index;

		return -1;
	}

	private Integer getItemForIndex(int index, Map<Integer, Integer> itemPositionIndexes) {
	    // TODO stop using a map if you're gonna use it both directions.
		for (Map.Entry<Integer, Integer> entry : itemPositionIndexes.entrySet()) {
			if (entry.getValue() == index) {
				return entry.getKey();
			}
		}
		return null;
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
	private boolean itemShouldBeTreatedAsHavingVariants(int nonPlaceholderItemId) {
		return itemHasVariants(nonPlaceholderItemId);
	}

	Map<Integer, Integer> getBankOrder(String bankTagName) {
		if (isShowingPreview()) {
			return previewLayout;
        }

		return getBankOrderNonPreview(bankTagName);
	}

	/**
	 * unlike getBankOrder, this will not return a preview layout when one is currently being show.
	 */
	private Map<Integer, Integer> getBankOrderNonPreview(String bankTagName) {
		String configuration = configManager.getConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName);
		if (LAYOUT_EXPLICITLY_DISABLED.equals(configuration)) return null;
		if (configuration == null && !config.layoutEnabledByDefault()) return null;
		if (configuration == null) configuration = "";
		return bankTagOrderStringToMapIgnoreNumberFormatException(configuration);
	}

	private void exportLayout(String tagName) {
		String exportString = BANK_TAG_STRING_PREFIX + tagName;
		String layout = bankTagOrderMapToString(getBankOrder(tagName));
		if (!layout.isEmpty()) {
			exportString += ",";
		}
		exportString += layout;

		List<String> tabNames = Text.fromCSV(MoreObjects.firstNonNull(configManager.getConfiguration(BankTagsPlugin.CONFIG_GROUP, TAG_TABS_CONFIG), ""));
		if (!tabNames.contains(tagName)) {
			chatErrorMessage("Couldn't export layout-ed tag tab - tag tab doesn't see to exist?");
		}

		List<String> data = new ArrayList<>();
		data.add(tagName);
		String tagTabIconItemId = configManager.getConfiguration(BankTagsPlugin.CONFIG_GROUP, ICON_SEARCH + tagName);
		if (tagTabIconItemId == null) {
			tagTabIconItemId = "" + ItemID.SPADE;
		}
		data.add(tagTabIconItemId);

		for (Integer item : tagManager.getItemsForTag(tagName)) {
			data.add(String.valueOf(item));
		}

		exportString += ",banktag:" + Text.toCSV(data);

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(exportString), null);
		chatMessage("Copied layout-ed tag \"" + tagName + "\" to clipboard");
	}

	private MessageNode chatErrorMessage(String message) {
		return chatMessage(ColorUtil.wrapWithColorTag(message, Color.RED));
	}

	private MessageNode chatMessage(String message) {
		return client.addChatMessage(ChatMessageType.GAMEMESSAGE, "bla", message, "bla");
	}

	private String bankTagOrderMapToString(Map<Integer, Integer> itemPositionMap) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Integer> integerIntegerEntry : itemPositionMap.entrySet()) {
			sb.append(integerIntegerEntry.getKey() + ":" + integerIntegerEntry.getValue() + ",");
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	private Map<Integer, Integer> bankTagOrderStringToMap(String s) {
		Map<Integer, Integer> map = new HashMap<>();
		if (s.isEmpty()) return map;
		for (String s1 : s.split(",")) {
			String[] split = s1.split(":");
			Integer itemId = Integer.valueOf(split[0]);
			Integer index = Integer.valueOf(split[1]);
			if (index >= 0) {
				map.put(itemId, index);
			}
		}
		return map;
	}

	private Map<Integer, Integer> bankTagOrderStringToMapIgnoreNumberFormatException(String s) {
		Map<Integer, Integer> map = new HashMap<>();
		if (s.isEmpty()) return map;
		for (String s1 : s.split(",")) {
			String[] split = s1.split(":");
			try {
				Integer itemId = Integer.valueOf(split[0]);
				Integer index = Integer.valueOf(split[1]);
				if (index >= 0) {
					map.put(itemId, index);
				} else {
					log.debug("Removed item " + itemName(itemId) + " (" + itemId + ") due to it having a negative index (" + index + ")");
				}
			} catch (NumberFormatException e) {
				log.debug("input string \"" + s + "\"");
			}
		}
		return map;
	}

	static int getXForIndex(int index) {
		return (index % 8) * 48 + 51;
	}

	static int getYForIndex(int index) {
		return (index / 8) * 36;
	}

	private void setItemPositions(Map<Integer, Widget> indexToWidget) {
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
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

	private String lastBankTagTab = null;
	private int lastHeight = Integer.MAX_VALUE;
	private void setContainerHeight(String bankTagName, Map<Integer, Integer> itemPositionIndexes) {
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);

		Optional<Integer> max = itemPositionIndexes.values().stream().max(Integer::compare);
		if (!max.isPresent()) return; // This will result in the minimum height.

		int height = getYForIndex(max.get()) + BANK_ITEM_HEIGHT + 12;
		container.setScrollHeight(height);

		int itemContainerScroll =
				(bankTagName != lastBankTagTab) ? container.getScrollY() :
				(height > lastHeight) ? container.getScrollHeight() : lastScrollY;
		lastHeight = height;
		lastBankTagTab = bankTagName;
		clientThread.invokeLater(() ->
				client.runScript(ScriptID.UPDATE_SCROLLBAR,
						WidgetInfo.BANK_SCROLLBAR.getId(),
						WidgetInfo.BANK_ITEM_CONTAINER.getId(),
						itemContainerScroll));
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

	private void customBankTagOrderInsert(String bankTagName, Widget draggedItem) {
		int draggedOnItemIndex = getIndexForMousePositionNoLowerLimit();
		if (draggedOnItemIndex == -1) return;

		Map<Integer, Integer> itemIdToIndexes = getBankOrder(bankTagName);
		if (itemIdToIndexes == null) return;

		Integer draggedItemIndex = null;
		for (Map.Entry<Integer, Widget> integerWidgetEntry : indexToWidget.entrySet()) {
			if (integerWidgetEntry.getValue().equals(draggedItem)) {
				draggedItemIndex = integerWidgetEntry.getKey();
			}
		}

		customBankTagOrderInsert(bankTagName, draggedItemIndex, draggedOnItemIndex);
	}

	private void customBankTagOrderInsert(String bankTagName, Integer draggedItemIndex, Integer draggedOnItemIndex) {
		// TODO Save multimap due to having multiple items with the same ids in different slots, potentially? I think currently extras are hidden, which is ok with me.
		Map<Integer, Integer> itemIdToIndexes = getBankOrder(bankTagName);
		if (itemIdToIndexes == null) return;

		Integer currentDraggedItemId = getIdForIndex(draggedItemIndex); // TODO getIdForIndex does not factor in variations.
		Integer currentDraggedOnItemId = getIdForIndex(draggedOnItemIndex);
		// Currently I'm just spilling the variant items out in bank order, so I don't care exactly what item id was there - although if I ever decide to change this, this section will become much more complicated, since if I drag a (2) charge onto a regular item, but there was supposed to be a (3) charge there then I have to move the (2) but also deal with where the (2)'s saved position is... At least that's how it'll go if I decide to handle jewellery that way.
		if (currentDraggedItemId == null || itemShouldBeTreatedAsHavingVariants(currentDraggedItemId)) {
			currentDraggedItemId = getItemForIndex(draggedItemIndex, itemIdToIndexes);
		}
		if (currentDraggedOnItemId != null && itemShouldBeTreatedAsHavingVariants(currentDraggedOnItemId)) {
			currentDraggedOnItemId = getItemForIndex(draggedOnItemIndex, itemIdToIndexes);
		}
//		Integer savedDraggedItemId = (itemHasVariants(draggedItemIndex)) ? getItemForIndex(draggedItemIndex, itemIdToIndexes) : ;
//		Integer savedDraggedOnItemId = getItemForIndex(draggedOnItemIndex, itemIdToIndexes);
		// If there is supposed to be an item in the dragged-on location, but that item isn't there, remove that item's custom position.
		if (currentDraggedOnItemId == null) {
			for (Map.Entry<Integer, Integer> itemIdToIndex : itemIdToIndexes.entrySet()) {
				if (itemIdToIndex.getValue().equals(draggedOnItemIndex)) {
					currentDraggedOnItemId = itemIdToIndex.getKey();
					itemIdToIndexes.remove(currentDraggedOnItemId);
					break;
				}
			}
		}

//		log.debug("drag complete: setting index " + draggedOnItemIndex + " from " + "?" + " to " + itemName(currentDraggedItemId));
//		log.debug("               setting index " + draggedItemIndex + " from " + "?" + " to " + itemName(currentDraggedOnItemId));

		Iterator<Map.Entry<Integer, Integer>> iterator = itemIdToIndexes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer> next = iterator.next();
			if (next.getValue() == draggedOnItemIndex || next.getValue() == draggedItemIndex) {
//				log.debug("removing item " + itemName(next.getKey()) + " from " + next.getValue());
				iterator.remove();
			}
		}

		if (currentDraggedItemId != null) itemIdToIndexes.put(currentDraggedItemId, draggedOnItemIndex);
//		if (savedDraggedItemId != null) itemIdToIndexes.remove(savedDraggedItemId);
		if (currentDraggedOnItemId != null) itemIdToIndexes.put(currentDraggedOnItemId, draggedItemIndex);
		saveLayout(bankTagName, itemIdToIndexes);
//		log.debug("saved tag " + bankTagName);

//		log.debug("tag items: " + itemIdToIndexes.toString());

		applyCustomBankTagItemPositions();
	}

	// TODO automatically items that are duplicates due to being placeholders.

    /* I don't know if this is necessary, but I used it in a more buggy version of this plugin to clean up some invalid data. */
	private void cleanDuplicateIndexes(Map<Integer, Integer> itemIdToIndexes) {
		Set<Integer> itemIdsSeen = new HashSet<>();
		List<Integer> toRemove = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : itemIdToIndexes.entrySet()) {
			if (itemIdsSeen.contains(entry.getValue())) {
				log.debug("THIS CODE DID SOMETHING: cleaning " + itemName(entry.getKey()));
				toRemove.add(entry.getKey());
			} else {
				itemIdsSeen.add(entry.getValue());
			}
		}
		toRemove.forEach(itemIdToIndexes::remove);
//		log.debug("HEY LOOK THIS THING ACTUALLY DID SOMETHING: cleaned " + toRemove.size() + " indexes");
	}

	private Integer getIdForIndex(Integer index) {
		for (Map.Entry<Integer, Widget> indexToWidget : indexToWidget.entrySet()) {
			if (indexToWidget.getKey().equals(index)) {
				return indexToWidget.getValue().getItemId();
			}
		}
		for (Map.Entry<Integer, Integer> indexToWidget : getBankOrder(tabInterface.getActiveTab().getTag()).entrySet()) {
			if (indexToWidget.getKey().equals(index)) {
				return indexToWidget.getValue();
			}
		}
		return null;
	}

	private static int assignPosition(Map<Integer, Integer> itemPositionIndexes) {
		List<Integer> indexes = new ArrayList<>(itemPositionIndexes.values());
		indexes.sort(Integer::compare);
		int lastIndex = -1;
		for (Integer integer : indexes) {
			if (integer - lastIndex > 1) {
				break;
			}
			lastIndex = integer;
		}
		lastIndex++;
		return lastIndex;
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
		if (draggedWidget.getId() == WidgetInfo.BANK_ITEM_CONTAINER.getId() && tabInterface.isActive()) {
		    if (hasLayoutEnabled(tabInterface.getActiveTab().getTag())) {
				client.setDraggedOnWidget(null);
			}
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
		MenuEntry[] menuEntries = client.getMenuEntries();
		for (MenuEntry entry : menuEntries)
		{
			if (entry.getOption().equals(PREVIEW_AUTO_LAYOUT))
			{
				event.setForceRightClick(true);
				return;
			}
		}
	}

}
