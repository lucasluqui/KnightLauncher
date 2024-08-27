package com.luuqui.launcher;

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

  private static void specialKeyPressed() {
    LauncherGUI.specialKeyPressed();
  }

  private static void specialKeyReleased() {
    LauncherGUI.specialKeyReleased();
  }

  private static void shiftPressed() {
    shiftPressed = true;
  }

  private static void shiftReleased() {
    shiftPressed = false;
  }

  private static void altPressed() {
    altPressed = true;
  }

  private static void altReleased() {
    altPressed = false;
  }

  public static Boolean isShiftPressed() { return shiftPressed; }
  public static Boolean isAltPressed() { return altPressed; }

}
