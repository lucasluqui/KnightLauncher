package com.lucasluqui.swing;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * A text pane that lays out and draws its text with fractional metrics, like the labels from
 * {@link FractionalMetrics} do, so it isn't jagged at in-between display scales.
 * <p>
 * Swing's default text views measure glyphs in whole pixels, so turning fractional metrics on only for painting
 * would make the text drift from where it was laid out. Instead, the document is flagged for Swing's i18n layout,
 * which lays out, hit tests and draws every run with a {@link java.awt.font.TextLayout} made from this pane's
 * font render context, and that context gets fractional metrics on.
 */
public class FractionalTextPane extends JTextPane
{
  @Override
  public void setDocument (Document doc)
  {
    // Has to be set before the views are built for the document, Swing picks the layout strategy then.
    if (doc != null) doc.putProperty(I18N, Boolean.TRUE);
    super.setDocument(doc);
  }

  @Override
  public FontMetrics getFontMetrics (Font font)
  {
    FontMetrics base = super.getFontMetrics(font);
    GraphicsConfiguration gc = getGraphicsConfiguration();
    AffineTransform tx = gc == null ? null : gc.getDefaultTransform();
    Object aaHint = getClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING);
    if (aaHint == null) aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
    return new Metrics(base, new FontRenderContext(tx, aaHint, RenderingHints.VALUE_FRACTIONALMETRICS_ON));
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    if (g instanceof Graphics2D) {
      Object aaHint = getClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING);
      if (aaHint != null) ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
    }
    FractionalMetrics.apply(g);
    super.paintComponent(g);
  }

  /**
   * The component's usual metrics, but carrying a fractional metrics render context.
   */
  private static final class Metrics extends FontMetrics
  {
    Metrics (FontMetrics base, FontRenderContext frc)
    {
      super(base.getFont());
      _base = base;
      _frc = frc;
    }

    @Override public FontRenderContext getFontRenderContext () { return _frc; }

    @Override public int getAscent () { return _base.getAscent(); }
    @Override public int getDescent () { return _base.getDescent(); }
    @Override public int getLeading () { return _base.getLeading(); }
    @Override public int getMaxAscent () { return _base.getMaxAscent(); }
    @Override public int getMaxDescent () { return _base.getMaxDescent(); }
    @Override public int getMaxAdvance () { return _base.getMaxAdvance(); }
    @Override public int charWidth (char ch) { return _base.charWidth(ch); }
    @Override public int charWidth (int codePoint) { return _base.charWidth(codePoint); }
    @Override public int charsWidth (char[] data, int off, int len) { return _base.charsWidth(data, off, len); }
    @Override public int stringWidth (String str) { return _base.stringWidth(str); }
    @Override public int[] getWidths () { return _base.getWidths(); }

    private final FontMetrics _base;
    private final FontRenderContext _frc;
  }

  // AbstractDocument.I18NProperty, which is package private.
  private static final String I18N = "i18n";
}
