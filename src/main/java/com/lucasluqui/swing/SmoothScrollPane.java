package com.lucasluqui.swing;

import com.lucasluqui.util.SystemUtil;

import javax.swing.*;

import static com.lucasluqui.launcher.Log.log;

public final class SmoothScrollPane extends JScrollPane
{
  private final int FPS = SystemUtil.getRefreshRate();

  private double friction = 0.80;
  private double maxVelocity = 60;
  private int scrollSpeed = 36;

  private double velocityY = 0.0;
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

    animator = new Timer(1000 / FPS, e -> animate());
    animator.setRepeats(true);

    // prevents authoritative scrolling from getting borked.
    vBar.addAdjustmentListener(e -> {
      if (e.getValueIsAdjusting()) {
        velocityY = 0;
        targetY = e.getValue();
        animator.stop();
      }
    });

    enableSmoothScrolling();
  }

  private void enableSmoothScrolling ()
  {
    addMouseWheelListener(e -> {
      if (e.isControlDown()) return;

      e.consume();

      JScrollBar vBar = getVerticalScrollBar();
      targetY = vBar.getValue();

      velocityY += e.getPreciseWheelRotation() * scrollSpeed;
      velocityY = Math.max(-maxVelocity, Math.min(velocityY, maxVelocity));

      if (!animator.isRunning()) {
        animator.start();
      }
    });
  }

  private void animate ()
  {
    JScrollBar vBar = getVerticalScrollBar();

    if (Math.abs(velocityY) < 0.5) {
      velocityY = 0;
      animator.stop();
      return;
    }

    targetY += velocityY;
    clampTarget();

    vBar.setValue((int) targetY);

    velocityY *= friction;
  }

  private void clampTarget ()
  {
    JScrollBar vBar = getVerticalScrollBar();
    int min = vBar.getMinimum();
    int max = vBar.getMaximum() - vBar.getVisibleAmount();
    targetY = Math.max(min, Math.min(targetY, max));
  }

  public double getFriction ()
  {
    return this.friction;
  }

  public void setFriction (double friction)
  {
    this.friction = friction;
  }

  public double getMaxVelocity ()
  {
    return this.maxVelocity;
  }

  public void setMaxVelocity (double maxVelocity)
  {
    this.maxVelocity = maxVelocity;
  }

  public int getScrollSpeed ()
  {
    return this.scrollSpeed;
  }

  public void setScrollSpeed (int scrollSpeed)
  {
    this.scrollSpeed = scrollSpeed;
  }
}
