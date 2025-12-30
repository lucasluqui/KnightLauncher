package com.lucasluqui.swing;

import javax.swing.*;

public final class SmoothScrollPane extends JScrollPane
{
  private static final int FPS = 60;
  private static final int TIMER_DELAY = 1000 / FPS;

  private static final double SMOOTHING = 0.15;
  private static final int SCROLL_SPEED = 50;

  private static final int UNIT_INCREMENT = 60;

  private double targetY;
  private final Timer animator;

  public SmoothScrollPane ()
  {
    this(null);
  }

  public SmoothScrollPane (JComponent view)
  {
    super(view);

    JScrollBar vBar = getVerticalScrollBar();
    targetY = vBar.getValue();

    animator = new Timer(TIMER_DELAY, e -> animate());
    animator.setRepeats(true);

    getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);

    enableSmoothScrolling();
  }

  private void enableSmoothScrolling ()
  {
    addMouseWheelListener(e -> {
      if (!e.isControlDown()) {
        e.consume();

        double delta = e.getPreciseWheelRotation();
        targetY += delta * SCROLL_SPEED;

        clampTarget();
        if (!animator.isRunning()) {
          animator.start();
        }
      }
    });
  }

  private void animate ()
  {
    JScrollBar vBar = getVerticalScrollBar();
    double current = vBar.getValue();
    double diff = targetY - current;

    if (Math.abs(diff) < 0.5) {
      vBar.setValue((int) targetY);
      animator.stop();
      return;
    }

    current += diff * SMOOTHING;
    vBar.setValue((int) current);
  }

  private void clampTarget ()
  {
    JScrollBar vBar = getVerticalScrollBar();
    int min = vBar.getMinimum();
    int max = vBar.getMaximum() - vBar.getVisibleAmount();
    targetY = Math.max(min, Math.min(targetY, max));
  }
}
