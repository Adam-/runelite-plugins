package com.tobmistaketracker.panel;

import com.tobmistaketracker.MistakeManager;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PlayerMistakesBox extends JPanel {

    private static final int ITEMS_PER_ROW = 5;

    private final MistakeManager mistakeManager;

    @NonNull
    @Getter
    private final String playerName;
    private int totalMistakes;

    private final JPanel mistakesContainer = new JPanel();
    private final JLabel raidCountLabel;
    private final JLabel mistakeCountLabel;

    protected PlayerMistakesBox(MistakeManager mistakeManager, @NonNull String playerName) {
        this.mistakeManager = mistakeManager;
        this.playerName = playerName;

        setLayout(new BorderLayout(0, 1));
        setBorder(new EmptyBorder(5, 0, 0, 0));

        final JPanel playerMistakesTitle = new JPanel(new BorderLayout(5, 0));
        playerMistakesTitle.setBorder(new EmptyBorder(7, 7, 7, 7));
        playerMistakesTitle.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        final JLabel playerNameLabel = new JLabel(playerName); // e.g. Questing Pet
        playerNameLabel.setFont(FontManager.getRunescapeSmallFont());
        playerNameLabel.setForeground(Color.WHITE);
        playerMistakesTitle.add(playerNameLabel, BorderLayout.WEST);

        raidCountLabel = new JLabel(); // TODO: e.g. x 53 Raids
        raidCountLabel.setFont(FontManager.getRunescapeSmallFont());
        raidCountLabel.setForeground(Color.LIGHT_GRAY);
        playerMistakesTitle.add(raidCountLabel, BorderLayout.CENTER);

        mistakeCountLabel = new JLabel(); // e.g. 78 Mistakes
        mistakeCountLabel.setFont(FontManager.getRunescapeSmallFont());
        mistakeCountLabel.setForeground(Color.LIGHT_GRAY);
        playerMistakesTitle.add(mistakeCountLabel, BorderLayout.EAST);

        add(playerMistakesTitle, BorderLayout.NORTH);
        add(mistakesContainer, BorderLayout.CENTER);

        // Create popup menu for resetting the player's mistakes
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        setComponentPopupMenu(popupMenu);
    }

    void rebuildAllMistakes() {
        // TODO: Instead of always repainting everything, just update what's needed.
        buildMistakes();
        raidCountLabel.setText(""); // TODO: e.g. x 53 raids (with mistakes?)
        mistakeCountLabel.setText(totalMistakes + " Mistakes");

        validate();
        repaint();
    }

    private void buildMistakes() {
        totalMistakes = 0;
        setVisible(true);

        List<TobMistakeCount> mistakeCountsForPlayer = new ArrayList<>();
        for (TobMistake mistake : TobMistake.values()) {
            int mistakeCount = mistakeManager.getMistakeCountForPlayer(playerName, mistake);
            if (mistakeCount > 0) {
                totalMistakes += mistakeCount;
                mistakeCountsForPlayer.add(new TobMistakeCount(mistake, mistakeCount));
            }
        }

        int numRows = calculateNumRows(mistakeCountsForPlayer.size());

        mistakesContainer.removeAll();
        mistakesContainer.setLayout(new GridLayout(numRows, ITEMS_PER_ROW, 1, 1));

        for (int i = 0; i < numRows * ITEMS_PER_ROW; i++) {
            final JPanel mistakeContainer = new JPanel();
            mistakeContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

            // If we still have mistakes to show for this row
            if (i < mistakeCountsForPlayer.size()) {
                TobMistakeCount mistakeCount = mistakeCountsForPlayer.get(i);
                final JLabel imageLabel = new JLabel();
                imageLabel.setToolTipText(mistakeCount.getTooltipText());
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

                BufferedImage mistakeImage = mistakeCount.getMistake().getMistakeImage();
                imageLabel.setIcon(new ImageIcon(mistakeImage));
                mistakeContainer.add(imageLabel);
            }
            mistakesContainer.add(mistakeContainer);
        }

        mistakesContainer.repaint();
    }

    private static int calculateNumRows(int numItems) {
        return (int) Math.ceil((double) numItems / ITEMS_PER_ROW);
    }

    @Value
    private static class TobMistakeCount {

        TobMistake mistake;
        int count;
        String tooltipText;

        TobMistakeCount(TobMistake mistake, int count) {
            this.mistake = mistake;
            this.count = count;
            this.tooltipText = String.format("%s x %s", mistake.getMistakeName(), count);
        }
    }
}
