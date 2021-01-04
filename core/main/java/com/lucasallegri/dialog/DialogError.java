package com.lucasallegri.dialog;

import com.lucasallegri.launcher.LanguageManager;

import javax.swing.*;

public class DialogError {

  public static void push(String msg) {
    JOptionPane.showMessageDialog(null,
            msg,
            "Knight Launcher Error",
            JOptionPane.ERROR_MESSAGE
    );
  }

  public static void pushTranslated(String msg) {
    JOptionPane.showMessageDialog(null,
            msg,
            LanguageManager.getValue("t.error"),
            JOptionPane.ERROR_MESSAGE
    );
  }

}
