package com.lucasluqui.launcher.setting;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.launcher.LauncherContext;
import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.setting.ui.SettingsUI;
import com.lucasluqui.util.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.lucasluqui.launcher.setting.Log.log;

@Singleton
public class SettingsManager
{
  public SettingsManager ()
  {}

  public void init ()
  {
    try {
      if (!FileUtil.fileExists(PROP_PATH)) {
        ZipUtil.extractFileWithinJar("/rsrc/config/launcher.properties", PROP_PATH);
      }
      loadProp();

      String currentPropVer = getValue("PROP_VER");
      if (!currentPropVer.equals(PROP_VER)) {
        log.warning("PROP_VER mismatch", "expected", PROP_VER, "found", currentPropVer);
        _migrationMap = getAllKeyValues();
        _prop.clear();
        FileUtil.deleteFile(PROP_PATH);
        ZipUtil.extractFileWithinJar("/rsrc/config/launcher.properties", PROP_PATH);
        log.info("Extracting latest properties file...");
        loadProp();
        migrateSettings();
        _prop.clear();
        loadProp();
      }
    } catch (IOException e) {
      log.error(e);
    } finally {
      loadSettings();
      initFinished();
    }
  }

  private void loadProp ()
  {
    log.info("Loading properties...");
    try (InputStream is = Files.newInputStream(Paths.get(PROP_PATH))) {
      _prop.load(is);
      log.info("Loaded properties");
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void loadSettings ()
  {
    // Launcher settings
    Settings.jvmPatched = Boolean.parseBoolean(getValue("launcher.jvm_patched"));
    Settings.lang = getValue("launcher.lang");
    Settings.doRebuilds = Boolean.parseBoolean(getValue("launcher.rebuilds"));
    Settings.filePurging = Boolean.parseBoolean(getValue("launcher.filePurging"));
    Settings.hideWhileGameRunning = Boolean.parseBoolean(getValue("launcher.hideWhileGameRunning"));
    Settings.hideWhileEditorRunning = Boolean.parseBoolean(getValue("launcher.hideWhileEditorRunning", "true"));
    Settings.createShortcut = Boolean.parseBoolean(getValue("launcher.createShortcut"));
    Settings.ingameRPCSetup = Boolean.parseBoolean(getValue("launcher.ingameRPCSetup"));
    Settings.useIngameRPC = Boolean.parseBoolean(getValue("launcher.useIngameRPC"));
    Settings.selectedServerName = getValue("launcher.selectedServerName");
    Settings.autoUpdate = Boolean.parseBoolean(getValue("launcher.autoUpdate"));
    Settings.playAnimatedBanners = Boolean.parseBoolean(getValue("launcher.playAnimatedBanners"));
    Settings.loadCodeMods = Boolean.parseBoolean(getValue("launcher.loadCodeMods.v2"));
    Settings.fileProtection = Boolean.parseBoolean(getValue("launcher.fileProtection"));
    Settings.betasEnabled = Boolean.parseBoolean(getValue("launcher.betasEnabled"));
    Settings.showLegacySceneEditor = Boolean.parseBoolean(getValue("launcher.showLegacySceneEditor"));
    try {
      Settings.uiScale = Integer.parseInt(getValue("launcher.uiScale", "100"));
    } catch (NumberFormatException e) {
      Settings.uiScale = 100;
    }

    // Compressor settings
    Settings.compressorUnzipMethod = getValue("compressor.unzipMethod");
    Settings.compressorExtractBuffer = Integer.parseInt(getValue("compressor.extractBuffer"));

    // Game settings
    // Older versions stored the platform's translated name, which is "Steam" in every language.
    // Anything else was a translation of "Standalone", so turn it into the language-independent value.
    String platform = getValue("game.platform");
    Settings.gamePlatform = platform.startsWith(Settings.PLATFORM_STEAM) ? Settings.PLATFORM_STEAM : Settings.PLATFORM_STANDALONE;
    if (!Settings.gamePlatform.equals(platform)) {
      log.info("Migrating stored game platform", "from", platform, "to", Settings.gamePlatform);
      setValue("game.platform", Settings.gamePlatform);
    }
    Settings.gameMemory = Integer.parseInt(getValue("game.memory"));
    Settings.gameDisableExplicitGC = Boolean.parseBoolean(getValue("game.disableExplicitGC"));
    Settings.gameUseCustomGC = Boolean.parseBoolean(getValue("game.useCustomGC"));
    Settings.gameGarbageCollector = getValue("game.garbageCollector.v2");
    Settings.gameExtraPerfMode = Boolean.parseBoolean(getValue("game.extraPerfMode"));
    Settings.gameEndpoint = getValue("game.endpoint");
    Settings.gamePort = Integer.parseInt(getValue("game.port"));
    Settings.gamePublicKey = getValue("game.publicKey");
    Settings.gameGetdownURL = getValue("game.getdownURL");
    Settings.gameGetdownFullURL = getValue("game.getdownFullURL");
    Settings.gameAdditionalArgs = getValue("game.additionalArgs.v2", "");

    log.info("Loaded required settings from properties file");
  }

  private void initFinished ()
  {
    if (SystemUtil.isWindows() && !SteamUtil.isRunningInSteamapps()) {
      Settings.gamePlatform = Settings.PLATFORM_STANDALONE;
      setValue("game.platform", Settings.PLATFORM_STANDALONE);
    }
  }

  public String getValue (String key)
  {
    return getValue(key, "");
  }

  public String getValue (String key, String defVal)
  {
    String value;
    value = _prop.getProperty(key);
    log.info("Request for key", "key", key, "value", value);
    return value != null ? value : defVal;
  }

  public String getValue (String key, Server server)
  {
    return getValue(key, "", server);
  }

  public String getValue (String key, String defVal, Server server)
  {
    String value;
    if (server != null) {
      value = _prop.getProperty(server.isOfficial() ? key : key + "_" + server.getSanitizedName());
    } else {
      value = _prop.getProperty(key);
    }
    log.info("Request for key", "key", key, "value", value);
    return value != null ? value : defVal;
  }

  public void setValue (String key, String value)
  {
    _prop.setProperty(key, value);
    try {
      _prop.store(Files.newOutputStream(Paths.get(PROP_PATH)), null);
    } catch (IOException e) {
      log.error(e);
    }
    log.info("Setting new key", "key", key, "value", value);
  }

  public void setValue (String key, String value, Server server)
  {
    if (server != null) {
      _prop.setProperty(server.isOfficial() ? key : key + "_" + server.getSanitizedName(), value);
    } else {
      _prop.setProperty(key, value);
    }
    try {
      _prop.store(Files.newOutputStream(Paths.get(PROP_PATH)), null);
    } catch (IOException e) {
      log.error(e);
    }
    log.info("Setting new server-specific key", "key", key, "value", value, "server", server == null ? "null" : server.name);
  }

  private HashMap<String, Object> getAllKeyValues ()
  {
    HashMap<String, Object> keyValues = new HashMap<>();
    for (String key : _prop.stringPropertyNames()) {
      keyValues.put(key, getValue(key));
    }
    return keyValues;
  }

  public void createKeyIfNotExists (String key, String value)
  {
    if (_prop.getProperty(key) == null) {
      _prop.setProperty(key, value);
      try {
        _prop.store(Files.newOutputStream(Paths.get(PROP_PATH)), null);
      } catch (IOException e) {
        log.error(e);
      }
      log.info("Setting new key", "key", key, "value", value);
    } else {
      log.info("Key already exists", "key", key, "value", value);
    }
  }

  private void migrateSettings ()
  {
    _migrating = true;
    log.info("Migrating settings to new properties file...");
    for (String key : _migrationMap.keySet()) {
      if (key.equals("PROP_VER")) continue;
      setValue(key, (String) _migrationMap.get(key));
    }

    // Successfully migrated to newer PROP_VER.
    setValue("PROP_VER", PROP_VER);
    log.info("Successfully migrated properties file");
    _migrating = false;
  }

  public void applyGameSettings ()
  {
    try {
      _ctx._progressBar.startTask();
      _ctx._progressBar.setBarMax(1);
      _ctx._progressBar.setBarValue(0);
      _ctx._progressBar.setState(_localeManager.getValue("m.apply"));

      // Run a platform check by triggering a change event just in case the value stored is incorrect.
      _ctx.getApp().getUI(SettingsUI.class).eventHandler.platformChangeEvent(null);

      // Back up the current extra.txt if there's no back up already.
      // This is useful if a user installs the launcher and had already
      // made its own extra.txt, this way it won't get deleted forever, just renamed.
      if (!FileUtil.fileExists("old-extra.txt")) {
        FileUtil.rename(new File("extra.txt"), new File("old-extra.txt"));
      }

      PrintWriter writer = new PrintWriter("extra.txt", "UTF-8");

      if (Settings.gameExtraPerfMode) {
        String extraPerfArgs = null;
        try {
          extraPerfArgs = com.google.common.io.Files.toString(
            new File(LauncherGlobals.USER_DIR + File.separator + "KnightLauncher" + File.separator + "modules" + File.separator + "extra-perf" + File.separator + "extra-perf"),
            Charsets.UTF_8
          );
        } catch (Exception e) {
          log.error(e);
        }

        if (extraPerfArgs != null) {
          writer.println(extraPerfArgs);
        }
      } else {
        if (Settings.gameUseStringDeduplication) writer.println("-XX:+UseStringDeduplication");
        if (Settings.gameDisableExplicitGC) writer.println("-XX:+DisableExplicitGC");

        if (Settings.gameUseCustomGC) {
          if (Settings.gameGarbageCollector.equals("Parallel")) {
            writer.println("-XX:+UseParallelGC");
          } else if (Settings.gameGarbageCollector.equals("ZGC")) {
            writer.println("-XX:+Use" + Settings.gameGarbageCollector);
            // TODO: Maybe add some extra settings for ZGC to use?
          } else {
            writer.println("-XX:+Use" + Settings.gameGarbageCollector + "GC");
          }
        }

        if (Settings.gameUndecoratedWindow) writer.println("-Dorg.lwjgl.opengl.Window.undecorated=true");

        if (Settings.gameGarbageCollector.equals("G1")) {
          writer.println("-Xms" + Settings.gameMemory / 2 + "M");
          writer.println("-Xmx" + Settings.gameMemory + "M");
        } else {
          writer.println("-Xms" + Settings.gameMemory + "M");
          writer.println("-Xmx" + Settings.gameMemory + "M");
        }
      }

      // Get rid of this "0" bug. Maybe delete this one day
      if (Settings.gameAdditionalArgs.equalsIgnoreCase("0")) {
        Settings.gameAdditionalArgs = "";
        _ctx.getApp().getUI(SettingsUI.class).eventHandler.saveAdditionalArgs(true);
      }

      // And now we validate all (possibly) REAL args.
      if (validAdditionalArgs(Settings.gameAdditionalArgs)) {
        writer.println(filterAdditionalArgs(Settings.gameAdditionalArgs));
      } else {
        Dialog.push(
          _localeManager.getValue("m.invalid_additional_args_warning"),
          _localeManager.getValue("t.invalid_additional_args"),
          JOptionPane.WARNING_MESSAGE
        );
      }
      writer.close();

      if (_flamingoManager.getSelectedServer().isOfficial()) applyConnectionSettings();

      _ctx._progressBar.setBarValue(1);
      _ctx._progressBar.finishTask();
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      log.error(e);
    }
  }

  public boolean validAdditionalArgs (String argString)
  {
    // Is it empty? goddamn that's the finest it can be.
    if (argString.isEmpty()) {
      return true;
    }

    // ParallelOld isn't compatible with the game's JVM anymore.
    if (argString.contains("ParallelOld")) {
      return false;
    }

    // iterate through all args.
    List<String> args = Arrays.asList(argString.split("\\r?\\n"));
    for (String arg : args) {
      // invalid arg? mark the whole thing as invalid.
      if (!JavaUtil.validJVMArg(arg)) {
        log.info("Ignoring all additional args due to invalid JVM arg", "arg", arg);
        return false;
      }
    }

    // there doesn't seem to be any unsafe args... gets a green light.
    return true;
  }

  public String filterAdditionalArgs (String argString)
  {
    if (argString.isEmpty()) return argString;

    // iterate through all args.
    List<String> args = Arrays.asList(argString.split("\\r?\\n"));
    for (String arg : args) {
      // filter out stuff we don't want while in extra perf mode.
      if (Settings.gameExtraPerfMode) {
        if (arg.startsWith("-Xms") || arg.startsWith("-Xmx")) {
          args.remove(arg);
        }
      }

      // any other filters, if any, should go here below.
    }

    // return the filtered args as a single string.
    return String.join(System.lineSeparator(), args);
  }

  public void applyConnectionSettings ()
  {
    Server server = _flamingoManager.getSelectedServer();

    // Every server shares the stock client, so an empty configuration must never be
    // written over it: that would wipe the connection settings of the other servers.
    if (isBlank(Settings.gameEndpoint) && isBlank(Settings.gamePublicKey) && isBlank(Settings.gameGetdownURL)) {
      log.info("No connection settings to apply", "server", server == null ? "null" : server.name);
      return;
    }

    Properties properties = new Properties();
    if (!isBlank(Settings.gameEndpoint)) {
      properties.setProperty("server_host", Settings.gameEndpoint);
    }
    if (Settings.gamePort > 0) {
      properties.setProperty("server_ports", String.valueOf(Settings.gamePort));
      properties.setProperty("datagram_ports", String.valueOf(Settings.gamePort));
    }
    if (!isBlank(Settings.gamePublicKey)) {
      properties.setProperty("key.public", Settings.gamePublicKey);
    }
    if (!isBlank(Settings.gameGetdownURL)) {
      properties.setProperty("client_root_url", Settings.gameGetdownURL);
    }

    try {
      ZipUtil.updatePropertiesInsideZip(
        Paths.get(LauncherGlobals.USER_DIR, "code", "config.jar"),
        "deployment.properties", properties);
    } catch (IOException | IllegalArgumentException e) {
      log.error("Failed to apply connection settings to code/config.jar",
        "server", server == null ? "null" : server.name);
      log.error(e);
    }
  }

  private static boolean isBlank (String value)
  {
    return value == null || value.trim().isEmpty();
  }

  /**
   * Whether the given server has connection settings worth applying. An empty endpoint
   * means the server was left on "do not touch the client", which is the default for
   * custom servers.
   */
  public boolean hasConnectionSettings (Server server)
  {
    return !isBlank(getValue("game.endpoint", "", server));
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

  @Inject protected LauncherContext _ctx;
  @Inject protected LocaleManager _localeManager;
  @Inject protected FlamingoManager _flamingoManager;

  private final Properties _prop = new Properties();
  private HashMap<String, Object> _migrationMap = new HashMap<>();
  private boolean _migrating = false;

  private final String PROP_VER = "32";
  private final String PROP_PATH = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.properties";
}
