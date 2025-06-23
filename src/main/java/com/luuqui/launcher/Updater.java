package com.luuqui.launcher;

import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.luuqui.launcher.Log.log;

public class Updater extends BaseGUI
{
  @Inject protected LocaleManager _localeManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;

  private int downloadAttempts = 0;

  public Updater ()
  {
    super(500, 125, false);
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
    guiFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-128.png"));
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().setLayout(null);

    JLabel launcherLogo = new JLabel();
    BufferedImage launcherLogoImage = ImageUtil.loadImageWithinJar("/img/icon-64.png");
    launcherLogo.setBounds(15, 48, 64, 64);
    launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
    guiFrame.add(launcherLogo);

    updaterState = new JLabel("");
    updaterState.setHorizontalAlignment(SwingConstants.LEFT);
    updaterState.setBounds(100, 49, 375, 25);
    updaterState.setFont(Fonts.fontRegBig);
    updaterState.setVisible(true);
    guiFrame.add(updaterState);

    updaterProgressBar = new JProgressBar();
    updaterProgressBar.setBounds(100, 79, 375, 25);
    updaterProgressBar.setVisible(true);
    guiFrame.add(updaterProgressBar);

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
    updaterProgressBar.setMaximum(5);

    updaterProgressBar.setValue(1);
    updaterState.setText("Removing current version...");
    //FileUtil.rename(new File(LauncherGlobals.USER_DIR + "/KnightLauncher.jar"), new File(LauncherGlobals.USER_DIR + "/KnightLauncher.jar.old"));

    updaterProgressBar.setValue(2);
    updaterState.setText("Downloading latest version...");
    downloadLatestVersion();

    if(this.downloadAttempts > 3) {
      String downloadErrMsg = "The updater couldn't be initiated after 3 download attempts." +
              "\nBooting back into current version, try updating later.";
      Dialog.push(downloadErrMsg, JOptionPane.ERROR_MESSAGE);
      log.error(downloadErrMsg);
      //FileUtil.rename(new File(LauncherGlobals.USER_DIR + "/KnightLauncher.jar.old"), new File(LauncherGlobals.USER_DIR + "/KnightLauncher.jar"));
      finish();
    }

    updaterProgressBar.setValue(4);
    updaterState.setText("Cleaning leftover files...");
    new File(LauncherGlobals.USER_DIR + "/KnightLauncher.jar.old").delete();
    new File(LauncherGlobals.USER_DIR + "/KnightLauncher.zip").delete();

    updaterProgressBar.setValue(5);
    updaterState.setText("Update finished.");
    this.finish();
  }

  private void downloadLatestVersion ()
  {
    boolean downloadCompleted = false;
    while(this.downloadAttempts <= 3 && !downloadCompleted) {
      this.downloadAttempts++;

      log.info("Fetching latest version", "attempts", this.downloadAttempts);

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

        String downloadUrl = "https://github.com/"
          + LauncherGlobals.GITHUB_AUTHOR + "/"
          + LauncherGlobals.GITHUB_REPO + "/"
          + "releases/download/"
          + latestRelease + "/"
          + "KnightLauncher-" + latestRelease + ".zip";

        log.info("Downloading latest version", "url", downloadUrl, "attempts", this.downloadAttempts);

        try {
          FileUtils.copyURLToFile(
            new URL(downloadUrl),
            new File(LauncherGlobals.USER_DIR + "/KnightLauncher.zip"),
            0,
            0
          );

          this.updaterProgressBar.setValue(3);
          this.updaterState.setText("Extracting latest version...");
          try {
            Compressor.unzip4j(LauncherGlobals.USER_DIR + "/KnightLauncher.zip", LauncherGlobals.USER_DIR + "/");
            downloadCompleted = true;
          } catch (ZipException e) {
            log.error(e);
          }
        } catch (IOException e) {
          // Keep retrying.
          log.error(e);
        }
      }
    }
  }

  private void finish ()
  {
    _discordPresenceClient.stop();
    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + "/KnightLauncher.jar" }, true);
    this.guiFrame.dispose();
    System.exit(1);
  }

  private JProgressBar updaterProgressBar;
  private JLabel updaterState;

}

