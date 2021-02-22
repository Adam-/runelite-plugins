package com.banktaglayouts;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.DraggingWidgetChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.TabInterface;
import net.runelite.client.plugins.banktags.tabs.TagTab;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
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

@Slf4j
/* TODO for release
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

	very low:
	// TODO Maybe fake placeholders so you can see that an item is supposed to be in a currently empty slot?
 */
@PluginDescriptor(
	name = "Bank Tag Layouts",
	tags = {"bank", "tag", "layout"}
)
@PluginDependency(BankTagsPlugin.class)
public class BankTagLayoutsPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "banktaglayouts";
	public static final String TAG_SEARCH = "tag:";
	public static final String LAYOUT_CONFIG_KEY_PREFIX = "layout_";
	public static final String ENABLE_LAYOUT = "Enable layout";
	public static final String DISABLE_LAYOUT = "Disable layout";

	@Inject
	private Client client;

	@Inject
	private BankTagLayoutsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private TabInterface tabInterface;

	@Inject
	private TagManager tagManager;

	@Inject
	private HasItemOverlay hasItemOverlay;

	@Inject
	private BankSearch bankSearch;

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
	BankTagLayoutsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankTagLayoutsConfig.class);
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
	    if (!debug) return;

	    // garbage, ignore this.
		if ("migratebanktaglayouts".equals(commandExecuted.getCommand())) {
			System.out.println("migrating bank tag layouts");

			for (String configurationKey : configManager.getConfigurationKeys("banktags")) {
//				System.out.println("key is " + configurationKey);
				if (configurationKey.contains(LAYOUT_CONFIG_KEY_PREFIX)) {
					String layout = configManager.getConfiguration("banktags", configurationKey.substring("banktags.".length()));

					System.out.println("moving layout " + configurationKey.substring("banktags.custom_banktagorder_".length()) + " ");
					configurationKey = LAYOUT_CONFIG_KEY_PREFIX + configurationKey.substring("banktags.custom_banktagorder_".length());
					System.out.println(CONFIG_GROUP + "-" + configurationKey + "-" + layout);
					configManager.setConfiguration(CONFIG_GROUP, configurationKey, layout);
				}
			}
		}

		if ("migratebanktaglayoutsdry".equals(commandExecuted.getCommand())) {
			System.out.println("migrating bank tag layouts");

			for (String configurationKey : configManager.getConfigurationKeys("banktags")) {
//				System.out.println("key is " + configurationKey);
				if (configurationKey.contains("banktags.custom_banktagorder_")) {
					String layout = configManager.getConfiguration("banktags", configurationKey);

					System.out.println("moving layout " + configurationKey.substring("banktags.custom_banktagorder_".length()) + " ");
					configurationKey = LAYOUT_CONFIG_KEY_PREFIX + configurationKey.substring("banktags.custom_banktagorder_".length());
//					configManager.setConfiguration(CONFIG_GROUP, configurationKey, layout);
				}
			}
		}

		if ("debugoverlay".equals(commandExecuted.getCommand())) {
			debugOverlay = !debugOverlay;
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
		if ("deletebanktags".equals(commandExecuted.getCommand())) {
			if (debug) System.out.println("deletebanktags");
			for (String configurationKey : configManager.getConfigurationKeys(CONFIG_GROUP)) {
//				System.out.println("key is " + configurationKey);
				if (configurationKey.contains(LAYOUT_CONFIG_KEY_PREFIX)) {
					configManager.unsetConfiguration(CONFIG_GROUP, configurationKey);
					if (debug) System.out.println("deleting " + configurationKey.substring(9, configurationKey.length()));
				}
			}
		}
		if ("removeItem".equals(commandExecuted.getCommand())) {
			TagTab activeTab = tabInterface.getActiveTab();
			String bankTag = activeTab.getTag();
			Map<Integer, Integer> bankOrder = getBankOrder(bankTag);
			int arg1 = Integer.valueOf(commandExecuted.getArguments()[0]);
			if (debug) System.out.println("removing " + arg1 + " from " + bankTag);
			if (!bankOrder.containsKey(arg1)) {
				if (debug) System.out.println("key not found");
			}
			bankOrder.remove(arg1);
			configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTag, bankTagOrderMapToString(bankOrder));
		}
		if ("deletecurrenttaborder".equals(commandExecuted.getCommand()) || "dcto".equals(commandExecuted.getCommand())) {
			TagTab activeTab = tabInterface.getActiveTab();
			String bankTag = activeTab.getTag();
			if (debug) System.out.println("deletecurrenttaborder: \"" + bankTag + "\"");
			if (debug) System.out.println(LAYOUT_CONFIG_KEY_PREFIX + bankTag);
			for (String configurationKey : configManager.getConfigurationKeys(CONFIG_GROUP)) {
				if (debug) System.out.println("key is " + configurationKey);
				if (configurationKey.equals(CONFIG_GROUP + "." + LAYOUT_CONFIG_KEY_PREFIX + bankTag)) {
					String substring = configurationKey.substring(CONFIG_GROUP.length() + ".".length(), configurationKey.length());
					String s = configManager.getConfiguration(CONFIG_GROUP, substring);
					configManager.unsetConfiguration(CONFIG_GROUP, substring);
					configManager.setConfiguration(CONFIG_GROUP, configurationKey, "");
					if (debug) System.out.println("deleting " + configurationKey.substring(9, configurationKey.length()) + " (" + s + ")");
				}
			}
		}
		if ("cleanemptyspots".equals(commandExecuted.getCommand())) {
			TagTab activeTab = tabInterface.getActiveTab();
			String bankTag = activeTab.getTag();
			Map<Integer, Integer> itemIdsToIndexes = getBankOrder(bankTag);
			List<Integer> itemIdsToRemove = new ArrayList<>();
			for (Map.Entry<Integer, Integer> entry : itemIdsToIndexes.entrySet()) {
				if (!indexToWidget.containsKey(entry.getValue()) || entry.getValue() > 300) {
					itemIdsToRemove.add(entry.getKey());
				}
			}
			itemIdsToRemove.forEach(itemIdsToIndexes::remove);
			if (debug) System.out.println("removed empty slots in " + bankTag);
			configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTag, bankTagOrderMapToString(itemIdsToIndexes));
		}
	}

	@Subscribe(priority = -1) // I want to run after the Bank Tags plugin does, since it will interfere with the layout-ing if hiding tab separators is enabled.
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == ScriptID.BANKMAIN_BUILD) {
			applyCustomBankTagItemPositions();
		}
	}

	// The current indexes for where each widget should appear in the custom bank layout. Should be ignored if there is not tab active.
	private final Map<Integer, Widget> indexToWidget = new HashMap<>();

	@Subscribe
	public void onClientTick(ClientTick t) {
		Widget widget = client.getWidget(12, 9);
		if (widget == null) return;
		for (Widget dynamicChild : widget.getDynamicChildren()) {
		    if (dynamicChild.getActions() == null) continue;
			List<String> actions = Arrays.asList(dynamicChild.getActions());
			if (!actions.contains("View tag tab")) continue;

			String bankTagName = Text.removeTags(dynamicChild.getName());
			int index = actions.indexOf(ENABLE_LAYOUT);
			if (index == -1) {
				index = actions.indexOf(DISABLE_LAYOUT);
				if (index == -1) {
					index = actions.size() + 1;

					setUpTagTab(dynamicChild);
				}
			}

			dynamicChild.setAction(index, hasLayoutEnabled(bankTagName) ? DISABLE_LAYOUT : ENABLE_LAYOUT);
		}
	}

	// Hooks the widget's onOpListener to handle my extra option.
	private void setUpTagTab(Widget dynamicChild) {
		JavaScriptCallback jsc = (JavaScriptCallback) dynamicChild.getOnOpListener()[0];
		dynamicChild.setOnOpListener((JavaScriptCallback) (e) -> {
			Widget clicked = e.getSource();
			String widgetName = Text.removeTags(clicked.getName());
			String action = clicked.getActions()[e.getOp() - 1];
			if (debug) System.out.println("clicked " + widgetName + ", option is " + action);
			if (clicked.getActions().length >= e.getOp()) System.out.println(action);
			if (action.equals(ENABLE_LAYOUT)) {
				if (debug) System.out.println("enabling layout on " + widgetName);
				enableLayout(widgetName);
			} else if (action.equals(DISABLE_LAYOUT)) {
				if (debug) System.out.println("disable layout on " + widgetName);
				disableLayout(widgetName);
			} else {
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
		configManager.unsetConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName);
		if (tabInterface.getActiveTab() != null && bankTagName.equals(tabInterface.getActiveTab().getTag())) {
		    bankSearch.layoutBank();
		}
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
		TagTab activeTab = tabInterface.getActiveTab();
		String bankTagName;
		if (activeTab == null) {
			final String input = client.getVar(VarClientStr.INPUT_TEXT);
			if (input.startsWith(TAG_SEARCH)) {
				bankTagName = input.substring(TAG_SEARCH.length());
			} else {
				return;
			}
		} else {
			bankTagName = activeTab.getTag();
		}

		if (debug) System.out.println("applyCustomBankTagItemPositions");

		// TODO can I use "icon_" instead of "item_"?
		boolean bankTagExists = configManager.getConfigurationKeys("banktags" + "." + "item_").stream()
				.filter(key -> {
					List<String> tags = Arrays.asList(configManager.getConfiguration("banktags", key.substring("banktags.".length())).split(","));
					return tags.contains(bankTagName);
				})
				.findAny().isPresent();
		if (!bankTagExists) {
			System.out.println("bank tag " + bankTagName + " does not exist");
			return;
		}

		indexToWidget.clear();
		Map<Integer, Integer> itemPositionIndexes = getBankOrder(bankTagName);
		if (itemPositionIndexes == null) return;

		cleanItemsNotInBankTag(itemPositionIndexes, bankTagName);
		cleanDuplicateIndexes(itemPositionIndexes);

		assignVariantItemPositions(itemPositionIndexes);

		for (Widget bankItem : client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER).getDynamicChildren()) {
			if (bankItem.isHidden()) continue;

			int itemId = bankItem.getItemId();
			if (itemId < 0) {
				continue; // idk what an item id < 0 means but I'm not interested in it.
			}

			int nonPlaceholderId = getNonPlaceholderId(itemId);

			if (!itemShouldBeTreatedAsHavingVariants(nonPlaceholderId)) {
				if (debug) System.out.println("\tassigning position for " + itemName(itemId) + ": ");
				int indexForItem = getIndexForItem(itemId, itemPositionIndexes);
				if (indexForItem != -1) {
					if (debug) System.out.println("\t\texisting position: " + indexForItem);
					indexToWidget.put(indexForItem, bankItem);
				} else {
					int newIndex = assignPosition(itemPositionIndexes, itemId);
					itemPositionIndexes.put(itemId, newIndex);
					if (debug) System.out.println("\t\tnew position: " + newIndex);
					indexToWidget.put(newIndex, bankItem);
				}
			}

			bankItem.setOnDragCompleteListener((JavaScriptCallback) (ev) -> {
				customBankTagOrderInsert(bankTagName, ev.getSource());
			});
		}
		if (debug) System.out.println("tag items: " + itemPositionIndexes.toString());

		setItemPositions(indexToWidget);
		configManager.setConfiguration(CONFIG_GROUP, LAYOUT_CONFIG_KEY_PREFIX + bankTagName, bankTagOrderMapToString(itemPositionIndexes));
		if (debug) System.out.println("saved tag " + bankTagName);
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
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean findTag(int itemId, String search) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method method = TagManager.class.getDeclaredMethod("findTag", int.class, String.class);
		method.setAccessible(true);
		return (boolean) method.invoke(tagManager, itemId, search);
	}

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
		if (debug) System.out.println("variant items in bank: " + variantItemsInBank);

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
				if (debug) System.out.println("item " + itemName(itemId) + " assigned on pass 1 to index " + index);
				indexToWidget.put(index, widget);
				iter.remove();
			}

			// If there was no perfect match, put the items in any unoccupied slot.
			iter = notYetPositionedWidgets.iterator();
			while (iter.hasNext()) {
				Widget widget = iter.next();
				int itemId = switchPlaceholderId(widget.getItemId());

				List<Integer> itemIds = variantItemsInLayout.get(variationBaseId);
				if (itemIds == null) continue; // TODO do I need this line?
				for (Integer id : itemIds) {
					int index = itemPositionIndexes.get(id);
					if (!indexToWidget.containsKey(index)) {
						if (debug) System.out.println("item " + itemName(itemId) + " assigned on pass 3 to index " + index);
						indexToWidget.put(index, widget);
						iter.remove();
						break;
					}
				}
			}

			if (!notYetPositionedWidgets.isEmpty()) {
				for (Widget notYetPositionedWidget : notYetPositionedWidgets) {
					int itemId = notYetPositionedWidget.getItemId();
					int index = assignPosition(itemPositionIndexes, itemId);
					itemPositionIndexes.put(itemId, index);
					if (debug) System.out.println("\t\tnew position: " + index);
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

			child.setHidden(true);
			child.revalidate();
		}


		for (Map.Entry<Integer, Widget> entry : indexToWidget.entrySet()) {
			Widget widget = entry.getValue();
			int index = entry.getKey();

			widget.setOriginalX((index % 8) * 48 + 51);
			widget.setOriginalY((index / 8) * 36);
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

		int row = (point.getY() + container.getScrollY()) / 36;
		int col = (point.getX() - 51) / 48;
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

	private static int assignPosition(Map<Integer, Integer> itemPositionIndexes, int itemId) {
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
		if (debug) System.out.println("\t\t!!! assigning new position for item " + itemId + " to " + lastIndex);
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
