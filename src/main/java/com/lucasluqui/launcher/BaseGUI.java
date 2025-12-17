package com.lucasluqui.launcher;

import com.formdev.flatlaf.FlatClientProperties;
import com.lucasluqui.util.ColorUtil;
import com.lucasluqui.util.SystemUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;

public abstract class BaseGUI
{
  protected int pY = 0, pX = 0;

  protected int width;
  protected int height;

  protected boolean displayTitleBar;

  public BaseGUI (int width, int height, boolean displayTitleBar)
  {
    this.width = width;
    this.height = height;
    this.displayTitleBar = displayTitleBar;
    compose();
  }

  public void switchVisibility ()
  {
    this.guiFrame.setVisible(!this.guiFrame.isVisible());
  }

  private void compose ()
  {
    guiFrame.setBounds(0, 0, width, height);
    guiFrame.setGlassPane(new BorderPane());
    guiFrame.getGlassPane().setVisible(true);

    if (displayTitleBar) {
      titleBar = new JPanel();
      titleBar.setBounds(0, 0, guiFrame.getWidth(), 35);
      titleBar.setBackground(ColorUtil.getTitleBarColor());
      guiFrame.getContentPane().add(titleBar);


      /*
       * Based on Paul Samsotha's reply @ StackOverflow
       * link: https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
       */
      titleBar.addMouseListener(new MouseAdapter()
      {
        public void mousePressed (MouseEvent me)
        {
          pX = me.getX();
          pY = me.getY();
        }
      });
      titleBar.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed (MouseEvent me)
        {
          pX = me.getX();
          pY = me.getY();
        }

        @Override
        public void mouseDragged (MouseEvent me)
        {
          guiFrame.setLocation(guiFrame.getLocation().x + me.getX() - pX,
            guiFrame.getLocation().y + me.getY() - pY);
        }
      });
      titleBar.addMouseMotionListener(new MouseMotionListener()
      {
        @Override
        public void mouseDragged (MouseEvent me)
        {
          guiFrame.setLocation(guiFrame.getLocation().x + me.getX() - pX,
            guiFrame.getLocation().y + me.getY() - pY);
        }

        @Override
        public void mouseMoved (MouseEvent arg0)
        {
          // Auto-generated method stub
        }
      });
      titleBar.setLayout(null);

      final int BUTTON_WIDTH = 35;
      final int BUTTON_HEIGHT = 35;

      Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 17, ColorUtil.getForegroundColor());
      closeButton = new JButton(closeIcon);

      if (SystemUtil.isMac()) {
        closeButton.setBounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
      } else {
        closeButton.setBounds(guiFrame.getWidth() - BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
      }

      closeButton.setToolTipText("{SET}");
      closeButton.setFocusPainted(false);
      closeButton.setFocusable(false);
      closeButton.setBackground(null);
      closeButton.setBorder(null);
      titleBar.add(closeButton);

      closeButton.addMouseListener(new MouseListener()
      {
        @Override
        public void mouseClicked (MouseEvent e) {}

        @Override
        public void mousePressed (MouseEvent e) {}

        @Override
        public void mouseReleased (MouseEvent e) {}

        @Override
        public void mouseEntered (MouseEvent e)
        {
          closeButton.setBackground(CustomColors.LIGHT_RED);
        }

        @Override
        public void mouseExited (MouseEvent e)
        {
          closeButton.setBackground(null);
        }
      });

      Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_MINIMIZE, 12, ColorUtil.getForegroundColor());
      minimizeButton = new JButton(minimizeIcon);

      if (SystemUtil.isMac()) {
        minimizeButton.setBounds(BUTTON_WIDTH, -7, BUTTON_WIDTH, BUTTON_HEIGHT + 7);
      } else {
        minimizeButton.setBounds(guiFrame.getWidth() - BUTTON_WIDTH * 2, -7, BUTTON_WIDTH, BUTTON_HEIGHT + 7);
      }

      minimizeButton.setToolTipText("{SET}");
      minimizeButton.setFocusPainted(false);
      minimizeButton.setFocusable(false);
      minimizeButton.setBackground(null);
      minimizeButton.setBorder(null);
      titleBar.add(minimizeButton);
      minimizeButton.addActionListener(e -> guiFrame.setState(Frame.ICONIFIED));
    }

    returnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
    returnButton.setVisible(false);
    returnButton.setFocusable(false);
    returnButton.setFocusPainted(false);
    returnButton.setBorder(null);
    returnButton.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    returnButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    guiFrame.add(returnButton);

    // Little trick to avoid the window not popping up on boot sometimes.
    guiFrame.setAlwaysOnTop(true);
    guiFrame.setAlwaysOnTop(false);
  }

  private class BorderPane extends JPanel
  {
    BorderPane ()
    {
      setOpaque(false);
      setFocusable(false);
      setEnabled(false);
    }

    @Override
    protected void paintComponent (Graphics g)
    {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

      Shape border = new RoundRectangle2D.Double(
        0.5,
        0.5,
        width - 1,
        height - 1,
        15, 15);

      g2.setColor(CustomColors.INTERFACE_DEFAULT_WINDOW_BORDER);
      g2.setStroke(new BasicStroke(1));
      g2.draw(border);

      g2.dispose();
    }
  }

  public JFrame guiFrame = new JFrame();
  protected JPanel titleBar;
  protected JButton returnButton;
  protected JButton closeButton;
  protected JButton minimizeButton;
}
