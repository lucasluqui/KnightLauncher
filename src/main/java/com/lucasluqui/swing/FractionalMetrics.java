package com.lucasluqui.swing;

import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatCheckBoxUI;
import com.formdev.flatlaf.ui.FlatLabelUI;
import com.formdev.flatlaf.ui.FlatMenuItemUI;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.ui.FlatToolTipUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Draws text with fractional metrics.
 * <p>
 * Since Java 9, Java scales the UI itself when Windows' display scale is above 100%, instead of Windows stretching the
 * whole window. At in-between scales (125%, 150%...) font sizes end up fractional, and without fractional metrics
 * glyphs get snapped to whole pixels: they look taller than they should and jagged. Fractional metrics keep them at
 * their real size and shape.
 * <p>
 * Swing has no global switch for it, so it's set by the UI delegates of the components that show text. Text
 * components (text fields, text areas...) are left out on purpose: Swing places their caret and selection using
 * whole-pixel metrics, which would then drift away from the text.
 * <p>
 * Custom painting code drawing text should call {@link #apply(Graphics)} too, before getting any font metrics.
 */
public final class FractionalMetrics
{
  private FractionalMetrics ()
  {}

  /**
   * Replaces FlatLaf's UI delegates with ones that draw text with fractional metrics.
   * Call it right after setting the look and feel.
   */
  public static void install ()
  {
    UIManager.put("LabelUI", Label.class.getName());
    UIManager.put("ButtonUI", Button.class.getName());
    UIManager.put("CheckBoxUI", CheckBox.class.getName());
    UIManager.put("MenuItemUI", MenuItem.class.getName());
    UIManager.put("TabbedPaneUI", TabbedPane.class.getName());
    UIManager.put("ToolTipUI", ToolTip.class.getName());
  }

  public static void apply (Graphics g)
  {
    if (g instanceof Graphics2D) {
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }
  }

  // Delegates are never shared between components (FlatLaf's "shared" flag is false), since components can be
  // styled individually through FlatClientProperties.STYLE, which shared delegates don't support.

  public static class Label extends FlatLabelUI
  {
    public Label ()
    {
      super(false);
    }

    public static ComponentUI createUI (JComponent c)
    {
      return new Label();
    }

    @Override
    public void update (Graphics g, JComponent c)
    {
      apply(g);
      super.update(g, c);
    }
  }

  public static class Button extends FlatButtonUI
  {
    public Button ()
    {
      super(false);
    }

    public static ComponentUI createUI (JComponent c)
    {
      return new Button();
    }

    @Override
    public void update (Graphics g, JComponent c)
    {
      apply(g);
      super.update(g, c);
    }
  }

  public static class CheckBox extends FlatCheckBoxUI
  {
    public CheckBox ()
    {
      super(false);
    }

    public static ComponentUI createUI (JComponent c)
    {
      return new CheckBox();
    }

    @Override
    public void update (Graphics g, JComponent c)
    {
      apply(g);
      super.update(g, c);
    }
  }

  public static class MenuItem extends FlatMenuItemUI
  {
    public static ComponentUI createUI (JComponent c)
    {
      return new MenuItem();
    }

    @Override
    public void update (Graphics g, JComponent c)
    {
      apply(g);
      super.update(g, c);
    }
  }

  public static class TabbedPane extends FlatTabbedPaneUI
  {
    public static ComponentUI createUI (JComponent c)
    {
      return new TabbedPane();
    }

    @Override
    public void update (Graphics g, JComponent c)
    {
      apply(g);
      super.update(g, c);
    }
  }

  public static class ToolTip extends FlatToolTipUI
  {
    public static ComponentUI createUI (JComponent c)
    {
      return new ToolTip();
    }

    @Override
    public void update (Graphics g, JComponent c)
    {
      apply(g);
      super.update(g, c);
    }
  }
}
