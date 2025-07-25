package com.luuqui.launcher;

import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.download.DownloadManager;
import com.luuqui.download.data.URLDownloadQueue;
import com.luuqui.util.*;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.luuqui.launcher.Log.log;

public class Updater extends BaseGUI
{
  @Inject protected LocaleManager _localeManager;
  @Inject protected DownloadManager _downloadManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;

  private int fetchAttempts = 0;

  public Updater ()
  {
    super(500, 125, true);
  }

  public void init ()
  {
    compose();
    startUpdate();
  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle("Updater");
    guiFrame.setResizable(false);
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    guiFrame.setUndecorated(true);
    guiFrame.setIconImage(ImageUtil.loadImageWithinJar("/rsrc/img/icon-128.png"));
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().setLayout(null);

    JLabel launcherLogo = new JLabel();
    BufferedImage launcherLogoImage = ImageUtil.loadImageWithinJar("/rsrc/img/icon-64.png");
    launcherLogo.setBounds(15, 48, 64, 64);
    launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
    guiFrame.add(launcherLogo);

    updaterState = new JLabel("");
    updaterState.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    updaterState.setHorizontalAlignment(SwingConstants.LEFT);
    updaterState.setBounds(100, 49, 375, 25);
    updaterState.setFont(Fonts.fontRegBig);
    updaterState.setVisible(true);
    guiFrame.add(updaterState);

    updaterProgressBar = new JProgressBar();
    updaterProgressBar.setBounds(100, 79, 375, 25);
    updaterProgressBar.setVisible(true);
    guiFrame.add(updaterProgressBar);

    closeButton.setVisible(false);
    minimizeButton.setVisible(false);

    guiFrame.setLocationRelativeTo(null);
    guiFrame.setVisible(true);
  }

  private void startUpdate ()
  {
    Thread updateThread = new Thread(this::update);
    updateThread.start();
  }

  private void update ()
  {
    updaterProgressBar.setMaximum(4);

    updaterProgressBar.setValue(0);
    updaterState.setText("Starting...");

    updaterProgressBar.setValue(1);
    updaterState.setText("Downloading latest version...");
    downloadLatestVersion();

    this.updaterProgressBar.setValue(2);
    this.updaterState.setText("Extracting latest version...");
    try {
      Compressor.unzip4j(LauncherGlobals.USER_DIR + "/KnightLauncher.zip", LauncherGlobals.USER_DIR + "/");
    } catch (ZipException e) {
      log.error(e);
    }

    updaterProgressBar.setValue(3);
    updaterState.setText("Cleaning leftover files...");
    new File(LauncherGlobals.USER_DIR + "/KnightLauncher.jar.old").delete();
    new File(LauncherGlobals.USER_DIR + "/KnightLauncher.zip").delete();

    updaterProgressBar.setValue(4);
    updaterState.setText("Update finished");
    this.finish(true);
  }

  private void downloadLatestVersion ()
  {
    boolean fetchedVersion = false;
    String rawResponseReleases = null;
    while (this.fetchAttempts < 3 && !fetchedVersion) {
      log.info("Fetching latest version", "attempts", this.fetchAttempts);
      rawResponseReleases = INetUtil.getWebpageContent(
          LauncherGlobals.GITHUB_API
              + "repos/"
              + LauncherGlobals.GITHUB_AUTHOR + "/"
              + LauncherGlobals.GITHUB_REPO + "/"
              + "releases/"
              + "latest"
      );
      if (rawResponseReleases != null) {
        fetchedVersion = true;
      }
      fetchAttempts++;
    }

    if (rawResponseReleases == null) {
      log.error("Couldn't fetch latest version, stopping launcher update", "attempts", this.fetchAttempts);
      String downloadErrMsg = "The updater failed to fetch the latest version after 3 attempts." +
          "\nBooting back into current version, try updating later.";
      Dialog.push(downloadErrMsg, JOptionPane.ERROR_MESSAGE);
      finish(false);
    }

    JSONObject jsonReleases = new JSONObject(rawResponseReleases);
    String latestRelease = jsonReleases.getString("tag_name");

    String downloadUrl = "https://github.com/"
        + LauncherGlobals.GITHUB_AUTHOR + "/"
        + LauncherGlobals.GITHUB_REPO + "/"
        + "releases/download/"
        + latestRelease + "/"
        + "KnightLauncher-" + latestRelease + ".zip";

    URLDownloadQueue downloadQueue = null;
    try {
      downloadQueue = new URLDownloadQueue(
          "Launcher update",
          new URL(downloadUrl),
          new File(LauncherGlobals.USER_DIR + "/KnightLauncher.zip")
      );
    } catch (MalformedURLException e) {
      log.error(e);
    }

    _downloadManager.add(downloadQueue);
    _downloadManager.processQueues();

    if (!_downloadManager.getQueueStatus(downloadQueue)) {
      log.error("Couldn't download latest version, stopping launcher update");
      String downloadErrMsg = "The update couldn't be initiated after 3 download attempts." +
          "\nBooting back into current version, try updating later.";
      Dialog.push(downloadErrMsg, JOptionPane.ERROR_MESSAGE);
      log.error(downloadErrMsg);
      finish(false);
    }
  }

  private void finish (boolean success)
  {
    _discordPresenceClient.stop();

    String[] startParams;
    if (success) {
      startParams = new String[] { "java", "-jar", LauncherGlobals.USER_DIR + "/KnightLauncher.jar" };
    } else {
      startParams = new String[] { "java", "-jar", LauncherGlobals.USER_DIR + "/KnightLauncher.jar", "updateFailed" };
    }
    ProcessUtil.run(startParams, true);

    this.guiFrame.dispose();
    System.exit(1);
  }

  private JProgressBar updaterProgressBar;
  private JLabel updaterState;

}

