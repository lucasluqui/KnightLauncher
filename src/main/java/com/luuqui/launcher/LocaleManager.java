package com.luuqui.launcher;

import com.google.inject.Singleton;
import com.luuqui.launcher.setting.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import static com.luuqui.launcher.Log.log;

@Singleton
public class LocaleManager
{
  private final Properties prop = new Properties();
  private InputStream propStream = null;

  private final Properties propFallback = new Properties();
  private InputStream propFallbackStream = null;

  public LocaleManager ()
  {
    // empty.
  }

  public void init ()
  {
    this.propStream = LocaleManager.class.getResourceAsStream("/rsrc/lang/lang_" + Settings.lang + ".properties");
    this.propFallbackStream = LocaleManager.class.getResourceAsStream("/rsrc/lang/lang_en.properties");
  }

  public String getValue (String key)
  {
    String value = null;

    // Look for this key's value on the selected language properties file.
    try {
      prop.load(propStream);
      value = prop.getProperty(key);
    } catch (IOException e) {
      log.error(e);
    }

    // The value is still null, we look on the fallback properties file instead (English).
    if (value == null) {
      try {
        propFallback.load(propFallbackStream);
        value = propFallback.getProperty(key);
      } catch (IOException e) {
        log.error(e);
      }
    }

    // Is it still null? We return the key, and that's it, otherwise the value we found.
    return value == null ? key : value.substring(1, value.length() - 1);
  }

  public String getValue (String key, String arg)
  {
    String value = null;

    // Look for this key's value on the selected language properties file.
    try {
      prop.load(propStream);
      value = prop.getProperty(key);

      // Format it.
      if (value != null) value = MessageFormat.format(prop.getProperty(key), arg);
    } catch (IOException e) {
      log.error(e);
    }

    // The value is still null, we look on the fallback properties file instead (English).
    if (value == null) {
      try {
        propFallback.load(propFallbackStream);
        value = propFallback.getProperty(key);
        if (value != null) value = MessageFormat.format(propFallback.getProperty(key), arg);
      } catch (IOException e) {
        log.error(e);
      }
    }

    // Is it still null? We return the key, and that's it, otherwise the value we found.
    return value == null ? key : value.substring(1, value.length() - 1);
  }

  public String getValue (String key, String[] args)
  {
    String value = null;

    // Look for this key's value on the selected language properties file.
    try {
      prop.load(propStream);
      value = prop.getProperty(key);

      // Format it (multiple).
      if (value != null) value = MessageFormat.format(prop.getProperty(key), (Object[]) args);
    } catch (IOException e) {
      log.error(e);
    }

    // The value is still null, we look on the fallback properties file instead (English).
    if (value == null) {
      try {
        propFallback.load(propFallbackStream);
        value = propFallback.getProperty(key);
        if (value != null) value = MessageFormat.format(propFallback.getProperty(key), (Object[]) args);
      } catch (IOException e) {
        log.error(e);
      }
    }

    // Is it still null? We return the key, and that's it, otherwise the value we found.
    return value == null ? key : value.substring(1, value.length() - 1);
  }

  public String getLangName (String code)
  {
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
        return "Russian";
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

  public String getLangCode (String detailed)
  {
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
      case "Russian":
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

  public String[] AVAILABLE_LANGUAGES = {
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
      "Russian",
      "Chinese (Simplified)",
      "Chinese (Traditional)",
  };

}
