package com.lucasluqui.swing;

import javax.swing.*;

import static com.lucasluqui.swing.Log.log;

public final class SmoothProgressBar extends JProgressBar
{
  public SmoothProgressBar ()
  {
    super();
    this.setMinimum(VISUAL_MIN);
    this.setMaximum(VISUAL_MAX);
    setStringPainted(false);
    _initialized = true;
  }

  @Override
  public void setMinimum (int min)
  {
    if (!_initialized) {
      super.setMinimum(min);
      return;
    }

    _logicalMin = min;
    if (_logicalValue < _logicalMin) {
      _logicalValue = _logicalMin;
    }
    reanimate();
  }

  @Override
  public void setMaximum (int max)
  {
    if (!_initialized) {
      super.setMaximum(max);
      return;
    }

    _logicalMax = max;
    if (_logicalValue > _logicalMax) {
      _logicalValue = _logicalMax;
    }
    reanimate();
  }

  @Override
  public void setValue (int value)
  {
    if (value < _logicalMin || value > _logicalMax) {
      log.error("Progress bar value out of logical range!",
        "value", value,
        "logicalMin", _logicalMin,
        "logicalMax", _logicalMax
      );
    }

    _logicalValue = value;

    // when being set to the logical minimum or maximum, don't animate, just go straight to it.
    if (value == _logicalMin) {
      stopAnimation();
      _currentVisualValue = VISUAL_MIN;
      super.setValue(VISUAL_MIN);
      setStringPainted(false);
      return;
    }

    if (value == _logicalMax) {
      stopAnimation();
      _currentVisualValue = VISUAL_MAX;
      super.setValue(VISUAL_MAX);
      setStringPainted(this._shouldPaintString);
      return;
    }

    setStringPainted(this._shouldPaintString);
    animateTo(logicalToVisual(value));
  }

  private int logicalToVisual (int value)
  {
    if (_logicalMax == _logicalMin) {
      return VISUAL_MIN;
    }

    double ratio = (value - _logicalMin) / (double) (_logicalMax - _logicalMin);

    return (int) Math.round(ratio * VISUAL_MAX);
  }

  private void reanimate ()
  {
    if (_logicalValue == _logicalMin) {
      setValue(_logicalMin);
      return;
    }
    animateTo(logicalToVisual(_logicalValue));
  }

  private void animateTo (int targetVisual)
  {
    if (targetVisual < _currentVisualValue) {
      _currentVisualValue = targetVisual;
      stopAnimation();
      super.setValue(_currentVisualValue);
      return;
    }

    stopAnimation();

    _animationTimer = new Timer(DELAY_MS, e -> {
      if (_currentVisualValue == targetVisual) {
        stopAnimation();
        return;
      }

      int direction = Integer.compare(targetVisual, _currentVisualValue);
      _currentVisualValue += direction * STEP;

      if ((direction > 0 && _currentVisualValue > targetVisual) ||
        (direction < 0 && _currentVisualValue < targetVisual)) {
        _currentVisualValue = targetVisual;
      }

      super.setValue(_currentVisualValue);
    });

    _animationTimer.start();
  }

  private void stopAnimation ()
  {
    if (_animationTimer != null && _animationTimer.isRunning()) {
      _animationTimer.stop();
    }
  }

  public void setShouldPaintString (boolean painted)
  {
    this._shouldPaintString = painted;
  }

  private int _logicalMin = 0;
  private int _logicalMax = 100;
  private int _logicalValue = 0;
  private int _currentVisualValue = 0;
  private Timer _animationTimer;
  private boolean _shouldPaintString = true;

  /** Tracks whether this element has finished intializing or not. */
  private boolean _initialized = false;

  private final int VISUAL_MIN = 0;
  private final int VISUAL_MAX = 500;
  private final int DELAY_MS = 3;
  private final int STEP = 4;
}