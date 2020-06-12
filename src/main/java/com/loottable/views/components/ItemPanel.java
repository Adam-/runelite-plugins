package com.loottable.views.components;

import java.awt.GridLayout;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.loottable.helpers.UiUtilities;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class ItemPanel extends JPanel {
    private static final long serialVersionUID = 8426321039456174778L;
    String itemName;
    String quantity;
    String rarity;
    String price;

	public ItemPanel(String itemName, String quantity, String rarity, String price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.rarity = rarity;
        this.price = price;
        setBorder(new EmptyBorder(0, 0, 5, 0));
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(ColorScheme.DARK_GRAY_COLOR);
        container.setBorder(UiUtilities.ITEM_BORDER);
        
        JPanel leftPanel = constructLeftSide();
        JPanel rightPanel = constructRightSide();

        container.add(leftPanel, BorderLayout.WEST);
        container.add(rightPanel, BorderLayout.EAST);

        add(container);
    }

    /**
     * Constructs left side of item panel
     * Item Name
     * Rarity
     */
    private JPanel constructLeftSide() {
        JPanel leftSidePanel = new JPanel(new GridLayout(2, 1));
        leftSidePanel.setBorder(new EmptyBorder(2, 2, 2, 2));

        JLabel itemNameLabel = new JLabel(itemName);
        itemNameLabel.setBorder(new EmptyBorder(0, 0, 3, 0));
        itemNameLabel.setFont(FontManager.getRunescapeBoldFont());
        itemNameLabel.setHorizontalAlignment(JLabel.LEFT);
        itemNameLabel.setVerticalAlignment(JLabel.CENTER);

        JLabel rarityLabel = new JLabel(rarity);
        rarityLabel.setHorizontalAlignment(JLabel.LEFT);
        rarityLabel.setVerticalAlignment(JLabel.CENTER);

        leftSidePanel.add(itemNameLabel);
        leftSidePanel.add(rarityLabel);

        return leftSidePanel;
    }


    /**
     * Constructs right side of item panel
     * quantity
     * Price
     */
    private JPanel constructRightSide() {
        JPanel rightSidePanel = new JPanel(new GridLayout(2, 1));
        rightSidePanel.setBorder(new EmptyBorder(2, 2, 2, 2));

        JLabel quantityLabel = new JLabel("x" + quantity);
        quantityLabel.setBorder(new EmptyBorder(0, 0, 3, 0));
        quantityLabel.setHorizontalAlignment(JLabel.RIGHT);
        quantityLabel.setVerticalAlignment(JLabel.CENTER);

        JLabel priceLabel = price == "Not sold" ? new JLabel(price) : new JLabel(price + "gp");
        priceLabel.setForeground(ColorScheme.GRAND_EXCHANGE_ALCH);
        priceLabel.setVerticalAlignment(JLabel.CENTER);

        rightSidePanel.add(quantityLabel);
        rightSidePanel.add(priceLabel);

        return rightSidePanel;
    }
}
