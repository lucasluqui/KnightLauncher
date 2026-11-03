package com.lucasluqui.swing;

import javax.swing.*;
import java.awt.*;

/**
 * A scroll pane whose wheel scrolling glides and slows down to a stop, instead of jumping.
 * <p>
 * The motion is worked out from the time that actually passed between frames, so it covers the same distance
 * in the same time whatever the monitor's refresh rate is, and a frame that takes longer than usual doesn't
 * slow it down. The refresh rate of the screen it's on only decides how often it updates. Speeds and friction are given per frame
 * at 60 FPS, which is what they were originally tuned at.
 */
public final class SmoothScrollPane extends JScrollPane
{
  public SmoothScrollPane ()
  {
    this(null);
  }

  public SmoothScrollPane (JComponent view)
  {
    super(view);

    setWheelScrollingEnabled(false); // disable default jswing scrolling.

    JScrollBar vBar = getVerticalScrollBar();
    _posY = vBar.getValue();

    // Delays are set to the screen's refresh rate whenever they start, see frameInterval().
    _animator = new Timer(1000 / FALLBACK_REFRESH_RATE, e -> animate());
    _animator.setRepeats(true);

    _scrollToAnimator = new Timer(1000 / FALLBACK_REFRESH_RATE, e -> animateScrollTo());
    _scrollToAnimator.setRepeats(true);

    // prevents authoritative scrolling from getting borked.
    vBar.addAdjustmentListener(e -> {
      if (e.getValueIsAdjusting()) {
        _velY = 0;
        _posY = e.getValue();
        _animator.stop();
        _scrollToAnimator.stop();
      }
    });

    enableSmoothScrolling();
  }

  private void enableSmoothScrolling ()
  {
    addMouseWheelListener(e -> {
      if (e.isControlDown()) return;

      e.consume();
      _scrollToAnimator.stop();

      // While gliding, the position keeps its fractions of a pixel; only start over from the bar when idle.
      if (!_animator.isRunning()) {
        _posY = getVerticalScrollBar().getValue();
      }

      double maxVel = toVelocity(_maxVel);
      _velY += toVelocity(e.getPreciseWheelRotation() * _scrollSpeed);
      _velY = Math.max(-maxVel, Math.min(_velY, maxVel));

      if (!_animator.isRunning()) {
        _lastFrame = System.nanoTime();
        _animator.setDelay(frameInterval());
        _animator.start();
      }
    });
  }

  private void animate ()
  {
    double dt = frameTime();

    // Friction slows it down exponentially, so the distance covered and the speed left after dt
    // come straight from integrating that, however long dt is.
    double decay = decayRate(_friction);
    double remaining = Math.exp(-decay * dt);
    _posY += _velY * (1 - remaining) / decay;
    _velY *= remaining;

    clampPosition();
    getVerticalScrollBar().setValue((int) Math.round(_posY));

    // Stopped, or ran into either end.
    if (Math.abs(_velY) < STOP_VELOCITY || isAtLimit()) {
      _velY = 0;
      _animator.stop();
    }
  }

  /**
   * Eases the view to the given vertical position, e.g. for a back to top button.
   * Scrolling with the wheel or grabbing the scroll bar interrupts it.
   */
  public void smoothScrollTo (int y)
  {
    _velY = 0;
    _animator.stop();

    JScrollBar vBar = getVerticalScrollBar();
    _scrollToY = Math.max(vBar.getMinimum(), Math.min(y, vBar.getMaximum() - vBar.getVisibleAmount()));

    if (!_scrollToAnimator.isRunning()) {
      _posY = vBar.getValue();
      _lastFrame = System.nanoTime();
      _scrollToAnimator.setDelay(frameInterval());
      _scrollToAnimator.start();
    }
  }

  /**
   * Jumps straight to the given vertical position, stopping any scrolling that was still in motion so it
   * doesn't carry on from there.
   */
  public void scrollToImmediately (int y)
  {
    _velY = 0;
    _animator.stop();
    _scrollToAnimator.stop();

    _posY = y;
    clampPosition();
    getVerticalScrollBar().setValue((int) Math.round(_posY));
  }

  private void animateScrollTo ()
  {
    double dt = frameTime();

    // Closes the same share of the remaining distance in the same time, whatever the frame rate.
    double remaining = (_scrollToY - _posY) * Math.exp(-decayRate(SCROLL_TO_FRICTION) * dt);

    if (Math.abs(remaining) < 0.5) {
      _posY = _scrollToY;
      getVerticalScrollBar().setValue(_scrollToY);
      _scrollToAnimator.stop();
      return;
    }

    _posY = _scrollToY - remaining;
    getVerticalScrollBar().setValue((int) Math.round(_posY));
  }

  /**
   * Milliseconds between frames, so it updates once per refresh of the screen it's currently on.
   * Only affects how smooth it looks, not how fast it goes.
   */
  private int frameInterval ()
  {
    int refreshRate = DisplayMode.REFRESH_RATE_UNKNOWN;
    try {
      GraphicsConfiguration config = getGraphicsConfiguration();
      if (config != null) refreshRate = config.getDevice().getDisplayMode().getRefreshRate();
    } catch (Exception ignored) {
      // Can't tell, the fallback will do.
    }
    if (refreshRate <= 0) refreshRate = FALLBACK_REFRESH_RATE;
    return Math.max(1, 1000 / refreshRate);
  }

  /**
   * Seconds since the last frame. Capped, so a long stall (e.g. the window being dragged) doesn't make it
   * jump all at once when it resumes.
   */
  private double frameTime ()
  {
    long now = System.nanoTime();
    double dt = (now - _lastFrame) / 1_000_000_000.0;
    _lastFrame = now;
    return Math.min(Math.max(dt, 0), MAX_FRAME_TIME);
  }

  /**
   * Turns friction, the share of speed kept each frame at the reference frame rate, into how fast it decays per second.
   */
  private static double decayRate (double friction)
  {
    return -Math.log(friction) * REFERENCE_FPS;
  }

  /**
   * Turns a speed in pixels per frame at the reference frame rate into pixels per second, adjusted so it glides
   * just as far as stepping frame by frame at that rate would: decaying continuously covers a bit less ground
   * than decaying in steps does.
   */
  private double toVelocity (double pixelsPerFrame)
  {
    return pixelsPerFrame * decayRate(_friction) / (1 - _friction);
  }

  private void clampPosition ()
  {
    JScrollBar vBar = getVerticalScrollBar();
    int min = vBar.getMinimum();
    int max = vBar.getMaximum() - vBar.getVisibleAmount();
    _posY = Math.max(min, Math.min(_posY, max));
  }

  private boolean isAtLimit ()
  {
    JScrollBar vBar = getVerticalScrollBar();
    return (_velY < 0 && _posY <= vBar.getMinimum())
      || (_velY > 0 && _posY >= vBar.getMaximum() - vBar.getVisibleAmount());
  }

  /**
   * Share of the speed kept from one frame to the next, at 60 FPS.
   */
  public double getFriction ()
  {
    return this._friction;
  }

  public void setFriction (double friction)
  {
    this._friction = friction;
  }

  /**
   * In pixels per frame, at 60 FPS.
   */
  public double getMaxVelocity ()
  {
    return this._maxVel;
  }

  public void setMaxVelocity (double maxVelocity)
  {
    this._maxVel = maxVelocity;
  }

  /**
   * Speed added per wheel notch, in pixels per frame, at 60 FPS.
   */
  public int getScrollSpeed ()
  {
    return this._scrollSpeed;
  }

  public void setScrollSpeed (int scrollSpeed)
  {
    this._scrollSpeed = scrollSpeed;
  }

  private final Timer _animator;
  private final Timer _scrollToAnimator;
  private int _scrollToY;
  private double _friction = 0.80;
  private double _maxVel = 60;
  private int _scrollSpeed = 36;

  // Position in pixels, kept with fractions so slow movement doesn't get stuck on rounding,
  // and speed in pixels per second.
  private double _posY;
  private double _velY = 0.0;
  private long _lastFrame;

  // The frame rate speeds and friction are given for.
  private static final double REFERENCE_FPS = 60.0;

  // Stops gliding below half a pixel per frame at 60 FPS.
  private static final double STOP_VELOCITY = 0.5 * REFERENCE_FPS;

  // The back to top easing covers 20% of the remaining distance per frame at 60 FPS.
  private static final double SCROLL_TO_FRICTION = 0.80;

  private static final double MAX_FRAME_TIME = 0.1;

  // Used when the screen doesn't report its refresh rate.
  private static final int FALLBACK_REFRESH_RATE = 60;
}
