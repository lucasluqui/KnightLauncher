package com.lucasluqui.swing;

import com.lucasluqui.util.SystemUtil;

import javax.swing.*;

public final class SmoothScrollPane extends JScrollPane
{
  public SmoothScrollPane ()
  {
    this(null);
  }

  public SmoothScrollPane (JComponent view)
  {
    super(view);

    setWheelScrollingEnabled(false); // disable default jswing scrolling.

    JScrollBar vBar = getVerticalScrollBar();
    _targetY = vBar.getValue();

    _animator = new Timer(1000 / FPS, e -> animate());
    _animator.setRepeats(true);

    _scrollToAnimator = new Timer(1000 / FPS, e -> animateScrollTo());
    _scrollToAnimator.setRepeats(true);

    // prevents authoritative scrolling from getting borked.
    vBar.addAdjustmentListener(e -> {
      if (e.getValueIsAdjusting()) {
        _velY = 0;
        _targetY = e.getValue();
        _animator.stop();
        _scrollToAnimator.stop();
      }
    });

    enableSmoothScrolling();
  }

  private void enableSmoothScrolling ()
  {
    addMouseWheelListener(e -> {
      if (e.isControlDown()) return;

      e.consume();
      _scrollToAnimator.stop();

      JScrollBar vBar = getVerticalScrollBar();
      _targetY = vBar.getValue();

      _velY += e.getPreciseWheelRotation() * _scrollSpeed;
      _velY = Math.max(-_maxVel, Math.min(_velY, _maxVel));

      if (!_animator.isRunning()) {
        _animator.start();
      }
    });
  }

  private void animate ()
  {
    JScrollBar vBar = getVerticalScrollBar();

    if (Math.abs(_velY) < 0.5) {
      _velY = 0;
      _animator.stop();
      return;
    }

    _targetY += _velY;
    clampTarget();

    vBar.setValue((int) _targetY);

    _velY *= _friction;
  }

  /**
   * Eases the view to the given vertical position, e.g. for a back to top button.
   * Scrolling with the wheel or grabbing the scroll bar interrupts it.
   */
  public void smoothScrollTo (int y)
  {
    _velY = 0;
    _animator.stop();

    JScrollBar vBar = getVerticalScrollBar();
    _scrollToY = Math.max(vBar.getMinimum(), Math.min(y, vBar.getMaximum() - vBar.getVisibleAmount()));

    if (!_scrollToAnimator.isRunning()) {
      _scrollToAnimator.start();
    }
  }

  /**
   * Jumps straight to the given vertical position, stopping any scrolling that was still in motion so it
   * doesn't carry on from there.
   */
  public void scrollToImmediately (int y)
  {
    _velY = 0;
    _animator.stop();
    _scrollToAnimator.stop();

    JScrollBar vBar = getVerticalScrollBar();
    _targetY = Math.max(vBar.getMinimum(), Math.min(y, vBar.getMaximum() - vBar.getVisibleAmount()));
    vBar.setValue((int) _targetY);
  }

  private void animateScrollTo ()
  {
    JScrollBar vBar = getVerticalScrollBar();
    int diff = _scrollToY - vBar.getValue();

    if (Math.abs(diff) <= 1) {
      vBar.setValue(_scrollToY);
      _targetY = _scrollToY;
      _scrollToAnimator.stop();
      return;
    }

    // Covers a fraction of the remaining distance each frame, at least a pixel so it always gets there.
    int step = (int) Math.signum(diff) * Math.max(1, (int) Math.abs(diff * 0.2));
    vBar.setValue(vBar.getValue() + step);
  }

  private void clampTarget ()
  {
    JScrollBar vBar = getVerticalScrollBar();
    int min = vBar.getMinimum();
    int max = vBar.getMaximum() - vBar.getVisibleAmount();
    _targetY = Math.max(min, Math.min(_targetY, max));
  }

  public double getFriction ()
  {
    return this._friction;
  }

  public void setFriction (double friction)
  {
    this._friction = friction;
  }

  public double getMaxVelocity ()
  {
    return this._maxVel;
  }

  public void setMaxVelocity (double maxVelocity)
  {
    this._maxVel = maxVelocity;
  }

  public int getScrollSpeed ()
  {
    return this._scrollSpeed;
  }

  public void setScrollSpeed (int scrollSpeed)
  {
    this._scrollSpeed = scrollSpeed;
  }

  private final Timer _animator;
  private final Timer _scrollToAnimator;
  private int _scrollToY;
  private double _friction = 0.80;
  private double _maxVel = 60;
  private int _scrollSpeed = 36;
  private double _velY = 0.0;
  private double _targetY;

  private final int FPS = SystemUtil.getRefreshRate();
}
