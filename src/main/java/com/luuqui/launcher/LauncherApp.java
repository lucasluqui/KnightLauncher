package com.luuqui.launcher;

import com.formdev.flatlaf.FlatDarkLaf;
import com.luuqui.dialog.DialogWarning;
import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.flamingo.Flamingo;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.flamingo.data.Status;
import com.luuqui.launcher.mods.ModListGUI;
import com.luuqui.launcher.mods.ModLoader;
import com.luuqui.launcher.settings.Settings;
import com.luuqui.launcher.settings.SettingsEventHandler;
import com.luuqui.launcher.settings.SettingsGUI;
import com.luuqui.launcher.settings.SettingsProperties;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.luuqui.launcher.Log.log;

public class LauncherApp {

  private String[] _args = null;
  protected static LauncherGUI lgui;
  protected static SettingsGUI sgui;
  protected static ModListGUI mgui;
  protected static JVMPatcher jvmPatcher;
  public static String projectXVersion = null;
  public static java.util.List<Server> serverList = new ArrayList<>();
  public static Server selectedServer = null;
  public static boolean flamingoOnline = false;

  public static void main(String[] args) {

    LauncherApp app = new LauncherApp();
    app._args = args;

    if (app.requiresJVMPatch()) {
      jvmPatcher = app.composeJVMPatcher(app);
    } else {
      lgui = app.composeLauncherGUI(app);
      sgui = app.composeSettingsGUI(app);
      mgui = app.composeModListGUI(app);
    }

    app.postInitialization();
  }

  public LauncherApp () {
    setupFileLogging();
    logVMInfo();
    checkStartLocation();
    setupHTTPSProtocol();
    SettingsProperties.setup();
    setupLauncherStyle();
    Locale.setup();
    Fonts.setup();
    if (!SystemUtil.isARM()) DiscordRPC.getInstance().start();
    KeyboardController.start();
    checkDirectories();
    LauncherDigester.doDigest();
    if (SystemUtil.isWindows() || SystemUtil.isUnix()) checkShortcut();
  }

  private LauncherGUI composeLauncherGUI(LauncherApp app) {
    EventQueue.invokeLater(() -> {
      try {
        lgui = new LauncherGUI(app);
        lgui.switchVisibility();
      } catch (Exception e) {
        log.error(e);
      }
    });
    return lgui;
  }

  private SettingsGUI composeSettingsGUI(LauncherApp app) {
    EventQueue.invokeLater(() -> {
      try {
        sgui = new SettingsGUI(app);
      } catch (Exception e) {
        log.error(e);
      }
    });
    return sgui;
  }

  private ModListGUI composeModListGUI(LauncherApp app) {
    EventQueue.invokeLater(() -> {
      try {
        mgui = new ModListGUI(app);
      } catch (Exception e) {
        log.error(e);
      }
    });
    return mgui;
  }

  private JVMPatcher composeJVMPatcher(LauncherApp app) {
    EventQueue.invokeLater(() -> {
      try {
        jvmPatcher = new JVMPatcher(app);
      } catch (Exception e) {
        log.error(e);
      }
    });
    return jvmPatcher;
  }

  private void checkDirectories() {
    // Stores /rsrc (.zip) mods.
    FileUtil.createDir("mods");

    // Stores /code (.jar) mods.
    FileUtil.createDir("code-mods");

    // Stores clients for third-party servers.
    FileUtil.createDir("thirdparty");

    // Miscellaneous image assets for the launcher to use.
    FileUtil.createDir("KnightLauncher/images/");

    // External modules necessary for extra functionality (eg. RPC)
    FileUtil.createDir("KnightLauncher/modules/");
  }

  // Checking if we're being ran inside the game's directory, "getdown-pro.jar" should always be present if so.
  private void checkStartLocation() {
    if (!FileUtil.fileExists("./getdown-pro.jar")) {
      String pathWarning = "The .jar file appears to be placed in the wrong directory. " +
              "In some cases this is due to a false positive and can be ignored. Knight Launcher will attempt to launch normally."
              + System.lineSeparator() + "If this persists try using the Batch (KnightLauncher_windows.bat) file for Windows " +
              "or the Shell (KnightLauncher_mac_linux.sh) file for OSX/Linux.";
      if (SystemUtil.isWindows()) {
        pathWarning += System.lineSeparator() + "Additionally, we've detected the following Steam path: " + SteamUtil.getGamePathWindows();
      }
      log.warning(pathWarning);
      DialogWarning.push(pathWarning);
      //if (SystemUtil.isWindows()) DesktopUtil.openDir(SteamUtil.getGamePathWindows());
    }
  }

  // Create a shortcut to the application if there's none.
  private void checkShortcut() {
    if (Settings.createShortcut
            && !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + LauncherGlobals.LAUNCHER_NAME)
            && !FileUtil.fileExists(DesktopUtil.getPathToDesktop() + "/" + LauncherGlobals.LAUNCHER_NAME + ".desktop")) {
      BufferedImage bimg = ImageUtil.loadImageWithinJar("/img/icon-128.png");
      try {
        ICOEncoder.write(bimg, new File(LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-128.ico"));
      } catch (IOException e) {
        log.error(e);
      }

      if (SystemUtil.isWindows()) {
        DesktopUtil.createShellLink(System.getProperty("java.home") + "\\bin\\javaw.exe",
                "-jar \"" + LauncherGlobals.USER_DIR + "\\KnightLauncher.jar\"",
                LauncherGlobals.USER_DIR,
                LauncherGlobals.USER_DIR + "\\KnightLauncher\\images\\icon-128.ico",
                "Start " + LauncherGlobals.LAUNCHER_NAME,
                LauncherGlobals.LAUNCHER_NAME
        );
      } else {
        makeDesktopFile();
      }
    }
  }

  private void makeDesktopFile() {
    File desktopFile = new File(DesktopUtil.getPathToDesktop(), LauncherGlobals.LAUNCHER_NAME + ".desktop");
    File shFile = new File(DesktopUtil.getPathToDesktop(), ".KL.sh");
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(desktopFile));
      out.write("[Desktop Entry]\n");
      out.write("Version=1.5\n");
      out.write("Exec=" + DesktopUtil.getPathToDesktop() + "/.KL.sh\n");
      out.write("Name=Knight Launcher\n");
      out.write("Type=Application\n");
      out.write("Icon=" + LauncherGlobals.USER_DIR + "/KnightLauncher/images/icon-128.ico\n");
      out.write("Comment=Open source game launcher for a certain game\n");
      out.close();
    } catch (IOException e) {
      log.error(e);
    }
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(shFile));
      out.write("#! /bin/bash\n\n");
      out.write("cd " + LauncherGlobals.USER_DIR.replaceAll(" ", "\\\\ ") + "\n");
      out.write("java -jar KnightLauncher.jar\n");
      out.close();
    } catch (IOException e) {
      log.error(e);
    }
    shFile.setExecutable(true);
  }

  private void setupLauncherStyle() {
    System.setProperty("awt.useSystemAAFontSettings", "on");
    System.setProperty("swing.aatext", "true");

    IconFontSwing.register(FontAwesome.getIconFont());
    try {
      UIManager.setLookAndFeel(new FlatDarkLaf());
    } catch (UnsupportedLookAndFeelException e) {
      log.error(e);
    }
  }

  private void setupHTTPSProtocol() {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    System.setProperty("http.agent", "Mozilla/5.0");
    System.setProperty("https.agent", "Mozilla/5.0");
  }

  private void setupFileLogging() {
    File logFile = new File("knightlauncher.log");
    File oldLogFile = new File("old-knightlauncher.log");

    if (logFile.exists()) {
      logFile.renameTo(oldLogFile);
    }

    try {
      PrintStream printStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(logFile.toPath())), true);
      System.setOut(printStream);
      System.setErr(printStream);
    } catch (IOException e) {
      e.printStackTrace();
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

  private boolean requiresJVMPatch() {

    // First of all see if we're being forced to patch.
    if(_args.length > 0 && _args[0].equals("forceJVMPatch")) return true;

    // You need a 64-bit system to begin with.
    if(!SystemUtil.is64Bit()) return false;

    // Currently Java VM patching is only supported on Windows systems and Linux installs through Steam.
    if(!SystemUtil.isWindows() && !(SystemUtil.isUnix() && Settings.gamePlatform.startsWith("Steam"))) return false;

    // Check if there's already a 64-bit Java VM in the game's directory or if it already has been installed by Knight Launcher.
    if(JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 ||
    Settings.jvmPatched) {
      Settings.jvmPatched = true;
      SettingsProperties.setValue("launcher.jvm_patched", "true");
      return false;
    }

    return true;
  }

  private void postInitialization() {
    ModLoader.checkInstalled();
    if (Settings.doRebuilds && ModLoader.rebuildRequired) ModLoader.startFileRebuild();
    if (Settings.useIngameRPC) ModuleLoader.loadIngameRPC();
    if (!FileUtil.fileExists(LauncherGlobals.USER_DIR + "\\KnightLauncher\\modules\\safeguard\\bundle.zip")) {
      ModLoader.extractSafeguard();
    }

    ModuleLoader.loadJarCommandLine();

    DiscordRPC.getInstance().setDetails(Locale.getValue("presence.launch_ready", String.valueOf(ModLoader.getEnabledModCount())));
    loadOnlineAssets();
  }

  private void loadOnlineAssets() {
    Thread onlineAssetsThread = new Thread(() -> {

      checkVersion();
      getProjectXVersion();

    });

    Thread flamingoThread = new Thread(() -> {

      LauncherEventHandler.updateServerList(Flamingo.getServerList());
      Status flamingoStatus = Flamingo.getStatus();
      SettingsEventHandler.updateAboutTab(flamingoStatus);
      if(flamingoStatus.version != null) LauncherApp.flamingoOnline = true;

    });

    onlineAssetsThread.start();
    flamingoThread.start();

    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    executor.schedule(this::checkFlamingoStatus, 10, TimeUnit.SECONDS);
  }

  private void checkFlamingoStatus() {
    if(!LauncherApp.flamingoOnline) {
      LauncherGUI.showWarning("Flamingo is offline");
    }
  }

  protected static String getSteamPlayerCountString() {
    int steamPlayers = SteamUtil.getCurrentPlayers("99900");
    if (steamPlayers == 0) {
      return Locale.getValue("error.get_player_count");
    } else {
      int approximateTotalPlayers = Math.round(steamPlayers * 1.6f);
      return Locale.getValue("m.player_count", new String[]{
        String.valueOf(approximateTotalPlayers), String.valueOf(steamPlayers)
      });
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

      String latestRelease = jsonReleases.getString("tag_name");
      if (!latestRelease.equalsIgnoreCase(LauncherGlobals.LAUNCHER_VERSION)) {
        Settings.isOutdated = true;
        LauncherGUI.updateButton.addActionListener(action -> DesktopUtil.openWebpage(
            "https://github.com/"
                + LauncherGlobals.GITHUB_AUTHOR + "/"
                + LauncherGlobals.GITHUB_REPO + "/"
                + "releases/tag/"
                + latestRelease
        ));
        LauncherGUI.updateButton.setVisible(true);
      }
    } else {
      log.error("Received no response from GitHub. Possible downtime?");
    }
  }

  private void getProjectXVersion() {
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

    LauncherApp.projectXVersion = prop.getProperty("version");
    log.info("Latest ProjectX version updated", "version", LauncherApp.projectXVersion);
  }

  public static String getSanitizedServerName(String serverName) {
    return serverName.toLowerCase().replace(" ", "-")
      .replace("(", "").replace(")", "");
  }

}
