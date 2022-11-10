package com.raidtracker.ui;

import com.google.inject.Inject;
import com.raidtracker.RaidTracker;
import com.raidtracker.RaidTrackerPlugin;
import com.raidtracker.utils.UniqueDrop;
import com.raidtracker.utils.uiUtils;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SplitChanger extends JPanel {

    @Getter
    public final UniqueDrop drop;
    private uiUtils uiUtils = new uiUtils();
    private final ItemManager itemManager;
    private final RaidTrackerPanel raidTrackerPanel;
    @Inject
    private static RaidTrackerPlugin RaidTrackerPlugin;

    private final  RaidTracker raidTracker;
    private final String profileKey;
    private final String name;
    private boolean locked = false;

    public SplitChanger(final ItemManager itemManager, final UniqueDrop drop, final RaidTrackerPanel raidTrackerPanel, RaidTracker raidTracker, String profileKey, String name) {
        this.raidTracker = raidTracker;
        this.itemManager = itemManager;
        this.drop = drop;
        this.raidTrackerPanel = raidTrackerPanel;
        this.profileKey = profileKey;
        this.name = name;
        this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(new EmptyBorder(3,5,5,5));

        this.add(getImagePanel());
        this.add(getVarPanel());
        this.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private JPanel getImagePanel() {
        String UniqueName = drop.getDrop();
        AsyncBufferedImage image = itemManager.getImage(getByName(UniqueName).getItemID(), 1, false);

        JPanel iconWrapper = new JPanel();
        iconWrapper.setLayout(new BoxLayout(iconWrapper, BoxLayout.Y_AXIS));
        iconWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel icon = new JLabel();
        icon.setIcon(new ImageIcon(uiUtils.resizeImage(image)));
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setBorder(new EmptyBorder(0,0,0,0));
        icon.setToolTipText(UniqueName);


        image.onLoaded(() ->
        {
            icon.setIcon(new ImageIcon(uiUtils.resizeImage(image)));
            icon.revalidate();
            icon.repaint();
        });

        JLabel date = textPanel(getDateText());
        date.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());

        iconWrapper.add(date);
        iconWrapper.add(Box.createRigidArea(new Dimension(0, 10)));
        iconWrapper.add(icon);
        iconWrapper.add(Box.createRigidArea(new Dimension(0, 10)));

        return iconWrapper;
    }

    private JPanel getVarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5,5,5,0));
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        boolean isOwnDrop = drop.getUsername().equalsIgnoreCase(profileKey);
        JPanel splitReceivedWrapper = new JPanel();
        splitReceivedWrapper.setLayout(new GridLayout(0,2));
        JLabel splitReceivedLabel = textPanel("Split Amount: ");
        splitReceivedLabel.setHorizontalAlignment(SwingConstants.LEFT);
        splitReceivedWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel itempriceWrapper = new JPanel();
        itempriceWrapper.setLayout(new GridLayout(0, 2));
        JLabel itempriceLabel = textPanel("Price : ");
        itempriceLabel.setHorizontalAlignment(SwingConstants.LEFT);
        itempriceWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JTextField splitReceived = getTextField();
        splitReceived.setText(format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
        splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
        JTextField itemPrice = getTextField();
        itemPrice.setText(format(atleastZero(drop.getValue())));
        itemPrice.setToolTipText(NumberFormat.getInstance().format(atleastZero(drop.getValue())));


        splitReceivedWrapper.add(splitReceivedLabel);
        splitReceivedWrapper.add(splitReceived);
        itempriceWrapper.add(itempriceLabel);
        itempriceWrapper.add(itemPrice);
        JPanel ffaWrapper = new JPanel();
        ffaWrapper.setLayout(new GridLayout(0, 2));
        ffaWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JCheckBox ffa = new JCheckBox("FFA?");
        ffa.setBorder(new EmptyBorder(0,15,0,0));
        ffa.setSelected(drop.isFfa());


        JPanel ReceivedWrapper = new JPanel();
        ReceivedWrapper.setLayout(new BoxLayout(ReceivedWrapper, BoxLayout.Y_AXIS));
        ReceivedWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel receivedBy = textPanel("received by: ");
        receivedBy.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());

        JLabel receiver = textPanel(fixSpaces(drop.getUsername().equalsIgnoreCase(profileKey) ? name : drop.getUsername()));
        receiver.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());

        ReceivedWrapper.add(receivedBy);
        ReceivedWrapper.add(receiver);

        ffaWrapper.add(ReceivedWrapper);
        ffaWrapper.add(ffa);

        JPanel teamSizeWrapper = new JPanel();
        teamSizeWrapper.setLayout(new GridLayout(0, 2));
        JLabel teamSizeLabel = textPanel("Splits : ");
        teamSizeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        teamSizeWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        SpinnerNumberModel model = new SpinnerNumberModel(Math.min(Math.max(1, drop.getSCount()), 100), 1, 100, 1);
        JSpinner teamSize = new JSpinner(model);
        teamSize.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        Component editor = teamSize.getEditor();

        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
        spinnerTextField.setColumns(2);

        teamSize.addChangeListener(e -> SplitComponentUpdated(itemPrice,splitReceived,ffa,teamSize));
        ffa.addActionListener((e) -> {SplitComponentUpdated(itemPrice,splitReceived,ffa,teamSize);});
        itemPrice.getDocument().addDocumentListener((SimpleDocumentListener) e -> {{SplitComponentUpdated(itemPrice,splitReceived,ffa,teamSize);};});
        splitReceived.getDocument().addDocumentListener((SimpleDocumentListener) e -> {{SplitComponentUpdated(itemPrice,splitReceived,ffa,teamSize);};});
        splitReceived.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {};
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    SplitComponentUpdated(itemPrice,splitReceived,ffa,teamSize, true);
                    e.consume();
                }
            };
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        teamSizeWrapper.add(teamSizeLabel);
        teamSizeWrapper.add(teamSize);


        panel.add(ffaWrapper);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(splitReceivedWrapper);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(itempriceWrapper);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(teamSizeWrapper);
        return panel;
    }

    /**
     * Fired Whenever someone changes a selection on the split changer, Split/Price/ffa/teamsize
     * @param itemPrice
     * @param splitReceived
     * @param ffa
     *
     */
    private void SplitComponentUpdated(JTextField itemPrice, JTextField splitReceived, JCheckBox ffa, JSpinner teamSize, boolean updateall)
    {
        if (locked) return;
        locked = true;
        boolean editPrice = itemPrice.hasFocus();
        boolean editSplit = splitReceived.hasFocus();

        boolean isOwnDrop = drop.getUsername().equalsIgnoreCase(profileKey);

        drop.setFfa(ffa.isSelected());
        drop.setSCount(Math.min(Math.max(1, Integer.parseInt(teamSize.getValue().toString())), 100));

        System.out.println(editPrice);
        if (updateall)
        {
            int value = drop.getValue();
            splitReceived.setText(format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
            splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
            itemPrice.setText(format(atleastZero(value)));
            itemPrice.setToolTipText(NumberFormat.getInstance().format(atleastZero(value)));
        }
        if (editPrice)
        {
            int value = parse(itemPrice.getText());
            System.out.println(value);
            drop.setValue(value);
            splitReceived.setText(format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
            splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
        }else if (editSplit)
        {
            if (!drop.isFfa())
            {
                int value = parse(splitReceived.getText());
                value = ffa.isSelected() ? value : value * drop.getSCount();
                drop.setValue(value);
                System.out.println(value);
                itemPrice.setText(format(atleastZero(value)));
                itemPrice.setToolTipText(NumberFormat.getInstance().format(atleastZero(value)));
            };
        } else
        {
            int value = drop.getValue();
            splitReceived.setText(format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
            splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(drop.isFfa() ? (isOwnDrop ? drop.getValue() : 0) : drop.getValue() / drop.getSCount())));
        };
        variablesChanged();
        locked = false;
    };
    private void SplitComponentUpdated(JTextField itemPrice, JTextField splitReceived, JCheckBox ffa, JSpinner teamSize)
    {
        SplitComponentUpdated(itemPrice, splitReceived, ffa, teamSize, false);
    };

    private JTextField getTextField()
    {
        JTextField textField = new JTextField();

        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textField.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        textField.setAlignmentX(RIGHT_ALIGNMENT);

        return textField;
    }

    private int atleastZero(int maybeLessThanZero) {
        return Math.max(maybeLessThanZero, 0);
    }

    private void variablesChanged() {
        raidTrackerPanel.setUpdateButton(true);
    }

    private JLabel textPanel(String text) {
        return raidTrackerPanel.textPanel(text, SwingConstants.CENTER, SwingConstants.CENTER);
    }
    private String getDateText() {
        String dateText;

        LocalDate date = Instant.ofEpochMilli(raidTracker.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        LocalDate lastMonth = today.minusDays(30);
        LocalDate lastYear = today.minusDays(365);


        if (date.equals(today)) {
            dateText = "today";
        }
        else if (date.equals(yesterday)) {
            dateText = "yesterday";
        }
        else if (date.isAfter(lastWeek)) {
            dateText = "last week";
        }
        else if (date.isAfter(lastMonth)) {
            dateText = "last month";
        }
        else if (date.isAfter(lastYear)) {
            dateText = "last year";
        }
        else {
            dateText = "a long time ago";
        }

        return dateText;
    }

    //yoinked from stackoverflow
    private static final NavigableMap<Long, String> suffixes = new TreeMap<> ();
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

    public static int parse (String s) {
        if (s == null || s.length() == 0) {
            return -5;
        }
        char c = s.charAt(s.length() - 1);
        if (Character.isLetter(c)) {
            int multiplier;

            if (c == 'k') {
                multiplier = 1000;
            }
            else if (c == 'm') {
                multiplier = 1000000;
            }
            else if (c == 'b') {
                multiplier = 1000000000;
            }
            else {
                return -5;
            }


            String substr = s.substring(0, s.length() - 1);

            if (isNumeric(substr)) {
                return (int) Math.round(Double.parseDouble(substr) * multiplier);
            }
        }
        else if (isNumeric(s)) {
            return (int) Math.round(Double.parseDouble(s));
        }
        return -5;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public String fixSpaces(String s) {
        //replace null characters with spaces
        return uiUtils.unescapeJavaString(s.replace("ï¿½", " ").replace("Â ", " "));
    }

    private RaidUniques getByName(String name) {
        EnumSet<RaidUniques> uniquesList = getUniquesList();

        for (RaidUniques unique: uniquesList) {
            if (unique.getName().equalsIgnoreCase(name)) {
                return unique;
            }
        }
        //should never reach this
        return RaidUniques.OLMLET;
    }

    EnumSet<RaidUniques> getUniquesList() {
        switch (raidTrackerPanel.getRaidIndex())
        {
            case 0 : return raidTrackerPanel.getCoxUniques();
            case 1 : return raidTrackerPanel.getTobUniques();
            case 2 : return raidTrackerPanel.getToaUniques();
        };
        return raidTrackerPanel.getCoxUniques();
    }

    RaidTracker getRaidTracker()
    {
        return this.raidTracker;
    };

    //also from stackoverflow
    @FunctionalInterface
    public interface SimpleDocumentListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }
}
