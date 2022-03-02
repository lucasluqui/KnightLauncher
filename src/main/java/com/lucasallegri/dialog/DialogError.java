package com.lucasallegri.dialog;

import com.lucasallegri.launcher.Locale;

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
            Locale.getValue("t.error"),
            JOptionPane.ERROR_MESSAGE
    );
  }

}
