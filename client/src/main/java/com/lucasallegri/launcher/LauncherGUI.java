package com.lucasallegri.launcher;

import com.lucasallegri.dialog.DialogWarning;
import com.lucasallegri.discord.DiscordRPC;
import com.lucasallegri.launcher.mods.ModListGUI;
import com.lucasallegri.launcher.settings.SettingsGUI;
import com.lucasallegri.util.ColorUtil;
import com.lucasallegri.util.DesktopUtil;
import com.lucasallegri.util.ImageUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class LauncherGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame launcherGUIFrame;
  public static JPanel mainPane;
  public static JTabbedPane layeredSettingsPane = new JTabbedPane();
  public static JPanel layeredModsPane = new JPanel();
  public static JButton layeredReturnButton;
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
    launcherGUIFrame.setTitle(Locale.getValue("t.main", LauncherGlobals.LAUNCHER_VERSION));
    launcherGUIFrame.setResizable(false);
    launcherGUIFrame.setBounds(100, 100, 1050, 550);
    launcherGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    launcherGUIFrame.setUndecorated(true);
    launcherGUIFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-128.png"));
    launcherGUIFrame.getContentPane().setBackground(new Color(56, 60, 71));
    launcherGUIFrame.getContentPane().setLayout(null);

    JPanel sidePane = new JPanel();
    sidePane.setBackground(new Color(45, 48, 57));
    sidePane.setVisible(true);
    sidePane.setLayout(null);
    sidePane.setBounds(0, 35, 250, 550);
    launcherGUIFrame.getContentPane().add(sidePane);

    mainPane = new JPanel();
    mainPane.setLayout(null);
    mainPane.setBackground(new Color(56, 60, 71));
    mainPane.setBounds(250, 35, 800, 550);
    launcherGUIFrame.getContentPane().add(mainPane);

    JLabel launcherLogo = new JLabel();
    BufferedImage launcherLogoImage = ImageUtil.loadImageWithinJar("/img/icon-92.png");
    launcherLogo.setBounds(80, -55, 256, 256);
    launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
    sidePane.add(launcherLogo);

    JLabel launcherName = new JLabel(LauncherGlobals.LAUNCHER_NAME);
    launcherName.setFont(Fonts.fontMedBig);
    launcherName.setHorizontalAlignment(SwingConstants.CENTER);
    launcherName.setVerticalAlignment(SwingConstants.CENTER);
    launcherName.setBounds(0, 100, 250, 80);
    sidePane.add(launcherName);

    JLabel launcherVersion = new JLabel("v" + LauncherGlobals.LAUNCHER_VERSION);
    launcherVersion.setFont(Fonts.fontMedIta);
    launcherVersion.setHorizontalAlignment(SwingConstants.CENTER);
    launcherVersion.setVerticalAlignment(SwingConstants.CENTER);
    launcherVersion.setBounds(0, 150, 250, 25);
    sidePane.add(launcherVersion);

    JLabel serverPickerLabel = new JLabel("Server");
    serverPickerLabel.setFont(Fonts.fontMed);
    serverPickerLabel.setBounds(28, 210, 80, 20);
    sidePane.add(serverPickerLabel);

    JComboBox serverPicker = new JComboBox();
    serverPicker.setBounds(73, 210, 150, 20);
    serverPicker.setFont(Fonts.fontReg);
    serverPicker.setFocusable(false);
    serverPicker.setRequestFocusEnabled(false);
    sidePane.add(serverPicker);
    serverPicker.addItem("Official");

    playerCountLabel = new JLabel(Locale.getValue("m.player_count_load"));
    playerCountLabel.setFont(Fonts.fontReg);
    playerCountLabel.setForeground(new Color(34, 197, 94));
    playerCountLabel.setBounds(28, 235, 200, 18);
    sidePane.add(playerCountLabel);

    Icon settingsIcon = IconFontSwing.buildIcon(FontAwesome.COGS, 16, ColorUtil.getForegroundColor());
    settingsButton = new JButton(Locale.getValue("b.settings"));
    settingsButton.setIcon(settingsIcon);
    settingsButton.setBounds(28, 320, 125, 35);
    settingsButton.setHorizontalAlignment(SwingConstants.LEFT);
    settingsButton.setFont(Fonts.fontMed);
    settingsButton.setFocusPainted(false);
    settingsButton.setFocusable(false);
    settingsButton.setBackground(new Color(107, 114, 128));
    settingsButton.setForeground(Color.WHITE);
    settingsButton.setToolTipText(Locale.getValue("b.settings"));
    settingsButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredModsPane.setVisible(false);

      layeredSettingsPane = SettingsGUI.tabbedPane;
      layeredSettingsPane.setBounds(250, 75, 800, 550);
      launcherGUIFrame.add(layeredSettingsPane);
      layeredSettingsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(255, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(new Color(56, 60, 71));
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      launcherGUIFrame.add(layeredReturnButton);
    });
    sidePane.add(settingsButton);

    Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, 16, ColorUtil.getForegroundColor());
    modButton = new JButton(Locale.getValue("b.mods"));
    modButton.setIcon(modsIcon);
    modButton.setBounds(28, 360, 125, 35);
    modButton.setHorizontalAlignment(SwingConstants.LEFT);
    modButton.setFont(Fonts.fontMed);
    modButton.setFocusPainted(false);
    modButton.setFocusable(false);
    modButton.setEnabled(false);
    modButton.setBackground(new Color(107, 114, 128));
    modButton.setForeground(Color.WHITE);
    modButton.setToolTipText(Locale.getValue("b.mods"));
    modButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredSettingsPane.setVisible(false);

      layeredModsPane = ModListGUI.modListPanel;
      layeredModsPane.setBounds(250, 75, 800, 550);
      launcherGUIFrame.add(layeredModsPane);
      layeredModsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(255, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(new Color(56, 60, 71));
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      launcherGUIFrame.add(layeredReturnButton);
    });
    sidePane.add(modButton);

    Icon updateIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 16, Color.WHITE);
    updateButton = new JButton(Locale.getValue("b.update_available"));
    updateButton.setHorizontalAlignment(SwingConstants.LEFT);
    updateButton.setIcon(updateIcon);
    updateButton.setFont(Fonts.fontMedIta);
    updateButton.setFocusPainted(false);
    updateButton.setFocusable(false);
    updateButton.setBackground(new Color(34, 197, 94));
    updateButton.setForeground(Color.WHITE);
    updateButton.setVisible(false);
    updateButton.setBounds(28, 400, 125, 35);
    sidePane.add(updateButton);

    JButton discordButton = new JButton(ImageUtil.imageStreamToIcon(LauncherGUI.class.getResourceAsStream("/img/icon-discord.png")));
    discordButton.setBounds(66, 465, 35, 35);
    discordButton.setToolTipText("Discord");
    discordButton.setFocusPainted(false);
    discordButton.setFocusable(false);
    discordButton.setBackground(new Color(107, 114, 128));
    discordButton.setFont(Fonts.fontMed);
    sidePane.add(discordButton);
    discordButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DISCORD));

    Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 16, Color.WHITE);
    JButton bugButton = new JButton(bugIcon);
    bugButton.setBounds(106, 465, 35, 35);
    bugButton.setToolTipText(Locale.getValue("b.bug_report"));
    bugButton.setFocusPainted(false);
    bugButton.setFocusable(false);
    bugButton.setBackground(new Color(107, 114, 128));
    bugButton.setFont(Fonts.fontMed);
    sidePane.add(bugButton);
    bugButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_BUG_REPORT));

    Icon kofiIcon = IconFontSwing.buildIcon(FontAwesome.COFFEE, 16, Color.WHITE);
    JButton kofiButton = new JButton(kofiIcon);
    kofiButton.setBounds(146, 465, 35, 35);
    kofiButton.setToolTipText(Locale.getValue("b.kofi"));
    kofiButton.setFocusPainted(false);
    kofiButton.setFocusable(false);
    kofiButton.setBackground(new Color(107, 114, 128));
    kofiButton.setFont(Fonts.fontMed);
    sidePane.add(kofiButton);
    kofiButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_KOFI));

    JLabel infoPane = new JLabel();
    BufferedImage infoPaneBackgroundImage = ImageUtil.loadImageWithinJar("/img/infopane.png");
    infoPane.setBounds(50, 40, 700, 340);
    infoPane.setIcon(new ImageIcon(ImageUtil.addRoundedCorners(infoPaneBackgroundImage, 25)));
    mainPane.add(infoPane);

    JLabel infoPaneLoading = new JLabel("Loading...");
    infoPaneLoading.setBounds(50, 40, 700, 340);
    infoPaneLoading.setFont(Fonts.fontMedBig);
    infoPaneLoading.setHorizontalAlignment(SwingConstants.CENTER);
    infoPaneLoading.setVerticalAlignment(SwingConstants.CENTER);
    mainPane.add(infoPaneLoading);
    mainPane.setComponentZOrder(infoPaneLoading, 0);

    launchButton = new JButton("Play Now");
    launchButton.setBounds(572, 423, 200, 66);
    launchButton.setFont(Fonts.fontMedBig);
    launchButton.setFocusPainted(false);
    launchButton.setFocusable(false);
    launchButton.setBackground(new Color(0, 133, 255));
    launchButton.setForeground(Color.WHITE);
    launchButton.setToolTipText("Play Now");
    mainPane.add(launchButton);
    launchButton.addActionListener(action -> {
      if (KeyboardController.isShiftPressed() || KeyboardController.isAltPressed()) {
        LauncherEventHandler.launchGameAltEvent();
      } else {
        LauncherEventHandler.launchGameEvent();
      }
    });

    String launchTooltipText = "Keep ALT or SHIFT pressed while launching to launch in alt account mode and save computing resources for your main instance.";
    Icon launchTooltipButtonIcon = IconFontSwing.buildIcon(FontAwesome.QUESTION, 16, Color.WHITE);
    JButton launchTooltipButton = new JButton();
    launchTooltipButton.setIcon(launchTooltipButtonIcon);
    launchTooltipButton.setBounds(550, 425, 16, 16);
    launchTooltipButton.setEnabled(true);
    launchTooltipButton.setFocusable(false);
    launchTooltipButton.setFocusPainted(false);
    launchTooltipButton.setForeground(Color.WHITE);
    launchTooltipButton.setToolTipText(launchTooltipText);
    launchTooltipButton.addActionListener(l -> {
      DialogWarning.push(launchTooltipText);
    });
    mainPane.add(launchTooltipButton);

    launchState = new JLabel("");
    launchState.setHorizontalAlignment(SwingConstants.LEFT);
    launchState.setBounds(30, 420, 510, 25);
    launchState.setFont(Fonts.fontRegBig);
    launchState.setVisible(false);
    mainPane.add(launchState);

    launchProgressBar = new JProgressBar();
    launchProgressBar.setBounds(30, 450, 510, 25);
    launchProgressBar.setVisible(false);
    mainPane.add(launchProgressBar);

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, launcherGUIFrame.getWidth(), 35);
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

    /*
    JLabel windowTitle = new JLabel(Locale.getValue("t.main", LauncherGlobals.LAUNCHER_VERSION));
    windowTitle.setFont(Fonts.fontMed);
    windowTitle.setBounds(10, 0, launcherGUIFrame.getWidth() - 200, 35);
    titleBar.add(windowTitle);
     */

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 20, ColorUtil.getForegroundColor());
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(launcherGUIFrame.getWidth() - 38, 3, 29, 29);
    closeButton.setToolTipText(Locale.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBackground(null);
    closeButton.setBorder(null);
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(e -> {
      DiscordRPC.getInstance().stop();
      System.exit(0);
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_DOWN, 20, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(launcherGUIFrame.getWidth() - 71, 3, 29, 29);
    minimizeButton.setToolTipText(Locale.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBackground(null);
    minimizeButton.setBorder(null);
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);
    minimizeButton.addActionListener(e -> launcherGUIFrame.setState(Frame.ICONIFIED));

    launcherGUIFrame.setLocationRelativeTo(null);

    launcherGUIFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        DiscordRPC.getInstance().stop();
      }
    });

  }
}
