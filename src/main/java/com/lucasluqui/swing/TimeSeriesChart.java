package com.lucasluqui.swing;

import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;

/**
 * A line chart of values over time.
 */
public class TimeSeriesChart extends JComponent
{
  public TimeSeriesChart ()
  {
    setAlignmentX(Component.LEFT_ALIGNMENT);
    setPreferredSize(Scale.dim(740, 210));

    MouseAdapter hover = new MouseAdapter()
    {
      @Override
      public void mouseMoved (MouseEvent e)
      {
        if (!_hoverEnabled) return;
        int nearest = -1;
        for (int i = 0; i < _times.length; i++) {
          if (nearest < 0 || Math.abs(x(_times[i]) - e.getX()) < Math.abs(x(_times[nearest]) - e.getX())) nearest = i;
        }
        setHovered(nearest);
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        setHovered(-1);
      }
    };
    addMouseListener(hover);
    addMouseMotionListener(hover);
  }

  /**
   * The title in the top left.
   */
  public void setTitle (String title)
  {
    _title = title;
    repaint();
  }

  /**
   * What to say instead of drawing anything when there are too few points in the time span.
   */
  public void setEmptyText (String emptyText)
  {
    _emptyText = emptyText;
    repaint();
  }

  /**
   * @param times Epoch milliseconds, in order.
   * @param values One for each time.
   */
  public void setData (long[] times, double[] values)
  {
    if (times.length != values.length) throw new IllegalArgumentException("Every time needs a value.");
    _allTimes = times.clone();
    _allValues = values.clone();
    update();
  }

  /**
   * The times at either end of the chart. Until set, the chart spans from its first point to its last.
   */
  public void setTimeSpan (long from, long to)
  {
    if (to < from) throw new IllegalArgumentException("The span can't end before it starts.");
    _spanFrom = from;
    _spanTo = to;
    update();
  }

  /**
   * Labels along the time axis, one every {@code step} on whole multiples of it in the chart's time zone, so whole
   * hours and days line up.
   */
  public void setTimeTicks (long step, DateTimeFormatter format)
  {
    if (step <= 0) throw new IllegalArgumentException("Ticks need to be some time apart.");
    _tickStep = step;
    _tickFormat = Objects.requireNonNull(format);
    repaint();
  }

  /**
   * How the hovered point's time and the peak's time are written. Until set, it's the day for points a day or more
   * apart and the day and time otherwise.
   */
  public void setHoverFormat (DateTimeFormatter format)
  {
    _hoverFormat = format;
    repaint();
  }

  /**
   * How values are written on the grid and when hovered. Until set, whole values have no decimals and the rest one.
   */
  public void setValueFormat (DoubleFunction<String> format)
  {
    _valueFormat = Objects.requireNonNull(format);
    repaint();
  }

  /**
   * The caption for the peak, drawn across from the title, given the peak's value and time as written. Null leaves it
   * out, which is the default.
   */
  public void setPeakLabel (BiFunction<String, String, String> peakLabel)
  {
    _peakLabel = peakLabel;
    repaint();
  }

  /**
   * Whether a crown marks the peak, both over it in the chart and next to its caption. On by default.
   */
  public void setPeakMarker (boolean peakMarker)
  {
    _peakMarker = peakMarker;
    repaint();
  }

  /**
   * How many points need to fall in the time span before the chart is worth drawing. With fewer, it shows its empty
   * text instead. One by default.
   */
  public void setMinimumPoints (int minimumPoints)
  {
    if (minimumPoints < 1) throw new IllegalArgumentException("A chart needs at least one point to draw.");
    _minimumPoints = minimumPoints;
    update();
  }

  /**
   * Whether hovering the chart snaps to the nearest point and shows its time and value. On by default.
   */
  public void setHoverEnabled (boolean hoverEnabled)
  {
    _hoverEnabled = hoverEnabled;
    if (!hoverEnabled) setHovered(-1);
  }

  /**
   * The hovered point's tooltip, given its time and value as written. Until set, it's the time then the value.
   */
  public void setHoverLabel (BiFunction<String, String, String> hoverLabel)
  {
    _hoverLabel = Objects.requireNonNull(hoverLabel);
    repaint();
  }

  /**
   * The least the grid goes up to, so low values don't fill the chart's whole height.
   */
  public void setMinimumRange (double minimumRange)
  {
    _minimumRange = minimumRange;
    update();
  }

  public void setGridLines (int gridLines)
  {
    if (gridLines < 1) throw new IllegalArgumentException("There needs to be at least one grid line.");
    _gridLines = gridLines;
    update();
  }

  /**
   * The time zone the time axis and the hovered times are in, the system's by default.
   */
  public void setZone (ZoneId zone)
  {
    _zone = Objects.requireNonNull(zone);
    repaint();
  }

  @Override
  public Dimension getMaximumSize ()
  {
    // As wide as there's room for, but no taller than it needs to be.
    return isMaximumSizeSet() ? super.getMaximumSize() : new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
  }

  /**
   * Works out which points are in the time span, the peak among them and how high the grid goes.
   */
  private void update ()
  {
    boolean auto = _spanFrom == null;
    long from = auto ? (_allTimes.length > 0 ? _allTimes[0] : 0) : _spanFrom;
    long to = auto ? (_allTimes.length > 0 ? _allTimes[_allTimes.length - 1] : 0) : _spanTo;

    int n = 0;
    long[] times = new long[_allTimes.length];
    double[] values = new double[_allValues.length];
    _peak = -1;
    for (int i = 0; i < _allTimes.length; i++) {
      if (_allTimes[i] < from || _allTimes[i] > to) continue;
      times[n] = _allTimes[i];
      values[n] = _allValues[i];
      if (_peak < 0 || values[n] > values[_peak]) _peak = n;
      n++;
    }
    if (n < _minimumPoints) {
      n = 0;
      _peak = -1;
    }
    _times = Arrays.copyOf(times, n);
    _values = Arrays.copyOf(values, n);
    _from = from;
    _to = to;
    _hovered = -1;

    // Grid lines go up in the smallest round step that fits the peak.
    double rough = Math.max(n > 0 ? _values[_peak] : 0, _minimumRange) / _gridLines;
    if (rough <= 0) rough = 1;
    double magnitude = Math.pow(10, Math.floor(Math.log10(rough)));
    double factor = 10;
    for (double step : GRID_STEPS) {
      if (step * magnitude >= rough) {
        factor = step;
        break;
      }
    }
    _top = factor * magnitude * _gridLines;

    repaint();
  }

  private void setHovered (int hovered)
  {
    if (hovered != _hovered) {
      _hovered = hovered;
      repaint();
    }
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    FractionalMetrics.apply(g2d);

    int w = getWidth(), h = getHeight();
    g2d.setColor(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
    g2d.fillRoundRect(0, 0, w, h, Scale.px(15), Scale.px(15));

    g2d.setColor(Color.WHITE);
    g2d.setFont(Fonts.getFont("default", 13.0f, Font.BOLD));
    if (_title != null) g2d.drawString(_title, PAD, TITLE_BASELINE);

    if (_times.length == 0) {
      if (_emptyText != null) {
        g2d.setColor(CustomColors.INTERFACE_DEFAULT);
        g2d.setFont(Fonts.getFont("default", 12.0f, Font.ITALIC));
        g2d.drawString(_emptyText, (w - g2d.getFontMetrics().stringWidth(_emptyText)) / 2, h / 2 + Scale.px(10));
      }
      g2d.dispose();
      return;
    }

    DateTimeFormatter hoverFormat = hoverFormat();

    if (_peakLabel != null) {
      String peak = _peakLabel.apply(_valueFormat.apply(_values[_peak]), hoverFormat.format(toDateTime(_times[_peak])));
      int peakX = w - PAD - g2d.getFontMetrics().stringWidth(peak);
      g2d.drawString(peak, peakX, TITLE_BASELINE);

      // The same crown as the one over the peak in the chart, next to the text that explains it, centered on its digits.
      if (_peakMarker) {
        float digitHeight = (float) g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), "0").getVisualBounds().getHeight();
        drawCrown(g2d, peakX - Scale.px(6.0f) - CROWN_WIDTH / 2, TITLE_BASELINE - (digitHeight - CROWN_HEIGHT) / 2);
      }
    }

    int x0 = x0(), x1 = x1(), y0 = y0(), y1 = y1();
    g2d.setFont(Fonts.getFont("default", 10.0f, Font.PLAIN));
    FontMetrics fm = g2d.getFontMetrics();

    // Grid lines with their values.
    for (int i = 0; i <= _gridLines; i++) {
      int y = y1 - (y1 - y0) * i / _gridLines;
      String label = _valueFormat.apply(_top * i / _gridLines);
      g2d.setColor(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      g2d.drawLine(x0, y, x1, y);
      g2d.setColor(CustomColors.INTERFACE_DEFAULT);
      g2d.drawString(label, x0 - Scale.px(8) - fm.stringWidth(label), y + fm.getAscent() / 2 - 1);
    }

    // Time labels, kept from sticking out of the chart.
    if (_tickStep > 0) {
      long offset = _zone.getRules().getOffset(Instant.ofEpochMilli(_from)).getTotalSeconds() * 1000L;
      for (long t = Math.ceilDiv(_from + offset, _tickStep) * _tickStep - offset; t <= _to; t += _tickStep) {
        String label = _tickFormat.format(toDateTime(t));
        int x = x(t) - fm.stringWidth(label) / 2;
        g2d.drawString(label, Math.max(PAD, Math.min(x, x1 - fm.stringWidth(label))), h - Scale.px(16));
      }
    }

    Path2D line = new Path2D.Float();
    for (int i = 0; i < _times.length; i++) {
      if (i == 0) line.moveTo(x(_times[i]), y(_values[i])); else line.lineTo(x(_times[i]), y(_values[i]));
    }
    Path2D area = new Path2D.Float(line);
    area.lineTo(x(_times[_times.length - 1]), y1);
    area.lineTo(x(_times[0]), y1);
    area.closePath();

    g2d.setPaint(new GradientPaint(0, y0, withAlpha(CustomColors.LAUNCH, 110), 0, y1, withAlpha(CustomColors.LAUNCH, 0)));
    g2d.fill(area);
    g2d.setColor(CustomColors.LAUNCH);
    g2d.setStroke(new BasicStroke(Scale.px(2.0f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2d.draw(line);

    // A lone point makes no line, so at least show where it is.
    if (_times.length == 1 && _hovered < 0) drawDot(g2d, x(_times[0]), y(_values[0]));

    // A crown sitting over the peak, clear of the dot drawn there when it's hovered.
    if (_peakMarker) drawCrown(g2d, x(_times[_peak]), y(_values[_peak]) - Scale.px(8.0f));

    if (_hovered >= 0) {
      int x = x(_times[_hovered]), y = y(_values[_hovered]);
      g2d.setColor(withAlpha(Color.WHITE, 90));
      g2d.setStroke(new BasicStroke(Scale.px(1.0f)));
      g2d.drawLine(x, y0, x, y1);
      drawDot(g2d, x, y);

      String text = _hoverLabel.apply(hoverFormat.format(toDateTime(_times[_hovered])), _valueFormat.apply(_values[_hovered]));
      g2d.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
      fm = g2d.getFontMetrics();
      int bw = fm.stringWidth(text) + Scale.px(16), bh = Scale.px(22);
      int bx = x + Scale.px(10) + bw > x1 ? x - Scale.px(10) - bw : x + Scale.px(10);
      g2d.setColor(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND_FOCUS_DARK);
      g2d.fillRoundRect(bx, y0, bw, bh, bh, bh);
      g2d.setColor(Color.WHITE);
      g2d.drawString(text, bx + Scale.px(8), y0 + (bh + fm.getAscent() - fm.getDescent()) / 2);
    }

    g2d.dispose();
  }

  private DateTimeFormatter hoverFormat ()
  {
    if (_hoverFormat != null) return _hoverFormat;
    boolean daily = _times.length > 1 && _times[1] - _times[0] >= TimeUnit.DAYS.toMillis(1);
    return daily ? DAILY_FORMAT : HOURLY_FORMAT;
  }

  private void drawDot (Graphics2D g2d, int x, int y)
  {
    float outer = Scale.px(6.0f), inner = Scale.px(4.0f);
    g2d.setColor(Color.WHITE);
    g2d.fill(new Ellipse2D.Float(x - outer, y - outer, outer * 2, outer * 2));
    g2d.setColor(CustomColors.LAUNCH);
    g2d.fill(new Ellipse2D.Float(x - inner, y - inner, inner * 2, inner * 2));
  }

  private void drawCrown (Graphics2D g2d, float cx, float bottom)
  {
    float w = CROWN_WIDTH, h = CROWN_HEIGHT, r = Scale.px(1.0f);
    float left = cx - w / 2, right = cx + w / 2, top = bottom - h;

    Path2D crown = new Path2D.Float();
    crown.moveTo(left, bottom);
    crown.lineTo(left, top + h * 0.3f);
    crown.lineTo(left + w * 0.3f, top + h * 0.6f);
    crown.lineTo(cx, top);
    crown.lineTo(right - w * 0.3f, top + h * 0.6f);
    crown.lineTo(right, top + h * 0.3f);
    crown.lineTo(right, bottom);
    crown.closePath();

    g2d.setColor(Color.WHITE);
    g2d.fill(crown);
    // Little gems on its tips.
    for (float[] tip : new float[][] { { left, top + h * 0.3f }, { cx, top }, { right, top + h * 0.3f } }) {
      g2d.fill(new Ellipse2D.Float(tip[0] - r, tip[1] - r, r * 2, r * 2));
    }
  }

  private int x0 ()
  {
    return PAD + Scale.px(32);
  }

  private int x1 ()
  {
    return getWidth() - PAD;
  }

  private int y0 ()
  {
    return Scale.px(52);
  }

  private int y1 ()
  {
    return getHeight() - Scale.px(36);
  }

  private int x (long time)
  {
    // A span of a single moment puts everything in the middle.
    if (_to == _from) return (x0() + x1()) / 2;
    return x0() + (int) ((x1() - x0()) * (double) (time - _from) / (_to - _from));
  }

  private int y (double value)
  {
    return y1() - (int) ((y1() - y0()) * value / _top);
  }

  private ZonedDateTime toDateTime (long time)
  {
    return Instant.ofEpochMilli(time).atZone(_zone);
  }

  private Color withAlpha (Color color, int alpha)
  {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
  }

  private String defaultValueFormat (double value)
  {
    return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
  }

  private String _title, _emptyText;
  private long[] _allTimes = new long[0];
  private double[] _allValues = new double[0];
  private Long _spanFrom, _spanTo;
  private long _tickStep;
  private DateTimeFormatter _tickFormat, _hoverFormat;
  private DoubleFunction<String> _valueFormat = this::defaultValueFormat;
  private BiFunction<String, String, String> _peakLabel;
  private BiFunction<String, String, String> _hoverLabel = (time, value) -> time + "  " + value;
  private boolean _peakMarker = true, _hoverEnabled = true;
  private int _minimumPoints = 1;
  private double _minimumRange = 1;
  private int _gridLines = 4;
  private ZoneId _zone = ZoneId.systemDefault();

  // Worked out from the above by update().
  private long[] _times = new long[0];
  private double[] _values = new double[0];
  private long _from, _to;
  private int _peak = -1;
  private double _top = 1;
  private int _hovered = -1;

  private final double[] GRID_STEPS = { 1, 1.5, 2, 2.5, 3, 4, 5 };
  private final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d");
  private final DateTimeFormatter HOURLY_FORMAT = DateTimeFormatter.ofPattern("EEE HH:mm");
  private final int PAD = Scale.px(24);
  private final int TITLE_BASELINE = Scale.px(30);
  private final float CROWN_WIDTH = Scale.px(9.0f);
  private final float CROWN_HEIGHT = Scale.px(6.5f);
}
