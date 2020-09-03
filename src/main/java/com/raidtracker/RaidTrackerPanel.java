package com.raidtracker;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
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
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class RaidTrackerPanel extends PluginPanel {

    private final ItemManager itemManager;
    private final FileReadWriter fw;

    @Setter
    private ArrayList<RaidTracker> RTList;

    private final HashMap<String, RaidTracker> UUIDMap = new LinkedHashMap<>();

    @Setter
    private boolean loaded = false;
    private final JPanel panel = new JPanel();

    private JButton update;


    public RaidTrackerPanel(final ItemManager itemManager, FileReadWriter fw) {
        this.itemManager = itemManager;
        this.fw = fw;

        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        add(panel, BorderLayout.NORTH);

        updateView();
    }

    private void updateView() {
        panel.removeAll();

        JPanel title = getTitle();
        JPanel killsLoggedPanel = getKillsLoggedPanel();
        JPanel uniquesPanel = getUniquesPanel();
        JPanel pointsPanel = getPointsPanel();
        JPanel splitsEarnedPanel = getSplitsEarnedPanel();
        JPanel regularDrops = getRegularDropsPanel();
        JPanel changePurples = getChangePurples();

        panel.add(title);
        panel.add(killsLoggedPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(uniquesPanel, BorderLayout.CENTER);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(pointsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(splitsEarnedPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(regularDrops);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(changePurples);

        panel.revalidate();
        panel.repaint();
    }

    private JPanel getTitle() {
        final JPanel title = new JPanel();

        final JPanel first = new JPanel();
        first.setBackground(ColorScheme.DARK_GRAY_COLOR);

        title.setBorder(new CompoundBorder(
                new EmptyBorder(5, 8, 8, 8),
                new MatteBorder(0, 0, 1, 0, Color.GRAY)));

        title.setLayout(new BorderLayout());
        title.setBackground(ColorScheme.DARK_GRAY_COLOR);

        final JLabel text = new JLabel("Chambers of Xeric Data Tracker");
        text.setForeground(Color.WHITE);

        first.add(text);

        title.add(first, BorderLayout.CENTER);

        return title;
    }

    private JPanel getUniquesPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        final JPanel title = new JPanel();
        title.setLayout(new GridLayout(0,3));
        title.setBorder(new EmptyBorder(3, 3, 3, 3));
        title.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        JLabel drop = textPanel("Drop");
        JLabel titleSeen = textPanel("Seen");
        JLabel titleReceived = textPanel("Received");

        title.add(drop);
        title.add(titleReceived);
        title.add(titleSeen);


        final JPanel uniques = new JPanel();

        uniques.setLayout(new GridLayout(0,3));
        uniques.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        uniques.setBorder(new EmptyBorder(5, 5, 5, 5));

        int totalUniques = 0;
        int totalOwnName = 0;

        for (RaidUniques unique : RaidUniques.values()) {
            boolean isKit = false;
            boolean isDust = false;

            final AsyncBufferedImage image = itemManager.getImage(unique.getItemID(), 1, false);

            final JLabel icon = new JLabel();


            icon.setIcon(new ImageIcon(resizeImage(image, 0.7)));
            uniques.add(icon);

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
                isDust = true;
            }
            else if (unique.getName().matches("Twisted Kit")) {
                l = filterKitReceivers();
                l2 = filterOwnKits(l);
                isKit = true;
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
            received.setFont(FontManager.getRunescapeSmallFont());
            seen.setForeground(Color.WHITE);
            seen.setFont(FontManager.getRunescapeSmallFont());

            final String tooltip = getUniqueToolTip(unique, l.size(), l2.size());

            if (!isDust && !isKit) {
                totalUniques += l.size();
                totalOwnName += l2.size();
            }

            int bottomBorder = 1;

            if (unique.getName().equals("Twisted Kit")) {
                bottomBorder = 0;
            }

            icon.setToolTipText(tooltip);
            icon.setBorder(new MatteBorder(0,0,bottomBorder,1,ColorScheme.LIGHT_GRAY_COLOR.darker()));
            icon.setVerticalAlignment(SwingConstants.CENTER);
            icon.setHorizontalAlignment(SwingConstants.CENTER);

            received.setToolTipText(tooltip);
            received.setBorder(new MatteBorder(0,0,bottomBorder,1,ColorScheme.LIGHT_GRAY_COLOR.darker()));
            received.setVerticalAlignment(SwingConstants.CENTER);
            received.setHorizontalAlignment(SwingConstants.CENTER);

            seen.setToolTipText(tooltip);
            seen.setBorder(new MatteBorder(0,0,bottomBorder,0,ColorScheme.LIGHT_GRAY_COLOR.darker()));
            seen.setVerticalAlignment(SwingConstants.CENTER);
            seen.setHorizontalAlignment(SwingConstants.CENTER);

            uniques.add(received);

            uniques.add(seen);
        }

        JPanel total = new JPanel();
        total.setLayout(new GridLayout(0,3));
        total.setBorder(new EmptyBorder(3, 3, 3, 3));
        total.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        JLabel totalText = textPanel("Total Purples:");
        JLabel totalOwnNameLabel = textPanel(Integer.toString(totalOwnName));
        JLabel totalUniquesLabel = textPanel(Integer.toString(totalUniques));

        total.add(totalText);
        total.add(totalOwnNameLabel);
        total.add(totalUniquesLabel);

        wrapper.add(title);
        wrapper.add(uniques);
        wrapper.add(total);

        return wrapper;
    }

    private JPanel getPointsPanel() {
        final JPanel points = new JPanel();
        points.setLayout(new GridLayout(0,2));
        points.setBorder(new EmptyBorder(3, 3, 3, 3));
        points.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        JLabel personalTitle = textPanel("Personal Points");
        JLabel totalTitle = textPanel("Total Points");

        points.add(personalTitle);
        points.add(totalTitle);

        int personalPoints = 0;
        int totalPoints = 0;

        if (loaded) {
            personalPoints = atleastZero(RTList.stream().mapToInt(RaidTracker::getPersonalPoints).sum());
            totalPoints = atleastZero(RTList.stream().mapToInt(RaidTracker::getTotalPoints).sum());
        }

        JLabel personalPointsLabel = textPanel(format(personalPoints));
        personalPointsLabel.setToolTipText(NumberFormat.getInstance().format(personalPoints) + " Personal Points");
        personalTitle.setToolTipText(NumberFormat.getInstance().format(personalPoints) + " Personal Points");

        JLabel totalPointsLabel = textPanel(format(totalPoints));
        totalPointsLabel.setToolTipText(NumberFormat.getInstance().format(totalPoints) + " Total Points");
        totalTitle.setToolTipText(NumberFormat.getInstance().format(totalPoints) + " Total Points");

        points.add(personalPointsLabel);
        points.add(totalPointsLabel);

        return points;
    }

    private JPanel getSplitsEarnedPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(0,2));
        wrapper.setBorder(new EmptyBorder(3, 3, 3, 3));
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        int splitGP = 0;

        if (loaded) {
            splitGP = atleastZero(RTList.stream().mapToInt(RaidTracker::getLootSplitReceived).sum());


        }

        JLabel textLabel = textPanel("Split GP earned:");
        textLabel.setToolTipText("GP earned counting the split GP you earned from a drop");

        JLabel valueLabel = textPanel(format(splitGP));
        valueLabel.setToolTipText(NumberFormat.getInstance().format(splitGP) + " gp");

        if (splitGP > 1000000) {
            valueLabel.setForeground(Color.GREEN);
        }

        wrapper.add(textLabel);
        wrapper.add(valueLabel);

        return wrapper;
    }

    private JPanel getKillsLoggedPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(0,2));
        wrapper.setBorder(new EmptyBorder(3, 3, 3, 3));
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        int killsLogged = 0;

        if (loaded) {
            killsLogged = RTList.size();
        }

        JLabel textLabel = textPanel("Kills Logged:");
        JLabel valueLabel = textPanel(Integer.toString(killsLogged));

        wrapper.add(textLabel);
        wrapper.add(valueLabel);

        return wrapper;
    }

    private JPanel getRegularDropsPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        if (loaded) {
            Map<Integer, RaidTrackerItem> uniqueIDs = getDistinctRegularDrops();

            if (uniqueIDs.values().size() > 0) {
                RTList.forEach(RT -> RT.getLootList().forEach(item -> {
                    RaidTrackerItem RTI = uniqueIDs.get(item.id);

                    if (RTI != null) {
                        int qty = RTI.getQuantity();
                        RTI.setQuantity(qty + item.getQuantity());

                        RTI.setPrice(itemManager.getItemPrice(item.id) * RTI.getQuantity());

                        uniqueIDs.replace(item.id, RTI);
                    }
                }));

                ArrayList<RaidTrackerItem> regularDropsList = new ArrayList<>(uniqueIDs.values());

                regularDropsList.sort((o2, o1) -> Integer.compare(o1.getPrice(), o2.getPrice()));


                int regularDropsSum = regularDropsList.stream().mapToInt(RaidTrackerItem::getPrice).sum();

                final JPanel drops = new JPanel();
                drops.setLayout(new GridLayout(0, 5));

                for (RaidTrackerItem drop : regularDropsList) {
                    AsyncBufferedImage image = itemManager.getImage(drop.getId(), drop.getQuantity(), drop.getQuantity() > 1);

                    JPanel iconWrapper = new JPanel();
                    iconWrapper.setPreferredSize(new Dimension(40, 40));
                    iconWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

                    JLabel icon = new JLabel();
                    image.addTo(icon);
                    icon.setBorder(new EmptyBorder(0,5,0,0));

                    image.onLoaded(() ->
                    {
                        image.addTo(icon);
                        icon.revalidate();
                        icon.repaint();
                    });

                    iconWrapper.add(icon, BorderLayout.CENTER);
                    iconWrapper.setBorder(new MatteBorder(1, 0, 0, 1, ColorScheme.DARK_GRAY_COLOR));
                    iconWrapper.setToolTipText(getRegularToolTip(drop));

                    drops.add(iconWrapper);
                }

                final JPanel title = new JPanel();
                title.setLayout(new GridLayout(0, 2));
                title.setBorder(new EmptyBorder(3, 20, 3, 10));
                title.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

                JLabel textLabel = textPanel("Regular Drops");
                textLabel.setHorizontalAlignment(SwingConstants.LEFT);

                JLabel valueLabel = textPanel(format(regularDropsSum) + " gp");
                valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                valueLabel.setForeground(Color.LIGHT_GRAY.darker());
                valueLabel.setToolTipText(NumberFormat.getInstance().format(regularDropsSum));

                title.add(textLabel);
                title.add(valueLabel);


                wrapper.add(title);
                wrapper.add(drops);
            }
        }

        return wrapper;
    }

    private JPanel getChangePurples() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        ArrayList<SplitChanger> SCList = new ArrayList<>();

        JPanel titleWrapper = new JPanel();
        titleWrapper.setLayout(new GridBagLayout());
        titleWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        titleWrapper.setBorder(new EmptyBorder(3,3,3,3));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = SwingConstants.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        JLabel changes = textPanel("Change Purple Splits");
        changes.setBorder(new EmptyBorder(0,5,0,0));

        update = new JButton();
        update.setText("Update");
        update.setFont(FontManager.getRunescapeSmallFont());
        update.setPreferredSize(new Dimension(60,20));
        update.setEnabled(false);
        update.setBorder(new EmptyBorder(2,2,2,2));
        update.setToolTipText("Nothing to update");
        update.addActionListener(e -> {
            SCList.forEach(SC -> {
                RaidTracker tempRaidTracker = SC.getRaidTracker();
                UUIDMap.put(tempRaidTracker.getUniqueID(), tempRaidTracker);
            });
            RTList = new ArrayList<>(UUIDMap.values());
            fw.updateRTList(RTList);

            updateView();
        });

        c.anchor = GridBagConstraints.WEST;
        titleWrapper.add(changes, c);

        c.gridx++;
        c.anchor = GridBagConstraints.EAST;

        titleWrapper.add(update , c);

        if (loaded) {
            wrapper.add(titleWrapper);
            wrapper.add(Box.createRigidArea(new Dimension(0, 2)));

            for (RaidTracker RT : filterPurples()) {
                SplitChanger SC = new SplitChanger(itemManager, RT, this);
                SCList.add(SC);
                wrapper.add(SC);
                wrapper.add(Box.createRigidArea(new Dimension(0, 7)));
            }
        }

        return wrapper;
    }

    public void setUpdateButton(boolean b) {
        update.setEnabled(b);
        update.setBackground(ColorScheme.BRAND_ORANGE);
        update.setToolTipText("Update");
    }

    public JLabel textPanel(String text) {
        JLabel label = new JLabel();
        label.setText(text);
        label.setForeground(Color.WHITE);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(FontManager.getRunescapeSmallFont());

        return label;
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
        for (RaidTracker RT : RTList) {
            UUIDMap.put(RT.getUniqueID(), RT);
        }
        loaded = true;
        updateView();
    }

    public ArrayList<RaidTracker> filterRTListByName(String name) {
        if (loaded) {

            return RTList.stream().filter(RT -> name.toLowerCase().matches(RT.getSpecialLoot().toLowerCase()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterKitReceivers() {
        if (loaded) {
            return RTList.stream().filter(RT -> !RT.getKitReceiver().isEmpty())
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterDustReceivers() {
        if (loaded) {
            return RTList.stream().filter(RT -> !RT.getDustReceiver().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterOwnDrops(ArrayList<RaidTracker> l) {
        if (loaded) {
            return l.stream().filter(RT -> {

                if (RT.getSpecialLoot().isEmpty()) {
                    return false;
                }
                return RT.getLootList().get(0).getId() == RaidUniques.getByName(RT.getSpecialLoot()).getItemID();
            }).collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterOwnKits(ArrayList<RaidTracker> l) {
        if (loaded) {
            return l.stream().filter(RT -> RT.getLootList().stream()
                    .anyMatch(loot -> loot.getId() == ItemID.TWISTED_ANCESTRAL_COLOUR_KIT))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterOwnDusts(ArrayList<RaidTracker> l) {
        if (loaded) {

            return l.stream().filter(RT -> RT.getLootList().stream()
                    .anyMatch(loot -> loot.getId() == ItemID.METAMORPHIC_DUST))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterPurples() {
        if (loaded) {

            return RTList.stream().filter(RT -> Stream.of(RaidUniques.values())
                    .anyMatch(value -> value.getName().toLowerCase().equals(RT.getSpecialLoot().toLowerCase())))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();

    }

    public String getUniqueToolTip(RaidUniques unique, int amountSeen, int amountReceived) {

        return "<html>" +
                unique.getName() +  "<br>" +
                "Received: " + amountReceived + "x" + "<br>" +
                "Seen: " + amountSeen + "x";
    }

    public String getRegularToolTip(RaidTrackerItem drop) {
        return "<html>" + drop.getName() + " x " + drop.getQuantity() + "<br>" +
                "Price: " + format(drop.getPrice()) + " gp";
    }

    public void addDrop(RaidTracker RT) {
        RTList.add(RT);
        UUIDMap.put(RT.getUniqueID(), RT);
        updateView();
    }

    public int atleastZero(int maybeLessThanZero) {
        return Math.max(maybeLessThanZero, 0);
    }

    //yoinked from stackoverflow
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "m");
        suffixes.put(1_000_000_000L, "b");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && truncated % 10 != 0;
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public Map<Integer, RaidTrackerItem> getDistinctRegularDrops() {
        if (loaded) {
            HashSet<Integer> uniqueIDs = new HashSet<>();

            RTList.forEach(RT -> RT.getLootList().forEach(item -> {
                boolean addToSet = true;
                for (RaidUniques unique : RaidUniques.values()) {
                    if (item.getId() == unique.getItemID()) {
                        addToSet = false;
                        break;
                    }
                }
                if (addToSet) {
                    uniqueIDs.add(item.id);
                }
            }));

            Map<Integer, RaidTrackerItem> m = new HashMap<>();

            for (Integer i : uniqueIDs) {
                ItemComposition IC = itemManager.getItemComposition(i);
                m.put(i, new RaidTrackerItem() {
                    {
                        name = IC.getName();
                        id = i;
                        quantity = 0;
                        price = 0;
                    }});
            }

            return m;
        }
        return new HashMap<>();
    }
}

