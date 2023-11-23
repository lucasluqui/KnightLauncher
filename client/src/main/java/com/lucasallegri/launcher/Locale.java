package com.lucasallegri.launcher;

import com.lucasallegri.launcher.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import static com.lucasallegri.launcher.Log.log;

public class Locale {

  private static final Properties prop = new Properties();
  private static InputStream propStream = null;

  public static String[] AVAILABLE_LANGUAGES = {
          "English",
          "Arabic",
          "Deutsch",
          "Español",
          "Eesti",
          "Français",
          "Italiano",
          "Japanese",
          "Polski",
          "Português (Brasil)",
          "Русский",
          "Chinese (Simplified)",
          "Chinese (Traditional)",
  };

  public static void setup() {
    propStream = Locale.class.getResourceAsStream("/lang/lang_" + Settings.lang + ".properties");
  }

  public static String getValue(String key) {
    String value = null;
    try {
      prop.load(propStream);
      value = prop.getProperty(key);
    } catch (IOException e) {
      log.error(e);
    }
    if (value != null) return value.substring(1, value.length() - 1);
    return key;
  }

  public static String getValue(String key, String arg) {
    String value = null;
    try {
      prop.load(propStream);
      value = prop.getProperty(key);
      if (value != null) value = MessageFormat.format(prop.getProperty(key), arg);
    } catch (IOException e) {
      log.error(e);
    }
    if (value != null) return value.substring(1, value.length() - 1);
    return key;
  }

  public static String getValue(String key, String[] args) {
    String value = null;
    try {
      prop.load(propStream);
      value = prop.getProperty(key);
      if (value != null) value = MessageFormat.format(prop.getProperty(key), (Object[]) args);
    } catch (IOException e) {
      log.error(e);
    }
    if (value != null) return value.substring(1, value.length() - 1);
    return key;
  }

  public static String getLangName(String code) {
    switch (code) {
      case "en":
        return "English";
      case "es":
        return "Español";
      case "de":
        return "Deutsch";
      case "pt-br":
        return "Português (Brasil)";
      case "fr":
        return "Français";
      case "jp":
        return "Japanese";
      case "et-ee":
        return "Eesti";
      case "ru":
        return "Русский";
      case "it":
        return "Italiano";
      case "pl":
        return "Polski";
      case "ar":
        return "Arabic";
      case "zh-hans":
        return "Chinese (Simplified)";
      case "zh-hant":
        return "Chinese (Traditional)";
    }
    return null;
  }

  public static String getLangCode(String detailed) {
    switch (detailed) {
      case "English":
        return "en";
      case "Español":
        return "es";
      case "Deutsch":
        return "de";
      case "Português (Brasil)":
        return "pt-br";
      case "Français":
        return "fr";
      case "Japanese":
        return "jp";
      case "Eesti":
        return "et-ee";
      case "Русский":
        return "ru";
      case "Italiano":
        return "it";
      case "Polski":
        return "pl";
      case "Arabic":
        return "ar";
      case "Chinese (Simplified)":
        return "zh-hans";
      case "Chinese (Traditional)":
        return "zh-hant";
    }
    return null;
  }

}
