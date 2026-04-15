package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.launcher.ui.BaseUI;

import java.awt.*;
import java.awt.event.KeyEvent;

@Singleton
public class KeyboardController
{
  public KeyboardController ()
  {}

  public void init ()
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
      switch (ke.getKeyCode()) {

        // SHIFT key event.
        case KeyEvent.VK_SHIFT:
          if (ke.getID() == KeyEvent.KEY_PRESSED) {
            shiftPressed();
            specialKeyPressed();
          }
          if (ke.getID() == KeyEvent.KEY_RELEASED) {
            shiftReleased();
            specialKeyReleased();
          }
          break;

        // ALT key event.
        case KeyEvent.VK_ALT:
          if (ke.getID() == KeyEvent.KEY_PRESSED) {
            altPressed();
            specialKeyPressed();
          }
          if (ke.getID() == KeyEvent.KEY_RELEASED) {
            altReleased();
            specialKeyReleased();
          }
          break;

        // ESC key event.
        case KeyEvent.VK_ESCAPE:
          if (ke.getID() == KeyEvent.KEY_RELEASED) {
            _ctx.getApp().returnToHome();
          }
          break;
      }
      return false;
    });
  }

  private void specialKeyPressed ()
  {
    for (BaseUI ui : _ctx.getApp().getUIMap().values()) {
      ui.specialKeyPressed();
    }
  }

  private void specialKeyReleased ()
  {
    for (BaseUI ui : _ctx.getApp().getUIMap().values()) {
      ui.specialKeyReleased();
    }
  }

  private void shiftPressed ()
  {
    this.shiftPressed = true;
  }

  private void shiftReleased ()
  {
    this.shiftPressed = false;
  }

  private void altPressed ()
  {
    this.altPressed = true;
  }

  private void altReleased ()
  {
    this.altPressed = false;
  }

  public Boolean isShiftPressed ()
  {
    return this.shiftPressed;
  }

  public Boolean isAltPressed ()
  {
    return this.altPressed;
  }

  public void clear ()
  {
    this.altPressed = false;
    this.shiftPressed = false;
    specialKeyReleased();
  }

  @Inject protected LauncherContext _ctx;

  private Boolean shiftPressed = false;
  private Boolean altPressed = false;
}
