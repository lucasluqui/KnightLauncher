package com.luuqui.launcher;

import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.launcher.setting.*;
import com.luuqui.util.*;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        String sanitizedServerName = LauncherApp.getSanitizedServerName(selectedServer.name);

        ProgressBar.startTask();
        ProgressBar.setBarMax(2);
        ProgressBar.setState("Retrieving data from " + LauncherApp.selectedServer.name + "...");
        ProgressBar.setBarValue(0);

        if(!FileUtil.fileExists(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "version.txt")) {
          // we did not download this third party client, time to get it from the deploy url.

          ProgressBar.setState("Downloading " + LauncherApp.selectedServer.name + "... (this might take a while)");
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

            ProgressBar.setState("Updating " + LauncherApp.selectedServer.name + "... (this might take a while)");
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

        // we already have the client files,
        // the client is up-to-date, or the download has finished.
        // and so we start it up!
        ProgressBar.setState("Starting " + LauncherApp.selectedServer.name + "...");
        ProgressBar.setBarValue(2);

        ProcessUtil.runFromDirectory(getThirdPartyClientStartCommand(selectedServer, altMode),
          LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName,
          true);

        ProgressBar.finishTask();
      }

      final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
      executor.schedule(LauncherEventHandler::checkGameLaunched, 8, TimeUnit.SECONDS);

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
    official.playerCountUrl = "Players online: ~" + LauncherApp.getOfficialAproxPlayerCount();
    LauncherApp.serverList.add(official);

    for(Server server : servers) {

      if(server.name.equalsIgnoreCase("Official")) {
        official.announceBanner = server.announceBanner;
        official.announceContent = server.announceContent;
        official.announceBannerLink = server.announceBannerLink;
        continue;
      }

      if(server.beta == 1) server.name += " (Beta)";

      LauncherGUI.serverList.addItem(server.name);
      LauncherApp.serverList.add(server);

      // make sure we have a folder to later download the client
      FileUtil.createDir(LauncherGlobals.USER_DIR + "/thirdparty/" + LauncherApp.getSanitizedServerName(server.name));
    }

    try {
      LauncherGUI.serverList.setSelectedIndex(Settings.selectedServerIdx);
      LauncherApp.selectedServer = findServerInServerList((String) LauncherGUI.serverList.getSelectedItem());
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

    String infoString = "";
    infoString += "Name: " + selectedServer.name + "\n";
    infoString += "Description: " + selectedServer.description + "\n";
    infoString += "Version: " + selectedServer.version + "\n";
    infoString += "Managed by: " + selectedServer.managedBy + "\n";
    if(!selectedServer.siteUrl.equalsIgnoreCase("null")) infoString += "Website: " + selectedServer.siteUrl + "\n";
    if(!selectedServer.communityUrl.equalsIgnoreCase("null")) infoString += "Community: " + selectedServer.communityUrl + "\n";
    if(!selectedServer.sourceCodeUrl.equalsIgnoreCase("null")) infoString += "Source code: " + selectedServer.sourceCodeUrl + "\n";
    infoString += "\n * Neither your activity in third party services nor theirs is endorsed \n by Knight Launcher. Play at your own discretion.";

    Dialog.push(
      infoString,
      selectedServer.name + " Server Information", JOptionPane.INFORMATION_MESSAGE
    );
  }

  public static void selectedServerChanged(ActionEvent event) {
    Server selectedServer = findServerInServerList((String) LauncherGUI.serverList.getSelectedItem());

    if(selectedServer != null) {
      if(selectedServer.name.equalsIgnoreCase("Official")) {
        LauncherGUI.launchButton.setText("Play Now");
        LauncherGUI.launchButton.setToolTipText("Play Now");
        LauncherGUI.launchButton.setEnabled(selectedServer.enabled == 1);
        LauncherGUI.playerCountLabel.setText(selectedServer.playerCountUrl);
        LauncherGUI.playerCountLabel.setVisible(true);
        LauncherGUI.playerCountTooltipButton.setVisible(true);
        LauncherGUI.serverInfoButton.setEnabled(false);
        LauncherGUI.serverInfoButton.setVisible(false);
        LauncherGUI.modButton.setEnabled(true);
        LauncherGUI.editorsButton.setEnabled(true);

        SettingsGUI.switchUseIngameRPC.setEnabled(true);
        SettingsGUI.choicePlatform.setEnabled(true);
        SettingsGUI.forceRebuildButton.setEnabled(true);
        SettingsGUI.labelDisclaimer.setVisible(false);
        SettingsGUI.serverAddressTextField.setEnabled(true);
        SettingsGUI.portTextField.setEnabled(true);
        SettingsGUI.publicKeyTextField.setEnabled(true);
        SettingsGUI.getdownURLTextField.setEnabled(true);
        SettingsGUI.resetConnectionSettingsButton.setEnabled(true);
      } else {
        LauncherGUI.launchButton.setText("Play " + selectedServer.name);
        LauncherGUI.launchButton.setToolTipText("Play " + selectedServer.name);
        LauncherGUI.launchButton.setEnabled(selectedServer.enabled == 1);
        LauncherGUI.serverInfoButton.setEnabled(true);
        LauncherGUI.serverInfoButton.setVisible(true);
        LauncherGUI.playerCountTooltipButton.setVisible(false);

        // TODO: Fetch player count.
        LauncherGUI.playerCountLabel.setVisible(false);

        // TODO: Modding support for third party servers.
        LauncherGUI.modButton.setEnabled(false);

        // TODO: Editors support for third party servers.
        LauncherGUI.editorsButton.setEnabled(false);

        SettingsGUI.switchUseIngameRPC.setEnabled(false);
        SettingsGUI.choicePlatform.setEnabled(false);
        SettingsGUI.forceRebuildButton.setEnabled(false);
        SettingsGUI.labelDisclaimer.setVisible(true);
        SettingsGUI.serverAddressTextField.setEnabled(false);
        SettingsGUI.portTextField.setEnabled(false);
        SettingsGUI.publicKeyTextField.setEnabled(false);
        SettingsGUI.getdownURLTextField.setEnabled(false);
        SettingsGUI.resetConnectionSettingsButton.setEnabled(false);
      }
      LauncherApp.selectedServer = selectedServer;
    } else {
      // fallback to official in rare error scenario
      LauncherGUI.serverList.setSelectedIndex(0);
      LauncherApp.selectedServer = findServerInServerList("Official");
    }

    updateBanner();
    updateGameJavaVMData();
    saveSelectedServer();
  }

  private static Server findServerInServerList(String serverName) {
    List<Server> results = LauncherApp.serverList.stream()
      .filter(s -> serverName.equals(s.name)).collect(Collectors.toList());
    return results.isEmpty() ? null : results.get(0);
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

  public static void updateGameJavaVMData() {
    Thread thread = new Thread(() -> {
      SettingsGUI.javaVMBadge.setText("Your Java VM: " + JavaUtil.getReadableGameJVMData());

      boolean is64Bit = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64;
      SettingsGUI.memorySlider.setMaximum(is64Bit ? 4096 : 1024);
      SettingsEventHandler.memoryChangeEvent(SettingsGUI.memorySlider.getValue());
    });
    thread.start();
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
    Dialog.push("Currently running version: " + LauncherGlobals.LAUNCHER_VERSION
      + ".\nLatest available version: " + LauncherGUI.latestRelease + ".\n\n" +
      "Knight Launcher " + LauncherGUI.latestRelease + "\n"
      + LauncherGUI.latestChangelog,
      "Latest Changelog", JOptionPane.INFORMATION_MESSAGE);
  }

  private static String[] getThirdPartyClientStartCommand(Server server, boolean altMode) {
    List<String> argsList = new ArrayList<>();
    String sanitizedServerName = LauncherApp.getSanitizedServerName(server.name);

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

  private static void checkGameLaunched() {
    // TODO: Add Linux and Mac compatibility to launch checking.
    if(!SystemUtil.isWindows() || ProcessUtil.isGameRunningByTitle(LauncherApp.selectedServer.name.equalsIgnoreCase("Official") ? "Spiral Knights" : LauncherApp.selectedServer.name)) {
      DiscordRPC.getInstance().stop();
      if (!Settings.keepOpen) {
        LauncherGUI.launcherGUIFrame.dispose();
        System.exit(1);
      }
    } else {
      Dialog.push("The game was not able to launch successfully.\n\nIt's recommended to follow these steps:\n- Enter launcher settings, go to the \"Game\" tab, and press the \"Load recommended settings\" button.\n- Lower the allocated memory.\n- Patch a 64-bit Java VM.\n\nIf this does still not solve your issue you can look for technical support on Discord.", "Error While Launching", JOptionPane.ERROR_MESSAGE);
    }

    // re-enable server switching
    LauncherGUI.serverList.setEnabled(true);
    LauncherGUI.launchButton.setEnabled(true);
  }

}
