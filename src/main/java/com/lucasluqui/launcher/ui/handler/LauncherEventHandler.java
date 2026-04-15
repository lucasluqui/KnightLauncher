package com.lucasluqui.launcher.ui.handler;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.launcher.*;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.mod.ModManager;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.launcher.ui.LauncherUI;
import com.lucasluqui.launcher.setting.ui.SettingsUI;
import com.lucasluqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.lucasluqui.launcher.ui.Log.log;

public class LauncherEventHandler
{
  @Inject
  public LauncherEventHandler (LauncherContext ctx,
                               ModManager modManager,
                               SettingsManager settingsManager,
                               LocaleManager localeManager,
                               FlamingoManager flamingoManager,
                               CacheManager cacheManager,
                               DownloadManager downloadManager,
                               DiscordPresenceClient discordPresenceClient)
  {
    this._ctx = ctx;
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

      // block interactions with the launcher
      _ctx.getApp().toggleElementsBlock(true);

      Server selectedServer = _flamingoManager.getSelectedServer();
      String sanitizedServerName = selectedServer.getSanitizedName();

      // check if any game update took place since last launch.
      if (_modManager.gameVersionChanged()) {
        _modManager.checkInstalled();
        _modManager.mount();

        // re-lock server switching and launch button after mounting.
        this.updateServerSwitcher(true);
        this.ui.launchButton.setEnabled(false);
      }

      if (selectedServer.isOfficial()) {
        // start: official servers launch procedure
        if (_modManager.getMountRequired()) {
          _modManager.mount();

          // re-lock server switching and launch button after mounting.
          this.updateServerSwitcher(true);
          this.ui.launchButton.setEnabled(false);
        }

        _ctx.getApp().getUI(SettingsUI.class).eventHandler.saveAdditionalArgs();
        _ctx.getApp().getUI(SettingsUI.class).eventHandler.saveConnectionSettings();
        _settingsManager.applyGameSettings();

        // Remove any arguments in _JAVA_OPTIONS.
        JavaUtil.clearJavaOptions();

        if (Settings.loadCodeMods) {
          ProcessUtil.run(getCodeModsStartCommand(altMode), true);
        } else {
          if (Settings.gamePlatform.startsWith("Steam")) {
            SteamUtil.runApp(99900, SystemUtil.isMac());
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
        _ctx._progressBar.startTask();
        _ctx._progressBar.setBarMax(2);
        _ctx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_data", selectedServer.name));
        _ctx._progressBar.setBarValue(0);

        // we did not download this third party client yet, time to get it from the deployment url.
        if (!selectedServer.isInstalled()) {
          log.info("Downloading a third party client", "client", sanitizedServerName);

          _ctx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_download", selectedServer.name));
          _ctx._progressBar.setBarValue(1);

          File localFile = new File(_flamingoManager.getThirdPartyBaseDir() + sanitizedServerName + File.separator + "bundle.zip");

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

          ZipUtil.unzip(localFile.getAbsolutePath(),
            _flamingoManager.getThirdPartyBaseDir() + sanitizedServerName);

          FileUtil.deleteFile(localFile.getAbsolutePath());
        }

        // let's see if we need to update the third party client
        if (selectedServer.isOutdated()) {
          log.info("Updating third party client", "client", selectedServer.name);

          _ctx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_update", selectedServer.name));
          _ctx._progressBar.setBarValue(1);

          File localFile = new File(_flamingoManager.getThirdPartyBaseDir() + sanitizedServerName + File.separator + "bundle.zip");

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

          ZipUtil.unzip(localFile.getAbsolutePath(),
            _flamingoManager.getThirdPartyBaseDir() + sanitizedServerName);

          FileUtil.deleteFile(localFile.getAbsolutePath());

          // Delete the old base.zip bundle so we have an up-to-date vanilla state zip.
          if (FileUtil.fileExists(selectedServer.getRootDirectory() + "/rsrc/base.zip")) {
            FileUtil.deleteFile(selectedServer.getRootDirectory() + "/rsrc/base.zip");
            try {
              _ctx._progressBar.setState(_localeManager.getValue("m.launch_thirdparty_bundle_regen", selectedServer.name));
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
        if (FileUtil.fileExists(rootDir + "/rsrc")
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
        _ctx._progressBar.setBarValue(2);

        ProcessUtil.runFromDirectory(getThirdPartyClientStartCommand(selectedServer, altMode),
          _flamingoManager.getThirdPartyBaseDir() + sanitizedServerName,
          true);

        _ctx._progressBar.finishTask();
      }

      log.info("Starting game", "server", selectedServer.name, "platform", Settings.gamePlatform, "codeMods", Settings.loadCodeMods);
      _ctx._progressBar.setState(_localeManager.getValue("m.game_launching"));
      ui.launchButton.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
      ui.launchButton.setText(_localeManager.getValue("m.launching"));
      ThreadingUtil.executeWithDelay(this::checkGameLaunch, 8000);
    });
    launchThread.start();

  }

  public void launchGameAltEvent ()
  {
    Thread launchAltThread = new Thread(() -> {

      if (_flamingoManager.getSelectedServer().isOfficial()) {
        // official servers alt launch procedure
        ProcessUtil.run(LauncherGlobals.ALT_CLIENT_ARGS, true);
        _discordPresenceClient.stop();
      } else {
        // third party alt launch procedure
      }

    });
    launchAltThread.start();

  }

  public void repairGameFilesEvent ()
  {
    Thread repairThread = new Thread(() -> {
      _modManager.setMountRequired(true);
      _modManager.startStrictFileRebuild();
      _modManager.mount();
    });
    repairThread.start();
  }

  public void gameSettingsEvent ()
  {
    _ctx.getApp().getUI(SettingsUI.class).tabbedPane.setSelectedIndex(1);
    _ctx.getApp().showUI(SettingsUI.class);
  }

  public void openGameFolderEvent ()
  {
    _ctx.getApp().getUI(SettingsUI.class).eventHandler.openRootFolderEvent(null);
  }

  public void displaySelectedServerInfo ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    String infoString = _localeManager.getValue(
      "m.server_info_text",
      new String[]{
        selectedServer.name,
        selectedServer.description,
        selectedServer.version,
        selectedServer.managedBy
      }
    );

    if (!selectedServer.siteUrl.equalsIgnoreCase("null"))
      infoString += _localeManager.getValue("m.server_info_text_siteurl", selectedServer.siteUrl);

    if (!selectedServer.communityUrl.equalsIgnoreCase("null"))
      infoString += _localeManager.getValue("m.server_info_text_communityurl", selectedServer.communityUrl);

    if (!selectedServer.sourceCodeUrl.equalsIgnoreCase("null"))
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
        ui.launchButton.setEnabled(selectedServer.enabled == 1);
        ui.selectedServerLabel.setText("Official");
        ui.playerCountLabel.setVisible(true);
        if (selectedServer.playerCountUrl != null) {
          ui.playerCountLabel.setText(selectedServer.playerCountUrl);
          ui.playerCountLabel.setVisible(true);
          ui.playerCountLabel.setIcon(null);
          ui.playerCountTooltipButton.setVisible(true);
        }
        ui.serverInfoButton.setEnabled(false);
        ui.serverInfoButton.setVisible(false);
      } else {
        ui.launchButton.setEnabled(selectedServer.enabled == 1);

        ui.selectedServerLabel.setText("");

        ui.serverInfoButton.setEnabled(true);
        ui.serverInfoButton.setVisible(true);
        ui.serverInfoButton.setText(selectedServer.name);

        // TODO: Fetch player count.
        ui.playerCountLabel.setText("??? ");
        ui.playerCountLabel.setIcon(null);
        ui.playerCountTooltipButton.setVisible(false);
      }

      if (selectedServer.announceBanner != null) updateBanner();

      for (ActionListener al : ui.serverNoticeButton.getActionListeners()) {
        ui.serverNoticeButton.removeActionListener(al);
      }

      if (selectedServer.notice != null && !selectedServer.notice.equalsIgnoreCase("null")) {
        String noticeTitle =
            selectedServer.noticeTitle.equalsIgnoreCase("null") ?
              _localeManager.getValue("b.server_notice") : selectedServer.noticeTitle;

        ui.serverNoticeButton.setText(noticeTitle);
        ui.serverNoticeButton.setToolTipText(noticeTitle);

        ui.serverNoticeButton.addActionListener(
          action -> Dialog.push(selectedServer.notice, noticeTitle, JOptionPane.WARNING_MESSAGE)
        );

        ui.serverNoticeButton.setVisible(true);
      } else {
        ui.serverNoticeButton.setVisible(false);
      }

      ui.resetLaunchButton();

      updateServerSwitcher(false);
    } else {
      // fallback to official in rare error scenario
      _flamingoManager.setSelectedServer(_flamingoManager.findServerByName("Official"));
    }
  }

  public void updateServerSwitcher (boolean locked)
  {
    this.ui.serverSwitcherPane.removeAll();

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
        if (server.isOfficial()) {
          serverIcon.setIcon(officialServerImageIcon);
        } else {
          ImageIcon serverIconImageIcon;
          if (server.serverIcon.equalsIgnoreCase("null")) {
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
          serverIcon.addMouseListener(new MouseListener()
          {
            @Override
            public void mouseClicked (MouseEvent e)
            {
              if (!locked) {
                _flamingoManager.setSelectedServer(server);
              }
            }

            @Override
            public void mousePressed (MouseEvent e)
            {
            }

            @Override
            public void mouseReleased (MouseEvent e)
            {
            }

            @Override
            public void mouseEntered (MouseEvent e)
            {
              if (!locked) {
                serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 15; border:2,8,2,8," + borderColor + ",2; background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND));
                serverIcon.updateUI();
              }
            }

            @Override
            public void mouseExited (MouseEvent e)
            {
              if (!locked) {
                serverIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 0; border:0,0,0,0");
                serverIcon.updateUI();
              }
            }
          });
        }

        this.ui.serverSwitcherPane.add(serverIconPane);
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
      addServerIcon.addMouseListener(new MouseListener()
      {
        @Override
        public void mouseClicked (MouseEvent e)
        {
          Dialog.push(_localeManager.getValue("m.add_server_text"), _localeManager.getValue("m.add_server"), JOptionPane.INFORMATION_MESSAGE);
        }

        @Override
        public void mousePressed (MouseEvent e)
        {
        }

        @Override
        public void mouseReleased (MouseEvent e)
        {
        }

        @Override
        public void mouseEntered (MouseEvent e)
        {
          addServerIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 15; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SERVERSWITCHER_HOVER_BORDER) + ",2");
          addServerIcon.updateUI();
        }

        @Override
        public void mouseExited (MouseEvent e)
        {
          addServerIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 0; border:0,0,0,0");
          addServerIcon.updateUI();
        }
      });

      this.ui.serverSwitcherPane.add(addServerPane);

      this.ui.serverSwitcherPane.setPreferredSize(new Dimension(50, count * 50));

      this.ui.serverSwitcherPaneScrollBar.setBounds(
        this.ui.serverSwitcherPaneScrollBar.getX(),
        this.ui.serverSwitcherPaneScrollBar.getY(),
        this.ui.serverSwitcherPaneScrollBar.getWidth(),
        550
      );

      this.ui.serverSwitcherPane.setLayout(null);

      this.ui.serverSwitcherPane.updateUI();
      this.ui.serverSwitcherPaneScrollBar.updateUI();
    }
  }

  public void updateBanner ()
  {
    Thread refreshThread = new Thread(() -> {
      this.displayingAnimBanner = false;

      if (!_flamingoManager.isOnline()) {
        return;
      }

      Server selectedServer = _flamingoManager.getSelectedServer();
      String bannerUrl = selectedServer.announceBanner.split("\\|")[0];
      double bannerIntensity = Double.parseDouble(selectedServer.announceBanner.split("\\|")[1]);
      if (!bannerUrl.contains(".gif")) {
        this.ui.banner = this.ui.processImageForBanner(_cacheManager.fetchImage(bannerUrl, 800, 550), bannerIntensity);
        this.ui.playAnimatedBannersButton.setVisible(false);
      } else {
        this.ui.processAnimatedImageForBanner(ImageUtil.getAnimatedImageFromURL(bannerUrl), bannerIntensity);
        this.ui.playAnimatedBannersButton.setVisible(true);
      }

      this.ui.bannerLoading.setVisible(false);
      this.ui.panel.repaint();

      this.ui.bannerTitle.setText(selectedServer.announceContent.split("\\|")[0]);
      this.ui.bannerTitle.setVisible(true);

      String bannerSubtitle = selectedServer.announceContent.split("\\|")[1];

      if (bannerSubtitle.contains("\n")) {
        this.ui.bannerSubtitle1.setText(bannerSubtitle.split("\n")[0]);
        this.ui.bannerSubtitle2.setText(bannerSubtitle.split("\n")[1]);
        this.ui.bannerSubtitle2.setVisible(true);
      } else {
        this.ui.bannerSubtitle1.setText(bannerSubtitle);
        this.ui.bannerSubtitle2.setVisible(false);
      }
      this.ui.bannerSubtitle1.setVisible(true);

      if (!selectedServer.announceBannerLink.equalsIgnoreCase("null")) {

        ActionListener[] listeners = this.ui.bannerLinkButton.getActionListeners();
        for (ActionListener listener : listeners) {
          this.ui.bannerLinkButton.removeActionListener(listener);
        }

        this.ui.bannerLinkButton.addActionListener(l -> {
          DesktopUtil.openWebpage(selectedServer.announceBannerLink);
        });
        this.ui.bannerLinkButton.setVisible(true);
      } else {
        this.ui.bannerLinkButton.setVisible(false);
      }

      if (selectedServer.announceBannerEndsAt != 0L || selectedServer.announceBannerStartsAt != 0L) {

        if (selectedServer.announceBannerStartsAt > System.currentTimeMillis()) {
          // The event has not yet started
          this.ui.bannerTimer.setText(_localeManager.getValue("m.banner_starts_at_time_remaining", localizeTimeRemaining(DateUtil.getFormattedTimeRemaining(selectedServer.announceBannerStartsAt))));
          this.ui.bannerTimer.setToolTipText(_localeManager.getValue("m.banner_starts_at_time", DateUtil.getFormattedTime(selectedServer.announceBannerStartsAt, "PST")));
        } else if (System.currentTimeMillis() > selectedServer.announceBannerEndsAt) {
          // The event already ended
          this.ui.bannerTimer.setText(_localeManager.getValue("m.banner_ends_at_ended"));
        } else {
          // The event is currently running
          this.ui.bannerTimer.setText(_localeManager.getValue("m.banner_ends_at_time_remaining", localizeTimeRemaining(DateUtil.getFormattedTimeRemaining(selectedServer.announceBannerEndsAt))));
          this.ui.bannerTimer.setToolTipText(_localeManager.getValue("m.banner_ends_at_time", DateUtil.getFormattedTime(selectedServer.announceBannerEndsAt, "PST")));
        }

        // In any case, the timer needs to be visible
        this.ui.bannerTimer.setVisible(true);
      } else {
        // Nothing to show here.
        this.ui.bannerTimer.setVisible(false);
      }
    });
    refreshThread.start();
  }

  public void switchBannerAnimations ()
  {
    Settings.playAnimatedBanners = !Settings.playAnimatedBanners;
    _settingsManager.setValue("launcher.playAnimatedBanners", Boolean.toString(Settings.playAnimatedBanners));

    Icon playAnimatedBannersIconEnabled = IconFontSwing.buildIcon(FontAwesome.EYE, 18, Color.WHITE);
    Icon playAnimatedBannersIconDisabled = IconFontSwing.buildIcon(FontAwesome.EYE_SLASH, 18, Color.WHITE);
    this.ui.playAnimatedBannersButton.setIcon(Settings.playAnimatedBanners ? playAnimatedBannersIconEnabled : playAnimatedBannersIconDisabled);
    this.ui.playAnimatedBannersButton.setToolTipText(_localeManager.getValue(Settings.playAnimatedBanners ? "m.animated_banners_disable" : "m.animated_banners_enable"));
    this.ui.playAnimatedBannersButton.setBackground(Settings.playAnimatedBanners ? CustomColors.INTERFACE_BUTTON_BACKGROUND : CustomColors.LIGHT_RED);
  }

  private String[] getCodeModsStartCommand (boolean altMode)
  {
    List<String> argsList = new ArrayList<>();

    if (SystemUtil.isWindows()) {
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
        LauncherGlobals.USER_DIR + File.separator + "./code/discord-game-sdk4j.jar;" +
        LauncherGlobals.USER_DIR + File.separator + "./code/gson.jar;" +
        LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar;");
      argsList.add("-Dcom.threerings.getdown=false");
      if (Settings.gameDisableExplicitGC) argsList.add("-XX:+DisableExplicitGC");
      if (Settings.gameUseCustomGC) argsList.add("-XX:+Use" + Settings.gameGarbageCollector + "GC");
      argsList.add(altMode ? "-Xms512M" : "-Xms" + Settings.gameMemory + "M");
      argsList.add(altMode ? "-Xmx512M" : "-Xmx" + Settings.gameMemory + "M");
      argsList.add("-XX:SoftRefLRUPolicyMSPerMB=10");

      if (!Settings.gameAdditionalArgs.isEmpty()) {
        argsList.addAll(Arrays.asList(Settings.gameAdditionalArgs.trim().split("\n")));
      }

      argsList.add("-Djava.library.path=" + LauncherGlobals.USER_DIR + File.separator + "./native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + LauncherGlobals.USER_DIR + File.separator + ".");
      argsList.add("-Dresource_dir=" + LauncherGlobals.USER_DIR + File.separator + "./rsrc");
      argsList.add("com.lucasluqui.bootstrap.Bootstrap");
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
        LauncherGlobals.USER_DIR + File.separator + "code/commons-logging.jar:" +
        LauncherGlobals.USER_DIR + File.separator + "./code/discord-game-sdk4j.jar;" +
        LauncherGlobals.USER_DIR + File.separator + "./code/gson.jar;");
      argsList.add("-Dcom.threerings.getdown=false");
      if (Settings.gameDisableExplicitGC) argsList.add("-XX:+DisableExplicitGC");
      if (Settings.gameUseCustomGC) argsList.add("-XX:+Use" + Settings.gameGarbageCollector + "GC");
      argsList.add(altMode ? "-Xms512M" : "-Xms" + Settings.gameMemory + "M");
      argsList.add(altMode ? "-Xmx512M" : "-Xmx" + Settings.gameMemory + "M");
      argsList.add("-XX:SoftRefLRUPolicyMSPerMB=10");

      if (!Settings.gameAdditionalArgs.isEmpty()) {
        argsList.addAll(Arrays.asList(Settings.gameAdditionalArgs.trim().split("\n")));
      }

      argsList.add("-Djava.library.path=" + LauncherGlobals.USER_DIR + File.separator + "native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + LauncherGlobals.USER_DIR + File.separator);
      argsList.add("-Dresource_dir=" + LauncherGlobals.USER_DIR + File.separator + "rsrc");
      argsList.add("com.lucasluqui.bootstrap.Bootstrap");
    }

    return argsList.toArray(new String[argsList.size()]);
  }

  private String[] getThirdPartyClientStartCommand (Server server, boolean altMode)
  {
    List<String> argsList = new ArrayList<>();
    String sanitizedServerName = server.getSanitizedName();
    String thirdPartyBaseDir = _flamingoManager.getThirdPartyBaseDir();

    if (SystemUtil.isWindows()) {
      argsList.add(thirdPartyBaseDir + sanitizedServerName + File.separator + "java_vm" + File.separator + "bin" + File.separator + "java");
      argsList.add("-classpath");
      argsList.add(
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/config.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/projectx-config.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/projectx-pcode.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/lwjgl.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/lwjgl_util.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/jinput.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/jshortcut.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/commons-beanutils.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/commons-digester.jar;" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "./code/commons-logging.jar;"
      );
      argsList.add("-Dcom.threerings.getdown=false");
      if (Settings.gameDisableExplicitGC) argsList.add("-XX:+DisableExplicitGC");
      if (Settings.gameUseCustomGC) {
        if (Settings.gameGarbageCollector.equalsIgnoreCase("ZGC")) {
          // do nothing.
        } else {
          if (Settings.gameGarbageCollector.equalsIgnoreCase("Parallel")) {
            argsList.add("-XX:+UseParallelOldGC");
          }
          argsList.add("-XX:+Use" + Settings.gameGarbageCollector + "GC");
        }
      }
      argsList.add(altMode ? "-Xms512M" : "-Xms" + Settings.gameMemory + "M");
      argsList.add(altMode ? "-Xmx512M" : "-Xmx" + Settings.gameMemory + "M");
      argsList.add("-XX:+AggressiveOpts");
      argsList.add("-XX:SoftRefLRUPolicyMSPerMB=10");

      if (!Settings.gameAdditionalArgs.isEmpty()) {
        argsList.addAll(Arrays.asList(Settings.gameAdditionalArgs.trim().split("\n")));
      }

      argsList.add("-Djava.library.path=" + thirdPartyBaseDir + sanitizedServerName + File.separator + "./native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + thirdPartyBaseDir + sanitizedServerName + File.separator + ".");
      argsList.add("-Dresource_dir=" + thirdPartyBaseDir + sanitizedServerName + File.separator + "./rsrc");
      argsList.add("com.threerings.projectx.client.ProjectXApp");
    } else {
      argsList.add(thirdPartyBaseDir + sanitizedServerName + File.separator + "java" + File.separator + "bin" + File.separator + "java");
      argsList.add("-classpath");
      argsList.add(
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/config.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/projectx-config.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/projectx-pcode.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/lwjgl.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/lwjgl_util.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/jinput.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/jshortcut.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/commons-beanutils.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/commons-digester.jar:" +
        thirdPartyBaseDir + sanitizedServerName + File.separator + "code/commons-logging.jar:"
      );
      argsList.add("-Dcom.threerings.getdown=false");
      if (Settings.gameDisableExplicitGC) argsList.add("-XX:+DisableExplicitGC");
      if (Settings.gameUseCustomGC) {
        if (Settings.gameGarbageCollector.equalsIgnoreCase("ZGC")) {
          // do nothing.
        } else {
          if (Settings.gameGarbageCollector.equalsIgnoreCase("Parallel")) {
            argsList.add("-XX:+UseParallelOldGC");
          }
          argsList.add("-XX:+Use" + Settings.gameGarbageCollector + "GC");
        }
      }
      argsList.add(altMode ? "-Xms512M" : "-Xms" + Settings.gameMemory + "M");
      argsList.add(altMode ? "-Xmx512M" : "-Xmx" + Settings.gameMemory + "M");
      argsList.add("-XX:+AggressiveOpts");
      argsList.add("-XX:SoftRefLRUPolicyMSPerMB=10");

      if (!Settings.gameAdditionalArgs.isEmpty()) {
        argsList.addAll(Arrays.asList(Settings.gameAdditionalArgs.trim().split("\n")));
      }

      argsList.add("-Djava.library.path=" + thirdPartyBaseDir + sanitizedServerName + File.separator + "native");
      argsList.add("-Dorg.lwjgl.util.NoChecks=true");
      argsList.add("-Dsun.java2d.d3d=false");
      argsList.add("-Dappdir=" + thirdPartyBaseDir + sanitizedServerName + File.separator);
      argsList.add("-Dresource_dir=" + thirdPartyBaseDir + sanitizedServerName + File.separator + "rsrc");
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

    if (gameRunning) _ctx.getApp().exit(false);

    // unblock launcher interactions
    _ctx.getApp().toggleElementsBlock(false);

    ui.resetLaunchButton();
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
    return !SystemUtil.isWindows() || ProcessUtil.isProcessRunning("javaw.exe", _flamingoManager.getSelectedServer().isOfficial() ? "Spiral Knights" : _flamingoManager.getSelectedServer().name);
  }

  @Inject private LauncherUI ui;

  protected LauncherContext _ctx;
  protected ModManager _modManager;
  protected SettingsManager _settingsManager;
  protected LocaleManager _localeManager;
  protected FlamingoManager _flamingoManager;
  protected CacheManager _cacheManager;
  protected DownloadManager _downloadManager;
  protected DiscordPresenceClient _discordPresenceClient;

  public String currentWarning = "";
  public String latestRelease = "";

  public boolean displayingAnimBanner = false;

  private final String[] RPC_COMMAND_LINE = new String[]{".\\KnightLauncher\\modules\\skdiscordrpc\\SK-DiscordRPC.exe", BuildConfig.getVersion()};
}
