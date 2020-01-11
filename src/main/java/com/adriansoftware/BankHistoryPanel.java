package com.adriansoftware;

import com.google.inject.Provides;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.QuantityFormatter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * A panel for showing a visualization of bank value over time.
 */
public class BankHistoryPanel extends PluginPanel
{
	@Inject
	@Setter
	private BankHistoryConfig config;
	@Inject
	@Setter
	private BankValueHistoryTracker tracker;
	private DatePickerPanel toDatePickerPanel;
	private DatePickerPanel startDatePickerPanel;
	private JFreeChart chart;
	private ChartPanel graphPanel = null;
	private SimpleTimeSelection timeSelection = SimpleTimeSelection.ALL;
	private Map<LocalDateTime, Long> currentDataRange;
	private JLabel changeLabel;
	private TimeSeriesCollection dataset;

	@Provides
	BankHistoryConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankHistoryConfig.class);
	}

	public void init()
	{
		init(false);
	}

	private void init(boolean isNewWindow)
	{
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		List<String> accounts = tracker.getAvailableUsers();

		if (accounts.isEmpty())
		{
			JLabel label = new JLabel("No account data found, log in to start tracking");
			add(label, BorderLayout.NORTH);
		}
		else
		{
			//wraps all user ui components to set a maximum height
			JPanel uiWrapperPanel = new JPanel();
			uiWrapperPanel.setLayout(new BoxLayout(uiWrapperPanel, BoxLayout.PAGE_AXIS));
			uiWrapperPanel.setMaximumSize(new Dimension(1280, 500));

			//account selection
			JComboBox<String> accountSelectionCombo = new JComboBox<>(new Vector<>(accounts));

			accountSelectionCombo.addItemListener((change) ->
			{
				updateDataset(change.getItem().toString());
			});

			String account = config.getDefaultAccount();

			//Simple Date selection
			JComboBox<String> simpleComboBox =
				new JComboBox<>(
					new Vector<>(
						Stream.of(SimpleTimeSelection
							.values())
							.map(SimpleTimeSelection::getFormattedName)
							.collect(Collectors.toList())));

			simpleComboBox
				.addItemListener(event ->
				{
					timeSelection = SimpleTimeSelection.of((String) event.getItem());
					updateDataset((String) accountSelectionCombo.getSelectedItem());
				});

			JButton showAdvancedButton = new JButton("Advanced");
			showAdvancedButton.setFocusPainted(false);

			JPanel simpleContainer = new JPanel();
			simpleContainer.setLayout(new BoxLayout(simpleContainer, BoxLayout.LINE_AXIS));
			simpleContainer.add(simpleComboBox);

			//Date picker
			LocalDate today = LocalDate.now();
			LocalDateTime startOfDay = today.atStartOfDay();
			Consumer<Void> callback = (t) ->
			{
				updateDataset((String) accountSelectionCombo.getSelectedItem());
			};

			startDatePickerPanel = new DatePickerPanel(startOfDay, "Start Date", callback);
			toDatePickerPanel = new DatePickerPanel(startOfDay.plusDays(1), "End Date", callback);
			startDatePickerPanel.init();
			toDatePickerPanel.init();

			JPanel datePickerContainer = new JPanel();
			datePickerContainer.setLayout(new BoxLayout(datePickerContainer, BoxLayout.PAGE_AXIS));
			datePickerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			datePickerContainer.add(startDatePickerPanel);
			datePickerContainer.add(Box.createRigidArea(new Dimension(0, 10)));
			datePickerContainer.add(toDatePickerPanel);
			datePickerContainer.setVisible(false);
			showAdvancedButton.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					boolean visible = datePickerContainer.isVisible();
					datePickerContainer.setVisible(!visible);
					simpleContainer.setVisible(visible);
					if (visible)
					{
						showAdvancedButton.setText("Advanced");
						timeSelection = null;
					}
					else
					{
						showAdvancedButton.setText("Simple");
					}
				}
			});
			//Advanced button
			JPanel advancedContainer = new JPanel();
			advancedContainer.setLayout(new BorderLayout());
			advancedContainer.add(showAdvancedButton, BorderLayout.CENTER);
			advancedContainer.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);
			advancedContainer.setMaximumSize(new Dimension(1280, 45));

			//Open in new window
			JPanel openInNewWindowContainer = new JPanel();
			openInNewWindowContainer.setLayout(new BorderLayout());
			openInNewWindowContainer.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);
			JButton newWindowButton = new JButton("Open In New Window");
			newWindowButton.setMaximumSize(new Dimension(100, 30));
			newWindowButton.setFocusPainted(false);
			newWindowButton.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent e)
				{
					JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(BankHistoryPanel.this);
					JDialog dialog = new JDialog(frame, "Bank History", false);
					BankHistoryPanel panel = new BankHistoryPanel();
					panel.setTracker(tracker);
					panel.setConfig(config);
					panel.init(true);
					dialog.setPreferredSize(new Dimension(500, 500));

					dialog.getContentPane().add(panel, BorderLayout.CENTER);

					dialog.setLocationRelativeTo(null);
					dialog.pack();
					dialog.setVisible(true);
				}
			});

			openInNewWindowContainer.add(newWindowButton, BorderLayout.CENTER);

			//render
			add(uiWrapperPanel);

			//add buttons/user interaction components here
			uiWrapperPanel.add(accountSelectionCombo);
			uiWrapperPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			uiWrapperPanel.add(advancedContainer);
			uiWrapperPanel.add(simpleContainer);
			uiWrapperPanel.add(datePickerContainer);
			uiWrapperPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			add(Box.createRigidArea(new Dimension(0, 5)));

			loadGraph(account.isEmpty() ? accounts.get(0) : account);

			// Increase/decrease panel (change) panel
			// Done after the graph is loaded so we have the data points available
			JPanel changePanel = new JPanel();
			changePanel.setLayout(new BorderLayout());
			changeLabel = getChangeLabelValue();
			changeLabel.setHorizontalAlignment(JLabel.CENTER);
			changePanel.add(changeLabel, BorderLayout.CENTER);
			uiWrapperPanel.add(changePanel);

			if (!isNewWindow)
			{
				add(Box.createRigidArea(new Dimension(0, 10)));
				add(openInNewWindowContainer);
			}
		}
	}

	private void updateDataset(String account)
	{
		dataset = getDataset(account);
		setChangeLabel();

		chart.getXYPlot().setDataset(dataset);
	}

	private void setChangeLabel()
	{
		JLabel result = getChangeLabelValue();
		if (changeLabel != null)
		{
			changeLabel.setForeground(result.getForeground());
			changeLabel.setText(result.getText());
		}
		else
		{
			changeLabel = result;
		}
	}

	private JLabel getChangeLabelValue()
	{
		JLabel result = new JLabel();
		result.setForeground(ColorScheme.BRAND_ORANGE);
		if (currentDataRange == null || currentDataRange.isEmpty())
		{
			result.setText("No Data available for selected range");
			return result;
		}

		LocalDateTime start = Collections.min(currentDataRange.keySet());
		LocalDateTime end = Collections.max(currentDataRange.keySet());

		long startValue = currentDataRange.get(start);
		long endValue = currentDataRange.get(end);
		long finishValue = endValue - startValue;
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setRoundingMode(RoundingMode.DOWN);

		String percentChange = decimalFormat.format((endValue - startValue) / (double) Math.abs(startValue) * 100);
		String formattedValue = QuantityFormatter.quantityToStackSize(finishValue);
		String formattedString = "No Change";
		if (finishValue < 0)
		{
			formattedString = formattedValue + " (- " + percentChange + "%)";
			result.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		}
		else if (finishValue > 0)
		{
			formattedString = formattedValue + " (+ " + percentChange + "%)";
			result.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		}

		result.setText(formattedString);
		return result;
	}

	public void loadGraph(String username)
	{
		dataset = getDataset(username);
		ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());
		chart = ChartFactory.createTimeSeriesChart(
			null,
			"Date/time",
			"Bank Value (mil)",
			dataset,
			false,
			true,
			false);

		XYPlot plot = chart.getXYPlot();
		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer)
		{
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);
		}
		plot.setBackgroundPaint(ColorScheme.DARKER_GRAY_HOVER_COLOR);
		chart.setBackgroundPaint(ColorScheme.DARK_GRAY_HOVER_COLOR);
		if (graphPanel != null)
		{
			remove(graphPanel);
		}

		graphPanel = new ChartPanel(chart);
		graphPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		graphPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		add(graphPanel, BorderLayout.SOUTH);
		revalidate();
		repaint();
	}

	private TimeSeriesCollection getDataset(String username)
	{
		BankValueHistoryContainer container = tracker.getBankValueHistory(username);
		TimeSeriesCollection collection = new TimeSeriesCollection();

		if (container == null)
		{
			return collection;
		}

		TimeSeries timeSeries = new TimeSeries("bankValueVsTime");
		Map<LocalDateTime, Long> currentDataRange = new HashMap<>();
		for (Map.Entry<LocalDateTime, Long> entry : container.getPricesMap().entrySet())
		{
			long price = entry.getValue();

			LocalDateTime entryDateTime = entry.getKey();
			LocalDateTime start;
			LocalDateTime end;

			if (timeSelection != null)
			{
				start = SimpleTimeSelection.getPastTime(timeSelection);
				end = LocalDateTime.now();
			}
			else
			{
				start = startDatePickerPanel.getLocalDateTime();
				end = toDatePickerPanel.getLocalDateTime();
			}

			if (timeSelection == SimpleTimeSelection.ALL ||
				end != null && start != null && onOrAfter(entryDateTime, start) && onOrBefore(entryDateTime, end))
			{
				currentDataRange.put(entryDateTime.atZone(ZoneId.systemDefault()).toLocalDateTime(), price);
				timeSeries.add(new Millisecond(Date.from(entryDateTime.atZone(ZoneId.systemDefault()).toInstant())), price);
			}
		}

		this.currentDataRange = currentDataRange;
		collection.addSeries(timeSeries);

		return collection;
	}


	private boolean onOrBefore(LocalDateTime first, LocalDateTime second)
	{
		return first.compareTo(second) == 0 || first.isBefore(second);
	}

	private boolean onOrAfter(LocalDateTime first, LocalDateTime second)
	{
		return first.compareTo(second) == 0 || first.isAfter(second);
	}

	@RequiredArgsConstructor
	private enum SimpleTimeSelection
	{
		ALL("All"),
		TODAY("Today"),
		HOUR("1 hour"),
		TWO_HOURS("2 Hours"),
		THREE_HOURS("4 Hours"),
		EIGHT_HOURS("8 Hours"),
		TWENTY_FOUR_HOURS("24 Hours"),
		WEEK("Week"),
		MONTH("Month"),
		SIX_MONTHS("6 Months"),
		YEAR("Year");

		@Getter
		private final String formattedName;

		/**
		 * Get a date/time that is {@link TimeSeriesCollection} in the past from now.
		 *
		 * @param timeSelection human-readable unit of time to go back to.
		 * @return
		 */
		static LocalDateTime getPastTime(SimpleTimeSelection timeSelection)
		{
			LocalDateTime dateTime = LocalDateTime.now();

			switch (timeSelection)
			{
				case ALL:
					return null;
				case TODAY:
					LocalDate date = LocalDate.now();
					return date.atStartOfDay();
				case HOUR:
					return dateTime.minusHours(1);
				case TWO_HOURS:
					return dateTime.minusHours(2);
				case THREE_HOURS:
					return dateTime.minusHours(3);
				case EIGHT_HOURS:
					return dateTime.minusHours(8);
				case TWENTY_FOUR_HOURS:
					return dateTime.minusDays(1);
				case WEEK:
					return dateTime.minusWeeks(1);
				case MONTH:
					return dateTime.minusMonths(1);
				case SIX_MONTHS:
					return dateTime.minusMonths(6);
				case YEAR:
					return dateTime.minusYears(1);
				default:
					throw new IllegalArgumentException("Unable to get past time");
			}
		}

		public static SimpleTimeSelection of(String item)
		{
			for (SimpleTimeSelection t : SimpleTimeSelection.values())
			{
				if (t.getFormattedName().equals(item))
				{
					return t;
				}
			}

			throw new IllegalArgumentException("no SimpleTimeSelection of " + item + " found.");
		}
	}
}
