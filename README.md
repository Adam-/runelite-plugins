# Raid Data Tracker

### Can be installed through the Runelite Pluginhub, under the name `Raid Tracker`

Logs COX data, like purple splits and total points, and can be viewed in a new side panel. Purple splits can be changed aswell as of v1.2.

## Images
![UI Part 1](https://i.imgur.com/NCSNGbw.png)
![UI Part 2](https://i.imgur.com/l1wmKuO.png)
![Config](https://i.imgur.com/e2aRLQh.png)


## Data Storage
Data is stored at `~/.runelite/raid-data-tracker/cox/raid_tracker_data.log` where `USERNAME` is your account's login name (or email address).

## Changelog: 
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

