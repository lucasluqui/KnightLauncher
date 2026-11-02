package com.lucasluqui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

/**
 * Helps images stay sharp on screens the OS scales up.
 * <p>
 * Java draws everything bigger on those screens, on top of the launcher's own {@link Scale}.
 * An image made at the size it takes up in the UI gets stretched by that, which blurs it.
 * <p>
 * Instead, images are made at the size they actually end up on screen,
 * in device pixels, and handed to Swing along with the size they take up
 * in the UI, so it can draw them 1:1.
 */
public final class HiDpi
{
  private HiDpi ()
  {
    // empty.
  }

  /**
   * How much the operating system scales things up, e.g. 1.5 at 150%.
   * With several screens biggest of them is chosen.
   */
  public static double scale ()
  {
    double scale = 1;
    try {
      if (GraphicsEnvironment.isHeadless()) return 1;
      for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
        AffineTransform transform = device.getDefaultConfiguration().getDefaultTransform();
        scale = Math.max(scale, Math.max(transform.getScaleX(), transform.getScaleY()));
      }
    } catch (Exception ignored) {
      // Can't tell, assume no scaling.
    }
    return scale;
  }

  /**
   * How much the given graphics draw things up.
   * e.g. 1.5 when painting on a screen at 150%. Never below 1.
   */
  public static double scale (Graphics g)
  {
    if (!(g instanceof Graphics2D g2d)) return 1;
    AffineTransform transform = g2d.getTransform();
    return Math.max(1, Math.max(Math.abs(transform.getScaleX()), Math.abs(transform.getScaleY())));
  }

  /**
   * Turns a size in the UI (already through {@link Scale#px}) into the device pixels it covers on screen.
   */
  public static int px (int size)
  {
    return px(size, scale());
  }

  public static int px (int size, double scale)
  {
    return (int) Math.ceil(size * scale - 0.01);
  }

  /**
   * An image taking up {@code width} x {@code height} in the UI, drawn from {@code image}, which should be made
   * at the device pixel size ({@link #px}). Swing picks whichever fits the screen being painted on.
   */
  public static Image image (BufferedImage image, int width, int height)
  {
    if (image.getWidth() <= width && image.getHeight() <= height) return image;
    return new BaseMultiResolutionImage(resize(image, width, height), image);
  }

  /**
   * An icon taking up {@code width} x {@code height} in the UI, see {@link #image}.
   */
  public static ImageIcon icon (BufferedImage image, int width, int height)
  {
    return new ImageIcon(image(image, width, height));
  }

  /**
   * A plain rounded rectangle icon, {@code width} x {@code height} in the UI with corners {@code arc} across.
   */
  public static ImageIcon roundedRectIcon (int width, int height, int arc, Color color)
  {
    double scale = scale();
    BufferedImage image = createBuffer(width, height, scale);
    Graphics2D g = createGraphics(image, width, height);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(color);
    g.fill(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));
    g.dispose();
    return icon(image, width, height);
  }

  /**
   * An offscreen buffer for painting something {@code width} x {@code height} big in the UI, with enough pixels
   * for the given scale. Its graphics come already scaled, so painting code can work in UI sizes as usual.
   * Draw it back with {@link #drawBuffer}.
   */
  public static BufferedImage createBuffer (int width, int height, double scale)
  {
    return new BufferedImage(Math.max(1, px(width, scale)), Math.max(1, px(height, scale)), BufferedImage.TYPE_INT_ARGB);
  }

  /**
   * Graphics for painting into a buffer from {@link #createBuffer} in UI sizes.
   */
  public static Graphics2D createGraphics (BufferedImage buffer, int width, int height)
  {
    Graphics2D g = buffer.createGraphics();
    g.scale(buffer.getWidth() / (double) width, buffer.getHeight() / (double) height);
    return g;
  }

  /**
   * Draws a buffer from {@link #createBuffer} so that it covers {@code width} x {@code height} in the UI.
   */
  public static void drawBuffer (Graphics g, BufferedImage buffer, int x, int y, int width, int height)
  {
    Graphics2D g2d = (Graphics2D) g.create();
    try {
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.drawImage(buffer, x, y, width, height, null);
    } finally {
      g2d.dispose();
    }
  }

  private static BufferedImage resize (BufferedImage image, int width, int height)
  {
    Image scaled = image.getScaledInstance(Math.max(1, width), Math.max(1, height), Image.SCALE_SMOOTH);
    BufferedImage resized = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = resized.createGraphics();
    g.drawImage(scaled, 0, 0, null);
    g.dispose();
    return resized;
  }
}
