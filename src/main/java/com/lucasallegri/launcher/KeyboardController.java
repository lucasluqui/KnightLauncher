package com.lucasallegri.launcher;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KeyboardController {

  private static Boolean shiftPressed = false;
  private static Boolean altPressed = false;

  public static void start() {

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
      switch (ke.getID()) {

        case KeyEvent.KEY_PRESSED:
          if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
          } else if (ke.getKeyCode() == KeyEvent.VK_ALT) {
            altPressed = true;
          }
          break;

        case KeyEvent.KEY_RELEASED:
          if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
          } else if (ke.getKeyCode() == KeyEvent.VK_ALT) {
            altPressed = false;
          }
          break;
      }
      return false;
    });
  }

  public static Boolean isShiftPressed() { return shiftPressed; }
  public static Boolean isAltPressed() { return altPressed; }

}
