package com.adriansoftware;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.sourceforge.jdatepicker.impl.DateComponentFormatter;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import org.apache.commons.lang3.StringUtils;

public class DatePickerPanel extends JPanel
{
	private final String labelText;
	private final LocalDateTime localDateTime;
	private final Consumer<Void> callback;
	private UtilDateModel model;
	private JComboBox<String> hour;
	private JComboBox<String> minute;
	private JComboBox<String> amPm;

	public DatePickerPanel(LocalDateTime localDateTime, String labelText, Consumer<Void> callback)
	{
		super();
		this.localDateTime = localDateTime;
		this.labelText = labelText;
		this.callback = callback;
	}

	public void init()
	{
		setLayout(new BorderLayout());
		model = new UtilDateModel(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");

		JDatePanelImpl datePanel = new JDatePanelImpl(model);
		JDatePickerImpl picker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

		picker.setTextEditable(true);

		hour = new JComboBox<>(getArrayOfIntegers(1, 12));
		hour.setSelectedItem("12");
		minute = new JComboBox<>(getArrayOfIntegers(0, 60));
		minute.setSelectedItem("00");
		amPm = new JComboBox<>(new Vector<>(Arrays.asList("AM", "PM")));
		amPm.setSelectedItem("AM");

		addListeners();

		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.LINE_AXIS));
		timePanel.add(hour);
		timePanel.add(Box.createHorizontalGlue());
		timePanel.add(minute);
		timePanel.add(Box.createHorizontalGlue());
		timePanel.add(amPm);

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.PAGE_AXIS));
		containerPanel.add(picker);
		containerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		containerPanel.add(timePanel);

		add(containerPanel);
		setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
			BorderFactory.createTitledBorder(labelText)));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
	}

	private void addListeners()
	{
		model.addChangeListener((event) -> callback.accept(null));
		hour.addItemListener((event) -> callback.accept(null));
		minute.addItemListener((event) -> callback.accept(null));
		amPm.addItemListener((event) -> callback.accept(null));
	}

	public LocalDateTime getLocalDateTime()
	{
		if (hour == null || amPm == null || model == null || minute == null)
		{
			return null;
		}

		int h = Integer.parseInt(String.valueOf(hour.getSelectedItem()).replaceAll("^0+", "")) - 1;

		if (amPm.getSelectedItem() == "PM")
		{
			h += 12;
		}
		return LocalDateTime.of(model.getYear(),
			model.getMonth() + 1,
			model.getDay(),
			h,
			Integer.parseInt((String) minute.getSelectedItem()),
			0,
			0);
	}

	private Vector<String> getArrayOfIntegers(int start, int end)
	{
		return Stream.iterate(start, n -> n + 1)
			.limit(end)
			.map(v -> getFormattedTimeString(String.valueOf(v)))
			.collect(Collectors.toCollection(Vector::new));
	}

	private String getFormattedTimeString(String s)
	{
		return StringUtils.leftPad(s, 2, '0');
	}
}
