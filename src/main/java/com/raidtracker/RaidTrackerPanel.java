package com.raidtracker;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
public class RaidTrackerPanel extends PluginPanel {

    private final ItemManager itemManager;
    private final RaidTrackerPlugin plugin;

    private final static Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;

    @Setter
    private ArrayList<RaidTracker> RTList;

    @Setter
    private boolean loaded = false;
    private final JPanel panel = new JPanel();

    public RaidTrackerPanel(final ItemManager itemManager, final RaidTrackerPlugin plugin) {
        this.itemManager = itemManager;
        this.plugin = plugin;

        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        add(panel, BorderLayout.NORTH);

        updateView();
    }

    private void updateView() {
        panel.removeAll();

        JPanel title = getTitle();
        JPanel uniquesPanel = getUniquesPanel();

        panel.add(title);
        panel.add(uniquesPanel, BorderLayout.CENTER);

        panel.revalidate();
        panel.repaint();
    }

    private JPanel getTitle() {
        final JPanel title = new JPanel();

        final JPanel first = new JPanel();
        first.setBackground(BACKGROUND_COLOR);

        title.setBorder(new CompoundBorder(
                new EmptyBorder(10, 8, 8, 8),
                new MatteBorder(0, 0, 1, 0, Color.GRAY)));

        title.setLayout(new BorderLayout());
        title.setBackground(BACKGROUND_COLOR);

        final JLabel text = new JLabel("Chambers of Xeric Data Tracker");
        text.setForeground(Color.WHITE);

        first.add(text);

        title.add(first, BorderLayout.CENTER);

        return title;
    }

    private JPanel getUniquesPanel() {
        final JPanel uniques = new JPanel();

        uniques.setLayout(new GridBagLayout());
        uniques.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        uniques.setBorder(new EmptyBorder(5, 5, 5, 5));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 2;
        c.ipadx = 30;
        c.insets = new Insets(0,10,0,10);



        for (RaidUniques unique : RaidUniques.values()) {
            final AsyncBufferedImage image = itemManager.getImage(unique.getItemID(), 1, false);

            final JLabel icon = new JLabel();

            c.gridx = 0;

            icon.setIcon(new ImageIcon(resizeImage(image, 0.7)));
            icon.setVerticalAlignment(SwingConstants.CENTER);
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            uniques.add(icon, c);

            image.onLoaded(() ->
            {
                icon.setIcon(new ImageIcon(resizeImage(image, 0.7)));
                icon.revalidate();
                icon.repaint();
            });

            String amountReceived;
            String amountSeen;
            ArrayList<RaidTracker> l;
            ArrayList<RaidTracker> l2;

            if (unique.getName().matches("Metamorphatic Dust")) {
                l = filterDustReceivers();
                l2 = filterOwnDusts(l);
            }
            else if (unique.getName().matches("Twisted Kit")) {
                l = filterKitReceivers();
                l2 = filterOwnKits(l);
            }
            else {
                l = filterRTListByName(unique.getName());
                l2 = filterOwnDrops(l);
            }

            amountSeen = Integer.toString(l.size());
            amountReceived = Integer.toString(l2.size());


            final JLabel received = new JLabel(amountReceived, SwingConstants.LEFT);
            final JLabel seen = new JLabel(amountSeen, SwingConstants.LEFT);

            received.setForeground(Color.WHITE);
            seen.setForeground(Color.WHITE);

            final String tooltip = getUniqueToolTip(unique, l.size(), l2.size());

            icon.setToolTipText(tooltip);
            received.setToolTipText(tooltip);
            seen.setToolTipText(tooltip);

            c.gridx = 1;
            uniques.add(received, c);

            c.gridx = 2;
            uniques.add(seen, c);

            c.gridy++;
        }
        return uniques;
    }

    public BufferedImage resizeImage(BufferedImage before, double scale) {
        int w = before.getWidth();
        int h = before.getHeight();
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, before.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR);
        scaleOp.filter(before, after);

        return after;
    }

    public void loadRTList(FileReadWriter fw) {
        RTList = fw.readFromFile();
        loaded = true;
        updateView();
    }

    public ArrayList<RaidTracker> filterRTListByName(String name) {
        if (loaded) {
            ArrayList<RaidTracker> filtered = new ArrayList<RaidTracker>(RTList.stream().filter(RT -> {
                return name.toLowerCase().matches(RT.getSpecialLoot().toLowerCase());
            }).collect(Collectors.toList()));

            return filtered;
        }
        return new ArrayList<RaidTracker>();
    }

    public ArrayList<RaidTracker> filterKitReceivers() {
        if (loaded) {
            ArrayList<RaidTracker> filtered = new ArrayList<RaidTracker>(RTList.stream().filter(RT -> {
                return !RT.getKitReceiver().isEmpty();
            }).collect(Collectors.toList()));

            return filtered;
        }
        return new ArrayList<RaidTracker>();
    }

    public ArrayList<RaidTracker> filterDustReceivers() {
        if (loaded) {
            ArrayList<RaidTracker> filtered = new ArrayList<RaidTracker>(RTList.stream().filter(RT -> {
                return !RT.getDustReceiver().isEmpty();
            }).collect(Collectors.toList()));

            return filtered;
        }
        return new ArrayList<RaidTracker>();
    }

    public ArrayList<RaidTracker> filterOwnDrops(ArrayList<RaidTracker> l) {
        if (loaded) {
            ArrayList<RaidTracker> filtered = new ArrayList<RaidTracker>(l.stream().filter(RT -> {

                if (RT.getSpecialLoot().isEmpty()) {
                    return false;
                }
                return RT.getLootList().get(0).getId() == RaidUniques.getByName(RT.getSpecialLoot()).getItemID();
            }).collect(Collectors.toList()));

            return filtered;
        }
        return new ArrayList<RaidTracker>();
    }

    public ArrayList<RaidTracker> filterOwnKits(ArrayList<RaidTracker> l) {
        if (loaded) {
            ArrayList<RaidTracker> filtered = new ArrayList<RaidTracker>(l.stream().filter(RT -> {
                return RT.getLootList().stream().filter(loot -> loot.getId() == ItemID.TWISTED_ANCESTRAL_COLOUR_KIT).collect(Collectors.toList()).size() > 0;
            }).collect(Collectors.toList()));

            return filtered;
        }
        return new ArrayList<RaidTracker>();
    }

    public ArrayList<RaidTracker> filterOwnDusts(ArrayList<RaidTracker> l) {
        if (loaded) {
            ArrayList<RaidTracker> filtered = new ArrayList<RaidTracker>(l.stream().filter(RT -> {
                return RT.getLootList().stream().filter(loot -> loot.getId() == ItemID.METAMORPHIC_DUST).collect(Collectors.toList()).size() > 0;
            }).collect(Collectors.toList()));

            return filtered;
        }
        return new ArrayList<RaidTracker>();
    }

    public String getUniqueToolTip(RaidUniques unique, int amountSeen, int amountReceived) {
        String tooltip = "<html>" +
                unique.getName() +  "<br>" +
                "Received: " + Integer.toString(amountReceived) + "x" + "<br>" +
                "Seen: " + Integer.toString(amountSeen) + "x";

        return tooltip;
    }

    public void addDrop(RaidTracker RT) {
        RTList.add(RT);
        updateView();
    }

}

