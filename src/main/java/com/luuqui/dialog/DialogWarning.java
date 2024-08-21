package com.luuqui.dialog;

import com.luuqui.launcher.Locale;

import javax.swing.*;

public class DialogWarning {

  public static void push(String msg, String title) {
    JOptionPane.showMessageDialog(null,
      msg,
      title,
      JOptionPane.WARNING_MESSAGE
    );
  }

  public static void push(String msg) {
   push(msg, "Knight Launcher Warning");
  }

  public static void pushTranslated(String msg) {
    push(msg, Locale.getValue("t.warning"));
  }

}
