package com.lucasluqui.dialog;

import com.lucasluqui.launcher.BuildConfig;
import com.lucasluqui.launcher.CustomColors;
import com.lucasluqui.launcher.Fonts;
import com.lucasluqui.launcher.LauncherGlobals;

import javax.swing.*;
import java.awt.*;

public class Dialog
{

  @SuppressWarnings("all")
  public static void push (String msg, String title, int messageType)
  {
    JOptionPane.showMessageDialog(null,
      formatMessage(msg),
      title,
      messageType
    );
  }

  public static void push (String msg, int messageType)
  {
    push(msg, BuildConfig.getName(), messageType);
  }

  public static boolean pushWithConfirm (String msg, String title, int messageType)
  {
    int reply = JOptionPane.showConfirmDialog(
        null, formatMessage(msg), title, JOptionPane.YES_NO_OPTION, messageType);
    return reply == JOptionPane.YES_OPTION;
  }

  @SuppressWarnings("unused")
  public static boolean pushWithConfirm (String msg, int messageType)
  {
    return pushWithConfirm(msg, BuildConfig.getName(), messageType);
  }

  private static JTextArea formatMessage (String msg)
  {
    JTextArea message = new JTextArea(msg);
    message.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    message.setForeground(CustomColors.INTERFACE_DEFAULT);
    message.setBackground(CustomColors.INTERFACE_DEFAULT_BACKGROUND);
    message.setEditable(false);
    return message;
  }

}
