package com.lucasluqui.launcher.ui;

import com.lucasluqui.swing.Scale;
import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.launcher.*;
import com.lucasluqui.launcher.editor.ui.EditorsUI;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.mod.ui.ModListUI;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.ui.SettingsUI;
import com.lucasluqui.launcher.ui.handler.LauncherEventHandler;
import com.lucasluqui.swing.SmoothProgressBar;
import com.lucasluqui.swing.SlimProgressBarUI;
import com.lucasluqui.util.*;
import com.lucasluqui.swing.LoadingIcon;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import org.madlonkay.desktopsupport.DesktopSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.function.BooleanSupplier;

import static com.lucasluqui.launcher.ui.Log.log;

public class LauncherUI extends BaseUI
{
  @Inject
  public LauncherUI ()
  {
    super(WIDTH, HEIGHT, true);
  }

  public void init ()
  {
    super.init();
    try {
      // macOS shenanigans to get the app to have a dock image.
      // Tends to fail on anything that's not 26+ (Tahoe).
      if (SystemUtil.isMac()) {
        try {
          Image image = Toolkit.getDefaultToolkit().getImage(
            LauncherGlobals.USER_DIR + File.separator + "KnightLauncher" + File.separator + "images" + File.separator + "icon-512.png"
          );
          DesktopSupport.getSupport().setDockIconImage(image);
        } catch (Exception e) {
          log.error("Failed to set macOS dock image", e);
        }
      }

      // Now proceed to create the main launcher UI.
      compose();
      initFinished();
    } catch (UnsatisfiedLinkError e) {
      // Some Windows installations don't allow you to write to the default temp dir and throw this error instead
      // when trying to set up any UI. Let's divert the temp directory to a custom one.
      log.error(e);
      SystemUtil.fixTempDir(LauncherGlobals.USER_DIR + "/KnightLauncher/temp/");
      compose();
      initFinished();
    }
  }

  public void initFinished ()
  {
    super.initFinished();
  }

  @Override
  public void loadOnline ()
  {

  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.main", BuildConfig.getVersion()));
    guiFrame.setResizable(false);
    guiFrame.setBounds(Scale.rect(100, 100, this.width, this.height));
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    guiFrame.setUndecorated(true);
    guiFrame.setIconImage(ImageUtil.loadImageWithinJar("/rsrc/img/icon-256.png"));
    guiFrame.setShape(new RoundRectangle2D.Double(0, 0, Scale.px(this.width), Scale.px(this.height), Scale.px(15), Scale.px(15)));
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().setLayout(null);

    guiFrame.addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        _kbController.clear();
      }
    });

    returnButton.setBounds(Scale.rect(305, 40, 25, 25));
    returnButton.setToolTipText(_localeManager.getValue("b.back"));
    returnButton.addActionListener(l -> {
      returnToHome();
    });

    serverSwitcherPane = new JPanel();
    serverSwitcherPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    serverSwitcherPane.setVisible(true);
    serverSwitcherPane.setBounds(Scale.rect(0, 42, 50, 550));
    guiFrame.getContentPane().add(serverSwitcherPane);

    serverSwitcherPaneScrollBar = new JScrollPane(serverSwitcherPane);
    serverSwitcherPaneScrollBar.setBounds(Scale.rect(0, 42, 50, 550));
    serverSwitcherPaneScrollBar.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    serverSwitcherPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    serverSwitcherPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    serverSwitcherPaneScrollBar.setBorder(null);
    serverSwitcherPaneScrollBar.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    serverSwitcherPaneScrollBar.getVerticalScrollBar().setUnitIncrement(Scale.px(16));
    serverSwitcherPaneScrollBar.setVisible(true);
    guiFrame.getContentPane().add(serverSwitcherPaneScrollBar);

    JPanel sidePane = new JPanel();
    sidePane.setBackground(CustomColors.INTERFACE_SIDEPANE_BACKGROUND);
    sidePane.setVisible(true);
    sidePane.setLayout(null);
    sidePane.setBounds(Scale.rect(50, 35, 250, 550));
    guiFrame.getContentPane().add(sidePane);

    // Sized to fill the banner area like the real banners, which get resized for the UI scale too.
    banner = ImageUtil.resizeImagePreserveTransparency(
      ImageUtil.loadImageWithinJar("/rsrc/img/banner-loading.png"), Scale.px(800), Scale.px(550));

    panel = new JPanel()
    {
      @Override
      protected void paintComponent (Graphics g)
      {
        super.paintComponent(g);
        BufferedImage previous = previousBanner;
        if (previous != null && bannerFade < 1f) {
          // Mid cross-fade, draw the new banner on top of the old one with increasing opacity.
          Graphics2D g2d = (Graphics2D) g.create();
          g2d.drawImage(previous, 0, 0, null);
          g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bannerFade));
          g2d.drawImage(banner, 0, 0, null);
          g2d.dispose();
        } else {
          g.drawImage(banner, 0, 0, null);
        }
      }
    };
    panel.setLayout(null);
    panel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    panel.setBounds(Scale.rect(300, 35, 800, 550));
    guiFrame.getContentPane().add(panel);

    bannerLoading = new JLabel(_localeManager.getValue("m.loading"));
    bannerLoading.setIcon(new LoadingIcon());
    bannerLoading.setBounds(Scale.rect(325, 235, 150, 45));
    bannerLoading.setFont(Fonts.getFont("default", 14.0f, Font.BOLD));
    bannerLoading.setHorizontalAlignment(SwingConstants.CENTER);
    bannerLoading.setVerticalAlignment(SwingConstants.CENTER);
    bannerLoading.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND)
        + "AA; foreground:" + ColorUtil.colorToHexString(Color.WHITE)
        + "; arc:999;");
    panel.add(bannerLoading);
    panel.setComponentZOrder(bannerLoading, 0);

    JLabel launcherLogo = new JLabel();
    // Shrunk down from a big version so it stays sharp at any UI scale. Hovering brightens it up a bit.
    BufferedImage launcherLogoImage = ImageUtil.resizeImagePreserveTransparency(
      ImageUtil.loadImageWithinJar("/rsrc/img/icon-256.png"), Scale.px(92), Scale.px(92));
    BufferedImage launcherLogoImageHover = ImageUtil.brighten(launcherLogoImage, 15);
    launcherLogo.setBounds(Scale.rect(0, -27, 251, 200));
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
    launcherName.setFont(Fonts.getFont("default", 15.0f, Font.BOLD));
    launcherName.setHorizontalAlignment(SwingConstants.CENTER);
    launcherName.setVerticalAlignment(SwingConstants.CENTER);
    launcherName.setBounds(Scale.rect(0, 100, 250, 80));
    sidePane.add(launcherName);

    selectedServerLabel = new JLabel("Official");
    selectedServerLabel.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    selectedServerLabel.setVisible(false);
    selectedServerLabel.setBounds(Scale.rect(28, 185, 120, 20));
    sidePane.add(selectedServerLabel);

    serverInfoButton = new JButton();
    serverInfoButton.setBounds(Scale.rect(35, 213, 178, 25));
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

    Icon playerCountTitleIcon = IconFontSwing.buildIcon(FontAwesome.USERS, Scale.px(17), CustomColors.INTERFACE_DEFAULT);
    JLabel playerCountTitleLabel = new JLabel(_localeManager.getValue("m.players_online"));
    playerCountTitleLabel.setIcon(playerCountTitleIcon);
    playerCountTitleLabel.setBounds(Scale.rect(35, 188, 135, 20));
    playerCountTitleLabel.setFont(Fonts.getFont("default", 13.0f, Font.BOLD | Font.ITALIC));
    sidePane.add(playerCountTitleLabel);

    playerCountLabel = new JLabel("");
    playerCountLabel.setIcon(new LoadingIcon(16));
    playerCountLabel.setBounds(Scale.rect(25, 188, 190, 20));
    playerCountLabel.setFont(Fonts.getFont("default", 13.0f, Font.BOLD | Font.ITALIC));
    playerCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    playerCountLabel.setVisible(false);
    sidePane.add(playerCountLabel);

    playerCountTooltipButton = new JButton(_localeManager.getValue("b.learn_more"));
    playerCountTooltipButton.setBounds(Scale.rect(35, 213, 178, 25));
    playerCountTooltipButton.setEnabled(true);
    playerCountTooltipButton.setFocusable(false);
    playerCountTooltipButton.setFocusPainted(false);
    playerCountTooltipButton.setBorderPainted(false);
    playerCountTooltipButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    playerCountTooltipButton.setForeground(Color.WHITE);
    playerCountTooltipButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    updatePlayerCountTooltip(null);
    sidePane.add(playerCountTooltipButton);
    playerCountTooltipButton.setVisible(false);

    // silverweave button needs the group to sit higher to stay centered between its neighbors.
    int sideButtonsY = 260;

    Icon settingsIcon = IconFontSwing.buildIcon(FontAwesome.COGS, Scale.px(16), ColorUtil.getForegroundColor());
    settingsButton = new JButton(_localeManager.getValue("b.settings"));
    settingsButton.setIcon(settingsIcon);
    settingsButton.setBounds(Scale.rect(28, sideButtonsY, 125, 35));
    settingsButton.setHorizontalAlignment(SwingConstants.LEFT);
    settingsButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    settingsButton.setFocusPainted(false);
    settingsButton.setFocusable(false);
    settingsButton.setBorderPainted(false);
    settingsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    settingsButton.setForeground(Color.WHITE);
    settingsButton.setToolTipText(_localeManager.getValue("b.settings"));
    settingsButton.addActionListener(action -> {
      _ctx.getApp().showUI(SettingsUI.class);
    });
    sidePane.add(settingsButton);

    Icon modsIcon = IconFontSwing.buildIcon(FontAwesome.PUZZLE_PIECE, Scale.px(16), ColorUtil.getForegroundColor());
    modButton = new JButton(_localeManager.getValue("b.mods"));
    modButton.setIcon(modsIcon);
    modButton.setBounds(Scale.rect(28, sideButtonsY + 40, 125, 35));
    modButton.setHorizontalAlignment(SwingConstants.LEFT);
    modButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    modButton.setFocusPainted(false);
    modButton.setFocusable(false);
    modButton.setBorderPainted(false);
    modButton.setEnabled(true);
    modButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    modButton.setForeground(Color.WHITE);
    modButton.setToolTipText(_localeManager.getValue("b.mods"));
    modButton.addActionListener(action -> {
      _ctx.getApp().showUI(ModListUI.class);
    });
    sidePane.add(modButton);

    Icon editorsIcon = IconFontSwing.buildIcon(FontAwesome.PENCIL, Scale.px(16), ColorUtil.getForegroundColor());
    editorsButton = new JButton(_localeManager.getValue("b.editors"));
    editorsButton.setIcon(editorsIcon);
    editorsButton.setBounds(Scale.rect(28, sideButtonsY + 80, 125, 35));
    editorsButton.setHorizontalAlignment(SwingConstants.LEFT);
    editorsButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    editorsButton.setFocusPainted(false);
    editorsButton.setFocusable(false);
    editorsButton.setBorderPainted(false);
    editorsButton.setEnabled(true);
    editorsButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    editorsButton.setForeground(Color.WHITE);
    editorsButton.setToolTipText(_localeManager.getValue("b.editors"));
    editorsButton.addActionListener(action -> {
      _ctx.getApp().showUI(EditorsUI.class);
    });
    sidePane.add(editorsButton);

    Icon silverweaveIcon = IconFontSwing.buildIcon(FontAwesome.MAGIC, Scale.px(16), Color.WHITE);
    silverweaveButton = new JButton(_localeManager.getValue("b.silverweave"));
    silverweaveButton.setIcon(silverweaveIcon);
    silverweaveButton.setBounds(Scale.rect(28, sideButtonsY + 120, 125, 35));
    silverweaveButton.setHorizontalAlignment(SwingConstants.LEFT);
    silverweaveButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    silverweaveButton.setFocusPainted(false);
    silverweaveButton.setFocusable(false);
    silverweaveButton.setBorderPainted(false);
    silverweaveButton.setEnabled(true);
    silverweaveButton.setBackground(CustomColors.SILVERWEAVE);
    silverweaveButton.setForeground(Color.WHITE);
    silverweaveButton.setToolTipText(_localeManager.getValue("b.silverweave"));
    silverweaveButton.addActionListener(action -> {
      // Straight to the editors' loading screen, so there's something to look at while it boots.
      _ctx.getApp().showUI(EditorsUI.class);
      _ctx.getApp().getUI(EditorsUI.class).eventHandler.startEditor("editor_silverweave");
    });
    sidePane.add(silverweaveButton);

    JButton discordButton = new JButton(ImageUtil.imageStreamToIcon(LauncherUI.class.getResourceAsStream("/rsrc/img/icon-discord.png")));
    discordButton.setBounds(Scale.rect(65, 440, 36, 36));
    discordButton.setToolTipText(_localeManager.getValue("b.discord"));
    discordButton.setFocusPainted(false);
    discordButton.setFocusable(false);
    discordButton.setBorderPainted(false);
    discordButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    sidePane.add(discordButton);
    discordButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DISCORD));

    Icon bugIcon = IconFontSwing.buildIcon(FontAwesome.BUG, Scale.px(17), Color.WHITE);
    JButton bugButton = new JButton(bugIcon);
    bugButton.setBounds(Scale.rect(107, 440, 36, 36));
    bugButton.setToolTipText(_localeManager.getValue("b.bugs"));
    bugButton.setFocusPainted(false);
    bugButton.setFocusable(false);
    bugButton.setBorderPainted(false);
    bugButton.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    sidePane.add(bugButton);
    bugButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_BUG_REPORT));

    Icon donateIcon = IconFontSwing.buildIcon(FontAwesome.USD, Scale.px(17), Color.WHITE);
    JButton donateButton = new JButton(donateIcon);
    donateButton.setBounds(Scale.rect(149, 440, 36, 36));
    donateButton.setToolTipText(_localeManager.getValue("b.donate"));
    donateButton.setFocusPainted(false);
    donateButton.setFocusable(false);
    donateButton.setBorderPainted(false);
    donateButton.setBackground(CustomColors.PREMIUM);
    sidePane.add(donateButton);
    donateButton.addActionListener(e -> DesktopUtil.openWebpage(LauncherGlobals.URL_DONATE));

    JLabel launcherVersion = new JLabel("v" + BuildConfig.getVersion() + " ");
    launcherVersion.setFont(Fonts.getFont("default", 9.0f, Font.ITALIC));
    launcherVersion.setForeground(CustomColors.INTERFACE_SIDEPANE_FOOTNOTE);
    launcherVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    launcherVersion.setBounds(Scale.rect(10, 493, 230, 15));
    sidePane.add(launcherVersion);

    bannerTimer = new JLabel("");
    bannerTimer.setBounds(Scale.rect(40, 50, 225, 25));
    bannerTimer.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    bannerTimer.setHorizontalAlignment(SwingConstants.CENTER);
    bannerTimer.setVerticalAlignment(SwingConstants.CENTER);
    setBannerTimerEndingSoon(false);
    bannerTimer.setVisible(false);
    panel.add(bannerTimer);

    bannerTitle = new ShadowedLabel(_localeManager.getValue("m.banner_title_default"));
    bannerTitle.setBounds(Scale.rect(35, -60, 700, 340));
    bannerTitle.setFont(Fonts.getFont("default", 40.0f, Font.BOLD));
    bannerTitle.setForeground(Color.WHITE);
    bannerTitle.setVisible(false);
    panel.add(bannerTitle);

    bannerSubtitle1 = new ShadowedLabel(_localeManager.getValue("m.banner_subtitle_default"));
    bannerSubtitle1.setBounds(Scale.rect(40, -15, 700, 340));
    bannerSubtitle1.setFont(Fonts.getFont("default", 14.0f, Font.BOLD));
    bannerSubtitle1.setForeground(Color.WHITE);
    bannerSubtitle1.setVisible(false);
    panel.add(bannerSubtitle1);

    bannerSubtitle2 = new ShadowedLabel("");
    bannerSubtitle2.setBounds(Scale.rect(40, 5, 700, 340));
    bannerSubtitle2.setFont(Fonts.getFont("default", 14.0f, Font.BOLD));
    bannerSubtitle2.setForeground(Color.WHITE);
    bannerSubtitle2.setVisible(false);
    panel.add(bannerSubtitle2);

    bannerLinkButton = new JButton(_localeManager.getValue("b.learn_more"));
    bannerLinkButton.setBounds(Scale.rect(40, 195, 110, 25));
    bannerLinkButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    bannerLinkButton.setForeground(Color.WHITE);
    bannerLinkButton.setFocusPainted(false);
    bannerLinkButton.setFocusable(false);
    bannerLinkButton.setOpaque(false);
    bannerLinkButton.setBackground(CustomColors.INTERFACE_MAINPANE_TRANSPARENT_BUTTON);
    bannerLinkButton.setBorderPainted(false);
    bannerLinkButton.setVisible(false);
    panel.add(bannerLinkButton);

    serverNoticeButton = new JButton(_localeManager.getValue("b.server_notice"));
    serverNoticeButton.setIcon(IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, Scale.px(13), Color.BLACK));
    serverNoticeButton.setBounds(Scale.rect(510, 388, 252, 25));
    serverNoticeButton.setFocusPainted(false);
    serverNoticeButton.setFocusable(false);
    serverNoticeButton.setBackground(CustomColors.WARNING);
    serverNoticeButton.setBorderPainted(false);
    serverNoticeButton.setForeground(Color.BLACK);
    //serverNoticeButton.setVisible(false);
    serverNoticeButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    serverNoticeButton.setToolTipText(_localeManager.getValue("b.server_notice"));
    panel.add(serverNoticeButton);

    launchButton = new JButton(_localeManager.getValue("b.play"));
    launchButton.setBounds(Scale.rect(500, 423, 210, 52));
    launchButton.setFont(Fonts.getFont("default", 15.0f, Font.BOLD));
    launchButton.setFocusPainted(false);
    launchButton.setFocusable(false);
    launchButton.setBackground(CustomColors.LAUNCH);
    launchButton.setBorderPainted(false);
    launchButton.setForeground(Color.WHITE);
    launchButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    launchButton.setToolTipText(_localeManager.getValue("b.play"));
    panel.add(launchButton);
    launchButton.addActionListener(action -> {
      if (_kbController.isShiftPressed() || _kbController.isAltPressed()) {
        // TODO: Consolidate alt launching inside LauncherEventHandler::launchGameEvent for both.
        if (_flamingoManager.getSelectedServer().isOfficial()) {
          this.eventHandler.launchGameAltEvent();
        } else {
          this.eventHandler.launchGameEvent(true);
        }
      } else {
        // Display a warning first if the server is currently during a maintenance window.
        Server selectedServer = _flamingoManager.getSelectedServer();
        if (selectedServer.getMaintenanceStatus() == 1) {
          Dialog.push(
            _localeManager.getValue("m.scheduled_maintenance_warning", new String[] { DateUtil.getFormattedTime(selectedServer.maintenanceEndsAt, TimeZone.getDefault().getID()) }),
            _localeManager.getValue("t.scheduled_maintenance"),
            JOptionPane.WARNING_MESSAGE
          );
        }

        this.eventHandler.launchGameEvent(false);
      }
    });

    Icon launchPopupMenuIcon = IconFontSwing.buildIcon(FontAwesome.BARS, Scale.px(24), Color.WHITE);
    launchPopupMenuButton = new JButton(launchPopupMenuIcon);
    launchPopupMenuButton.setBounds(Scale.rect(720, 424, 52, 52));
    launchPopupMenuButton.setOpaque(false);
    launchPopupMenuButton.setBorderPainted(false);
    launchPopupMenuButton.setBackground(CustomColors.INTERFACE_MAINPANE_TRANSPARENT_BUTTON);
    launchPopupMenuButton.setToolTipText(_localeManager.getValue("m.launch_popup_tooltip"));
    launchPopupMenuButton.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
    panel.add(launchPopupMenuButton);

    JPopupMenu launchPopupMenu = new JPopupMenu();
    launchPopupMenu.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
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
    gameSettingsMenuItem.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    gameSettingsMenuItem.putClientProperty(FlatClientProperties.STYLE, "margin: 10,10,10,10; selectionArc: 15; selectionBackground: " + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND_FOCUS) + ";");

    JMenuItem repairGameFilesMenuItem = new JMenuItem(new AbstractAction(_localeManager.getValue("m.repair_game_files"))
    {
      public void actionPerformed (ActionEvent e)
      {
        eventHandler.repairGameFilesEvent();
      }
    });
    repairGameFilesMenuItem.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    repairGameFilesMenuItem.putClientProperty(FlatClientProperties.STYLE, "margin: 10,10,10,10; selectionArc: 15; selectionBackground: " + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND_FOCUS) + ";");

    JMenuItem openGameFolderMenuItem = new JMenuItem(new AbstractAction(_localeManager.getValue("b.open_game_dir"))
    {
      public void actionPerformed (ActionEvent e)
      {
        eventHandler.openGameFolderEvent();
      }
    });
    openGameFolderMenuItem.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
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
    altModeMenuItem.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
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
          launchPopupMenu.show(panel, Scale.px(575), Scale.px(245));
        }
      }
    });

    Icon altModeEnabledIcon = IconFontSwing.buildIcon(FontAwesome.USER_PLUS, Scale.px(16), Color.WHITE);
    altModeEnabledLabel = new JLabel(_localeManager.getValue("m.alt_mode"));
    altModeEnabledLabel.setIcon(altModeEnabledIcon);
    altModeEnabledLabel.setBounds(Scale.rect(540, 480, 130, 20));
    altModeEnabledLabel.setFont(Fonts.getFont("default", 11.0f, Font.ITALIC));
    altModeEnabledLabel.setForeground(Color.WHITE);
    altModeEnabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
    altModeEnabledLabel.setVerticalAlignment(SwingConstants.CENTER);
    altModeEnabledLabel.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND)
        + "AA; foreground:" + ColorUtil.colorToHexString(Color.WHITE)
        + "; arc:999;");
    altModeEnabledLabel.setVisible(false);
    panel.add(altModeEnabledLabel);

    // Progress box: the current task with a spinner and its percentage on top, a slim bar below.
    // Vertically centered on the play button next to it.
    BufferedImage launchBackgroundImage = ImageUtil.generatePlainColorImage(Scale.px(446), Scale.px(64), CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND_FOCUS_DARK);
    launchBackgroundImage = (BufferedImage) ImageUtil.addRoundedCorners(launchBackgroundImage, Scale.px(28));
    ImageUtil.setAlpha(launchBackgroundImage, (byte) 245);
    launchBackground = new JLabel("");
    launchBackground.setBounds(Scale.rect(28, 417, 446, 64));
    launchBackground.setIcon(new ImageIcon(launchBackgroundImage));
    launchBackground.setVisible(false);
    panel.add(launchBackground);
    panel.setComponentZOrder(launchBackground, 1);

    launchState = new JLabel("");
    launchState.setHorizontalAlignment(SwingConstants.LEFT);
    launchState.setIconTextGap(Scale.px(8));
    launchState.setBounds(Scale.rect(44, 426, 330, 22));
    launchState.setFont(Fonts.getFont("default", 13.0f, Font.ITALIC));
    launchState.setVisible(false);
    panel.add(launchState);
    panel.setComponentZOrder(launchState, 0);

    launchProgressPercent = new JLabel("0%");
    launchProgressPercent.setHorizontalAlignment(SwingConstants.RIGHT);
    launchProgressPercent.setBounds(Scale.rect(384, 426, 74, 22));
    launchProgressPercent.setFont(Fonts.getFont("default", 13.0f, Font.BOLD));
    launchProgressPercent.setForeground(Color.WHITE);
    launchProgressPercent.setVisible(false);
    panel.add(launchProgressPercent);
    panel.setComponentZOrder(launchProgressPercent, 0);

    // Bounds include the room the bar's glow needs around it.
    launchProgressBar = new SmoothProgressBar();
    launchProgressBar.setUI(new SlimProgressBarUI(CustomColors.LAUNCH));
    launchProgressBar.setOpaque(false);
    launchProgressBar.setBorder(null);
    launchProgressBar.setBounds(Scale.rect(44 - SlimProgressBarUI.GLOW_MARGIN, 464 - 8, 414 + SlimProgressBarUI.GLOW_MARGIN * 2, 16));
    launchProgressBar.setVisible(false);
    launchProgressBar.setShouldPaintString(false);
    // The percentage follows the bar's animation rather than jumping straight to the new value.
    launchProgressBar.addChangeListener(e ->
      launchProgressPercent.setText(Math.round(launchProgressBar.getPercentComplete() * 100) + "%"));
    panel.add(launchProgressBar);
    panel.setComponentZOrder(launchProgressBar, 0);

    Icon changelogIcon = IconFontSwing.buildIcon(FontAwesome.BOOK, Scale.px(18), Color.WHITE);
    changelogButton = new JButton(changelogIcon);
    changelogButton.setBounds(Scale.rect(736, 26, 36, 36));
    changelogButton.setToolTipText(_localeManager.getValue("m.changelog"));
    changelogButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    changelogButton.setFocusPainted(false);
    changelogButton.setFocusable(false);
    changelogButton.setBorderPainted(false);
    changelogButton.setBackground(CustomColors.CHANGELOG);
    changelogButton.setForeground(Color.WHITE);
    changelogButton.setVisible(true);
    panel.add(changelogButton);
    changelogButton.addActionListener(l -> _ctx.getApp().showUI(ChangelogUI.class));

    Icon warningNoticeIcon = IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, Scale.px(16), Color.WHITE);
    warningNotice = new JButton(warningNoticeIcon);
    warningNotice.setBounds(Scale.rect(691, 26, 36, 36));
    warningNotice.setToolTipText(_localeManager.getValue("m.warning_notice"));
    warningNotice.setFocusPainted(false);
    warningNotice.setFocusable(false);
    warningNotice.setBorderPainted(false);
    warningNotice.setForeground(Color.WHITE);
    warningNotice.setBackground(CustomColors.LIGHT_RED);
    warningNotice.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    warningNotice.setVisible(false);
    warningNotice.addActionListener(l -> {
      Dialog.push(this.eventHandler.currentWarning, _localeManager.getValue("m.warning_notice"), JOptionPane.ERROR_MESSAGE);
    });
    panel.add(warningNotice);

    Icon updateIcon = IconFontSwing.buildIcon(FontAwesome.CLOUD_DOWNLOAD, Scale.px(16), Color.WHITE);
    updateButton = new JButton(updateIcon);
    updateButton.setBounds(Scale.rect(691, 26, 36, 36));
    updateButton.setToolTipText(_localeManager.getValue("b.update"));
    updateButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    updateButton.setFocusPainted(false);
    updateButton.setFocusable(false);
    updateButton.setBorderPainted(false);
    updateButton.setBackground(CustomColors.UPDATE);
    updateButton.setForeground(Color.WHITE);
    updateButton.setVisible(false);
    panel.add(updateButton);
    updateButton.addActionListener(
      l -> _ctx.getApp().doUpdate()
    );

    Icon playAnimatedBannersIconEnabled = IconFontSwing.buildIcon(FontAwesome.EYE, Scale.px(18), Color.WHITE);
    Icon playAnimatedBannersIconDisabled = IconFontSwing.buildIcon(FontAwesome.EYE_SLASH, Scale.px(18), Color.WHITE);
    playAnimatedBannersButton = new JButton(Settings.playAnimatedBanners ? playAnimatedBannersIconEnabled : playAnimatedBannersIconDisabled);
    playAnimatedBannersButton.setBounds(Scale.rect(736, 71, 36, 36));
    playAnimatedBannersButton.setToolTipText(_localeManager.getValue(Settings.playAnimatedBanners ? "m.animated_banners_disable" : "m.animated_banners_enable"));
    playAnimatedBannersButton.setFont(Fonts.getFont("default", 11.0f, Font.BOLD));
    playAnimatedBannersButton.setFocusPainted(false);
    playAnimatedBannersButton.setFocusable(false);
    playAnimatedBannersButton.setBorderPainted(false);
    playAnimatedBannersButton.setBackground(Settings.playAnimatedBanners ? CustomColors.INTERFACE_BUTTON_BACKGROUND : CustomColors.LIGHT_RED);
    playAnimatedBannersButton.setForeground(Color.WHITE);
    playAnimatedBannersButton.setVisible(false);
    panel.add(playAnimatedBannersButton);
    playAnimatedBannersButton.addActionListener(l -> this.eventHandler.switchBannerAnimations());

    closeButton.addActionListener(e -> {
      _ctx.getApp().exit();
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

  public void selectedServerChanged ()
  {
    this.eventHandler.selectedServerChanged();
  }

  public void showWarning (String message)
  {
    // we're also showing an available update, let's move the warning notice
    // slightly to the left, so they don't overlap.
    if (updateButton.isVisible()) {
      warningNotice.setBounds(warningNotice.getX() - Scale.px(45), Scale.px(26), Scale.px(35), Scale.px(35));
    }

    warningNotice.setVisible(true);
    this.eventHandler.currentWarning = message;
  }

  public BufferedImage processImageForBanner (BufferedImage image, double intensity)
  {
    image = ImageUtil.resizeImage(image, Scale.px(800), Scale.px(550));
    image = ImageUtil.fadeEdges(image, intensity);
    return image;
  }

  public BannerImage processAnimatedImageForBanner (byte[] gifData, double intensity)
  {
    try {
      final GifDecoder.GifImage gif = GifDecoder.read(gifData);
      final int frameCount = gif.getFrameCount();
      final List<BufferedImage> proccesedImages = new ArrayList<>();
      final int[] delays = new int[frameCount];

      // process every single frame of the GIF.
      for (int i = 0; i < frameCount; i++) {
        BufferedImage frame = gif.getFrame(i);
        frame = ImageUtil.resizeImage(frame, Scale.px(800), Scale.px(550));
        if (intensity > 0) ImageUtil.fadeEdges(frame, intensity);
        proccesedImages.add(frame);
        delays[i] = gif.getDelay(i);
      }

      return new BannerImage(proccesedImages, delays);
    } catch (IOException e) {
      log.error(e);
      return null;
    }
  }

  /**
   * Plays the given animated banner until {@code active} returns false, which happens
   * as soon as a different banner is displayed.
   */
  public void playAnimatedBanner (BannerImage image, BooleanSupplier active)
  {
    Thread animThread = new Thread(() -> {
      while (active.getAsBoolean()) {
        for (int i = 0; i < image.frames.size(); i++) {
          try {
            Thread.sleep(image.delays[i] * 10L);
          } catch (InterruptedException e) {
            return;
          }

          // the banner might have changed while we were sleeping.
          if (!active.getAsBoolean()) return;

          banner = Settings.playAnimatedBanners ? image.frames.get(i) : image.frames.get(0);
          panel.repaint();
        }
      }
    });
    animThread.setDaemon(true);
    animThread.start();
  }

  /**
   * Gives the banner timer a slight yellow tint when the event is about to end.
   */
  public void setBannerTimerEndingSoon (boolean endingSoon)
  {
    Color background = CustomColors.INTERFACE_MAINPANE_BACKGROUND;
    Color foreground = Color.WHITE;
    if (endingSoon) {
      background = blend(background, CustomColors.WARNING, 0.3f);
      foreground = blend(foreground, CustomColors.WARNING, 0.35f);
    }

    bannerTimer.setIcon(IconFontSwing.buildIcon(FontAwesome.CLOCK_O, Scale.px(16), foreground));
    bannerTimer.setForeground(foreground);
    bannerTimer.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(background)
        + "AA; foreground:" + ColorUtil.colorToHexString(foreground)
        + "; arc:999;");
  }

  private static Color blend (Color from, Color to, float amount)
  {
    return new Color(
      Math.round(from.getRed() + (to.getRed() - from.getRed()) * amount),
      Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * amount),
      Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * amount));
  }

  /**
   * Replaces the event cards with one per given title, flagging the ones that are ending soon.
   * No cards are shown for a single event, as there's nothing to switch between.
   */
  public void setEventCards (List<String> titles, List<Boolean> endingSoon)
  {
    for (EventCard card : eventCards) panel.remove(card);
    eventCards.clear();

    if (titles.size() > 1) {
      String endingSoonBadge = _localeManager.getValue("m.event_ending_soon");
      for (int i = 0; i < titles.size(); i++) {
        final int index = i;
        EventCard card = new EventCard(titles.get(i), endingSoon.get(i) ? endingSoonBadge : null,
          () -> this.eventHandler.selectBannerEvent(index));
        eventCards.add(card);
        panel.add(card);

        // Keep them above the banner texts, which can be wide enough to overlap.
        panel.setComponentZOrder(card, 0);
      }
    }

    layoutEventCards();
    panel.repaint();
  }

  public void setEventCardImage (int index, BufferedImage image)
  {
    if (index < eventCards.size()) eventCards.get(index).setImage(image);
  }

  public void setActiveEventCard (int index)
  {
    for (int i = 0; i < eventCards.size(); i++) eventCards.get(i).setActive(i == index);
  }

  /**
   * Stacks the event cards right above the launch button, or above the server notice when it's showing.
   * Cards get shorter if there are too many of them to fit.
   */
  public void layoutEventCards ()
  {
    if (eventCards.isEmpty()) return;

    int bottom = Scale.unpx(serverNoticeButton.isVisible() ? serverNoticeButton.getY() : launchButton.getY());
    bottom -= EVENT_CARDS_BOTTOM_MARGIN;

    int count = eventCards.size();
    int available = bottom - EVENT_CARDS_TOP;
    int cardHeight = Math.max(EVENT_CARD_MIN_HEIGHT,
      Math.min(EVENT_CARD_HEIGHT, (available - EVENT_CARD_GAP * (count - 1)) / count));

    int y = bottom - count * cardHeight - (count - 1) * EVENT_CARD_GAP;
    for (EventCard card : eventCards) {
      card.setBounds(Scale.rect(EVENT_CARDS_X, y, EVENT_CARDS_WIDTH, cardHeight));
      y += cardHeight + EVENT_CARD_GAP;
    }
  }

  /**
   * Cross-fades from the banner currently on screen to {@code next} and fades the banner texts in.
   * Passing null keeps the current image and only fades the texts.
   */
  public void fadeToBanner (BufferedImage next)
  {
    if (next != null) {
      previousBanner = banner;
      banner = next;
    } else {
      previousBanner = null;
    }

    if (bannerFadeTimer != null) bannerFadeTimer.stop();

    final long start = System.currentTimeMillis();
    bannerFade = 0f;
    setBannerTextAlpha(0f);
    bannerFadeTimer = new Timer(15, e -> {
      float t = Math.min(1f, (System.currentTimeMillis() - start) / (float) BANNER_FADE_DURATION);
      // ease-out cubic, the change is noticeable right away and settles smoothly.
      bannerFade = 1f - (float) Math.pow(1f - t, 3);
      setBannerTextAlpha(bannerFade);
      if (t >= 1f) {
        previousBanner = null;
        ((Timer) e.getSource()).stop();
      }
      panel.repaint();
    });
    bannerFadeTimer.start();
  }

  private void setBannerTextAlpha (float alpha)
  {
    Color color = new Color(255, 255, 255, Math.round(alpha * 255));
    bannerTitle.setForeground(color);
    bannerSubtitle1.setForeground(color);
    bannerSubtitle2.setForeground(color);
  }

  /**
   * A label with a soft drop shadow under its text.
   * The shadow fades along with the text's own alpha.
   */
  private static class ShadowedLabel extends JLabel
  {
    public ShadowedLabel (String text)
    {
      super(text);
    }

    @Override
    protected void paintComponent (Graphics g)
    {
      String text = getText();
      if (text != null && !text.isEmpty()) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(getFont());
        FontMetrics metrics = g2d.getFontMetrics();

        // Place the text exactly where the label itself will draw it.
        Insets insets = getInsets();
        Rectangle view = new Rectangle(insets.left, insets.top,
          getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);
        Rectangle iconBounds = new Rectangle();
        Rectangle textBounds = new Rectangle();
        String clipped = SwingUtilities.layoutCompoundLabel(this, metrics, text, null,
          getVerticalAlignment(), getHorizontalAlignment(), getVerticalTextPosition(), getHorizontalTextPosition(),
          view, iconBounds, textBounds, 0);

        int alpha = Math.round(SHADOW_ALPHA * (getForeground().getAlpha() / 255f));
        g2d.setColor(new Color(0, 0, 0, alpha));
        g2d.drawString(clipped, textBounds.x + Scale.px(1), textBounds.y + metrics.getAscent() + Scale.px(2));
        g2d.dispose();
      }
      super.paintComponent(g);
    }

    private static final int SHADOW_ALPHA = 90;
  }

  public static class BannerImage
  {
    public BannerImage (BufferedImage image)
    {
      this.frames = Collections.singletonList(image);
      this.delays = null;
    }

    public BannerImage (List<BufferedImage> frames, int[] delays)
    {
      this.frames = frames;
      this.delays = delays;
    }

    public boolean isAnimated ()
    {
      return delays != null;
    }

    public final List<BufferedImage> frames;

    public final int[] delays;
  }

  public void returnToHome ()
  {
    _ctx.getApp().returnToHome();
  }

  public void resetLaunchButton ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();
    if (selectedServer != null) {
      switch (selectedServer.getMaintenanceStatus()) {
        case 0:
          // no maintenance.
          launchButton.setBackground(CustomColors.LAUNCH);
          launchButton.setForeground(Color.WHITE);
          launchButton.setIcon(null);
          break;
        case 1:
          // maintenance ongoing.
          launchButton.setBackground(CustomColors.WARNING);
          launchButton.setForeground(Color.BLACK);
          launchButton.setIcon(IconFontSwing.buildIcon(FontAwesome.EXCLAMATION_TRIANGLE, Scale.px(13), Color.BLACK));
          break;
        case 2:
          // maintenance scheduled.
          launchButton.setBackground(CustomColors.LAUNCH);
          launchButton.setForeground(Color.WHITE);
          launchButton.setIcon(null);
          break;
        default:
          launchButton.setBackground(CustomColors.LAUNCH);
          launchButton.setForeground(Color.WHITE);
          launchButton.setIcon(null);
          break;
      }

      if (!selectedServer.isInstalled()) {
        launchButton.setText(_localeManager.getValue("b.install"));
        launchButton.setToolTipText(_localeManager.getValue("b.install"));
      } else if (selectedServer.isOutdated()) {
        launchButton.setText(_localeManager.getValue("b.update"));
        launchButton.setToolTipText(_localeManager.getValue("b.update"));
      } else {
        launchButton.setText(_localeManager.getValue("b.play"));
        launchButton.setToolTipText(_localeManager.getValue("b.play"));
      }
    } else {
      launchButton.setBackground(CustomColors.LAUNCH);
      launchButton.setForeground(Color.WHITE);
      launchButton.setIcon(null);
      launchButton.setText(_localeManager.getValue("b.play"));
      launchButton.setToolTipText(_localeManager.getValue("b.play"));
    }
  }

  public void specialKeyPressed ()
  {
    launchButton.setBackground(CustomColors.LAUNCH_ALT);
    launchButton.setIcon(null);
    launchButton.updateUI();
    altModeEnabledLabel.setVisible(true);
  }

  public void specialKeyReleased ()
  {
    resetLaunchButton();

    launchButton.updateUI();
    altModeEnabledLabel.setVisible(false);
  }

  public void toggleElementsBlock (boolean block)
  {
    super.toggleElementsBlock(block);
    this.eventHandler.updateServerSwitcher(block);
    this.updateButton.setEnabled(!block);
    this.launchButton.setEnabled(!block);
    this.launchPopupMenuButton.setEnabled(!block);
    this.settingsButton.setEnabled(!block);
    this.modButton.setEnabled(!block);
    this.editorsButton.setEnabled(!block);
    if (this.silverweaveButton != null) this.silverweaveButton.setEnabled(!block);
  }

  public void updatePlayerCountTooltip (PlayerCount playerCount)
  {
    // when we got no player count, do nothing.
    if (playerCount == null) {
      return;
    }

    String playerCountTooltipTitle = _localeManager.getValue("m.players_online");
    String playerCountTooltipText = _localeManager.getValue(
      "m.players_online_text",
      new String[] {
        String.valueOf(playerCount.getProdCount()),
        String.valueOf(playerCount.getProdEstimated()),
        String.valueOf(playerCount.getPreviewCount()),
        String.valueOf(playerCount.getPreviewEstimated())
      }
    );

    playerCountTooltipButton.setToolTipText(playerCountTooltipTitle);

    // This gets called every time the player count is refreshed, so get rid of the previous count's listener.
    for (ActionListener listener : playerCountTooltipButton.getActionListeners()) {
      playerCountTooltipButton.removeActionListener(listener);
    }
    playerCountTooltipButton.addActionListener(l -> {
      Dialog.push(playerCountTooltipText, playerCountTooltipTitle, JOptionPane.INFORMATION_MESSAGE);
    });
  }

  @Inject public LauncherEventHandler eventHandler;

  @Inject protected LauncherContext _ctx;
  @Inject protected LocaleManager _localeManager;
  @Inject protected FlamingoManager _flamingoManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;
  @Inject protected KeyboardController _kbController;

  // Server switcher pane
  public JPanel serverSwitcherPane;
  public JScrollPane serverSwitcherPaneScrollBar;

  // Side pane
  public JButton settingsButton;
  public JButton modButton;
  public JButton editorsButton;
  public JButton silverweaveButton;
  public JButton playerCountTooltipButton;
  public JLabel playerCountLabel;
  public JLabel selectedServerLabel;
  public JButton serverInfoButton;

  // Main pane
  public volatile BufferedImage banner = null;
  public JLabel bannerLoading;
  public JLabel bannerTimer;
  public JLabel bannerTitle;
  public JLabel bannerSubtitle1;
  public JLabel bannerSubtitle2;
  public JButton bannerLinkButton;
  public JButton serverNoticeButton;
  public JButton launchButton;
  public JButton launchPopupMenuButton;
  public JButton updateButton;
  public JButton changelogButton;
  public JButton playAnimatedBannersButton;
  public JLabel launchBackground;
  public JLabel launchState;
  // The main window's size at 100% scale. It's the biggest window, so it decides which scales fit on screen.
  public static final int WIDTH = 1100;
  public static final int HEIGHT = 550;

  public JLabel launchProgressPercent;
  public SmoothProgressBar launchProgressBar = new SmoothProgressBar(); // does this need to init here? why?
  public JButton warningNotice;
  public JLabel altModeEnabledLabel;

  private final List<EventCard> eventCards = new ArrayList<>();
  private volatile BufferedImage previousBanner = null;
  private volatile float bannerFade = 1f;
  private Timer bannerFadeTimer;

  private static final int BANNER_FADE_DURATION = 400;

  // Event cards sit in the column above the launch button, below the buttons on the top right.
  private static final int EVENT_CARDS_X = 500;
  private static final int EVENT_CARDS_WIDTH = 272;
  private static final int EVENT_CARDS_TOP = 120;
  private static final int EVENT_CARD_HEIGHT = 46;
  private static final int EVENT_CARD_MIN_HEIGHT = 28;
  private static final int EVENT_CARD_GAP = 6;

  // Space between the bottom card and the button right below the stack.
  private static final int EVENT_CARDS_BOTTOM_MARGIN = 14;
}
