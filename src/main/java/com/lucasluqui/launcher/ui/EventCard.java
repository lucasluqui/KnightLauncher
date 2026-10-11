package com.lucasluqui.launcher.ui;

import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * A small clickable card representing one of the active events, using its banner as the background.
 * The event currently on display is highlighted, the rest are dimmed.
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
      @Override
      public void mouseClicked (MouseEvent e)
      {
        onClick.run();
      }

      @Override
      public void mouseEntered (MouseEvent e)
      {
        hovered = true;
        repaint();
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        hovered = false;
        repaint();
      }
    });
  }

  public void setImage (BufferedImage image)
  {
    this.image = image;
    repaint();
  }

  public void setActive (boolean active)
  {
    if (this.active == active) return;
    this.active = active;
    repaint();
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) return;

    // Clipping isn't antialiased in Java2D, which leaves the slanted edges jagged. Instead, draw the
    // antialiased shape into a buffer first, and paint the background only where the shape is.
    BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D bg = buffer.createGraphics();
    bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    bg.setColor(Color.WHITE);
    bg.fill(getShape(0, w, h));
    bg.setComposite(AlphaComposite.SrcIn);

    // Background: the event's banner scaled to the card's width, cropped around its vertical center.
    if (image != null) {
      int srcHeight = Math.min(image.getHeight(), Math.round(image.getWidth() * (h / (float) w)));
      int srcY = (image.getHeight() - srcHeight) / 2;
      bg.drawImage(image, 0, 0, w, h, 0, srcY, image.getWidth(), srcY + srcHeight, null);
    } else {
      bg.setColor(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      bg.fillRect(0, 0, w, h);
    }

    // Darken it so the title stays readable, more so when it's not the event being shown.
    int leftAlpha = active ? 150 : (hovered ? 170 : 200);
    int rightAlpha = active ? 30 : (hovered ? 100 : 140);
    bg.setComposite(AlphaComposite.SrcAtop);
    bg.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, leftAlpha), w, 0, new Color(0, 0, 0, rightAlpha)));
    bg.fillRect(0, 0, w, h);
    bg.dispose();

    g2d.drawImage(buffer, 0, 0, null);

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
    g2d.drawString(text, PADDING + SLANT / 2, (h - metrics.getHeight()) / 2 + metrics.getAscent());

    // Outline, brighter for the active event. Inset a bit so the stroke isn't cut by the bounds.
    g2d.setStroke(new BasicStroke(active ? 1.5f : 1f));
    g2d.setColor(active ? new Color(255, 255, 255, 200) : new Color(255, 255, 255, 40));
    g2d.draw(getShape(1f, w, h));

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

  private static final int PADDING = 12;

  private static final int BADGE_HEIGHT = 16;
  private static final int BADGE_PADDING = 7;
  private static final int BADGE_GAP = 8;

  // How far the top edge is shifted right compared to the bottom one.
  private static final int SLANT = 14;
  private static final float CORNER_RADIUS = 7f;
}
