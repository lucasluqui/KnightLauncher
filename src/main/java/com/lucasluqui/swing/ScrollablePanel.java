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
  public Dimension getPreferredSize ()
  {
    // Wrapping children rewrap while being measured, invalidating us, and with that the layout's cached sizes,
    // halfway through measuring them. Hold that off until the measuring is done.
    _measuring = true;
    try {
      return super.getPreferredSize();
    } finally {
      _measuring = false;
      if (_invalidatedWhileMeasuring) {
        _invalidatedWhileMeasuring = false;
        invalidate();
      }
    }
  }

  @Override
  public void invalidate ()
  {
    if (_measuring) {
      _invalidatedWhileMeasuring = true;
    } else {
      super.invalidate();
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
  private boolean _measuring;
  private boolean _invalidatedWhileMeasuring;
}
