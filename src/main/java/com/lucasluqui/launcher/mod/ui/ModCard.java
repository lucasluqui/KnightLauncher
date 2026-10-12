package com.lucasluqui.launcher.mod.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.jhlabs.image.GaussianFilter;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.util.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A card for one installed mod. Its image sits against the left edge and fades into the card, while a
 * blurred copy of it tints the whole background. Name, type badge, description and footer sit next to it,
 * with the enabled toggle at the top right and the action buttons at the bottom right.
 */
public class ModCard extends JPanel
{
  /**
   * @param badge Text for the type badge next to the name, or null for none.
   * @param accent The mod type's color, used for the badge.
   */
  public ModCard (String title, String badge, Color accent, Color badgeForeground,
                  String description, String footer, BufferedImage image)
  {
    this.title = title;
    this.description = description;
    this.footer = footer;
    this.image = image;
    this.ambient = image == null ? null : blur(image);
    setLayout(null);
    setOpaque(false);
    setToolTipText(title);

    if (badge != null) {
      badgeLabel = new JLabel(badge);
      badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
      badgeLabel.setFont(Fonts.getFont("default", 9.0f, Font.ITALIC));
      badgeLabel.putClientProperty(FlatClientProperties.STYLE,
        "background:" + ColorUtil.colorToHexString(accent)
          + "1A; foreground:" + ColorUtil.colorToHexString(badgeForeground)
          + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(accent));
      badgeLabel.setToolTipText(badge);
      add(badgeLabel);
    }

    addMouseListener(hoverListener);
  }

  /**
   * The component at the top right, to toggle the mod on and off.
   */
  public void setToggle (JComponent toggle)
  {
    this.toggle = toggle;
    toggle.addMouseListener(hoverListener);
    add(toggle);
  }

  /**
   * Adds a button to the bottom right row. The first one added is the rightmost.
   */
  public void addAction (JButton button)
  {
    actions.add(button);
    button.addMouseListener(hoverListener);
    add(button);
  }

  /**
   * Disabled mods have their image and tint faded out.
   */
  public void setDimmed (boolean dimmed)
  {
    this.dimmed = dimmed;
    repaint();
  }

  @Override
  public void doLayout ()
  {
    int w = getWidth();
    int h = getHeight();

    if (toggle != null) {
      Dimension size = toggle.getPreferredSize();
      toggle.setBounds(w - PADDING - size.width + 4, PADDING - 3, size.width, size.height);
    }

    int x = w - PADDING;
    for (JButton action : actions) {
      x -= ACTION_SIZE;
      action.setBounds(x, h - PADDING - ACTION_SIZE, ACTION_SIZE, ACTION_SIZE);
      x -= ACTION_GAP;
    }
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) return;

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
    bg.setColor(lerp(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND_FOCUS_DARK, CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND, t));
    bg.fill(getShape(w, h));

    // A blurred copy of the image tints the whole card with its colors.
    if (ambient != null) {
      float opacity = dimmed ? 0.06f : lerp(0.2f, 0.3f, t);
      bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
      bg.drawImage(ambient, 0, 0, w, h, null);
    }

    // The image itself fills a square against the left edge, fading out to the right into the card.
    if (image != null) {
      BufferedImage splash = new BufferedImage(h, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D sg = splash.createGraphics();
      sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      sg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      // Scaled around the center with a transform rather than rounded pixel sizes, so the zoom
      // moves smoothly instead of jumping a pixel at a time.
      float zoom = Math.max(h / (float) image.getWidth(), h / (float) image.getHeight()) * (1f + HOVER_ZOOM * t);
      sg.translate(h / 2.0, h / 2.0);
      sg.scale(zoom, zoom);
      sg.drawImage(image, AffineTransform.getTranslateInstance(-image.getWidth() / 2.0, -image.getHeight() / 2.0), null);
      sg.setTransform(new AffineTransform());
      sg.setComposite(AlphaComposite.DstIn);
      sg.setPaint(new GradientPaint(h * 0.3f, 0, Color.BLACK, h, 0, new Color(0, 0, 0, 0)));
      sg.fillRect(0, 0, h, h);
      sg.dispose();

      float opacity = dimmed ? 0.35f : 1f;
      bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
      bg.drawImage(splash, 0, 0, null);
    }
    bg.dispose();
    g2d.drawImage(buffer, 0, 0, null);

    // Texts between the image and the controls on the right.
    int textX = h + 4;
    int controlsWidth = Math.max(toggle == null ? 0 : toggle.getPreferredSize().width,
      actions.size() * (ACTION_SIZE + ACTION_GAP));
    int textWidth = w - textX - PADDING - controlsWidth - 16;

    Font titleFont = Fonts.getFont("default", 16.0f, Font.BOLD);
    Font descriptionFont = Fonts.getFont("default", 11.0f, Font.PLAIN);
    Font footerFont = Fonts.getFont("default", 10.0f, Font.ITALIC);
    FontMetrics titleMetrics = g2d.getFontMetrics(titleFont);
    FontMetrics descriptionMetrics = g2d.getFontMetrics(descriptionFont);
    FontMetrics footerMetrics = g2d.getFontMetrics(footerFont);

    int badgeWidth = badgeLabel == null ? 0 : badgeLabel.getPreferredSize().width;
    String titleText = ellipsize(title, titleMetrics, textWidth - (badgeLabel == null ? 0 : badgeWidth + 8));
    int titleY = PADDING + titleMetrics.getAscent() - 2;
    g2d.setFont(titleFont);
    g2d.setColor(new Color(0, 0, 0, 90));
    g2d.drawString(titleText, textX + 1, titleY + 2);
    g2d.setColor(dimmed ? new Color(170, 172, 180) : Color.WHITE);
    g2d.drawString(titleText, textX, titleY);

    // The badge sits right after the name, which is only known now. It paints itself after this.
    if (badgeLabel != null) {
      int badgeX = textX + titleMetrics.stringWidth(titleText) + 8;
      int badgeY = titleY - titleMetrics.getAscent() / 2 - BADGE_HEIGHT / 2 + 2;
      Rectangle bounds = new Rectangle(badgeX, badgeY, badgeWidth, BADGE_HEIGHT);
      if (!bounds.equals(badgeLabel.getBounds())) badgeLabel.setBounds(bounds);
    }

    List<String> descriptionLines = description == null || description.isEmpty()
      ? new ArrayList<>() : wrap(description, descriptionMetrics, textWidth, MAX_DESCRIPTION_LINES);
    int descriptionY = titleY + titleMetrics.getDescent() + 6 + descriptionMetrics.getAscent();
    g2d.setFont(descriptionFont);
    g2d.setColor(dimmed ? new Color(120, 124, 135) : new Color(185, 189, 200));
    for (int i = 0; i < descriptionLines.size(); i++) {
      g2d.drawString(descriptionLines.get(i), textX, descriptionY + i * descriptionMetrics.getHeight());
    }

    // Footer lines up with the action buttons at the bottom.
    g2d.setFont(footerFont);
    g2d.setColor(new Color(125, 130, 145));
    g2d.drawString(ellipsize(footer, footerMetrics, textWidth), textX,
      h - PADDING - (ACTION_SIZE - footerMetrics.getHeight()) / 2 - footerMetrics.getDescent());

    g2d.dispose();
  }

  private final MouseAdapter hoverListener = new MouseAdapter()
  {
    @Override
    public void mouseEntered (MouseEvent e)
    {
      setHovered(true);
    }

    @Override
    public void mouseExited (MouseEvent e)
    {
      // Moving onto one of the card's own controls doesn't count as leaving it.
      setHovered(getMousePosition(true) != null);
    }
  };

  private void setHovered (boolean hovered)
  {
    if (this.hovered == hovered) return;
    this.hovered = hovered;
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

  private static Shape getShape (int width, int height)
  {
    return new RoundRectangle2D.Float(0, 0, width, height, CORNER_RADIUS * 2, CORNER_RADIUS * 2);
  }

  /**
   * A small, heavily blurred copy of the image. It's stretched over the whole card, so detail doesn't matter.
   */
  private static BufferedImage blur (BufferedImage image)
  {
    BufferedImage small = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = small.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(image, 0, 0, 48, 48, null);
    g.dispose();
    return new GaussianFilter(12f).filter(small, null);
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

  /**
   * Splits the text into lines that fit the given width, cutting the last one short with an
   * ellipsis if there's more text than lines.
   */
  private static List<String> wrap (String text, FontMetrics metrics, int maxWidth, int maxLines)
  {
    List<String> lines = new ArrayList<>();
    String remaining = text.replaceAll("\\s+", " ").trim();
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

  private final String title;
  private final String description;
  private final String footer;
  private final BufferedImage image;
  private final BufferedImage ambient;
  private JLabel badgeLabel;
  private JComponent toggle;
  private final List<JButton> actions = new ArrayList<>();
  private boolean hovered = false;
  private float hoverProgress = 0f;
  private float hoverTarget = 0f;
  private Timer hoverTimer;
  private boolean dimmed = false;

  public static final int HEIGHT = 116;

  private static final int PADDING = 14;
  private static final float CORNER_RADIUS = 14f;

  private static final int ACTION_SIZE = 28;
  private static final int ACTION_GAP = 6;

  private static final int BADGE_HEIGHT = 18;

  private static final int MAX_DESCRIPTION_LINES = 2;

  private static final int HOVER_DURATION = 180;
  private static final float HOVER_ZOOM = 0.05f;
}
