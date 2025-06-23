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
    super();
  }

  public void init ()
  {
    compose();
    startUpdate();
  }

  public void switchVisibility ()
  {
    this.updaterFrame.setVisible(!this.updaterFrame.isVisible());
  }

  private void compose ()
  {
    updaterFrame = new JFrame();
    updaterFrame.setVisible(false);
    updaterFrame.setTitle("Updater");
    updaterFrame.setResizable(false);
    updaterFrame.setBounds(100, 100, 500, 125);
    updaterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    updaterFrame.setUndecorated(true);
    updaterFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-128.png"));
    updaterFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    updaterFrame.getContentPane().setLayout(null);

    JLabel launcherLogo = new JLabel();
    BufferedImage launcherLogoImage = ImageUtil.loadImageWithinJar("/img/icon-64.png");
    launcherLogo.setBounds(15, 48, 64, 64);
    launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
    updaterFrame.add(launcherLogo);

    updaterState = new JLabel("");
    updaterState.setHorizontalAlignment(SwingConstants.LEFT);
    updaterState.setBounds(100, 49, 375, 25);
    updaterState.setFont(Fonts.fontRegBig);
    updaterState.setVisible(true);
    updaterFrame.add(updaterState);

    updaterProgressBar = new JProgressBar();
    updaterProgressBar.setBounds(100, 79, 375, 25);
    updaterProgressBar.setVisible(true);
    updaterFrame.add(updaterProgressBar);

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, updaterFrame.getWidth(), 35);
    titleBar.setBackground(ColorUtil.getTitleBarColor());
    updaterFrame.getContentPane().add(titleBar);

    /*
     * Based on Paul Samsotha's reply @ StackOverflow
     * link: https://stackoverflow.com/questions/24476496/drag-and-resize-undecorated-jframe
     */
    titleBar.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me) {

        pX = me.getX();
        pY = me.getY();
      }
    });
    titleBar.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent me) {

        pX = me.getX();
        pY = me.getY();
      }

      @Override
      public void mouseDragged(MouseEvent me) {

        updaterFrame.setLocation(updaterFrame.getLocation().x + me.getX() - pX,
          updaterFrame.getLocation().y + me.getY() - pY);
      }
    });
    titleBar.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent me) {

        updaterFrame.setLocation(updaterFrame.getLocation().x + me.getX() - pX,
          updaterFrame.getLocation().y + me.getY() - pY);
      }

      @Override
      public void mouseMoved(MouseEvent arg0) {
        // Auto-generated method stub
      }
    });
    titleBar.setLayout(null);

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 20, ColorUtil.getForegroundColor());
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(updaterFrame.getWidth() - 38, 3, 29, 29);
    closeButton.setToolTipText(_localeManager.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBackground(null);
    closeButton.setBorder(null);
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(e -> {
      _discordPresenceClient.stop();
      System.exit(0);
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_DOWN, 20, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(updaterFrame.getWidth() - 71, 3, 29, 29);
    minimizeButton.setToolTipText(_localeManager.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBackground(null);
    minimizeButton.setBorder(null);
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);
    minimizeButton.addActionListener(e -> updaterFrame.setState(Frame.ICONIFIED));

    updaterFrame.setLocationRelativeTo(null);
    updaterFrame.setVisible(true);
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
    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + "/KnightLauncher.jar" }, true);
    this.updaterFrame.dispose();
    System.exit(1);
  }

  public JFrame updaterFrame;
  private JProgressBar updaterProgressBar;
  private JLabel updaterState;

}

