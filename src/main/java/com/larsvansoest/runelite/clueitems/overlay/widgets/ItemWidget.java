package com.larsvansoest.runelite.clueitems.overlay.widgets;

public enum ItemWidget
{
	BANK(12, 12),

	BANK_EQUIPMENT(12, 68),

	BANK_EQUIPMENT_INVENTORY(15, 4),

	BANK_INVENTORY(15, 3),

	EQUIPMENT(387, 0),

	EQUIPMENT_EQUIPMENT(84, 1),

	EQUIPMENT_INVENTORY(85, 0),

	DEPOSIT_BOX(192, 2),

	GUIDE_PRICES(464, 2),

	GUIDE_PRICES_INVENTORY(238, 0),

	INVENTORY(149, 0),

	KEPT_ON_DEATH(4, 5),

	SHOP(300, 16),

	SHOP_INVENTORY(301, 0);

	public final int id;

	/**
	 * group id of the widget, displayed in RuneLites widget inspector as groupId.childId;
	 */
	public final int groupId;

	/**
	 * child id of the widget, displayed in RuneLites widget inspector as groupId.childId;
	 */
	public final int childId;

	ItemWidget(int groupId, int childId)
	{
		this.id = groupId << 16 | childId;
		this.groupId = groupId;
		this.childId = childId;
	}
}
