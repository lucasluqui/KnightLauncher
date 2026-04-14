package com.lucasluqui.launcher;

import com.google.inject.*;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.launcher.ui.*;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Status;
import com.lucasluqui.launcher.mod.ModManager;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.util.*;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.lucasluqui.launcher.Log.log;

@Singleton
public class LauncherApp
{
  public LauncherApp ()
  {
    this.args = null;
    this.injector = null;
  }

  public LauncherApp (String[] args, Injector injector)
  {
    this.args = args;
    this.injector = injector;
  }

  public static void main (String[] args)
  {
    Injector injector = Guice.createInjector(new LauncherModule());
    LauncherApp app = new LauncherApp(args, injector);
    injector.injectMembers(app);

    app.init();
  }

  @SuppressWarnings("ConstantConditions")
  private void init ()
  {
    setupLogging();
    log.info(BuildConfig.getName() + " started. Running version: " + BuildConfig.getVersion());

    logVMInfo();
    logGameVMInfo();
    logHostInfo();
    setupHTTPSProtocol();
    checkTempDir();
    checkDirectories();

    initManagers();

    Fonts.setup();
    Stylesheet.setup();

    checkStartLocation();
    checkShortcut();

    maybeInitUI();

    if (!this.requiresJVMPatch() && !this.requiresUpdate()) {
      initFinished();
      ThreadingUtil.executeWithDelay(this.getUI(UINames.UI_ID_LAUNCHER)::switchVisibility, 200);
    }
  }

  private void initManagers ()
  {
    _ctx.init(this);
    _settingsManager.init();
    _localeManager.init();
    _modManager.init();
    _flamingoManager.init();
    _moduleManager.init();
    _cacheManager.init();
    _downloadManager.init();
    _keyboardController.init();

    if (SystemUtil.isARM() || SystemUtil.isMac()) {
      _discordPresenceClient.init("0", true);
    } else {
      _discordPresenceClient.init(LauncherGlobals.RPC_CLIENT_ID, false);
    }
  }

  private void maybeInitUI ()
  {
    if (this.requiresJVMPatch()) {
      this.initJVMPatcher();
    } else if (this.requiresUpdate()) {
      this.initUpdater();
    } else {
      this.initUI();
    }
  }

  private void initUI ()
  {
    this.initLauncherUI();
    this.initSettingsUI();
    this.initModListUI();
    this.initEditorsUI();
  }

  private void initFinished ()
  {
    _moduleManager.loadModules();

    checkGetdown();

    if (!FileUtil.fileExists(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip")) {
      _modManager.extractSafeguard();
    }

    _flamingoManager.updateServerList();

    loadOnlineAssets();

    // Only re-check installed mods if we got a different selected server.
    if (!_flamingoManager.getSelectedServer().isOfficial())
      new Thread(_modManager::checkInstalled).start();

    _discordPresenceClient.setDetails(_localeManager.getValue("presence.ready"));
  }

  private void initLauncherUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          LauncherUI launcherUI = injector.getInstance(LauncherUI.class);
          launcherUI.init();
          this.registerUI(UINames.UI_ID_LAUNCHER, launcherUI);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initSettingsUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          SettingsUI settingsUI = injector.getInstance(SettingsUI.class);
          settingsUI.init();
          this.registerUI(UINames.UI_ID_SETTINGS, settingsUI);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initModListUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          ModListUI modListUI = injector.getInstance(ModListUI.class);
          modListUI.init();
          this.registerUI(UINames.UI_ID_MODLIST, modListUI);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initEditorsUI ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          EditorsUI editorsUI = injector.getInstance(EditorsUI.class);
          editorsUI.init();
          this.registerUI(UINames.UI_ID_EDITORS, editorsUI);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initJVMPatcher ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          JVMPatcher jvmPatcher = injector.getInstance(JVMPatcher.class);

          final String path;
          final boolean legacy;

          if (args.length > 1) {
            // If there are more than one argument, it means this is a forced JVM patch, and there's extra info we should parse.
            // Set the path dir to wherever we're being forced to patch to, and set legacy to only allow legacy JVMs.
            // This is primarily used for patching when a third party server was selected.
            path = args[1];
            legacy = Boolean.parseBoolean(args[2]);
          } else {
            // Organic JVM patch.
            path = LauncherGlobals.USER_DIR;

            // If the game's Java VM version is 8 or lower, mark it as legacy so that only
            // legacy Java VMs are offered to patch.
            legacy = JavaUtil.isLegacy();
          }
          jvmPatcher.init(path, legacy);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void initUpdater ()
  {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          Updater updater = injector.getInstance(Updater.class);
          updater.init(this.args[1]);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void checkDirectories ()
  {
    // Stores all mods.
    FileUtil.createDir("mods");

    // Stores clients for third-party servers.
    FileUtil.createDir("thirdparty");

    // Miscellaneous image assets for the launcher to use.
    FileUtil.createDir("KnightLauncher/images/");

    // External modules necessary for extra functionality (e.g., RPC)
    FileUtil.createDir("KnightLauncher/modules/");

    // Check if the deprecated "code-mods" folder exists, in that case, start migrating.
    if (FileUtil.fileExists("code-mods")) migrateLegacyCodeModsFolder();
  }

  private void migrateLegacyCodeModsFolder ()
  {
    File oldCodeModsFolder = new File("code-mods");
    File[] oldCodeModsFolderFiles = oldCodeModsFolder.listFiles();
    if (oldCodeModsFolderFiles == null) {
      oldCodeModsFolder.delete();
    } else {
      for (File file : oldCodeModsFolderFiles) {
        try {
          FileUtils.moveFile(file, new File(LauncherGlobals.USER_DIR + "/mods/" + file.getName()));
        } catch (IOException e) {
          log.error(e);
        }
      }
      oldCodeModsFolder.delete();
    }
  }

  // Checking if we're being run inside the game's directory, "getdown-pro.jar" should always be present if so.
  private void checkStartLocation ()
  {
    if (!FileUtil.fileExists("./getdown-pro.jar")) {
      String pathWarning = _localeManager.getValue("error.start_location");
      if (SystemUtil.isWindows()) {
        String steamGamePath = SteamUtil.getGamePathWindows();
        if (steamGamePath != null)
          pathWarning += _localeManager.getValue("error.start_location_steam_path", steamGamePath);
      }
      log.warning(pathWarning);
      Dialog.push(pathWarning, JOptionPane.WARNING_MESSAGE);
      //if (SystemUtil.isWindows()) DesktopUtil.openDir(SteamUtil.getGamePathWindows());
    }
  }

  // Create a shortcut to the application if there's none.
  private void checkShortcut ()
  {
    if (SystemUtil.isWindows() && FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + BuildConfig.getName())) {
      return;
    }

    if (SystemUtil.isUnix() && FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + BuildConfig.getName() + ".desktop")) {
      return;
    }

    if (SystemUtil.isMac() && FileUtil.fileExists(System.getProperty("user.home") + "/Applications/Knight Launcher.app")) {
      return;
    }

    if (Settings.createShortcut) {
      BufferedImage bimg = ImageUtil.loadImageWithinJar("/rsrc/img/icon-512.png");
      try {
        if (SystemUtil.isWindows()) {
          ICOEncoder.write(bimg, new File(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.ico"));
        } else if (SystemUtil.isUnix()) {
          File outputfile = new File(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.png");
          ImageIO.write(bimg, "png", outputfile);
        } else if (SystemUtil.isMac()) {
          ZipUtil.extractFileWithinJar(
            "/rsrc/img/icon-512.icns",
            LauncherGlobals.USER_DIR + File.separator + "KnightLauncher" + File.separator + "images" + File.separator + "icon-512.icns"
          );
        }
      } catch (IOException e) {
        log.error(e);
      }

      if (SystemUtil.isWindows()) {
        makeShellLink();
      } else if (SystemUtil.isUnix()) {
        makeDesktopFile();
      } else if (SystemUtil.isMac()) {
        makeApplicationFile();
      }
    }
  }

  private void makeShellLink ()
  {
    try {
      DesktopUtil.createShellLink(System.getProperty("java.home") + "\\bin\\javaw.exe",
        "-jar \"" + LauncherGlobals.USER_DIR + "\\KnightLauncher.jar\"",
        LauncherGlobals.USER_DIR,
        LauncherGlobals.USER_DIR + "\\KnightLauncher\\images\\icon-512.ico",
        "Start " + BuildConfig.getName(),
        BuildConfig.getName()
      );
    } catch (Exception e) {
      log.error("Failed to create shell link");
      log.error(e);
    }
  }

  private void makeDesktopFile ()
  {
    File desktopFile = new File(DesktopUtil.getPathToDesktop(), BuildConfig.getName() + ".desktop");
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(desktopFile));
      out.write("[Desktop Entry]\n");
      out.write("Version=1.5\n");
      out.write("Name=" + BuildConfig.getName() + "\n");
      out.write("Comment=Open source game launcher for a certain game\n");
      out.write("Exec=java -jar KnightLauncher.jar\n");
      out.write("Icon=" + LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.png\n");
      out.write("Path=" + LauncherGlobals.USER_DIR + "/\n");
      out.write("Type=Application\n");
      out.write("Categories=Game;\n");
      out.close();
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void makeApplicationFile ()
  {
    String appName = "Knight Launcher";
    String appExec = "launch";
    String bundleId = "com.lucasluqui.knightlauncher";

    Path applicationsDir = Paths.get(
      System.getProperty("user.home"),
      "Applications"
    );

    Path appBundle = applicationsDir.resolve(appName + ".app");
    Path contents = appBundle.resolve("Contents");
    Path macos = contents.resolve("MacOS");
    Path resources = contents.resolve("Resources");

    try {
      Files.createDirectories(macos);
      Files.createDirectories(resources);
    } catch (IOException e) {
      log.error(e);
    }

    Path launcher = macos.resolve(appExec);
    String launcherScript =
      "#!/bin/bash\n" +
      "cd \"" + LauncherGlobals.USER_DIR + "\"\n" +
      "java -jar KnightLauncher.jar\n";

    try {
      Files.write(launcher, launcherScript.getBytes("UTF-8"));
    } catch (IOException e) {
      log.error(e);
    }
    launcher.toFile().setExecutable(true);

    String plist =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\"\n" +
      " \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
      "<plist version=\"1.0\">\n" +
      "<dict>\n" +
      "    <key>CFBundleExecutable</key>\n" +
      "    <string>" + appExec + "</string>\n" +
      "    <key>CFBundleIdentifier</key>\n" +
      "    <string>" + bundleId + "</string>\n" +
      "    <key>CFBundleName</key>\n" +
      "    <string>" + appName + "</string>\n" +
      "    <key>CFBundlePackageType</key>\n" +
      "    <string>APPL</string>\n" +
      "    <key>CFBundleIconFile</key>\n" +
      "    <string>app</string>\n" +
      "    <key>CFBundleVersion</key>\n" +
      "    <string>1.0</string>\n" +
      "</dict>\n" +
      "</plist>\n";

    try {
      Files.write(contents.resolve("Info.plist"), plist.getBytes("UTF-8"));
    } catch (IOException e) {
      log.error(e);
    }

    try {
      Files.copy(
        Paths.get(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.icns"),
        resources.resolve("app.icns"),
        StandardCopyOption.REPLACE_EXISTING
      );
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void setupHTTPSProtocol ()
  {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    System.setProperty("http.agent", "Mozilla/5.0");
    System.setProperty("https.agent", "Mozilla/5.0");
  }

  private void setupLogging ()
  {
    File logFile = new File(LauncherGlobals.USER_DIR + File.separator + "knightlauncher.log");
    File oldLogFile = new File(LauncherGlobals.USER_DIR + File.separator + "old-knightlauncher.log");

    // delete the current old log file if it exists.
    if (FileUtil.fileExists(oldLogFile.getAbsolutePath())) {
      FileUtil.deleteFile(oldLogFile.getAbsolutePath());
    }

    // rename the last log file to old.
    if (FileUtil.fileExists(logFile.getAbsolutePath())) {
      FileUtil.rename(logFile, oldLogFile);
    }

    if (DeployConfig.isDev()) {
      setupDebugLogging();
    }

    try {
      PrintStream printStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(logFile.toPath())), true);
      System.setOut(printStream);
      System.setErr(printStream);
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void setupDebugLogging ()
  {
    Logger rootLogger = LogManager
      .getLogManager()
      .getLogger("");
    rootLogger.setLevel(Level.ALL);
    for (Handler handler : rootLogger.getHandlers()) {
      handler.setLevel(Level.ALL);
    }
    log.debug("Enabled debug logging");
  }

  private void logVMInfo ()
  {
    String systemUsername = System.getProperty("user.name");

    log.info("------------ VM Info ------------");
    log.info("OS Name: " + System.getProperty("os.name"));
    log.info("OS Arch: " + System.getProperty("os.arch"));
    log.info("OS Vers: " + System.getProperty("os.version"));
    log.info("Java Home: " + System.getProperty("java.home"));
    log.info("Java Vers: " + System.getProperty("java.version"));
    log.info("User Name Valid: " + TextUtil.isAlphanumeric(systemUsername));
    log.info("User Home: " + System.getProperty("user.home").replace(systemUsername, "USERNAME"));
    log.info("Current Directory: " + System.getProperty("user.dir").replace(systemUsername, "USERNAME"));
    log.info("---------------------------------");
  }

  private void logGameVMInfo ()
  {
    try {
      String systemUsername = System.getProperty("user.name");

      log.info("--------- Game VM Info ----------");
      log.info("Directory: " + JavaUtil.getGameJVMDirPath().replace(systemUsername, "USERNAME"));
      log.info("Executable: " + JavaUtil.getGameJVMExePath().replace(systemUsername, "USERNAME"));
      log.info("Data: " + JavaUtil.getGameJVMData());
      log.info("Version: " + JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath()));
      log.info("Arch: " + JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()));
      log.info("---------------------------------");
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void logHostInfo ()
  {
    SystemInfo systemInfo = new SystemInfo();
    HardwareAbstractionLayer hardwareAbstractLayer = systemInfo.getHardware();

    log.info("----------- Host Info -----------");

    OperatingSystem os = systemInfo.getOperatingSystem();
    log.info("OS Version: " + os.getVersionInfo());

    CentralProcessor cpu = hardwareAbstractLayer.getProcessor();
    CentralProcessor.ProcessorIdentifier cpuProcId = cpu.getProcessorIdentifier();
    log.info("CPU: " + cpuProcId.getName());

    List<GraphicsCard> gpus = hardwareAbstractLayer.getGraphicsCards();
    for (int i = 0; i < gpus.size(); i++) {
      GraphicsCard gpu = gpus.get(i);
      log.info("GPU " + i + ": " + gpu.getVendor() + ", " + gpu.getName());
      log.info("GPU " + i + " Driver: " + gpu.getVersionInfo());
    }

    GlobalMemory memory = hardwareAbstractLayer.getMemory();
    log.info("Memory: " + FileUtils.byteCountToDisplaySize(memory.getTotal()));

    Baseboard baseboard = hardwareAbstractLayer.getComputerSystem().getBaseboard();
    log.info("Motherboard: " + baseboard.getManufacturer() + ", " + baseboard.getModel());
    log.info("Motherboard Version: " + baseboard.getVersion());

    List<Display> displays = hardwareAbstractLayer.getDisplays();
    for (int i = 0; i < displays.size(); i++) {
      Display display = displays.get(i);
      String displayString = display.toString();

      int maxRefreshRate = 0;
      try {
        String refreshRateString = displayString.split("Field Rate ")[1].split(" Hz")[0];
        if (refreshRateString.contains("--")) {
          maxRefreshRate = Integer.parseInt(
            refreshRateString.split("--")[1]
          );
        } else {
          maxRefreshRate = Integer.parseInt(
            refreshRateString.split("-")[1]
          );
        }
        if (maxRefreshRate > SystemUtil.getRefreshRate()) SystemUtil.setRefreshRate(maxRefreshRate);
      } catch (Exception e) {
        log.error("Failed to get display max refresh rate", e);
      }

      log.info("Display " + i + ": " + display.toString()
        .trim()
        .replace("\n", ", ")
        .replace("\r", "")
        .replaceAll(" +", " ")
      );

      if (maxRefreshRate > 0) {
        log.info("Display " + i + " detected max refresh rate: " + maxRefreshRate);
      }
    }

    log.info("---------------------------------");
  }

  private boolean requiresJVMPatch ()
  {
    // First see if we're being forced to patch.
    if (this.args.length > 0 && this.args[0].equals("forceJVMPatch")) {
      return true;
    }

    // You need a 64-bit system to begin with.
    if (!SystemUtil.is64Bit()) return false;

    // Currently, Java VM patching is only supported on Windows systems and Linux installs through Steam.
    if (!SystemUtil.isWindows() && !(SystemUtil.isUnix() && Settings.gamePlatform.startsWith("Steam"))) return false;

    // Check if there's already a 64-bit Java VM in the game's directory or if it already has been installed by the launcher.
    String javaVMVersion = JavaUtil.getGameJVMData();

    if ((JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 && !javaVMVersion.contains("1.7")) || Settings.jvmPatched) {
      Settings.jvmPatched = true;
      _settingsManager.setValue("launcher.jvm_patched", "true");
      return false;
    }

    return true;
  }

  private boolean requiresUpdate ()
  {
    return this.args.length > 0 && this.args[0].equals("update");
  }

  private void loadOnlineAssets ()
  {
    new Thread(() -> {
      Status flamingoStatus = _flamingoManager.getStatus();
      if (flamingoStatus != null) {
        _flamingoManager.setOnline(true);
        ((SettingsUI) this.getUI(UINames.UI_ID_SETTINGS)).eventHandler.updateAboutTab(flamingoStatus);
      }
      _flamingoManager.updateServerList();
      ((SettingsUI) this.getUI(UINames.UI_ID_SETTINGS)).eventHandler.updateActiveBetaCodes();
    }).start();

    ThreadingUtil.executeWithDelay(this::checkVersion, 3000);
    ThreadingUtil.executeWithDelay(this::checkFlamingoStatus, 10000);
  }

  private void checkFlamingoStatus ()
  {
    if (!_flamingoManager.isOnline()) {
      ((LauncherUI) this.getUI(UINames.UI_ID_LAUNCHER)).showWarning(_localeManager.getValue("error.flamingo_offline"));
    }
  }

  public int getOfficialApproxPlayerCount ()
  {
    int steamPlayers = SteamUtil.getCurrentPlayers("99900");
    if (steamPlayers == 0) {
      return 0;
    } else {
      return Math.round(steamPlayers * 1.4f);
    }
  }

  @SuppressWarnings("all")
  private void checkVersion ()
  {
    String rawResponseReleases = INetUtil.getWebpageContent(
      LauncherGlobals.GITHUB_API
        + "repos/"
        + LauncherGlobals.GITHUB_AUTHOR + "/"
        + LauncherGlobals.GITHUB_REPO + "/"
        + "releases/"
        + "latest"
    );

    LauncherUI launcherUI = (LauncherUI) this.getUI(UINames.UI_ID_LAUNCHER);

    if (rawResponseReleases != null) {
      JSONObject jsonReleases = new JSONObject(rawResponseReleases);
      String latestRelease = jsonReleases.getString("tag_name");
      String latestChangelog = jsonReleases.getString("body");

      launcherUI.eventHandler.latestRelease = latestRelease;
      launcherUI.eventHandler.latestChangelog = latestChangelog;

      String currentVersion = BuildConfig.getVersion();

      if (!latestRelease.equalsIgnoreCase(currentVersion)) {
        if (Settings.autoUpdate && !currentVersion.contains("SNAPSHOT")) {
          // Check if we're coming from a failed update, in that case do not autoupdate even if all other conditions matched.
          if (!(this.args.length > 0 && this.args[0].equals("updateFailed"))) {
            launcherUI.eventHandler.updateLauncher(latestRelease);
          }
        }
        Settings.isOutdated = true;
        launcherUI.updateButton.setVisible(true);
      }
    } else {
      log.error("Received no response from GitHub. Possible downtime?");
    }
  }

  public void checkGetdown ()
  {
    try {
      BufferedReader br = new BufferedReader(new FileReader(LauncherGlobals.USER_DIR + File.separator + "getdown.txt"));
      String firstLine = br.readLine();
      br.close();

      if (firstLine.contains("Customized")) {
        log.info("Detected a legacy modified Getdown file. Resetting");
        resetGetdown();
      }
    } catch (IOException e) {
      log.error(e);
      resetGetdown();
    }
  }

  private void resetGetdown ()
  {
    log.info("Resetting Getdown...");
    URLDownloadQueue downloadQueue = null;
    try {
      downloadQueue = new URLDownloadQueue(
        "Getdown Reset",
        new URL("http://gamemedia2.spiralknights.com/spiral/" + _flamingoManager.getLocalGameVersion() + "/getdown.txt"),
        new File(LauncherGlobals.USER_DIR, "getdown.txt")
      );
    } catch (MalformedURLException e) {
      log.error(e);
    }

    _downloadManager.add(downloadQueue);
    _downloadManager.processQueues();
  }

  /**
   * Sometimes OS usernames that have cyrillic characters in them can make Java have a
   * bad time trying to read them for locating their TEMP path.
   * Let's give our friend a hand and store the temp files ourselves.
   */
  protected void checkTempDir ()
  {
    boolean containsCyrillic = System.getProperty("user.name").codePoints()
      .mapToObj(Character.UnicodeScript::of)
      .anyMatch(Character.UnicodeScript.CYRILLIC::equals);
    if (containsCyrillic) SystemUtil.fixTempDir(LauncherGlobals.USER_DIR + "/KnightLauncher/temp/");
  }

  public void registerUI (String id, BaseUI ui)
  {
    _uiSet.put(id, ui);
    log.info("Registered UI", "id", id);
  }

  public <T extends BaseUI> T getUI (String id)
  {
    return (T) _uiSet.get(id);
  }

  public Map<String, BaseUI> getUISet ()
  {
    return _uiSet;
  }

  public void disposeUI (String id)
  {
    _uiSet.remove(id);
  }

  public void toggleElementsBlock (boolean block)
  {
    for (String id : _uiSet.keySet()) {
      _uiSet.get(id).toggleElementsBlock(block);
    }
  }

  public void selectedServerChanged ()
  {
    for (String id : _uiSet.keySet()) {
      _uiSet.get(id).selectedServerChanged();
    }
  }

  public void showUI (String targetId)
  {
    LauncherUI launcherUI = getUI(UINames.UI_ID_LAUNCHER);
    BaseUI targetUI = null;

    for (String id : getUISet().keySet()) {
      if (id.equals(targetId)) {
        targetUI = getUI(id);
      } else {
        JComponent otherPanel = getUI(id).getPanel();
        otherPanel.setVisible(false);
      }
    }

    if (targetUI == null) {
      log.error("UI is null", "id", targetId);
      return;
    }

    targetUI.getPanel().setBounds(300, 75, 800, 550);
    launcherUI.guiFrame.add(targetUI.getPanel());
    targetUI.getPanel().setVisible(true);
    launcherUI.returnButton.setVisible(true);
  }

  public void returnToHome ()
  {
    for (String id : getUISet().keySet()) {
      BaseUI ui = getUI(id);
      if (id.equalsIgnoreCase(UINames.UI_ID_LAUNCHER)) {
        ui.getPanel().setVisible(true);
        ui.returnButton.setVisible(false);
      } else {
        ui.getPanel().setVisible(false);
        ui.returnButton.setVisible(false);
      }
    }
  }

  public void exit (boolean force)
  {
    _discordPresenceClient.stop();
    if (force || !Settings.keepOpen) {
      try {
        getUI(UINames.UI_ID_LAUNCHER).guiFrame.dispose();
      } catch (NullPointerException e) {
        log.error("Failed to dispose frame on exit");
      }
      System.exit(0);
    }
  }

  public static class LauncherModule extends AbstractModule
  {
    @Override protected void configure ()
    {
      super.configure();
    }
  }

  @Inject protected LauncherContext _ctx;
  @Inject protected SettingsManager _settingsManager;
  @Inject protected LocaleManager _localeManager;
  @Inject protected ModManager _modManager;
  @Inject protected FlamingoManager _flamingoManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;
  @Inject protected ModuleManager _moduleManager;
  @Inject protected CacheManager _cacheManager;
  @Inject protected DownloadManager _downloadManager;
  @Inject protected KeyboardController _keyboardController;

  private final Map<String, BaseUI> _uiSet = new HashMap<>();
  private final List<String> _uiHistory = new ArrayList<>();

  private final String[] args;
  private final Injector injector;
}
