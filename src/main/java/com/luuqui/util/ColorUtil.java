package com.luuqui.util;

import com.luuqui.launcher.CustomColors;

import java.awt.*;

public class ColorUtil
{

  public static Color getTitleBarColor ()
  {
    return CustomColors.INTERFACE_TITLEBAR;
  }

  public static Color getForegroundColor ()
  {
    return Color.WHITE;
  }

  public static Color getBackgroundColor ()
  {
    return CustomColors.INTERFACE_MAINPANE_BACKGROUND;
  }

  public static Color getGreenForegroundColor ()
  {
    return CustomColors.BRIGHT_GREEN;
  }

  public static Color getRedForegroundColor ()
  {
    return CustomColors.BRIGHT_RED;
  }

  public static String colorToHexString (Color color)
  {
    return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
  }

}
