package com.lucasluqui.swing;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for use as a scroll pane's view that always fits the viewport's width and only scrolls vertically,
 * letting wrapping children such as text panes grow downwards instead of sideways.
 */
public class ScrollablePanel extends JPanel implements Scrollable
{
  @Override
  public void doLayout ()
  {
    super.doLayout();

    // Wrapping children only know their height once they've been given a width, so lay out once more
    // whenever ours changes.
    if (getWidth() != _laidOutWidth) {
      _laidOutWidth = getWidth();
      revalidate();
    }
  }

  @Override
  public Dimension getPreferredScrollableViewportSize ()
  {
    return getPreferredSize();
  }

  @Override
  public int getScrollableUnitIncrement (Rectangle visibleRect, int orientation, int direction)
  {
    return Scale.px(16);
  }

  @Override
  public int getScrollableBlockIncrement (Rectangle visibleRect, int orientation, int direction)
  {
    return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
  }

  @Override
  public boolean getScrollableTracksViewportWidth ()
  {
    return true;
  }

  @Override
  public boolean getScrollableTracksViewportHeight ()
  {
    return false;
  }

  private int _laidOutWidth = -1;
}
