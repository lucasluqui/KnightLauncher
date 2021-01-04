package com.lucasallegri.launcher;

import com.lucasallegri.discord.DiscordInstance;
import com.lucasallegri.util.ColorUtil;
import com.lucasallegri.util.DesktopUtil;
import com.lucasallegri.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mdlaf.utils.MaterialBorders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LauncherGUI {

  private static LauncherApp app;
  public static JFrame launcherGUIFrame;
  public static JButton launchButton;
  public static JButton settingsButton;
  public static JButton modButton;
  public static JButton updateButton;
  public static JTextPane tweetsContainer;
  public static JLabel launchState;
  public static JProgressBar launchProgressBar;
  public static JLabel imageContainer;
  public static JLabel playerCountLabel;

  int pX, pY;

  public LauncherGUI(LauncherApp app) {
    initialize();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.launcherGUIFrame.setVisible(!this.launcherGUIFrame.isVisible());
  }

  /** @wbp.parser.entryPoint */
  private void initialize() {

    launcherGUIFrame = new JFrame();
    launcherGUIFrame.setVisible(false);
    launcherGUIFrame.setTitle(LanguageManager.getValue("t.main", LauncherConstants.VERSION));
    launcherGUIFrame.setResizable(false);
    launcherGUIFrame.setBounds(100, 100, 850, 475);
    launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    launcherGUIFrame.setUndecorated(true);
    launcherGUIFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-128.png"));
    launcherGUIFrame.getContentPane().setLayout(null);

    Icon launchIcon = IconFontSwing.buildIcon(FontAwesome.PLAY_CIRCLE_O, 49, ColorUtil.getForegroundColor());
    launchButton = new JButton(launchIcon);
    launchButton.setBounds(21, 400, 52, 52);
    launchButton.setFont(FontManager.fontMedBig);
    launchButton.setFocusPainted(false);
    launchButton.setFocusable(false);
    launchButton.setToolTipText(LanguageManager.getValue("b.launch"));
    launcherGUIFrame.getContentPane().add(launchButton);
    launchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        if (KeyboardController.isShiftPressed() || KeyboardController.isAltPressed()) {
          LauncherEventHandler.launchGameAltEvent(_action);
        } else {
          LauncherEventHandler.launchGameEvent(_action);
        }
      }
    });

    imageContainer = new JLabel("Loading...");
    imageContainer.setBounds(23, 48, 525, 305);
    imageContainer.setFont(FontManager.fontRegBig);
    imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
    launcherGUIFrame.getContentPane().add(imageContainer);

    Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, 16, ColorUtil.getForegroundColor());
    modButton = new JButton(LanguageManager.getValue("b.mods"));
    modButton.setIcon(modsIcon);
    modButton.setBounds(80, 401, 100, 25);
    modButton.setHorizontalAlignment(SwingConstants.LEFT);
    modButton.setFont(FontManager.fontMed);
    modButton.setFocusPainted(false);
    modButton.setFocusable(false);
    modButton.setToolTipText(LanguageManager.getValue("b.mods"));
    launcherGUIFrame.getContentPane().add(modButton);
    modButton.addActionListener(new ActionListener() {
      @SuppressWarnings("static-access")
      public void actionPerformed(ActionEvent _action) {
        app.mgui.switchVisibility();
      }
    });

    Icon settingsIcon = IconFontSwing.buildIcon(FontAwesome.COGS, 16, ColorUtil.getForegroundColor());
    settingsButton = new JButton(LanguageManager.getValue("b.settings"));
    settingsButton.setIcon(settingsIcon);
    settingsButton.setBounds(80, 427, 100, 25);
    settingsButton.setHorizontalAlignment(SwingConstants.LEFT);
    settingsButton.setFont(FontManager.fontMed);
    settingsButton.setFocusPainted(false);
    settingsButton.setFocusable(false);
    settingsButton.setToolTipText(LanguageManager.getValue("b.settings"));
    launcherGUIFrame.getContentPane().add(settingsButton);
    settingsButton.addActionListener(new ActionListener() {
      @SuppressWarnings("static-access")
      public void actionPerformed(ActionEvent _action) {
        app.sgui.switchVisibility();
      }
    });

    JLabel labelTweets = new JLabel("<html>" + LanguageManager.getValue("m.twitter_title") + "</html>");
    labelTweets.setBounds(567, 36, 127, 28);
    labelTweets.setFont(FontManager.fontReg);
    launcherGUIFrame.getContentPane().add(labelTweets);

    tweetsContainer = new JTextPane();
    tweetsContainer.setText(LanguageManager.getValue("m.twitter_load"));
    tweetsContainer.setBounds(567, 75, 260, 297);
    tweetsContainer.setEditable(false);
    tweetsContainer.setFont(FontManager.fontReg);
    tweetsContainer.setBackground(ColorUtil.getBackgroundColor());
    tweetsContainer.setForeground(Color.WHITE);
    launcherGUIFrame.getContentPane().add(tweetsContainer);
    tweetsContainer.setCaretPosition(0);

    /*
     * Comment the following three lines to preview this GUI with WindowBuilder
     * I have no idea why they're conflicting with it, but without doing so
     * you won't be able to see anything, throwing errors on LanguageManager.getValue()
     * during t.main and b.launch parsing. Java is fun :)
     *
     * Of course you have to uncomment it before pushing any changes;
     * just a self reminder for myself...
     */
    JScrollPane tweetsJsp = new JScrollPane(tweetsContainer);
    tweetsJsp.setBounds(567, 75, 260, 297);
    LauncherGUI.launcherGUIFrame.getContentPane().add(tweetsJsp);

    launchProgressBar = new JProgressBar();
    launchProgressBar.setBounds(0, 470, 850, 5);
    launchProgressBar.setVisible(false);
    launcherGUIFrame.getContentPane().add(launchProgressBar);

    launchState = new JLabel("");
    launchState.setHorizontalAlignment(SwingConstants.RIGHT);
    launchState.setBounds(638, 443, 203, 23);
    launchState.setFont(FontManager.fontRegBig);
    launchState.setVisible(false);
    launcherGUIFrame.getContentPane().add(launchState);

    Icon updateIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 20, ColorUtil.getGreenForegroundColor());
    updateButton = new JButton(LanguageManager.getValue("b.update_available"));
    updateButton.setHorizontalAlignment(SwingConstants.CENTER);
    updateButton.setIcon(updateIcon);
    updateButton.setFont(FontManager.fontMedIta);
    updateButton.setFocusPainted(false);
    updateButton.setFocusable(false);
    updateButton.setForeground(ColorUtil.getGreenForegroundColor());
    updateButton.setVisible(false);
    updateButton.setBounds(185, 427, 150, 25);
    launcherGUIFrame.getContentPane().add(updateButton);
    updateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        DesktopUtil.openWebpage(
                "https://github.com/"
                        + LauncherConstants.GITHUB_AUTHOR + "/"
                        + LauncherConstants.GITHUB_REPO + "/"
                        + "releases/tag/"
                        + LauncherConstants.LATEST_RELEASE
        );
      }
    });

    playerCountLabel = new JLabel(LanguageManager.getValue("m.player_count_load"));
    playerCountLabel.setFont(FontManager.fontReg);
    playerCountLabel.setForeground(ColorUtil.getGreenForegroundColor());
    playerCountLabel.setBounds(23, 378, 507, 14);
    launcherGUIFrame.getContentPane().add(playerCountLabel);

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, launcherGUIFrame.getWidth(), 20);
    titleBar.setBackground(ColorUtil.getTitleBarColor());
    launcherGUIFrame.getContentPane().add(titleBar);


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

        launcherGUIFrame.setLocation(launcherGUIFrame.getLocation().x + me.getX() - pX,
                launcherGUIFrame.getLocation().y + me.getY() - pY);
      }
    });
    titleBar.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent me) {

        launcherGUIFrame.setLocation(launcherGUIFrame.getLocation().x + me.getX() - pX,
                launcherGUIFrame.getLocation().y + me.getY() - pY);
      }

      @Override
      public void mouseMoved(MouseEvent arg0) {
        // Auto-generated method stub
      }
    });
    titleBar.setLayout(null);

    JLabel windowTitle = new JLabel(LanguageManager.getValue("t.main", LauncherConstants.VERSION));
    windowTitle.setFont(FontManager.fontMed);
    windowTitle.setBounds(10, 0, launcherGUIFrame.getWidth() - 200, 20);
    titleBar.add(windowTitle);

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 14, ColorUtil.getForegroundColor());
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(launcherGUIFrame.getWidth() - 18, 1, 20, 21);
    closeButton.setToolTipText(LanguageManager.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    closeButton.setFont(FontManager.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DiscordInstance.stop();
        System.exit(0);
      }
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_DOWN, 14, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(launcherGUIFrame.getWidth() - 38, 1, 20, 21);
    minimizeButton.setToolTipText(LanguageManager.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    minimizeButton.setFont(FontManager.fontMed);
    titleBar.add(minimizeButton);
    minimizeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        launcherGUIFrame.setState(Frame.ICONIFIED);
      }
    });

    JButton discordButton = new JButton(ImageUtil.imageStreamToIcon(LauncherGUI.class.getResourceAsStream("/img/discord-16.png")));
    discordButton.setBounds(launcherGUIFrame.getWidth() - 67, 1, 18, 18);
    discordButton.setToolTipText("Discord");
    discordButton.setFocusPainted(false);
    discordButton.setFocusable(false);
    discordButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
    discordButton.setFont(FontManager.fontMed);
    titleBar.add(discordButton);
    discordButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DesktopUtil.openWebpage(LauncherConstants.DISCORD_URL);
      }
    });

    Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 16, ColorUtil.getForegroundColor());
    JButton bugButton = new JButton(bugIcon);
    bugButton.setBounds(launcherGUIFrame.getWidth() - 89, 1, 18, 18);
    bugButton.setToolTipText(LanguageManager.getValue("b.bug_report"));
    bugButton.setFocusPainted(false);
    bugButton.setFocusable(false);
    bugButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
    bugButton.setFont(FontManager.fontMed);
    titleBar.add(bugButton);
    bugButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DesktopUtil.openWebpage(LauncherConstants.BUG_REPORT_URL);
      }
    });

    Icon kofiIcon = IconFontSwing.buildIcon(FontAwesome.COFFEE, 16, Colors.KOFI);
    JButton kofiButton = new JButton(kofiIcon);
    kofiButton.setBounds(launcherGUIFrame.getWidth() - 111, 1, 18, 18);
    kofiButton.setToolTipText(LanguageManager.getValue("b.kofi"));
    kofiButton.setFocusPainted(false);
    kofiButton.setFocusable(false);
    kofiButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
    kofiButton.setFont(FontManager.fontMed);
    titleBar.add(kofiButton);
    kofiButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DesktopUtil.openWebpage(LauncherConstants.KOFI_URL);
      }
    });

    launcherGUIFrame.setLocationRelativeTo(null);

    launcherGUIFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        DiscordInstance.stop();
      }
    });

  }
}
