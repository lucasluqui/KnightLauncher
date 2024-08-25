package com.luuqui.dialog;

import com.luuqui.launcher.CustomColors;
import com.luuqui.launcher.Fonts;
import com.luuqui.launcher.Locale;

import javax.swing.*;

public class Dialog {

  public static void push(String msg, String title, int messageType) {
    JTextArea message = new JTextArea(msg);
    message.setFont(Fonts.fontReg);
    message.setForeground(CustomColors.INTERFACE_DEFAULT);
    message.setBackground(CustomColors.INTERFACE_DEFAULT_BACKGROUND);
    message.setEditable(false);
    JOptionPane.showMessageDialog(null,
      message,
      title,
      messageType
    );
  }

  public static void push(String msg, int messageType) {
    push(msg, "Knight Launcher", messageType);
  }

}
