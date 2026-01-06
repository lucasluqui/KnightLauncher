package com.lucasluqui.swing;

import javax.swing.*;

import static com.lucasluqui.swing.Log.log;

public final class SmoothProgressBar extends JProgressBar
{
  private static final int VISUAL_MIN = 0;
  private static final int VISUAL_MAX = 500;

  private static final int DELAY_MS = 3;
  private static final int STEP = 4;

  private int logicalMin = 0;
  private int logicalMax = 100;
  private int logicalValue = 0;

  private int currentVisualValue = 0;
  private Timer animationTimer;

  public SmoothProgressBar ()
  {
    super(VISUAL_MIN, VISUAL_MAX);
    setStringPainted(false);
  }

  @Override
  public void setMinimum (int min)
  {
    logicalMin = min;
    if (logicalValue < logicalMin) {
      logicalValue = logicalMin;
    }
    reanimate();
  }

  @Override
  public void setMaximum (int max)
  {
    logicalMax = max;
    if (logicalValue > logicalMax) {
      logicalValue = logicalMax;
    }
    reanimate();
  }

  @Override
  public void setValue (int value)
  {
    if (value < logicalMin || value > logicalMax) {
      log.error("Progress bar value out of logical range!",
        "value", value,
        "logicalMin", logicalMin,
        "logicalMax", logicalMax
      );
    }

    logicalValue = value;

    // when being set to the logical minimum, don't animate, just go straight to it.
    if (value == logicalMin) {
      stopAnimation();
      currentVisualValue = VISUAL_MIN;
      super.setValue(VISUAL_MIN);
      setStringPainted(false);
      return;
    }

    setStringPainted(true);
    animateTo(logicalToVisual(value));
  }

  private int logicalToVisual (int value)
  {
    if (logicalMax == logicalMin) {
      return VISUAL_MIN;
    }

    double ratio = (value - logicalMin) / (double) (logicalMax - logicalMin);

    return (int) Math.round(ratio * VISUAL_MAX);
  }

  private void reanimate ()
  {
    if (logicalValue == logicalMin) {
      setValue(logicalMin);
      return;
    }
    animateTo(logicalToVisual(logicalValue));
  }

  private void animateTo (int targetVisual)
  {
    if (targetVisual < currentVisualValue) {
      currentVisualValue = targetVisual;
      stopAnimation();
      super.setValue(currentVisualValue);
      return;
    }

    stopAnimation();

    animationTimer = new Timer(DELAY_MS, e -> {
      if (currentVisualValue == targetVisual) {
        stopAnimation();
        return;
      }

      int direction = Integer.compare(targetVisual, currentVisualValue);
      currentVisualValue += direction * STEP;

      if ((direction > 0 && currentVisualValue > targetVisual) ||
        (direction < 0 && currentVisualValue < targetVisual)) {
        currentVisualValue = targetVisual;
      }

      super.setValue(currentVisualValue);
    });

    animationTimer.start();
  }

  private void stopAnimation ()
  {
    if (animationTimer != null && animationTimer.isRunning()) {
      animationTimer.stop();
    }
  }
}