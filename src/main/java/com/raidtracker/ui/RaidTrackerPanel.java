package com.raidtracker.ui;


import com.raidtracker.RaidTracker;
import com.raidtracker.RaidTrackerConfig;
import com.raidtracker.RaidTrackerItem;
import com.raidtracker.filereadwriter.FileReadWriter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class RaidTrackerPanel extends PluginPanel {

    private final ItemManager itemManager;
    private final FileReadWriter fw;
    private final RaidTrackerConfig config;

    @Setter
    private ArrayList<RaidTracker> RTList;

    private final HashMap<String, RaidTracker> UUIDMap = new LinkedHashMap<>();

    @Setter
    private boolean loaded = false;
    private final JPanel panel = new JPanel();

    private JButton update;

    private String dateFilter = "All Time";
    private String cmFilter = "CM & Normal";


    public RaidTrackerPanel(final ItemManager itemManager, FileReadWriter fw, RaidTrackerConfig config) {
        this.itemManager = itemManager;
        this.fw = fw;
        this.config = config;

        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        add(panel, BorderLayout.NORTH);

        updateView();
    }

    private void updateView() {
        panel.removeAll();

        JPanel title = getTitle();
        JPanel filterPanel = getFilterPanel();
        JPanel killsLoggedPanel = getKillsLoggedPanel();
        JPanel uniquesPanel = getUniquesPanel();
        JPanel pointsPanel = getPointsPanel();
        JPanel splitsEarnedPanel = getSplitsEarnedPanel();
        JPanel regularDrops = getRegularDropsPanel();
        JPanel changePurples = getChangePurples();

        panel.add(title);
        panel.add(filterPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
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


            icon.setIcon(new ImageIcon(resizeImage(image, 0.7, AffineTransformOp.TYPE_BILINEAR)));
            uniques.add(icon);

            image.onLoaded(() ->
            {
                icon.setIcon(new ImageIcon(resizeImage(image, 0.7, AffineTransformOp.TYPE_BILINEAR)));
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
            personalPoints = atleastZero(getFilteredRTList().stream().mapToInt(RaidTracker::getPersonalPoints).sum());
            totalPoints = atleastZero(getFilteredRTList().stream().mapToInt(RaidTracker::getTotalPoints).sum());
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
            splitGP = atleastZero(getFilteredRTList().stream().mapToInt(RaidTracker::getLootSplitReceived).sum());


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
            killsLogged = getFilteredRTList().size();
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


        //TODO: Stack elite clue scrolls
        if (loaded) {
            Map<Integer, RaidTrackerItem> uniqueIDs = getDistinctRegularDrops();

            if (uniqueIDs.values().size() > 0) {
                getFilteredRTList().forEach(RT -> RT.getLootList().forEach(item -> {
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
        update.setFocusPainted(false);
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
            ArrayList<RaidTracker> purpleList = filterPurples();
            purpleList.sort((o2, o1) -> Long.compare(o1.getDate(), o2.getDate()));

            if (purpleList.size() > 0) {
                wrapper.add(titleWrapper);
                wrapper.add(Box.createRigidArea(new Dimension(0, 2)));

                for (RaidTracker RT : purpleList) {
                    SplitChanger SC = new SplitChanger(itemManager, RT, this);
                    SCList.add(SC);
                    wrapper.add(SC);
                    wrapper.add(Box.createRigidArea(new Dimension(0, 7)));
                }
            }
        }

        return wrapper;
    }

    @SuppressWarnings("ConstantConditions")
    private JPanel getFilterPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout());
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        wrapper.setBorder(new EmptyBorder(5,5,5,5));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = SwingConstants.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        JLabel filter = textPanel("Filter kills logged");
        filter.setHorizontalAlignment(SwingConstants.LEFT);
        filter.setBorder(new EmptyBorder(0,0,0,17));
        c.anchor = GridBagConstraints.WEST;
        wrapper.add(filter);

        JComboBox<String> choices = new JComboBox<>(new String []{"All Time", "Today", "Last Week", "Last Month", "Last Year", "Last X Kills"});
        choices.setSelectedItem(dateFilter);
        choices.setPreferredSize(new Dimension(100, 25));
        choices.setFocusable(false);

        choices.addActionListener(e ->  {
            dateFilter = choices.getSelectedItem().toString();
            if (dateFilter.equals("Last X Kills")) {
                choices.setToolTipText("X can be changed in the settings");
            }
            else {
                choices.setToolTipText(null);
            }
            if (loaded) {
                updateView();
            }
        });

        JComboBox<String> cm = new JComboBox<>(new String []{"CM & Normal", "Normal Only", "CM Only"});
        cm.setFocusable(false);
        cm.setPreferredSize(new Dimension(108,25));
        cm.setSelectedItem(cmFilter);

        ComboBoxListRenderer renderer = new ComboBoxListRenderer();
        renderer.setFont(FontManager.getRunescapeSmallFont());
        cm.setRenderer(renderer);

        cm.addActionListener(e -> {
            cmFilter = cm.getSelectedItem().toString();
            if (loaded) {
                updateView();
            }
        });

        c.gridy = 1;
        wrapper.add(Box.createRigidArea(new Dimension(0, 5)), c);

        c.gridy = 2;
        wrapper.add(choices, c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.EAST;
        wrapper.add(cm, c);


        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setPreferredSize(new Dimension(50, 20));
        buttonWrapper.setLayout(new GridLayout(0, 2, 2 ,0));
        buttonWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        BufferedImage refreshIcon = ImageUtil.getResourceStreamFromClass(getClass(), "refresh-grey.png");
        BufferedImage refreshHover = ImageUtil.getResourceStreamFromClass(getClass(), "refresh-white.png");
        BufferedImage deleteIcon = ImageUtil.getResourceStreamFromClass(getClass(), "delete-grey.png");
        BufferedImage deleteHover = ImageUtil.getResourceStreamFromClass(getClass(), "delete-white.png");

        JButton refresh = imageButton(refreshIcon);
        refresh.setToolTipText("Refresh kills logged");
        refresh.addActionListener(e -> {
            if (loaded) {
                loadRTList();
            }
        });

        JButton delete = imageButton(deleteIcon);
        delete.setToolTipText("Delete all logged kills");
        delete.addActionListener(e -> {
            if (loaded) {
                clearData();
            }
        });

        refresh.addMouseListener(new MouseAdapter() {
            public void mouseEntered (MouseEvent e){
                refresh.setIcon(new ImageIcon(refreshHover));
            }

            public void mouseExited (java.awt.event.MouseEvent e){
                refresh.setIcon(new ImageIcon(refreshIcon));
            }
        });

        delete.addMouseListener(new MouseAdapter() {
            public void mouseEntered (MouseEvent e){
                delete.setIcon(new ImageIcon(deleteHover));
            }

            public void mouseExited (java.awt.event.MouseEvent e){
                delete.setIcon(new ImageIcon(deleteIcon));
            }
        });


        buttonWrapper.add(refresh);
        buttonWrapper.add(delete);

        c.gridy = 0;

        wrapper.add(buttonWrapper, c);
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

    public BufferedImage resizeImage(BufferedImage before, double scale, int af) {
        int w = before.getWidth();
        int h = before.getHeight();
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, before.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, af);
        scaleOp.filter(before, after);

        return after;
    }

    public JButton imageButton(BufferedImage image) {
        JButton b = new JButton();
        b.setIcon(new ImageIcon(image));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);

        return b;
    }

    public void loadRTList() {
        RTList = fw.readFromFile();
        for (RaidTracker RT : RTList) {
            UUIDMap.put(RT.getUniqueID(), RT);
        }
        loaded = true;
        updateView();
    }

    public ArrayList<RaidTracker> filterRTListByName(String name) {
        if (loaded) {

            return getFilteredRTList().stream().filter(RT -> name.toLowerCase().matches(RT.getSpecialLoot().toLowerCase()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterKitReceivers() {
        if (loaded) {
            return getFilteredRTList().stream().filter(RT -> !RT.getKitReceiver().isEmpty())
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    public ArrayList<RaidTracker> filterDustReceivers() {
        if (loaded) {
            return getFilteredRTList().stream().filter(RT -> !RT.getDustReceiver().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
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

            return getFilteredRTList().stream().filter(RT -> Stream.of(RaidUniques.values())
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
        boolean hasDecimal = truncated < 1000;
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public Map<Integer, RaidTrackerItem> getDistinctRegularDrops() {
        if (loaded) {
            HashSet<Integer> uniqueIDs = new HashSet<>();

            getFilteredRTList().forEach(RT -> RT.getLootList().forEach(item -> {
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

    private ArrayList<RaidTracker> getFilteredRTList() {
        ArrayList<RaidTracker> tempRTList;

        if (cmFilter.equals("CM & Normal")) {
            tempRTList = RTList;
        }
        else if (cmFilter.equals("CM Only")) {
            tempRTList = RTList.stream().filter(RaidTracker::isChallengeMode)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        else {
            tempRTList = RTList.stream().filter(RT -> !RT.isChallengeMode())
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(7);
        LocalDate lastMonth = today.minusDays(30);
        LocalDate lastYear = today.minusDays(365);

        switch (dateFilter) {
            case "All Time":
                return tempRTList;
            case "Today":
                return tempRTList.stream().filter(RT -> Instant.ofEpochMilli(RT.getDate()).atZone(ZoneId.systemDefault()).toLocalDate().equals(today))
                        .collect(Collectors.toCollection(ArrayList::new));
            case "Last Week":
                return tempRTList.stream().filter(RT -> Instant.ofEpochMilli(RT.getDate()).atZone(ZoneId.systemDefault()).toLocalDate().equals(lastWeek))
                        .collect(Collectors.toCollection(ArrayList::new));
            case "Last Month":
                return tempRTList.stream().filter(RT -> Instant.ofEpochMilli(RT.getDate()).atZone(ZoneId.systemDefault()).toLocalDate().equals(lastMonth))
                        .collect(Collectors.toCollection(ArrayList::new));
            case "Last Year":
                return tempRTList.stream().filter(RT -> Instant.ofEpochMilli(RT.getDate()).atZone(ZoneId.systemDefault()).toLocalDate().equals(lastYear))
                        .collect(Collectors.toCollection(ArrayList::new));
            case "Last X Kills":
                return new ArrayList<>(tempRTList.subList(Math.max(tempRTList.size() - config.lastXKills(), 0), tempRTList.size()));
        }

        return RTList;
    }

    private void clearData()
    {
        // Confirm delete action
        final int delete = JOptionPane.showConfirmDialog(this.getRootPane(), "<html>Are you sure you want to clear all data for this tab?<br/>There is no way to undo this action.</html>", "Warning", JOptionPane.YES_NO_OPTION);
        if (delete == JOptionPane.YES_OPTION)
        {
            if (!fw.delete())
            {
                JOptionPane.showMessageDialog(this.getRootPane(), "Unable to clear stored data, please try again.");
                return;
            }

            loadRTList();
        }
    }

}

