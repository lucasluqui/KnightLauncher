package com.lucasluqui.launcher.ui;

import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.swing.FractionalMetrics;
import com.lucasluqui.swing.Scale;
import com.lucasluqui.swing.HiDpi;
import com.lucasluqui.swing.TextShadow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * A small clickable card representing one of the active events, using its banner as the background.
 * The event currently on display is highlighted, the rest are dimmed. The highlighted card also lights up from
 * left to right as its display time runs out, until the rotation moves on to the next one.
 */
public class EventCard extends JComponent
{
  /**
   * @param badge Text for a small badge on the card's right side, or null for none.
   */
  public EventCard (String title, String badge, Runnable onClick)
  {
    this.title = title;
    this.badge = badge;
    setOpaque(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setToolTipText(title);
    addMouseListener(new MouseAdapter()
    {
      // Not mouseClicked, which gets dropped if the mouse moves even a pixel between press and release.
      @Override
      public void mousePressed (MouseEvent e)
      {
        if (SwingUtilities.isLeftMouseButton(e)) pressed = true;
      }

      @Override
      public void mouseReleased (MouseEvent e)
      {
        if (!pressed) return;
        pressed = false;
        // Only counts as a click if the mouse is still over the card.
        if (contains(e.getPoint())) onClick.run();
      }

      @Override
      public void mouseEntered (MouseEvent e)
      {
        hovered = true;
        updateHover();
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        hovered = false;
        updateHover();
      }
    });
  }

  public void setImage (BufferedImage image)
  {
    this.image = image;
    repaint();
  }

  /**
   * @param duration How long, in milliseconds, the event stays on display before rotating to the next one,
   *                 or 0 if it doesn't rotate. Only used when becoming active, and restarts the progress.
   */
  public void setActive (boolean active, int duration)
  {
    this.active = active;
    this.progressDuration = active ? duration : 0;
    this.progressStart = System.nanoTime();
    if (progressDuration > 0) {
      if (progressTimer == null) progressTimer = new Timer(PROGRESS_FRAME_INTERVAL, e -> repaint());
      progressTimer.restart();
    } else if (progressTimer != null) {
      progressTimer.stop();
    }
    repaint();
  }

  /**
   * How much of the active event's display time has gone by, from 0 to 1.
   */
  private float getProgress ()
  {
    if (progressDuration <= 0) return 0f;
    float elapsed = (System.nanoTime() - progressStart) / 1_000_000f;
    float progress = Math.min(1f, elapsed / progressDuration);
    if (progress >= 1f && progressTimer != null) progressTimer.stop();
    return progress;
  }

  @Override
  public void removeNotify ()
  {
    // Cards get thrown away whenever the events change, don't leave their timers running.
    super.removeNotify();
    if (progressTimer != null) progressTimer.stop();
    if (hoverTimer != null) hoverTimer.stop();
    buffer = null;
  }

  private void updateHover ()
  {
    hoverTarget = hovered ? 1f : 0f;
    if (hoverTimer == null) {
      hoverTimer = new Timer(15, e -> {
        float step = 15f / HOVER_DURATION;
        hoverProgress = hoverTarget > hoverProgress
          ? Math.min(hoverTarget, hoverProgress + step)
          : Math.max(hoverTarget, hoverProgress - step);
        if (hoverProgress == hoverTarget) ((Timer) e.getSource()).stop();
        repaint();
      });
    }
    hoverTimer.start();
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    FractionalMetrics.apply(g2d);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) return;

    // Clipping isn't antialiased in Java2D, which leaves the slanted edges jagged. Instead, draw the
    // antialiased shape into a buffer first, and paint the background only where the shape is.
    // Made at the display's resolution, and kept around between paints: the active card repaints constantly
    // while its progress sweeps across, and a new buffer each time adds up to a lot of garbage.
    double scale = HiDpi.scale(g);
    if (buffer == null || buffer.getWidth() != HiDpi.px(w, scale) || buffer.getHeight() != HiDpi.px(h, scale)) {
      buffer = HiDpi.createBuffer(w, h, scale);
    }
    Graphics2D bg = HiDpi.createGraphics(buffer, w, h);
    bg.setComposite(AlphaComposite.Clear);
    bg.fillRect(0, 0, w, h);
    bg.setComposite(AlphaComposite.SrcOver);
    bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    bg.setColor(Color.WHITE);
    bg.fill(getShape(0, w, h));
    bg.setComposite(AlphaComposite.SrcIn);

    // Scaled with a transform rather than rounded pixel sizes,
    // so the zoom moves smoothly instead of jumping a pixel at a time.
    if (image != null) {
      float zoom = Math.max(w / (float) image.getWidth(), h / (float) image.getHeight())
        * (1f + HOVER_ZOOM * (1f - (1f - hoverProgress) * (1f - hoverProgress)));
      bg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      AffineTransform transform = AffineTransform.getTranslateInstance(w / 2.0, h / 2.0);
      transform.scale(zoom, zoom);
      transform.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
      bg.drawImage(image, transform, null);
    } else {
      bg.setColor(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      bg.fillRect(0, 0, w, h);
    }

    // Darken it so the title stays readable, more so when it's not the event being shown.
    int leftAlpha = active ? 150 : (hovered ? 170 : 200);
    int rightAlpha = active ? 30 : (hovered ? 100 : 140);
    bg.setComposite(AlphaComposite.SrcAtop);
    float progress = active ? getProgress() : 0f;
    if (progress > 0f) {
      // The part of the display time that has gone by is darkened less, sweeping across the card. Its edge leans
      // the same way as the card's sides. The lighter shade goes everywhere first and the regular one over the
      // rest, so there's a single antialiased edge between them and no seam.
      bg.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, leftAlpha - PROGRESS_LIGHTEN), w, 0,
        new Color(0, 0, 0, Math.max(0, rightAlpha - PROGRESS_LIGHTEN))));
      bg.fillRect(0, 0, w, h);
      float edge = progress * (w - SLANT);
      Path2D.Float remaining = new Path2D.Float();
      remaining.moveTo(edge + SLANT, 0);
      remaining.lineTo(w, 0);
      remaining.lineTo(w, h);
      remaining.lineTo(edge, h);
      remaining.closePath();
      bg.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, leftAlpha), w, 0, new Color(0, 0, 0, rightAlpha)));
      bg.fill(remaining);
    } else {
      bg.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, leftAlpha), w, 0, new Color(0, 0, 0, rightAlpha)));
      bg.fillRect(0, 0, w, h);
    }
    bg.dispose();

    HiDpi.drawBuffer(g2d, buffer, 0, 0, w, h);

    // Badge, a pill against the right edge. Halfway down, each slanted edge sits half the slant in.
    int badgeSpace = 0;
    if (badge != null) {
      g2d.setFont(Fonts.getFont("default", 9.0f, Font.BOLD));
      FontMetrics badgeMetrics = g2d.getFontMetrics();
      int badgeWidth = badgeMetrics.stringWidth(badge) + BADGE_PADDING * 2;
      int badgeX = w - PADDING - SLANT / 2 - badgeWidth;
      int badgeY = (h - BADGE_HEIGHT) / 2;
      Color badgeColor = CustomColors.WARNING;
      g2d.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), active || hovered ? 230 : 150));
      g2d.fillRoundRect(badgeX, badgeY, badgeWidth, BADGE_HEIGHT, BADGE_HEIGHT, BADGE_HEIGHT);
      g2d.setColor(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND_FOCUS_DARK);
      g2d.drawString(badge, badgeX + BADGE_PADDING,
        badgeY + (BADGE_HEIGHT - badgeMetrics.getHeight()) / 2 + badgeMetrics.getAscent());
      badgeSpace = badgeWidth + BADGE_GAP;
    }

    // Title, cut short with an ellipsis if it doesn't fit.
    g2d.setFont(Fonts.getFont("default", 12.0f, Font.BOLD));
    g2d.setColor(active ? Color.WHITE : (hovered ? new Color(220, 220, 220) : new Color(165, 165, 165)));
    FontMetrics metrics = g2d.getFontMetrics();
    // Keep the text clear of both slanted edges, and of the badge.
    String text = ellipsize(title, metrics, w - PADDING * 2 - SLANT - badgeSpace);
    int textX = PADDING + SLANT / 2;
    int textTop = (h - metrics.getHeight()) / 2;
    titleShadow.paint(g2d, text, g2d.getFont(), metrics, textX, textTop, 1f);
    g2d.drawString(text, textX, textTop + metrics.getAscent());

    // Outline, brighter for the active event. Inset a bit so the stroke isn't cut by the bounds.
    g2d.setStroke(new BasicStroke(Scale.px(active ? 1.5f : 1f)));
    g2d.setColor(active ? new Color(255, 255, 255, 200) : new Color(255, 255, 255, 40));
    g2d.draw(getShape(Scale.px(1f), w, h));

    g2d.dispose();
  }

  @Override
  public boolean contains (int x, int y)
  {
    // Only react to the mouse within the card's slanted shape, not its empty corners.
    return getShape(0, getWidth(), getHeight()).contains(x, y);
  }

  /**
   * The card's shape: a parallelogram leaning to the right, with rounded corners.
   *
   * @param inset How far in from the component's bounds to place the shape.
   */
  private static Shape getShape (float inset, int width, int height)
  {
    float left = inset;
    float right = width - inset;
    float top = inset;
    float bottom = height - inset;

    // Corners going clockwise from the top left one.
    float[][] corners = {
      { left + SLANT, top },
      { right, top },
      { right - SLANT, bottom },
      { left, bottom }
    };

    Path2D.Float path = new Path2D.Float();
    for (int i = 0; i < corners.length; i++) {
      float[] prev = corners[(i + corners.length - 1) % corners.length];
      float[] corner = corners[i];
      float[] next = corners[(i + 1) % corners.length];

      // Cut each corner short along both of its edges, then curve through it.
      float[] start = towards(corner, prev, CORNER_RADIUS);
      float[] end = towards(corner, next, CORNER_RADIUS);
      if (i == 0) {
        path.moveTo(start[0], start[1]);
      } else {
        path.lineTo(start[0], start[1]);
      }
      path.quadTo(corner[0], corner[1], end[0], end[1]);
    }
    path.closePath();
    return path;
  }

  /**
   * The point {@code distance} away from {@code from}, in the direction of {@code to}.
   */
  private static float[] towards (float[] from, float[] to, float distance)
  {
    float dx = to[0] - from[0];
    float dy = to[1] - from[1];
    float length = (float) Math.sqrt(dx * dx + dy * dy);
    float t = Math.min(distance / length, 0.5f);
    return new float[] { from[0] + dx * t, from[1] + dy * t };
  }

  private static String ellipsize (String text, FontMetrics metrics, int maxWidth)
  {
    if (metrics.stringWidth(text) <= maxWidth) return text;
    String ellipsis = "...";
    int end = text.length();
    while (end > 0 && metrics.stringWidth(text.substring(0, end) + ellipsis) > maxWidth) end--;
    return text.substring(0, end).trim() + ellipsis;
  }

  private final String title;
  private final String badge;
  private BufferedImage image;
  private boolean active = false;
  private boolean hovered = false;
  private boolean pressed = false;
  private float hoverProgress = 0f;
  private float hoverTarget = 0f;
  private Timer hoverTimer;
  private long progressStart;
  private int progressDuration = 0;
  private Timer progressTimer;
  private final TextShadow titleShadow = new TextShadow();

  // Where the card's background gets painted, reused as long as the card's size and the display's scale stay put.
  private BufferedImage buffer;

  // In actual pixels, for the current UI scale.
  private static final int PADDING = Scale.px(12);

  private static final int BADGE_HEIGHT = Scale.px(16);
  private static final int BADGE_PADDING = Scale.px(7);
  private static final int BADGE_GAP = Scale.px(8);

  // How far the top edge is shifted right compared to the bottom one.
  private static final int SLANT = Scale.px(14);
  private static final float CORNER_RADIUS = Scale.px(7f);

  private static final int HOVER_DURATION = 180;
  private static final float HOVER_ZOOM = 0.05f;

  // How much less the elapsed part of the active card is darkened.
  private static final int PROGRESS_LIGHTEN = 70;

  // Roughly 30 fps, plenty for a slow sweep.
  private static final int PROGRESS_FRAME_INTERVAL = 33;
}
