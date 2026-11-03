package com.lucasluqui.swing;

import javax.swing.text.DefaultCaret;

/**
 * A caret that's never drawn, for read-only text that should still be selectable and copyable.
 */
public class HiddenCaret extends DefaultCaret
{
  public HiddenCaret ()
  {
    // Clicking or selecting shouldn't scroll the component around either.
    setUpdatePolicy(NEVER_UPDATE);
  }

  @Override
  public void setVisible (boolean visible)
  {
    super.setVisible(false);
  }
}
