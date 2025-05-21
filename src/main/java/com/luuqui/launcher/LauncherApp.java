package com.luuqui.launcher;

import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.launcher.editor.EditorsGUI;
import com.luuqui.launcher.flamingo.Flamingo;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.flamingo.data.Status;
import com.luuqui.launcher.mod.ModListGUI;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsEventHandler;
import com.luuqui.launcher.setting.SettingsGUI;
import com.luuqui.launcher.setting.SettingsProperties;
import com.luuqui.util.*;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.luuqui.launcher.Log.log;

public class LauncherApp {

  private String[] _args = null;

  protected static LauncherGUI lgui;
  protected static SettingsGUI sgui;
  protected static ModListGUI mgui;
  protected static EditorsGUI egui;
  protected static JVMPatcher jvmPatcher;
  protected static Updater updater;

  public static boolean flamingoOnline = false;

  public static java.util.List<Server> serverList = new ArrayList<>();
  public static Server selectedServer = null;

  public static void main(String[] args) {

    LauncherApp app = new LauncherApp();
    app._args = args;

    if (app.requiresJVMPatch()) {
      jvmPatcher = app.composeJVMPatcher(app);
    } else if (app.requiresUpdate()) {
      updater = app.composeUpdater(app);
    } else {
      lgui = app.composeLauncherGUI(app);
      sgui = app.composeSettingsGUI(app);
      mgui = app.composeModListGUI(app);
      egui = app.composeEditorsGUI(app);

      ThreadingUtil.executeWithDelay(lgui::switchVisibility, 200);

      app.postInitialization();
    }
  }

  public LauncherApp () {
    setupFileLogging();
    logVMInfo();
    logGameVMInfo();
    checkTempDir();
    SettingsProperties.setup();
    Stylesheet.setup();
    Fonts.setup();
    Locale.setup();
    checkStartLocation();
    setupHTTPSProtocol();
    DiscordPresenceClient.getInstance().start();
    KeyboardController.start();
    checkDirectories();
    Cache.setup();
    if (SystemUtil.isWindows() || SystemUtil.isUnix()) checkShortcut();
  }

  private LauncherGUI composeLauncherGUI(LauncherApp app) {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          lgui = new LauncherGUI(app);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
    return lgui;
  }

  private SettingsGUI composeSettingsGUI(LauncherApp app) {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          sgui = new SettingsGUI(app);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
    return sgui;
  }

  private ModListGUI composeModListGUI(LauncherApp app) {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          mgui = new ModListGUI(app);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
    return mgui;
  }

  private EditorsGUI composeEditorsGUI(LauncherApp app) {
    try {
      EventQueue.invokeLater(() -> {
        try {
          egui = new EditorsGUI(app);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
    return egui;
  }

  private JVMPatcher composeJVMPatcher(LauncherApp app) {
    String path;
    boolean legacy;

    if(_args.length > 0) {
      // If there's any arguments it means this is a forced JVM patch and there's extra info we should parse.
      // Set the path dir to wherever we're being forced to patch to, and set legacy to only allow legacy JVMs.
      // This is primarily used for patching when a third party server was selected.
      path = _args[1];
      legacy = Boolean.parseBoolean(_args[2]);
    } else {
      // Organic JVM patch, set default values.
      path = LauncherGlobals.USER_DIR;
      //legacy = false;
      legacy = true; // Temporarily set Official to use legacy JVMs too
    }

      try {
      EventQueue.invokeAndWait(() -> {
        try {
          jvmPatcher = new JVMPatcher(app, path, legacy);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
    return jvmPatcher;
  }

  private Updater composeUpdater(LauncherApp app) {
    try {
      EventQueue.invokeAndWait(() -> {
        try {
          updater = new Updater(app);
        } catch (Exception e) {
          log.error(e);
        }
      });
    } catch (Exception e) {
      log.error(e);
    }
    return updater;
  }

  private void checkDirectories() {
    // Stores /rsrc (.zip) mods.
    FileUtil.createDir("mods");

    // Stores clients for third-party servers.
    FileUtil.createDir("thirdparty");

    // Miscellaneous image assets for the launcher to use.
    FileUtil.createDir("KnightLauncher/images/");

    // External modules necessary for extra functionality (eg. RPC)
    FileUtil.createDir("KnightLauncher/modules/");

    // Check if the deprecated "code-mods" folder exists, in that case start migrating.
    if(FileUtil.fileExists("code-mods")) migrateLegacyCodeModsFolder();

  }

  private void migrateLegacyCodeModsFolder() {
    File oldCodeModsFolder = new File("code-mods");
    File[] oldCodeModsFolderFiles = oldCodeModsFolder.listFiles();
    if(oldCodeModsFolderFiles == null) {
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
  private void checkStartLocation() {
    if (!FileUtil.fileExists("./getdown-pro.jar")) {
      String pathWarning = Locale.getValue("error.start_location");
      if (SystemUtil.isWindows()) {
        pathWarning += Locale.getValue("error.start_location_steam_path", SteamUtil.getGamePathWindows());
      }
      log.warning(pathWarning);
      Dialog.push(pathWarning, JOptionPane.WARNING_MESSAGE);
      //if (SystemUtil.isWindows()) DesktopUtil.openDir(SteamUtil.getGamePathWindows());
    }
  }

  // Create a shortcut to the application if there's none.
  private void checkShortcut() {
    if (Settings.createShortcut
            && !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + LauncherGlobals.LAUNCHER_NAME)
            && !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + LauncherGlobals.LAUNCHER_NAME + ".desktop")) {
      BufferedImage bimg = ImageUtil.loadImageWithinJar("/img/icon-512.png");
      try {
        if (SystemUtil.isWindows()) {
          ICOEncoder.write(bimg, new File(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.ico"));
        } else {
          File outputfile = new File(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.png");
          ImageIO.write(bimg, "png", outputfile);
        }
      } catch (IOException e) {
        log.error(e);
      }

      if (SystemUtil.isWindows()) {
        makeShellLink();
      } else {
        makeDesktopFile();
      }
    }
  }

  private void makeShellLink() {
    DesktopUtil.createShellLink(System.getProperty("java.home") + "\\bin\\javaw.exe",
      "-jar \"" + LauncherGlobals.USER_DIR + "\\KnightLauncher.jar\"",
      LauncherGlobals.USER_DIR,
      LauncherGlobals.USER_DIR + "\\KnightLauncher\\images\\icon-512.ico",
      "Start " + LauncherGlobals.LAUNCHER_NAME,
      LauncherGlobals.LAUNCHER_NAME
    );
  }

  private void makeDesktopFile() {
    File desktopFile = new File(DesktopUtil.getPathToDesktop(), LauncherGlobals.LAUNCHER_NAME + ".desktop");
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(desktopFile));
      out.write("[Desktop Entry]\n");
      out.write("Version=1.5\n");
      out.write("Name=Knight Launcher\n");
      out.write("Comment=Open source game launcher for a certain game\n");
      out.write("Exec=java -jar KnightLauncher.jar\n");
      out.write("Icon=" + LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-512.png\n");
      out.write("Path=" + LauncherGlobals.USER_DIR + "/\n");
      out.write("Type=Application\n");
      out.write("Categories=Game;\n");
      out.close();
      out.close();
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void setupHTTPSProtocol() {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    System.setProperty("http.agent", "Mozilla/5.0");
    System.setProperty("https.agent", "Mozilla/5.0");
  }

  private void setupFileLogging() {
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

    try {
      PrintStream printStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(logFile.toPath())), true);
      System.setOut(printStream);
      System.setErr(printStream);
    } catch (IOException e) {
      log.error(e);
    }

    log.info("Knight Launcher started. Running version: " + LauncherGlobals.LAUNCHER_VERSION);
  }

  private void logVMInfo() {
    log.info("------------ VM Info ------------");
    log.info("OS Name: " + System.getProperty("os.name"));
    log.info("OS Arch: " + System.getProperty("os.arch"));
    log.info("OS Vers: " + System.getProperty("os.version"));
    log.info("Java Home: " + System.getProperty("java.home"));
    log.info("Java Vers: " + System.getProperty("java.version"));
    log.info("User Name: " + System.getProperty("user.name"));
    log.info("User Home: " + System.getProperty("user.home"));
    log.info("Current Directory: " + System.getProperty("user.dir"));
    log.info("---------------------------------");
  }

  private void logGameVMInfo() {
    log.info("--------- Game VM Info ----------");
    log.info("Directory: " + JavaUtil.getGameJVMDirPath());
    log.info("Executable: " + JavaUtil.getGameJVMExePath());
    log.info("Data: " + JavaUtil.getGameJVMData());
    log.info("Version: " + JavaUtil.getJVMVersion(JavaUtil.getGameJVMExePath()));
    log.info("Arch: " + JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()));
    log.info("---------------------------------");
  }

  /* Uncomment this when the dreaded day comes around.

  private boolean requiresJVMPatch() {
    return _args.length > 0 && _args[0].equals("forceJVMPatch");
  }

  */

  private boolean requiresJVMPatch() {
    // First of all see if we're being forced to patch.
    if(_args.length > 0 && _args[0].equals("forceJVMPatch")) {
      return true;
    }

    // You need a 64-bit system to begin with.
    if(!SystemUtil.is64Bit()) return false;

    // Currently Java VM patching is only supported on Windows systems and Linux installs through Steam.
    if(!SystemUtil.isWindows() && !(SystemUtil.isUnix() && Settings.gamePlatform.startsWith("Steam"))) return false;

    // Check if there's already a 64-bit Java VM in the game's directory or if it already has been installed by Knight Launcher.
    String javaVMVersion = JavaUtil.getGameJVMData();

    if(( JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 && !javaVMVersion.contains("1.7") ) || Settings.jvmPatched) {
      Settings.jvmPatched = true;
      SettingsProperties.setValue("launcher.jvm_patched", "true");
      return false;
    }

    return true;
  }

  private boolean requiresUpdate() {
    return _args.length > 0 && _args[0].equals("update");
  }

  private void postInitialization() {
    new Thread(ModuleLoader::loadModules).start();
    if (!FileUtil.fileExists(LauncherGlobals.USER_DIR + "/KnightLauncher/modules/safeguard/bundle.zip")) {
      ModLoader.extractSafeguard();
    }

    loadOnlineAssets();
    new Thread(ModLoader::checkInstalled).start();
    DiscordPresenceClient.getInstance().setDetails(Locale.getValue("presence.launch_ready"));
  }

  private void loadOnlineAssets() {
    new Thread(this::checkVersion).start();

    new Thread(() -> {

      Status flamingoStatus = Flamingo.getStatus();
      if(flamingoStatus.version != null) LauncherApp.flamingoOnline = true;
      LauncherEventHandler.updateServerList(Flamingo.getServerList());
      SettingsEventHandler.updateAboutTab(flamingoStatus);
      SettingsEventHandler.updateActiveBetaCodes();

      getOfficialServerVersion();

    }).start();

    ThreadingUtil.executeWithDelay(this::checkFlamingoStatus, 10000);
  }

  private void checkFlamingoStatus() {
    if(!LauncherApp.flamingoOnline) {
      LauncherGUI.showWarning(Locale.getValue("error.flamingo_offline"));
    }
  }

  protected static int getOfficialAproxPlayerCount() {
    int steamPlayers = SteamUtil.getCurrentPlayers("99900");
    if (steamPlayers == 0) {
      return 0;
    } else {
      return Math.round(steamPlayers * 1.6f);
    }
  }

  private void checkVersion() {

    if (LauncherGlobals.LAUNCHER_VERSION.contains("dev")) return;

    String rawResponseReleases = INetUtil.getWebpageContent(
        LauncherGlobals.GITHUB_API
            + "repos/"
            + LauncherGlobals.GITHUB_AUTHOR + "/"
            + LauncherGlobals.GITHUB_REPO + "/"
            + "releases/"
            + "latest"
    );

    if(rawResponseReleases != null) {
      JSONObject jsonReleases = new JSONObject(rawResponseReleases);

      LauncherGUI.latestRelease = jsonReleases.getString("tag_name");
      LauncherGUI.latestChangelog = jsonReleases.getString("body");

      if (!LauncherGUI.latestRelease.equalsIgnoreCase(LauncherGlobals.LAUNCHER_VERSION)) {
        if(Settings.autoUpdate) {
          LauncherEventHandler.updateLauncher();
        }
        Settings.isOutdated = true;
        LauncherGUI.updateButton.setVisible(true);
      }
    } else {
      log.error("Received no response from GitHub. Possible downtime?");
    }
  }

  private void getOfficialServerVersion() {
    URL url = null;
    try {
      url = new URL(Settings.gameGetdownFullURL + "getdown.txt");
    } catch (MalformedURLException e) {
      log.error(e);
    }
    Properties prop = new Properties();
    try {
      prop.load(url.openStream());
    } catch (IOException e) {
      log.error(e);
    }

    Server officialServer = Objects.requireNonNull(findServerByName("Official"));
    officialServer.version = prop.getProperty("version");
    log.info("Latest Official server version updated", "version", officialServer.version);
  }

  public static Server findServerByName(String serverName) {
    List<Server> results = LauncherApp.serverList.stream()
      .filter(s -> serverName.equals(s.name)).collect(Collectors.toList());
    return results.isEmpty() ? null : results.get(0);
  }

  public static Server findServerBySanitizedName(String sanitizedServerName) {
    List<Server> results = LauncherApp.serverList.stream()
      .filter(s -> sanitizedServerName.equals(s.getSanitizedName())).collect(Collectors.toList());
    return results.isEmpty() ? null : results.get(0);
  }

  protected static void checkTempDir() {
    // sometimes os usernames that have cyrillic characters can make Java have a
    // bad time trying to read them for locating their TEMP path.
    // let's give our old friend a hand and store the temp files ourselves for the time being.
    boolean containsCyrillic = System.getProperty("user.name").codePoints()
      .mapToObj(Character.UnicodeScript::of)
      .anyMatch(Character.UnicodeScript.CYRILLIC::equals);
    if (containsCyrillic) SystemUtil.fixTempDir(LauncherGlobals.USER_DIR + "/KnightLauncher/temp/");
  }

  protected static void exit() {
    DiscordPresenceClient.getInstance().stop();
    if (!Settings.keepOpen) {
      LauncherGUI.launcherGUIFrame.dispose();
      System.exit(1);
    }
  }

}
