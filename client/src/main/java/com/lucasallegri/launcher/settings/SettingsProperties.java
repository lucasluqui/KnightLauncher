package com.lucasallegri.launcher.settings;

import com.lucasallegri.launcher.LauncherGlobals;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.SteamUtil;
import com.lucasallegri.util.SystemUtil;

import java.io.*;
import java.util.*;

import static com.lucasallegri.launcher.settings.Log.log;

public class SettingsProperties {

  private static final String PROP_VER = "15";

  private static Properties _prop = new Properties();
  private static final String _propPath = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.properties";
  private static HashMap<String, Object> migrationMap = new HashMap<>();
  private static boolean migrationOngoing = false;

  public static void setup() {
    try {
      if (!FileUtil.fileExists(_propPath)) {
        FileUtil.extractFileWithinJar("/config/client-base.properties", _propPath);
      } else if (FileUtil.fileExists(_propPath) && getValue("PROP_VER") != null
              && !getValue("PROP_VER").equals(PROP_VER)) {
        log.warning("Old PROP_VER detected, beginning migration...");
        migrationMap = getAllKeyValues();
        FileUtil.deleteFile(_propPath);
        FileUtil.extractFileWithinJar("/config/client-base.properties", _propPath);
        migrate();
      }
    } catch (IOException e) {
      log.error(e);
    } finally {
      load();
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
    try (InputStream is = new FileInputStream(_propPath)) {
      if(migrationOngoing) _prop.load(is);
      _prop.setProperty(key, value);
      _prop.store(new FileOutputStream(_propPath), null);
      log.info("Setting new key value", "key", key, "value", value);
    } catch (IOException e) {
      log.error(e);
    }
  }

  private static HashMap<String, Object> getAllKeyValues() {
    HashMap<String, Object> keyValues = new HashMap<>();
    try (InputStream is = new FileInputStream(_propPath)) {
      _prop.load(is);
      for(String key : _prop.stringPropertyNames()) {
        keyValues.put(key, getValue(key));
      }
      return keyValues;
    } catch (IOException e) {
      log.error(e);
    }
    return null;
  }

  public static void load() {
    Settings.jvmPatched = Boolean.parseBoolean(getValue("launcher.jvm_patched"));
    Settings.launcherStyle = getValue("launcher.style");
    Settings.lang = getValue("launcher.lang");
    Settings.doRebuilds = Boolean.parseBoolean(getValue("launcher.rebuilds"));
    Settings.keepOpen = Boolean.parseBoolean(getValue("launcher.keepOpen"));
    Settings.createShortcut = Boolean.parseBoolean(getValue("launcher.createShortcut"));
    Settings.ingameRPCSetup = Boolean.parseBoolean(getValue("launcher.ingameRPCSetup"));
    Settings.useIngameRPC = Boolean.parseBoolean(getValue("launcher.useIngameRPC"));
    Settings.compressorUnzipMethod = getValue("compressor.unzipMethod");
    Settings.compressorExtractBuffer = Integer.parseInt(getValue("compressor.extractBuffer"));
    Settings.gamePlatform = getValue("game.platform");
    Settings.gameUseStringDeduplication = Boolean.parseBoolean(getValue("game.useStringDeduplication"));
    Settings.gameDisableExplicitGC = Boolean.parseBoolean(getValue("game.disableExplicitGC"));
    Settings.gameUndecoratedWindow = Boolean.parseBoolean(getValue("game.undecoratedWindow"));
    Settings.gameUseCustomGC = Boolean.parseBoolean(getValue("game.useCustomGC"));
    Settings.gameGarbageCollector = getValue("game.garbageCollector");
    Settings.gameMemory = Integer.parseInt(getValue("game.memory"));
    Settings.gameEndpoint = getValue("game.endpoint");
    Settings.gamePort = Integer.parseInt(getValue("game.port"));
    Settings.gamePublicKey = getValue("game.publicKey");
    Settings.gameGetdownURL = getValue("game.getdownURL");
    Settings.gameGetdownFullURL = getValue("game.getdownFullURL");
    Settings.gameAdditionalArgs = getValue("game.additionalArgs");
    log.info("Successfully loaded all settings from prop file.");
    finishLoading();
  }

  private static void finishLoading() {
    if(SystemUtil.isWindows() && !SteamUtil.isRunningInSteamapps()) {
      setValue("game.platform", "Standalone");
    }
  }

  private static void migrate() {
    migrationOngoing = true;
    for(String key : migrationMap.keySet()) {
      if(key.equals("PROP_VER")) continue;
      setValue(key, (String) migrationMap.get(key));
    }

    // Successfully migrated to newer PROP_VER.
    setValue("PROP_VER", PROP_VER);
    migrationOngoing = false;
  }

}
