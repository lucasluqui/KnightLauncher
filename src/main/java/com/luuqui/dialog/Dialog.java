package com.luuqui.dialog;

import com.luuqui.launcher.Locale;

import javax.swing.*;

public class Dialog {

  public static void push(String msg, String title, int messageType) {
    JOptionPane.showMessageDialog(null,
      msg,
      title,
      messageType
    );
  }

  public static void push(String msg, int messageType) {
    push(msg, "Knight Launcher", messageType);
  }

  public static void pushTranslated(String msg, int messageType) {
    push(msg, Locale.getValue("t.info"), messageType);
  }

}
