package com.lucasluqui.swing;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.GrayFilter;
import jiconfont.IconCode;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.RGBImageFilter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An icon font glyph drawn as text whenever it's painted.
 * Keeps it sharp at any display scaling.
 */
public final class FontIcon implements Icon, FlatLaf.DisabledIconProvider
{
  /**
   * Builds the icon for a glyph at the given size, in actual pixels (use {@link Scale#px}).
   */
  public static FontIcon of (IconCode code, float size, Color color)
  {
    return new FontIcon(code, size, color, false);
  }

  /**
   * Like {@link #of}, but the glyph's visible shape is centered horizontally in the icon, instead of being
   * placed by its advance width. Some glyphs have uneven side bearings and look off center in square buttons.
   */
  public static FontIcon centered (IconCode code, float size, Color color)
  {
    return new FontIcon(code, size, color, true);
  }

  private FontIcon (IconCode code, float size, Color color, boolean centered)
  {
    this.code = code;
    this.centered = centered;
    this.size = size;
    this.color = color;
    this.text = Character.toString(code.getUnicode());
    this.font = baseFont(code.getFontFamily()).deriveFont(size);

    // Measured the way IconFontSwing does.
    JLabel label = new JLabel(text);
    label.setFont(font);
    Dimension preferred = label.getPreferredSize();
    this.width = preferred.width + 1;
    this.height = preferred.height + 1;

    if (centered) {
      FontRenderContext frc = new FontRenderContext(null, true, true);
      Rectangle2D bounds = font.createGlyphVector(frc, text).getVisualBounds();
      this.offsetX = (float) ((width - bounds.getWidth()) / 2 - bounds.getX());
    } else {
      this.offsetX = 0f;
    }
  }

  @Override
  public void paintIcon (Component c, Graphics g, int x, int y)
  {
    Graphics2D g2d = (Graphics2D) g.create();
    try {
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g2d.setFont(font);
      g2d.setColor(color);
      FontMetrics metrics = g2d.getFontMetrics();
      // Vertically centered the way a label would have it.
      int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
      g2d.drawString(text, x + offsetX, textY);
    } finally {
      g2d.dispose();
    }
  }

  @Override
  public int getIconWidth ()
  {
    return width;
  }

  @Override
  public int getIconHeight ()
  {
    return height;
  }

  /**
   * The grayed-out look FlatLaf gives disabled image icons applied to the glyph's color.
   */
  @Override
  public Icon getDisabledIcon ()
  {
    Object filter = UIManager.get("Component.grayFilter");
    RGBImageFilter grayFilter = filter instanceof RGBImageFilter rgbFilter
      ? rgbFilter
      : GrayFilter.createDisabledIconFilter(FlatLaf.isLafDark());
    Color disabled = new Color(grayFilter.filterRGB(0, 0, color.getRGB()), true);
    return new FontIcon(code, size, disabled, centered);
  }

  /**
   * Done once per font.
   */
  private static Font baseFont (String family)
  {
    return BASE_FONTS.computeIfAbsent(family, IconFontSwing::buildFont);
  }

  private static final Map<String, Font> BASE_FONTS = new ConcurrentHashMap<>();

  private final IconCode code;
  private final float size;
  private final Color color;
  private final String text;
  private final Font font;
  private final int width;
  private final int height;
  private final boolean centered;
  private final float offsetX;
}
