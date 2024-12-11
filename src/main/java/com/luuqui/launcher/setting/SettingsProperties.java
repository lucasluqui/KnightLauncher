package com.luuqui.launcher.setting;

import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.util.FileUtil;
import com.luuqui.util.SteamUtil;
import com.luuqui.util.SystemUtil;

import java.io.*;
import java.util.*;

import static com.luuqui.launcher.setting.Log.log;

public class SettingsProperties {

  private static final String PROP_VER = "21";

  private static Properties _prop = new Properties();
  private static final String _propPath = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.properties";
  private static HashMap<String, Object> migrationMap = new HashMap<>();
  private static boolean migrating = false;

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
      log.info("Request for key", "key", key, "value", value);
      return value;
    } catch (IOException e) {
      log.error(e);
    }
    return null;
  }

  public static void setValue(String key, String value) {
    try (InputStream is = new FileInputStream(_propPath)) {
      if(migrating) _prop.load(is);
      _prop.setProperty(key, value);
      _prop.store(new FileOutputStream(_propPath), null);
      log.info("Setting new key", "key", key, "value", value);
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

  public static void createKeyIfNotExists(String key, String value) {
    try (InputStream is = new FileInputStream(_propPath)) {
      if(_prop.getProperty(key) == null) {
        _prop.setProperty(key, value);
        _prop.store(new FileOutputStream(_propPath), null);
        log.info("Setting new key", "key", key, "value", value);
      } else {
        log.info("Key already exists", "key", key, "value", value);
      }
    } catch (IOException e) {
      log.error(e);
    }
  }

  public static void load() {
    // Launcher settings
    Settings.jvmPatched = Boolean.parseBoolean(getValue("launcher.jvm_patched"));
    Settings.lang = getValue("launcher.lang");
    Settings.doRebuilds = Boolean.parseBoolean(getValue("launcher.rebuilds"));
    Settings.keepOpen = Boolean.parseBoolean(getValue("launcher.keepOpen"));
    Settings.createShortcut = Boolean.parseBoolean(getValue("launcher.createShortcut"));
    Settings.ingameRPCSetup = Boolean.parseBoolean(getValue("launcher.ingameRPCSetup"));
    Settings.useIngameRPC = Boolean.parseBoolean(getValue("launcher.useIngameRPC"));
    Settings.selectedServerName = getValue("launcher.selectedServerName");
    Settings.autoUpdate = Boolean.parseBoolean(getValue("launcher.autoUpdate"));
    Settings.playAnimatedBanners = Boolean.parseBoolean(getValue("launcher.playAnimatedBanners"));

    // Compressor settings
    Settings.compressorUnzipMethod = getValue("compressor.unzipMethod");
    Settings.compressorExtractBuffer = Integer.parseInt(getValue("compressor.extractBuffer"));

    // Game settings
    Settings.gamePlatform = getValue("game.platform");
    Settings.gameDisableExplicitGC = Boolean.parseBoolean(getValue("game.disableExplicitGC"));
    Settings.gameUseCustomGC = Boolean.parseBoolean(getValue("game.useCustomGC"));
    Settings.gameGarbageCollector = getValue("game.garbageCollector");
    Settings.gameMemory = Integer.parseInt(getValue("game.memory"));
    Settings.gameEndpoint = getValue("game.endpoint");
    Settings.gamePort = Integer.parseInt(getValue("game.port"));
    Settings.gamePublicKey = getValue("game.publicKey");
    Settings.gameGetdownURL = getValue("game.getdownURL");
    Settings.gameGetdownFullURL = getValue("game.getdownFullURL");
    Settings.gameAdditionalArgs = getValue("game.additionalArgs");

    log.info("Successfully loaded required settings from prop file.");
    finishLoading();
  }

  private static void finishLoading() {
    if(SystemUtil.isWindows() && !SteamUtil.isRunningInSteamapps()) {
      setValue("game.platform", "Standalone");
    }
  }

  private static void migrate() {
    migrating = true;
    for(String key : migrationMap.keySet()) {
      if(key.equals("PROP_VER")) continue;
      setValue(key, (String) migrationMap.get(key));
    }

    // Successfully migrated to newer PROP_VER.
    setValue("PROP_VER", PROP_VER);
    migrating = false;
  }

}
