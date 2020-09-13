package com.raidtracker.ui;

import com.raidtracker.RaidTracker;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;
import org.apache.commons.text.StringEscapeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SplitChanger extends JPanel {

    @Getter
    public final RaidTracker raidTracker;
    private final ItemManager itemManager;
    private final RaidTrackerPanel raidTrackerPanel;

    public SplitChanger(final ItemManager itemManager, final RaidTracker raidTracker, final RaidTrackerPanel raidTrackerPanel) {
        this.itemManager = itemManager;
        this.raidTracker = raidTracker;
        this.raidTrackerPanel = raidTrackerPanel;

        this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(new EmptyBorder(3,5,5,5));

        this.add(getImagePanel());
        this.add(getVarPanel());
    }

    private JPanel getImagePanel() {
        AsyncBufferedImage image = itemManager.getImage(RaidUniques.getByName(raidTracker.getSpecialLoot()).getItemID(), 1, false);

        JPanel iconWrapper = new JPanel();
        iconWrapper.setLayout(new BoxLayout(iconWrapper, BoxLayout.Y_AXIS));
        iconWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel icon = new JLabel();
        icon.setIcon(new ImageIcon(resizeImage(image)));
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setBorder(new EmptyBorder(0,0,0,0));
        icon.setToolTipText(raidTracker.getSpecialLoot());


        image.onLoaded(() ->
        {
            icon.setIcon(new ImageIcon(resizeImage(image)));
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

        JPanel splitReceivedWrapper = new JPanel();
        splitReceivedWrapper.setLayout(new GridLayout(0,2));
        JLabel splitReceivedLabel = textPanel("Split Amount: ");
        splitReceivedLabel.setHorizontalAlignment(SwingConstants.LEFT);
        splitReceivedWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JTextField splitReceived = getTextField();
        splitReceived.setText(format(atleastZero(raidTracker.getLootSplitReceived())));
        splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(raidTracker.getLootSplitReceived())));

        splitReceived.addActionListener(e -> {
            int value = parse(splitReceived.getText());

            if (value != raidTracker.getLootSplitReceived()) {
                raidTracker.setLootSplitReceived(value);
                if (raidTracker.isFreeForAll()) {
                    raidTracker.setSpecialLootValue(value);
                }
                else {
                    raidTracker.setSpecialLootValue(value * raidTracker.getTeamSize());
                    setSplit();
                }

                splitReceived.setText(format(atleastZero(raidTracker.getLootSplitReceived())));
                splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(raidTracker.getLootSplitReceived())));
                splitReceivedLabel.requestFocusInWindow();

                variablesChanged();
            }
        });


        splitReceivedWrapper.add(splitReceivedLabel);
        splitReceivedWrapper.add(splitReceived);

        JPanel ffaWrapper = new JPanel();
        ffaWrapper.setLayout(new GridLayout(0, 2));
        ffaWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JCheckBox ffa = new JCheckBox("FFA?");
        ffa.setBorder(new EmptyBorder(0,15,0,0));
        ffa.setSelected(raidTracker.isFreeForAll());
        ffa.addActionListener((e) -> {
            raidTracker.setFreeForAll(ffa.isSelected());

            if (ffa.isSelected()) {
                setFFA();
            }
            else {
                setSplit();
            }
            splitReceived.setText(format(atleastZero(raidTracker.getLootSplitReceived())));
            splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(raidTracker.getLootSplitReceived())));

            variablesChanged();
        });

        JPanel ReceivedWrapper = new JPanel();
        ReceivedWrapper.setLayout(new BoxLayout(ReceivedWrapper, BoxLayout.Y_AXIS));
        ReceivedWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel receivedBy = textPanel("received by: ");
        receivedBy.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());

        JLabel receiver = textPanel(fixSpaces(raidTracker.getSpecialLootReceiver()));
        receiver.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());

        ReceivedWrapper.add(receivedBy);
        ReceivedWrapper.add(receiver);

        ffaWrapper.add(ReceivedWrapper);
        ffaWrapper.add(ffa);

        JPanel teamSizeWrapper = new JPanel();
        teamSizeWrapper.setLayout(new GridLayout(0, 2));
        JLabel teamSizeLabel = textPanel("Team Size: ");
        teamSizeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        teamSizeWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        SpinnerNumberModel model = new SpinnerNumberModel(raidTracker.getTeamSize(), 1, 100, 1);
        JSpinner teamSize = new JSpinner(model);
        teamSize.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        Component editor = teamSize.getEditor();

        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
        spinnerTextField.setColumns(2);
        teamSize.addChangeListener(e -> {
            raidTracker.setTeamSize(Integer.parseInt(teamSize.getValue().toString()));
            setSplit();
            splitReceived.setText(format(atleastZero(raidTracker.getLootSplitReceived())));
            splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(raidTracker.getLootSplitReceived())));

            variablesChanged();
        });

        teamSizeWrapper.add(teamSizeLabel);
        teamSizeWrapper.add(teamSize);

        panel.add(ffaWrapper);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(splitReceivedWrapper);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
        panel.add(teamSizeWrapper);

        return panel;
    }

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

    private void setFFA() {
        boolean inOwnName = raidTracker.getLootList().get(0).getId() == RaidUniques.getByName(raidTracker.getSpecialLoot()).getItemID();

        if (inOwnName) {
            raidTracker.setLootSplitReceived(raidTracker.getSpecialLootValue());
            raidTracker.setLootSplitPaid(-1);
        }
        else {
            raidTracker.setLootSplitPaid(-1);
            raidTracker.setLootSplitReceived(-1);
        }
    }

    private void setSplit() {
        boolean inOwnName = raidTracker.getLootList().get(0).getId() == RaidUniques.getByName(raidTracker.getSpecialLoot()).getItemID();

        int splitSize = raidTracker.getSpecialLootValue() / raidTracker.getTeamSize();

        if (!raidTracker.isFreeForAll()) {
            if (inOwnName) {
                raidTracker.setLootSplitPaid(splitSize);
            } else {
                raidTracker.setLootSplitPaid(-1);
            }

            raidTracker.setLootSplitReceived(splitSize);
        }
    }

    private JLabel textPanel(String text) {
        return raidTrackerPanel.textPanel(text);
    }

    private BufferedImage resizeImage(BufferedImage before) {
        return raidTrackerPanel.resizeImage(before, 1.75, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
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
                return 0;
            }


            String substr = s.substring(0, s.length() - 1);

            if (isNumeric(substr)) {
                return (int) Math.round(Double.parseDouble(substr) * multiplier);
            }
        }
        else if (isNumeric(s)) {
            return (int) Math.round(Double.parseDouble(s));
        }
        return 0;
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
        return StringEscapeUtils.unescapeJava(StringEscapeUtils.escapeJava(s).replace("\\u00C2\\u00A0", "\\u00A0"));
    }


}
