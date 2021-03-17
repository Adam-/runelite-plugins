# Bank tag layouts
Allows you to layout your bank tabs.
Right click a tag tab, click "Enable layout", then drag items anywhere while the tab is selected.

![alt text](https://github.com/geheur/bank-tag-custom-layouts/blob/master/example.png?raw=true)

## Changelog
### v1.1
* Added importing/exporting of layout-ed tag tabs.
* Added layout placeholders to indicate when an item is in the layout but not in your bank. You can right click these to remove them from the layout (although you can't currently drag them - would like to add that in the future). Useful for importing layouts from people who have different gear than you, and for items that don't leave normal placeholders, such as clue scrolls.
* Added checkbox button in bank interface to enable/disable layout, since it can be hard to tell if layout is enabled if you use the Bank Tags plugin's "Remove tag separators" option. Can be disabled.
* Added "Enable layout by default" config option. You can still disable the layout for individual tabs even when this option is enabled.
* Print warning message when you drag an item on a tab that doesn't have layout enabled.
* Workaround for bug where the Bank Tags plugin's "Remove tab separators" option would rearrange items *after* Bank Tag Layouts' laid out the layout, causing any gaps in the layout to be collapsed.