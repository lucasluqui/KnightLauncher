package com.luuqui.launcher;

import com.luuqui.launcher.setting.Settings;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static com.luuqui.launcher.Log.log;

public class Fonts
{
  private static final String PATH_REGULAR = "/rsrc/fonts/Figtree-Regular.ttf";
  private static final String PATH_MEDIUM = "/rsrc/fonts/Figtree-SemiBold.ttf";
  private static final String PATH_JP_REGULAR = "/rsrc/fonts/NotoSansJP-Regular.ttf";
  private static final String PATH_JP_MEDIUM = "/rsrc/fonts/NotoSansJP-Medium.ttf";
  private static final String PATH_AR_REGULAR = "/rsrc/fonts/Lemonada-Regular.ttf";
  private static final String PATH_AR_MEDIUM = "/rsrc/fonts/Lemonada-Medium.ttf";
  private static final String PATH_CODE_REGULAR = "/rsrc/fonts/SourceCodePro-Regular.ttf";

  private static final Font FONT_NULL = new Font(null);

  private static final HashMap<String, Font> fonts = new HashMap<>();

  private static float sizeMultiplier = 1.0f;

  public static void setup ()
  {
    InputStream fontRegIs;
    InputStream fontMedIs;
    InputStream fontCodeRegIs;

    if (Settings.lang.equalsIgnoreCase("jp")
        || Settings.lang.equalsIgnoreCase("ru")) {
      fontRegIs = LauncherGUI.class.getResourceAsStream(PATH_JP_REGULAR);
      fontMedIs = LauncherGUI.class.getResourceAsStream(PATH_JP_MEDIUM);
      sizeMultiplier = 1.1f;
    } else if (Settings.lang.equalsIgnoreCase("ar")) {
      fontRegIs = LauncherGUI.class.getResourceAsStream(PATH_AR_REGULAR);
      fontMedIs = LauncherGUI.class.getResourceAsStream(PATH_AR_MEDIUM);
    } else {
      fontRegIs = LauncherGUI.class.getResourceAsStream(PATH_REGULAR);
      fontMedIs = LauncherGUI.class.getResourceAsStream(PATH_MEDIUM);
      sizeMultiplier = 1.2f;
    }

    fontCodeRegIs = LauncherGUI.class.getResourceAsStream(PATH_CODE_REGULAR);

    try {
      fonts.put("defaultRegular", Font.createFont(Font.TRUETYPE_FONT, fontRegIs));
      fonts.put("defaultMedium", Font.createFont(Font.TRUETYPE_FONT, fontMedIs));
      fonts.put("codeRegular", Font.createFont(Font.TRUETYPE_FONT, fontCodeRegIs));

      fontRegIs.close();
      fontMedIs.close();
      fontCodeRegIs.close();
    } catch (FontFormatException | IOException e) {
      log.error(e);
    }
  }

  public static Font getFont (String fontName, float size, int style) {
    if (Settings.lang.equalsIgnoreCase("zh-hans")
        || Settings.lang.equalsIgnoreCase("zh-hant")) {
      return FONT_NULL;
    }

    // TODO: implement font caching.
    return fonts.get(fontName).deriveFont(size * sizeMultiplier).deriveFont(style);
  }
}
