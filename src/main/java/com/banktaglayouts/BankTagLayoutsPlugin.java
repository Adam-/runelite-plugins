package com.banktaglayouts;

import com.google.common.collect.ObjectArrays;
import com.google.common.util.concurrent.Runnables;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.MessageNode;
import net.runelite.api.Point;
import net.runelite.api.ScriptEvent;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.DraggingWidgetChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetType;
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
import net.runelite.client.plugins.menuentryswapper.ShiftDepositMode;
import net.runelite.client.plugins.menuentryswapper.ShiftWithdrawMode;
import net.runelite.client.ui.overlay.OverlayManager;
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
import java.util.Collection;
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
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Slf4j
/* TODO
    upgrade "enable layout" action.
    inventory setups.
    autolayout.
    import/export.
    enable on all by default.
    	should still be selectively disable-able.
    fake items.
    duplicate tab.
    	Can easily be done with an export then an import, but this would be nice for convenience I guess.
    Lines still show up if you have layout enabled.

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

	public static final String CONFIG_GROUP = "banktaglayouts";
	public static final String TAG_SEARCH = "tag:";
	public static final String LAYOUT_CONFIG_KEY_PREFIX = "layout_";
	public static final String ENABLE_LAYOUT = "Enable layout";
	public static final String DISABLE_LAYOUT = "Delete layout";
	public static final String IMPORT_LAYOUT = "Import layout";
	public static final String EXPORT_LAYOUT = "Export layout";

	private static final int BANK_ITEM_WIDTH = 36;
	private static final int BANK_ITEM_HEIGHT = 32;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	public ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	TabInterface tabInterface;

	@Inject
	private TagManager tagManager;

	@Inject
	private HasItemOverlay hasItemOverlay;

	@Inject
	private BankSearch bankSearch;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

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


//	@Subscribe(priority = -1) // I want to run after the Bank Tags plugin does, since it will interfere with the layout-ing if hiding tab separators is enabled.
	@Subscribe
	public void onScriptPreFired(ScriptPreFired event) {
//		if (event.getScriptId() == ScriptID.BANKMAIN_BUILD) {
//		if (event.getScriptId() == 276) {
//			System.out.println("before: " + client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren().length);
//			client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).deleteAllChildren();
//			System.out.println("after: " + client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren().length);
//		}
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
				dynamicChild.setAction(index + 1, EXPORT_LAYOUT);
			} else if (actions.contains("New tag tab")) {
				int index = actions.indexOf(IMPORT_LAYOUT);
				if (index == -1) {
					index = actions.size() + 1;

					hookOnOpListener(dynamicChild, (e) -> {
						Widget clicked = e.getSource();
						String widgetName = Text.removeTags(clicked.getName());
						String action = clicked.getActions()[e.getOp() - 1];
						if (action.equals(IMPORT_LAYOUT)) {
							try {
								importLayout();
							} catch (IOException | UnsupportedFlavorException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ioException) {
								ioException.printStackTrace();
							}
							return true;
						}
						return false;
					});
				}

				dynamicChild.setAction(index, IMPORT_LAYOUT);
			}
		}
	}

	private void importLayout() throws IOException, UnsupportedFlavorException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    System.out.println("importLayout");
		final String dataString = Toolkit
				.getDefaultToolkit()
				.getSystemClipboard()
				.getData(DataFlavor.stringFlavor)
				.toString()
				.trim();

		// TODO import warning overwrite.
		// TODO base64encoding. It's not like people are gonna edit it anyways.

		String[] split = dataString.split(",tag:");
		if (split.length == 0 || split.length > 2) {
			System.out.println("split[0] is " + split[0]);
			throw new IllegalArgumentException();
		}
		String substring = split[0].substring("banktaglayout,".length());
		String layoutString = substring.substring(substring.indexOf(","));
		String name = substring.substring(0, substring.indexOf(","));
		System.out.println("layout: " + layoutString + " " + name);
		Map<Integer, Integer> layout = bankTagOrderStringToMap(layoutString);

		System.out.println("saving layout for tab " + name);
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + name, bankTagOrderMapToString(layout));

		if (split.length == 2) {
		    System.out.println("importing tag data. " + split[1]);
			final Iterator<String> dataIter = Text.fromCSV(split[1]).iterator();
			dataIter.next(); // skip name.
			StringBuilder sb = new StringBuilder();
			for (char c : name.toCharArray())
			{
				if (FILTERED_CHARS.test(c))
				{
					sb.append(c);
				}
			}

			// TODO precheck all reflective stuff to make sure it actually exists.

			if (sb.length() == 0)
			{
			    chatMessage("couldn't import tag tab");
				return;
			}

			name = sb.toString();

			final String icon = dataIter.next();
			Object tabManager = getTabManager();

			Method setIcon = tabManager.getClass().getDeclaredMethod("setIcon", String.class, String.class);
			setIcon.setAccessible(true);
			setIcon.invoke(tabManager, name, icon);

			while (dataIter.hasNext())
			{
				final int itemId = Integer.parseInt(dataIter.next());
				tagManager.addTag(itemId, name, itemId < 0);
			}

			Method loadTab = tabInterface.getClass().getDeclaredMethod("loadTab", String.class);
			loadTab.setAccessible(true);
			loadTab.invoke(tabInterface, name);
			Method save = tagManager.getClass().getDeclaredMethod("save");
			save.setAccessible(true);
			save.invoke(tagManager);
			Method scrollTab = tabInterface.getClass().getDeclaredMethod("scrollTab", int.class);
			scrollTab.setAccessible(true);
			scrollTab.invoke(tabInterface, 0);

			if (tabInterface.getActiveTab() != null && name.equals(tabInterface.getActiveTab().getTag()))
			{
				Method openTag = tabInterface.getClass().getDeclaredMethod("openTag", String.class);
				openTag.setAccessible(true);
				openTag.invoke(tabInterface, tabInterface.getActiveTab());
			}
		}
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

	// TODO my test tabs disappeared?

	private void applyCustomBankTagItemPositions() {
        fakeItems.clear();

        System.out.println(tabInterface);
		if (!tabInterface.isActive()) {
			System.out.println("returning, not active.");
			return;
		}

		String bankTagName = tabInterface.getActiveTab().getTag();

		if (debug) System.out.println("applyCustomBankTagItemPositions " + bankTagName);

		indexToWidget.clear();
		Map<Integer, Integer> itemPositionIndexes = getBankOrder(bankTagName);
		if (itemPositionIndexes == null) return; // layout not enabled.

		// TODO uncomment.
//		cleanItemsNotInBankTag(itemPositionIndexes, bankTagName);
//		cleanDuplicateIndexes(itemPositionIndexes);

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

		if (debug) System.out.println("tag items: " + itemPositionIndexes.toString());

		setItemPositions(indexToWidget);
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, bankTagOrderMapToString(itemPositionIndexes));
		if (debug) System.out.println("saved tag " + bankTagName);
	}

	private void assignNonVariantItemPositions(Map<Integer, Integer> itemPositionIndexes, List<Widget> bankItems) {
		for (Widget bankItem : bankItems) {
			int itemId = bankItem.getItemId();

			int nonPlaceholderId = getNonPlaceholderId(itemId);

			if (!itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
				if (debug) System.out.println("\tassigning position for " + itemName(itemId) + itemId + ": ");
				int indexForItem = getIndexForItem(itemId, itemPositionIndexes);
				if (indexForItem != -1) {
					if (debug) System.out.println("\t\texisting position: " + indexForItem);
					indexToWidget.put(indexForItem, bankItem);
				} else {
					int newIndex = assignPosition(itemPositionIndexes);
					itemPositionIndexes.put(itemId, newIndex);
					if (debug) System.out.println("\t\tnew position: " + newIndex);
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

		for (FakeItem fakeItem : fakeItems) {
			if (fakeItem.contains(client.getMouseCanvasPosition())) {
				MenuEntry hopTo = new MenuEntry();
				hopTo.setOption(client.getMouseCanvasPosition().getX() > 200 ? "test option 1" : "test option 2");
				hopTo.setTarget("bla");
				hopTo.setType(MenuAction.RUNELITE.getId());

				insertMenuEntry(hopTo, client.getMenuEntries(), true);
			}
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

	// TODO can I use "icon_" instead of "item_"?
	private boolean bankTagExists(String bankTagName) {
		return configManager.getConfigurationKeys("banktags" + "." + "item_").stream()
				.filter(key -> {
					List<String> tags = Arrays.asList(configManager.getConfiguration("banktags", key.substring("banktags.".length())).split(","));
					return tags.contains(bankTagName);
				})
				.findAny().isPresent();
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
		if (debug) System.out.println("variant items in bank: " + variantItemsInBank);

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
		if (debug) System.out.println("variant items in layout: " + variantItemsInLayout);

		for (Map.Entry<Integer, List<Widget>> integerListEntry : variantItemsInBank.entrySet()) {
			int variationBaseId = integerListEntry.getKey();
			List<Widget> notYetPositionedWidgets = new ArrayList<>(integerListEntry.getValue());
			// first, figure out if there is a perfect match.
			Iterator<Widget> iter = notYetPositionedWidgets.iterator();
			while (iter.hasNext()) {
				Widget widget = iter.next();
				int itemId = widget.getItemId();

				if (debug) System.out.println("variationBaseId is " + variationBaseId);
				List<Integer> itemIds = variantItemsInLayout.get(variationBaseId);
				if (itemIds == null) continue; // TODO do I need this line?

				if (!itemIds.contains(itemId)) itemId = switchPlaceholderId(widget.getItemId());
				if (!itemIds.contains(itemId)) continue;

				int index = itemPositionIndexes.get(itemId);
				if (debug) System.out.println("item " + itemName(itemId) + " (" + itemId + ") assigned on pass 1 to index " + index);
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
						if (debug) System.out.println("item " + itemName(switchPlaceholderId(widget.getItemId())) + switchPlaceholderId(widget.getItemId()) + " assigned on pass 3 to index " + index);
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
					if (debug) System.out.println("\t\tnew position: " + index + notYetPositionedWidget.getItemId());
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

		Deflater d = new Deflater();
		d.setInput(layout.getBytes());
		byte[] output = new byte[10000];
		int a = d.deflate(output);
		Inflater i = new Inflater();
		i.setInput(output);
		byte[] out = new byte[10000];
		try {
		    System.out.println(i.inflate(out));
			i.inflate(out);
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		System.out.println("compressed length: " + a + " " + layout.getBytes().length + " " + out.length);

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(layout), null);
		chatMessage("Copied to clipboard");
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

			System.out.println("hiding widget " + child.getItemId());
			child.setHidden(true);
			child.revalidate();
		}


		for (Map.Entry<Integer, Widget> entry : indexToWidget.entrySet()) {
			Widget widget = entry.getValue();
			int index = entry.getKey();

			System.out.println("moving widget " + widget.getItemId() + " to " + ((index % 8) * 48 + 51) + ", " + (index / 8) * 36);
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

	private void customBankTagOrderInsert(String bankTagName, Widget draggedItem) {
		net.runelite.api.Point mouseCanvasPosition = client.getMouseCanvasPosition();
		Widget container = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		net.runelite.api.Point point = new net.runelite.api.Point(mouseCanvasPosition.getX() - container.getCanvasLocation().getX(), mouseCanvasPosition.getY() - container.getCanvasLocation().getY());
		if (debug) System.out.println("mouse canvas position: " + mouseCanvasPosition + " bank widget canvas position: " + container.getCanvasLocation());
		if (debug) System.out.println("bank container relative position: " + point);
//		System.out.println("mouse drag complete " + draggedItemId + " " + draggedOnItemId + " " + ((lastDraggedOnWidget == null) ? "null" : lastDraggedOnWidget.getItemId()) + " " + ((draggedWidget == null) ? "null" : draggedWidget.getItemId()));

		Map<Integer, Integer> itemIdToIndexes = getBankOrder(bankTagName);
		if (itemIdToIndexes == null) return;

		int row = (point.getY() + container.getScrollY() + 2) / 36;
		int col = (point.getX() - 51 + 6) / 48;
		if (debug) System.out.println("row col " + row + " " + col + " " + row * 8 + col);
		int draggedOnItemIndex = row * 8 + col;
		Integer draggedItemIndex = null;
		for (Map.Entry<Integer, Widget> integerWidgetEntry : indexToWidget.entrySet()) {
			if (integerWidgetEntry.getValue().equals(draggedItem)) {
				draggedItemIndex = integerWidgetEntry.getKey();
			}
		}
		if (draggedItemIndex == null) {
			if (debug) System.out.println("DRAGGED ITEM WAS NULL");
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

		if (debug) System.out.println("drag complete: setting index " + draggedOnItemIndex + " from " + "?" + " to " + itemName(currentDraggedItemId));
		if (debug) System.out.println("               setting index " + draggedItemIndex + " from " + "?" + " to " + itemName(currentDraggedOnItemId));

		Iterator<Map.Entry<Integer, Integer>> iterator = itemIdToIndexes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Integer> next = iterator.next();
			if (next.getValue() == draggedOnItemIndex || next.getValue() == draggedItemIndex) {
				if (debug) System.out.println("removing item " + itemName(next.getKey()) + " from " + next.getValue());
				iterator.remove();
			}
		}

		if (currentDraggedItemId != null) itemIdToIndexes.put(currentDraggedItemId, draggedOnItemIndex);
//		if (savedDraggedItemId != null) itemIdToIndexes.remove(savedDraggedItemId);
		if (currentDraggedOnItemId != null) itemIdToIndexes.put(currentDraggedOnItemId, draggedItemIndex);
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, bankTagOrderMapToString(itemIdToIndexes));
		if (debug) System.out.println("saved tag " + bankTagName);

		if (debug) System.out.println("tag items: " + itemIdToIndexes.toString());

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
