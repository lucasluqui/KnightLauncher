package com.lucasluqui.launcher.editor.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.util.ColorUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A clickable card for one of the editors: its name and a short description at the bottom left,
 * with its splash image fading in from the top right. Hovering brings the image forward and reveals a
 * launch arrow. Editors that can't run on the selected server are dimmed and say so.
 */
public class EditorCard extends JComponent
{
  /**
   * @param badge Text for a small badge next to the name, or null for none.
   */
  public EditorCard (String title, String badge, String description, BufferedImage image, Runnable onClick)
  {
    this.title = title;
    this.description = description;
    this.image = image;
    this.arrowIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_RIGHT, 14, Color.WHITE);
    this.lockIcon = IconFontSwing.buildIcon(FontAwesome.LOCK, 11, new Color(200, 200, 200));
    setOpaque(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Same look as the launcher's other badges, e.g. the Java VM one in settings.
    if (badge != null) {
      badgeLabel = new JLabel(badge);
      badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
      badgeLabel.setFont(Fonts.getFont("default", 9.0f, Font.ITALIC));
      badgeLabel.putClientProperty(FlatClientProperties.STYLE,
        "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND)
          + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_FOREGROUND)
          + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND));
      add(badgeLabel);
    }

    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed (MouseEvent e)
      {
        if (!SwingUtilities.isLeftMouseButton(e) || unavailableReason != null) return;
        pressed = true;
        repaint();
      }

      @Override
      public void mouseReleased (MouseEvent e)
      {
        if (!pressed) return;
        pressed = false;
        repaint();
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

  /**
   * Marks the editor as unavailable on the selected server with the given reason, or available again
   * when null.
   */
  public void setUnavailableReason (String reason)
  {
    this.unavailableReason = reason;
    setCursor(Cursor.getPredefinedCursor(reason == null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    setToolTipText(reason);
    if (badgeLabel != null) badgeLabel.setEnabled(reason == null);
    updateHover();
    repaint();
  }

  private void updateHover ()
  {
    hoverTarget = hovered && unavailableReason == null ? 1f : 0f;
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
    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) return;

    boolean available = unavailableReason == null;

    // Ease out, so the movement settles in gently.
    float t = 1f - (1f - hoverProgress) * (1f - hoverProgress);

    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    // Clipping isn't antialiased in Java2D, so the card is drawn into a buffer holding its shape first,
    // and everything else only lands where the shape is.
    BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D bg = buffer.createGraphics();
    bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    bg.setColor(lerp(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND, CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND_FOCUS_DARK, t));
    bg.fill(getShape(0, w, h));
    bg.setComposite(AlphaComposite.SrcAtop);

    // The splash image covers the card, zooming in a little around its center while hovered,
    // and fades out towards the bottom left so the texts sit on a plain background.
    if (image != null) {
      BufferedImage splash = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D sg = splash.createGraphics();
      sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      // Scale it to cover the whole card, cropping whatever sticks out.
      float zoom = Math.max(w / (float) image.getWidth(), h / (float) image.getHeight()) * (1f + HOVER_ZOOM * t);
      int zw = Math.round(image.getWidth() * zoom);
      int zh = Math.round(image.getHeight() * zoom);
      sg.drawImage(image, (w - zw) / 2, (h - zh) / 2, zw, zh, null);
      sg.setComposite(AlphaComposite.DstIn);
      float fadeStart = lerp(0.15f, 0f, t);
      sg.setPaint(new GradientPaint(w * fadeStart, h, new Color(0, 0, 0, 0), w * 0.9f, 0, Color.BLACK));
      sg.fillRect(0, 0, w, h);
      sg.dispose();

      float opacity = available ? lerp(0.45f, 1f, t) : 0.2f;
      if (pressed) opacity *= 0.8f;
      bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
      bg.drawImage(splash, 0, 0, null);
    }
    bg.dispose();
    g2d.drawImage(buffer, 0, 0, null);

    Composite composite = g2d.getComposite();

    // Name and description at the bottom left, leaving room for the launch arrow on the right.
    int textX = PADDING;
    int textWidth = w - PADDING * 2 - ARROW_SIZE - 10;
    Font titleFont = Fonts.getFont("default", 20.0f, Font.BOLD);
    Font descriptionFont = Fonts.getFont("default", 11.0f, Font.ITALIC);
    FontMetrics titleMetrics = g2d.getFontMetrics(titleFont);
    FontMetrics descriptionMetrics = g2d.getFontMetrics(descriptionFont);

    List<String> descriptionLines = description == null || description.isEmpty()
      ? new ArrayList<>() : wrap(description, descriptionMetrics, textWidth, MAX_DESCRIPTION_LINES);
    int descriptionY = h - PADDING - descriptionMetrics.getDescent()
      - (descriptionLines.size() - 1) * descriptionMetrics.getHeight();
    int titleY = descriptionLines.isEmpty()
      ? h - PADDING - titleMetrics.getDescent()
      : descriptionY - descriptionMetrics.getAscent() - titleMetrics.getDescent() - 2;

    int badgeWidth = badgeLabel == null ? 0 : badgeLabel.getPreferredSize().width;
    String titleText = ellipsize(title, titleMetrics, textWidth - (badgeLabel == null ? 0 : badgeWidth + 8));
    g2d.setFont(titleFont);
    g2d.setColor(new Color(0, 0, 0, 90));
    g2d.drawString(titleText, textX + 1, titleY + 2);
    g2d.setColor(available ? lerp(new Color(225, 225, 225), Color.WHITE, t) : new Color(130, 130, 135));
    g2d.drawString(titleText, textX, titleY);

    // The badge sits right after the name, which is only known now. It paints itself after this.
    if (badgeLabel != null) {
      int badgeX = textX + titleMetrics.stringWidth(titleText) + 8;
      int badgeY = titleY - titleMetrics.getAscent() / 2 - BADGE_HEIGHT / 2 + 2;
      Rectangle bounds = new Rectangle(badgeX, badgeY, badgeWidth, BADGE_HEIGHT);
      if (!bounds.equals(badgeLabel.getBounds())) badgeLabel.setBounds(bounds);
    }

    g2d.setFont(descriptionFont);
    g2d.setColor(available ? lerp(new Color(160, 165, 178), new Color(210, 212, 220), t) : new Color(105, 108, 118));
    for (int i = 0; i < descriptionLines.size(); i++) {
      g2d.drawString(descriptionLines.get(i), textX, descriptionY + i * descriptionMetrics.getHeight());
    }

    if (available) {
      // Launch arrow at the bottom right, fading and sliding in while hovered.
      if (t > 0f) {
        int cx = w - PADDING - ARROW_SIZE / 2 - Math.round(HOVER_SLIDE * (1f - t));
        int cy = h - PADDING - ARROW_SIZE / 2;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, t));
        g2d.setColor(CustomColors.LAUNCH);
        g2d.fillOval(cx - ARROW_SIZE / 2, cy - ARROW_SIZE / 2, ARROW_SIZE, ARROW_SIZE);
        arrowIcon.paintIcon(this, g2d, cx - arrowIcon.getIconWidth() / 2 + 1, cy - arrowIcon.getIconHeight() / 2);
        g2d.setComposite(composite);
      }
    } else {
      // A chip at the top right explaining why it can't be launched.
      g2d.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
      FontMetrics chipMetrics = g2d.getFontMetrics();
      int chipWidth = chipMetrics.stringWidth(unavailableReason) + lockIcon.getIconWidth() + CHIP_PADDING * 2 + 6;
      int chipX = w - CHIP_MARGIN - chipWidth;
      int chipY = CHIP_MARGIN;
      g2d.setColor(new Color(255, 255, 255, 25));
      g2d.fill(new RoundRectangle2D.Float(chipX, chipY, chipWidth, CHIP_HEIGHT, CHIP_HEIGHT, CHIP_HEIGHT));
      lockIcon.paintIcon(this, g2d, chipX + CHIP_PADDING, chipY + (CHIP_HEIGHT - lockIcon.getIconHeight()) / 2);
      g2d.setColor(new Color(200, 200, 200));
      g2d.drawString(unavailableReason, chipX + CHIP_PADDING + lockIcon.getIconWidth() + 6,
        chipY + (CHIP_HEIGHT - chipMetrics.getHeight()) / 2 + chipMetrics.getAscent());
    }

    g2d.dispose();
  }

  @Override
  public boolean contains (int x, int y)
  {
    // Only react to the mouse within the card's rounded shape.
    return getShape(0, getWidth(), getHeight()).contains(x, y);
  }

  private static Shape getShape (float inset, int width, int height)
  {
    return new RoundRectangle2D.Float(inset, inset, width - inset * 2, height - inset * 2,
      CORNER_RADIUS * 2, CORNER_RADIUS * 2);
  }

  /**
   * Splits the text into lines that fit the given width, cutting the last one short with an
   * ellipsis if there's more text than lines.
   */
  private static List<String> wrap (String text, FontMetrics metrics, int maxWidth, int maxLines)
  {
    List<String> lines = new ArrayList<>();
    String remaining = text.trim();
    while (!remaining.isEmpty() && lines.size() < maxLines) {
      if (lines.size() == maxLines - 1 || metrics.stringWidth(remaining) <= maxWidth) {
        lines.add(ellipsize(remaining, metrics, maxWidth));
        break;
      }
      // Break at the last space that fits, or mid-word if there's none (e.g. CJK text).
      int end = remaining.length();
      while (end > 1 && metrics.stringWidth(remaining.substring(0, end)) > maxWidth) end--;
      int space = remaining.lastIndexOf(' ', end);
      if (space > 0) end = space;
      lines.add(remaining.substring(0, end).trim());
      remaining = remaining.substring(end).trim();
    }
    return lines;
  }

  private static String ellipsize (String text, FontMetrics metrics, int maxWidth)
  {
    if (metrics.stringWidth(text) <= maxWidth) return text;
    String ellipsis = "...";
    int end = text.length();
    while (end > 0 && metrics.stringWidth(text.substring(0, end) + ellipsis) > maxWidth) end--;
    return text.substring(0, end).trim() + ellipsis;
  }

  private static float lerp (float from, float to, float t)
  {
    return from + (to - from) * t;
  }

  private static Color lerp (Color from, Color to, float t)
  {
    return new Color(
      Math.round(lerp(from.getRed(), to.getRed(), t)),
      Math.round(lerp(from.getGreen(), to.getGreen(), t)),
      Math.round(lerp(from.getBlue(), to.getBlue(), t)));
  }

  private final String title;
  private JLabel badgeLabel;
  private final String description;
  private final BufferedImage image;
  private final Icon arrowIcon;
  private final Icon lockIcon;
  private String unavailableReason = null;
  private boolean hovered = false;
  private boolean pressed = false;
  private float hoverProgress = 0f;
  private float hoverTarget = 0f;
  private Timer hoverTimer;

  private static final int PADDING = 20;
  private static final float CORNER_RADIUS = 14f;

  private static final int ARROW_SIZE = 36;
  private static final int CHIP_HEIGHT = 24;
  private static final int CHIP_PADDING = 10;
  private static final int CHIP_MARGIN = 14;

  private static final int BADGE_HEIGHT = 18;

  private static final int MAX_DESCRIPTION_LINES = 2;

  private static final int HOVER_DURATION = 180;
  private static final float HOVER_ZOOM = 0.05f;
  private static final float HOVER_SLIDE = 8f;
}
