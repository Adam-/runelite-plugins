package com.raidtracker.ui;

import com.raidtracker.RaidTracker;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;
import org.apache.commons.text.StringEscapeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
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
    public final RaidTracker raidTracker;
    private final ItemManager itemManager;
    private final RaidTrackerPanel raidTrackerPanel;
    private boolean locked = false;

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
        AsyncBufferedImage image = itemManager.getImage(getByName(raidTracker.getSpecialLoot()).getItemID(), 1, false);

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

        splitReceived.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            if (!locked) {
                int value = parse(splitReceived.getText());

                if (value != raidTracker.getLootSplitReceived() && value != -5) {
                    raidTracker.setLootSplitReceived(value);
                    if (raidTracker.isFreeForAll()) {
                        raidTracker.setSpecialLootValue(value);
                    } else {
                        raidTracker.setSpecialLootValue(value * raidTracker.getTeamSize());
                        setSplit();
                    }

                    splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(raidTracker.getLootSplitReceived())));

                    variablesChanged();
                }
            }
        });

        splitReceived.addActionListener(e -> {
            //format the number when losing focus
            splitReceived.setText(format(atleastZero(raidTracker.getLootSplitReceived())));
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

            locked = true;

            if (ffa.isSelected()) {
                setFFA();
            }
            else {
                setSplit();
            }
            splitReceived.setText(format(atleastZero(raidTracker.getLootSplitReceived())));
            splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(raidTracker.getLootSplitReceived())));

            variablesChanged();

            locked = false;
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

        SpinnerNumberModel model = new SpinnerNumberModel(Math.min(Math.max(1, raidTracker.getTeamSize()), 100), 1, 100, 1);
        JSpinner teamSize = new JSpinner(model);
        teamSize.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        Component editor = teamSize.getEditor();

        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) editor).getTextField();
        spinnerTextField.setColumns(2);
        teamSize.addChangeListener(e -> {
            locked = true;
            raidTracker.setTeamSize(Math.min(Math.max(1, Integer.parseInt(teamSize.getValue().toString())), 100));
            setSplit();
            splitReceived.setText(format(atleastZero(raidTracker.getLootSplitReceived())));
            splitReceived.setToolTipText(NumberFormat.getInstance().format(atleastZero(raidTracker.getLootSplitReceived())));

            variablesChanged();

            locked = false;
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
        boolean inOwnName = raidTracker.isSpecialLootInOwnName();

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
        boolean inOwnName = raidTracker.isSpecialLootInOwnName();

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
        return unescapeJavaString(s.replace("ï¿½", " ").replace("Â ", " "));
    }

    private RaidUniques getByName(String name) {
        EnumSet<RaidUniques> uniquesList = getUniquesList();
        for (RaidUniques unique: uniquesList) {
            if (unique.getName().toLowerCase().equals(name.toLowerCase())) {
                return unique;
            }
        }
        //should never reach this
        return RaidUniques.OLMLET;
    }

    EnumSet<RaidUniques> getUniquesList() {
        if (raidTrackerPanel.isTob()) {
            return raidTrackerPanel.getTobUniques();
        }
        return raidTrackerPanel.getCoxUniques();
    }

    //from stackoverflow
    public String unescapeJavaString(String st) {

        if (st == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

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
