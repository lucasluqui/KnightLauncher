package com.lucasallegri.util;

import com.lucasallegri.launcher.Colors;
import com.lucasallegri.launcher.settings.Settings;

import java.awt.*;

public class ColorUtil {

  public static Color getTitleBarColor() {
    Color c = Settings.launcherStyle.equals("dark") ? Colors.INTERFACE_TITLE_BAR_DARK : Colors.INTERFACE_TITLE_BAR_LIGHT;
    return c;
  }

  public static Color getForegroundColor() {
    Color c = Settings.launcherStyle.equals("dark") ? Color.WHITE : Color.BLACK;
    return c;
  }

  public static Color getBackgroundColor() {
    Color c = Settings.launcherStyle.equals("dark") ? Colors.INTERFACE_PRIMARY_DARK : Color.WHITE;
    return c;
  }

  public static Color getGreenForegroundColor() {
    Color c = Settings.launcherStyle.equals("dark") ? Colors.BRIGHT_GREEN : Colors.DARK_GREEN;
    return c;
  }

  public static Color getRedForegroundColor() {
    Color c = Settings.launcherStyle.equals("dark") ? Colors.BRIGHT_RED : Colors.DARK_RED;
    return c;
  }

}
