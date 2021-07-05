package com.tobmistaketracker.panel;

import com.tobmistaketracker.TobMistake;
import com.tobmistaketracker.TobMistakeTrackerPlugin;
import com.tobmistaketracker.state.MistakeStateManager;
import com.tobmistaketracker.state.MistakeStateReader;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.SwingUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class TobMistakeTrackerPanel extends PluginPanel {

    private static final String HTML_LABEL_TEMPLATE =
            "<html><body style='color:%s'>%s<span style='color:white'>%s</span></body></html>";

    private static final boolean DEFAULT_IS_RAID_DEATHS = true;

    private static final ImageIcon RAID_DEATHS_ICON;
    private static final ImageIcon RAID_DEATHS_ICON_FADED;
    private static final ImageIcon RAID_DEATHS_ICON_HOVER;

    private static final ImageIcon ROOM_DEATHS_ICON;
    private static final ImageIcon ROOM_DEATHS_ICON_FADED;
    private static final ImageIcon ROOM_DEATHS_ICON_HOVER;

    private final Client client;

    private final MistakeStateManager mistakeStateManager;

    private final JLabel currentViewTitle = new JLabel();
    private final JButton switchMistakesViewBtn = new JButton();

    private final JRadioButton raidDeathsBtn = new JRadioButton();
    private final JRadioButton roomDeathsBtn = new JRadioButton();

    // Panel for overall mistake data
    private final JPanel overallPanel = new JPanel();
    private final JLabel overallPlayersLabel = new JLabel();
    private final JLabel overallMistakesLabel = new JLabel();
    private final JLabel overallRaidsLabel = new JLabel();
    private final JLabel overallIcon = new JLabel();

    // Panel for all PlayerMistakesBoxes
    private final JPanel mistakesContainer = new JPanel();

    // Keep track of all boxes
    private final List<PlayerMistakesBox> playerMistakesBoxes = new ArrayList<>();

    // Keep track of the current death grouping
    private boolean isRaidDeaths;
    // Keep track of the current view we're showing
    private boolean isShowingAll = false;

    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    static {
        final BufferedImage raidDeathsImg = ImageUtil.loadImageResource(TobMistakeTrackerPlugin.class, "raid_deaths.png");
        final BufferedImage roomDeathsImg = ImageUtil.loadImageResource(TobMistakeTrackerPlugin.class, "room_deaths.png");

        RAID_DEATHS_ICON = new ImageIcon(raidDeathsImg);
        RAID_DEATHS_ICON_FADED = new ImageIcon(ImageUtil.alphaOffset(raidDeathsImg, -180));
        RAID_DEATHS_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(raidDeathsImg, -220));

        ROOM_DEATHS_ICON = new ImageIcon(roomDeathsImg);
        ROOM_DEATHS_ICON_FADED = new ImageIcon(ImageUtil.alphaOffset(roomDeathsImg, -180));
        ROOM_DEATHS_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(roomDeathsImg, -220));
    }

    @Inject
    public TobMistakeTrackerPanel(Client client, MistakeStateReader mistakeStateReader,
                                  @Named("developerMode") boolean developerMode) {
        this.client = client;
        this.mistakeStateManager = mistakeStateReader.read();

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Add the outer panel for wrapping everything else inside
        final JPanel layoutPanel = new JPanel();
        layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
        add(layoutPanel, BorderLayout.NORTH);

        // Create panel for the header (contains things like view, action buttons, etc.)
        JPanel headerContainer = new JPanel(new GridLayout(2, 1, 0, 0));
        headerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Create the view container
        final JPanel viewContainer = new JPanel(new BorderLayout());
        viewContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        viewContainer.setPreferredSize(new Dimension(0, 30));
        viewContainer.setBorder(new EmptyBorder(5, 5, 5, 10));

        // Create the container for the view title
        final JPanel leftTitleContainer = new JPanel(new BorderLayout(5, 0));
        leftTitleContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Create the current view title
        currentViewTitle.setForeground(Color.WHITE);
        currentViewTitle.setText(getCurrentViewTitleText());
        leftTitleContainer.add(currentViewTitle, BorderLayout.WEST);

        // Create the switch view button
        SwingUtil.removeButtonDecorations(switchMistakesViewBtn);
        switchMistakesViewBtn.setText(getSwitchMistakesViewButtonText());
        switchMistakesViewBtn.setBackground(Color.WHITE);
        switchMistakesViewBtn.setBorder(new EmptyBorder(10, 10, 10, 10));
        switchMistakesViewBtn.setBorderPainted(true);
        switchMistakesViewBtn.setPreferredSize(new Dimension(100, 10));

        // Add the listeners to the button
        switchMistakesViewBtn.addActionListener(e -> switchMistakesView());
        switchMistakesViewBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                switchMistakesViewBtn.setForeground(Color.LIGHT_GRAY);
                switchMistakesViewBtn.setBackground(Color.LIGHT_GRAY);
            }

            public void mouseExited(MouseEvent e) {
                switchMistakesViewBtn.setForeground(Color.WHITE);
                switchMistakesViewBtn.setBackground(Color.WHITE);
            }
        });

        // Add the view container to header
        viewContainer.add(leftTitleContainer, BorderLayout.WEST);
        viewContainer.add(switchMistakesViewBtn, BorderLayout.EAST);
        headerContainer.add(viewContainer);

        // Create the panel for the action buttons
        final JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionButtons.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        actionButtons.setPreferredSize(new Dimension(0, 30));
        actionButtons.setBorder(new EmptyBorder(5, 5, 5, 10));

        // Create the raid deaths button
        SwingUtil.removeButtonDecorations(raidDeathsBtn);
        raidDeathsBtn.setIcon(RAID_DEATHS_ICON_FADED);
        raidDeathsBtn.setRolloverIcon(RAID_DEATHS_ICON_HOVER);
        raidDeathsBtn.setSelectedIcon(RAID_DEATHS_ICON);
        raidDeathsBtn.setToolTipText("Group all raid deaths into a singular mistake");
        raidDeathsBtn.addActionListener(e -> changeDeathGrouping(true));

        // Create the room deaths button
        SwingUtil.removeButtonDecorations(roomDeathsBtn);
        roomDeathsBtn.setIcon(ROOM_DEATHS_ICON_FADED);
        roomDeathsBtn.setRolloverIcon(ROOM_DEATHS_ICON_HOVER);
        roomDeathsBtn.setSelectedIcon(ROOM_DEATHS_ICON);
        roomDeathsBtn.setToolTipText("Show each room's deaths as their own mistakes");
        roomDeathsBtn.addActionListener(e -> changeDeathGrouping(false));

        ButtonGroup raidRoomGroup = new ButtonGroup();
        raidRoomGroup.add(raidDeathsBtn);
        raidRoomGroup.add(roomDeathsBtn);

        // Add all action buttons to the header
        actionButtons.add(raidDeathsBtn);
        actionButtons.add(roomDeathsBtn);
        headerContainer.add(actionButtons);

        changeDeathGrouping(DEFAULT_IS_RAID_DEATHS);
        headerContainer.setVisible(true);

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
        overallInfo.setLayout(new GridLayout(3, 1));
        overallInfo.setBorder(new EmptyBorder(2, 10, 2, 0));
        overallPlayersLabel.setFont(FontManager.getRunescapeSmallFont());
        overallMistakesLabel.setFont(FontManager.getRunescapeSmallFont());
        overallRaidsLabel.setFont(FontManager.getRunescapeSmallFont());
        overallInfo.add(overallPlayersLabel);
        overallInfo.add(overallMistakesLabel);
        overallInfo.add(overallRaidsLabel);
        overallPanel.add(overallIcon, BorderLayout.WEST);
        overallPanel.add(overallInfo, BorderLayout.CENTER);

        // Create reset all menu
        final JMenuItem reset = new JMenuItem("Reset All");
        reset.addActionListener(e ->
        {
            final int result = JOptionPane.showOptionDialog(overallPanel,
                    "This will permanently delete ALL mistakes across ALL raids from the client.",
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
        layoutPanel.add(headerContainer);
        layoutPanel.add(overallPanel);
        layoutPanel.add(mistakesContainer);

        // Create testing button
        if (developerMode) {
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
        updateVisiblePanels(true);
    }

    /**
     * The plugin has been reloaded, reload the panel
     */
    public void reload() {
        rebuildAll();
    }

    public void loadHeaderIcon(BufferedImage img) {
        overallIcon.setIcon(new ImageIcon(img));
    }

    /**
     * Resets the current raid mistakes and panel, and notifies the manager that a new raid has been entered
     */
    public void newRaid(Set<String> playerNames) {
        mistakeStateManager.newRaid(playerNames);
        if (isShowingAll) {
            // Only the overall panel can change between raids
            updateOverallPanel();
        } else {
            // We're looking at the current raid view
            resetUi();
        }
    }

    /**
     * Adds a mistake for the specified player, both in the manager and the panel.
     *
     * @param playerName - The player name that a mistake was added for
     */
    public void addMistakeForPlayer(String playerName, TobMistake mistake) {
        mistakeStateManager.addMistakeForPlayer(playerName, mistake);

        PlayerMistakesBox box = buildBox(playerName);
        box.rebuildAllMistakes(isRaidDeaths);
        updateOverallPanel();
    }

    /**
     * Rebuilds all the boxes from scratch based on which view we're currently looking at
     */
    private void rebuildAll() {
        if (SwingUtilities.isEventDispatchThread()) {
            SwingUtil.fastRemoveAll(mistakesContainer);
        } else {
            mistakesContainer.removeAll();
        }
        playerMistakesBoxes.clear();

        for (String playerName : mistakeStateManager.getPlayersWithMistakes()) {
            buildBox(playerName);
        }

        playerMistakesBoxes.forEach(box -> box.rebuildAllMistakes(isRaidDeaths));
        updateOverallPanel();
        mistakesContainer.revalidate();
        mistakesContainer.repaint();

        updateVisiblePanels(playerMistakesBoxes.isEmpty());
    }

    private PlayerMistakesBox buildBox(String playerName) {
        for (PlayerMistakesBox box : playerMistakesBoxes) {
            if (box.getPlayerName().equals(playerName)) {
                if (client.getLocalPlayer() != null && playerName.equals(client.getLocalPlayer().getName())) {
                    // This existing box is for me, make sure it goes first if it somehow isn't already
                    mistakesContainer.setComponentZOrder(box, 0);
                } else if (doesLocalPlayerHaveMistakesBox()) {
                    // I already have some mistakes, so this should go right after
                    mistakesContainer.setComponentZOrder(box, 1);
                } else {
                    // It's not for me, and I have no mistakes, it can go in the front
                    mistakesContainer.setComponentZOrder(box, 0);
                }
                return box;
            }
        }

        // Create a new box if one could not be found
        PlayerMistakesBox box = new PlayerMistakesBox(mistakeStateManager, playerName);

        // Use the existing popup menu or create a new one
        JPopupMenu popupMenu = box.getComponentPopupMenu();
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
            box.setComponentPopupMenu(popupMenu);
        }

        // Create reset menu
        final JMenuItem reset = new JMenuItem("Reset ALL Mistakes for " + playerName);
        reset.addActionListener(e -> {
            mistakeStateManager.removeAllMistakesForPlayer(playerName);
            playerMistakesBoxes.remove(box);

            updateOverallPanel();
            mistakesContainer.remove(box);
            mistakesContainer.repaint();

            if (playerMistakesBoxes.isEmpty()) {
                updateVisiblePanels(true);
            }
        });

        popupMenu.add(reset);

        // Show main view
        updateVisiblePanels(false);

        // Add box to panel
        if (client.getLocalPlayer() != null && playerName.equals(client.getLocalPlayer().getName())) {
            // This box is for me, put at the front
            mistakesContainer.add(box, 0);
        } else if (doesLocalPlayerHaveMistakesBox()) {
            // I already have some mistakes, so this should go right after
            mistakesContainer.add(box, 1);
        } else {
            // It's not for me, and I have no mistakes, it can go in the front
            mistakesContainer.add(box, 0);
        }
        playerMistakesBoxes.add(box);

        return box;
    }

    private void resetAll() {
        mistakeStateManager.resetAll();
        resetUi();
    }

    private void resetUi() {
        playerMistakesBoxes.clear();

        updateOverallPanel();
        mistakesContainer.removeAll();
        mistakesContainer.repaint();

        updateVisiblePanels(true);
    }

    private void updateOverallPanel() {
        overallPlayersLabel.setText(htmlLabel("Total players: ",
                mistakeStateManager.getPlayersWithMistakes().size()));
        overallMistakesLabel.setText(htmlLabel("Total mistakes: ",
                mistakeStateManager.getTotalMistakeCountForAllPlayers()));
        overallRaidsLabel.setText(htmlLabel("Tracked raids: ",
                mistakeStateManager.getTrackedRaids()));
    }

    private static String htmlLabel(String key, long value) {
        final String valueStr = QuantityFormatter.quantityToStackSize(value);
        return String.format(HTML_LABEL_TEMPLATE, ColorUtil.toHexColor(ColorScheme.LIGHT_GRAY_COLOR), key, valueStr);
    }

    private boolean doesLocalPlayerHaveMistakesBox() {
        if (client.getLocalPlayer() != null) {
            String name = client.getLocalPlayer().getName();
            return playerMistakesBoxes.stream().anyMatch(b -> b.getPlayerName().equals(name));
        }

        return false;
    }

    private void updateVisiblePanels(boolean isEmpty) {
        if (isEmpty) {
            add(errorPanel);
            overallPanel.setVisible(false);
            mistakesContainer.setVisible(false);
        } else {
            remove(errorPanel);
            overallPanel.setVisible(true);
            mistakesContainer.setVisible(true);
        }
    }

    private void changeDeathGrouping(boolean isRaid) {
        isRaidDeaths = isRaid;
        (isRaid ? raidDeathsBtn : roomDeathsBtn).setSelected(true);
        rebuildAll();
    }

    private void switchMistakesView() {
        // TODO: Minor UI bug: Switching panels re-orders mistakes on the first switch, and is consistent afterwards
        // until another mistake is added. This is because we re-order the boxes we add, but don't persist that ordering
        // in the underlying manager.
        isShowingAll = !isShowingAll;
        currentViewTitle.setText(getCurrentViewTitleText());
        switchMistakesViewBtn.setText(getSwitchMistakesViewButtonText());

        mistakeStateManager.switchMistakes();
        rebuildAll();
    }

    private String getCurrentViewTitleText() {
        return isShowingAll ? "All Raids" : "Current Raid";
    }

    private String getSwitchMistakesViewButtonText() {
        return isShowingAll ? "Show Current" : "Show All";
    }
}
