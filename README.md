# Bank tag layouts
Allows you to layout your bank tabs.
Right click a tag tab, click "Enable layout", then drag items anywhere while the tab is selected.

![alt text](https://github.com/geheur/bank-tag-custom-layouts/blob/master/example.png?raw=true)

## Changelog
### v1.1
* Adds importing/exporting of layout-ed tag tabs.
* Adds layout placeholders to indicate when an item is in the layout but not in your bank. You can right click these to remove them from the layout (although you can't currently drag them - would like to add that in the future). Useful for importing layouts from people who have different gear than you, and for items that don't leave normal placeholders, such as clue scrolls.
* Adds "Enable layout by default" config option. You can still disable the layout for individual tabs even when this option is enabled.
* Prints tutorial message when you drag an item on a tab and you don't have any layout-ed tag tabs yet.
* Prints warning message when you might have unintentionally reordered your actual bank instead of a layout.
* Workaround for bug where the Bank Tags plugin's "Remove tab separators" option would rearrange items *after* Bank Tag Layouts' laid out the layout, causing any gaps in the layout to be collapsed.
* Fixed bug where dragging an item above the bank caused it to disappear. Oops.