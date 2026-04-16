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

    // prevents authoritative scrolling from getting borked.
    vBar.addAdjustmentListener(e -> {
      if (e.getValueIsAdjusting()) {
        _velY = 0;
        _targetY = e.getValue();
        _animator.stop();
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
  private double _friction = 0.80;
  private double _maxVel = 60;
  private int _scrollSpeed = 36;
  private double _velY = 0.0;
  private double _targetY;

  private final int FPS = SystemUtil.getRefreshRate();
}
