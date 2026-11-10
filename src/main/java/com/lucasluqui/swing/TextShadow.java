package com.lucasluqui.swing;

import com.jhlabs.image.GaussianFilter;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A soft shadow drawn under a piece of text. Keeps the last blurred shadow around, so it's only rendered again
 * when the text, font or scale change.
 */
public class TextShadow
{
  /**
   * Paints the shadow for text whose line box has its top left corner at {@code x}, {@code top}, meaning the text
   * itself is drawn with its baseline at {@code top + metrics.getAscent()}.
   *
   * @param metrics The metrics the text is drawn with, from a graphics context with fractional metrics applied.
   * @param alpha   The text's own opacity, which the shadow follows.
   */
  public void paint (Graphics2D g, String text, Font font, FontMetrics metrics, int x, int top, float alpha)
  {
    if (text == null || text.isEmpty() || alpha <= 0f) return;
    Graphics2D g2d = (Graphics2D) g.create();
    BufferedImage shadow = getShadow(text, font, metrics, HiDpi.scale(g2d));
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITY * alpha));
    HiDpi.drawBuffer(g2d, shadow, x - _padding, top - _padding + Scale.px(1), _width, _height);
    g2d.dispose();
  }

  /**
   * Returns the blurred shadow for the given text, only rendering it again when the text, font or scale change.
   * <p>
   * It's rendered at the screen's actual resolution and with fractional metrics, the same way the text itself is
   * drawn. Otherwise, its glyphs get snapped to whole pixels at 100% and stretched, drifting further from the text
   * with every character.
   */
  private BufferedImage getShadow (String text, Font font, FontMetrics metrics, double scale)
  {
    if (_shadow != null && text.equals(_text) && font.equals(_font) && scale == _scale) {
      return _shadow;
    }

    // Bigger text gets a wider blur, so the shadow looks the same at any size.
    int radius = Math.max(Scale.px(5), Math.round(font.getSize2D() * SPREAD));
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
    _text = text;
    _font = font;
    _scale = scale;
    _padding = padding;
    _width = width;
    _height = height;
    return _shadow;
  }

  private BufferedImage _shadow;
  private String _text;
  private Font _font;
  private double _scale;
  private int _padding;
  private int _width;
  private int _height;

  private static final float OPACITY = 1.0f;
  private static final float SPREAD = 0.2f;
}
