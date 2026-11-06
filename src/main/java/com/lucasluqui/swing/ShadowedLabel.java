package com.lucasluqui.swing;

import com.jhlabs.image.GaussianFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A label with a soft shadow under its text.
 */
public class ShadowedLabel extends JLabel
{
  public ShadowedLabel (String text)
  {
    super(text);
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    String text = getText();
    float alpha = getForeground().getAlpha() / 255f;
    if (text != null && !text.isEmpty() && alpha > 0f) {
      Graphics2D g2d = (Graphics2D) g.create();
      FractionalMetrics.apply(g2d);
      FontMetrics metrics = g2d.getFontMetrics(getFont());

      // Place the shadow exactly where the label itself will draw.
      Insets insets = getInsets();
      Rectangle view = new Rectangle(insets.left, insets.top,
        getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);
      Rectangle iconBounds = new Rectangle();
      Rectangle textBounds = new Rectangle();
      String clipped = SwingUtilities.layoutCompoundLabel(this, metrics, text, null,
        getVerticalAlignment(), getHorizontalAlignment(), getVerticalTextPosition(), getHorizontalTextPosition(),
        view, iconBounds, textBounds, 0);

      BufferedImage shadow = getShadow(clipped, metrics, HiDpi.scale(g2d));
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, SHADOW_OPACITY * alpha));
      HiDpi.drawBuffer(g2d, shadow, textBounds.x - _shadowPadding, textBounds.y - _shadowPadding + Scale.px(1),
        _shadowWidth, _shadowHeight);
      g2d.dispose();
    }
    super.paintComponent(g);
  }

  /**
   * Returns the blurred shadow for the given text, only rendering it again when the text, font or scale change.
   * <p>
   * It's rendered at the screen's actual resolution and with fractional metrics, the same way the label draws its
   * text. Otherwise, its glyphs get snapped to whole pixels at 100% and stretched, drifting further from the text
   * with every character.
   */
  private BufferedImage getShadow (String text, FontMetrics metrics, double scale)
  {
    Font font = getFont();
    if (_shadow != null && text.equals(_shadowText) && font.equals(_shadowFont) && scale == _shadowScale) {
      return _shadow;
    }

    // Bigger text gets a wider blur, so the shadow looks the same at any size.
    int radius = Math.max(Scale.px(5), Math.round(font.getSize2D() * SHADOW_SPREAD));
    int padding = radius * 2;
    int textWidth = (int) Math.ceil(font.getStringBounds(text, metrics.getFontRenderContext()).getWidth());
    int width = textWidth + padding * 2;
    int height = metrics.getHeight() + padding * 2;

    BufferedImage image = HiDpi.createBuffer(width, height, scale);
    Graphics2D g2d = HiDpi.createGraphics(image, width, height);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    FractionalMetrics.apply(g2d);
    g2d.setFont(font);
    g2d.setColor(Color.BLACK);
    g2d.drawString(text, (float) padding, (float) (padding + metrics.getAscent()));
    g2d.dispose();

    // The blur works in the buffer's pixels, so it's scaled up along with it.
    _shadow = new GaussianFilter((float) (radius * image.getWidth() / (double) width)).filter(image, null);
    _shadowText = text;
    _shadowFont = font;
    _shadowScale = scale;
    _shadowPadding = padding;
    _shadowWidth = width;
    _shadowHeight = height;
    return _shadow;
  }

  private BufferedImage _shadow;
  private String _shadowText;
  private Font _shadowFont;
  private double _shadowScale;
  private int _shadowPadding;
  private int _shadowWidth;
  private int _shadowHeight;

  private static final float SHADOW_OPACITY = 1.0f;
  private static final float SHADOW_SPREAD = 0.2f;
}
