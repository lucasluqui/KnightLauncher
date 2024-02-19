package com.luuqui.dialog;

import com.luuqui.launcher.Locale;

import javax.swing.*;

public class DialogInfo {

  public static void push(String msg) {
    JOptionPane.showMessageDialog(null,
      msg,
      "Knight Launcher Information",
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  public static void push(String msg, String title) {
    JOptionPane.showMessageDialog(null,
      msg,
      title,
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  public static void pushTranslated(String msg) {
    JOptionPane.showMessageDialog(null,
      msg,
      Locale.getValue("t.info"),
      JOptionPane.INFORMATION_MESSAGE
    );
  }

}
