package com.lucasallegri.dialog;

import com.lucasallegri.launcher.LanguageManager;

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
            LanguageManager.getValue("t.warning"),
            JOptionPane.WARNING_MESSAGE
    );
  }

}
