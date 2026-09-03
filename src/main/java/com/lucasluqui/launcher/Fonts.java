package com.lucasluqui.launcher;

import com.lucasluqui.launcher.setting.Settings;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.lucasluqui.launcher.Log.log;

public class Fonts
{
  public static void setup ()
  {
    String[] defaultFontPaths = getDefaultFontPaths();

    try (
      InputStream regularFontStream = Fonts.class.getResourceAsStream(defaultFontPaths[0]);
      InputStream mediumFontStream = Fonts.class.getResourceAsStream(defaultFontPaths[1]);
      InputStream codeRegularFontStream = Fonts.class.getResourceAsStream(PATH_CODE_REGULAR)
    ) {
      _fonts.put(getFontName(FONT_FAMILY_DEFAULT, FONT_VARIANT_REGULAR), loadFont(regularFontStream));
      _fonts.put(getFontName(FONT_FAMILY_DEFAULT, FONT_VARIANT_MEDIUM), loadFont(mediumFontStream));
      _fonts.put(getFontName(FONT_FAMILY_CODE, FONT_VARIANT_REGULAR), loadFont(codeRegularFontStream));
    } catch (FontFormatException | IOException e) {
      log.error(e);
    }
  }

  private static String[] getDefaultFontPaths ()
  {
    String language = Settings.lang.toLowerCase(Locale.ROOT);

    if ("jp".equals(language) || "ru".equals(language)) {
      _sizeMultiplier = 1.1f;
      return new String[] { PATH_JP_REGULAR, PATH_JP_MEDIUM };
    }

    if ("ar".equals(language)) {
      return new String[] { PATH_AR_REGULAR, PATH_AR_MEDIUM };
    }

    _sizeMultiplier = 1.2f;
    return new String[] { PATH_REGULAR, PATH_MEDIUM };
  }

  private static Font loadFont (InputStream fontStream)
    throws FontFormatException, IOException
  {
    return Font.createFont(Font.TRUETYPE_FONT, fontStream);
  }

  public static Font getFont (String fontFamily, float size, int style)
  {
    if (Settings.lang.equalsIgnoreCase("zh-hans")
      || Settings.lang.equalsIgnoreCase("zh-hant")) {
      return FONT_NULL;
    }

    String resolvedFontName = resolveFontVariant(fontFamily, style);
    int resolvedStyle = resolveFontStyle(resolvedFontName, style);

    Font baseFont = _fonts.get(resolvedFontName);
    if (baseFont == null) {
      log.warning("Requested unknown font: " + resolvedFontName);
      return FONT_NULL;
    }

    float effectiveSize = resolvedFontName.startsWith(FONT_FAMILY_CODE)
      ? Math.round(size)
      : size * _sizeMultiplier;

    String cacheKey = resolvedFontName + ":" + effectiveSize + ":" + resolvedStyle;
    Font cachedFont = _derivedFonts.get(cacheKey);
    if (cachedFont != null) {
      return cachedFont;
    }

    Font derivedFont = baseFont.deriveFont(resolvedStyle, effectiveSize);
    _derivedFonts.put(cacheKey, derivedFont);
    return derivedFont;
  }

  private static String resolveFontVariant (String fontFamily, int style)
  {
    boolean bold = (style & Font.BOLD) != 0;
    boolean italic = (style & Font.ITALIC) != 0;

    if (bold && italic) {
      String variant = findLoadedFont(getBoldItalicFontCandidates(fontFamily));
      if (variant != null) {
        return variant;
      }
    } else if (bold) {
      String variant = findLoadedFont(getBoldFontCandidates(fontFamily));
      if (variant != null) {
        return variant;
      }
    } else if (italic) {
      String variant = findLoadedFont(new String[] {
        fontFamily + FONT_VARIANT_ITALIC,
        getRegularFontName(fontFamily)
      });

      if (variant != null) {
        return variant;
      }
    }

    return getRegularFontName(fontFamily);
  }

  private static String[] getBoldItalicFontCandidates (String fontFamily)
  {
    return new String[] {
      fontFamily + FONT_VARIANT_BOLD_ITALIC,
      fontFamily + FONT_VARIANT_MEDIUM_ITALIC,
      fontFamily + FONT_VARIANT_SEMI_BOLD_ITALIC,
      fontFamily + FONT_VARIANT_ITALIC,
      fontFamily + FONT_VARIANT_BOLD,
      fontFamily + FONT_VARIANT_MEDIUM,
      fontFamily + FONT_VARIANT_SEMI_BOLD,
      getRegularFontName(fontFamily)
    };
  }

  private static String[] getBoldFontCandidates (String fontFamily)
  {
    return new String[] {
      fontFamily + FONT_VARIANT_BOLD,
      fontFamily + FONT_VARIANT_MEDIUM,
      fontFamily + FONT_VARIANT_SEMI_BOLD,
      getRegularFontName(fontFamily)
    };
  }

  private static String getRegularFontName (String fontFamily)
  {
    return getFontName(fontFamily, FONT_VARIANT_REGULAR);
  }

  private static String getFontName (String fontFamily, String fontVariant)
  {
    return fontFamily + fontVariant;
  }

  private static int resolveFontStyle (String resolvedFontName, int requestedStyle)
  {
    int resolvedStyle = Font.PLAIN;

    boolean requestedBold = (requestedStyle & Font.BOLD) != 0;
    boolean requestedItalic = (requestedStyle & Font.ITALIC) != 0;

    boolean resolvedHasBoldWeight = resolvedFontName.endsWith(FONT_VARIANT_BOLD)
      || resolvedFontName.endsWith(FONT_VARIANT_MEDIUM)
      || resolvedFontName.endsWith(FONT_VARIANT_SEMI_BOLD)
      || resolvedFontName.endsWith(FONT_VARIANT_BOLD_ITALIC)
      || resolvedFontName.endsWith(FONT_VARIANT_MEDIUM_ITALIC)
      || resolvedFontName.endsWith(FONT_VARIANT_SEMI_BOLD_ITALIC);

    boolean resolvedHasItalic = resolvedFontName.endsWith(FONT_VARIANT_ITALIC)
      || resolvedFontName.endsWith(FONT_VARIANT_BOLD_ITALIC)
      || resolvedFontName.endsWith(FONT_VARIANT_MEDIUM_ITALIC)
      || resolvedFontName.endsWith(FONT_VARIANT_SEMI_BOLD_ITALIC);

    if (requestedBold && !resolvedHasBoldWeight) {
      resolvedStyle |= Font.BOLD;
    }

    if (requestedItalic && !resolvedHasItalic) {
      resolvedStyle |= Font.ITALIC;
    }

    return resolvedStyle;
  }

  private static String findLoadedFont (String[] fontNames)
  {
    for (String fontName : fontNames) {
      if (_fonts.containsKey(fontName)) {
        return fontName;
      }
    }

    return null;
  }

  private static float _sizeMultiplier = 1.0f;

  private static final Map<String, Font> _fonts = new HashMap<>();
  private static final Map<String, Font> _derivedFonts = new HashMap<>();

  private static final String FONT_FAMILY_DEFAULT = "default";
  private static final String FONT_FAMILY_CODE = "code";

  private static final String FONT_VARIANT_REGULAR = "Regular";
  private static final String FONT_VARIANT_ITALIC = "Italic";
  private static final String FONT_VARIANT_BOLD = "Bold";
  private static final String FONT_VARIANT_MEDIUM = "Medium";
  private static final String FONT_VARIANT_SEMI_BOLD = "SemiBold";
  private static final String FONT_VARIANT_BOLD_ITALIC = "BoldItalic";
  private static final String FONT_VARIANT_MEDIUM_ITALIC = "MediumItalic";
  private static final String FONT_VARIANT_SEMI_BOLD_ITALIC = "SemiBoldItalic";

  private static final Font FONT_NULL = new Font(null);

  private static final String PATH_REGULAR = "/rsrc/fonts/Figtree-Regular.ttf";
  private static final String PATH_MEDIUM = "/rsrc/fonts/Figtree-SemiBold.ttf";
  private static final String PATH_JP_REGULAR = "/rsrc/fonts/MPLUS1p-Regular.ttf";
  private static final String PATH_JP_MEDIUM = "/rsrc/fonts/MPLUS1p-Medium.ttf";
  private static final String PATH_AR_REGULAR = "/rsrc/fonts/Lemonada-Regular.ttf";
  private static final String PATH_AR_MEDIUM = "/rsrc/fonts/Lemonada-Medium.ttf";
  private static final String PATH_CODE_REGULAR = "/rsrc/fonts/SourceCodePro-Regular.ttf";
}
