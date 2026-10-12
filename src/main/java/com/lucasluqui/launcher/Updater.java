package com.lucasluqui.launcher;

import com.lucasluqui.swing.Scale;
import com.google.inject.Inject;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.launcher.ui.BaseUI;
import com.lucasluqui.util.FileUtil;
import com.lucasluqui.util.ImageUtil;
import com.lucasluqui.util.ProcessUtil;
import com.lucasluqui.util.ZipUtil;
import com.lucasluqui.swing.LoadingIcon;
import com.lucasluqui.swing.SlimProgressBarUI;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.lucasluqui.launcher.Log.log;
import static com.lucasluqui.launcher.Updater.ErrorCode.ERR_DOWNLOAD;
import static com.lucasluqui.launcher.Updater.ErrorCode.ERR_FETCH;

public class Updater extends BaseUI
{
  public Updater ()
  {
    super(460, 176, false);
  }

  public void init (String newVersion)
  {
    super.init();
    this.newVersion = newVersion;
    compose();
    initFinished();
  }

  public void initFinished ()
  {
    super.initFinished();
    startUpdate();
  }

  @Override
  public void loadOnline ()
  {

  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle("Updater");
    guiFrame.setResizable(false);
    guiFrame.setBounds(Scale.rect(100, 100, this.width, this.height));
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    guiFrame.setUndecorated(true);
    guiFrame.setIconImage(ImageUtil.loadImageWithinJar("/rsrc/img/icon-128.png"));
    guiFrame.setShape(new RoundRectangle2D.Double(0, 0, Scale.px(this.width), Scale.px(this.height), Scale.px(15), Scale.px(15)));

    BufferedImage logoImage = ImageUtil.loadImageWithinJar("/rsrc/img/icon-128.png");

    // A soft glow in the launcher's color behind the logo, fading out before it reaches the texts.
    JPanel content = new JPanel(null)
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        Color glow = CustomColors.LAUNCHER;
        g2d.setPaint(new RadialGradientPaint(Scale.px(60), Scale.px(64), Scale.px(150), new float[]{ 0f, 1f }, new Color[]{
          new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 70),
          new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 0) }));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
      }
    };
    guiFrame.setContentPane(content);

    // There's no title bar, so the whole window can be dragged around instead.
    MouseAdapter dragListener = new MouseAdapter()
    {
      @Override
      public void mousePressed (MouseEvent e)
      {
        pX = e.getX();
        pY = e.getY();
      }

      @Override
      public void mouseDragged (MouseEvent e)
      {
        guiFrame.setLocation(guiFrame.getX() + e.getX() - pX, guiFrame.getY() + e.getY() - pY);
      }
    };
    content.addMouseListener(dragListener);
    content.addMouseMotionListener(dragListener);

    JLabel launcherLogo = new JLabel(new ImageIcon(logoImage.getScaledInstance(Scale.px(72), Scale.px(72), Image.SCALE_SMOOTH)));
    launcherLogo.setBounds(Scale.rect(24, 28, 72, 72));
    content.add(launcherLogo);

    JLabel title = new JLabel("Updating Knight Launcher");
    title.setBounds(Scale.rect(116, 26, 320, 26));
    title.setFont(Fonts.getFont("default", 18.0f, Font.BOLD));
    title.setForeground(Color.WHITE);
    content.add(title);

    updaterState = new JLabel("");
    updaterState.setIcon(new LoadingIcon(16));
    updaterState.setIconTextGap(Scale.px(8));
    updaterState.setHorizontalAlignment(SwingConstants.LEFT);
    updaterState.setBounds(Scale.rect(116, 94, 260, 20));
    updaterState.setFont(Fonts.getFont("default", 12.0f, Font.ITALIC));
    content.add(updaterState);

    updaterStep = new JLabel("");
    updaterStep.setHorizontalAlignment(SwingConstants.RIGHT);
    updaterStep.setBounds(Scale.rect(376, 94, 60, 20));
    updaterStep.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    updaterStep.setForeground(new Color(170, 174, 186));
    content.add(updaterStep);

    // Bounds include the room the bar's glow needs around it.
    updaterProgressBar = new JProgressBar();
    updaterProgressBar.setUI(new SlimProgressBarUI(CustomColors.LAUNCH));
    updaterProgressBar.setOpaque(false);
    updaterProgressBar.setBorder(null);
    updaterProgressBar.setBounds(Scale.rect(116 - SlimProgressBarUI.GLOW_MARGIN, 124 - 8, 320 + SlimProgressBarUI.GLOW_MARGIN * 2, 16));
    updaterProgressBar.addChangeListener(e ->
      updaterStep.setText(updaterProgressBar.getValue() + " / " + updaterProgressBar.getMaximum()));
    content.add(updaterProgressBar);

    JLabel footnote = new JLabel("The launcher will restart by itself once it's done.");
    footnote.setBounds(Scale.rect(116, 142, 320, 16));
    footnote.setFont(Fonts.getFont("default", 10.0f, Font.ITALIC));
    footnote.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    content.add(footnote);

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

    if (this.newVersion == null) {
      fail(ERR_FETCH);
    }

    updaterProgressBar.setValue(1);
    updaterState.setText("Downloading version " + this.newVersion + "...");
    downloadLatestVersion();

    this.updaterProgressBar.setValue(2);
    this.updaterState.setText("Extracting version " + this.newVersion + " files...");
    ZipUtil.unzip(LauncherGlobals.USER_DIR + "/KnightLauncher.zip", LauncherGlobals.USER_DIR + "/");

    updaterProgressBar.setValue(3);
    updaterState.setText("Cleaning leftover files...");
    new File(LauncherGlobals.USER_DIR + "/KnightLauncher.jar.old").delete();
    new File(LauncherGlobals.USER_DIR + "/KnightLauncher.zip").delete();

    updaterProgressBar.setValue(4);
    updaterState.setIcon(IconFontSwing.buildIcon(FontAwesome.CHECK, Scale.px(14), CustomColors.GREEN));
    updaterState.setText("Update finished");
    this.finish(true);
  }

  private void downloadLatestVersion ()
  {
    String downloadUrl = "https://github.com/"
      + LauncherGlobals.GITHUB_AUTHOR + "/"
      + LauncherGlobals.GITHUB_REPO + "/"
      + "releases/download/"
      + this.newVersion + "/"
      + "KnightLauncher-" + this.newVersion + ".zip";

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
      fail(ERR_DOWNLOAD);
    }
  }

  private void fail (ErrorCode errorCode)
  {
    String errMsg = "";

    switch (errorCode) {
      case ERR_FETCH:
        log.error("Couldn't fetch latest version, stopping launcher update", "attempts", this.fetchAttempts);
        errMsg += "The updater failed to fetch the latest version after 3 attempts." +
          "\nBooting back into current version, try updating later.";
        break;
      case ERR_DOWNLOAD:
        log.error("Couldn't download latest version, stopping launcher update");
        errMsg += "The update couldn't be initiated after 3 download attempts." +
          "\nBooting back into current version, try updating later.";
        break;
      default:
        break;
    }

    List<File> files = new ArrayList<>();
    files.add(new File(LauncherGlobals.USER_DIR + File.separator + "knightlauncher.log"));
    files.add(new File(LauncherGlobals.USER_DIR + File.separator + "old-knightlauncher.log"));
    FileUtil.copyFilesToClipboard(files);
    errMsg += "\n\nRelevant log files were automatically copied to your clipboard.";

    Dialog.push(errMsg, JOptionPane.ERROR_MESSAGE);
    finish(false);
  }

  private void finish (boolean success)
  {
    _discordPresenceClient.stop();

    String[] startParams;
    if (success) {
      startParams = new String[]{"java", "-jar", LauncherGlobals.USER_DIR + "/KnightLauncher.jar"};
    } else {
      startParams = new String[]{"java", "-jar", LauncherGlobals.USER_DIR + "/KnightLauncher.jar", "updateFailed"};
    }
    ProcessUtil.run(startParams, true);

    this.guiFrame.dispose();
    System.exit(0);
  }

  @Inject protected LocaleManager _localeManager;
  @Inject protected DownloadManager _downloadManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;

  private String newVersion = "";
  private final int fetchAttempts = 0;

  private JProgressBar updaterProgressBar;
  private JLabel updaterState;
  private JLabel updaterStep;

  enum ErrorCode
  {
    ERR_FETCH,
    ERR_DOWNLOAD
  }
}

