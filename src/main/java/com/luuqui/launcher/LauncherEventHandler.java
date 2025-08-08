package com.luuqui.launcher;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.download.DownloadManager;
import com.luuqui.download.data.URLDownloadQueue;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.ModManager;
import com.luuqui.launcher.setting.*;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

import static com.luuqui.launcher.Log.log;

public class LauncherEventHandler
{
  @Inject private LauncherGUI gui;

  protected LauncherContext _launcherCtx;
  protected ModManager _modManager;
  protected SettingsManager _settingsManager;
  protected LocaleManager _localeManager;
  protected FlamingoManager _flamingoManager;
  protected CacheManager _cacheManager;
  protected DownloadManager _downloadManager;
  protected DiscordPresenceClient _discordPresenceClient;

  public String currentWarning = "";
  public String latestRelease = "";
  public String latestChangelog = "";

  public boolean displayingAnimBanner = false;

  @Inject
  public LauncherEventHandler (LauncherContext launcherCtx,
                               ModManager modManager,
                               SettingsManager settingsManager,
                               LocaleManager localeManager,
                               FlamingoManager flamingoManager,
                               CacheManager cacheManager,
                               DownloadManager downloadManager,
                               DiscordPresenceClient discordPresenceClient)
  {
    this._launcherCtx = launcherCtx;
    this._modManager = modManager;
    this._settingsManager = settingsManager;
    this._localeManager = localeManager;
    this._flamingoManager = flamingoManager;
    this._cacheManager = cacheManager;
    this._downloadManager = downloadManager;
    this._discordPresenceClient = discordPresenceClient;
  }

  public void launchGameEvent (boolean altMode)
  {
    Thread launchThread = new Thread(() -> {

      // disable server switching and launch button during launch procedure
      this.updateServerSwitcher(true);
      this.gui.launchButton.setEnabled(false);

      Server selectedServer = _flamingoManager.getSelectedServer();
      String sanitizedServerName = selectedServer.getSanitizedName();

      if (selectedServer.isOfficial()) {
        // start: official servers launch procedure
        if (_modManager.getMountRequired()) {
          _modManager.mount();

          // re-lock server switching and launch button after mounting.
          this.updateServerSwitcher(true);
          this.gui.launchButton.setEnabled(false);
        }

        _launcherCtx.settingsGUI.eventHandler.saveAdditionalArgs();
        _launcherCtx.settingsGUI.eventHandler.saveConnectionSettings();
        _settingsManager.applyGameSettings();

        if (Settings.loadCodeMods) {
          ProcessUtil.run(getCodeModsStartCommand(altMode), true);
        } else {
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
        }

        if (Settings.useIngameRPC) ProcessUtil.run(RPC_COMMAND_LINE, true);
        // end: official servers launch procedure
      } else {
        // start: third party server launch procedure
        _launcherCtx._progressBar.startTask();
        _launcherCtx._progressBar.setBarMax(2);
        _launcherCtx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_data", selectedServer.name));
        _launcherCtx._progressBar.setBarValue(0);

        // we did not download this third party client yet, time to get it from the deployment url.
        if (!selectedServer.isInstalled()) {
          log.info("Downloading a third party client", "client", sanitizedServerName);

          _launcherCtx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_download", selectedServer.name));
          _launcherCtx._progressBar.setBarValue(1);

          File localFile = new File(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip");

          try {
            _downloadManager.add(new URLDownloadQueue(
                sanitizedServerName,
                new URL(selectedServer.deployUrl + "/" + selectedServer.version + ".zip"),
                localFile
            ));
          } catch (MalformedURLException e) {
            log.error(e);
          }
          _downloadManager.processQueues();

          ZipUtil.normalUnzip(localFile.getAbsolutePath(),
              LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName);

          FileUtil.deleteFile(localFile.getAbsolutePath());
        }

        // let's see if we need to update the third party client
        if (selectedServer.isOutdated()) {
          log.info("Updating third party client", "client", selectedServer.name);

          _launcherCtx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_update", selectedServer.name));
          _launcherCtx._progressBar.setBarValue(1);

          File localFile = new File(LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName + File.separator + "bundle.zip");

          try {
            _downloadManager.add(new URLDownloadQueue(
                sanitizedServerName + " Update",
                new URL(selectedServer.deployUrl + "/" + selectedServer.version + ".zip"),
                localFile
            ));
          } catch (MalformedURLException e) {
            log.error(e);
          }
          _downloadManager.processQueues();

          ZipUtil.normalUnzip(localFile.getAbsolutePath(),
              LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName);

          FileUtil.deleteFile(localFile.getAbsolutePath());

          // Delete the old base.zip bundle so we have an up-to-date vanilla state zip.
          if (FileUtil.fileExists(selectedServer.getRootDirectory() + "/rsrc/base.zip")) {
            FileUtil.deleteFile(selectedServer.getRootDirectory() + "/rsrc/base.zip");
            try {
              _launcherCtx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_bundle_regen", selectedServer.name));
              ZipUtil.zipFolderContents(new File(selectedServer.getRootDirectory() + "/rsrc"),
                  new File(selectedServer.getRootDirectory() + "/rsrc/base.zip"), "base.zip");
            } catch (Exception e) {
              log.error(e);
            }
          }
        }
        log.info("Third party client up to date", "client", selectedServer.name);

        // make sure there's a base zip file we can use to clean files with.
        String rootDir = selectedServer.getRootDirectory();
        if(FileUtil.fileExists(rootDir + "/rsrc")
          && !FileUtil.fileExists(rootDir + "/rsrc/base.zip")) {
          try {
            ZipUtil.zipFolderContents(new File(rootDir + "/rsrc"), new File(rootDir + "/rsrc/base.zip"), "base.zip");
          } catch (Exception e) {
            log.error(e);
          }
        }

        // we already have the client files,
        // the client is up to date, or the download has finished.
        // and so we start it up!
        _launcherCtx._progressBar.setBarValue(2);

        ProcessUtil.runFromDirectory(getThirdPartyClientStartCommand(selectedServer, altMode),
          LauncherGlobals.USER_DIR + File.separator + "thirdparty" + File.separator + sanitizedServerName,
          true);

        _launcherCtx._progressBar.finishTask();
      }

      log.info("Starting game", "server", selectedServer.name, "platform", Settings.gamePlatform, "codeMods", Settings.loadCodeMods);
      _launcherCtx._progressBar.setState(_localeManager.getValue("m.launching"));
      _launcherCtx.launcherGUI.launchButton.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
      _launcherCtx.launcherGUI.launchButton.setText(_localeManager.getValue("b.launching"));
      ThreadingUtil.executeWithDelay(this::checkGameLaunch, 8000);
    });
    launchThread.start();

  }

  public void launchGameAltEvent()
  {
    Thread launchAltThread = new Thread(() -> {

      if(_flamingoManager.getSelectedServer().isOfficial()) {
        // official servers alt launch procedure
        ProcessUtil.run(LauncherGlobals.ALT_CLIENT_ARGS, true);
        _discordPresenceClient.stop();
      } else {
        // third party alt launch procedure
      }

    });
    launchAltThread.start();

  }

  public void updateServerList (List<Server> servers)
  {
    List<Server> newServerList = _flamingoManager.getServerList();
    newServerList.clear();
    Server official = new Server("Official");
    newServerList.add(official);

    if (servers != null) {
      for (Server server : servers) {

        if (server.name.equalsIgnoreCase("Official")) {
          official.playerCountUrl = _localeManager.getValue("m.players_online_official", String.valueOf(LauncherApp.getOfficialApproxPlayerCount()));
          official.announceBanner = server.announceBanner;
          official.announceContent = server.announceContent;
          official.announceBannerLink = server.announceBannerLink;
          official.announceBannerStartsAt = server.announceBannerStartsAt;
          official.announceBannerEndsAt = server.announceBannerEndsAt;
          continue;
        }

        // Prevent from adding duplicate servers
        if (_flamingoManager.findServerByName(server.name) != null) {
          log.info("Tried to add duplicate server", "server", server.name);
          continue;
        }

        if (server.beta == 1) server.name += " (Beta)";

        newServerList.add(server);

        // make sure we have a proper folder structure for this server.
        String serverName = server.getSanitizedName();
        FileUtil.createDir(LauncherGlobals.USER_DIR + "/thirdparty/" + serverName);
        FileUtil.createDir(LauncherGlobals.USER_DIR + "/thirdparty/" + serverName + "/mods");

        // make sure there's a base zip file we can use to clean files with.
        String rootDir = server.getRootDirectory();
        if (FileUtil.fileExists(rootDir + "/rsrc")
            && !FileUtil.fileExists(rootDir + "/rsrc/base.zip")) {
          try {
            ZipUtil.zipFolderContents(new File(rootDir + "/rsrc"), new File(rootDir + "/rsrc/base.zip"), "base.zip");
          } catch (Exception e) {
            log.error(e);
          }
        }

        // check server specific settings keys.
        _launcherCtx.settingsGUI.eventHandler.checkServerSettingsKeys(serverName);
        _launcherCtx.modListGUI.eventHandler.checkServerSettingsKeys(serverName);
      }

      _flamingoManager.setServerList(newServerList);

      try {
        _flamingoManager.setSelectedServer(_flamingoManager.findServerBySanitizedName(Settings.selectedServerName));
      } catch (Exception e) {
        log.error(e);
        _flamingoManager.setSelectedServer(official);
      }
    } else {
      _flamingoManager.setSelectedServer(official);
    }

    selectedServerChanged();
  }

  public void saveSelectedServer ()
  {
    String serverName = _flamingoManager.getSelectedServer().getSanitizedName();
    if(serverName.isEmpty()) serverName = "official";
    _settingsManager.setValue("launcher.selectedServerName", serverName);
  }

  public void saveSelectedServer (String serverName)
  {
    if(serverName.isEmpty()) serverName = "official";
    _settingsManager.setValue("launcher.selectedServerName", serverName);
  }

  public void openAuctionsWebpage (ActionEvent action)
  {
    DesktopUtil.openWebpage("https://www.sk-ah.com");
  }

  public void displaySelectedServerInfo ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    String infoString = _localeManager.getValue(
      "m.server_info_text",
      new String[] {
        selectedServer.name,
        selectedServer.description,
        selectedServer.version,
        selectedServer.managedBy
      }
    );

    if(!selectedServer.siteUrl.equalsIgnoreCase("null"))
      infoString += _localeManager.getValue("m.server_info_text_siteurl", selectedServer.siteUrl);

    if(!selectedServer.communityUrl.equalsIgnoreCase("null"))
      infoString += _localeManager.getValue("m.server_info_text_communityurl", selectedServer.communityUrl);

    if(!selectedServer.sourceCodeUrl.equalsIgnoreCase("null"))
      infoString += _localeManager.getValue("m.server_info_text_sourcecode", selectedServer.sourceCodeUrl);

    infoString += _localeManager.getValue("m.server_info_text_disclaimer");

    Dialog.push(
      infoString,
      selectedServer.name + " " + _localeManager.getValue("t.server_info"),
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  public void selectedServerChanged ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    if (selectedServer != null) {
      if (selectedServer.isOfficial()) {
        gui.launchButton.setText(_localeManager.getValue("b.play"));
        gui.launchButton.setToolTipText(_localeManager.getValue("b.play"));
        gui.launchButton.setEnabled(selectedServer.enabled == 1);
        gui.selectedServerLabel.setText("Official");
        gui.playerCountLabel.setVisible(true);
        if (selectedServer.playerCountUrl != null) {
          gui.playerCountLabel.setText(selectedServer.playerCountUrl);
          gui.playerCountTooltipButton.setVisible(true);
        }
        gui.serverInfoButton.setEnabled(false);
        gui.serverInfoButton.setVisible(false);
        //gui.auctionButton.setVisible(true);
      } else {
        gui.launchButton.setEnabled(selectedServer.enabled == 1);
        if (!selectedServer.isInstalled()) {
          gui.launchButton.setText(_localeManager.getValue("b.install"));
          gui.launchButton.setToolTipText(_localeManager.getValue("b.install"));
        } else if (selectedServer.isOutdated()) {
          gui.launchButton.setText(_localeManager.getValue("b.update"));
          gui.launchButton.setToolTipText(_localeManager.getValue("b.update"));
        } else {
          gui.launchButton.setText(_localeManager.getValue("b.play"));
          gui.launchButton.setToolTipText(_localeManager.getValue("b.play"));
        }

        gui.selectedServerLabel.setText("");

        gui.serverInfoButton.setEnabled(true);
        gui.serverInfoButton.setVisible(true);
        gui.serverInfoButton.setText(selectedServer.name);

        // TODO: Fetch player count.
        gui.playerCountLabel.setText("Players online: Unavailable");
        gui.playerCountTooltipButton.setVisible(false);

        //gui.auctionButton.setVisible(false);
      }

      if (selectedServer.announceBanner != null) updateBanner();

      _launcherCtx.settingsGUI.eventHandler.selectedServerChanged();
      _launcherCtx.modListGUI.eventHandler.selectedServerChanged();
      _launcherCtx.editorsGUI.eventHandler.selectedServerChanged();

      saveSelectedServer();
      updateServerSwitcher(false);
    } else {
      // fallback to official in rare error scenario
      _flamingoManager.setSelectedServer(_flamingoManager.findServerByName("Official"));
      selectedServerChanged();
    }
  }

  public void updateServerSwitcher (boolean locked)
  {
    this.gui.serverSwitcherPane.removeAll();

    List<Server> serverList = _flamingoManager.getServerList();
    if (!serverList.isEmpty()) {
      int count = 0;
      String borderColor = ColorUtil.colorToHexString(locked ? CustomColors.INTERFACE_SERVERSWITCHER_HOVER_BORDER_LOCKED : CustomColors.INTERFACE_SERVERSWITCHER_HOVER_BORDER);

      BufferedImage officialServerBufferedImage = ImageUtil.loadImageWithinJar("/rsrc/img/server-official.png");
      officialServerBufferedImage = ImageUtil.resizeImagePreserveTransparency(officialServerBufferedImage, 32, 32);
      ImageIcon officialServerImageIcon = new ImageIcon(ImageUtil.addRoundedCorners(officialServerBufferedImage, 15));

      BufferedImage defaultServerBufferedImage = ImageUtil.loadImageWithinJar("/rsrc/img/icon-default.png");
      defaultServerBufferedImage = ImageUtil.resizeImagePreserveTransparency(defaultServerBufferedImage, 32, 32);
      ImageIcon defaultServerImageIcon = new ImageIcon(ImageUtil.addRoundedCorners(defaultServerBufferedImage, 15));

      for (Server server : serverList) {
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
            BufferedImage serverIconBufferedImage = _cacheManager.fetchImage(server.serverIcon, 32, 32);
            serverIconImageIcon = new ImageIcon(ImageUtil.addRoundedCorners(serverIconBufferedImage, 15));
          }
          serverIcon.setIcon(server.serverIcon.equalsIgnoreCase("null") ? defaultServerImageIcon : serverIconImageIcon);
        }
        serverIcon.setToolTipText(server.name);
        serverIconPane.add(serverIcon);

        if (server == _flamingoManager.getSelectedServer()) {
          serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 15; border:2,8,2,8," + borderColor + ",2; background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND));
          serverIcon.updateUI();
        } else {
          serverIcon.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {
              if (!locked) {
                _flamingoManager.setSelectedServer(server);
                selectedServerChanged();
                saveSelectedServer(server.getSanitizedName());
              }
            }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {
              if (!locked) {
                serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 15; border:2,8,2,8," + borderColor + ",2; background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND));
                serverIcon.updateUI();
              }
            }
            @Override public void mouseExited(MouseEvent e) {
              if (!locked) {
                serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 0; border:0,0,0,0");
                serverIcon.updateUI();
              }
            }
          });
        }

        this.gui.serverSwitcherPane.add(serverIconPane);
        count++;
      }

      JPanel addServerPane = new JPanel();
      addServerPane.setLayout(null);
      addServerPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      addServerPane.setBounds(0, count * 50, 50, 50);

      JLabel addServerIcon = new JLabel();
      addServerIcon.setBounds(4, 4, 42, 42);
      addServerIcon.setHorizontalAlignment(SwingConstants.CENTER);
      addServerIcon.setToolTipText(_localeManager.getValue("m.add_server"));
      addServerIcon.setIcon(IconFontSwing.buildIcon(FontAwesome.PLUS, 18, Color.WHITE));
      addServerIcon.setVisible(false);
      addServerPane.add(addServerIcon);
      addServerIcon.addMouseListener(new MouseListener() {
        @Override public void mouseClicked(MouseEvent e) {
          Dialog.push(_localeManager.getValue("m.add_server_text"), _localeManager.getValue("m.add_server"), JOptionPane.INFORMATION_MESSAGE);
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

      this.gui.serverSwitcherPane.add(addServerPane);

      this.gui.serverSwitcherPane.setPreferredSize(new Dimension(50, count * 50));

      this.gui.serverSwitcherPaneScrollBar.setBounds(
        this.gui.serverSwitcherPaneScrollBar.getX(),
        this.gui.serverSwitcherPaneScrollBar.getY(),
        this.gui.serverSwitcherPaneScrollBar.getWidth(),
        550
      );

      this.gui.serverSwitcherPane.setLayout(null);

      this.gui.serverSwitcherPane.updateUI();
      this.gui.serverSwitcherPaneScrollBar.updateUI();
    }
  }

  public void updateBanner ()
  {
    Thread refreshThread = new Thread(() -> {
      this.displayingAnimBanner = false;

      if(!_flamingoManager.getOnline()) {
        return;
      }

      Server selectedServer = _flamingoManager.getSelectedServer();
      String bannerUrl = selectedServer.announceBanner.split("\\|")[0];
      double bannerIntensity = Double.parseDouble(selectedServer.announceBanner.split("\\|")[1]);
      if(!bannerUrl.contains(".gif")) {
        this.gui.banner = this.gui.processImageForBanner(_cacheManager.fetchImage(bannerUrl, 800, 550), bannerIntensity);
        this.gui.playAnimatedBannersButton.setVisible(false);
      } else {
        this.gui.processAnimatedImageForBanner(ImageUtil.getAnimatedImageFromURL(bannerUrl), bannerIntensity);
        this.gui.playAnimatedBannersButton.setVisible(true);
      }

      this.gui.bannerLoading.setVisible(false);
      this.gui.mainPane.repaint();

      this.gui.bannerTitle.setText(selectedServer.announceContent.split("\\|")[0]);
      this.gui.bannerTitle.setVisible(true);

      String bannerSubtitle = selectedServer.announceContent.split("\\|")[1];

      if (bannerSubtitle.contains("\n")) {
        this.gui.bannerSubtitle1.setText(bannerSubtitle.split("\n")[0]);
        this.gui.bannerSubtitle2.setText(bannerSubtitle.split("\n")[1]);
        this.gui.bannerSubtitle2.setVisible(true);
      } else {
        this.gui.bannerSubtitle1.setText(bannerSubtitle);
        this.gui.bannerSubtitle2.setVisible(false);
      }
      this.gui.bannerSubtitle1.setVisible(true);

      if (!selectedServer.announceBannerLink.equalsIgnoreCase("null")) {

        ActionListener[] listeners = this.gui.bannerLinkButton.getActionListeners();
        for (ActionListener listener : listeners) {
          this.gui.bannerLinkButton.removeActionListener(listener);
        }

        this.gui.bannerLinkButton.addActionListener(l -> {
          DesktopUtil.openWebpage(selectedServer.announceBannerLink);
        });
        this.gui.bannerLinkButton.setVisible(true);
      } else {
        this.gui.bannerLinkButton.setVisible(false);
      }

      if (selectedServer.announceBannerEndsAt != 0L || selectedServer.announceBannerStartsAt != 0L) {

        if (selectedServer.announceBannerStartsAt > System.currentTimeMillis()) {
          // The event has not yet started
          this.gui.bannerTimer.setText(_localeManager.getValue("m.banner_starts_at_time_remaining", localizeTimeRemaining(DateUtil.getFormattedTimeRemaining(selectedServer.announceBannerStartsAt))));
          this.gui.bannerTimer.setToolTipText(_localeManager.getValue("m.banner_starts_at_time", DateUtil.getFormattedTime(selectedServer.announceBannerStartsAt, "PST")));
        } else if (System.currentTimeMillis() > selectedServer.announceBannerEndsAt) {
          // The event already ended
          this.gui.bannerTimer.setText(_localeManager.getValue("m.banner_ends_at_ended"));
        } else {
          // The event is currently running
          this.gui.bannerTimer.setText(_localeManager.getValue("m.banner_ends_at_time_remaining", localizeTimeRemaining(DateUtil.getFormattedTimeRemaining(selectedServer.announceBannerEndsAt))));
          this.gui.bannerTimer.setToolTipText(_localeManager.getValue("m.banner_ends_at_time", DateUtil.getFormattedTime(selectedServer.announceBannerEndsAt, "PST")));
        }

        // In any case, the timer needs to be visible
        this.gui.bannerTimer.setVisible(true);
      } else {
        // Nothing to show here.
        this.gui.bannerTimer.setVisible(false);
      }
    });
    refreshThread.start();
  }

  public void updateLauncher (String newVersion)
  {
    try {
      Files.copy(Paths.get(LauncherGlobals.USER_DIR + "/KnightLauncher.jar"), Paths.get(LauncherGlobals.USER_DIR + "/updater.jar"), StandardCopyOption.REPLACE_EXISTING);

      // Sleep the thread for a bit to be fully sure updater.jar isn't locked.
      Thread.sleep(1000);

      ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + "/updater.jar", "update", newVersion}, true);
      _launcherCtx.exit(true);
    } catch (Exception e) {
      String downloadErrMsg = "An error occurred while trying to start the launcher updater." +
          "\nPlease try again later.";
      downloadErrMsg += "\n\nError: " + e;
      for (StackTraceElement stElement : e.getStackTrace()) {
        downloadErrMsg += "\n" + stElement.toString();
      }

      List<File> files = new ArrayList<>();
      files.add(new File(LauncherGlobals.USER_DIR + File.separator + "knightlauncher.log"));
      files.add(new File(LauncherGlobals.USER_DIR + File.separator + "old-knightlauncher.log"));
      FileUtil.copyFilesToClipboard(files);
      downloadErrMsg += "\n\nRelevant log files were automatically copied to your clipboard.";

      Dialog.push(downloadErrMsg, JOptionPane.ERROR_MESSAGE);
      log.error(e);
    }
  }

  public void showLatestChangelog ()
  {
    Dialog.push(_localeManager.getValue(
        "m.changelog_text",
        new String[] {
          LauncherGlobals.LAUNCHER_VERSION,
          this.latestRelease, this.latestChangelog
        }), JOptionPane.INFORMATION_MESSAGE);
  }

  public void switchBannerAnimations ()
  {
    Settings.playAnimatedBanners = !Settings.playAnimatedBanners;
    _settingsManager.setValue("launcher.playAnimatedBanners", Boolean.toString(Settings.playAnimatedBanners));

    Icon playAnimatedBannersIconEnabled = IconFontSwing.buildIcon(FontAwesome.EYE, 18, Color.WHITE);
    Icon playAnimatedBannersIconDisabled = IconFontSwing.buildIcon(FontAwesome.EYE_SLASH, 18, Color.WHITE);
    this.gui.playAnimatedBannersButton.setIcon(Settings.playAnimatedBanners ? playAnimatedBannersIconEnabled : playAnimatedBannersIconDisabled);
    this.gui.playAnimatedBannersButton.setToolTipText(_localeManager.getValue(Settings.playAnimatedBanners ? "m.animated_banners_disable" : "m.animated_banners_enable"));
    this.gui.playAnimatedBannersButton.setBackground(Settings.playAnimatedBanners ? CustomColors.INTERFACE_BUTTON_BACKGROUND : CustomColors.LIGHT_RED);
  }

  private String[] getCodeModsStartCommand (boolean altMode)
  {
    List<String> argsList = new ArrayList<>();

    if(SystemUtil.isWindows()) {
      argsList.add(LauncherGlobals.USER_DIR + File.separator + "java_vm" + File.separator + "bin" + File.separator + "java");
      argsList.add("-classpath");
      argsList.add(LauncherGlobals.USER_DIR + File.separator + "./code/config.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/projectx-config.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/projectx-pcode.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/lwjgl.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/lwjgl_util.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/jinput.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/jutils.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/jshortcut.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/commons-beanutils.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/commons-digester.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "./code/commons-logging.jar;" +
          LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar;");
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

      argsList.add("-Djava.library.path=" + LauncherGlobals.USER_DIR + File.separator + "./native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + LauncherGlobals.USER_DIR + File.separator + ".");
      argsList.add("-Dresource_dir=" + LauncherGlobals.USER_DIR + File.separator + "./rsrc");
      argsList.add("com.luuqui.bootstrap.Bootstrap");
    } else {
      argsList.add(LauncherGlobals.USER_DIR + File.separator + "java" + File.separator + "bin" + File.separator + "java");
      argsList.add("-classpath");
      argsList.add(LauncherGlobals.USER_DIR + File.separator + "code/config.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/projectx-config.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/projectx-pcode.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/lwjgl.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/lwjgl_util.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/jinput.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/jshortcut.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/commons-beanutils.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/commons-digester.jar:" +
          LauncherGlobals.USER_DIR + File.separator + "code/commons-logging.jar:");
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

      argsList.add("-Djava.library.path=" + LauncherGlobals.USER_DIR + File.separator + "native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + LauncherGlobals.USER_DIR + File.separator);
      argsList.add("-Dresource_dir=" + LauncherGlobals.USER_DIR + File.separator + "rsrc");
      argsList.add("com.luuqui.bootstrap.Bootstrap");
    }

    return argsList.toArray(new String[argsList.size()]);
  }

  private String[] getThirdPartyClientStartCommand (Server server, boolean altMode)
  {
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

  private void checkGameLaunch ()
  {
    boolean gameRunning = isGameRunning();
    if (!gameRunning) {
      try {
        Thread.sleep(8000);
        gameRunning = isGameRunning();
        if (!gameRunning) {
          Dialog.push(_localeManager.getValue("error.game_launch"), _localeManager.getValue("t.game_launch_error"), JOptionPane.ERROR_MESSAGE);
        }
      } catch (InterruptedException e) {
        log.error(e);
      }
    }

    if (gameRunning) _launcherCtx.exit(false);

    // re-enable server switching and launching.
    this.updateServerSwitcher(false);
    this.gui.launchButton.setEnabled(true);

    _launcherCtx.launcherGUI.launchButton.setIcon(null);
    _launcherCtx.launcherGUI.launchButton.setText(_localeManager.getValue("b.play"));
  }

  private String localizeTimeRemaining (String remainingString)
  {
    if (remainingString.isEmpty()) return _localeManager.getValue("m.less_than_minute");
    return remainingString.replace("{d}", _localeManager.getValue("m.days"))
        .replace("{h}", _localeManager.getValue("m.hours"))
        .replace("{m}", _localeManager.getValue("m.minutes"));
  }

  private boolean isGameRunning ()
  {
    // TODO: Add Linux and Mac compatibility to launch checking.
    return !SystemUtil.isWindows() || ProcessUtil.isProcessRunning("java.exe", _flamingoManager.getSelectedServer().isOfficial() ? "Spiral Knights" : _flamingoManager.getSelectedServer().name);
  }

  private final String[] RPC_COMMAND_LINE = new String[] { ".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe", LauncherGlobals.LAUNCHER_VERSION };

}
