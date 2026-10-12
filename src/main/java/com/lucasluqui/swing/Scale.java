package com.lucasluqui.swing;

import java.awt.*;

/**
 * The launcher's UI scale. Every size in the UI is written for 100% and goes through here, which turns it
 * into actual pixels for the chosen scale. It's set once on startup, before any UI is built, so changing
 * it needs a restart
 *
 * TODO: In the future; when we're no longer restrained by Java 8: Upgrade this so we don't need to restart.
 *       Java 9+ should allow this to follow through smoothly without one. One day!
 */
public final class Scale
{
  private Scale ()
  {}

  public static void init (float factor)
  {
    Scale.factor = factor;
  }

  public static float factor ()
  {
    return factor;
  }

  public static int px (int value)
  {
    return Math.round(value * factor);
  }

  public static float px (float value)
  {
    return value * factor;
  }

  /**
   * The other way around: actual pixels, e.g. a component's current position, back into 100% units.
   */
  public static int unpx (int value)
  {
    return Math.round(value / factor);
  }

  /**
   * Both edges are scaled rather than the size, so components that touch at 100% still touch when scaled.
   */
  public static Rectangle rect (int x, int y, int width, int height)
  {
    int left = px(x);
    int top = px(y);
    return new Rectangle(left, top, px(x + width) - left, px(y + height) - top);
  }

  public static Dimension dim (int width, int height)
  {
    return new Dimension(px(width), px(height));
  }

  /**
   * The biggest scale at which a window of the given size, at 100%, still fits on the screen it'd be shown on.
   */
  public static float maxFactorFor (int width, int height)
  {
    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    return Math.min(screen.width / (float) width, screen.height / (float) height);
  }

  private static float factor = 1f;
}
