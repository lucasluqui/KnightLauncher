package com.lucasluqui.swing;

import com.formdev.flatlaf.ui.FlatComboBoxUI;
import com.formdev.flatlaf.ui.FlatSliderUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

/**
 * Shows the hand cursor over clickable components while they're enabled.
 * It's set by the UI delegates, so components pick it up on their own. Components that already set a cursor of
 * their own when the delegate is installed are left alone. Setting one after the fact overrides it.
 */
public final class HandCursor
{
  private HandCursor ()
  {}

  /**
   * Replaces FlatLaf's UI delegates for the clickable components {@link FractionalMetrics} doesn't already cover.
   * Call it right after setting the look and feel.
   */
  public static void installDelegates ()
  {
    UIManager.put("ComboBoxUI", ComboBox.class.getName());
    UIManager.put("SliderUI", Slider.class.getName());
  }

  public static void install (JComponent c)
  {
    if (c.isCursorSet()) return;
    c.putClientProperty(KEY, Boolean.TRUE);
    c.addPropertyChangeListener("enabled", ENABLED_LISTENER);
    update(c);
  }

  public static void uninstall (JComponent c)
  {
    if (c.getClientProperty(KEY) == null) return;
    c.putClientProperty(KEY, null);
    c.removePropertyChangeListener("enabled", ENABLED_LISTENER);
    c.setCursor(null);
  }

  /**
   * Only the tabs themselves get the hand cursor, not the tab's content.
   * The listener has to go on the pane. Events over scrolling tabs reach it because the component they're painted
   * on has no mouse listeners of its own; giving it one would keep them from reaching the pane's own handling.
   */
  public static void installTabs (JTabbedPane pane)
  {
    if (pane.isCursorSet()) return;
    TabTracker tracker = new TabTracker(pane);
    pane.putClientProperty(KEY, tracker);
    pane.addMouseListener(tracker);
    pane.addMouseMotionListener(tracker);
  }

  public static void uninstallTabs (JTabbedPane pane)
  {
    if (!(pane.getClientProperty(KEY) instanceof TabTracker tracker)) return;
    pane.putClientProperty(KEY, null);
    pane.removeMouseListener(tracker);
    pane.removeMouseMotionListener(tracker);
    pane.setCursor(null);
  }

  /**
   * Has an overlay show the cursor of whatever's under it.
   * An overlay without mouse listeners lets clicks through to the components below, but the cursor still comes from
   * the overlay, which would hide theirs.
   */
  public static void passThrough (JComponent overlay)
  {
    Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
      MouseEvent me = (MouseEvent) e;
      Container parent = overlay.getParent();
      if (parent == null || !overlay.isShowing()) return;
      if (SwingUtilities.getWindowAncestor(overlay) != windowOf(me.getComponent())) return;

      Point p = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), parent);
      if (!overlay.getBounds().contains(p)) return;

      Component below = componentBelow(parent, overlay, p);
      overlay.setCursor(below != null ? below.getCursor() : null);
    }, AWTEvent.MOUSE_MOTION_EVENT_MASK);
  }

  private static Window windowOf (Component c)
  {
    return c instanceof Window w ? w : SwingUtilities.getWindowAncestor(c);
  }

  private static Component componentBelow (Container parent, Component overlay, Point p)
  {
    for (Component child : parent.getComponents()) {
      if (child == overlay || !child.isVisible()) continue;
      Component c = SwingUtilities.getDeepestComponentAt(child, p.x - child.getX(), p.y - child.getY());
      if (c != null) return c;
    }
    return null;
  }

  private static void update (JComponent c)
  {
    // null inherits the parent's cursor. a disabled component doesn't promise a click.
    c.setCursor(c.isEnabled() ? HAND : null);
  }

  private static final class TabTracker extends MouseAdapter
  {
    TabTracker (JTabbedPane pane)
    {
      this.pane = pane;
    }

    @Override
    public void mouseMoved (MouseEvent e)
    {
      Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), pane);
      int tab = pane.getUI().tabForCoordinate(pane, p.x, p.y);
      boolean clickable = pane.isEnabled() && tab >= 0 && pane.isEnabledAt(tab);
      pane.setCursor(clickable ? HAND : null);
    }

    @Override
    public void mouseExited (MouseEvent e)
    {
      // Moving from the tabs into their content can exit the pane, and the content would inherit its cursor.
      pane.setCursor(null);
    }

    final JTabbedPane pane;
  }

  /**
   * Whether a label should catch the cursor.
   */
  static boolean takesMouse (JComponent c)
  {
    // Children are only found through their parent, so a label holding any has to stay reachable.
    return c.isCursorSet()
      || c.getComponentCount() > 0
      || c.getMouseListeners().length > 0
      || c.getMouseMotionListeners().length > 0
      || c.getMouseWheelListeners().length > 0;
  }

  public static class ComboBox extends FlatComboBoxUI
  {
    public static ComponentUI createUI (JComponent c)
    {
      return new ComboBox();
    }

    @Override
    public void installUI (JComponent c)
    {
      super.installUI(c);
      install(c);
      // The dropdown's items are clickable too. Its arrow is a button, which takes care of itself.
      if (popup != null) popup.getList().setCursor(HAND);
    }

    @Override
    public void uninstallUI (JComponent c)
    {
      uninstall(c);
      super.uninstallUI(c);
    }
  }

  public static class Slider extends FlatSliderUI
  {
    public static ComponentUI createUI (JComponent c)
    {
      return new Slider();
    }

    @Override
    public void installUI (JComponent c)
    {
      super.installUI(c);
      install(c);
    }

    @Override
    public void uninstallUI (JComponent c)
    {
      uninstall(c);
      super.uninstallUI(c);
    }
  }

  private static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  private static final String KEY = HandCursor.class.getName();

  private static final PropertyChangeListener ENABLED_LISTENER = e -> update((JComponent) e.getSource());
}
