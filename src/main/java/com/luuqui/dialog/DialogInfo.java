package com.luuqui.dialog;

import com.luuqui.launcher.LauncherGUI;
import com.luuqui.launcher.Locale;

import javax.swing.*;

public class DialogInfo {

  public static void push(String msg, String title) {
    JOptionPane.showMessageDialog(null,
      msg,
      title,
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  public static void push(String msg) {
    push(msg, "Knight Launcher Information");
  }

  public static void pushTranslated(String msg) {
    push(msg, Locale.getValue("t.info"));
  }

}
