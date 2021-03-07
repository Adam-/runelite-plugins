# Raid Data Tracker

### Can be installed through the Runelite Pluginhub, under the name `Raid Tracker`

Logs COX and TOB data, like purple splits, total points (in COX), or the amount of MVP's (in TOB). The data can be viewed in a side panel. Purple splits can be changed as of v1.2.

## Images
<div class="row">
  <img align="Top" src="https://i.imgur.com/NCSNGbw.png">
  <img align="Top" src="https://i.imgur.com/vwNVqe4.png">
  <img align="Top" src="https://i.imgur.com/SKVqNAy.png">
  <img align="Top" src="https://i.imgur.com/vwNVqe4.png">
  <img align="Top" src="https://i.imgur.com/LPOsdaM.png">
</div>

## Data Storage
Data is stored at `~/.runelite/raid-data-tracker/USERNAME/RAID/raid_tracker_data.log` where `USERNAME` is your account's login name (or email address), and `RAID` is either cox or tob.

## Bugs & Problems
If you find any bugs or problems, feel free to add me on discord, and I'll accept you asap: `baniraai#0996`

## Changelog:

### v1.4.2
- Added dey0 time tracking to the raid_tracker_data.log file, toggleable in the settings. Adding these times to the ui are planned for the next version.
- Increased the amount of date filters in the filter panel: added a filter for 12 hours, 3 days and 3 months.
- Deleted verification-metadata as it was not necessary and caused the plugin not to build as of v1.7.0 runelite.
- Fixed raid times > 1 hour not counting the extra hour.
- Fixed a bug where the filters > 7 people wouldn't work; all the kills were shown instead.
- Fixed a bug where times wouldn't be tracked if precise timing was enabled. The times are now rounded in the tracker instead.

### v1.4.1
- Fixed a bug where the regular drops always returned an empty list, effectively removing the regular drops from the ui.
- Fixed a bug in the Split Changer panel, where the plugin would crash if a team size was outside of the boundaries (1 - 100). Now if the team size is outside of the boundaries, a 1 will be used if it's below the lower limit, and a 100 if it's above the upper limit.
- Changed the plugin-hub name to Raid Data Tracker so that the name is more descriptive.

### v1.4
- Added the option to disable any panel within the ui, if desired.
- Added the option to filter by team size.
- Added a time splits panel where the best splits for each boss/level completion can be seen
- Added an icon for the plugin hub.
- Added a maximum for the amount of uniques shown in the Change Purple Splits Panel.
- Changed the way the Today filter works. Now it tracks everything from the past 24 hours, rather than every drop on that day after 12 PM.
- Fixed a bug where all uniques were set to FFA, even though "default FFA" wasn't checked.
- Fixed a bug where duplicate dust and kit recipients were tracked wrongly (this was because duplicate dusts and kits are split by a comma, rather than by a new line).
- Fixed a bug where a unique wouldn't show in the uniques table even though it is in the log file.
- Fixed a bug where it was possible that loot would write to the log file multiple times
- Fixed a bug where the TOB pet would track with the regular uniques
- Fixed a typo in Metamorphic Dust.
- Clue scrolls now *really* stack, there was a bug which caused the clues to not stack, contrary to the patch notes of v1.3
- Removed any StringEscapeUtils usage from the plugin, and changed the function that used it accordingly. This to avoid a plugin crash for some players.
- Improved ui performance by not pausing the swing thread, but instead updating the panel when the variable is loaded.

### v1.3
- Added Theatre of Blood to the plugin.
- Now tracking the COX and TOB pets.
- Duplicate purples/dusts/kits in one KC are now tracked. Previously only the last of each was tracked.
- Elite clues now stack.
- Fixed a bug where the "Last Week" up until the "Last Year" filters weren't showing anything.
- The plugin now runs with -ea.


### v1.2.2
- Fixed a bug with the migrate function, where the wrong RaidTracker list was added to the new file.
- Changed the value formatter to show decimals up until 100m instead of up until 10m.
 
### v1.2.1:
- Fixed a bug where, if you switched from one account to the other, the data would overwrite the last account's.
- Sorted the Change Purple list to be in chronological order
- Fixed a bug for players that have a space in their name: The space would show as "Â ", now correctly shows as " "

### v1.2:
- Added the option to filter kills shown in the UI, based on time or a total number of kills. It is also possible with the filter to only show CM kills, no CM kills, or to show both.
- Added the option to change purple splits in the UI, rather than having to change the logfile itself. When you get a purple it will show at the bottom of the UI, where you can change the team size, split, and whether it was FFA or not. These values will then update to the log file.
- Bugfix regarding not updating the UI after a kill has been made, due to the reset() function being called too early.
- Now all the values in the UI will show at least zero. Previously, it was possible that it would show `-1 * killcount`, if there were no splits/points earned yet.

### v1.1:
- Added a UI and config.
- Changed the data storage folder from `~/.runelite/loots` to `~/.runelite/raid-data-tracker`. Added a migrate function to move the contents of the existing file in `~/.runelite/loots` to `~/.runelite/raid-data-tracker`.
- Changed the way team size is determined at the end of a raid. The way the team size is determined now is using a varbit for the team size, rather than scraping the team size from the chat. If a team is larger than 10 people, the chat will display the team size like `11-15 players`, which resulted in a wrong number for the team size.

### v1.0
- Added functionality to log the raid data to a file. 

## Planned changes
- Adding room specific best times in Chambers of Xeric. This will only work with dey0's cox timers plugin, and is togglable in the settings.
- Adding the ability to add, and edit kc's through the ui rather than through the raid_tracker_data.log file.