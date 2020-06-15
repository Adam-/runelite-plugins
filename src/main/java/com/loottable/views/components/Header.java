package com.loottable.views.components;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import net.runelite.client.input.KeyListener;
import net.runelite.client.ui.ColorScheme;

public class Header extends JPanel {
    private static final long serialVersionUID = -5426065729242203114L;
    
    private ActionListener onSearchButtonPressed;
    private Consumer<String> onSearchBarTextChanged;

	public Header(
        String monsterName, 
        ActionListener onSearchButtonPressed, 
        Consumer<String> onSearchBarTextChanged
    ) {
        this.onSearchButtonPressed = onSearchButtonPressed;
        this.onSearchBarTextChanged = onSearchBarTextChanged;
        constructSearchBar(monsterName);
        constructBorder();
    }

    private void constructBorder() {
        Border border = BorderFactory.createMatteBorder(0, 0, 3, 0, ColorScheme.BRAND_ORANGE);
        setBorder(border);
    }

    private void constructSearchBar(String monsterName) {
        JPanel searchBarContainer = new JPanel();
        searchBarContainer.setLayout(new BoxLayout(searchBarContainer, BoxLayout.X_AXIS));

        JButton searchButton = new JButton("Search");
        JTextField searchInput = new JTextField(14);
        searchInput.setText(monsterName);
        searchButton.addActionListener(onSearchButtonPressed);
        searchInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {}
        
            @Override
            public void keyReleased(KeyEvent event) {
                JTextField textField = (JTextField) event.getSource();
                String text = textField.getText();
                onSearchBarTextChanged.accept(text);
            }
        
            @Override
            public void keyPressed(KeyEvent event) {}
        });

        searchBarContainer.add(searchInput);
        searchBarContainer.add(searchButton);
        add(searchBarContainer);
    }
}