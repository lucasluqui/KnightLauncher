package com.lucasallegri.util;

import java.awt.Color;

import com.lucasallegri.launcher.DefaultColors;
import com.lucasallegri.launcher.settings.Settings;

public class ColorUtil {
	
	public static Color getTitleBarColor() {
		Color c = Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_TITLEBAR_DARK : DefaultColors.INTERFACE_TITLEBAR_LIGHT;
		return c;
	}
	
	public static Color getForegroundColor() {
		Color c = Settings.launcherStyle.equals("dark") ? Color.WHITE : Color.BLACK;
		return c;
	}
	
	public static Color getBackgroundColor() {
		Color c = Settings.launcherStyle.equals("dark") ? DefaultColors.INTERFACE_PRIMARY_DARK : Color.WHITE;
		return c;
	}
	
	public static Color getGreenForegroundColor() {
		Color c = Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_GREEN : DefaultColors.DARK_GREEN;
		return c;
	}
	
	public static Color getRedForegroundColor() {
		Color c = Settings.launcherStyle.equals("dark") ? DefaultColors.BRIGHT_RED : DefaultColors.DARK_RED;
		return c;
	}

}
