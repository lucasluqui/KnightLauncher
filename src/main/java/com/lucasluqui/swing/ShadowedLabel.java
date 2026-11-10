package com.lucasluqui.swing;

import javax.swing.*;
import java.awt.*;

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

      _shadow.paint(g2d, clipped, getFont(), metrics, textBounds.x, textBounds.y, alpha);
      g2d.dispose();
    }
    super.paintComponent(g);
  }

  private final TextShadow _shadow = new TextShadow();
}
