package com.lucasluqui.swing;

import com.lucasluqui.launcher.CustomColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel with rounded edges. As wide as there's room for but no taller than it needs to be.
 */
public class Card extends JPanel
{
  public Card (LayoutManager layout)
  {
    super(layout);
    setOpaque(false);
    setAlignmentX(Component.LEFT_ALIGNMENT);
  }

  @Override
  public Dimension getMaximumSize ()
  {
    return isMaximumSizeSet() ? super.getMaximumSize() : new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
  }

  /**
   * Card's rounded outline.
   */
  protected RoundRectangle2D shape ()
  {
    return new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC, ARC);
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
    g2d.fill(shape());
    g2d.dispose();
  }

  private final int ARC = Scale.px(15);
}
