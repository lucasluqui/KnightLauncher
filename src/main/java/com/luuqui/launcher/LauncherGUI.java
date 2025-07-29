package com.luuqui.launcher;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static com.luuqui.launcher.Log.log;

public class LauncherGUI extends BaseGUI {

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

  /** @wbp.parser.entryPoint */
  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.main", LauncherGlobals.LAUNCHER_VERSION));
    guiFrame.setResizable(false);
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    guiFrame.setUndecorated(true);
    guiFrame.setIconImage(ImageUtil.loadImageWithinJar("/rsrc/img/icon-256.png"));
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().setLayout(null);

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

    banner = ImageUtil.generatePlainColorImage(800, 550, CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    mainPane = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
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
    bannerLoading.setBounds(49, 65, 700, 340);
    bannerLoading.setFont(Fonts.fontMedBig);
    bannerLoading.setHorizontalAlignment(SwingConstants.CENTER);
    bannerLoading.setVerticalAlignment(SwingConstants.CENTER);
    mainPane.add(bannerLoading);
    mainPane.setComponentZOrder(bannerLoading, 0);

    JLabel launcherLogo = new JLabel();
    BufferedImage launcherLogoImage = ImageUtil.loadImageWithinJar("/rsrc/img/icon-92.png");
    BufferedImage launcherLogoImageHover = ImageUtil.loadImageWithinJar("/rsrc/img/icon-92-hover.png");
    launcherLogo.setBounds(0, -27, 251, 200);
    launcherLogo.setHorizontalAlignment(SwingConstants.CENTER);
    launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
    sidePane.add(launcherLogo);
    launcherLogo.addMouseListener(new MouseListener() {
      @Override public void mouseClicked(MouseEvent e) {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      }
      @Override public void mousePressed(MouseEvent e) {}
      @Override public void mouseReleased(MouseEvent e) {}
      @Override public void mouseEntered(MouseEvent e) {
        launcherLogo.setIcon(new ImageIcon(launcherLogoImageHover));
        launcherLogo.updateUI();
      }
      @Override public void mouseExited(MouseEvent e) {
        launcherLogo.setIcon(new ImageIcon(launcherLogoImage));
        launcherLogo.updateUI();
      }
    });

    JLabel launcherName = new JLabel(LauncherGlobals.LAUNCHER_NAME);
    launcherName.setFont(Fonts.fontMedBig);
    launcherName.setHorizontalAlignment(SwingConstants.CENTER);
    launcherName.setVerticalAlignment(SwingConstants.CENTER);
    launcherName.setBounds(0, 100, 250, 80);
    sidePane.add(launcherName);

    selectedServerLabel = new JLabel("Official");
    selectedServerLabel.setFont(Fonts.fontMed);
    selectedServerLabel.setVisible(true);
    selectedServerLabel.setBounds(28, 185, 120, 20);
    sidePane.add(selectedServerLabel);

    serverInfoButton = new JButton();
    serverInfoButton.setBounds(28, 185, 130, 20);
    serverInfoButton.setEnabled(false);
    serverInfoButton.setVisible(false);
    serverInfoButton.setFocusable(false);
    serverInfoButton.setFocusPainted(false);
    serverInfoButton.setForeground(Color.WHITE);
    serverInfoButton.setToolTipText(_localeManager.getValue("m.server_info"));
    serverInfoButton.addActionListener(l -> this.eventHandler.displaySelectedServerInfo());
    sidePane.add(serverInfoButton);

    Icon playerCountIcon = IconFontSwing.buildIcon(FontAwesome.USERS, 14, CustomColors.INTERFACE_DEFAULT);
    playerCountLabel = new JLabel(_localeManager.getValue("m.players_online_load"));
    playerCountLabel.setFont(Fonts.fontReg);
    playerCountLabel.setIcon(playerCountIcon);
    playerCountLabel.setBounds(28, 210, 200, 18);
    sidePane.add(playerCountLabel);

    String playerCountTooltipTitle = _localeManager.getValue("m.players_online");
    String playerCountTooltipText = _localeManager.getValue("m.players_online_text");
    Icon playerCountTooltipButtonIcon = IconFontSwing.buildIcon(FontAwesome.QUESTION, 12, Color.WHITE);
    playerCountTooltipButton = new JButton();
    playerCountTooltipButton.setIcon(playerCountTooltipButtonIcon);
    playerCountTooltipButton.setBounds(173, 213, 13, 13);
    playerCountTooltipButton.setEnabled(true);
    playerCountTooltipButton.setFocusable(false);
    playerCountTooltipButton.setFocusPainted(false);
    playerCountTooltipButton.setBorderPainted(false);
    playerCountTooltipButton.setForeground(Color.WHITE);
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
    settingsButton.setFont(Fonts.fontMed);
    settingsButton.setFocusPainted(false);
    settingsButton.setFocusable(false);
    settingsButton.setBorderPainted(false);
    settingsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    settingsButton.setForeground(Color.WHITE);
    settingsButton.setToolTipText(_localeManager.getValue("b.settings"));
    settingsButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredModsPane.setVisible(false);
      layeredEditorsPane.setVisible(false);

      layeredSettingsPane = _launcherCtx.settingsGUI.tabbedPane;
      layeredSettingsPane.setBounds(300, 75, 800, 550);
      guiFrame.add(layeredSettingsPane);
      layeredSettingsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(305, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      guiFrame.add(layeredReturnButton);
    });
    sidePane.add(settingsButton);

    Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, 16, ColorUtil.getForegroundColor());
    modButton = new JButton(_localeManager.getValue("b.mods"));
    modButton.setIcon(modsIcon);
    modButton.setBounds(28, 315, 125, 35);
    modButton.setHorizontalAlignment(SwingConstants.LEFT);
    modButton.setFont(Fonts.fontMed);
    modButton.setFocusPainted(false);
    modButton.setFocusable(false);
    modButton.setBorderPainted(false);
    modButton.setEnabled(true);
    modButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    modButton.setForeground(Color.WHITE);
    modButton.setToolTipText(_localeManager.getValue("b.mods"));
    modButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredSettingsPane.setVisible(false);
      layeredEditorsPane.setVisible(false);

      layeredModsPane = _launcherCtx.modListGUI.modListPanel;
      layeredModsPane.setBounds(300, 75, 800, 550);
      guiFrame.add(layeredModsPane);
      layeredModsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(305, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      guiFrame.add(layeredReturnButton);
    });
    sidePane.add(modButton);

    Icon editorsIcon = IconFontSwing.buildIcon(FontAwesome.PENCIL, 16, ColorUtil.getForegroundColor());
    editorsButton = new JButton(_localeManager.getValue("b.editors"));
    editorsButton.setIcon(editorsIcon);
    editorsButton.setBounds(28, 355, 125, 35);
    editorsButton.setHorizontalAlignment(SwingConstants.LEFT);
    editorsButton.setFont(Fonts.fontMed);
    editorsButton.setFocusPainted(false);
    editorsButton.setFocusable(false);
    editorsButton.setBorderPainted(false);
    editorsButton.setEnabled(true);
    editorsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    editorsButton.setForeground(Color.WHITE);
    editorsButton.setToolTipText(_localeManager.getValue("b.editors"));
    editorsButton.addActionListener(action -> {
      mainPane.setVisible(false);
      layeredSettingsPane.setVisible(false);
      layeredModsPane.setVisible(false);

      layeredEditorsPane = _launcherCtx.editorsGUI.editorsPanel;
      layeredEditorsPane.setBounds(300, 75, 800, 550);
      guiFrame.add(layeredEditorsPane);
      layeredEditorsPane.setVisible(true);

      layeredReturnButton = new JButton(IconFontSwing.buildIcon(FontAwesome.ARROW_LEFT, 12, Color.WHITE));
      layeredReturnButton.setBounds(305, 40, 25, 25);
      layeredReturnButton.setVisible(true);
      layeredReturnButton.setFocusable(false);
      layeredReturnButton.setFocusPainted(false);
      layeredReturnButton.setBorder(null);
      layeredReturnButton.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
      layeredReturnButton.addActionListener(l -> {
        layeredSettingsPane.setVisible(false);
        layeredModsPane.setVisible(false);
        layeredEditorsPane.setVisible(false);
        mainPane.setVisible(true);
        layeredReturnButton.setVisible(false);
      });
      guiFrame.add(layeredReturnButton);
    });
    sidePane.add(editorsButton);

    Icon auctionIcon = IconFontSwing.buildIcon(FontAwesome.SHOPPING_CART, 16, ColorUtil.getForegroundColor());
    auctionButton = new JButton(_localeManager.getValue("b.auction"));
    auctionButton.setIcon(auctionIcon);
    auctionButton.setBounds(28, 375, 125, 35);
    auctionButton.setHorizontalAlignment(SwingConstants.LEFT);
    auctionButton.setFont(Fonts.fontMed);
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
    discordButton.setFont(Fonts.fontMed);
    sidePane.add(discordButton);
    discordButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DISCORD));

    Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, 17, Color.WHITE);
    JButton bugButton = new JButton(bugIcon);
    bugButton.setBounds(107, 440, 36, 36);
    bugButton.setToolTipText(_localeManager.getValue("b.bug_report"));
    bugButton.setFocusPainted(false);
    bugButton.setFocusable(false);
    bugButton.setBorderPainted(false);
    bugButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    bugButton.setFont(Fonts.fontMed);
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
    donateButton.setFont(Fonts.fontMed);
    sidePane.add(donateButton);
    donateButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DONATE));

    JLabel launcherVersion = new JLabel("v" + LauncherGlobals.LAUNCHER_VERSION);
    launcherVersion.setFont(Fonts.fontRegSmall);
    launcherVersion.setForeground(CustomColors.INTERFACE_SIDEPANE_FOOTNOTE);
    launcherVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    launcherVersion.setBounds(10, 493, 230, 15);
    sidePane.add(launcherVersion);

    Icon timerIcon = IconFontSwing.buildIcon(FontAwesome.CLOCK_O, 16, Color.WHITE);
    bannerTimer = new JLabel("");
    bannerTimer.setIcon(timerIcon);
    bannerTimer.setBounds(40, 50, 225, 25);
    bannerTimer.setFont(Fonts.fontReg);
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
    bannerTitle.setFont(Fonts.fontMedGiant);
    bannerTitle.setForeground(Color.WHITE);
    bannerTitle.setVisible(false);
    mainPane.add(bannerTitle);

    bannerSubtitle1 = new JLabel(_localeManager.getValue("m.banner_subtitle_default"));
    bannerSubtitle1.setBounds(40, -15, 700, 340);
    bannerSubtitle1.setFont(Fonts.fontMedBig);
    bannerSubtitle1.setForeground(Color.WHITE);
    bannerSubtitle1.setVisible(false);
    mainPane.add(bannerSubtitle1);

    bannerSubtitle2 = new JLabel("");
    bannerSubtitle2.setBounds(40, 5, 700, 340);
    bannerSubtitle2.setFont(Fonts.fontMedBig);
    bannerSubtitle2.setForeground(Color.WHITE);
    bannerSubtitle2.setVisible(false);
    mainPane.add(bannerSubtitle2);

    bannerLinkButton = new JButton(_localeManager.getValue("b.learn_more"));
    bannerLinkButton.setBounds(40, 195, 110, 25);
    bannerLinkButton.setFont(Fonts.fontMed);
    bannerLinkButton.setForeground(Color.WHITE);
    bannerLinkButton.setFocusPainted(false);
    bannerLinkButton.setFocusable(false);
    bannerLinkButton.setOpaque(false);
    bannerLinkButton.setBackground(CustomColors.INTERFACE_MAINPANE_TRANSPARENT_BUTTON);
    bannerLinkButton.setBorderPainted(false);
    bannerLinkButton.setVisible(false);
    mainPane.add(bannerLinkButton);

    launchButton = new JButton(_localeManager.getValue("b.play"));
    launchButton.setBounds(572, 423, 200, 66);
    launchButton.setFont(Fonts.fontMedBig);
    launchButton.setFocusPainted(false);
    launchButton.setFocusable(false);
    launchButton.setBackground(CustomColors.LAUNCH);
    launchButton.setBorderPainted(false);
    launchButton.setForeground(Color.WHITE);
    launchButton.setToolTipText(_localeManager.getValue("b.play"));
    mainPane.add(launchButton);
    launchButton.addActionListener(action -> {
      if (_keyboardController.isShiftPressed() || _keyboardController.isAltPressed()) {
        // TODO: Consolidate alt launching inside LauncherEventHandler::launchGameEvent for both.
        if(_flamingoManager.getSelectedServer().isOfficial()) {
          this.eventHandler.launchGameAltEvent();
        } else {
          this.eventHandler.launchGameEvent(true);
        }
      } else {
        this.eventHandler.launchGameEvent(false);
      }
    });

    String launchTooltipTitle = _localeManager.getValue("m.alt_mode");
    String launchTooltipText = _localeManager.getValue("m.alt_mode_text");
    Icon launchTooltipButtonIcon = IconFontSwing.buildIcon(FontAwesome.QUESTION, 16, Color.WHITE);
    JButton launchTooltipButton = new JButton();
    launchTooltipButton.setIcon(launchTooltipButtonIcon);
    launchTooltipButton.setBounds(548, 424, 20, 20);
    launchTooltipButton.setEnabled(true);
    launchTooltipButton.setFocusable(false);
    launchTooltipButton.setFocusPainted(false);
    launchTooltipButton.setBorderPainted(false);
    launchTooltipButton.setBackground(CustomColors.INTERFACE_MAINPANE_TRANSPARENT_BUTTON);
    launchTooltipButton.setForeground(Color.WHITE);
    launchTooltipButton.setToolTipText(launchTooltipTitle);
    launchTooltipButton.addActionListener(l -> {
      Dialog.push(launchTooltipText, launchTooltipTitle, JOptionPane.INFORMATION_MESSAGE);
    });
    mainPane.add(launchTooltipButton);

    Icon altModeIcon = IconFontSwing.buildIcon(FontAwesome.USER_PLUS, 16, Color.WHITE);
    altModeEnabledPill = new JLabel(_localeManager.getValue("m.alt_mode_enabled"));
    altModeEnabledPill.setIcon(altModeIcon);
    altModeEnabledPill.setBounds(572, 399, 200, 20);
    altModeEnabledPill.setFont(Fonts.fontReg);
    altModeEnabledPill.setForeground(Color.WHITE);
    altModeEnabledPill.setHorizontalAlignment(SwingConstants.CENTER);
    altModeEnabledPill.setVerticalAlignment(SwingConstants.CENTER);
    altModeEnabledPill.putClientProperty(FlatClientProperties.STYLE,
        "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND)
            + "AA; foreground:" + ColorUtil.colorToHexString(Color.WHITE)
            + "; arc:999;");
    altModeEnabledPill.setVisible(false);
    mainPane.add(altModeEnabledPill);

    BufferedImage launchBackgroundImage = ImageUtil.generatePlainColorImage(500, 85, new Color(0, 0, 0));
    launchBackgroundImage = (BufferedImage) ImageUtil.addRoundedCorners(launchBackgroundImage, 25);
    ImageUtil.setAlpha(launchBackgroundImage, (byte) 191);
    launchBackground = new JLabel("");
    launchBackground.setBounds(20, 410, 500, 85);
    launchBackground.setIcon(new ImageIcon(launchBackgroundImage));
    launchBackground.setVisible(false);
    mainPane.add(launchBackground);
    mainPane.setComponentZOrder(launchBackground, 1);

    launchState = new JLabel("");
    launchState.setHorizontalAlignment(SwingConstants.LEFT);
    launchState.setBounds(35, 420, 505, 25);
    launchState.setFont(Fonts.fontRegBig);
    launchState.setVisible(false);
    mainPane.add(launchState);
    mainPane.setComponentZOrder(launchState, 0);

    launchProgressBar = new JProgressBar();
    launchProgressBar.setBounds(35, 450, 470, 25);
    launchProgressBar.setVisible(false);
    mainPane.add(launchProgressBar);
    mainPane.setComponentZOrder(launchProgressBar, 0);

    Icon changelogIcon = IconFontSwing.buildIcon(FontAwesome.BOOK, 18, Color.WHITE);
    changelogButton = new JButton(changelogIcon);
    changelogButton.setBounds(736, 26, 36, 36);
    changelogButton.setToolTipText(_localeManager.getValue("m.changelog"));
    changelogButton.setFont(Fonts.fontMed);
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
    warningNotice.setFont(Fonts.fontMed);
    warningNotice.setVisible(false);
    warningNotice.addActionListener(l -> {
      Dialog.push(this.eventHandler.currentWarning, _localeManager.getValue("m.warning_notice"), JOptionPane.ERROR_MESSAGE);
    });
    mainPane.add(warningNotice);

    Icon updateIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, 16, Color.WHITE);
    updateButton = new JButton(updateIcon);
    updateButton.setBounds(691, 26, 36, 36);
    updateButton.setToolTipText(_localeManager.getValue("b.update_launcher"));
    updateButton.setFont(Fonts.fontMed);
    updateButton.setFocusPainted(false);
    updateButton.setFocusable(false);
    updateButton.setBorderPainted(false);
    updateButton.setBackground(CustomColors.UPDATE);
    updateButton.setForeground(Color.WHITE);
    updateButton.setVisible(false);
    mainPane.add(updateButton);
    updateButton.addActionListener(l -> this.eventHandler.updateLauncher());

    Icon playAnimatedBannersIconEnabled = IconFontSwing.buildIcon(FontAwesome.EYE, 18, Color.WHITE);
    Icon playAnimatedBannersIconDisabled = IconFontSwing.buildIcon(FontAwesome.EYE_SLASH, 18, Color.WHITE);
    playAnimatedBannersButton = new JButton(Settings.playAnimatedBanners ? playAnimatedBannersIconEnabled : playAnimatedBannersIconDisabled);
    playAnimatedBannersButton.setBounds(736, 71, 36, 36);
    playAnimatedBannersButton.setToolTipText(_localeManager.getValue(Settings.playAnimatedBanners ? "m.animated_banners_disable" : "m.animated_banners_enable"));
    playAnimatedBannersButton.setFont(Fonts.fontMed);
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

    guiFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
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
        if(intensity > 0) ImageUtil.fadeEdges(frame, intensity);
        proccesedImages.add(frame);
      }

      this.eventHandler.displayingAnimBanner = true;
      new Thread(() -> {
        while (this.eventHandler.displayingAnimBanner) {
          for (int i = 0; i < proccesedImages.size(); i++) {
            // we might need to end prematurely to avoid concurrent modifications.
            if (!this.eventHandler.displayingAnimBanner) break;

            try {
              Thread.sleep(gif.getDelay(i) * 10);
            } catch (InterruptedException e) {
              log.error(e);
            }

            if(Settings.playAnimatedBanners) {
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

  public void setOnTop ()
  {
    layeredSettingsPane.setVisible(false);
    layeredModsPane.setVisible(false);
    layeredEditorsPane.setVisible(false);
    layeredReturnButton.setVisible(false);

    mainPane.setVisible(true);
  }

  protected void specialKeyPressed ()
  {
    launchButton.setBackground(CustomColors.LAUNCH_ALT);
    launchButton.updateUI();
    altModeEnabledPill.setVisible(true);
  }

  protected void specialKeyReleased ()
  {
    launchButton.setBackground(CustomColors.LAUNCH);
    launchButton.updateUI();
    altModeEnabledPill.setVisible(false);
  }

  // Shared
  public JTabbedPane layeredSettingsPane = new JTabbedPane();
  public JPanel layeredModsPane = new JPanel();
  public JPanel layeredEditorsPane = new JPanel();
  public JButton layeredReturnButton;

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
  public JButton updateButton;
  public JButton changelogButton;
  public JButton playAnimatedBannersButton;
  public JLabel launchBackground;
  public JLabel launchState;
  public JProgressBar launchProgressBar = new JProgressBar();
  public JButton warningNotice;
  public JLabel altModeEnabledPill;

}
