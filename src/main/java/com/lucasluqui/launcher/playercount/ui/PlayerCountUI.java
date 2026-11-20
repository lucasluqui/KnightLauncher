package com.lucasluqui.launcher.playercount.ui;

import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LauncherContext;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.playercount.ui.handler.PlayerCountEventHandler;
import com.lucasluqui.launcher.ui.BaseUI;
import com.lucasluqui.swing.Card;
import com.lucasluqui.swing.CardTable;
import com.lucasluqui.swing.FontIcon;
import com.lucasluqui.swing.FractionalText;
import com.lucasluqui.swing.LoadingIcon;
import com.lucasluqui.swing.Scale;
import com.lucasluqui.swing.ScrollablePanel;
import com.lucasluqui.swing.SmoothScrollPane;
import com.lucasluqui.swing.TimeSeriesChart;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import jiconfont.icons.font_awesome.FontAwesome;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayerCountUI extends BaseUI
{
  @Inject
  public PlayerCountUI ()
  {
    super(800, 455, false);
  }

  public void init ()
  {
    super.init();
    compose();
    initFinished();
  }

  public void initFinished ()
  {
    super.initFinished();
  }

  @Override
  public void loadOnline ()
  {
    // empty. the history is loaded every time the UI is opened instead.
  }

  public void open ()
  {
    _ctx.getApp().showUI(PlayerCountUI.class);
    this.eventHandler.loadHistory();
  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.main"));
    guiFrame.setBounds(Scale.rect(100, 100, this.width, this.height));
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);
    panel = (JPanel) guiFrame.getContentPane();

    loadingLabel = new JLabel();
    loadingLabel.setBounds(Scale.rect(0, 0, this.width, this.height));
    loadingLabel.setFont(Fonts.getFont("default", 14.0f, Font.BOLD));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loadingLabel.setVerticalAlignment(SwingConstants.CENTER);
    panel.add(loadingLabel);
    panel.setComponentZOrder(loadingLabel, 0);

    titleLabel = new JLabel(_localeManager.getValue("m.player_count_title"));
    titleLabel.setFont(Fonts.getFont("default", 40.0f, Font.BOLD));
    titleLabel.setBorder(new EmptyBorder(Scale.px(4), 0, Scale.px(2), 0));

    playerCountList = new ScrollablePanel();
    playerCountList.setLayout(new BoxLayout(playerCountList, BoxLayout.Y_AXIS));
    playerCountList.setOpaque(false);
    playerCountList.setBorder(new EmptyBorder(0, 0, Scale.px(15), Scale.px(15)));

    playerCountListScroll = new SmoothScrollPane(playerCountList);
    playerCountListScroll.setBounds(Scale.rect(25, 0, this.width - 35, this.height - 10));
    playerCountListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    playerCountListScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    playerCountListScroll.setBorder(null);
    playerCountListScroll.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    playerCountListScroll.setOpaque(false);
    playerCountListScroll.getViewport().setOpaque(false);
    playerCountListScroll.getVerticalScrollBar().setOpaque(false);
    panel.add(playerCountListScroll);
  }

  public void showLoading ()
  {
    // The title scrolls along with everything else.
    playerCountList.removeAll();
    playerCountList.add(titleLabel);
    playerCountList.repaint();
    loadingLabel.setIcon(new LoadingIcon());
    loadingLabel.setText(_localeManager.getValue("m.loading"));
    loadingLabel.setVisible(true);
  }

  public void showHistory (JSONObject history)
  {
    if (history == null) {
      loadingLabel.setIcon(null);
      loadingLabel.setText(_localeManager.getValue("m.player_count_unavailable"));
      return;
    }

    // Anything from before, whether the loading screen or an older history, makes way.
    // Refreshes keep their place.
    int scroll = playerCountListScroll.getVerticalScrollBar().getValue();
    playerCountList.removeAll();
    playerCountList.add(titleLabel);

    long day = TimeUnit.DAYS.toMillis(1);
    long now = System.currentTimeMillis();
    LocalDate today = LocalDate.now();
    long todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    long monthStart = today.minusDays(29).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

    JSONArray dayCounts = history.getJSONArray("day");
    int dayPeak = -1;
    for (int i = 0; i < dayCounts.length(); i++) dayPeak = Math.max(dayPeak, dayCounts.getJSONArray(i).getInt(1));

    // Long enough to need wrapping, which a label can't do.
    // Its height depends on how wide it is, so it's measured
    // against the list's width instead of whatever width it had before.
    JTextArea noteLabel = new FractionalText.Area(_localeManager.getValue("m.player_count_note",
      String.valueOf(TimeUnit.MILLISECONDS.toMinutes(PlayerCountEventHandler.REFRESH_INTERVAL))))
    {
      @Override
      public Dimension getPreferredSize ()
      {
        Container parent = getParent();
        if (parent != null && parent.getWidth() > 0) {
          Insets insets = parent.getInsets();
          setSize(parent.getWidth() - insets.left - insets.right, Short.MAX_VALUE);
        }
        return super.getPreferredSize();
      }

      @Override
      public Dimension getMaximumSize ()
      {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
      }
    };
    noteLabel.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    noteLabel.setForeground(CustomColors.INTERFACE_DEFAULT);
    noteLabel.setLineWrap(true);
    noteLabel.setWrapStyleWord(true);
    noteLabel.setEditable(false);
    noteLabel.setFocusable(false);
    noteLabel.setOpaque(false);
    noteLabel.setBorder(new EmptyBorder(0, 0, Scale.px(3), 0));
    noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    playerCountList.add(noteLabel);
    playerCountList.add(Box.createVerticalStrut(Scale.px(25)));

    JPanel stats = new JPanel(new GridLayout(1, 3, Scale.px(15), 0));
    stats.setOpaque(false);
    stats.setAlignmentX(Component.LEFT_ALIGNMENT);
    stats.add(createStat("m.player_count_current", history.optInt("playerCount", -1), history.optInt("trend", 0)));
    stats.add(createStat("m.player_count_day_peak", dayPeak, 0));
    stats.add(createStat("m.player_count_all_time_peak", history.optInt("allTimePeak", -1), 0));
    stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, stats.getPreferredSize().height));
    playerCountList.add(stats);
    playerCountList.add(Box.createVerticalStrut(Scale.px(15)));

    JSONObject usualDayPeak = history.optJSONObject("usualDayPeak");
    JSONObject usualWeekPeak = history.optJSONObject("usualWeekPeak");
    JPanel usualPeaks = new JPanel(new GridLayout(1, 2, Scale.px(15), 0));
    usualPeaks.setOpaque(false);
    usualPeaks.setAlignmentX(Component.LEFT_ALIGNMENT);
    usualPeaks.add(createStat("m.player_count_usual_day_peak",
      usualDayPeak == null ? "-" : formatHour(usualDayPeak.getInt("hour")), 0));
    usualPeaks.add(createStat("m.player_count_usual_week_peak", usualWeekPeak == null ? "-" :
      DayOfWeek.of(usualWeekPeak.getInt("day")).getDisplayName(TextStyle.FULL, Locale.getDefault())
        + " " + formatHour(usualWeekPeak.getInt("hour")), 0));
    usualPeaks.setMaximumSize(new Dimension(Integer.MAX_VALUE, usualPeaks.getPreferredSize().height));
    playerCountList.add(usualPeaks);
    playerCountList.add(Box.createVerticalStrut(Scale.px(15)));
    playerCountList.add(
      createChart(
        "m.player_count_day",
        dayCounts,
        1,
        now - day,
        now,
        TimeUnit.HOURS.toMillis(3),
        "HH:mm"
      )
    );
    playerCountList.add(Box.createVerticalStrut(Scale.px(15)));
    playerCountList.add(
      createChart(
        "m.player_count_week",
        history.getJSONArray("week"),
        2,
        now - 7 * day,
        now,
        day,
        "EEE d"
      )
    );
    playerCountList.add(Box.createVerticalStrut(Scale.px(15)));
    playerCountList.add(
      createChart(
        "m.player_count_month",
        history.getJSONArray("month"),
        7,
        monthStart,
        todayStart,
        5 * day,
        "MMM d"
      )
    );
    playerCountList.add(Box.createVerticalStrut(Scale.px(15)));
    playerCountList.add(createMonthTable(history.getJSONArray("months")));

    playerCountList.revalidate();
    playerCountList.repaint();
    loadingLabel.setVisible(false);
    SwingUtilities.invokeLater(() -> playerCountListScroll.getVerticalScrollBar().setValue(scroll));
  }

  /**
   * Whether this is the UI being looked at right now.
   */
  public boolean isShowing ()
  {
    return panel != null && panel.isShowing();
  }

  private static String formatHour (int hour)
  {
    return LocalTime.of(hour, 0).format(DateTimeFormatter.ofPattern("HH:mm"));
  }

  /**
   * An arrow for the player count going up or down, or null when it's doing neither.
   *
   * @param size In unscaled pixels.
   */
  public static Icon trendIcon (int trend, float size)
  {
    if (trend == 0) return null;
    return FontIcon.of(trend > 0 ? FontAwesome.ARROW_UP : FontAwesome.ARROW_DOWN, Scale.px(size),
      trend > 0 ? CustomColors.LIGHT_GREEN : CustomColors.LIGHT_RED);
  }

  public static String trendText (LocaleManager localeManager, int trend)
  {
    if (trend == 0) return null;
    return localeManager.getValue(trend > 0 ? "m.player_count_trend_up" : "m.player_count_trend_down");
  }

  /**
   * @param trend Whether the count is going up (1), down (-1) or neither (0).
   */
  private Card createStat (String captionKey, int count, int trend)
  {
    return createStat(captionKey, count < 0 ? "-" : String.valueOf(count), count < 0 ? 0 : trend);
  }

  private Card createStat (String captionKey, String value, int trend)
  {
    JLabel countLabel = new JLabel(value);
    countLabel.setFont(Fonts.getFont("default", 30.0f, Font.BOLD));
    countLabel.setHorizontalTextPosition(SwingConstants.LEADING);
    countLabel.setIconTextGap(Scale.px(10));
    countLabel.setIcon(trendIcon(trend, 18));
    countLabel.setToolTipText(trendText(_localeManager, trend));

    JLabel captionLabel = new JLabel(_localeManager.getValue(captionKey));
    captionLabel.setFont(Fonts.getFont("default", 12.0f, Font.PLAIN));
    captionLabel.setForeground(CustomColors.INTERFACE_DEFAULT);

    Card stat = new Card(new BorderLayout());
    stat.setBorder(new EmptyBorder(Scale.px(14), Scale.px(20), Scale.px(14), Scale.px(20)));
    stat.add(countLabel, BorderLayout.CENTER);
    stat.add(captionLabel, BorderLayout.SOUTH);
    return stat;
  }

  /**
   * A chart of player counts over time.
   *
   * @param counts [time, count] pairs, in order.
   * @param minCounts How many counts need to fall between {@code from} and {@code to} for the chart to say much.
   * @param tickStep Time between labels on the time axis.
   */
  private TimeSeriesChart createChart (String titleKey, JSONArray counts, int minCounts,
                                       long from, long to, long tickStep, String tickPattern)
  {
    TimeSeriesChart chart = new TimeSeriesChart();
    chart.setTitle(_localeManager.getValue(titleKey));
    chart.setEmptyText(_localeManager.getValue("m.player_count_not_enough_data"));
    chart.setPeakLabel((count, time) -> _localeManager.getValue("m.player_count_peak", new String[] { count, time }));
    chart.setMinimumPoints(minCounts);
    chart.setMinimumRange(40); // keeps a handful of players from looking like a crowd.
    chart.setTimeSpan(from, to);
    chart.setTimeTicks(tickStep, DateTimeFormatter.ofPattern(tickPattern));

    long[] times = new long[counts.length()];
    double[] values = new double[counts.length()];
    for (int i = 0; i < counts.length(); i++) {
      times[i] = counts.getJSONArray(i).getLong(0);
      values[i] = counts.getJSONArray(i).getInt(1);
    }
    chart.setData(times, values);
    return chart;
  }

  /**
   * Every month's average and peak player count, newest first.
   */
  private CardTable createMonthTable (JSONArray months)
  {
    String[] headers = { "month", "average", "peak", "gain", "gain_percent" };
    for (int i = 0; i < headers.length; i++) headers[i] = _localeManager.getValue("m.player_count_table_" + headers[i]);

    CardTable table = new CardTable(headers);
    table.setBoldFirstColumn(true);
    table.setEmptyText(_localeManager.getValue("m.player_count_not_enough_data"));
    table.setMinimumRows(2); // a single month has nothing to gain on.

    for (int i = months.length() - 1; i >= 0; i--) {
      JSONObject month = months.getJSONObject(i);
      double average = month.getDouble("average");

      String gain = "-", gainPercent = "-";
      Color gainColor = CustomColors.INTERFACE_DEFAULT;
      if (i > 0) {
        double previous = months.getJSONObject(i - 1).getDouble("average");
        gain = String.format("%+.1f", average - previous);
        gainPercent = previous == 0 ? "-" : String.format("%+.2f%%", (average - previous) / previous * 100);
        gainColor = average >= previous ? CustomColors.LIGHT_GREEN : CustomColors.LIGHT_RED;
      }

      String name = YearMonth.parse(month.getString("month")).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
      JLabel[] row = table.addRow(name, String.format("%.1f", average), String.valueOf(month.getInt("peak")),
        gain, gainPercent);
      row[3].setForeground(gainColor);
      row[4].setForeground(gainColor);
    }

    return table;
  }

  public void selectedServerChanged ()
  {
    this.eventHandler.selectedServerChanged();
  }

  @Inject public PlayerCountEventHandler eventHandler;
  @Inject protected LauncherContext _ctx;
  @Inject protected LocaleManager _localeManager;

  public JLabel loadingLabel;
  public JLabel titleLabel;
  public ScrollablePanel playerCountList;
  public SmoothScrollPane playerCountListScroll;
}
