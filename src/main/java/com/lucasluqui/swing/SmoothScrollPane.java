package com.lucasluqui.swing;

import javax.swing.*;

public final class SmoothScrollPane extends JScrollPane
{
  private static final int FPS = 60;
  private static final int TIMER_DELAY = 1000 / FPS;

  private static final double SMOOTHING = 0.33;
  private static final int SCROLL_SPEED = 120;

  private static final int UNIT_INCREMENT = 16;

  private double targetY;
  private final Timer animator;

  public SmoothScrollPane ()
  {
    this(null);
  }

  public SmoothScrollPane (JComponent view)
  {
    super(view);

    setWheelScrollingEnabled(false); // disable default jswing scrolling.

    JScrollBar vBar = getVerticalScrollBar();
    targetY = vBar.getValue();

    animator = new Timer(TIMER_DELAY, e -> animate());
    animator.setRepeats(true);

    // prevents authoritative scrolling from getting borked.
    vBar.addAdjustmentListener(e -> {
      if (e.getValueIsAdjusting()) {
        targetY = e.getValue();
        animator.stop();
      }
    });

    getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);

    enableSmoothScrolling();
  }

  private void enableSmoothScrolling ()
  {
    addMouseWheelListener(e -> {
      if (e.isControlDown()) return;

      e.consume();

      JScrollBar vBar = getVerticalScrollBar();

      targetY = vBar.getValue();
      targetY += e.getPreciseWheelRotation() * SCROLL_SPEED;
      clampTarget();

      if (!animator.isRunning()) {
        animator.start();
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
