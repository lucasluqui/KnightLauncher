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
import java.awt.font.FontRenderContext;

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
 * whole-pixel metrics, which would then drift away from the text. Use the ones in {@link FractionalText} instead.
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

    // Labels that ignore the mouse don't count as being under it, so they don't hide the cursor of controls they
    // overlap. Mouse events skip them already.
    @Override
    public boolean contains (JComponent c, int x, int y)
    {
      return HandCursor.takesMouse(c) && super.contains(c, x, y);
    }

    @Override
    public void update (Graphics g, JComponent c)
    {
      apply(g);
      super.update(g, c);
    }

    // Both the preferred size and the text's position when painting come from here.
    // Measuring with the same metrics the text is drawn with keeps it from overflowing
    // the label and getting cut off.
    @Override
    protected String layoutCL (JLabel label, FontMetrics fontMetrics, String text, Icon icon,
                               Rectangle viewR, Rectangle iconR, Rectangle textR)
    {
      return super.layoutCL(label, new Metrics(fontMetrics), text, icon, viewR, iconR, textR);
    }
  }

  /**
   * Swing measures text with whole-pixel metrics at 100% scale which won't match text drawn with fractional
   * metrics at the actual scale; it can come out wider than measured. This measures widths the way they're drawn,
   * also counting glyphs that hang past their advance (italics), and rounds up.
   */
  private static final class Metrics extends FontMetrics
  {
    Metrics (FontMetrics base)
    {
      super(base.getFont());
      _base = base;
    }

    @Override
    public int stringWidth (String str)
    {
      if (str == null || str.isEmpty()) return 0;
      double advance = font.getStringBounds(str, FRC).getWidth();
      double visual = font.createGlyphVector(FRC, str).getVisualBounds().getMaxX();
      return (int) Math.ceil(Math.max(advance, visual));
    }

    @Override
    public int charsWidth (char[] data, int off, int len)
    {
      return stringWidth(new String(data, off, len));
    }

    @Override
    public int charWidth (char ch)
    {
      return stringWidth(String.valueOf(ch));
    }

    @Override
    public int charWidth (int codePoint)
    {
      return stringWidth(new String(Character.toChars(codePoint)));
    }

    @Override
    public FontRenderContext getFontRenderContext ()
    {
      return FRC;
    }

    @Override
    public int getAscent ()
    {
      return _base.getAscent();
    }

    @Override
    public int getDescent ()
    {
      return _base.getDescent();
    }

    @Override
    public int getLeading ()
    {
      return _base.getLeading();
    }

    @Override
    public int getMaxAscent ()
    {
      return _base.getMaxAscent();
    }

    @Override
    public int getMaxDescent ()
    {
      return _base.getMaxDescent();
    }

    @Override
    public int getMaxAdvance ()
    {
      return _base.getMaxAdvance();
    }

    private final FontMetrics _base;

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
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
    public void installUI (JComponent c)
    {
      super.installUI(c);
      HandCursor.install(c);
    }

    @Override
    public void uninstallUI (JComponent c)
    {
      HandCursor.uninstall(c);
      super.uninstallUI(c);
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
    public void installUI (JComponent c)
    {
      super.installUI(c);
      HandCursor.install(c);
    }

    @Override
    public void uninstallUI (JComponent c)
    {
      HandCursor.uninstall(c);
      super.uninstallUI(c);
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
    public void installUI (JComponent c)
    {
      super.installUI(c);
      HandCursor.install(c);
    }

    @Override
    public void uninstallUI (JComponent c)
    {
      HandCursor.uninstall(c);
      super.uninstallUI(c);
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
    public void installUI (JComponent c)
    {
      super.installUI(c);
      HandCursor.installTabs((JTabbedPane) c);
    }

    @Override
    public void uninstallUI (JComponent c)
    {
      HandCursor.uninstallTabs((JTabbedPane) c);
      super.uninstallUI(c);
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
