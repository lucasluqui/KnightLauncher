package com.luuqui.launcher.setting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherContext;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.LocaleManager;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.util.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static com.luuqui.launcher.setting.Log.log;

@Singleton
public class SettingsManager
{
  @Inject protected LauncherContext _launcherCtx;
  @Inject protected LocaleManager _localeManager;
  @Inject protected FlamingoManager _flamingoManager;

  private final String PROP_VER = "24";

  private Properties _prop = new Properties();
  private final String _propPath = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.properties";

  private HashMap<String, Object> migrationMap = new HashMap<>();
  private boolean migrating = false;

  public SettingsManager ()
  {

  }

  public void init ()
  {
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

  public String getValue (String key)
  {
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

  public void setValue (String key, String value)
  {
    try (InputStream is = new FileInputStream(_propPath)) {
      if(migrating) _prop.load(is);
      _prop.setProperty(key, value);
      _prop.store(new FileOutputStream(_propPath), null);
      log.info("Setting new key", "key", key, "value", value);
    } catch (IOException e) {
      log.error(e);
    }
  }

  private HashMap<String, Object> getAllKeyValues ()
  {
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

  public void createKeyIfNotExists (String key, String value)
  {
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

  public void load ()
  {
    // Launcher settings
    Settings.jvmPatched = Boolean.parseBoolean(getValue("launcher.jvm_patched")); // Delete this when the dreaded day comes.
    Settings.lang = getValue("launcher.lang");
    Settings.doRebuilds = Boolean.parseBoolean(getValue("launcher.rebuilds"));
    Settings.keepOpen = Boolean.parseBoolean(getValue("launcher.keepOpen"));
    Settings.createShortcut = Boolean.parseBoolean(getValue("launcher.createShortcut"));
    Settings.ingameRPCSetup = Boolean.parseBoolean(getValue("launcher.ingameRPCSetup"));
    Settings.useIngameRPC = Boolean.parseBoolean(getValue("launcher.useIngameRPC"));
    Settings.selectedServerName = getValue("launcher.selectedServerName");
    Settings.autoUpdate = Boolean.parseBoolean(getValue("launcher.autoUpdate"));
    Settings.playAnimatedBanners = Boolean.parseBoolean(getValue("launcher.playAnimatedBanners"));
    Settings.loadCodeMods = Boolean.parseBoolean(getValue("launcher.loadCodeMods"));
    Settings.fileProtection = Boolean.parseBoolean(getValue("launcher.fileProtection"));

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

  private void finishLoading ()
  {
    if(SystemUtil.isWindows() && !SteamUtil.isRunningInSteamapps()) {
      setValue("game.platform", "Standalone");
    }
  }

  private void migrate ()
  {
    migrating = true;
    for(String key : migrationMap.keySet()) {
      if(key.equals("PROP_VER")) continue;
      setValue(key, (String) migrationMap.get(key));
    }

    // Successfully migrated to newer PROP_VER.
    setValue("PROP_VER", PROP_VER);
    migrating = false;
  }

  public void loadGameSettings ()
  {
    try {
      _launcherCtx._progressBar.startTask();
      _launcherCtx._progressBar.setBarMax(1);
      _launcherCtx._progressBar.setBarValue(0);
      _launcherCtx._progressBar.setState(_localeManager.getValue("m.apply"));

      /**
       * Back up the current extra.txt if there's no back up already.
       * This is useful if a user installs Knight Launcher and had already
       * made its own extra.txt, this way it won't get deleted forever, just renamed.
       */
      if(!FileUtil.fileExists("old-extra.txt")) {
        FileUtil.rename(new File("extra.txt"), new File("old-extra.txt"));
      }

      PrintWriter writer = new PrintWriter("extra.txt", "UTF-8");

      if (Settings.gameUseStringDeduplication) writer.println("-XX:+UseStringDeduplication");
      if (Settings.gameDisableExplicitGC) writer.println("-XX:+DisableExplicitGC");

      if (Settings.gameUseCustomGC) {
        if (Settings.gameGarbageCollector.equals("ParallelOld")) {
          writer.println("-XX:+UseParallelGC");
          writer.println("-XX:+Use" + Settings.gameGarbageCollector + "GC");
        } else {
          writer.println("-XX:+Use" + Settings.gameGarbageCollector + "GC");
        }
      }

      if (Settings.gameUndecoratedWindow) writer.println("-Dorg.lwjgl.opengl.Window.undecorated=true");

      if (Settings.gameGarbageCollector.equals("G1")) {
        writer.println("-Xms" + Settings.gameMemory + "M");
        writer.println("-Xmx" + Settings.gameMemory + "M");
      } else {
        writer.println("-Xms512M");
        writer.println("-Xmx" + Settings.gameMemory + "M");
      }

      writer.println(Settings.gameAdditionalArgs);
      writer.close();

      if(_flamingoManager.getSelectedServer().isOfficial()) loadConnectionSettings();

      _launcherCtx._progressBar.setBarValue(1);
      _launcherCtx._progressBar.finishTask();
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      log.error(e);
    }
  }

  private void loadConnectionSettings ()
  {
    try {
      FileUtil.extractFileWithinJar("/config/deployment.properties", LauncherGlobals.USER_DIR + "/deployment.properties");
    } catch (IOException e) {
      log.error(e);
    }
    Properties properties = new Properties();
    try {
      properties.load(Files.newInputStream(new File(LauncherGlobals.USER_DIR + "/deployment.properties").toPath()));
    } catch (IOException e) {
      log.error(e);
    }

    properties.setProperty("server_host", Settings.gameEndpoint);
    properties.setProperty("server_ports", String.valueOf(Settings.gamePort));
    properties.setProperty("datagram_ports", String.valueOf(Settings.gamePort));
    properties.setProperty("key.public", Settings.gamePublicKey);
    properties.setProperty("client_root_url", Settings.gameGetdownURL);

    try {
      properties.store(Files.newOutputStream(new File(LauncherGlobals.USER_DIR + "/deployment.properties").toPath()), null);
    } catch (IOException e) {
      log.error(e);
    }

    String[] outputCapture = null;
    if(SystemUtil.isWindows()) {
      outputCapture = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", JavaUtil.getGameJVMDirPath() + "/bin/jar.exe", "uf", "code/config.jar", "deployment.properties" });
    } else {
      outputCapture = ProcessUtil.runAndCapture(new String[]{ "/bin/bash", "-c", JavaUtil.getGameJVMDirPath() + "/bin/jar", "uf", "code/config.jar", "deployment.properties" });
    }
    log.debug("Connection settings capture, stdout=", outputCapture[0], "stderr=", outputCapture[1]);
    FileUtil.deleteFile(LauncherGlobals.USER_DIR + "/deployment.properties");
  }

  public int getMaxAllowedMemoryAlloc ()
  {
    int MAX_ALLOWED_MEMORY_64_BIT = 4096;
    int MAX_ALLOWED_MEMORY_32_BIT = 1024;

    if (JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64) {
      return MAX_ALLOWED_MEMORY_64_BIT;
    } else {
      return MAX_ALLOWED_MEMORY_32_BIT;
    }
  }

}
