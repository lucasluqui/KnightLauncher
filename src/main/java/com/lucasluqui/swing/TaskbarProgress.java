package com.lucasluqui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;

/**
 * Mirrors a progress bar onto the window's taskbar button, so progress can be followed even with the
 * window in the background or minimized. Only does anything where the platform supports it.
 */
public final class TaskbarProgress
{
  private TaskbarProgress ()
  {
    // empty.
  }

  /**
   * Keeps the taskbar button of the window holding the bar in sync with it.
   */
  public static void follow (JProgressBar bar)
  {
    if (!isSupported()) return;

    Tracker tracker = new Tracker(bar);
    bar.addChangeListener(e -> tracker.update());
    bar.addPropertyChangeListener("indeterminate", e -> tracker.update());
    bar.addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentShown (ComponentEvent e)
      {
        tracker.update();
      }

      @Override
      public void componentHidden (ComponentEvent e)
      {
        tracker.update();
      }
    });
    // The window it's in being shown again (like coming back from the tray) gets a fresh taskbar button,
    // which has to be told again.
    bar.addHierarchyListener(e -> {
      if ((e.getChangeFlags() & (HierarchyEvent.SHOWING_CHANGED | HierarchyEvent.PARENT_CHANGED)) != 0) {
        tracker.refresh();
      }
    });
  }

  private static boolean isSupported ()
  {
    try {
      return Taskbar.isTaskbarSupported()
        && Taskbar.getTaskbar().isSupported(Taskbar.Feature.PROGRESS_VALUE_WINDOW)
        && Taskbar.getTaskbar().isSupported(Taskbar.Feature.PROGRESS_STATE_WINDOW);
    } catch (Exception e) {
      return false;
    }
  }

  private static final class Tracker
  {
    private final JProgressBar bar;

    /**
     * What was last sent to the taskbar.
     */
    private Window lastWindow;
    private Taskbar.State lastState = Taskbar.State.OFF;
    private int lastPercent = -1;

    Tracker (JProgressBar bar)
    {
      this.bar = bar;
    }

    void update ()
    {
      runOnEdt(() -> apply(false));
    }

    /**
     * Sends the current state.
     */
    void refresh ()
    {
      runOnEdt(() -> apply(true));
    }

    private static void runOnEdt (Runnable runnable)
    {
      if (SwingUtilities.isEventDispatchThread()) {
        runnable.run();
      } else {
        SwingUtilities.invokeLater(runnable);
      }
    }

    private void apply (boolean force)
    {
      Window window = SwingUtilities.getWindowAncestor(bar);
      if (window != lastWindow) {
        // Moved to another window (or out of one), leave the old one clean.
        if (lastWindow != null) send(lastWindow, Taskbar.State.OFF, -1);
        lastWindow = window;
        lastState = Taskbar.State.OFF;
        lastPercent = -1;
      }
      if (window == null || !window.isShowing()) return;

      // Goes by the bar's own visibility rather than whether it's on screen right now: a task still runs while
      // the user is looking at another screen that hides the panel the bar is on.
      Taskbar.State state;
      int percent = -1;
      if (!bar.isVisible()) {
        state = Taskbar.State.OFF;
      } else if (bar.isIndeterminate()) {
        state = Taskbar.State.INDETERMINATE;
      } else {
        percent = (int) Math.round(bar.getPercentComplete() * 100);
        // A full bar is a finished task, which the taskbar shouldn't keep showing.
        state = percent >= 100 ? Taskbar.State.OFF : Taskbar.State.NORMAL;
        if (state == Taskbar.State.OFF) percent = -1;
      }

      if (!force && state == lastState && percent == lastPercent) return;
      lastState = state;
      lastPercent = percent;
      send(window, state, percent);
    }

    private static void send (Window window, Taskbar.State state, int percent)
    {
      // The taskbar button only exists once the window has a native peer.
      if (!window.isDisplayable()) return;
      try {
        Taskbar taskbar = Taskbar.getTaskbar();
        if (state == Taskbar.State.NORMAL) {
          taskbar.setWindowProgressValue(window, percent);
        } else {
          taskbar.setWindowProgressState(window, state);
        }
      } catch (Exception ignored) {
        // never worth failing over.
      }
    }
  }
}
