package com.luuqui.launcher;

import com.formdev.flatlaf.FlatClientProperties;
import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.launcher.editor.EditorsEventHandler;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.ModListEventHandler;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.launcher.setting.*;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.luuqui.launcher.Log.log;

public class LauncherEventHandler {

  private static final String[] RPC_COMMAND_LINE = new String[] { ".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe" };

  public static void launchGameEvent(boolean altMode) {

    Thread launchThread = new Thread(() -> {

      // disable server switching and launch button during launch procedure
      LauncherEventHandler.updateServerSwitcher(true);
      LauncherGUI.launchButton.setEnabled(false);

      if(LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
        // start: official servers launch procedure
        if (ModLoader.mountRequired) ModLoader.mount();
        SettingsEventHandler.saveAdditionalArgs();
        SettingsEventHandler.saveConnectionSettings();
        GameSettings.load();

        if (Settings.gamePlatform.startsWith("Steam")) {

          try {
            SteamUtil.startGameById(99900, SystemUtil.isMac());
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
        // end: official servers launch procedure
      } else {
        // start: third party server launch procedure
        Server selectedServer = LauncherApp.selectedServer;
        String sanitizedServerName = selectedServer.getSanitizedName();

        ProgressBar.startTask();
        ProgressBar.setBarMax(2);
        ProgressBar.setState(Locale.getValue("m.launch_thirdparty_data", LauncherApp.selectedServer.name));
        ProgressBar.setBarValue(0);

        // we did not download this third party client yet, time to get it from the deploy url.
        if(!selectedServer.isInstalled()) {
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
        if(selectedServer.isOutdated()) {
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
        ProcessUtil.run(LauncherGlobals.ALT_CLIENT_ARGS, true);
        DiscordPresenceClient.getInstance().stop();
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
        official.announceBannerStartsAt = server.announceBannerStartsAt;
        official.announceBannerEndsAt = server.announceBannerEndsAt;
        continue;
      }

      // Prevent from adding duplicate servers
      if(LauncherApp.findServerByName(server.name) != null) {
        log.info("Tried to add duplicate server", "server", server.name);
        continue;
      }

      if(server.beta == 1) server.name += " (Beta)";

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
      LauncherApp.selectedServer = LauncherApp.findServerBySanitizedName(Settings.selectedServerName);
    } catch (Exception e) {
      log.error(e);
      LauncherApp.selectedServer = official;
    }
    selectedServerChanged();
  }

  public static void saveSelectedServer() {
    String serverName = LauncherApp.selectedServer.getSanitizedName();
    if(serverName.isEmpty()) serverName = "official";
    SettingsProperties.setValue("launcher.selectedServerName", serverName);
  }

  public static void saveSelectedServer(String serverName) {
    if(serverName.isEmpty()) serverName = "official";
    SettingsProperties.setValue("launcher.selectedServerName", serverName);
  }

  public static void openAuctionsWebpage(ActionEvent action) {
    DesktopUtil.openWebpage("https://www.sk-ah.com");
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
      selectedServer.name + " " + Locale.getValue("t.server_info"),
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  public static void selectedServerChanged() {
    Server selectedServer = LauncherApp.selectedServer;

    if(selectedServer != null) {
      if(selectedServer.isOfficial()) {
        LauncherGUI.launchButton.setText(Locale.getValue("b.play_now"));
        LauncherGUI.launchButton.setToolTipText(Locale.getValue("b.play_now"));
        LauncherGUI.launchButton.setEnabled(selectedServer.enabled == 1);
        LauncherGUI.selectedServerLabel.setText(Locale.getValue("m.server", "Official"));
        LauncherGUI.playerCountLabel.setText(selectedServer.playerCountUrl);
        LauncherGUI.playerCountLabel.setVisible(true);
        LauncherGUI.playerCountTooltipButton.setVisible(true);
        LauncherGUI.serverInfoButton.setEnabled(false);
        LauncherGUI.serverInfoButton.setVisible(false);
        LauncherGUI.auctionButton.setVisible(true);
      } else {
        LauncherGUI.launchButton.setEnabled(selectedServer.enabled == 1);
        if(!selectedServer.isInstalled()) {
          LauncherGUI.launchButton.setText(Locale.getValue("b.install_thirdparty", selectedServer.name));
          LauncherGUI.launchButton.setToolTipText(Locale.getValue("b.install_thirdparty", selectedServer.name));
        } else if (selectedServer.isOutdated()) {
          LauncherGUI.launchButton.setText(Locale.getValue("b.update_thirdparty", selectedServer.name));
          LauncherGUI.launchButton.setToolTipText(Locale.getValue("b.update_thirdparty", selectedServer.name));
        } else {
          LauncherGUI.launchButton.setText(Locale.getValue("b.play_thirdparty", selectedServer.name));
          LauncherGUI.launchButton.setToolTipText(Locale.getValue("b.play_thirdparty", selectedServer.name));
        }

        LauncherGUI.selectedServerLabel.setText(Locale.getValue("m.server", ""));

        LauncherGUI.serverInfoButton.setEnabled(true);
        LauncherGUI.serverInfoButton.setVisible(true);
        LauncherGUI.serverInfoButton.setText(selectedServer.name);

        // TODO: Fetch player count.
        LauncherGUI.playerCountLabel.setText("Players online: Unavailable");
        LauncherGUI.playerCountTooltipButton.setVisible(false);

        LauncherGUI.auctionButton.setVisible(false);
      }

      updateBanner();
      SettingsEventHandler.selectedServerChanged();
      ModListEventHandler.selectedServerChanged();
      EditorsEventHandler.selectedServerChanged();
      saveSelectedServer();
      updateServerSwitcher(false);
      updateServerSwitcher(false); // why do we have to call this twice for it to work correctly? TODO: figure out!
    } else {
      // fallback to official in rare error scenario
      LauncherApp.selectedServer = LauncherApp.findServerByName("Official");
      selectedServerChanged();
    }
  }

  public static void updateServerSwitcher(boolean locked) {
    LauncherGUI.serverSwitcherPane.removeAll();

    List<Server> serverList = LauncherApp.serverList;
    if(!serverList.isEmpty()) {
      int count = 0;
      String borderColor = ColorUtil.colorToHexString(locked ? CustomColors.INTERFACE_SERVERSWITCHER_LOCKED_HOVER_BORDER : CustomColors.INTERFACE_SERVERSWITCHER_HOVER_BORDER);

      BufferedImage officialServerBufferedImage = ImageUtil.loadImageWithinJar("/img/server-official.png");
      officialServerBufferedImage = ImageUtil.resizeImagePreserveTransparency(officialServerBufferedImage, 32, 32);
      ImageIcon officialServerImageIcon = new ImageIcon(ImageUtil.addRoundedCorners(officialServerBufferedImage, 15));

      BufferedImage defaultServerBufferedImage = ImageUtil.loadImageWithinJar("/img/default-64.png");
      defaultServerBufferedImage = ImageUtil.resizeImagePreserveTransparency(defaultServerBufferedImage, 32, 32);
      ImageIcon defaultServerImageIcon = new ImageIcon(ImageUtil.addRoundedCorners(defaultServerBufferedImage, 15));

      for(Server server : serverList) {
        JPanel serverIconPane = new JPanel();
        serverIconPane.setLayout(null);
        serverIconPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
        serverIconPane.setBounds(0, count * 50, 50, 50);

        JLabel serverIcon = new JLabel();
        serverIcon.setBounds(4, 4, 42, 42);
        serverIcon.setHorizontalAlignment(SwingConstants.CENTER);
        if(server.isOfficial()) {
          serverIcon.setIcon(officialServerImageIcon);
        } else {
          ImageIcon serverIconImageIcon;
          if(server.serverIcon.equalsIgnoreCase("null")) {
            serverIconImageIcon = defaultServerImageIcon;
          } else {
            BufferedImage serverIconBufferedImage = Cache.fetchImage(server.serverIcon, 32, 32);
            serverIconImageIcon = new ImageIcon(ImageUtil.addRoundedCorners(serverIconBufferedImage, 15));
          }
          serverIcon.setIcon(server.serverIcon.equalsIgnoreCase("null") ? defaultServerImageIcon : serverIconImageIcon);
        }
        serverIcon.setToolTipText(server.name);
        serverIconPane.add(serverIcon);

        if(server == LauncherApp.selectedServer) {
          serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 15; border:2,8,2,8," + borderColor + ",2");
          serverIcon.updateUI();
        } else {
          serverIcon.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
              if(!locked) {
                LauncherApp.selectedServer = server;
                selectedServerChanged();
                saveSelectedServer(server.getSanitizedName());
              }
            }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {
              serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 15; border:2,8,2,8," + borderColor + ",2");
              serverIcon.updateUI();
            }
            @Override public void mouseExited(MouseEvent e) {
              serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 0; border:0,0,0,0");
              serverIcon.updateUI();
            }
          });
        }

        LauncherGUI.serverSwitcherPane.add(serverIconPane);
        count++;
      }

      JPanel addServerPane = new JPanel();
      addServerPane.setLayout(null);
      addServerPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      addServerPane.setBounds(0, count * 50, 50, 50);

      JLabel addServerIcon = new JLabel();
      addServerIcon.setBounds(4, 4, 42, 42);
      addServerIcon.setHorizontalAlignment(SwingConstants.CENTER);
      addServerIcon.setToolTipText(Locale.getValue("m.add_server"));
      addServerIcon.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 18, Color.WHITE));
      addServerPane.add(addServerIcon);
      addServerIcon.addMouseListener(new MouseListener() {
        @Override public void mouseClicked(MouseEvent e) {
          Dialog.push(Locale.getValue("m.add_server_text"), Locale.getValue("m.add_server"), JOptionPane.INFORMATION_MESSAGE);
        }
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {
          addServerIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 15; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SERVERSWITCHER_HOVER_BORDER) + ",2");
          addServerIcon.updateUI();
        }
        @Override public void mouseExited(MouseEvent e) {
          addServerIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 0; border:0,0,0,0");
          addServerIcon.updateUI();
        }
      });

      LauncherGUI.serverSwitcherPane.add(addServerPane);

      LauncherGUI.serverSwitcherPane.setPreferredSize(new Dimension(50, count * 50));

      LauncherGUI.serverSwitcherPaneScrollBar.setBounds(
        LauncherGUI.serverSwitcherPaneScrollBar.getX(),
        LauncherGUI.serverSwitcherPaneScrollBar.getY(),
        LauncherGUI.serverSwitcherPaneScrollBar.getWidth(),
        550
      );

      LauncherGUI.serverSwitcherPane.setLayout(null);

      LauncherGUI.serverSwitcherPane.updateUI();
      LauncherGUI.serverSwitcherPaneScrollBar.updateUI();
    }
  }

  public static void updateBanner() {
    Thread refreshThread = new Thread(() -> {
      LauncherGUI.displayAnimBanner = false;

      if(!LauncherApp.flamingoOnline) {
        return;
      }

      String bannerUrl = LauncherApp.selectedServer.announceBanner.split("\\|")[0];
      double bannerIntensity = Double.parseDouble(LauncherApp.selectedServer.announceBanner.split("\\|")[1]);
      if(!bannerUrl.contains(".gif")) {
        LauncherGUI.banner = LauncherGUI.processImageForBanner(Cache.fetchImage(bannerUrl, 800, 550), bannerIntensity);
        LauncherGUI.playAnimatedBannersButton.setVisible(false);
      } else {
        LauncherGUI.processAnimatedImageForBanner(ImageUtil.getAnimatedImageFromURL(bannerUrl), bannerIntensity);
        LauncherGUI.playAnimatedBannersButton.setVisible(true);
      }
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

      if(LauncherApp.selectedServer.announceBannerEndsAt != 0L || LauncherApp.selectedServer.announceBannerStartsAt != 0L) {

        if(LauncherApp.selectedServer.announceBannerStartsAt > System.currentTimeMillis()) {
          // The event has not yet started
          LauncherGUI.bannerTimer.setText(Locale.getValue("m.banner_starts_at_remaining", DateUtil.getFormattedRemaining(LauncherApp.selectedServer.announceBannerStartsAt)));
        } else if(System.currentTimeMillis() > LauncherApp.selectedServer.announceBannerEndsAt) {
          // The event already ended
          LauncherGUI.bannerTimer.setText(Locale.getValue("m.banner_ends_at_ended"));
        } else {
          // The event is currently running
          LauncherGUI.bannerTimer.setText(Locale.getValue("m.banner_ends_at_remaining", DateUtil.getFormattedRemaining(LauncherApp.selectedServer.announceBannerEndsAt)));
        }

        // In any case, the timer needs to be visible
        LauncherGUI.bannerTimer.setVisible(true);
      } else {
        // Nothing to show here.
        LauncherGUI.bannerTimer.setVisible(false);
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

  public static void switchBannerAnimations() {
    Settings.playAnimatedBanners = !Settings.playAnimatedBanners;
    SettingsProperties.setValue("launcher.playAnimatedBanners", Boolean.toString(Settings.playAnimatedBanners));

    Icon playAnimatedBannersIconEnabled = IconFontSwing.buildIcon(FontAwesome.EYE, 18, Color.WHITE);
    Icon playAnimatedBannersIconDisabled = IconFontSwing.buildIcon(FontAwesome.EYE_SLASH, 18, Color.WHITE);
    LauncherGUI.playAnimatedBannersButton.setIcon(Settings.playAnimatedBanners ? playAnimatedBannersIconEnabled : playAnimatedBannersIconDisabled);
    LauncherGUI.playAnimatedBannersButton.setToolTipText(Locale.getValue(Settings.playAnimatedBanners ? "m.animated_banners_disable" : "m.animated_banners_enable"));
    LauncherGUI.playAnimatedBannersButton.setBackground(Settings.playAnimatedBanners ? CustomColors.INTERFACE_SIDEPANE_BUTTON : CustomColors.MID_RED);
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
      LauncherEventHandler.updateServerSwitcher(false);
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
        LauncherEventHandler.updateServerSwitcher(false);
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
