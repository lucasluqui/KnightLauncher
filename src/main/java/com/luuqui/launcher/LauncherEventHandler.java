package com.luuqui.launcher;

import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.ModListEventHandler;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.launcher.setting.*;
import com.luuqui.util.*;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static com.luuqui.launcher.Log.log;

public class LauncherEventHandler {

  private static final String[] RPC_COMMAND_LINE = new String[] { ".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe" };

  public static void launchGameEvent(boolean altMode) {

    Thread launchThread = new Thread(() -> {

      // disable server switching and launch button during launch procedure
      LauncherGUI.serverList.setEnabled(false);
      LauncherGUI.launchButton.setEnabled(false);

      if(LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
        // official servers launch procedure
        if (ModLoader.mountRequired) ModLoader.mount();
        SettingsEventHandler.saveAdditionalArgs();
        SettingsEventHandler.saveConnectionSettings();
        GameSettings.load();

        if (Settings.gamePlatform.startsWith("Steam")) {

          try {
            SteamUtil.startGameById(99900);
          } catch (Exception e) {
            log.error(e);
          }

        } else {

          if (SystemUtil.isWindows()) {
            ProcessUtil.run(LauncherGlobals.GETDOWN_ARGS_WIN, true);
          } else {
            ProcessUtil.run(LauncherGlobals.GETDOWN_ARGS, true);
          }

        }

        log.info("Starting game", "platform", Settings.gamePlatform);
        if (Settings.useIngameRPC) ProcessUtil.run(RPC_COMMAND_LINE, true);
      } else {
        // third party server launch procedure
        Server selectedServer = LauncherApp.selectedServer;
        String sanitizedServerName = selectedServer.getSanitizedName();

        ProgressBar.startTask();
        ProgressBar.setBarMax(2);
        ProgressBar.setState(Locale.getValue("m.launch_thirdparty_data", LauncherApp.selectedServer.name));
        ProgressBar.setBarValue(0);

        if(!FileUtil.fileExists(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "version.txt")) {
          // we did not download this third party client, time to get it from the deploy url.

          ProgressBar.setState(Locale.getValue("m.launch_thirdparty_download", LauncherApp.selectedServer.name));
          ProgressBar.setBarValue(1);

          boolean downloadCompleted = false;
          int downloadAttempts = 0;

          while(downloadAttempts <= 3 && !downloadCompleted) {
            downloadAttempts++;
            log.info("Downloading a third party client: " + sanitizedServerName,
              "attempts", downloadAttempts);
            try {
              FileUtils.copyURLToFile(
                new URL(selectedServer.deployUrl + "/" + selectedServer.version + ".zip"),
                new File(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip"),
                0,
                0
              );
              Compressor.unzip(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip",
                LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName, false);
              FileUtil.deleteFile(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip");
              downloadCompleted = true;
            } catch (IOException e) {
              // Just keep retrying.
              log.error(e);
            }
          }
        }

        // let's see if we need to update the third party client
        try {
          String localVersion = FileUtil.readFile(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "version.txt");
          if(!selectedServer.version.equalsIgnoreCase(localVersion)) {
            log.info("Updating third party client: " + selectedServer.name);

            ProgressBar.setState(Locale.getValue("m.launch_thirdparty_update", LauncherApp.selectedServer.name));
            ProgressBar.setBarValue(1);

            boolean downloadCompleted = false;
            int downloadAttempts = 0;

            while (downloadAttempts <= 3 && !downloadCompleted) {
              downloadAttempts++;
              log.info("Downloading a third party client: " + sanitizedServerName,
                "attempts", downloadAttempts);
              try {
                FileUtils.copyURLToFile(
                  new URL(selectedServer.deployUrl + "/" + selectedServer.version + ".zip"),
                  new File(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip"),
                  0,
                  0
                );
                Compressor.unzip(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip",
                  LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName, false);
                FileUtil.deleteFile(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip");

                // Delete old base.zip bundle so we have an up-to-date vanilla state zip.
                if(FileUtil.fileExists(selectedServer.getRootDirectory() + "/rsrc/base.zip")) {
                  FileUtil.deleteFile(selectedServer.getRootDirectory() + "/rsrc/base.zip");
                  try {
                    ProgressBar.setState(Locale.getValue("m.launch_thirdparty_bundle_regen", LauncherApp.selectedServer.name));
                    Compressor.zipFolderContents(new File(selectedServer.getRootDirectory() + "/rsrc"),
                      new File(selectedServer.getRootDirectory() + "/rsrc/base.zip"), "base.zip");
                  } catch (Exception e) {
                    log.error(e);
                  }
                }

                // ...and we're done updating.
                downloadCompleted = true;
              } catch (IOException e) {
                // Just keep retrying.
                log.error(e);
              }

            }
          }
          log.info("Third party client up to date: " + selectedServer.name);
        } catch (IOException e) {
          log.error(e);
        }

        // make sure there's a base zip file we can use to clean files with.
        String rootDir = selectedServer.getRootDirectory();
        if(FileUtil.fileExists(rootDir + "/rsrc")
          && !FileUtil.fileExists(rootDir + "/rsrc/base.zip")) {
          try {
            Compressor.zipFolderContents(new File(rootDir + "/rsrc"), new File(rootDir + "/rsrc/base.zip"), "base.zip");
          } catch (Exception e) {
            log.error(e);
          }
        }

        // we already have the client files,
        // the client is up-to-date, or the download has finished.
        // and so we start it up!
        ProgressBar.setState(Locale.getValue("m.launch_thirdparty_start", LauncherApp.selectedServer.name));
        ProgressBar.setBarValue(2);

        ProcessUtil.runFromDirectory(getThirdPartyClientStartCommand(selectedServer, altMode),
          LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName,
          true);

        ProgressBar.finishTask();
      }

      ThreadingUtil.executeWithDelay(LauncherEventHandler::checkGameLaunch, 8000);
    });
    launchThread.start();

  }

  public static void launchGameAltEvent() {

    Thread launchAltThread = new Thread(() -> {

      if(LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
        // official servers alt launch procedure
        if (!SystemUtil.isWindows()) {
          ProcessUtil.run(LauncherGlobals.ALT_CLIENT_ARGS, true);
        } else {
          ProcessUtil.run(LauncherGlobals.ALT_CLIENT_ARGS_WIN, true);
        }

        DiscordRPC.getInstance().stop();
      } else {
        // third party alt launch procedure
      }

    });
    launchAltThread.start();

  }

  public static void updateServerList(List<Server> servers) {
    LauncherApp.serverList.clear();
    Server official = new Server("Official");
    official.playerCountUrl = Locale.getValue("m.players_online_official", String.valueOf(LauncherApp.getOfficialAproxPlayerCount()));
    LauncherApp.serverList.add(official);

    for(Server server : servers) {

      if(server.name.equalsIgnoreCase("Official")) {
        official.announceBanner = server.announceBanner;
        official.announceContent = server.announceContent;
        official.announceBannerLink = server.announceBannerLink;
        continue;
      }

      // Prevent from adding duplicate servers
      if(LauncherApp.findServerByName(server.name) != null) {
        log.info("Tried to add duplicate server", "server", server.name);
        continue;
      }

      if(server.beta == 1) server.name += " (Beta)";

      LauncherGUI.serverList.addItem(server.name);
      LauncherApp.serverList.add(server);

      // make sure we have a proper folder structure for this server.
      String serverName = server.getSanitizedName();
      FileUtil.createDir(LauncherGlobals.USER_DIR + "/thirdparty/" + serverName);
      FileUtil.createDir(LauncherGlobals.USER_DIR + "/thirdparty/" + serverName + "/mods");

      // make sure there's a base zip file we can use to clean files with.
      String rootDir = server.getRootDirectory();
      if(FileUtil.fileExists(rootDir + "/rsrc")
        && !FileUtil.fileExists(rootDir + "/rsrc/base.zip")) {
        try {
          Compressor.zipFolderContents(new File(rootDir + "/rsrc"), new File(rootDir + "/rsrc/base.zip"), "base.zip");
        } catch (Exception e) {
          log.error(e);
        }
      }

      // check server specific settings keys.
      SettingsEventHandler.checkServerSettingsKeys(serverName);
      ModListEventHandler.checkServerSettingsKeys(serverName);
    }

    try {
      LauncherGUI.serverList.setSelectedIndex(Settings.selectedServerIdx);
      LauncherApp.selectedServer = LauncherApp.findServerByName((String) LauncherGUI.serverList.getSelectedItem());
    } catch (Exception e) {
      log.error(e);
      LauncherGUI.serverList.setSelectedIndex(0);
      LauncherApp.selectedServer = official;
    }

    selectedServerChanged(null);
  }

  public static void saveSelectedServer() {
    SettingsProperties.setValue("launcher.selectedServerIdx", String.valueOf(LauncherGUI.serverList.getSelectedIndex()));
  }

  public static void displaySelectedServerInfo() {
    Server selectedServer = LauncherApp.selectedServer;

    String infoString = Locale.getValue(
      "m.server_info_text",
      new String[] {
        selectedServer.name,
        selectedServer.description,
        selectedServer.version,
        selectedServer.managedBy
      }
    );

    if(!selectedServer.siteUrl.equalsIgnoreCase("null"))
      infoString += Locale.getValue("m.server_info_text_siteurl", selectedServer.siteUrl);

    if(!selectedServer.communityUrl.equalsIgnoreCase("null"))
      infoString += Locale.getValue("m.server_info_text_communityurl", selectedServer.communityUrl);

    if(!selectedServer.sourceCodeUrl.equalsIgnoreCase("null"))
      infoString += Locale.getValue("m.server_info_text_sourcecode", selectedServer.sourceCodeUrl);

    infoString += Locale.getValue("m.server_info_text_disclaimer");

    Dialog.push(
      infoString,
      selectedServer.name + " " + Locale.getValue("m.server_info"),
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  public static void selectedServerChanged(ActionEvent event) {
    Server selectedServer = LauncherApp.findServerByName((String) LauncherGUI.serverList.getSelectedItem());

    if(selectedServer != null) {
      if(selectedServer.name.equalsIgnoreCase("Official")) {
        LauncherGUI.launchButton.setText(Locale.getValue("b.play_now"));
        LauncherGUI.launchButton.setToolTipText(Locale.getValue("b.play_now"));
        LauncherGUI.launchButton.setEnabled(selectedServer.enabled == 1);
        LauncherGUI.playerCountLabel.setText(selectedServer.playerCountUrl);
        LauncherGUI.playerCountLabel.setVisible(true);
        LauncherGUI.playerCountTooltipButton.setVisible(true);
        LauncherGUI.serverInfoButton.setEnabled(false);
        LauncherGUI.serverInfoButton.setVisible(false);
        LauncherGUI.editorsButton.setEnabled(true);
        LauncherGUI.editorsButton.setVisible(true);
      } else {
        LauncherGUI.launchButton.setText(Locale.getValue("b.play_now_thirdparty", selectedServer.name));
        LauncherGUI.launchButton.setToolTipText(Locale.getValue("b.play_now_thirdparty", selectedServer.name));
        LauncherGUI.launchButton.setEnabled(selectedServer.enabled == 1);
        LauncherGUI.serverInfoButton.setEnabled(true);
        LauncherGUI.serverInfoButton.setVisible(true);
        LauncherGUI.playerCountTooltipButton.setVisible(false);

        // TODO: Fetch player count.
        LauncherGUI.playerCountLabel.setVisible(false);

        // TODO: Editors support for third party servers.
        LauncherGUI.editorsButton.setEnabled(false);
        LauncherGUI.editorsButton.setVisible(false);
      }
      LauncherApp.selectedServer = selectedServer;

      updateBanner();
      SettingsEventHandler.selectedServerChanged();
      ModListEventHandler.selectedServerChanged();
      saveSelectedServer();
    } else {
      // fallback to official in rare error scenario
      LauncherGUI.serverList.setSelectedIndex(0);
      LauncherApp.selectedServer = LauncherApp.findServerByName("Official");
    }
  }

  public static void updateBanner() {
    Thread refreshThread = new Thread(() -> {
      if(!LauncherApp.flamingoOnline) {
        return;
      }
      String bannerUrl = LauncherApp.selectedServer.announceBanner.split("\\|")[0];
      double bannerIntensity = Double.parseDouble(LauncherApp.selectedServer.announceBanner.split("\\|")[1]);
      LauncherGUI.banner = LauncherGUI.processImageForBanner(ImageUtil.toBufferedImage(ImageUtil.getImageFromURL(bannerUrl, 800, 550)), bannerIntensity);
      LauncherGUI.mainPane.repaint();

      LauncherGUI.bannerTitle.setText(LauncherApp.selectedServer.announceContent.split("\\|")[0]);

      String bannerSubtitle = LauncherApp.selectedServer.announceContent.split("\\|")[1];

      if(bannerSubtitle.contains("\n")) {
        LauncherGUI.bannerSubtitle1.setText(bannerSubtitle.split("\n")[0]);
        LauncherGUI.bannerSubtitle2.setText(bannerSubtitle.split("\n")[1]);
        LauncherGUI.bannerSubtitle2.setVisible(true);
      } else {
        LauncherGUI.bannerSubtitle1.setText(bannerSubtitle);
        LauncherGUI.bannerSubtitle2.setVisible(false);
      }

      if(!LauncherApp.selectedServer.announceBannerLink.equalsIgnoreCase("null")) {

        ActionListener[] listeners = LauncherGUI.bannerLinkButton.getActionListeners();
        for (ActionListener listener : listeners) {
          LauncherGUI.bannerLinkButton.removeActionListener(listener);
        }

        LauncherGUI.bannerLinkButton.addActionListener(l -> {
          DesktopUtil.openWebpage(LauncherApp.selectedServer.announceBannerLink);
        });
        LauncherGUI.bannerLinkButton.setVisible(true);
      } else {
        LauncherGUI.bannerLinkButton.setVisible(false);
      }
    });
    refreshThread.start();
  }

  public static void updateLauncher() {
    // delete any existing updaters from previous updates
    new File(LauncherGlobals.USER_DIR + "/updater.jar").delete();
    try {
      Files.copy(Paths.get(LauncherGlobals.USER_DIR + "/KnightLauncher.jar"), Paths.get(LauncherGlobals.USER_DIR + "/updater.jar"), StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      log.error(e);
    }
    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + "/updater.jar", "update"}, true);
    System.exit(1);
  }

  public static void showLatestChangelog() {
    Dialog.push(Locale.getValue(
        "m.changelog_text",
        new String[] {
          LauncherGlobals.LAUNCHER_VERSION,
          LauncherGUI.latestRelease,
          LauncherGUI.latestChangelog
        }), JOptionPane.INFORMATION_MESSAGE);
  }

  private static String[] getThirdPartyClientStartCommand(Server server, boolean altMode) {
    List<String> argsList = new ArrayList<>();
    String sanitizedServerName = server.getSanitizedName();

    if(SystemUtil.isWindows()) {
      argsList.add(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "java_vm" + File.separator + "bin" + File.separator + "java");
      argsList.add("-classpath");
      argsList.add(LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/config.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/projectx-config.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/projectx-pcode.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/lwjgl.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/lwjgl_util.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/jinput.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/jshortcut.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/commons-beanutils.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/commons-digester.jar;" +
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/commons-logging.jar;");
      argsList.add("-Dcom.threerings.getdown=false");
      if(Settings.gameDisableExplicitGC) argsList.add("-XX:+DisableExplicitGC");
      if(Settings.gameUseCustomGC && Settings.gameGarbageCollector.equalsIgnoreCase("ParallelOld")) argsList.add("-XX:+UseParallelGC");
      if(Settings.gameUseCustomGC) argsList.add("-XX:+Use" + Settings.gameGarbageCollector + "GC");
      argsList.add(altMode ? "-Xms256M" : Settings.gameGarbageCollector.equalsIgnoreCase("G1") ? "-Xms" + Settings.gameMemory + "M" : "-Xms" + Settings.gameMemory / 2 + "M");
      argsList.add(altMode ? "-Xmx512M" : "-Xmx" + Settings.gameMemory + "M");
      argsList.add("-XX:+AggressiveOpts");
      argsList.add("-XX:SoftRefLRUPolicyMSPerMB=10");

      if(!Settings.gameAdditionalArgs.isEmpty()) {
        argsList.addAll(Arrays.asList(Settings.gameAdditionalArgs.trim().split("\n")));
      }

      argsList.add("-Djava.library.path=" + LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + ".");
      argsList.add("-Dresource_dir=" + LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./rsrc");
      argsList.add("com.threerings.projectx.client.ProjectXApp");
    } else {
      argsList.add(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "java" + File.separator + "bin" + File.separator + "java");
      argsList.add("-classpath");
      argsList.add(LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/config.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/projectx-config.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/projectx-pcode.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/lwjgl.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/lwjgl_util.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/jinput.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/jshortcut.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/commons-beanutils.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/commons-digester.jar:" +
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/commons-logging.jar:");
      argsList.add("-Dcom.threerings.getdown=false");
      if(Settings.gameDisableExplicitGC) argsList.add("-XX:+DisableExplicitGC");
      if(Settings.gameUseCustomGC && Settings.gameGarbageCollector.equalsIgnoreCase("ParallelOld")) argsList.add("-XX:+UseParallelGC");
      if(Settings.gameUseCustomGC) argsList.add("-XX:+Use" + Settings.gameGarbageCollector + "GC");
      argsList.add(altMode ? "-Xms256M" : Settings.gameGarbageCollector.equalsIgnoreCase("G1") ? "-Xms" + Settings.gameMemory + "M" : "-Xms" + Settings.gameMemory / 2 + "M");
      argsList.add(altMode ? "-Xmx512M" : "-Xmx" + Settings.gameMemory + "M");
      argsList.add("-XX:+AggressiveOpts");
      argsList.add("-XX:SoftRefLRUPolicyMSPerMB=10");

      if(!Settings.gameAdditionalArgs.isEmpty()) {
        argsList.addAll(Arrays.asList(Settings.gameAdditionalArgs.trim().split("\n")));
      }

      argsList.add("-Djava.library.path=" + LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator);
      argsList.add("-Dresource_dir=" + LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "rsrc");
      argsList.add("com.threerings.projectx.client.ProjectXApp");
    }

    return argsList.toArray(new String[argsList.size()]);
  }

  private static void checkGameLaunch() {
    if(isGameRunning()) {
      LauncherApp.exit();

      // re-enable server switching and launching.
      LauncherGUI.serverList.setEnabled(true);
      LauncherGUI.launchButton.setEnabled(true);
    } else {
      try {
        Thread.sleep(8000);
        if(isGameRunning()) {
          LauncherApp.exit();
        } else {
          Dialog.push(Locale.getValue("error.game_launch"), Locale.getValue("t.game_launch_error"), JOptionPane.ERROR_MESSAGE);
        }

        // re-enable server switching and launching.
        LauncherGUI.serverList.setEnabled(true);
        LauncherGUI.launchButton.setEnabled(true);
      } catch (InterruptedException e) {
        log.error(e);
      }
    }
  }

  private static boolean isGameRunning() {
    // TODO: Add Linux and Mac compatibility to launch checking.
    return !SystemUtil.isWindows() || ProcessUtil.isGameRunningByTitle(LauncherApp.selectedServer.name.equalsIgnoreCase("Official") ? "Spiral Knights" : LauncherApp.selectedServer.name);
  }

}
