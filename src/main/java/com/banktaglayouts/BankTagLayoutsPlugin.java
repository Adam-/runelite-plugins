package com.banktaglayouts;

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
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.MessageNode;
import net.runelite.api.Point;
import net.runelite.api.ScriptEvent;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.DraggingWidgetChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
/* TODO
	importing should refresh layout and tag, probably. This is for when the tag that is being
		modified is already active.

	Handling of barrows items still questionable. What happens if you put in a degraded one where previously there was something of a different degradation in the tab?

    upgrade "enable layout" action.

	Why did 1,2,3 dose scb and stamina potions suddenly appear in my cox tab?
	Option to show fake items for things that are in the tag but not in the layout.
	Show fake item menu entry in top left like "Release" placeholder menu entry does.
	Drag fake items.
	Enable/Delete layout does not show up in tag tab tab view.
    enable on all by default.
    	should still be selectively disable-able.
    duplicate tab.
    	Can easily be done with an export then an import, but this would be nice for convenience I guess.

    inventory setups.
    autolayout.

    am i unable to drag an item from one tag to another? Not a huge deal but still.
    	This is also a use case for that pr I submitted on the view tags thing.

	high:
	// dragging an item doesn't seem to use its new itemId in the new position - instead it uses the old saved value.
		// Apparently this doesn't even happen, lol. Problem solved before it even existed.

    medium:
    Make it work with "tag:" search.
    	// What if you have a tag named "tob" and a tag named "tob_somethingelse"?
    	// I thing it would be nice if searching tags just selected the tag normally via the interface.
    	// fix scrolling issue.
	// TODO items with same id that do not stack in bank.

	low:
	// TODO treat variant items which have a placeholder for each variant (e.g. potions) as non-variant items. Probably need to build a list by iterating all items in the game.
		// What is the point of this???? What would it accomplish? Idk why i wrote this weeks ago.

	very low:
	// TODO Maybe fake placeholders so you can see that an item is supposed to be in a currently empty slot?
 */
@PluginDescriptor(
	name = "Bank Tag Layouts in dev",
	description = "Right click a bank tag tabs and click \"Enable layout\", select the tag tab, then drag items in the tag to reposition them.",
	tags = {"bank", "tag", "layout"}
)
@PluginDependency(BankTagsPlugin.class)
public class BankTagLayoutsPlugin extends Plugin
{
	public static final IntPredicate FILTERED_CHARS = c -> "</>:".indexOf(c) == -1;

	public static final Color itemTooltipColor = new Color(0xFF9040);

	public static final String CONFIG_GROUP = "banktaglayouts";
	public static final String TAG_SEARCH = "tag:";
	public static final String LAYOUT_CONFIG_KEY_PREFIX = "layout_";
	public static final String ENABLE_LAYOUT = "Enable layout";
	public static final String DISABLE_LAYOUT = "Delete layout";
	public static final String IMPORT_LAYOUT = "Import tag tab with layout";
	public static final String EXPORT_LAYOUT = "Export tag tab with layout";

	private static final int BANK_ITEM_WIDTH = 36;
	private static final int BANK_ITEM_HEIGHT = 32;
	public static final String REMOVE_FROM_TAG_MENU_OPTION = "Remove-tag";
	public static final String REMOVE_FROM_LAYOUT_MENU_OPTION = "Remove-layout";

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	// This is package-private for use in FakeItemOverlay because if it's @Injected there it's a different TabInterface due to some bug I don't understand.
	@Inject
	TabInterface tabInterface;

	@Inject
	private TagManager tagManager;

	@Inject
	private FakeItemOverlay hasItemOverlay;

	@Inject
	private BankSearch bankSearch;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private BankTagLayoutsConfig config;

	private static final boolean debug = true;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(hasItemOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(hasItemOverlay);
	}

	@Provides
	BankTagLayoutsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankTagLayoutsConfig.class);
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
	    if (!debug) return;

		if ("debugoverlay".equals(commandExecuted.getCommand())) {
			debugOverlay = !debugOverlay;
		}
		if ("itemname".equals(commandExecuted.getCommand())) {
			String[] arguments = commandExecuted.getArguments();
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "bla", itemName(Integer.valueOf(arguments[0])) + " " + arguments[0], "bla");
		}
		if ("printplaceholders".equals(commandExecuted.getCommand())) {
			Map<Integer, Integer> map = new HashMap<>();
//			for (Integer i : Arrays.asList(15282, 19389, 19390, 19391, 12695, 12697, 12699, 12701)) {
			for (int i = 0; i < 40000; i++) {
				ItemComposition itemComposition = itemManager.getItemComposition(i);
				if (itemComposition.getPlaceholderTemplateId() == 14401) { // is placeholder.
					int map1 = ItemVariationMapping.map(getNonPlaceholderId(i));
					if (debug) System.out.println("is placeholder: " + i + " " + getNonPlaceholderId(i) + " " + ItemVariationMapping.getVariations(map1));
					if (ItemVariationMapping.getVariations(map1).size() > 1) {
						int j = map.getOrDefault(map1, 0);
						map.put(map1, j + 1);
						if (debug) System.out.println("item " + itemName(i) + " " + i + " " + "found placeholder (total: " + map.getOrDefault(ItemVariationMapping.map(i), 0) + ")");
					}
				}
			}
			for (Map.Entry<Integer, Integer> integerIntegerEntry : map.entrySet()) {
				if (integerIntegerEntry.getValue() > 1) {
					if (debug) System.out.println("item " + itemName(integerIntegerEntry.getKey()) + " has " + integerIntegerEntry.getValue() + " values.");
				}
			}
		}
	}

	@Subscribe(priority = -1) // I want to run after the Bank Tags plugin does, since it will interfere with the layout-ing if hiding tab separators is enabled.
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == ScriptID.BANKMAIN_BUILD) {
			System.out.println("bank tag layouts bankmain " + client.getTickCount() + " " + System.currentTimeMillis());
			applyCustomBankTagItemPositions();
		}
	}

	// The current indexes for where each widget should appear in the custom bank layout. Should be ignored if there is not tab active.
	private final Map<Integer, Widget> indexToWidget = new HashMap<>();

	// TODO rewrite to use onmenuentryadded.
	@Subscribe
	public void onClientTick(ClientTick t) {
		Widget widget = client.getWidget(12, 9);
		if (widget == null) return;
		for (Widget dynamicChild : widget.getDynamicChildren()) {
		    if (dynamicChild.getActions() == null) continue;
			List<String> actions = Arrays.asList(dynamicChild.getActions());
			if (actions.contains("View tag tab")) {
				String bankTagName = Text.removeTags(dynamicChild.getName());
				int index = actions.indexOf(ENABLE_LAYOUT);
				if (index == -1) {
					index = actions.indexOf(DISABLE_LAYOUT);
					if (index == -1) {
						index = actions.size() + 1;

						hookOnOpListener(dynamicChild, (e) -> {
							Widget clicked = e.getSource();
							String widgetName = Text.removeTags(clicked.getName());
							String action = clicked.getActions()[e.getOp() - 1];
							if (debug) System.out.println("clicked " + widgetName + ", option is " + action);
							if (action.equals(ENABLE_LAYOUT)) {
								if (debug) System.out.println("enabling layout on " + widgetName);
								enableLayout(widgetName);
								return true;
							} else if (action.equals(DISABLE_LAYOUT)) {
								if (debug) System.out.println("disable layout on " + widgetName);
								disableLayout(widgetName);
								return true;
							} else if (action.equals(EXPORT_LAYOUT)) {
								exportLayout(widgetName);
								return true;
							}
							return false;
						});
					}
				}

				dynamicChild.setAction(index, hasLayoutEnabled(bankTagName) ? DISABLE_LAYOUT : ENABLE_LAYOUT);
				if (hasLayoutEnabled(bankTagName)) dynamicChild.setAction(index + 1, EXPORT_LAYOUT);
			} else if (actions.contains("New tag tab")) {
				int index = actions.indexOf(IMPORT_LAYOUT);
				if (index == -1) {
					index = actions.size() + 1;

					hookOnOpListener(dynamicChild, (e) -> {
						Widget clicked = e.getSource();
						String widgetName = Text.removeTags(clicked.getName());
						String action = clicked.getActions()[e.getOp() - 1];
						if (action.equals(IMPORT_LAYOUT)) {
							importLayout();
							return true;
						}
						return false;
					});
				}

				dynamicChild.setAction(index, IMPORT_LAYOUT);
			}
		}
	}

	private void importLayout() {
		System.out.println("importLayout");
		final String dataString;
		try {
			dataString = Toolkit
					.getDefaultToolkit()
					.getSystemClipboard()
					.getData(DataFlavor.stringFlavor)
					.toString()
					.trim();
		} catch (UnsupportedFlavorException | IOException e) {
			chatMessage(ColorUtil.wrapWithColorTag("couldn't import layout-ed tag tab: sorry! Report this to the github page please.", Color.RED));
			e.printStackTrace();
			return;
		}

		// TODO base64encoding. It's not like people are gonna edit it anyways.

		String[] split = dataString.split(",tag:");
		if (split.length != 2) {
			chatMessage(ColorUtil.wrapWithColorTag("couldn't import layout-ed tag tab: invalid format", Color.RED));
			System.out.println("split[0] is " + split[0]);
			throw new IllegalArgumentException();
		}
		String substring = split[0].substring("banktaglayout,".length());
		String layoutString = substring.substring(substring.indexOf(","));
		String name = substring.substring(0, substring.indexOf(","));
		System.out.println("layout: " + layoutString + " " + name);

		if (!tagManager.getItemsForTag(name).isEmpty()) {
			chatboxPanelManager.openTextMenuInput("Tag tab with same name (" + name + ") already exists.")
					.option("Keep both, renaming imported tab", () -> {
					    clientThread.invokeLater(() -> { // If the option is selected by a key, this will not be on the client thread.
							System.out.println("thread: " + Thread.currentThread().getName());
							String newName = generateUniqueName(name);
							if (newName == null) {
								chatMessage(ColorUtil.wrapWithColorTag("couldn't import layout-ed tag tab: couldn't find a unique name", Color.RED));
								return;
							}
							importLayoutNoOverwriteCheck(newName, layoutString, split.length == 2 ? split[1] : null);
						});
					})
					.option("Overwrite existing tab", () -> {
						clientThread.invokeLater(() -> { // If the option is selected by a key, this will not be on the client thread.
							System.out.println("thread: " + Thread.currentThread().getName());
							importLayoutNoOverwriteCheck(name, layoutString, split.length == 2 ? split[1] : null);
						});
					})
					.option("Cancel", Runnables::doNothing)
					.build();
		}
	}

	private String generateUniqueName(String name) {
		for (int i = 2; i < 100; i++) {
			String newName = name + " (" + i + ")";
			if (tagManager.getItemsForTag(newName).isEmpty()) {
				return newName;
			}
		}
		return null;
	}

	private void importLayoutNoOverwriteCheck(String name, String layoutString, String tagString) {
		System.out.println("importing tag data. " + tagString);
		final Iterator<String> dataIter = Text.fromCSV(tagString).iterator();
		dataIter.next(); // skip name.
		StringBuilder sb = new StringBuilder();
		for (char c : name.toCharArray()) {
			if (FILTERED_CHARS.test(c)) {
				sb.append(c);
			}
		}

		if (sb.length() == 0) {
			chatMessage("couldn't import layout-ed tag tab");
			return;
		}

		name = sb.toString();

		final String icon = dataIter.next();

		// precheck all reflective stuff to make sure it's all present before continuing.
		Object tabManager = null;
		try {
			tabManager = getTabManager();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		Method setIcon, loadTab, save, scrollTab, openTag;
		try {
			setIcon = tabManager.getClass().getDeclaredMethod("setIcon", String.class, String.class);
			setIcon.setAccessible(true);
			loadTab = tabInterface.getClass().getDeclaredMethod("loadTab", String.class);
			loadTab.setAccessible(true);
			save = tabManager.getClass().getDeclaredMethod("save");
			save.setAccessible(true);
			scrollTab = tabInterface.getClass().getDeclaredMethod("scrollTab", int.class);
			scrollTab.setAccessible(true);
			openTag = tabInterface.getClass().getDeclaredMethod("openTag", String.class);
			openTag.setAccessible(true);
		} catch (NoSuchMethodException e) {
			chatMessage(ColorUtil.wrapWithColorTag("couldn't import layout-ed tag tab: sorry! Report this to the github page please.", Color.RED));
			e.printStackTrace();
			return;
		}

		try {
			setIcon.invoke(tabManager, name, icon);

			while (dataIter.hasNext()) {
				final int itemId = Integer.parseInt(dataIter.next());
				tagManager.addTag(itemId, name, itemId < 0);
			}

			loadTab.invoke(tabInterface, name);
			save.invoke(tabManager);
			scrollTab.invoke(tabInterface, 0);

			if (tabInterface.getActiveTab() != null && name.equals(tabInterface.getActiveTab().getTag())) {
				openTag.invoke(tabInterface, tabInterface.getActiveTab().getTag());
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			chatMessage(ColorUtil.wrapWithColorTag("couldn't import layout-ed tag tab: sorry! Report this to the github page please.", Color.RED));
			e.printStackTrace();
			return;
		}

		System.out.println("saving layout for tab " + name);
		Map<Integer, Integer> layout = bankTagOrderStringToMap(layoutString);
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + name, bankTagOrderMapToString(layout));
		chatMessage("Imported layout-ed tag tab \"" + name + "\"");

		applyCustomBankTagItemPositions();
	}

	/**
	 * Hooks the widget's onOpListener.
	 *
	 * @param hook the hook. should return true if the click is to be consumed, false otherwise.
 	 */
	private void hookOnOpListener(Widget dynamicChild, Predicate<ScriptEvent> hook) {
		JavaScriptCallback jsc = (JavaScriptCallback) dynamicChild.getOnOpListener()[0];
		dynamicChild.setOnOpListener((JavaScriptCallback) (e) -> {
		    if (!hook.test(e)) {
		    	jsc.run(e);
			}
		});
	}

	private boolean hasLayoutEnabled(String bankTagName) {
		return configManager.getConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName) != null;
	}

	private void enableLayout(String bankTagName) {
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, bankTagOrderMapToString(new HashMap<>()));
		if (tabInterface.getActiveTab() != null && bankTagName.equals(tabInterface.getActiveTab().getTag())) {
			applyCustomBankTagItemPositions();
		}
	}

	private void disableLayout(String bankTagName) {
		chatboxPanelManager.openTextMenuInput("Delete layout for " + bankTagName + "?")
				.option("Yes", () ->
						clientThread.invoke(() ->
						{
							configManager.unsetConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName);
							if (tabInterface.getActiveTab() != null && bankTagName.equals(tabInterface.getActiveTab().getTag())) {
								bankSearch.layoutBank();
							}
						})
				)
				.option("No", Runnables::doNothing)
				.build();
	}

//	@Subscribe
//	public void onGrandExchangeSearched(GrandExchangeSearched event)
//	{
//		final String input = client.getVar(VarClientStr.INPUT_TEXT);
//		if (input.startsWith(TAG_SEARCH)) {
//			applyCustomBankTagItemPositions();
//		}
//    }
//

	private void applyCustomBankTagItemPositions() {
        fakeItems.clear();

		if (!tabInterface.isActive()) {
			System.out.println("returning, not active.");
			return;
		}

		String bankTagName = tabInterface.getActiveTab().getTag();

		if (debug) System.out.println("applyCustomBankTagItemPositions " + bankTagName);

		indexToWidget.clear();
		Map<Integer, Integer> itemPositionIndexes = getBankOrder(bankTagName);
		if (itemPositionIndexes == null) return; // layout not enabled.

		cleanItemsNotInBankTag(itemPositionIndexes, bankTagName);
		cleanDuplicateIndexes(itemPositionIndexes);

		List<Widget> bankItems = Arrays.stream(client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren()).filter(bankItem -> !bankItem.isHidden() && bankItem.getItemId() >= 0).collect(Collectors.toList());

		assignVariantItemPositions(itemPositionIndexes, bankItems);
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

			fakeItems.add(new FakeItem(getXForIndex(index), getYForIndex(index), getNonPlaceholderId(entry.getKey())));
		}

//		if (debug) System.out.println("tag items: " + itemPositionIndexes.toString());

		// What the fuck? It appears that despite my priority setting on the @Subscribe, Bank Tags can still sometimes run after me.
        // invokeLater solves this issue, but it's weird as heck.
//		setItemPositions(indexToWidget);
		clientThread.invokeLater(() -> {
			setItemPositions(indexToWidget);
		});
		System.out.println("indexToWidget " + indexToWidget.size());
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, bankTagOrderMapToString(itemPositionIndexes));
		if (debug) System.out.println("saved tag " + bankTagName);
	}

	private void assignNonVariantItemPositions(Map<Integer, Integer> itemPositionIndexes, List<Widget> bankItems) {
		for (Widget bankItem : bankItems) {
			int itemId = bankItem.getItemId();

			int nonPlaceholderId = getNonPlaceholderId(itemId);

			if (!itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
//				if (debug) System.out.println("\tassigning position for " + itemName(itemId) + itemId + ": ");
				int indexForItem = getIndexForItem(itemId, itemPositionIndexes);
				if (indexForItem != -1) {
//					if (debug) System.out.println("\t\texisting position: " + indexForItem);
					indexToWidget.put(indexForItem, bankItem);
				} else {
					int newIndex = assignPosition(itemPositionIndexes);
					itemPositionIndexes.put(itemId, newIndex);
//					if (debug) System.out.println("\t\tnew position: " + newIndex);
					indexToWidget.put(newIndex, bankItem);
				}
			}
		}
	}

	public final Set<FakeItem> fakeItems = new HashSet<>();

	@Data
	public static class FakeItem {
		public final int originalX;
		public final int originalY;
		public final int itemId;

		public boolean contains(Point mouseCanvasPosition) {
			return true;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		if (!menuEntryAdded.getOption().equalsIgnoreCase("cancel")) return;

		if (!tabInterface.isActive() || !config.showLayoutPlaceholders()) {
			return;
		}
		Map<Integer, Integer> itemIdToIndexes = getBankOrder(tabInterface.getActiveTab().getTag());
		if (itemIdToIndexes == null) return;

		Point mouseCanvasPosition = client.getMouseCanvasPosition();
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null) return;
		Point canvasLocation = bankItemContainer.getCanvasLocation();
		int scrollY = bankItemContainer.getScrollY();

		int row = (mouseCanvasPosition.getY() - bankItemContainer.getCanvasLocation().getY() + scrollY + 2) / 36;
		int col = (mouseCanvasPosition.getX() - bankItemContainer.getCanvasLocation().getX() - 51 + 6) / 48;
		int index = row * 8 + col;
		Map.Entry<Integer, Integer> entry = itemIdToIndexes.entrySet().stream()
				.filter(e -> e.getValue() == index)
				.findAny().orElse(null);

		if (entry != null && !indexToWidget.containsKey(entry.getValue())) {
			MenuEntry newEntry = new MenuEntry();
			newEntry.setOption(REMOVE_FROM_TAG_MENU_OPTION + " (" + tabInterface.getActiveTab().getTag() + ")");
			newEntry.setTarget(ColorUtil.wrapWithColorTag(itemName(entry.getKey()), itemTooltipColor));
			newEntry.setType(MenuAction.RUNELITE.getId());
			newEntry.setParam0(entry.getKey());

			insertMenuEntry(newEntry, client.getMenuEntries(), true);

			newEntry = new MenuEntry();
			newEntry.setOption(REMOVE_FROM_LAYOUT_MENU_OPTION + " (" + tabInterface.getActiveTab().getTag() + ")");
			newEntry.setTarget(ColorUtil.wrapWithColorTag(itemName(entry.getKey()), itemTooltipColor));
			newEntry.setType(MenuAction.RUNELITE.getId());
			newEntry.setParam0(entry.getKey());

			insertMenuEntry(newEntry, client.getMenuEntries(), true);
		}
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
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		// If this is on a real item, then the bank tags plugin will remove it from the tag, and this plugin only needs
		// to remove it from the layout. If this is on a fake item, this plugin must do both (unless the "Remove-layout"
		// option was clicked, then the tags are not touched).
		if (
				event.getMenuAction() == MenuAction.RUNELITE
				&& (
						event.getMenuOption().startsWith(REMOVE_FROM_TAG_MENU_OPTION)
						|| event.getMenuOption().startsWith(REMOVE_FROM_LAYOUT_MENU_OPTION)
				)
		) {
			boolean isOnFakeItem = event.getWidgetId() != WidgetInfo.BANK_ITEM_CONTAINER.getId();
			boolean layoutOnly = event.getMenuOption().startsWith(REMOVE_FROM_LAYOUT_MENU_OPTION);
			System.out.println("clicked: " + event.getMenuOption());
			event.consume();
			int index = event.getActionParam();
			System.out.println("action param: " + index);
			System.out.println("action param: " + event.getId() + " " + event.getMenuAction() + " " + event.getWidgetId() + " " + event.getSelectedItemIndex());

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
					System.out.println("removing from layout " + entryItemId);
					iter.remove();
				}
			}
			configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, bankTagOrderMapToString(bankOrder));

			if (isOnFakeItem && !layoutOnly) {
				ItemComposition itemComposition = itemManager.getItemComposition(itemId);
				System.out.println("Removing from tag: " + itemId + " " + itemComposition.getId());
				tagManager.removeTag(itemComposition.getId(), bankTagName);

				// This will trigger applyCustomBankTagItemPositions via the bankmain_build script, in addition to removing the item from the tag.
				bankSearch.layoutBank();
			} else if (layoutOnly) {
				applyCustomBankTagItemPositions();
			}
		}
	}

	private void cleanItemsNotInBankTag(Map<Integer, Integer> itemPositionIndexes, String bankTagName) {
		Iterator<Map.Entry<Integer, Integer>> iter = itemPositionIndexes.entrySet().iterator();

		while (iter.hasNext()) {
			int itemId = iter.next().getKey();

			try {
				if (!findTag(itemId, bankTagName)) {
					if (debug) System.out.println("removing " + itemName(itemId) + " (" + itemId + ") because it is no longer in the bank tag");
					iter.remove();
				}
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				if (debug) e.printStackTrace();
			}
		}
	}

	private boolean findTag(int itemId, String search) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = TagManager.class.getDeclaredMethod("findTag", int.class, String.class);
		method.setAccessible(true);
		return (boolean) method.invoke(tagManager, itemId, search);
	}

	private void assignVariantItemPositions(Map<Integer, Integer> itemPositionIndexes, List<Widget> bankItems) {
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
//		if (debug) System.out.println("variant items in bank: " + variantItemsInBank);

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
//		if (debug) System.out.println("variant items in layout: " + variantItemsInLayout);

		for (Map.Entry<Integer, List<Widget>> integerListEntry : variantItemsInBank.entrySet()) {
			int variationBaseId = integerListEntry.getKey();
			List<Widget> notYetPositionedWidgets = new ArrayList<>(integerListEntry.getValue());
			// first, figure out if there is a perfect match.
			Iterator<Widget> iter = notYetPositionedWidgets.iterator();
			while (iter.hasNext()) {
				Widget widget = iter.next();
				int itemId = widget.getItemId();

//				if (debug) System.out.println("variationBaseId is " + variationBaseId);
				List<Integer> itemIds = variantItemsInLayout.get(variationBaseId);
				if (itemIds == null) continue; // TODO do I need this line?

				if (!itemIds.contains(itemId)) itemId = switchPlaceholderId(widget.getItemId());
				if (!itemIds.contains(itemId)) continue;

				int index = itemPositionIndexes.get(itemId);
//				if (debug) System.out.println("item " + itemName(itemId) + " (" + itemId + ") assigned on pass 1 to index " + index);
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
//						if (debug) System.out.println("item " + itemName(switchPlaceholderId(widget.getItemId())) + switchPlaceholderId(widget.getItemId()) + " assigned on pass 3 to index " + index);
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
//					if (debug) System.out.println("\t\tnew position: " + index + notYetPositionedWidget.getItemId());
					indexToWidget.put(index, notYetPositionedWidget);
				}
			}
		}
	}

	/**
	 * Doesn't handle variation items.
	 * @param itemId
	 * @param itemPositionIndexes
	 * @return
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
	 * @param nonPlaceholderItemId
	 */
	private boolean itemHasVariants(int nonPlaceholderItemId) {
		return ItemVariationMapping.getVariations(ItemVariationMapping.map(nonPlaceholderItemId)).size() > 1;
	}

	/**
	 * Whether this item should be treated as having variants for the purpose of custom bank layouts.
	 * If true, this means that the item should occupy the next available position in the custom layout which matches either its own id or any of its variants.
	 * This includes placeholders for the item.
	 * This does mean that the order that items appear in in the normal bank has an impact on the custom layout. Not something you'd expect from this feature, lol.
	 * @param nonPlaceholderItemId
	 */
	private boolean itemShouldBeTreatedAsHavingVariants(int nonPlaceholderItemId) {
		return itemHasVariants(nonPlaceholderItemId);
	}

	Map<Integer, Integer> getBankOrder(String bankTagName) {
		String configuration = configManager.getConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName);
		return (configuration == null) ? null : bankTagOrderStringToMap(configuration);
	}

	private void exportLayout(String tagName) {
	    // TODO check that layout is enabled. Do this also for the right-click option.
//		"banktaglayout,tagName,layoutstring,tag:tagstring";
		String layout = "banktaglayout," + tagName + ",";
		layout += configManager.getConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + tagName);

		TagTab tagTab = null;
		try {
			tagTab = getTab(tagName);
		} catch (ReflectiveOperationException e) {
			if (debug) e.printStackTrace();
			chatMessage("Couldn't get tab string.");
		}

		if (tagTab != null) {
			List<String> data = new ArrayList<>();
			data.add(tagTab.getTag());
			data.add(String.valueOf(tagTab.getIconItemId()));

			for (Integer item : tagManager.getItemsForTag(tagTab.getTag())) {
				data.add(String.valueOf(item));
			}

			layout += ",tag:" + Text.toCSV(data);
		}

		if (debug) System.out.println("export: \"" + layout + "\"");

//		Deflater d = new Deflater();
//		d.setInput(layout.getBytes());
//		byte[] output = new byte[10000];
//		int a = d.deflate(output);
//		Inflater i = new Inflater();
//		i.setInput(output);
//		byte[] out = new byte[10000];
//		try {
//		    System.out.println(i.inflate(out));
//			i.inflate(out);
//		} catch (DataFormatException e) {
//			e.printStackTrace();
//		}
//		System.out.println("compressed length: " + a + " " + layout.getBytes().length + " " + out.length);
//
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(layout), null);
		chatMessage("Copied layout-ed tag \"" + tagName + "\" to clipboard");
	}

	private MessageNode chatMessage(String message) {
		return client.addChatMessage(ChatMessageType.GAMEMESSAGE, "bla", message, "bla");
	}

	private static String escapeCommas(String s) {
		String escaped = s.replaceAll("\\\\", "\\\\\\\\").replaceAll(",", "\\,");
		System.out.println("escaped: " + escaped);
		return escaped;
	}

	public static void main(String[] args) {

		String x = escapeCommas("hello, there\\");
		System.out.println(x);
		System.out.println(unescapeCommas(x));
	}

	private static String unescapeCommas(String s) {
		return s.replaceAll("\\,", ",").replaceAll("\\\\", "\\");
	}

	private TagTab getTab(String tagName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		Object tabManager = getTabManager();

		Method find = tabManager.getClass().getDeclaredMethod("find", String.class);
		find.setAccessible(true);
		return (TagTab) find.invoke(tabManager, tagName);
	}

	private Object getTabManager() throws NoSuchFieldException, IllegalAccessException {
		Field tabManagerField = TabInterface.class.getDeclaredField("tabManager");
		tabManagerField.setAccessible(true);
		Object o = tabManagerField.get(tabInterface);
		return o;
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
			try {
				map.put(Integer.valueOf(split[0]), Integer.valueOf(split[1]));

			} catch (NumberFormatException e) {
//				e.printStackTrace();
				if (debug) System.out.println("input string \"" + s + "\"");
			}
		}
		return map;
	}

	int getXForIndex(int index) {
		return (index % 8) * 48 + 51;
	}

	int getYForIndex(int index) {
		return (index / 8) * 36;
	}

	private void setItemPositions(Map<Integer, Widget> indexToWidget) {
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		outer_loop:
		for (Widget child : container.getDynamicChildren()) {
			int itemId = getNonPlaceholderId(child.getItemId());
			for (Map.Entry<Integer, Widget> integerWidgetEntry : indexToWidget.entrySet()) {
				if (integerWidgetEntry.getValue().equals(child)) {
					continue outer_loop;
				}
			}

//			System.out.println("hiding widget " + child.getItemId());
			child.setHidden(true);
			child.revalidate();
		}


		for (Map.Entry<Integer, Widget> entry : indexToWidget.entrySet()) {
			Widget widget = entry.getValue();
			int index = entry.getKey();

//			System.out.println("moving widget " + widget.getItemId() + " to " + ((index % 8) * 48 + 51) + ", " + (index / 8) * 36);
			widget.setOriginalX(getXForIndex(index));
			widget.setOriginalY(getYForIndex(index));
			widget.revalidate();
		}

		container.setScrollHeight(2000);
		final int itemContainerScroll = container.getScrollY();
		clientThread.invokeLater(() ->
				client.runScript(ScriptID.UPDATE_SCROLLBAR,
						WidgetInfo.BANK_SCROLLBAR.getId(),
						WidgetInfo.BANK_ITEM_CONTAINER.getId(),
						itemContainerScroll));
	}

	public String itemName(Integer itemId) {
		return (itemId == null) ? "null" : itemManager.getItemComposition(itemId).getName();
	}

	public boolean debugOverlay = true;

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
		net.runelite.api.Point mouseCanvasPosition = client.getMouseCanvasPosition();
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		net.runelite.api.Point point = new net.runelite.api.Point(mouseCanvasPosition.getX() - container.getCanvasLocation().getX(), mouseCanvasPosition.getY() - container.getCanvasLocation().getY());
//		if (debug) System.out.println("mouse canvas position: " + mouseCanvasPosition + " bank widget canvas position: " + container.getCanvasLocation());
//		if (debug) System.out.println("bank container relative position: " + point);
//		System.out.println("mouse drag complete " + draggedItemId + " " + draggedOnItemId + " " + ((lastDraggedOnWidget == null) ? "null" : lastDraggedOnWidget.getItemId()) + " " + ((draggedWidget == null) ? "null" : draggedWidget.getItemId()));

		Map<Integer, Integer> itemIdToIndexes = getBankOrder(bankTagName);
		if (itemIdToIndexes == null) return;

		int row = (point.getY() + container.getScrollY() + 2) / 36;
		int col = (point.getX() - 51 + 6) / 48;
//		if (debug) System.out.println("row col " + row + " " + col + " " + row * 8 + col);
		int draggedOnItemIndex = row * 8 + col;
		Integer draggedItemIndex = null;
		for (Map.Entry<Integer, Widget> integerWidgetEntry : indexToWidget.entrySet()) {
			if (integerWidgetEntry.getValue().equals(draggedItem)) {
				draggedItemIndex = integerWidgetEntry.getKey();
			}
		}
		if (draggedItemIndex == null) {
//			if (debug) System.out.println("DRAGGED ITEM WAS NULL");
		}

		// TODO Save multimap due to having multiple items with the same ids in different slots, potentially? I think currently extras are hidden, which is ok with me.
		Integer currentDraggedItemId = getIdForIndex(draggedItemIndex); // TODO getIdForIndex does not factor in variations.
		Integer currentDraggedOnItemId = getIdForIndex(draggedOnItemIndex);
		// Currently I'm just spilling the variant items out in bank order, so I don't care exactly what item id was there - although if I ever decide to change this, this section will become much more complicated, since if I drag a (2) charge onto a regular item, but there was supposed to be a (3) charge there then I have to move the (2) but also deal with where the (2)'s saved position is... At least that's how it'll go if I decide to handle jewellery that way.
		if (itemShouldBeTreatedAsHavingVariants(currentDraggedItemId)) {
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

//		if (debug) System.out.println("drag complete: setting index " + draggedOnItemIndex + " from " + "?" + " to " + itemName(currentDraggedItemId));
//		if (debug) System.out.println("               setting index " + draggedItemIndex + " from " + "?" + " to " + itemName(currentDraggedOnItemId));

		Iterator<Map.Entry<Integer, Integer>> iterator = itemIdToIndexes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer> next = iterator.next();
			if (next.getValue() == draggedOnItemIndex || next.getValue() == draggedItemIndex) {
//				if (debug) System.out.println("removing item " + itemName(next.getKey()) + " from " + next.getValue());
				iterator.remove();
			}
		}

		if (currentDraggedItemId != null) itemIdToIndexes.put(currentDraggedItemId, draggedOnItemIndex);
//		if (savedDraggedItemId != null) itemIdToIndexes.remove(savedDraggedItemId);
		if (currentDraggedOnItemId != null) itemIdToIndexes.put(currentDraggedOnItemId, draggedItemIndex);
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, bankTagOrderMapToString(itemIdToIndexes));
//		if (debug) System.out.println("saved tag " + bankTagName);

//		if (debug) System.out.println("tag items: " + itemIdToIndexes.toString());

		applyCustomBankTagItemPositions();
	}

	// TODO automatically items that are duplicates due to being placeholders.

    /* I don't know if this is necessary, but I used it in a more buggy version of this plugin to clean up some invalid data. */
	private void cleanDuplicateIndexes(Map<Integer, Integer> itemIdToIndexes) {
		Set<Integer> itemIdsSeen = new HashSet<>();
		List<Integer> toRemove = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : itemIdToIndexes.entrySet()) {
			if (itemIdsSeen.contains(entry.getValue())) {
				if (debug) System.out.println("THIS CODE DID SOMETHING: cleaning " + itemName(entry.getKey()));
				toRemove.add(entry.getKey());
			} else {
				itemIdsSeen.add(entry.getValue());
			}
		}
		toRemove.forEach(itemIdToIndexes::remove);
//		if (debug) System.out.println("HEY LOOK THIS THING ACTUALLY DID SOMETHING: cleaned " + toRemove.size() + " indexes");
	}

	private Integer getIdForIndex(Integer index) {
		for (Map.Entry<Integer, Widget> indexToWidget : indexToWidget.entrySet()) {
			if (indexToWidget.getKey().equals(index)) {
				return indexToWidget.getValue().getItemId();
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
	@Subscribe
	public void onDraggingWidgetChanged(DraggingWidgetChanged event) {
		Widget widget = client.getWidget(WidgetInfo.BANK_CONTAINER);
		if (widget == null || widget.isHidden()) {
			return;
		}

		Widget draggedWidget = client.getDraggedWidget();

		// Returning early or nulling the drag release listener has no effect. Hence, we need to
		// null the draggedOnWidget instead.
		if (draggedWidget.getId() == WidgetInfo.BANK_ITEM_CONTAINER.getId() && tabInterface.isActive()) {
			client.setDraggedOnWidget(null);
		}
	}
}
