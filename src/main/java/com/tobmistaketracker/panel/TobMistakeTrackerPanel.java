package com.tobmistaketracker.panel;

import com.tobmistaketracker.MistakeManager;
import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerConfig;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class TobMistakeTrackerPanel extends PluginPanel {

    private static final String HTML_LABEL_TEMPLATE =
            "<html><body style='color:%s'>%s<span style='color:white'>%s</span></body></html>";

    private final TobMistakeTrackerConfig config;
    private final MistakeManager mistakeManager;


    // Panel for overall mistake data
    private final JPanel overallPanel = new JPanel();
    private final JLabel overallPlayersLabel = new JLabel();
    private final JLabel overallMistakesLabel = new JLabel();
    private final JLabel overallIcon = new JLabel();

    // Panel for all PlayerMistakesBoxes
    private final JPanel mistakesContainer = new JPanel();

    // Keep track of all boxes
    private final List<PlayerMistakesBox> playerMistakesBoxes = new ArrayList<>();

    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    @Inject
    public TobMistakeTrackerPanel(TobMistakeTrackerConfig config, MistakeManager mistakeManager) {
        this.config = config;
        this.mistakeManager = mistakeManager;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Add the outer panel
        final JPanel layoutPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
        layoutPanel.setLayout(boxLayout);
        add(layoutPanel, BorderLayout.NORTH);

        // Create panel that will contain overall data (at the top)
        overallPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(5, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        overallPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        overallPanel.setLayout(new BorderLayout());

        // Add icon and contents to overallPanel
        final JPanel overallInfo = new JPanel();
        overallInfo.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        overallInfo.setLayout(new GridLayout(2, 1));
        overallInfo.setBorder(new EmptyBorder(2, 10, 2, 0));
        overallPlayersLabel.setFont(FontManager.getRunescapeSmallFont());
        overallMistakesLabel.setFont(FontManager.getRunescapeSmallFont());
        overallInfo.add(overallPlayersLabel);
        overallInfo.add(overallMistakesLabel);
        overallPanel.add(overallIcon, BorderLayout.WEST);
        overallPanel.add(overallInfo, BorderLayout.CENTER);

        // Create reset all menu
        final JMenuItem reset = new JMenuItem("Reset All");
        reset.addActionListener(e ->
        {
            final int result = JOptionPane.showOptionDialog(overallPanel,
                    "This will permanently delete the current mistakes from the client.",
                    "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, new String[]{"Yes", "No"}, "No");

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            resetAll();
        });

        // Create popup menu
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
        popupMenu.add(reset);
        overallPanel.setComponentPopupMenu(popupMenu);

        // Create the mistakes panel which shows all mistakes for all players
        mistakesContainer.setLayout(new BoxLayout(mistakesContainer, BoxLayout.Y_AXIS));

        // Add all our panels in the order we want them to appear
        layoutPanel.add(overallPanel);
        layoutPanel.add(mistakesContainer);

        // Create testing button
        if (config.isDebug()) {
            JButton testButton = new JButton("Test Mistakes");
            testButton.addActionListener(e ->
            {
                for (TobMistake mistake : TobMistake.values()) {
                    addMistakeForPlayer("Test Player", mistake);
                }
            });
            layoutPanel.add(testButton);
        }

        // Add the error for when there are no mistakes yet
        errorPanel.setContent("Tob Mistake Tracker", "You have not tracked any mistakes yet.");
        add(errorPanel);
    }

    public void loadHeaderIcon(BufferedImage img) {
        overallIcon.setIcon(new ImageIcon(img));
    }

    /**
     * Adds a mistake for the specified player, both in the manager and the panel.
     *
     * @param playerName - The player name that a mistake was added for
     */
    public void addMistakeForPlayer(String playerName, TobMistake mistake) {
        // TODO: Instead of repainting everything, just update what's needed.
        mistakeManager.addMistakeForPlayer(playerName, mistake);

        PlayerMistakesBox box = buildBox(playerName, mistake);
        box.rebuildAllMistakes();
        updateOverallPanel();
    }

    private PlayerMistakesBox buildBox(String playerName, TobMistake mistake) {
        for (PlayerMistakesBox box : playerMistakesBoxes) {
            if (box.getPlayerName().equals(playerName)) {
                // TODO: Ordering? -- Localplayer always first, then everyone else in order of most recent?
//                box.addMistake(mistake);
                return box;
            }
        }

        // Create a new box if one could not be found
        PlayerMistakesBox box = new PlayerMistakesBox(mistakeManager, playerName);
//        box.addMistake(mistake);

        // Use the existing popup menu or create a new one
        JPopupMenu popupMenu = box.getComponentPopupMenu();
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
            box.setComponentPopupMenu(popupMenu);
        }

        // Create reset menu
        final JMenuItem reset = new JMenuItem("Reset Mistakes for " + playerName);
        reset.addActionListener(e -> {
            mistakeManager.removeMistakesForPlayer(playerName);
            playerMistakesBoxes.remove(box);

            updateOverallPanel();
            mistakesContainer.remove(box);
            mistakesContainer.repaint();

            if (playerMistakesBoxes.isEmpty()) {
                add(errorPanel);
                overallPanel.setVisible(false);
                mistakesContainer.setVisible(false);
            }
        });

        popupMenu.add(reset);

        // Show main view
        remove(errorPanel);
        overallPanel.setVisible(true);
        mistakesContainer.setVisible(true);

        // Add box to panel
        playerMistakesBoxes.add(box);
        // TODO: Ordering? -- Localplayer always first, then everyone else in order of most recent?
        mistakesContainer.add(box);

        return box;
    }

    private void resetAll() {
        mistakeManager.clearAllMistakes();
        playerMistakesBoxes.clear();

        updateOverallPanel();
        mistakesContainer.removeAll();
        mistakesContainer.repaint();

        add(errorPanel);
        overallPanel.setVisible(false);
        mistakesContainer.setVisible(false);
    }

    private void updateOverallPanel() {
        overallPlayersLabel.setText(htmlLabel("Total players: ", mistakeManager.getPlayersWithMistakes().size()));
        overallMistakesLabel.setText(htmlLabel("Total mistakes: ", mistakeManager.getTotalMistakesForAllPlayers()));
    }

    private static String htmlLabel(String key, long value) {
        final String valueStr = QuantityFormatter.quantityToStackSize(value);
        return String.format(HTML_LABEL_TEMPLATE, ColorUtil.toHexColor(ColorScheme.LIGHT_GRAY_COLOR), key, valueStr);
    }

    /**
     * Removes a mistake for the specified player, both in the manager and the panel. This is only callable from
     * the box's mistake's Reset action.
     *
     * @param playerName - The player name that a mistake was added for
     */
    // TODO: This is currently unused as I'm not sure I even want this feature.
    private void removeMistakeForPlayer(String playerName, TobMistake mistake) {
        mistakeManager.removeMistakeForPlayer(playerName, mistake);

        for (PlayerMistakesBox box : playerMistakesBoxes) {
            if (box.getPlayerName().equals(playerName)) {
//                box.removeMistake(mistake);
                box.rebuildAllMistakes();
                updateOverallPanel();
                return;
            }
        }
    }
}
