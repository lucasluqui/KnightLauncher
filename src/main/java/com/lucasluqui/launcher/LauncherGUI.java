package com.lucasluqui.launcher;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static com.lucasluqui.launcher.Log.log;

public class LauncherGUI extends BaseGUI
{

  @Inject public LauncherEventHandler eventHandler;

  @Inject protected LauncherContext _launcherCtx;
  @Inject protected LocaleManager _localeManager;
  @Inject protected FlamingoManager _flamingoManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;
  @Inject protected KeyboardController _keyboardController;

  @Inject
  public LauncherGUI ()
  {
    super(1100, 550, true);
  }

  public void init ()
  {
    try {
      compose();
    } catch (UnsatisfiedLinkError e) {
      // Some Windows installations don't allow you to write to the default temp dir and throw this error instead
      // when trying to set up any UI. Let's divert the temp directory to a custom one.
      log.error(e);
      SystemUtil.fixTempDir(LauncherGlobals.USER_DIR + "/KnightLauncher/temp/");
      compose();
    }
  }

  /**
   * @wbp.parser.entryPoint
   */
  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.main", BuildConfig.getVersion()));
    guiFrame.setResizable(false);
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    guiFrame.setUndecorated(true);
    guiFrame.setIconImage(ImageUtil.loadImageWithinJar("/rsrc/img/icon-256.png"));
    guiFrame.setShape(new RoundRectangle2D.Double(0, 0, this.width, this.height, 15, 15));
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().setLayout(null);

    returnButton.setBounds(305, 40, 25, 25);
    returnButton.setToolTipText(_localeManager.getValue("b.back"));
    returnButton.addActionListener(l -> {
      layeredSettingsPane.setVisible(false);
      layeredModsPane.setVisible(false);
      layeredEditorsPane.setVisible(false);
      mainPane.setVisible(true);
      returnButton.setVisible(false);
    });

    serverSwitcherPane = new JPanel();
    serverSwitcherPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    serverSwitcherPane.setVisible(true);
    serverSwitcherPane.setBounds(0, 42, 50, 550);
    guiFrame.getContentPane().add(serverSwitcherPane);

    serverSwitcherPaneScrollBar = new JScrollPane(serverSwitcherPane);
    serverSwitcherPaneScrollBar.setBounds(0, 42, 50, 550);
    serverSwitcherPaneScrollBar.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    serverSwitcherPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    serverSwitcherPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    serverSwitcherPaneScrollBar.setBorder(null);
    serverSwitcherPaneScrollBar.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    serverSwitcherPaneScrollBar.getVerticalScrollBar().setUnitIncrement(16);
    serverSwitcherPaneScrollBar.setVisible(true);
    guiFrame.getContentPane().add(serverSwitcherPaneScrollBar);

    JPanel sidePane = new JPanel();
    sidePane.setBackground(CustomColors.INTERFACE_SIDEPANE_BACKGROUND);
    sidePane.setVisible(true);
    sidePane.setLayout(null);
    sidePane.setBounds(50, 35, 250, 550);
    guiFrame.getContentPane().add(sidePane);

    banner = ImageUtil.loadImageWithinJar("/rsrc/img/banner-loading.png");

    mainPane = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        super.paintComponent(g);
        g.drawImage(banner, 0, 0, null);
      }
    };
    mainPane.setLayout(null);
    mainPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    mainPane.setBounds(300, 35, 800, 550);
    guiFrame.getContentPane().add(mainPane);

    bannerLoading = new JLabel(_localeManager.getValue("m.loading"));
    bannerLoading.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    bannerLoading.setBounds(325, 235, 150, 45);
    bannerLoading.setFont(Fonts.getFont("defaultMedium", 14.0f, Font.PLAIN));
    bannerLoading.setHorizontalAlignment(SwingConstants.CENTER);
    bannerLoading.setVerticalAlignment(SwingConstants.CENTER);
    bannerLoading.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND)
        + "AA; foreground:" + ColorUtil.colorToHexString(Color.WHITE)
        + "; arc:999;");
    mainPane.add(bannerLoading);
    mainPane.setComponentZOrder(bannerLoading, 0);

    JLabel launcherLogo = new JLabel();
    BufferedImage launcherLogoImage = ImageUtil.loadImageWithinJar("/rsrc/img/icon-92.png");
    BufferedImage launcherLogoImageHover = ImageUtil.loadImageWithinJar("/rsrc/img/icon-92-hover.png");
    launcherLogo.setBounds(0, -27, 251, 200);
    launcherLogo.setHorizontalAlignment(SwingConstants.CENTER);
    launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
    sidePane.add(launcherLogo);
    launcherLogo.addMouseListener(new MouseListener()
    {
      @Override
      public void mouseClicked (MouseEvent e)
      {
        returnToHome();
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
        launcherLogo.setIcon(new ImageIcon(launcherLogoImageHover));
        launcherLogo.updateUI();
      }

      @Override
      public void mouseExited (MouseEvent e)
      {
        launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
        launcherLogo.updateUI();
      }
    });

    JLabel launcherName = new JLabel(BuildConfig.getName());
    launcherName.setFont(Fonts.getFont("defaultMedium", 15.0f, Font.PLAIN));
    launcherName.setHorizontalAlignment(SwingConstants.CENTER);
    launcherName.setVerticalAlignment(SwingConstants.CENTER);
    launcherName.setBounds(0, 100, 250, 80);
    sidePane.add(launcherName);

    selectedServerLabel = new JLabel("Official");
    selectedServerLabel.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    selectedServerLabel.setVisible(false);
    selectedServerLabel.setBounds(28, 185, 120, 20);
    sidePane.add(selectedServerLabel);

    serverInfoButton = new JButton();
    serverInfoButton.setBounds(35, 213, 178, 25);
    serverInfoButton.setEnabled(false);
    serverInfoButton.setVisible(false);
    serverInfoButton.setFocusable(false);
    serverInfoButton.setFocusPainted(false);
    serverInfoButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    serverInfoButton.setForeground(Color.WHITE);
    serverInfoButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    serverInfoButton.setToolTipText(_localeManager.getValue("m.server_info"));
    serverInfoButton.addActionListener(l -> this.eventHandler.displaySelectedServerInfo());
    sidePane.add(serverInfoButton);

    Icon playerCountTitleIcon = IconFontSwing.buildIcon(FontAwesome.USERS, 17, CustomColors.INTERFACE_DEFAULT);
    JLabel playerCountTitleLabel = new JLabel(_localeManager.getValue("m.players_online"));
    playerCountTitleLabel.setIcon(playerCountTitleIcon);
    playerCountTitleLabel.setBounds(35, 188, 200, 20);
    playerCountTitleLabel.setFont(Fonts.getFont("defaultMedium", 13.0f, Font.ITALIC));
    sidePane.add(playerCountTitleLabel);

    playerCountLabel = new JLabel("");
    playerCountLabel.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    playerCountLabel.setBounds(25, 188, 190, 20);
    playerCountLabel.setFont(Fonts.getFont("defaultMedium", 13.0f, Font.ITALIC));
    playerCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    playerCountLabel.setVisible(false);
    sidePane.add(playerCountLabel);

    String playerCountTooltipTitle = _localeManager.getValue("m.players_online");
    String playerCountTooltipText = _localeManager.getValue("m.players_online_text");
    playerCountTooltipButton = new JButton(_localeManager.getValue("b.learn_more"));
    playerCountTooltipButton.setBounds(35, 213, 178, 25);
    playerCountTooltipButton.setEnabled(true);
    playerCountTooltipButton.setFocusable(false);
    playerCountTooltipButton.setFocusPainted(false);
    playerCountTooltipButton.setBorderPainted(false);
    playerCountTooltipButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    playerCountTooltipButton.setForeground(Color.WHITE);
    playerCountTooltipButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    playerCountTooltipButton.setToolTipText(playerCountTooltipTitle);
    playerCountTooltipButton.addActionListener(l -> {
      Dialog.push(playerCountTooltipText, playerCountTooltipTitle, JOptionPane.INFORMATION_MESSAGE);
    });
    sidePane.add(playerCountTooltipButton);
    playerCountTooltipButton.setVisible(false);

    Icon settingsIcon = IconFontSwing.buildIcon(FontAwesome.COGS, 16, ColorUtil.getForegroundColor());
    settingsButton = new JButton(_localeManager.getValue("b.settings"));
    settingsButton.setIcon(settingsIcon);
    settingsButton.setBounds(28, 275, 125, 35);
    settingsButton.setHorizontalAlignment(SwingConstants.LEFT);
    settingsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    settingsButton.setFocusPainted(false);
    settingsButton.setFocusable(false);
    settingsButton.setBorderPainted(false);
    settingsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    settingsButton.setForeground(Color.WHITE);
    settingsButton.setToolTipText(_localeManager.getValue("b.settings"));
    settingsButton.addActionListener(action -> {
      showSettingsMenu();
    });
    sidePane.add(settingsButton);

    Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, 16, ColorUtil.getForegroundColor());
    modButton = new JButton(_localeManager.getValue("b.mods"));
    modButton.setIcon(modsIcon);
    modButton.setBounds(28, 315, 125, 35);
    modButton.setHorizontalAlignment(SwingConstants.LEFT);
    modButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    modButton.setFocusPainted(false);
    modButton.setFocusable(false);
    modButton.setBorderPainted(false);
    modButton.setEnabled(true);
    modButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    modButton.setForeground(Color.WHITE);
    modButton.setToolTipText(_localeManager.getValue("b.mods"));
    modButton.addActionListener(action -> {
      showModsMenu();
    });
    sidePane.add(modButton);

    Icon editorsIcon = IconFontSwing.buildIcon(FontAwesome.PENCIL, 16, ColorUtil.getForegroundColor());
    editorsButton = new JButton(_localeManager.getValue("b.editors"));
    editorsButton.setIcon(editorsIcon);
    editorsButton.setBounds(28, 355, 125, 35);
    editorsButton.setHorizontalAlignment(SwingConstants.LEFT);
    editorsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    editorsButton.setFocusPainted(false);
    editorsButton.setFocusable(false);
    editorsButton.setBorderPainted(false);
    editorsButton.setEnabled(true);
    editorsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    editorsButton.setForeground(Color.WHITE);
    editorsButton.setToolTipText(_localeManager.getValue("b.editors"));
    editorsButton.addActionListener(action -> {
      showEditorsMenu();
    });
    sidePane.add(editorsButton);

    Icon auctionIcon = IconFontSwing.buildIcon(FontAwesome.SHOPPING_CART, 16, ColorUtil.getForegroundColor());
    auctionButton = new JButton(_localeManager.getValue("b.auction"));
    auctionButton.setIcon(auctionIcon);
    auctionButton.setBounds(28, 375, 125, 35);
    auctionButton.setHorizontalAlignment(SwingConstants.LEFT);
    auctionButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    auctionButton.setFocusPainted(false);
    auctionButton.setFocusable(false);
    auctionButton.setBorderPainted(false);
    auctionButton.setEnabled(true);
    auctionButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    auctionButton.setForeground(Color.WHITE);
    auctionButton.setToolTipText(_localeManager.getValue("b.auction"));
    auctionButton.addActionListener(this.eventHandler::openAuctionsWebpage);
    sidePane.add(auctionButton);
    auctionButton.setVisible(false);

    JButton discordButton = new JButton(ImageUtil.imageStreamToIcon(LauncherGUI.class.getResourceAsStream("/rsrc/img/icon-discord.png")));
    discordButton.setBounds(65, 440, 36, 36);
    discordButton.setToolTipText(_localeManager.getValue("b.discord"));
    discordButton.setFocusPainted(false);
    discordButton.setFocusable(false);
    discordButton.setBorderPainted(false);
    discordButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    sidePane.add(discordButton);
    discordButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DISCORD));

    Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 17, Color.WHITE);
    JButton bugButton = new JButton(bugIcon);
    bugButton.setBounds(107, 440, 36, 36);
    bugButton.setToolTipText(_localeManager.getValue("b.bugs"));
    bugButton.setFocusPainted(false);
    bugButton.setFocusable(false);
    bugButton.setBorderPainted(false);
    bugButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    sidePane.add(bugButton);
    bugButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_BUG_REPORT));

    Icon donateIcon = IconFontSwing.buildIcon(FontAwesome.USD, 17, Color.WHITE);
    JButton donateButton = new JButton(donateIcon);
    donateButton.setBounds(149, 440, 36, 36);
    donateButton.setToolTipText(_localeManager.getValue("b.donate"));
    donateButton.setFocusPainted(false);
    donateButton.setFocusable(false);
    donateButton.setBorderPainted(false);
    donateButton.setBackground(CustomColors.PREMIUM);
    sidePane.add(donateButton);
    donateButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DONATE));

    JLabel launcherVersion = new JLabel("v" + BuildConfig.getVersion() + " ");
    launcherVersion.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
    launcherVersion.setForeground(CustomColors.INTERFACE_SIDEPANE_FOOTNOTE);
    launcherVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    launcherVersion.setBounds(10, 493, 230, 15);
    sidePane.add(launcherVersion);

    Icon timerIcon = IconFontSwing.buildIcon(FontAwesome.CLOCK_O, 16, Color.WHITE);
    bannerTimer = new JLabel("");
    bannerTimer.setIcon(timerIcon);
    bannerTimer.setBounds(40, 50, 225, 25);
    bannerTimer.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    bannerTimer.setForeground(Color.WHITE);
    bannerTimer.setHorizontalAlignment(SwingConstants.CENTER);
    bannerTimer.setVerticalAlignment(SwingConstants.CENTER);
    bannerTimer.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND)
        + "AA; foreground:" + ColorUtil.colorToHexString(Color.WHITE)
        + "; arc:999;");
    bannerTimer.setVisible(false);
    mainPane.add(bannerTimer);

    bannerTitle = new JLabel(_localeManager.getValue("m.banner_title_default"));
    bannerTitle.setBounds(35, -60, 700, 340);
    bannerTitle.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    bannerTitle.setForeground(Color.WHITE);
    bannerTitle.setVisible(false);
    mainPane.add(bannerTitle);

    bannerSubtitle1 = new JLabel(_localeManager.getValue("m.banner_subtitle_default"));
    bannerSubtitle1.setBounds(40, -15, 700, 340);
    bannerSubtitle1.setFont(Fonts.getFont("defaultMedium", 14.0f, Font.PLAIN));
    bannerSubtitle1.setForeground(Color.WHITE);
    bannerSubtitle1.setVisible(false);
    mainPane.add(bannerSubtitle1);

    bannerSubtitle2 = new JLabel("");
    bannerSubtitle2.setBounds(40, 5, 700, 340);
    bannerSubtitle2.setFont(Fonts.getFont("defaultMedium", 14.0f, Font.PLAIN));
    bannerSubtitle2.setForeground(Color.WHITE);
    bannerSubtitle2.setVisible(false);
    mainPane.add(bannerSubtitle2);

    bannerLinkButton = new JButton(_localeManager.getValue("b.learn_more"));
    bannerLinkButton.setBounds(40, 195, 110, 25);
    bannerLinkButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    bannerLinkButton.setForeground(Color.WHITE);
    bannerLinkButton.setFocusPainted(false);
    bannerLinkButton.setFocusable(false);
    bannerLinkButton.setOpaque(false);
    bannerLinkButton.setBackground(CustomColors.INTERFACE_MAINPANE_TRANSPARENT_BUTTON);
    bannerLinkButton.setBorderPainted(false);
    bannerLinkButton.setVisible(false);
    mainPane.add(bannerLinkButton);

    launchButton = new JButton(_localeManager.getValue("b.play"));
    launchButton.setBounds(500, 423, 210, 52);
    launchButton.setFont(Fonts.getFont("defaultMedium", 15.0f, Font.PLAIN));
    launchButton.setFocusPainted(false);
    launchButton.setFocusable(false);
    launchButton.setBackground(CustomColors.LAUNCH);
    launchButton.setBorderPainted(false);
    launchButton.setForeground(Color.WHITE);
    launchButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    launchButton.setToolTipText(_localeManager.getValue("b.play"));
    mainPane.add(launchButton);
    launchButton.addActionListener(action -> {
      if (_keyboardController.isShiftPressed() || _keyboardController.isAltPressed()) {
        // TODO: Consolidate alt launching inside LauncherEventHandler::launchGameEvent for both.
        if (_flamingoManager.getSelectedServer().isOfficial()) {
          this.eventHandler.launchGameAltEvent();
        } else {
          this.eventHandler.launchGameEvent(true);
        }
      } else {
        this.eventHandler.launchGameEvent(false);
      }
    });

    Icon launchPopupMenuIcon = IconFontSwing.buildIcon(FontAwesome.BARS, 24, Color.WHITE);
    launchPopupMenuButton = new JButton(launchPopupMenuIcon);
    launchPopupMenuButton.setBounds(720, 424, 52, 52);
    launchPopupMenuButton.setOpaque(false);
    launchPopupMenuButton.setBorderPainted(false);
    launchPopupMenuButton.setBackground(CustomColors.INTERFACE_MAINPANE_TRANSPARENT_BUTTON);
    launchPopupMenuButton.setToolTipText(_localeManager.getValue("m.launch_popup_tooltip"));
    launchPopupMenuButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    mainPane.add(launchPopupMenuButton);

    JPopupMenu launchPopupMenu = new JPopupMenu();
    launchPopupMenu.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    launchPopupMenu.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    launchPopupMenu.putClientProperty(FlatClientProperties.POPUP_BORDER_CORNER_RADIUS, 5);
    launchPopupMenu.putClientProperty(FlatClientProperties.STYLE,
      "borderColor: " + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND) + "; borderInsets: 5,5,5,5;");

    JMenuItem gameSettingsMenuItem = new JMenuItem(new AbstractAction(_localeManager.getValue("m.game_settings"))
    {
      public void actionPerformed (ActionEvent e)
      {
        eventHandler.gameSettingsEvent();
      }
    });
    gameSettingsMenuItem.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    gameSettingsMenuItem.putClientProperty(FlatClientProperties.STYLE, "margin: 10,10,10,10; selectionArc: 15; selectionBackground: " + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND_FOCUS) + ";");

    JMenuItem repairGameFilesMenuItem = new JMenuItem(new AbstractAction(_localeManager.getValue("m.repair_game_files"))
    {
      public void actionPerformed (ActionEvent e)
      {
        eventHandler.repairGameFilesEvent();
      }
    });
    repairGameFilesMenuItem.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    repairGameFilesMenuItem.putClientProperty(FlatClientProperties.STYLE, "margin: 10,10,10,10; selectionArc: 15; selectionBackground: " + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND_FOCUS) + ";");

    JMenuItem openGameFolderMenuItem = new JMenuItem(new AbstractAction(_localeManager.getValue("b.open_game_dir"))
    {
      public void actionPerformed (ActionEvent e)
      {
        eventHandler.openGameFolderEvent();
      }
    });
    openGameFolderMenuItem.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    openGameFolderMenuItem.putClientProperty(FlatClientProperties.STYLE, "margin: 10,10,10,10; selectionArc: 15; selectionBackground: " + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND_FOCUS) + ";");

    JMenuItem altModeMenuItem = new JMenuItem(new AbstractAction(_localeManager.getValue("m.alt_mode_title"))
    {
      public void actionPerformed (ActionEvent e)
      {
        Dialog.push(
          _localeManager.getValue("m.alt_mode_text"),
          _localeManager.getValue("m.alt_mode_title"),
          JOptionPane.INFORMATION_MESSAGE);
      }
    });
    altModeMenuItem.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    altModeMenuItem.putClientProperty(FlatClientProperties.STYLE, "margin: 10,10,10,10; selectionArc: 15; selectionBackground: " + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND_FOCUS) + ";");

    launchPopupMenu.add(gameSettingsMenuItem);
    launchPopupMenu.add(repairGameFilesMenuItem);
    launchPopupMenu.add(openGameFolderMenuItem);
    launchPopupMenu.add(altModeMenuItem);

    launchPopupMenuButton.addMouseListener(new MouseAdapter()
    {
      public void mousePressed (MouseEvent e)
      {
        if (launchPopupMenuButton.isEnabled()) {
          launchPopupMenu.show(mainPane, 575, 245);
        }
      }
    });

    Icon altModeEnabledIcon = IconFontSwing.buildIcon(FontAwesome.USER_PLUS, 16, Color.WHITE);
    altModeEnabledLabel = new JLabel(_localeManager.getValue("m.alt_mode"));
    altModeEnabledLabel.setIcon(altModeEnabledIcon);
    altModeEnabledLabel.setBounds(540, 480, 130, 20);
    altModeEnabledLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    altModeEnabledLabel.setForeground(Color.WHITE);
    altModeEnabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
    altModeEnabledLabel.setVerticalAlignment(SwingConstants.CENTER);
    altModeEnabledLabel.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND)
        + "AA; foreground:" + ColorUtil.colorToHexString(Color.WHITE)
        + "; arc:999;");
    altModeEnabledLabel.setVisible(false);
    mainPane.add(altModeEnabledLabel);

    BufferedImage launchBackgroundImage = ImageUtil.generatePlainColorImage(446, 80, Color.BLACK);
    launchBackgroundImage = (BufferedImage) ImageUtil.addRoundedCorners(launchBackgroundImage, 35);
    ImageUtil.setAlpha(launchBackgroundImage, (byte) 191);
    launchBackground = new JLabel("");
    launchBackground.setBounds(28, 409, 446, 80);
    launchBackground.setIcon(new ImageIcon(launchBackgroundImage));
    launchBackground.setVisible(false);
    mainPane.add(launchBackground);
    mainPane.setComponentZOrder(launchBackground, 1);

    launchState = new JLabel("");
    launchState.setHorizontalAlignment(SwingConstants.LEFT);
    launchState.setBounds(43, 419, 416, 25);
    launchState.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launchState.setVisible(false);
    mainPane.add(launchState);
    mainPane.setComponentZOrder(launchState, 0);

    launchProgressBar = new JProgressBar();
    launchProgressBar.setBounds(43, 449, 416, 25);
    launchProgressBar.setVisible(false);
    launchProgressBar.putClientProperty(FlatClientProperties.STYLE, "arc: 35;");
    mainPane.add(launchProgressBar);
    mainPane.setComponentZOrder(launchProgressBar, 0);

    Icon changelogIcon = IconFontSwing.buildIcon(FontAwesome.BOOK, 18, Color.WHITE);
    changelogButton = new JButton(changelogIcon);
    changelogButton.setBounds(736, 26, 36, 36);
    changelogButton.setToolTipText(_localeManager.getValue("m.changelog"));
    changelogButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    changelogButton.setFocusPainted(false);
    changelogButton.setFocusable(false);
    changelogButton.setBorderPainted(false);
    changelogButton.setBackground(CustomColors.CHANGELOG);
    changelogButton.setForeground(Color.WHITE);
    changelogButton.setVisible(true);
    mainPane.add(changelogButton);
    changelogButton.addActionListener(l -> this.eventHandler.showLatestChangelog());

    Icon warningNoticeIcon = IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, 16, Color.WHITE);
    warningNotice = new JButton(warningNoticeIcon);
    warningNotice.setBounds(691, 26, 36, 36);
    warningNotice.setToolTipText(_localeManager.getValue("m.warning_notice"));
    warningNotice.setFocusPainted(false);
    warningNotice.setFocusable(false);
    warningNotice.setBorderPainted(false);
    warningNotice.setForeground(Color.WHITE);
    warningNotice.setBackground(CustomColors.LIGHT_RED);
    warningNotice.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    warningNotice.setVisible(false);
    warningNotice.addActionListener(l -> {
      Dialog.push(this.eventHandler.currentWarning, _localeManager.getValue("m.warning_notice"), JOptionPane.ERROR_MESSAGE);
    });
    mainPane.add(warningNotice);

    Icon updateIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 16, Color.WHITE);
    updateButton = new JButton(updateIcon);
    updateButton.setBounds(691, 26, 36, 36);
    updateButton.setToolTipText(_localeManager.getValue("b.update"));
    updateButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    updateButton.setFocusPainted(false);
    updateButton.setFocusable(false);
    updateButton.setBorderPainted(false);
    updateButton.setBackground(CustomColors.UPDATE);
    updateButton.setForeground(Color.WHITE);
    updateButton.setVisible(false);
    mainPane.add(updateButton);
    updateButton.addActionListener(l -> this.eventHandler.updateLauncher(_launcherCtx.launcherGUI.eventHandler.latestRelease));

    Icon playAnimatedBannersIconEnabled = IconFontSwing.buildIcon(FontAwesome.EYE, 18, Color.WHITE);
    Icon playAnimatedBannersIconDisabled = IconFontSwing.buildIcon(FontAwesome.EYE_SLASH, 18, Color.WHITE);
    playAnimatedBannersButton = new JButton(Settings.playAnimatedBanners ? playAnimatedBannersIconEnabled : playAnimatedBannersIconDisabled);
    playAnimatedBannersButton.setBounds(736, 71, 36, 36);
    playAnimatedBannersButton.setToolTipText(_localeManager.getValue(Settings.playAnimatedBanners ? "m.animated_banners_disable" : "m.animated_banners_enable"));
    playAnimatedBannersButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    playAnimatedBannersButton.setFocusPainted(false);
    playAnimatedBannersButton.setFocusable(false);
    playAnimatedBannersButton.setBorderPainted(false);
    playAnimatedBannersButton.setBackground(Settings.playAnimatedBanners ? CustomColors.INTERFACE_BUTTON_BACKGROUND : CustomColors.LIGHT_RED);
    playAnimatedBannersButton.setForeground(Color.WHITE);
    playAnimatedBannersButton.setVisible(false);
    mainPane.add(playAnimatedBannersButton);
    playAnimatedBannersButton.addActionListener(l -> this.eventHandler.switchBannerAnimations());

    closeButton.addActionListener(e -> {
      _launcherCtx.exit(true);
    });
    closeButton.setToolTipText(_localeManager.getValue("b.close"));
    minimizeButton.setToolTipText(_localeManager.getValue("b.minimize"));

    guiFrame.setLocationRelativeTo(null);

    guiFrame.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosed (WindowEvent windowEvent)
      {
        _discordPresenceClient.stop();
      }
    });

  }

  public void showWarning (String message)
  {
    // we're also showing an available update, let's move the warning notice
    // slightly to the left, so they don't overlap.
    if (updateButton.isVisible()) {
      warningNotice.setBounds(warningNotice.getX() - 45, 26, 35, 35);
    }

    warningNotice.setVisible(true);
    this.eventHandler.currentWarning = message;
  }

  public BufferedImage processImageForBanner (BufferedImage image, double intensity)
  {
    image = ImageUtil.resizeImage(image, 800, 550);
    image = ImageUtil.fadeEdges(image, intensity);
    return image;
  }

  public void processAnimatedImageForBanner (byte[] gifData, double intensity)
  {
    try {
      final GifDecoder.GifImage gif = GifDecoder.read(gifData);
      final int frameCount = gif.getFrameCount();
      final java.util.List<BufferedImage> proccesedImages = new ArrayList<>();

      // process every single frame of the GIF.
      for (int i = 0; i < frameCount; i++) {
        BufferedImage frame = gif.getFrame(i);
        frame = ImageUtil.resizeImage(frame, 800, 550);
        if (intensity > 0) ImageUtil.fadeEdges(frame, intensity);
        proccesedImages.add(frame);
      }

      this.eventHandler.displayingAnimBanner = true;
      new Thread(() -> {
        while (this.eventHandler.displayingAnimBanner) {
          for (int i = 0; i < proccesedImages.size(); i++) {
            // we might need to end prematurely to avoid concurrent modifications.
            if (!this.eventHandler.displayingAnimBanner) break;

            try {
              Thread.sleep(gif.getDelay(i) * 10L);
            } catch (InterruptedException e) {
              log.error(e);
            }

            if (Settings.playAnimatedBanners) {
              // set the new frame.
              banner = proccesedImages.get(i);
              mainPane.repaint();
            } else {
              banner = proccesedImages.get(0);
              mainPane.repaint();
            }
          }
        }
      }).start();

    } catch (IOException e) {
      log.error(e);
    }
  }

  public void showSettingsMenu ()
  {
    mainPane.setVisible(false);
    layeredModsPane.setVisible(false);
    layeredEditorsPane.setVisible(false);

    layeredSettingsPane = _launcherCtx.settingsGUI.tabbedPane;
    layeredSettingsPane.setBounds(300, 75, 800, 550);
    guiFrame.add(layeredSettingsPane);
    layeredSettingsPane.setVisible(true);
    returnButton.setVisible(true);
  }

  public void showModsMenu ()
  {
    mainPane.setVisible(false);
    layeredSettingsPane.setVisible(false);
    layeredEditorsPane.setVisible(false);

    layeredModsPane = _launcherCtx.modListGUI.modListPanel;
    layeredModsPane.setBounds(300, 75, 800, 550);
    guiFrame.add(layeredModsPane);
    layeredModsPane.setVisible(true);
    returnButton.setVisible(true);
  }

  public void showEditorsMenu ()
  {
    mainPane.setVisible(false);
    layeredSettingsPane.setVisible(false);
    layeredModsPane.setVisible(false);

    layeredEditorsPane = _launcherCtx.editorsGUI.editorsPanel;
    layeredEditorsPane.setBounds(300, 75, 800, 550);
    guiFrame.add(layeredEditorsPane);
    layeredEditorsPane.setVisible(true);
    returnButton.setVisible(true);
  }

  public void returnToHome ()
  {
    layeredSettingsPane.setVisible(false);
    layeredModsPane.setVisible(false);
    layeredEditorsPane.setVisible(false);
    returnButton.setVisible(false);

    mainPane.setVisible(true);
  }

  protected void specialKeyPressed ()
  {
    launchButton.setBackground(CustomColors.LAUNCH_ALT);
    launchButton.updateUI();
    altModeEnabledLabel.setVisible(true);
  }

  protected void specialKeyReleased ()
  {
    launchButton.setBackground(CustomColors.LAUNCH);
    launchButton.updateUI();
    altModeEnabledLabel.setVisible(false);
  }

  // Shared
  public JTabbedPane layeredSettingsPane = new JTabbedPane();
  public JPanel layeredModsPane = new JPanel();
  public JPanel layeredEditorsPane = new JPanel();

  // Server switcher pane
  public JPanel serverSwitcherPane;
  public JScrollPane serverSwitcherPaneScrollBar;

  // Side pane
  public JButton settingsButton;
  public JButton modButton;
  public JButton editorsButton;
  public JButton auctionButton;
  public JButton playerCountTooltipButton;
  public JLabel playerCountLabel;
  public JLabel selectedServerLabel;
  public JButton serverInfoButton;

  // Main pane
  public JPanel mainPane;
  public BufferedImage banner = null;
  public JLabel bannerLoading;
  public JLabel bannerTimer;
  public JLabel bannerTitle;
  public JLabel bannerSubtitle1;
  public JLabel bannerSubtitle2;
  public JButton bannerLinkButton;
  public JButton launchButton;
  public JButton launchPopupMenuButton;
  public JButton updateButton;
  public JButton changelogButton;
  public JButton playAnimatedBannersButton;
  public JLabel launchBackground;
  public JLabel launchState;
  public JProgressBar launchProgressBar = new JProgressBar();
  public JButton warningNotice;
  public JLabel altModeEnabledLabel;

}
