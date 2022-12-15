package com.lucasallegri.launcher;

import com.lucasallegri.discord.DiscordRPC;
import com.lucasallegri.util.ColorUtil;
import com.lucasallegri.util.DesktopUtil;
import com.lucasallegri.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mdlaf.utils.MaterialBorders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LauncherGUI extends BaseGUI {

  private final LauncherApp app;
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

  public LauncherGUI(LauncherApp app) {
    super();
    this.app = app;
    initialize();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.launcherGUIFrame.setVisible(!this.launcherGUIFrame.isVisible());
  }

  /** @wbp.parser.entryPoint */
  @SuppressWarnings("static-access")
  private void initialize() {

    launcherGUIFrame = new JFrame();
    launcherGUIFrame.setVisible(false);
    launcherGUIFrame.setTitle(Locale.getValue("t.main", LauncherGlobals.VERSION));
    launcherGUIFrame.setResizable(false);
    launcherGUIFrame.setBounds(100, 100, 850, 475);
    launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    launcherGUIFrame.setUndecorated(true);
    launcherGUIFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-128.png"));
    launcherGUIFrame.getContentPane().setLayout(null);

    Icon launchIcon = IconFontSwing.buildIcon(FontAwesome.PLAY_CIRCLE_O, 49, ColorUtil.getForegroundColor());
    launchButton = new JButton(launchIcon);
    launchButton.setBounds(21, 400, 52, 52);
    launchButton.setFont(Fonts.fontMedBig);
    launchButton.setFocusPainted(false);
    launchButton.setFocusable(false);
    launchButton.setToolTipText(Locale.getValue("b.launch"));
    launcherGUIFrame.getContentPane().add(launchButton);
    launchButton.addActionListener(action -> {
      if (KeyboardController.isShiftPressed() || KeyboardController.isAltPressed()) {
        LauncherEventHandler.launchGameAltEvent();
      } else {
        LauncherEventHandler.launchGameEvent();
      }
    });

    imageContainer = new JLabel("Loading...");
    imageContainer.setBounds(23, 48, 525, 305);
    imageContainer.setFont(Fonts.fontRegBig);
    imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
    launcherGUIFrame.getContentPane().add(imageContainer);

    Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, 16, ColorUtil.getForegroundColor());
    modButton = new JButton(Locale.getValue("b.mods"));
    modButton.setIcon(modsIcon);
    modButton.setBounds(80, 401, 100, 25);
    modButton.setHorizontalAlignment(SwingConstants.LEFT);
    modButton.setFont(Fonts.fontMed);
    modButton.setFocusPainted(false);
    modButton.setFocusable(false);
    modButton.setToolTipText(Locale.getValue("b.mods"));
    launcherGUIFrame.getContentPane().add(modButton);
    modButton.addActionListener(action -> app.mgui.switchVisibility());

    Icon settingsIcon = IconFontSwing.buildIcon(FontAwesome.COGS, 16, ColorUtil.getForegroundColor());
    settingsButton = new JButton(Locale.getValue("b.settings"));
    settingsButton.setIcon(settingsIcon);
    settingsButton.setBounds(80, 427, 100, 25);
    settingsButton.setHorizontalAlignment(SwingConstants.LEFT);
    settingsButton.setFont(Fonts.fontMed);
    settingsButton.setFocusPainted(false);
    settingsButton.setFocusable(false);
    settingsButton.setToolTipText(Locale.getValue("b.settings"));
    launcherGUIFrame.getContentPane().add(settingsButton);
    settingsButton.addActionListener(action -> app.sgui.switchVisibility());

    JLabel labelTweets = new JLabel("<html>" + Locale.getValue("m.twitter_title") + "</html>");
    labelTweets.setBounds(567, 36, 170, 35);
    labelTweets.setFont(Fonts.fontReg);
    launcherGUIFrame.getContentPane().add(labelTweets);

    tweetsContainer = new JTextPane();
    tweetsContainer.setText(Locale.getValue("m.twitter_load"));
    tweetsContainer.setBounds(567, 75, 260, 297);
    tweetsContainer.setEditable(false);
    tweetsContainer.setFont(Fonts.fontReg);
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
    launchState.setFont(Fonts.fontRegBig);
    launchState.setVisible(false);
    launcherGUIFrame.getContentPane().add(launchState);

    Icon updateIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 20, ColorUtil.getGreenForegroundColor());
    updateButton = new JButton(Locale.getValue("b.update_available"));
    updateButton.setHorizontalAlignment(SwingConstants.CENTER);
    updateButton.setIcon(updateIcon);
    updateButton.setFont(Fonts.fontMedIta);
    updateButton.setFocusPainted(false);
    updateButton.setFocusable(false);
    updateButton.setForeground(ColorUtil.getGreenForegroundColor());
    updateButton.setVisible(false);
    updateButton.setBounds(185, 427, 165, 25);
    launcherGUIFrame.getContentPane().add(updateButton);

    playerCountLabel = new JLabel(Locale.getValue("m.player_count_load"));
    playerCountLabel.setFont(Fonts.fontReg);
    playerCountLabel.setForeground(ColorUtil.getGreenForegroundColor());
    playerCountLabel.setBounds(23, 375, 507, 18);
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

    JLabel windowTitle = new JLabel(Locale.getValue("t.main", LauncherGlobals.VERSION));
    windowTitle.setFont(Fonts.fontMed);
    windowTitle.setBounds(10, 0, launcherGUIFrame.getWidth() - 200, 20);
    titleBar.add(windowTitle);

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 14, ColorUtil.getForegroundColor());
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(launcherGUIFrame.getWidth() - 18, 1, 20, 21);
    closeButton.setToolTipText(Locale.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(e -> {
      DiscordRPC.getInstance().stop();
      System.exit(0);
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_DOWN, 14, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(launcherGUIFrame.getWidth() - 38, 1, 20, 21);
    minimizeButton.setToolTipText(Locale.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);
    minimizeButton.addActionListener(e -> launcherGUIFrame.setState(Frame.ICONIFIED));

    JButton discordButton = new JButton(ImageUtil.imageStreamToIcon(LauncherGUI.class.getResourceAsStream("/img/discord-16.png")));
    discordButton.setBounds(launcherGUIFrame.getWidth() - 67, 1, 18, 18);
    discordButton.setToolTipText("Discord");
    discordButton.setFocusPainted(false);
    discordButton.setFocusable(false);
    discordButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
    discordButton.setFont(Fonts.fontMed);
    titleBar.add(discordButton);
    discordButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.DISCORD_URL));

    Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 16, ColorUtil.getForegroundColor());
    JButton bugButton = new JButton(bugIcon);
    bugButton.setBounds(launcherGUIFrame.getWidth() - 89, 1, 18, 18);
    bugButton.setToolTipText(Locale.getValue("b.bug_report"));
    bugButton.setFocusPainted(false);
    bugButton.setFocusable(false);
    bugButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
    bugButton.setFont(Fonts.fontMed);
    titleBar.add(bugButton);
    bugButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.BUG_REPORT_URL));

    Icon kofiIcon = IconFontSwing.buildIcon(FontAwesome.COFFEE, 16, Colors.KOFI);
    JButton kofiButton = new JButton(kofiIcon);
    kofiButton.setBounds(launcherGUIFrame.getWidth() - 111, 1, 18, 18);
    kofiButton.setToolTipText(Locale.getValue("b.kofi"));
    kofiButton.setFocusPainted(false);
    kofiButton.setFocusable(false);
    kofiButton.setBorder(BorderFactory.createLineBorder(ColorUtil.getTitleBarColor()));
    kofiButton.setFont(Fonts.fontMed);
    titleBar.add(kofiButton);
    kofiButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.KOFI_URL));

    launcherGUIFrame.setLocationRelativeTo(null);

    launcherGUIFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        DiscordRPC.getInstance().stop();
      }
    });

  }
}
