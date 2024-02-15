package com.luuqui.dialog;

import com.luuqui.launcher.Locale;

import javax.swing.*;

public class DialogWarning {

  public static void push(String msg) {
    JOptionPane.showMessageDialog(null,
            msg,
            "Knight Launcher Warning",
            JOptionPane.WARNING_MESSAGE
    );
  }

  public static void pushTranslated(String msg) {
    JOptionPane.showMessageDialog(null,
            msg,
            Locale.getValue("t.warning"),
            JOptionPane.WARNING_MESSAGE
    );
  }

}
