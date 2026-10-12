package com.lucasluqui.swing;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A thin, fully rounded progress bar: a faint track with a gradient fill and a soft glow around it.
 * Doesn't paint any text, show the progress elsewhere if needed. The bar is drawn {@link #GLOW_MARGIN} in
 * from the component's bounds, so the glow isn't cut off.
 */
public class SlimProgressBarUI extends BasicProgressBarUI
{
  public SlimProgressBarUI (Color fill)
  {
    this(fill, DEFAULT_BAR_HEIGHT);
  }

  /**
   * @param barHeight How thick the bar is, for places with less room than the default needs.
   */
  public SlimProgressBarUI (Color fill, float barHeight)
  {
    this.fill = fill;
    this.fillEnd = blend(fill, Color.WHITE, 0.35f);
    this.barHeight = barHeight;
  }

  @Override
  public void paint (Graphics g, JComponent c)
  {
    int w = c.getWidth();
    int h = c.getHeight();
    if (w <= 0 || h <= 0) return;

    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    // The bar is inset from the bounds and vertically centered, leaving room around it for the glow.
    float barHeight = Math.min(this.barHeight, h);
    float x = GLOW_MARGIN;
    float y = (h - barHeight) / 2f;
    float barWidth = w - GLOW_MARGIN * 2;

    g2d.setColor(new Color(255, 255, 255, 30));
    g2d.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, barHeight, barHeight));

    // Never narrower than the bar is tall, so it's a dot rather than a sliver right at the start.
    double percent = progressBar.getPercentComplete();
    if (percent > 0) {
      float fillWidth = Math.max(barHeight, (float) (barWidth * percent));

      // Soft glow, a few wider and fainter copies of the fill behind it.
      for (int i = GLOW_LAYERS; i > 0; i--) {
        float spread = i * 1.5f;
        g2d.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 18));
        g2d.fill(new RoundRectangle2D.Float(x - spread, y - spread, fillWidth + spread * 2, barHeight + spread * 2,
          barHeight + spread * 2, barHeight + spread * 2));
      }

      g2d.setPaint(new GradientPaint(x, 0, fill, x + fillWidth, 0, fillEnd));
      g2d.fill(new RoundRectangle2D.Float(x, y, fillWidth, barHeight, barHeight, barHeight));
    }

    g2d.dispose();
  }

  private static Color blend (Color from, Color to, float t)
  {
    return new Color(
      Math.round(from.getRed() + (to.getRed() - from.getRed()) * t),
      Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
      Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t));
  }

  private final Color fill;
  private final Color fillEnd;
  private final float barHeight;

  private static final float DEFAULT_BAR_HEIGHT = 6f;
  private static final int GLOW_LAYERS = 3;

  /**
   * Room the glow takes on each side of the bar. Size the component this much larger than the bar on every side.
   */
  public static final int GLOW_MARGIN = 5;
}
