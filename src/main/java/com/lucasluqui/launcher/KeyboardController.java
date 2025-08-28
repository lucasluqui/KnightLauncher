package com.lucasluqui.launcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.awt.*;
import java.awt.event.KeyEvent;

@Singleton
public class KeyboardController
{
  @Inject protected LauncherContext _launcherCtx;

  private Boolean shiftPressed = false;
  private Boolean altPressed = false;

  public KeyboardController ()
  {

  }

  public void init ()
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
      switch (ke.getID()) {
        case KeyEvent.KEY_PRESSED:
          if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed();
            specialKeyPressed();
          } else if (ke.getKeyCode() == KeyEvent.VK_ALT) {
            altPressed();
            specialKeyPressed();
          }
          break;
        case KeyEvent.KEY_RELEASED:
          if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftReleased();
            specialKeyReleased();
          } else if (ke.getKeyCode() == KeyEvent.VK_ALT) {
            altReleased();
            specialKeyReleased();
          }
          break;
      }
      return false;
    });
  }

  private void specialKeyPressed ()
  {
    _launcherCtx.launcherGUI.specialKeyPressed();
  }

  private void specialKeyReleased ()
  {
    _launcherCtx.launcherGUI.specialKeyReleased();
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

}
