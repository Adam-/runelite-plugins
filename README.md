## Emote Clue Items v3.1.0 [![Plugin Installs](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/emote-clue-items&label=Active%20installs)](https://runelite.net/plugin-hub/Lars%20van%20Soest)

Emote Clue Items is a RuneLite plugin which highlights items required for emote clue steps and provides a user-friendly
item collection log with STASHUnit integration.

### Take back your bank space

Maintaining bank space can be quite cumbersome, especially when you are not sure which items should be kept for future
clue scrolls. With this plugin, throwing away items may be a bit less stressful, as this plugin aims to highlight all
emote clue items.

### 3.1.0 Patch notes
Opening your bank to begin progression logging is no longer a requirement. Instead, the progression loggin will now start
right after a player has logged in. A new disclaimer will inform the user that opening the bank will add its contents
to the collection log.

### New in 3.0.0: STASHUnit integration

With the release of 3.0.0, Emote Clue Items supports STASHUnits. Users are now able to select which STASHUnits are
filled, and are provided with an elaborate overview. The overview provides a plethora of details, such a collection log
of items required to fill the STASHUnit, construction requirements and much more.

#### Filling stashes now updates the emote clue item requirement status

Listed under each emote clue item requirement, the original overview now shows which related stash units are built
and/or filled. With this integration, next to the original inventory item tracking, requirement completion can also be
performed through filling stash units.

![Filling stashes updates status](/readme/filling-stashes-updates-status.gif)

#### Status management and updates

Whats more, STASHUnit fill statuses are stored in your Runelite's config manager for each runescape account that you
have. By logging into your Runelite account on the client, even when you log in on another computer, your fill statuses
are saved. STASHUnit build statuses are automatically updated when building a STASHUnit, and the results are immediately
visible in the overview.

![Status management updates](/readme/status-management-updates.gif)

### Other plugin features

Among other interfaces, items in your bank, inventory and equipment are highlighted by their respective tier colours.
Individually, for each supported in-game interface, overlay display may be switched on or off in the plugin settings.

![Interface item highlighting](/readme/interface-item-highlighting.gif)

#### Dynamic item collection log caching

Whenever the user picks up an item, the item collection log of related emote clue item requirement status are updated,
cached, and displayed.

![Item collection log caching](/readme/item-collection-log-caching.gif)

#### Requirement lookup

The overview panel allows for querying and sorting the data. Moreover, the input header allows for any combination of
requirement name, emoteClueDifficulty and completion status filtering.

![Requirement lookup](/readme/requirement-lookup.gif)

### Planned future updates

- Add buttons to display EmoteClue and STASHUnit locations on the in-game world map.
- Include more types of item requirements
