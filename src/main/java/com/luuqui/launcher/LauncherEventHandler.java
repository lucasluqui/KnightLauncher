package com.luuqui.launcher;

import com.luuqui.dialog.DialogInfo;
import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mods.ModLoader;
import com.luuqui.launcher.settings.*;
import com.luuqui.util.*;
import org.apache.commons.io.FileUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import static com.luuqui.launcher.Log.log;

public class LauncherEventHandler {

  private static final String[] RPC_COMMAND_LINE = new String[] { ".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe" };

  public static void launchGameEvent() {

    Thread launchThread = new Thread(() -> {

      // disable server switching during launch procedure
      LauncherGUI.serverList.setEnabled(false);

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

        ProcessUtil.runFromDirectory(getThirdPartyClientStartCommand(selectedServer),
          LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName,
          true);

        ProgressBar.finishTask();
      }

      DiscordRPC.getInstance().stop();
      if (!Settings.keepOpen) {
        LauncherGUI.launcherGUIFrame.dispose();
        System.exit(1);
      }

      // re-enable server switching
      LauncherGUI.serverList.setEnabled(true);

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
    official.playerCountUrl = LauncherApp.getSteamPlayerCountString();
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

    DialogInfo.push(
      infoString,
      selectedServer.name + " Server Information"
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
        LauncherGUI.serverInfoButton.setEnabled(false);
        LauncherGUI.serverInfoButton.setVisible(false);
        LauncherGUI.modButton.setEnabled(true);
      } else {
        LauncherGUI.launchButton.setText("Play " + selectedServer.name);
        LauncherGUI.launchButton.setToolTipText("Play " + selectedServer.name);
        LauncherGUI.launchButton.setEnabled(selectedServer.enabled == 1);
        LauncherGUI.serverInfoButton.setEnabled(true);
        LauncherGUI.serverInfoButton.setVisible(true);

        // TODO: Fetch player count.
        LauncherGUI.playerCountLabel.setVisible(false);

        // TODO: Modding support for third party servers.
        LauncherGUI.modButton.setEnabled(false);
      }
      LauncherApp.selectedServer = selectedServer;
    } else {
      // fallback to official in rare error scenario
      LauncherGUI.serverList.setSelectedIndex(0);
      LauncherApp.selectedServer = findServerInServerList("Official");
    }

    updateBanner();
    saveSelectedServer();
  }

  private static Server findServerInServerList(String serverName) {
    List<Server> results = LauncherApp.serverList.stream()
      .filter(s -> serverName.equals(s.name)).collect(Collectors.toList());
    return results.isEmpty() ? null : results.get(0);
  }

  public static void updateBanner() {
    Thread refreshThread = new Thread(() -> {
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

  private static String[] getThirdPartyClientStartCommand(Server server) {
    String[] args;
    String sanitizedServerName = LauncherApp.getSanitizedServerName(server.name);
    if(SystemUtil.isWindows()) {
      args = new String[]{
        LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "java_vm" + File.separator + "bin" + File.separator + "java",
        "-classpath",
        LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/config.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/projectx-config.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/projectx-pcode.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/lwjgl.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/lwjgl_util.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/jinput.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/jshortcut.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/commons-beanutils.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/commons-digester.jar;" +
          LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./code/commons-logging.jar;",
        "-Dcom.threerings.getdown=false",
        "-Xms256M",
        "-Xmx512M",
        "-XX:+AggressiveOpts",
        "-XX:SoftRefLRUPolicyMSPerMB=10",
        "-Djava.library.path=" + LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./native",
        "-Dorg.lwjgl.util.NoChecks=true",
        "-Dsun.java2d.d3d=false",
        "-Dappdir=" + LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + ".",
        "-Dresource_dir=" + LauncherGlobals.USER_DIR + "\\thirdparty\\" + sanitizedServerName + File.separator + "./rsrc",
        "com.threerings.projectx.client.ProjectXApp",
      };
    } else {
      args = new String[]{
        LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "java" + File.separator + "bin" + File.separator + "java",
        "-classpath",
        LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/config.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/projectx-config.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/projectx-pcode.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/lwjgl.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/lwjgl_util.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/jinput.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/jshortcut.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/commons-beanutils.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/commons-digester.jar:" +
          LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "code/commons-logging.jar:",
        "-Dcom.threerings.getdown=false",
        "-Xms256M",
        "-Xmx512M",
        "-XX:+AggressiveOpts",
        "-XX:SoftRefLRUPolicyMSPerMB=10",
        "-Djava.library.path=" + LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "native",
        "-Dorg.lwjgl.util.NoChecks=true",
        "-Dsun.java2d.d3d=false",
        "-Dappdir=" + LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator,
        "-Dresource_dir=" + LauncherGlobals.USER_DIR + "/thirdparty/" + sanitizedServerName + File.separator + "rsrc",
        "com.threerings.projectx.client.ProjectXApp",
      };
    }

    return args;
  }

}
