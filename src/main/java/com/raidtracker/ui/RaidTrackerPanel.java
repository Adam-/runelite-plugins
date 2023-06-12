package com.raidtracker.ui;


import com.raidtracker.*;
import com.raidtracker.filereadwriter.FileReadWriter;
import com.raidtracker.utils.UniqueDrop;
import com.raidtracker.utils.uiUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Slf4j
public class RaidTrackerPanel extends PluginPanel {

    @Setter
    private ItemManager itemManager;
    private final FileReadWriter fw;
    private final RaidTrackerConfig config;
    private final ClientThread clientThread;
    private final Client client;
    private final uiUtils uiUtils = new uiUtils();
    @Setter
    private ArrayList<RaidTracker> RTList;
    private ArrayList<RaidTracker> tobRTList;
    private ArrayList<RaidTracker> toaRTList;
    private final HashMap<String, RaidTracker> UUIDMap = new LinkedHashMap<>();
    private final HashMap<String, RaidTracker> TobUUIDMap = new LinkedHashMap<>();
    private final HashMap<String, RaidTracker> ToaUUIDMap = new LinkedHashMap<>();

    @Setter
    private boolean loaded = false;
    private final JPanel panel = new JPanel();

    private JButton update;

    @Setter
    private String dateFilter = "All Time";
    @Setter
    private String cmFilter = "";

    @Setter
    private Integer cmIndex = -1;

    @Setter
    private String mvpFilter = "Both";
    @Setter
    private String teamSizeFilter = "All sizes";

    @Setter
    private int RaidIndex = 0;


    @Getter
    private final boolean isTob = false;

    @Getter
    EnumSet<RaidUniques> tobUniques = EnumSet.of(
            RaidUniques.AVERNIC,
            RaidUniques.RAPIER,
            RaidUniques.SANGSTAFF,
            RaidUniques.JUSTI_FACEGUARD,
            RaidUniques.JUSTI_CHESTGUARD,
            RaidUniques.JUSTI_LEGGUARDS,
            RaidUniques.SCYTHE,
            RaidUniques.LILZIK,
            RaidUniques.HOLY_KIT,
            RaidUniques.SANG_DUST,
            RaidUniques.SANG_KIT
    );

    @Getter
    EnumSet<RaidUniques> coxUniques = EnumSet.of(
            RaidUniques.DEX,
            RaidUniques.ARCANE,
            RaidUniques.TWISTED_BUCKLER,
            RaidUniques.DHCB,
            RaidUniques.DINNY_B,
            RaidUniques.ANCESTRAL_HAT,
            RaidUniques.ANCESTRAL_TOP,
            RaidUniques.ANCESTRAL_BOTTOM,
            RaidUniques.DRAGON_CLAWS,
            RaidUniques.ELDER_MAUL,
            RaidUniques.KODAI,
            RaidUniques.TWISTED_BOW,
            RaidUniques.DUST,
            RaidUniques.TWISTED_KIT,
            RaidUniques.OLMLET
    );

    @Getter
    EnumSet<RaidUniques> toaUniques = EnumSet.of(
            RaidUniques.GUARDIAN,
            RaidUniques.SHADOW,
            RaidUniques.ELIDNIS_WARD,
            RaidUniques.MASORI_HEAD,
            RaidUniques.MASORI_CHEST,
            RaidUniques.MASORI_LEGS,
            RaidUniques.FANG,
            RaidUniques.LIGHTBEARER
    );

    public RaidTrackerPanel(final ItemManager itemManager, FileReadWriter fw, RaidTrackerConfig config, ClientThread clientThread, Client client) {
        this.itemManager = itemManager;
        this.fw = fw;
        this.config = config;
        this.clientThread = clientThread;
        this.client = client;

        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        add(panel, BorderLayout.NORTH);
        reloadPanel(0);
    }


    private JPanel getHeader() {
        final JPanel title = new JPanel();
        title.setBorder(new EmptyBorder(3, 0, 10, 0));
        title.setLayout(new BoxLayout(title,BoxLayout.Y_AXIS));

        final JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new GridLayout(0,1));
        buttonWrapper.setBorder(new EmptyBorder(5,0,0,0));

        JComboBox<String> raidType = new JComboBox<>(new String []{"Chambers Of Xeric", "Theatre of Blood", "Tombs of Amascot"});
        raidType.setRenderer(new MyComboRenderer());
        raidType.setFocusable(false);
        raidType.setSelectedIndex(RaidIndex);
        raidType.setPreferredSize(new Dimension(220,25));
        buttonWrapper.add(raidType);
        raidType.addActionListener(e -> {
            RaidIndex  = raidType.getSelectedIndex();
            reloadPanel(raidType.getSelectedIndex());
        });

        JPanel titleLabelWrapper = new JPanel();
        JLabel titleLabel = new JLabel("Raids Data Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ColorScheme.LIGHT_GRAY_COLOR.darker()),
                new EmptyBorder(0, 20, 5, 20)
        ));

        titleLabelWrapper.add(titleLabel, BorderLayout.CENTER);
        title.add(titleLabelWrapper);
        title.add(buttonWrapper);


        panel.add(title);


        return title;
    }

    private void reloadPanel(int index) {

        panel.removeAll();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel title = getHeader();

        panel.add(title);
        panel.add(getFilterPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getKillsLoggedPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        switch (index) {
            case 0 :
                panel.add(getPointsPanel());
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
                break;
            case 1 :
                panel.add(getMvpPanel());
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
                break;
            case 2 :
                break;
            default :
                System.out.println("Error with user selection");
                break;
        }
        panel.add(getUniquesPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getSplitsEarnedPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getTimeSplitsPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getRegularDropsPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(getChangePurples());
        panel.revalidate();
        panel.repaint();
    }

    public int getRaidIndex() {
        return this.RaidIndex;
    }


    public final class MyComboRenderer extends JLabel implements ListCellRenderer<Object> {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            final AsyncBufferedImage[] images = {
                    itemManager.getImage(ItemID.OLMLET, 1, false),
                    itemManager.getImage(ItemID.LIL_ZIK, 1, false),
                    itemManager.getImage(ItemID.TUMEKENS_GUARDIAN, 1, false)
            };

            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setText(value.toString());

            if (index == -1)
            {
                label.setIcon(new ImageIcon(images[Math.max(RaidIndex, 0)]));
            } else
            {
                label.setIcon(new ImageIcon(images[Math.max(index, 0)]));
            }
            return label;
        }
    }

    private JPanel getUniquesPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        final JPanel title = new JPanel();
        title.setLayout(new GridLayout(0,3));
        title.setBorder(new EmptyBorder(3, 3, 3, 3));
        title.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        JLabel drop = textPanel("Drop", SwingConstants.CENTER, SwingConstants.CENTER);
        JLabel titleSeen = textPanel("Seen", SwingConstants.CENTER, SwingConstants.CENTER);
        JLabel titleReceived = textPanel("Received", SwingConstants.CENTER, SwingConstants.CENTER);

        title.add(drop);
        title.add(titleReceived);
        title.add(titleSeen);


        final JPanel uniques = new JPanel();

        uniques.setLayout(new GridLayout(0,3));
        uniques.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        uniques.setBorder(new EmptyBorder(5, 5, 5, 5));

        int totalUniques = 0;
        int totalOwnName = 0;

        for (RaidUniques unique : getUniquesList()) {
            boolean untradable = false;
            boolean isPet = false;

            final AsyncBufferedImage image = itemManager.getImage(unique.getItemID(), 1, false);

            final JLabel icon = new JLabel();

            if (image != null)
            {
                icon.setIcon(new ImageIcon(uiUtils.resizeImage(image, 0.7, AffineTransformOp.TYPE_BILINEAR)));
                uniques.add(icon);
                image.onLoaded(() ->
                {
                    icon.setIcon(new ImageIcon(uiUtils.resizeImage(image, 0.7, AffineTransformOp.TYPE_BILINEAR)));
                    icon.revalidate();
                    icon.repaint();
                });
            }

            String amountReceived;
            String amountSeen;
            int l;
            int l2;
            String pDrop = uiUtils.unescapeJavaString(unique.getName());
            switch (uiUtils.unescapeJavaString(unique.getName())) {
                case "Metamorphic Dust":
                case "Twisted Kit":
                case "Sanguine Dust":
                case "Holy Ornement Kit":
                case "Sanguine Ornement Kit":
                    l = filterUntradables(getFilteredRTList(), "untradable", pDrop);
                    l2 = filterUntradables(getFilteredRTList(), "untradable", pDrop, true);
                    untradable = true;
                    break;
                case "Olmlet":
                case "Lil' Zik":
                case "Tumeken's guardian":
                    l = filterUntradables(getFilteredRTList(), "pet", pDrop);
                    System.out.println(l);
                    l2 = filterUntradables(getFilteredRTList(), "pet", pDrop, true);
                    isPet = true;
                    break;
                default:
                    l = filterRTListByName(unique.getName());
                    l2 = filterRTListByName(unique.getName(), true);
                    break;
            }

            amountSeen = Integer.toString(l);
            amountReceived = Integer.toString(l2);


            final JLabel received = new JLabel(amountReceived, SwingConstants.LEFT);
            final JLabel seen = new JLabel(amountSeen, SwingConstants.LEFT);

            received.setForeground(Color.WHITE);
            received.setFont(FontManager.getRunescapeSmallFont());
            seen.setForeground(Color.WHITE);
            seen.setFont(FontManager.getRunescapeSmallFont());

            final String tooltip = getUniqueToolTip(unique, l, l2);

            if (!untradable && !isPet) {
                totalUniques += l;
                totalOwnName += l2;
            }

            int bottomBorder = 1;

            if (isPet) {
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

        JLabel totalText = textPanel("Total Purples:", SwingConstants.CENTER, SwingConstants.CENTER);
        totalText.setHorizontalAlignment(SwingConstants.LEFT);
        JLabel totalOwnNameLabel = textPanel(Integer.toString(totalOwnName), SwingConstants.CENTER, SwingConstants.CENTER);
        JLabel totalUniquesLabel = textPanel(Integer.toString(totalUniques), SwingConstants.CENTER, SwingConstants.CENTER);

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
        points.setLayout(new GridLayout(0,2, 10, 5));
        points.setBorder(new EmptyBorder(5,5,5,40));
        points.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        JLabel personalTitle = textPanel("Personal Points", SwingConstants.CENTER, SwingConstants.CENTER);
        JLabel totalTitle = textPanel("Total Points", SwingConstants.CENTER, SwingConstants.CENTER);
        personalTitle.setHorizontalAlignment(SwingConstants.LEFT);
        totalTitle.setHorizontalAlignment(SwingConstants.LEFT);

        int personalPoints = 0;
        int totalPoints = 0;

        if (loaded) {
            personalPoints = atleastZero(getFilteredRTList().stream().mapToInt(RaidTracker::getPersonalPoints).sum());
            totalPoints = atleastZero(getFilteredRTList().stream().mapToInt(RaidTracker::getTotalPoints).sum());
        }

        JLabel personalPointsLabel = textPanel(format(personalPoints), SwingConstants.CENTER, SwingConstants.RIGHT);
        personalPointsLabel.setToolTipText(NumberFormat.getInstance().format(personalPoints) + " Personal Points");
        personalTitle.setToolTipText(NumberFormat.getInstance().format(personalPoints) + " Personal Points");

        JLabel totalPointsLabel = textPanel(format(totalPoints), SwingConstants.CENTER, SwingConstants.RIGHT);
        totalPointsLabel.setToolTipText(NumberFormat.getInstance().format(totalPoints) + " Total Points");
        totalTitle.setToolTipText(NumberFormat.getInstance().format(totalPoints) + " Total Points");

        points.add(personalTitle);
        points.add(personalPointsLabel);
        points.add(totalTitle);
        points.add(totalPointsLabel);
        return points;
    }

    private JPanel getSplitsEarnedPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(0,3));
        wrapper.setBorder(new EmptyBorder(3, 3, 3, 3));
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        JLabel textLabel = textPanel("Split GP", SwingConstants.CENTER, SwingConstants.CENTER);
        textLabel.setHorizontalAlignment(SwingConstants.LEFT);

        textLabel.setToolTipText("GP earned counting the split GP you earned from a drop");

        final int[] splitGP = {0};

        //TODO Change to new way of storing uniques
        if (loaded)
        {
            splitGP[0] = 0;
            getFilteredRTList().forEach(rt ->
                    rt.getUniques().forEach(d -> splitGP[0] += d.isFfa() ? (d.getUsername().equalsIgnoreCase(RaidTrackerPlugin.profileKey) ? d.getValue() : 0) : d.getValue() / d.getSCount()));
        }


        JLabel valueLabel = textPanel(format(splitGP[0]), SwingConstants.CENTER, SwingConstants.CENTER);
        valueLabel.setToolTipText(NumberFormat.getInstance().format(splitGP[0]) + " gp");

        if (splitGP[0] > 1000000) {
            valueLabel.setForeground(Color.GREEN);
        }

        wrapper.add(textLabel);
        wrapper.add(textPanel("", SwingConstants.CENTER, SwingConstants.CENTER));
        wrapper.add(valueLabel);

        return wrapper;
    }

    private JPanel getKillsLoggedPanel() {
        final JPanel wrapper = new JPanel();

        wrapper.setLayout(new GridLayout(0,2, 10, 5));
        wrapper.setBorder(new EmptyBorder(5,5,5,40));
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        int killsLogged = 0;

        if (loaded) {
            killsLogged = getDistinctKills(getFilteredRTList()).size();
        }

        JLabel textLabel = textPanel("Kills Logged:", SwingConstants.CENTER, SwingConstants.LEFT);
        JLabel valueLabel = textPanel(Integer.toString(killsLogged), SwingConstants.CENTER, SwingConstants.RIGHT);
        wrapper.add(textLabel);
        wrapper.add(valueLabel);

        return wrapper;
    }

    private JPanel getRegularDropsPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        if (loaded) {
            Map<Integer, RaidTrackerItem> uniqueIDs = new HashMap<>();
            try {
                uniqueIDs = getDistinctRegularDrops().get();
            } catch (InterruptedException | ExecutionException e) {
                uniqueIDs = new HashMap<>();
            } finally {
                Map<Integer, Integer> priceMap = new HashMap<>();

                for (RaidTrackerItem item : uniqueIDs.values()) {
                    priceMap.put(item.getId(), item.getPrice());
                }

                if (uniqueIDs.values().size() > 0) {
                    for (RaidTracker RT : getFilteredRTList()) {
                        for (RaidTrackerItem item : RT.getLootList()) {
                            RaidTrackerItem RTI = uniqueIDs.get(item.getId());

                            //making sure to not change the clues here as it's been handled in getDistinctRegularDrops
                            if (RTI != null && RTI.getId() != 12073) {
                                int qty = RTI.getQuantity();
                                RTI.setQuantity(qty + item.getQuantity());

                                RTI.setPrice(priceMap.get(item.getId()) * RTI.getQuantity());

                                uniqueIDs.replace(item.getId(), RTI);
                            }
                        }
                    }

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
                        icon.setBorder(new EmptyBorder(0, 5, 0, 0));

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

                    JLabel textLabel = textPanel("Regular Drops", SwingConstants.CENTER, SwingConstants.CENTER);
                    textLabel.setHorizontalAlignment(SwingConstants.LEFT);

                    JLabel valueLabel = textPanel(format(regularDropsSum) + " gp", SwingConstants.CENTER, SwingConstants.CENTER);
                    valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                    valueLabel.setForeground(Color.LIGHT_GRAY.darker());
                    valueLabel.setToolTipText(NumberFormat.getInstance().format(regularDropsSum));

                    title.add(textLabel);
                    title.add(valueLabel);


                    wrapper.add(title);
                    wrapper.add(drops);
                }
            }
        }

        return wrapper;
    }

    private JPanel getChangePurples()
    {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        ArrayList<RaidTracker> trackerList = new ArrayList<>();
        JPanel titleWrapper = new JPanel();
        titleWrapper.setLayout(new GridBagLayout());
        titleWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        titleWrapper.setBorder(new EmptyBorder(3,3,3,3));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = SwingConstants.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        JLabel changes = textPanel("Change Purple Splits", SwingConstants.CENTER, SwingConstants.CENTER);
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
            trackerList.forEach(tempRaidTracker -> {
                    switch (RaidIndex)
                    {
                        case 0 :
                            UUIDMap.put(tempRaidTracker.getUniqueID(), tempRaidTracker);
                            RTList = new ArrayList<>(UUIDMap.values());
                            fw.updateRTList(RTList, 0);
                            break;
                        case 1 :
                            TobUUIDMap.put(tempRaidTracker.getUniqueID(), tempRaidTracker);
                            tobRTList = new ArrayList<>(TobUUIDMap.values());
                            fw.updateRTList(tobRTList, 1);
                            break;
                        case 2 :
                            ToaUUIDMap.put(tempRaidTracker.getUniqueID(), tempRaidTracker);
                            toaRTList = new ArrayList<>(ToaUUIDMap.values());
                            fw.updateRTList(toaRTList, 2);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + RaidIndex);
                    }
            });
            reloadPanel(RaidIndex);
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

                for (int i = 0; i < Math.min(purpleList.size(), 10); i++) {

                    RaidTracker RT = purpleList.get(i);
                    ArrayList<UniqueDrop> d = RT.getUniques();
                    trackerList.add(RT);
                    d.forEach(e -> {
                        SplitChanger SC = new SplitChanger(itemManager, e, this, RT, RaidTrackerPlugin.profileKey,client.getLocalPlayer().getName());
                        wrapper.add(SC);
                        wrapper.add(Box.createRigidArea(new Dimension(0, 7)));
                    });
                }
            }
        }

        return wrapper;
    }

    @SuppressWarnings("ConstantConditions")
    private JPanel getFilterPanel() {

        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(0, 1, 1, 5));
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        wrapper.setBorder(new EmptyBorder(5,5,5,5));

        final JLabel title_wrapper = new JLabel();

        wrapper.add(title_wrapper);

        title_wrapper.setLayout(new GridLayout(1, 1));
        title_wrapper.add(textPanel("Filter kills logged", SwingConstants.CENTER, SwingConstants.LEFT));
        title_wrapper.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel buttonWrapper = new JLabel();
        buttonWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT));
        title_wrapper.add(buttonWrapper);

        BufferedImage refreshIcon = ImageUtil.loadImageResource(getClass(), "refresh-grey.png");
        BufferedImage refreshHover = ImageUtil.loadImageResource(getClass(), "refresh-white.png");
        BufferedImage deleteIcon = ImageUtil.loadImageResource(getClass(), "delete-grey.png");
        BufferedImage deleteHover = ImageUtil.loadImageResource(getClass(), "delete-white.png");

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

        buttonWrapper.add(refresh);
        buttonWrapper.add(delete);

        JComboBox<String> teamSize;
        JComboBox<String> mvpList;

        switch (RaidIndex) {
            case 0 :
                teamSize = new JComboBox<>(new String []{"All sizes", "Solo", "Duo", "Trio", "4-man", "5-man", "6-man", "7-man", "8-10 Players", "11-14 Players", "15-24 Players", "24+ Players"});
                break;
            case 1 :
                teamSize = new JComboBox<>(new String []{"All sizes", "Solo", "Duo", "Trio", "4-man", "5-man"});
                break;
            case 2 :
                teamSize = new JComboBox<>(new String []{"All sizes", "Solo", "Duo", "Trio", "4-man", "5-man", "6-man", "7-man", "8-Man"});

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + RaidIndex);
        }


        teamSize.setFocusable(false);
        teamSize.setPreferredSize(new Dimension(110,25));
        teamSize.setSelectedItem(teamSizeFilter);

        teamSize.addActionListener(e -> {
            teamSizeFilter = teamSize.getSelectedItem().toString();
            if (loaded) {
                reloadPanel(RaidIndex);
            }
        });

        wrapper.add(teamSize);


        JComboBox<String> choices = new JComboBox<>(new String []{"All Time", "12 Hours", "Today", "3 Days", "Week", "Month","3 Months", "Year", "X Kills"});
        choices.setSelectedItem(dateFilter);
        choices.setPreferredSize(new Dimension(100, 25));
        choices.setFocusable(false);

        choices.addActionListener(e ->  {
            dateFilter = choices.getSelectedItem().toString();
            if (dateFilter.equals("X Kills")) {
                choices.setToolTipText("X can be changed in the settings");
            }
            else {
                choices.setToolTipText(null);
            }
            if (loaded) {
                reloadPanel(RaidIndex);
            }
        });

        JComboBox<String> type;
        String[] filters;

        switch (RaidIndex) {
            case 0 :
                filters = new String []{"CM & Normal", "Normal Only", "CM Only"};
                break;
            case 1 :
                filters = new String []{"Hard Mode & Normal", "Hard Mode Only", "Normal Only"};
                break;
            case 2 :
                filters = new String []{"Normal, Expert & Entry", "Entry Only", "Normal Only", "Expert Only"};
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + RaidIndex);
        }

        type = new JComboBox<>(filters);
        if (!ArrayUtils.contains(filters, cmFilter))
        {
            cmFilter = type.getSelectedItem().toString();
            cmIndex = type.getSelectedIndex();
        }

        type.setFocusable(false);
        type.setPreferredSize(new Dimension(110,25));
        type.setSelectedItem(cmFilter);

        type.addActionListener(e -> {
            cmFilter = type.getSelectedItem().toString();
            cmIndex = type.getSelectedIndex();
            if (loaded) {
               reloadPanel(RaidIndex);
            }
        });


        wrapper.add(choices);
        wrapper.add(type);

        refresh.addMouseListener(new MouseAdapter() {
            public void mouseEntered (MouseEvent e){
                refresh.setIcon(new ImageIcon(refreshHover));
            }

            public void mouseExited (MouseEvent e){
                refresh.setIcon(new ImageIcon(refreshIcon));
            }
        });

        delete.addMouseListener(new MouseAdapter() {
            public void mouseEntered (MouseEvent e){
                delete.setIcon(new ImageIcon(deleteHover));
            }

            public void mouseExited (MouseEvent e){
                delete.setIcon(new ImageIcon(deleteIcon));
            }
        });

        // Raid Extras
        if (RaidIndex == 1) {
            mvpList = new JComboBox<>(new String[]{"Both", "My MVP"});
            mvpList.setFocusable(false);
            mvpList.setPreferredSize(new Dimension(110, 25));
            mvpList.setSelectedItem(mvpFilter);

            mvpList.addActionListener(e -> {
                mvpFilter = mvpList.getSelectedItem().toString();
                if (loaded) {
                    reloadPanel(RaidIndex);
                }
            });
            wrapper.add(mvpList);
        }

        return wrapper;
        }

    private JPanel getMvpPanel() {
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(0,2));
        wrapper.setBorder(new EmptyBorder(5,5,5,40));
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        int mvpAmount = 0;


        if (loaded) {
            mvpAmount = tobRTList.stream().mapToInt(RT -> {
                if (RT.isMvpInOwnName()) {
                    return 1;
                }
                return 0;
            }).sum();


        }

        JLabel textLabel = textPanel("Total MVP's:", SwingConstants.CENTER, SwingConstants.LEFT);

        JLabel valueLabel = textPanel(Integer.toString(mvpAmount), SwingConstants.CENTER, SwingConstants.RIGHT);

        wrapper.add(textLabel);
        wrapper.add(valueLabel);
        return wrapper;
    }


    private JPanel getTimeSplitsPanel() {
        final JPanel wrapper = new JPanel();

        if (loaded) {
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
            wrapper.setBorder(new EmptyBorder(3, 0, 0, 0));
            wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

            final JPanel title = new JPanel();

            title.setBorder(new EmptyBorder(3, 20, 3, 10));
            title.setLayout(new GridLayout(0, 1));
            title.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

            JLabel textLabel = textPanel("Best Recorded Times", SwingConstants.CENTER, SwingConstants.CENTER);

            title.add(textLabel);

            JPanel timeTable = new JPanel();
            timeTable.setLayout(new GridLayout(0, 2));
            timeTable.setBorder(new EmptyBorder(5,3,1,3));
            timeTable.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            String[] rooms;
            String[] ignored = new String[0];
            switch (RaidIndex)
            {
                case 0 :
                    if (config.dey0Tracker())
                    {
                        rooms = new String[] { "Upper", "Middle", "Lower", "Shamans", "Vasa","Vanguards","Mystics","Tekton", "Muttadiles", "Vespula", "Ice Demon", "Thieving", "Tightrope", "Crabs" };
                    } else
                    {
                        rooms = new String[] { "Upper", "Middle", "Lower"};
                    }
                    break;
                case 1 :
                    rooms = new String[] {"Maiden", "Bloat", "Nylocas", "Sotetseg", "Xarpus", "Verzik"};
                    break;
                case 2 :
                    if (config.toatracker())
                    {
                        rooms = new String[]{"Path of Crondis", "Zebak", "Path of Apmeken", "Ba-Ba", "Path of Het", "Akkha", "Path of Scabaras", "Kephri", "The Wardens", "Zebak (Crondis)", "Ba-Ba (Apmeken)", "Akkha (Het)", "Kephri (Scabaras"};
                    } else
                    {
                        rooms = new String[]{"Path of Crondis", "Zebak", "Path of Apmeken", "Ba-Ba", "Path of Het", "Akkha", "Path of Scabaras", "Kephri", "The Wardens"};
                    }
                    ignored = new String[]{"Path of Crondis", "Zebak", "Path of Apmeken", "Ba-Ba", "Path of Het", "Akkha", "Path of Scabaras", "Kephri"};
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + RaidIndex);
            }


            ArrayList<String[]> labels = new ArrayList<>();
            for (int i = 0; i < rooms.length; i++)
            {
                RaidTracker best = new RaidTracker();
                for (RaidTracker raidTracker : getFilteredRTList())
                {
                    if  (raidTracker.getRoomTimes()[i] > 0)
                    {
                        best = ((best.getRoomTimes()[i] > 0) && (best.getRoomTimes()[i] < raidTracker.getRoomTimes()[i])) ? best: raidTracker;
                    }
                }
                if ((config.toatracker() && !(ArrayUtils.contains(ignored, rooms[i]))) || !config.toatracker())
                {
                    labels.add(new String[] {rooms[i], uiUtils.secondsToMinuteString(best.getRoomTimes()[i])});
                }
            }

            if (config.toatracker() && labels.size() == 5)
            {
                Collections.reverse(labels);
            }
            for (String[] label : labels)
            {
                timeTable.add(textPanel(label[0], 0));
                timeTable.add(textPanel(label[1], 0));
            }

            timeTable.add(textPanel("Overall Time", 2));
            timeTable.add(textPanel(uiUtils.secondsToMinuteString(getFilteredRTList().stream().filter(RT -> RT.getRaidTime() > 0).min(comparing(RaidTracker::getRaidTime)).orElse(new RaidTracker()).getRaidTime()), 3));

            wrapper.add(title);
            wrapper.add(timeTable);
        }

        return wrapper;
    }



    public void setUpdateButton(boolean b) {
        update.setEnabled(b);
        update.setBackground(ColorScheme.BRAND_ORANGE);
        update.setToolTipText("Update");
    }

    public JLabel textPanel(String text, int halignment, int valignment) {
        JLabel label = new JLabel();
        label.setText(text);
        label.setForeground(Color.WHITE);
        label.setVerticalAlignment(halignment);
        label.setHorizontalAlignment(valignment);
        label.setFont(FontManager.getRunescapeSmallFont());

        return label;
    }

    public JLabel textPanel(String text, int borderOptions) {
        JLabel label = new JLabel();
        label.setText(text);
        label.setForeground(Color.WHITE);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(FontManager.getRunescapeSmallFont());

        if (borderOptions == 0) {
            label.setBorder(new CompoundBorder(
                    new MatteBorder(0,0,1,1,ColorScheme.LIGHT_GRAY_COLOR.darker()),
                    new EmptyBorder(5,3,5,3)));
        }
        else if (borderOptions == 1) {
            label.setBorder(new MatteBorder(0,0,1,0,ColorScheme.LIGHT_GRAY_COLOR.darker()));
        }
        else if (borderOptions == 2) {
            label.setBorder(new MatteBorder(0,0,0,1,ColorScheme.LIGHT_GRAY_COLOR.darker()));
        }
        else {
            label.setBorder(new MatteBorder(0,0,0,0,ColorScheme.LIGHT_GRAY_COLOR.darker()));
        }

        return label;
    }


    public JButton imageButton(BufferedImage image) {
        JButton b = new JButton();
        b.setIcon(new ImageIcon(image));
        b.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);

        return b;
    }

    public void loadRTList() {
        //TODO: support for a custom file so that it can be added to onedrive for example.
        RTList = fw.readFromFile(0);
        for (RaidTracker RT : RTList) {
            UUIDMap.put(RT.getUniqueID(), RT);
        }

        tobRTList = fw.readFromFile(1);

        for (RaidTracker RT : tobRTList) {
            TobUUIDMap.put(RT.getUniqueID(), RT);
        }

        toaRTList = fw.readFromFile(2);

        for (RaidTracker RT : toaRTList) {
            ToaUUIDMap.put(RT.getUniqueID(), RT);
        }
        loaded = true;
        reloadPanel(RaidIndex);
    }


    public int filterRTListByName(String name, boolean own)
    {
        int ret = 0;
        if (loaded)
        {
            for (RaidTracker rt : getFilteredRTList())
            {
                for (UniqueDrop ud : rt.getUniques())
                {
                    ArrayList<UniqueDrop> drops = rt.getUniques().stream().filter(d -> d.getDrop().equalsIgnoreCase(name)).collect(Collectors.toCollection(ArrayList::new));
                    ret += own ? drops.stream().filter(d -> d.getUsername().equalsIgnoreCase(RaidTrackerPlugin.profileKey)).count() : drops.size();
                };
            };
        }
        return ret;
    };
    public int filterRTListByName(String name)
    {
        return filterRTListByName(name, false);
    }
    public int filterUntradables(ArrayList<RaidTracker> l, String f, String filter, boolean own)
    {
        int ret = 0;
        if (loaded)
        {
            System.out.println(f);
            for (RaidTracker rt : getFilteredRTList())
            {
                switch (f)
                {
                    case "pet" :
                    {
                        System.out.println("test pets");
                        System.out.println(own ?
                                rt.getPets().stream().filter(nt -> nt.getUsername().equalsIgnoreCase(RaidTrackerPlugin.profileKey) && nt.getDrop().equalsIgnoreCase(filter)).count() :
                                rt.getPets().stream().filter(nt -> nt.getDrop().equalsIgnoreCase(filter)).count()
                        );
                        System.out.println(filter);
                        ret += (int) (own ?
                                rt.getPets().stream().filter(nt -> nt.getUsername().equalsIgnoreCase(RaidTrackerPlugin.profileKey) && nt.getDrop().equalsIgnoreCase(filter)).count() :
                                rt.getPets().stream().filter(nt -> nt.getDrop().equalsIgnoreCase(filter)).count()
                        );

                    }
                    case "untradable" :
                    {
                        System.out.println("test untradables");
                        ret += (int) (own ?
                            rt.getNTradables().stream().filter(nt -> nt.getUsername().equalsIgnoreCase(RaidTrackerPlugin.profileKey) && nt.getDrop().equalsIgnoreCase(filter)).count() :
                            rt.getNTradables().stream().filter(nt -> nt.getDrop().equalsIgnoreCase(filter)).count()
                        );
                    }
                    default : {};
                };
            };
        };
        return ret;
    };


    public int filterUntradables(ArrayList<RaidTracker> l, String f, String filter)
    {
        return filterUntradables(l, f, filter, false);
    };

    public ArrayList<RaidTracker> filterPurples() {
        if (loaded) {
            return getFilteredRTList().stream().filter(RT -> !RT.getUniques().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
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

    public void addDrop(RaidTracker RT, boolean update)
    {
        switch (RT.getInRaidType())
        {
            case 0 :
                RTList.add(RT);
                if (!update) return;
                UUIDMap.put(RT.getUniqueID(), RT);
                break;
            case 1 :
                tobRTList.add(RT);
                if (!update) return;
                TobUUIDMap.put(RT.getUniqueID(), RT);
                break;
            case 2 :
                toaRTList.add(RT);
                if (!update) return;
                ToaUUIDMap.put(RT.getUniqueID(), RT);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + RT.getInRaidType());
        }
        reloadPanel(RaidIndex);
    }

    public void addDrop(RaidTracker RT) {
        addDrop(RT, true);
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
        //Long.MIN_VALUE == -Long.MIN_VALUE, so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 100); //the number part of the output times 100
        boolean hasDecimal = truncated < 1000;
        return hasDecimal ? (truncated / 100d) + suffix : (truncated / 100) + suffix;
    }

    public Future<Map<Integer, RaidTrackerItem>> getDistinctRegularDrops()  {
        CompletableFuture<Map<Integer, RaidTrackerItem>> future = new CompletableFuture<>();

        clientThread.invokeLater(() -> {

            if (loaded) {
                HashSet<Integer> uniqueIDs = new HashSet<>();

                int clues = 0;

                for (RaidTracker RT : getFilteredRTList()) {
                    for (RaidTrackerItem item : RT.getLootList()) {
                        boolean addToSet = true;
                        for (RaidUniques unique : getUniquesList()) {
                            if (item.getId() == unique.getItemID()) {
                                addToSet = false;
                                break;
                            }
                        }
                        if (item.getName().toLowerCase().contains("clue")) {
                            addToSet = false;
                            clues++;
                        }
                        if (addToSet) {
                            uniqueIDs.add(item.id);
                        }
                    }
                }

                Map<Integer, RaidTrackerItem> m = new HashMap<>();

                for (Integer i : uniqueIDs) {
                    ItemComposition IC = itemManager.getItemComposition(i);

                    m.put(i, new RaidTrackerItem() {
                        {
                            name = IC.getName();
                            id = i;
                            quantity = 0;
                            price = itemManager.getItemPrice(i);
                        }
                    });

                }

                if (clues > 0) {
                    int finalClues = clues;
                    m.put(12073, new RaidTrackerItem() {
                        {
                            name = "Clue scroll (elite)";
                            id = 12073;
                            quantity = finalClues;
                            price = itemManager.getItemPrice(12073);
                        }
                    });
                }

                future.complete(m);
                return;
            }

            future.complete(new HashMap<>());

        });
        return future;
    }

    private ArrayList<RaidTracker> getFilteredRTList() {
        ArrayList<RaidTracker> tempRTList;

        if (!loaded) {
            return new ArrayList<>();
        }
        switch (RaidIndex)
        {
            case 0:
            {// Chambers
                if (cmFilter.equals("CM & Normal")) {
                    tempRTList = RTList;
                } else if (cmFilter.equals("CM Only")) {
                    tempRTList = RTList.stream().filter(RaidTracker::isChallengeMode)
                            .collect(Collectors.toCollection(ArrayList::new));
                } else {
                    tempRTList = RTList.stream().filter(RT -> !RT.isChallengeMode())
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                break;
            }
            case 1:
            { // Tob
                if (mvpFilter.equals("Both")) {
                    tempRTList = tobRTList;
                } else if (mvpFilter.equals("My MVP")) {
                    tempRTList = tobRTList.stream().filter(RaidTracker::isMvpInOwnName)
                            .collect(Collectors.toCollection(ArrayList::new));
                } else {
                    tempRTList = tobRTList.stream().filter(RT -> !RT.isMvpInOwnName())
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                if (cmFilter.equals("Both")) {
                    tempRTList = tobRTList;
                } else if (mvpFilter.equals("My MVP")) {
                    tempRTList = tobRTList.stream().filter(RaidTracker::isMvpInOwnName)
                            .collect(Collectors.toCollection(ArrayList::new));
                } else {
                    tempRTList = tobRTList.stream().filter(RT -> !RT.isMvpInOwnName())
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                break;
            }
            case 2:
            {// Toa  "Normal, Expert & Entry", "Entry Only", "Normal Only", "Expert Only"
                switch (cmIndex)
                {
                    case 1 :
                        tempRTList = toaRTList.stream().filter(RT -> (RT.getInvocation() < 150))
                                .collect(Collectors.toCollection(ArrayList::new));
                        break;
                    case 2 :
                        tempRTList = toaRTList.stream().filter(RT -> ((RT.getInvocation() > 150) && (RT.getInvocation() < 300)))
                                .collect(Collectors.toCollection(ArrayList::new));
                        break;
                    case 3 :
                        tempRTList = toaRTList.stream().filter(RT -> (RT.getInvocation() > 300))
                                .collect(Collectors.toCollection(ArrayList::new));
                        break;
                    default:
                        tempRTList = toaRTList;
                        break;
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + RaidIndex);
        }

        switch (teamSizeFilter) {
            case "Solo":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 1))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "Duo":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 2))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "Trio":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 3))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "4-man":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 4))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "5-man":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 5))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "6-man":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 6))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "7-man":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 7))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "8-man":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() == 8))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "8-10 Players":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() >= 8 && RT.getTeamSize() <= 10))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "11-14 Players":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() >= 11 && RT.getTeamSize() <= 14))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "15-24 Players":
				tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() >= 15 && RT.getTeamSize() <= 24))
					.collect(Collectors.toCollection(ArrayList::new));
			case "24+ Players":
                tempRTList = tempRTList.stream().filter(RT -> (RT.getTeamSize() >= 25))
                        .collect(Collectors.toCollection(ArrayList::new));
                break;
            default:
                //all sizes
        }

        //if people want to crash my plugin using a system year of before 1970, that's fine
        long now = System.currentTimeMillis();


        long last12Hours = now - 43200000L;
        long yesterday = now - 86400000L;
        long last3Days = now - 259200000L;
        long lastWeek = now - 604800000L;
        long lastMonth = now - 2629746000L;
        long last3Months = now - 7889400000L;
        long lastYear = now - 31536000000L;

        switch (dateFilter) {
            case "All Time":
                return tempRTList;
			case "12 Hours":
				return tempRTList.stream().filter(RT -> RT.getDate() > last12Hours)
					.collect(Collectors.toCollection(ArrayList::new));
            case "Today":
                return tempRTList.stream().filter(RT -> RT.getDate() > yesterday)
                        .collect(Collectors.toCollection(ArrayList::new));
			case "3 Days":
				return tempRTList.stream().filter(RT -> RT.getDate() > last3Days)
					.collect(Collectors.toCollection(ArrayList::new));
            case "Week":
                return tempRTList.stream().filter(RT -> RT.getDate() > lastWeek)
                        .collect(Collectors.toCollection(ArrayList::new));
            case "Month":
                return tempRTList.stream().filter(RT -> RT.getDate() > lastMonth)
                        .collect(Collectors.toCollection(ArrayList::new));
			case "3 Months":
				return tempRTList.stream().filter(RT -> RT.getDate() > last3Months)
					.collect(Collectors.toCollection(ArrayList::new));
            case "Year":
                return tempRTList.stream().filter(RT -> RT.getDate() > lastYear)
                        .collect(Collectors.toCollection(ArrayList::new));
            case "X Kills":
                ArrayList<RaidTracker> tempUniqueKills = getDistinctKills(tempRTList);
                ArrayList<RaidTracker> uniqueKills = new ArrayList<>(tempUniqueKills.subList(Math.max(tempUniqueKills.size() - config.lastXKills(), 0), tempUniqueKills.size()));

                return tempRTList.stream().filter(RT -> uniqueKills.stream()
                        .anyMatch(temp -> RT.getKillCountID().equals(temp.getKillCountID())))
                        .collect(Collectors.toCollection(ArrayList::new));
        }

        return tempRTList;
    }

    public EnumSet<RaidUniques> getUniquesList() {
        switch (RaidIndex) {
            case 0:
                return coxUniques;
            case 1:
                return tobUniques;
            case 2:
                return toaUniques;
            default:
                throw new IllegalStateException("Unexpected value: " + RaidIndex);
        }
    }
        public RaidUniques getByName(String name) {
        EnumSet<RaidUniques> uniquesList = getUniquesList();
        for (RaidUniques unique: uniquesList) {
            if (unique.getName().equalsIgnoreCase(name)) {
                return unique;
            }
        }
        //should never reach this
        return RaidUniques.OLMLET;
    }

    public ArrayList<RaidTracker> getDistinctKills(ArrayList<RaidTracker> tempRTList) {
        HashMap<String, RaidTracker> tempUUIDMap = new LinkedHashMap<>();

        for (RaidTracker RT : tempRTList) {
            tempUUIDMap.put(RT.getKillCountID(), RT);
        }

        return new ArrayList<>(tempUUIDMap.values());
    }

    private void clearData()
    {
        // Confirm delete action
        final int delete = JOptionPane.showConfirmDialog(this.getRootPane(), "<html>Are you sure you want to clear all data for this tab?<br/>There is no way to undo this action.</html>", "Warning", JOptionPane.YES_NO_OPTION);
        if (delete == JOptionPane.YES_OPTION)
        {
            if (!fw.delete(RaidIndex))
            {
                JOptionPane.showMessageDialog(this.getRootPane(), "Unable to clear stored data, please try again.");
                return;
            }

            loadRTList();
        }
    }


}

