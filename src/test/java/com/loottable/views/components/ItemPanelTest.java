package com.loottable.views.components;

import com.loottable.views.ViewSetup;

public class ItemPanelTest {
    public static void main(String[] args) {
        ItemPanel itemPanel = new ItemPanel("Rune Scimitar", "1", "1/128", "12,000");
        ViewSetup.launch(itemPanel);
    }
}