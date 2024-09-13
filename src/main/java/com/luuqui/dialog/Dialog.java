package com.luuqui.dialog;

import com.luuqui.launcher.CustomColors;
import com.luuqui.launcher.Fonts;
import com.luuqui.launcher.Locale;

import javax.swing.*;

public class Dialog {

  public static void push(String msg, String title, int messageType) {
    JOptionPane.showMessageDialog(null,
      formatMessage(msg),
      title,
      messageType
    );
  }

  public static void push(String msg, int messageType) {
    push(msg, "Knight Launcher", messageType);
  }

  public static boolean pushWithConfirm(String msg, String title, int messageType) {
    int reply = JOptionPane.showConfirmDialog(null, formatMessage(msg), title, JOptionPane.YES_NO_OPTION, messageType);
    return reply == JOptionPane.YES_OPTION;
  }

  public static boolean pushWithConfirm(String msg, int messageType) {
    return pushWithConfirm(msg, "Knight Launcher", messageType);
  }

  private static JTextArea formatMessage(String msg) {
    JTextArea message = new JTextArea(msg);
    message.setFont(Fonts.fontReg);
    message.setForeground(CustomColors.INTERFACE_DEFAULT);
    message.setBackground(CustomColors.INTERFACE_DEFAULT_BACKGROUND);
    message.setEditable(false);
    return message;
  }

}
