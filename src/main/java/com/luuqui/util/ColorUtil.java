package com.luuqui.util;

import com.luuqui.launcher.Colors;
import com.luuqui.launcher.settings.Settings;

import java.awt.*;

public class ColorUtil {

  public static Color getTitleBarColor() {
    return Settings.launcherStyle.equals("dark") ? Colors.INTERFACE_TITLE_BAR_DARK : Colors.INTERFACE_TITLE_BAR_LIGHT;
  }

  public static Color getForegroundColor() {
    return Settings.launcherStyle.equals("dark") ? Color.WHITE : Color.BLACK;
  }

  public static Color getBackgroundColor() {
    return Settings.launcherStyle.equals("dark") ? Colors.INTERFACE_PRIMARY_DARK : Color.WHITE;
  }

  public static Color getGreenForegroundColor() {
    return Settings.launcherStyle.equals("dark") ? Colors.BRIGHT_GREEN : Colors.DARK_GREEN;
  }

  public static Color getRedForegroundColor() {
    return Settings.launcherStyle.equals("dark") ? Colors.BRIGHT_RED : Colors.DARK_RED;
  }

}
