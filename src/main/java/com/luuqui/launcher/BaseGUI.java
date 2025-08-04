package com.luuqui.launcher;

import com.luuqui.util.ColorUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class BaseGUI
{
  protected int pY = 0, pX = 0;

  protected int width = 0;
  protected int height = 0;

  protected boolean displayTitleBar = false;

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
  };

  private void compose ()
  {
    guiFrame.setBounds(0, 0, width, height);

    if (displayTitleBar) {
      titleBar = new JPanel();
      titleBar.setBounds(0, 0, guiFrame.getWidth(), 35);
      titleBar.setBackground(ColorUtil.getTitleBarColor());
      guiFrame.getContentPane().add(titleBar);


      /*
       * Based on Paul Samsotha's reply @ StackOverflow
       * link: https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
       */
      titleBar.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent me) {
          pX = me.getX();
          pY = me.getY();
        }
      });
      titleBar.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent me) {
          pX = me.getX();
          pY = me.getY();
        }

        @Override
        public void mouseDragged(MouseEvent me) {
          guiFrame.setLocation(guiFrame.getLocation().x + me.getX() - pX,
              guiFrame.getLocation().y + me.getY() - pY);
        }
      });
      titleBar.addMouseMotionListener(new MouseMotionListener() {
        @Override
        public void mouseDragged(MouseEvent me) {
          guiFrame.setLocation(guiFrame.getLocation().x + me.getX() - pX,
              guiFrame.getLocation().y + me.getY() - pY);
        }

        @Override
        public void mouseMoved(MouseEvent arg0) {
          // Auto-generated method stub
        }
      });
      titleBar.setLayout(null);

      final int BUTTON_WIDTH = 35;
      final int BUTTON_HEIGHT = 35;

      Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 17, ColorUtil.getForegroundColor());
      closeButton = new JButton(closeIcon);
      closeButton.setBounds(guiFrame.getWidth() - BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
      closeButton.setToolTipText("{SET}");
      closeButton.setFocusPainted(false);
      closeButton.setFocusable(false);
      closeButton.setBackground(null);
      closeButton.setBorder(null);
      titleBar.add(closeButton);
      //closeButton.addActionListener(e -> {
      //    _discordPresenceClient.stop();
      //    System.exit(0);
      //});
      closeButton.addMouseListener(new MouseListener() {
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {
          closeButton.setBackground(CustomColors.LIGHT_RED);
        }
        @Override public void mouseExited(MouseEvent e) {
          closeButton.setBackground(null);
        }
      });

      Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_MINIMIZE, 12, ColorUtil.getForegroundColor());
      minimizeButton = new JButton(minimizeIcon);
      minimizeButton.setBounds(guiFrame.getWidth() - BUTTON_WIDTH * 2, -7, BUTTON_WIDTH, BUTTON_HEIGHT + 7);
      minimizeButton.setToolTipText("{SET}");
      minimizeButton.setFocusPainted(false);
      minimizeButton.setFocusable(false);
      minimizeButton.setBackground(null);
      minimizeButton.setBorder(null);
      titleBar.add(minimizeButton);
      minimizeButton.addActionListener(e -> guiFrame.setState(Frame.ICONIFIED));
    }

    // Little trick to avoid the window not popping up on boot sometimes.
    guiFrame.setAlwaysOnTop(true);
    guiFrame.setAlwaysOnTop(false);
  }

  public JFrame guiFrame = new JFrame();
  protected JPanel titleBar;
  protected JButton closeButton;
  protected JButton minimizeButton;
}
