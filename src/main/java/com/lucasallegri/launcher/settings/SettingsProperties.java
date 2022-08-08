package com.lucasallegri.launcher.settings;

import com.lucasallegri.launcher.LauncherGlobals;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.SteamUtil;
import com.lucasallegri.util.SystemUtil;

import java.io.*;
import java.util.Properties;

import static com.lucasallegri.launcher.settings.Log.log;

public class SettingsProperties {

  private static final String PROP_VER = "12";

  private static final Properties _prop = new Properties();
  private static final String _propPath = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.properties";

  public static void setup() {
    try {
      if (!FileUtil.fileExists(_propPath)) {
        FileUtil.extractFileWithinJar("/config/base.properties", _propPath);
      } else if (FileUtil.fileExists(_propPath) && getValue("PROP_VER") != null
              && !getValue("PROP_VER").equals(PROP_VER)) {
        log.warning("Old PROP_VER detected, resetting properties file.");
        FileUtil.extractFileWithinJar("/config/base.properties", _propPath);
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static String getValue(String key) {
    String value;
    try (InputStream is = new FileInputStream(_propPath)) {
      _prop.load(is);
      value = _prop.getProperty(key);
      log.info("Request for prop key", "key", key, "value", value);
      return value;
    } catch (IOException e) {
      log.error(e);
    }
    return null;
  }

  public static void setValue(String key, String value) {
    try (OutputStream os = new FileOutputStream(_propPath)) {
      _prop.setProperty(key, value);
      _prop.store(new FileOutputStream(_propPath), null);
      log.info("Setting new key value", "key", key, "value", value);
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static void load() {
    Settings.doRebuilds = Boolean.parseBoolean(getValue("launcher.rebuilds"));
    Settings.keepOpen = Boolean.parseBoolean(getValue("launcher.keepOpen"));
    Settings.createShortcut = Boolean.parseBoolean(getValue("launcher.createShortcut"));
    Settings.lang = getValue("launcher.lang");
    Settings.launcherStyle = getValue("launcher.style");
    Settings.compressorUnzipMethod = getValue("compressor.unzipMethod");
    Settings.compressorExtractBuffer = Integer.parseInt(getValue("compressor.extractBuffer"));
    Settings.gamePlatform = getValue("game.platform");
    Settings.gameUseStringDeduplication = Boolean.parseBoolean(getValue("game.useStringDeduplication"));
    Settings.gameDisableExplicitGC = Boolean.parseBoolean(getValue("game.disableExplicitGC"));
    Settings.gameUndecoratedWindow = Boolean.parseBoolean(getValue("game.undecoratedWindow"));
    Settings.gameUseCustomGC = Boolean.parseBoolean(getValue("game.useCustomGC"));
    Settings.gameGarbageCollector = getValue("game.garbageCollector");
    Settings.gameAdditionalArgs = getValue("game.additionalArgs");
    Settings.gameMemory = Integer.parseInt(getValue("game.memory"));
    Settings.jvmPatched = Boolean.parseBoolean(getValue("launcher.jvm_patched"));
    Settings.ingameRPCSetup = Boolean.parseBoolean(getValue("launcher.ingameRPCSetup"));
    Settings.useIngameRPC = Boolean.parseBoolean(getValue("launcher.useIngameRPC"));
    log.info("Successfully loaded all settings from prop file.");
    finishLoading();
  }

  private static void finishLoading() {
    if(SystemUtil.isWindows() && SteamUtil.getGamePathWindows() == null) {
      setValue("game.platform", "Standalone");
    }
  }

}
