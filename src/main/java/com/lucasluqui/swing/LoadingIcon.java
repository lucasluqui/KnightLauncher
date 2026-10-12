package com.lucasluqui.swing;

import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An antialiased loading spinner: an arc going around a faint ring. The rotation is based on the time,
 * so it stays smooth no matter how often it's repainted, and every spinner on screen moves in sync.
 * A single timer keeps repainting wherever a spinner was last painted, for as long as it's still on screen.
 */
public final class LoadingIcon implements Icon, FlatLaf.DisabledIconProvider
{
  public LoadingIcon ()
  {
    this(DEFAULT_SIZE);
  }

  /**
   * @param size Width and height at 100% scale, for places with less room than the default size needs.
   */
  public LoadingIcon (int size)
  {
    this(Scale.px(size), 1f);
  }

  /**
   * @param size Width and height in actual pixels, already scaled.
   */
  private LoadingIcon (int size, float opacity)
  {
    this.size = size;
    this.opacity = opacity;
    // Thickness scales along with the size, so smaller spinners keep the same proportions.
    this.stroke = Scale.px(DEFAULT_STROKE) * size / Scale.px(DEFAULT_SIZE);
  }

  @Override
  public void paintIcon (Component c, Graphics g, int x, int y)
  {
    track(c, new Rectangle(x, y, size, size));

    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    // Keep the stroke inside the icon's bounds.
    float inset = stroke / 2f + Scale.px(1f);
    float diameter = size - inset * 2f;

    g2d.setColor(new Color(1f, 1f, 1f, 0.15f * opacity));
    g2d.draw(new Ellipse2D.Float(x + inset, y + inset, diameter, diameter));

    // Clockwise, which is a negative angle for Arc2D.
    double angle = -360.0 * (System.nanoTime() / 1_000_000L % PERIOD) / PERIOD;
    g2d.setColor(new Color(1f, 1f, 1f, opacity));
    g2d.draw(new Arc2D.Float(x + inset, y + inset, diameter, diameter, (float) angle + 90f, -ARC_LENGTH, Arc2D.OPEN));

    g2d.dispose();
  }

  @Override
  public int getIconWidth ()
  {
    return size;
  }

  @Override
  public int getIconHeight ()
  {
    return size;
  }

  /**
   * Shown dimmed on disabled components, e.g. the launch button while the game is launching.
   */
  @Override
  public Icon getDisabledIcon ()
  {
    return new LoadingIcon(size, 0.5f);
  }

  private static void track (Component c, Rectangle bounds)
  {
    TRACKED.put(c, bounds);
    if (!TIMER.isRunning()) TIMER.start();
  }

  private static void tick ()
  {
    Iterator<Map.Entry<Component, Rectangle>> it = TRACKED.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Component, Rectangle> entry = it.next();
      Component c = entry.getKey();

      // Hidden, or showing something else now. If it shows a spinner again, painting it tracks it again.
      if (c == null || !c.isShowing() || !showsSpinner(c)) {
        it.remove();
        continue;
      }
      Rectangle bounds = entry.getValue();
      c.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    if (TRACKED.isEmpty()) TIMER.stop();
  }

  private static boolean showsSpinner (Component c)
  {
    // The disabled icon is derived from this one, so it's enough to check it.
    if (c instanceof JLabel) return ((JLabel) c).getIcon() instanceof LoadingIcon;
    if (c instanceof AbstractButton) return ((AbstractButton) c).getIcon() instanceof LoadingIcon;
    // Painted by something else, there's no telling, so keep it going while it's on screen.
    return true;
  }

  private final int size;
  private final float stroke;
  private final float opacity;

  // Weak keys, so tracking a component never keeps it around.
  private static final Map<Component, Rectangle> TRACKED = new WeakHashMap<>();
  private static final Timer TIMER = new Timer(15, e -> tick());

  // Same size as the GIF it replaces, so layouts stay as they were.
  private static final int DEFAULT_SIZE = 25;
  private static final float DEFAULT_STROKE = 2.5f;
  private static final float ARC_LENGTH = 100f;
  private static final long PERIOD = 900;
}
