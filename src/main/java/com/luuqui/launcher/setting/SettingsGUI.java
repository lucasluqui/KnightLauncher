package com.luuqui.launcher.setting;

import com.formdev.flatlaf.FlatClientProperties;
import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.*;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.mod.ModListGUI;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static com.luuqui.launcher.setting.Log.log;

public class SettingsGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame settingsGUIFrame;
  public static JTabbedPane tabbedPane;
  public static JComboBox<String> choicePlatform = new JComboBox<String>();
  public static JComboBox<String> choiceLanguage;
  public static JComboBox<String> choiceGC;
  public static JCheckBox switchCleaning;
  public static JCheckBox switchKeepOpen;
  public static JCheckBox switchShortcut;
  public static JButton jvmPatchButton = new JButton();
  public static JButton forceRebuildButton = new JButton();
  public static JCheckBox switchExplicitGC;
  public static JCheckBox switchUseCustomGC;
  public static JSlider memorySlider;
  public static JLabel memoryValue;
  public static JCheckBox switchUseIngameRPC = new JCheckBox();
  public static JCheckBox switchAutoUpdate;
  public static JEditorPane argumentsPane;
  public static JLabel labelDisclaimer = new JLabel();
  public static JTextField serverAddressTextField = new JTextField();
  public static JTextField portTextField = new JTextField();
  public static JTextField publicKeyTextField = new JTextField();
  public static JTextField getdownURLTextField = new JTextField();
  public static JButton resetConnectionSettingsButton = new JButton();
  public static JTextField betaCodeTextField;
  public static JLabel labelFlamingoStatus = new JLabel();
  public static JLabel labelFlamingoVersion = new JLabel();
  public static JLabel labelFlamingoUptime = new JLabel();
  public static JButton betaCodeRevalidateButton;
  public static JButton betaCodeClearLocalButton;
  public static JButton resetGameSettingsButton;
  public static JLabel javaVMBadge = new JLabel();
  public static JLabel activeCodesLabel = new JLabel();
  public static JLabel activeCodesBackground = new JLabel();
  public static JPanel activeCodesPane = new JPanel();
  public static JScrollPane activeCodesPaneScrollBar = new JScrollPane();

  public SettingsGUI(LauncherApp app) {
    super();
    this.app = app;
    initialize();
    checkBetaCodes();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.settingsGUIFrame.setVisible(!this.settingsGUIFrame.isVisible());
  }

  private void initialize() {
    settingsGUIFrame = new JFrame();
    settingsGUIFrame.setVisible(false);
    settingsGUIFrame.setTitle(Locale.getValue("t.settings"));
    settingsGUIFrame.setBounds(100, 100, 850, 475);
    settingsGUIFrame.setResizable(false);
    settingsGUIFrame.setUndecorated(true);
    settingsGUIFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    settingsGUIFrame.getContentPane().setLayout(null);

    tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.setBounds(-2, 20, 852, 455);
    tabbedPane.setFont(Fonts.fontMedBig);
    tabbedPane.setTabPlacement(JTabbedPane.LEFT);
    tabbedPane.setFocusable(false);
    tabbedPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    tabbedPane.addTab(Locale.getValue("tab.launcher"), createLauncherPanel());
    tabbedPane.addTab(Locale.getValue("tab.game"), createGamePanel());
    tabbedPane.addTab(Locale.getValue("tab.betas"), createBetasPanel());
    tabbedPane.addTab(Locale.getValue("tab.advanced"), createAdvancedPanel());
    tabbedPane.addTab(Locale.getValue("tab.about"), createAboutPanel());

    settingsGUIFrame.getContentPane().add(tabbedPane);

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, settingsGUIFrame.getWidth(), 20);
    titleBar.setBackground(ColorUtil.getTitleBarColor());
    settingsGUIFrame.getContentPane().add(titleBar);


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

        settingsGUIFrame.setLocation(settingsGUIFrame.getLocation().x + me.getX() - pX,
                settingsGUIFrame.getLocation().y + me.getY() - pY);
      }
    });
    titleBar.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent me) {

        settingsGUIFrame.setLocation(settingsGUIFrame.getLocation().x + me.getX() - pX,
                settingsGUIFrame.getLocation().y + me.getY() - pY);
      }

      @Override
      public void mouseMoved(MouseEvent arg0) {
        // Auto-generated method stub
      }
    });
    titleBar.setLayout(null);

    JLabel windowTitle = new JLabel(Locale.getValue("t.settings"));
    windowTitle.setFont(Fonts.fontMed);
    windowTitle.setBounds(10, 0, settingsGUIFrame.getWidth() - 100, 20);
    titleBar.add(windowTitle);

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 14, ColorUtil.getForegroundColor());
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(settingsGUIFrame.getWidth() - 18, 1, 20, 21);
    closeButton.setToolTipText(Locale.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(e -> settingsGUIFrame.setVisible(false));

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_DOWN, 14, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(settingsGUIFrame.getWidth() - 38, 1, 20, 21);
    minimizeButton.setToolTipText(Locale.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);

    settingsGUIFrame.setLocationRelativeTo(null);
  }

  protected JPanel createLauncherPanel() {
    JPanel launcherPanel = new JPanel();
    launcherPanel.setLayout(null);
    launcherPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);


    JLabel headerLabel = new JLabel(Locale.getValue("tab.launcher"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    launcherPanel.add(headerLabel);

    JLabel labelLanguage = new JLabel(Locale.getValue("m.language"));
    labelLanguage.setBounds(25, 90, 175, 20);
    labelLanguage.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelLanguage);

    choiceLanguage = new JComboBox<String>();
    choiceLanguage.setBounds(110, 90, 150, 20);
    choiceLanguage.setFocusable(false);
    choiceLanguage.setFont(Fonts.fontReg);
    for (String lang : Locale.AVAILABLE_LANGUAGES) {
      choiceLanguage.addItem(lang);
    }
    launcherPanel.add(choiceLanguage);
    choiceLanguage.setSelectedItem(Locale.getLangName(Settings.lang));
    choiceLanguage.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        SettingsEventHandler.languageChangeEvent(event);
      }
    });

    JLabel labelCleaning = new JLabel(Locale.getValue("m.rebuilds"));
    labelCleaning.setBounds(25, 140, 350, 20);
    labelCleaning.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelCleaning);

    JLabel labelCleaningExplained = new JLabel(Locale.getValue("m.file_cleaning_explained"));
    labelCleaningExplained.setBounds(25, 160, 600, 16);
    labelCleaningExplained.setFont(Fonts.fontReg);
    launcherPanel.add(labelCleaningExplained);

    switchCleaning = new JCheckBox("");
    switchCleaning.setBounds(590, 145, 30, 23);
    switchCleaning.setFocusPainted(false);
    launcherPanel.add(switchCleaning);
    switchCleaning.setSelected(Settings.doRebuilds);
    switchCleaning.addActionListener(SettingsEventHandler::rebuildsChangeEvent);

    JSeparator sep = new JSeparator();
    sep.setBounds(25, 190, 600, 16);
    launcherPanel.add(sep);

    JLabel labelKeepOpen = new JLabel(Locale.getValue("m.keep_open"));
    labelKeepOpen.setBounds(25, 205, 350, 20);
    labelKeepOpen.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelKeepOpen);

    JLabel labelKeepOpenExplained = new JLabel(Locale.getValue("m.keep_open_explained"));
    labelKeepOpenExplained.setBounds(25, 225, 600, 16);
    labelKeepOpenExplained.setFont(Fonts.fontReg);
    launcherPanel.add(labelKeepOpenExplained);

    switchKeepOpen = new JCheckBox("");
    switchKeepOpen.setBounds(590, 210, 30, 23);
    switchKeepOpen.setFocusPainted(false);
    launcherPanel.add(switchKeepOpen);
    switchKeepOpen.setSelected(Settings.keepOpen);
    switchKeepOpen.addActionListener(SettingsEventHandler::keepOpenChangeEvent);

    JSeparator sep2 = new JSeparator();
    sep2.setBounds(25, 255, 600, 16);
    launcherPanel.add(sep2);

    JLabel labelShortcut = new JLabel(Locale.getValue("m.create_shortcut"));
    labelShortcut.setBounds(25, 270, 225, 20);
    labelShortcut.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelShortcut);

    JLabel labelShortcutExplained = new JLabel(Locale.getValue("m.create_shortcut_explained"));
    labelShortcutExplained.setBounds(25, 290, 600, 16);
    labelShortcutExplained.setFont(Fonts.fontReg);
    launcherPanel.add(labelShortcutExplained);

    switchShortcut = new JCheckBox("");
    switchShortcut.setBounds(590, 275, 30, 23);
    switchShortcut.setFocusPainted(false);
    launcherPanel.add(switchShortcut);
    switchShortcut.setSelected(Settings.createShortcut);
    switchShortcut.addActionListener(SettingsEventHandler::createShortcutChangeEvent);

    JSeparator sepAutoUpdate = new JSeparator();
    sepAutoUpdate.setBounds(25, 320, 600, 16);
    launcherPanel.add(sepAutoUpdate);

    JLabel labelAutoUpdate = new JLabel(Locale.getValue("m.autoupdate"));
    labelAutoUpdate.setBounds(25, 335, 350, 20);
    labelAutoUpdate.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelAutoUpdate);

    JLabel labelAutoUpdateExplained = new JLabel(Locale.getValue("m.autoupdate_explained"));
    labelAutoUpdateExplained.setBounds(25, 355, 600, 16);
    labelAutoUpdateExplained.setFont(Fonts.fontReg);
    launcherPanel.add(labelAutoUpdateExplained);

    switchAutoUpdate = new JCheckBox("");
    switchAutoUpdate.setBounds(590, 340, 30, 23);
    switchAutoUpdate.setFocusPainted(false);
    launcherPanel.add(switchAutoUpdate);
    switchAutoUpdate.setSelected(Settings.autoUpdate);
    switchAutoUpdate.addActionListener(SettingsEventHandler::autoUpdateChangeEvent);

    if(SystemUtil.isWindows() && SystemUtil.is64Bit()) {
      JSeparator sepDiscord = new JSeparator();
      sepDiscord.setBounds(25, 385, 600, 16);
      launcherPanel.add(sepDiscord);

      JLabel labelUseIngameRPC = new JLabel(Locale.getValue("m.use_ingame_rpc"));
      labelUseIngameRPC.setBounds(25, 400, 350, 20);
      labelUseIngameRPC.setFont(Fonts.fontRegBig);
      launcherPanel.add(labelUseIngameRPC);

      JLabel labelUseIngameRPCExplained = new JLabel(Locale.getValue("m.use_ingame_rpc_explained"));
      labelUseIngameRPCExplained.setBounds(25, 420, 600, 16);
      labelUseIngameRPCExplained.setFont(Fonts.fontReg);
      launcherPanel.add(labelUseIngameRPCExplained);

      switchUseIngameRPC = new JCheckBox("");
      switchUseIngameRPC.setBounds(590, 405, 30, 23);
      switchUseIngameRPC.setFocusPainted(false);
      launcherPanel.add(switchUseIngameRPC);
      switchUseIngameRPC.setSelected(Settings.useIngameRPC);
      switchUseIngameRPC.addActionListener(SettingsEventHandler::ingameRPCChangeEvent);
    }

    return launcherPanel;
  }

  protected JPanel createGamePanel() {
    JPanel gamePanel = new JPanel();
    gamePanel.setLayout(null);
    gamePanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.game"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    gamePanel.add(headerLabel);

    javaVMBadge = new JLabel(Locale.getValue("m.game_java_vm_data", JavaUtil.getReadableGameJVMData()));
    javaVMBadge.setBounds(370, 35, 235, 18);
    javaVMBadge.setHorizontalAlignment(SwingConstants.CENTER);
    javaVMBadge.setFont(Fonts.fontRegSmall);
    javaVMBadge.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND) + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_FOREGROUND) + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND));
    gamePanel.add(javaVMBadge);

    JLabel labelStyle = new JLabel(Locale.getValue("m.platform"));
    labelStyle.setBounds(25, 90, 125, 18);
    labelStyle.setFont(Fonts.fontRegBig);
    gamePanel.add(labelStyle);

    choicePlatform = new JComboBox<String>();
    choicePlatform.setBounds(25, 115, 150, 20);
    choicePlatform.setFont(Fonts.fontReg);
    choicePlatform.setFocusable(false);
    gamePanel.add(choicePlatform);
    choicePlatform.addItem(Locale.getValue("o.steam"));
    choicePlatform.addItem(Locale.getValue("o.standalone"));
    if(SystemUtil.isWindows() && !SteamUtil.isRunningInSteamapps()) {
      choicePlatform.removeItem(Locale.getValue("o.steam"));
    }
    choicePlatform.setSelectedItem(Settings.gamePlatform);
    choicePlatform.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        SettingsEventHandler.platformChangeEvent(event);
      }
    });

    JLabel labelMemory = new JLabel(Locale.getValue("m.allocated_memory"));
    labelMemory.setBounds(275, 90, 275, 18);
    labelMemory.setFont(Fonts.fontRegBig);
    gamePanel.add(labelMemory);

    // Make sure the currently selected game memory does not exceed the max.
    Settings.gameMemory = Math.min(Settings.gameMemory, getMaxAllowedMemoryAlloc());
    SettingsEventHandler.memoryChangeEvent(Settings.gameMemory);

    memorySlider = new JSlider(JSlider.HORIZONTAL, 256, getMaxAllowedMemoryAlloc(), Settings.gameMemory);
    memorySlider.setBounds(265, 105, 350, 40);
    memorySlider.setFocusable(false);
    memorySlider.setFont(Fonts.fontReg);
    memorySlider.setPaintTicks(true);
    memorySlider.setMinorTickSpacing(256);
    memorySlider.setSnapToTicks(true);
    gamePanel.add(memorySlider);

    memoryValue = new JLabel();
    memoryValue.setBounds(270, 139, 350, 25);
    memoryValue.setFont(Fonts.fontReg);
    memoryValue.setText(Locale.getValue("o.memory_" + Settings.gameMemory));
    gamePanel.add(memoryValue);

    memorySlider.addChangeListener(l -> {
      memoryValue.setText(Locale.getValue("o.memory_" + memorySlider.getValue()));
      SettingsEventHandler.memoryChangeEvent(memorySlider.getValue());
    });

    JLabel labelUseCustomGC = new JLabel(Locale.getValue("m.use_custom_gc"));
    labelUseCustomGC.setBounds(25, 175, 375, 18);
    labelUseCustomGC.setFont(Fonts.fontRegBig);
    gamePanel.add(labelUseCustomGC);

    JLabel labelUseCustomGCExplained = new JLabel(Locale.getValue("m.use_custom_gc_explained"));
    labelUseCustomGCExplained.setBounds(25, 195, 600, 16);
    labelUseCustomGCExplained.setFont(Fonts.fontReg);
    gamePanel.add(labelUseCustomGCExplained);

    switchUseCustomGC = new JCheckBox("");
    switchUseCustomGC.setBounds(590, 180, 30, 23);
    switchUseCustomGC.setFocusPainted(false);
    gamePanel.add(switchUseCustomGC);
    switchUseCustomGC.setSelected(Settings.gameUseCustomGC);
    switchUseCustomGC.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsEventHandler.customGCChangeEvent(_action);
      }
    });
    switchUseCustomGC.setEnabled(SystemUtil.is64Bit());

    choiceGC = new JComboBox<String>();
    choiceGC.setBounds(465, 180, 110, 20);
    choiceGC.setFocusable(false);
    choiceGC.setFont(Fonts.fontReg);
    gamePanel.add(choiceGC);
    choiceGC.addItem("ParallelOld");
    choiceGC.addItem("Serial");
    choiceGC.addItem("G1");
    choiceGC.setSelectedItem(Settings.gameGarbageCollector);
    choiceGC.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        SettingsEventHandler.choiceGCChangeEvent(event);
      }
    });
    choiceGC.setEnabled(SystemUtil.is64Bit());

    JSeparator sep2 = new JSeparator();
    sep2.setBounds(25, 225, 600, 16);
    gamePanel.add(sep2);

    JLabel labelExplicitGC = new JLabel(Locale.getValue("m.disable_explicit_gc"));
    labelExplicitGC.setBounds(25, 240, 275, 18);
    labelExplicitGC.setFont(Fonts.fontRegBig);
    gamePanel.add(labelExplicitGC);

    JLabel labelExplicitGCExplained = new JLabel(Locale.getValue("m.explicit_gc_explained"));
    labelExplicitGCExplained.setBounds(25, 260, 600, 16);
    labelExplicitGCExplained.setFont(Fonts.fontReg);
    gamePanel.add(labelExplicitGCExplained);

    switchExplicitGC = new JCheckBox("");
    switchExplicitGC.setBounds(590, 245, 30, 23);
    switchExplicitGC.setFocusPainted(false);
    gamePanel.add(switchExplicitGC);
    switchExplicitGC.setSelected(Settings.gameDisableExplicitGC);
    switchExplicitGC.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsEventHandler.disableExplicitGCChangeEvent(_action);
      }
    });
    switchExplicitGC.setEnabled(SystemUtil.is64Bit());

    JSeparator sep3 = new JSeparator();
    sep3.setBounds(25, 290, 600, 16);
    gamePanel.add(sep3);

    JLabel labelFileClean = new JLabel(Locale.getValue("b.force_rebuild"));
    labelFileClean.setBounds(25, 305, 275, 18);
    labelFileClean.setFont(Fonts.fontRegBig);
    gamePanel.add(labelFileClean);

    JLabel labelFileCleanExplained = new JLabel(Locale.getValue("m.clean_files_explained"));
    labelFileCleanExplained.setBounds(25, 325, 600, 16);
    labelFileCleanExplained.setFont(Fonts.fontReg);
    gamePanel.add(labelFileCleanExplained);

    Icon startIcon = IconFontSwing.buildIcon(FontAwesome.SHARE, 16, ColorUtil.getForegroundColor());
    forceRebuildButton = new JButton(startIcon);
    forceRebuildButton.setBounds(585, 310, 30, 23);
    forceRebuildButton.setFocusPainted(false);
    forceRebuildButton.setFocusable(false);
    gamePanel.add(forceRebuildButton);
    forceRebuildButton.addActionListener(action -> {
      SettingsGUI.settingsGUIFrame.setVisible(false);
      LauncherGUI.launcherGUIFrame.setVisible(true);
      SettingsEventHandler.forceRebuildEvent();
    });

    JSeparator sep4 = new JSeparator();
    sep4.setBounds(25, 355, 600, 16);
    gamePanel.add(sep4);

    JLabel labelJVMPatch = new JLabel(Locale.getValue("m.force_jvm_patch"));
    labelJVMPatch.setBounds(25, 370, 350, 18);
    labelJVMPatch.setFont(Fonts.fontRegBig);
    gamePanel.add(labelJVMPatch);

    JLabel labelJVMPatchExplained = new JLabel(Locale.getValue("m.force_jvm_patch_explained"));
    labelJVMPatchExplained.setBounds(25, 390, 600, 16);
    labelJVMPatchExplained.setFont(Fonts.fontReg);
    gamePanel.add(labelJVMPatchExplained);

    jvmPatchButton = new JButton(startIcon);
    jvmPatchButton.setBounds(585, 375, 30, 23);
    jvmPatchButton.setFocusPainted(false);
    jvmPatchButton.setFocusable(false);
    jvmPatchButton.setEnabled(false);
    jvmPatchButton.setToolTipText(Locale.getValue("error.unsupported_64bit"));
    gamePanel.add(jvmPatchButton);
    jvmPatchButton.addActionListener(SettingsEventHandler::jvmPatchEvent);

    if(((SystemUtil.isWindows() && SystemUtil.is64Bit() ) || ( SystemUtil.isUnix() && Settings.gamePlatform.equalsIgnoreCase("Steam")))) {
      jvmPatchButton.setEnabled(true);
      jvmPatchButton.setToolTipText(null);
    }

    JButton loadRecommendedSettingsButton = new JButton(Locale.getValue("b.recommended_settings"));
    loadRecommendedSettingsButton.setFont(Fonts.fontMed);
    loadRecommendedSettingsButton.setBounds(195, 423, 230, 23);
    loadRecommendedSettingsButton.setFocusPainted(false);
    loadRecommendedSettingsButton.setFocusable(false);
    loadRecommendedSettingsButton.setToolTipText(Locale.getValue("b.recommended_settings"));
    gamePanel.add(loadRecommendedSettingsButton);
    loadRecommendedSettingsButton.addActionListener(SettingsEventHandler::loadRecommendedSettingsButtonEvent);

    resetGameSettingsButton = new JButton(Locale.getValue("b.reset_default"));
    resetGameSettingsButton.setFont(Fonts.fontMed);
    resetGameSettingsButton.setBounds(435, 423, 180, 23);
    resetGameSettingsButton.setFocusPainted(false);
    resetGameSettingsButton.setFocusable(false);
    resetGameSettingsButton.setForeground(CustomColors.BUTTON_FOREGROUND_DANGER);
    resetGameSettingsButton.setToolTipText(Locale.getValue("b.reset_default"));
    gamePanel.add(resetGameSettingsButton);
    resetGameSettingsButton.addActionListener(SettingsEventHandler::resetGameSettingsButtonEvent);

    return gamePanel;
  }

  protected JPanel createBetasPanel() {
    JPanel betasPanel = new JPanel();
    betasPanel.setLayout(null);
    betasPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.betas"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    betasPanel.add(headerLabel);

    JLabel betaCodeLabel = new JLabel(Locale.getValue("m.beta_code_activate"));
    betaCodeLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeLabel.setBounds(25, 72, 450, 50);
    betaCodeLabel.setFont(Fonts.fontRegBig);
    betasPanel.add(betaCodeLabel);

    betaCodeTextField = new JTextField();
    betaCodeTextField.setFont(Fonts.fontCodeReg);
    betaCodeTextField.setBounds(25, 112, 250, 25);
    betasPanel.add(betaCodeTextField);

    JLabel betaCodeResultLabel = new JLabel("");
    betaCodeResultLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeResultLabel.setBounds(25, 127, 450, 50);
    betaCodeResultLabel.setFont(Fonts.fontReg);
    betaCodeResultLabel.setVisible(false);
    betasPanel.add(betaCodeResultLabel);

    JButton betaCodeButton = new JButton(Locale.getValue("b.activate"));
    betaCodeButton.setFont(Fonts.fontMed);
    betaCodeButton.setFocusPainted(false);
    betaCodeButton.setFocusable(false);
    betaCodeButton.setToolTipText(Locale.getValue("b.activate"));
    betaCodeButton.setBounds(290, 112, 100, 25);
    betasPanel.add(betaCodeButton);
    betaCodeButton.addActionListener(action -> {
      int result = SettingsEventHandler.activateBetaCode(betaCodeTextField.getText(), false);

      // TODO: use enums.
      switch(result) {
        case -1: betaCodeResultLabel.setText(Locale.getValue("m.beta_code_invalid")); break;
        case 0: betaCodeResultLabel.setText(Locale.getValue("m.beta_code_error")); break;
        case 1: betaCodeResultLabel.setText(Locale.getValue("m.beta_code_success")); break;
        case 2: betaCodeResultLabel.setText(Locale.getValue("m.beta_code_duplicate")); break;
        case 3: betaCodeResultLabel.setText(Locale.getValue("m.beta_code_already_used")); break;
        case 4: betaCodeResultLabel.setText(Locale.getValue("m.beta_code_already_used_same")); break;
        case 5: betaCodeResultLabel.setText(Locale.getValue("m.beta_code_not_exists")); break;
      }

      betaCodeResultLabel.setVisible(true);

      // Hide the result label after some time.
      Thread hideResultThread = new Thread(() -> {
        betaCodeResultLabel.setVisible(false);
      });
      ThreadingUtil.executeWithDelay(hideResultThread, 5000);
    });

    activeCodesLabel = new JLabel(Locale.getValue("m.beta_code_activated"));
    activeCodesLabel.setHorizontalAlignment(SwingConstants.LEFT);
    activeCodesLabel.setBounds(25, 190, 200, 30);
    activeCodesLabel.setFont(Fonts.fontRegBig);
    activeCodesLabel.setVisible(false);
    betasPanel.add(activeCodesLabel);

    BufferedImage activeCodesBackgroundImage = ImageUtil.generatePlainColorImage(475, 140, CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
    activeCodesBackgroundImage = (BufferedImage) ImageUtil.addRoundedCorners(activeCodesBackgroundImage, 25);
    activeCodesBackground = new JLabel("");
    activeCodesBackground.setIcon(new ImageIcon(activeCodesBackgroundImage));
    activeCodesBackground.setBounds(25, 220, 475, 140);
    activeCodesBackground.setVisible(false);
    betasPanel.add(activeCodesBackground);
    betasPanel.setComponentZOrder(activeCodesBackground, 1);

    activeCodesPane = new JPanel();
    activeCodesPane.setBackground(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
    activeCodesPane.setBorder(null);
    activeCodesPane.setVisible(false);
    betasPanel.setComponentZOrder(activeCodesPane, 0);

    activeCodesPaneScrollBar = new JScrollPane(activeCodesPane);
    activeCodesPaneScrollBar.setBounds(38, 232, 449, 105);
    activeCodesPaneScrollBar.setBackground(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
    activeCodesPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    activeCodesPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    activeCodesPaneScrollBar.setBorder(null);
    activeCodesPaneScrollBar.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    activeCodesPaneScrollBar.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND));
    activeCodesPaneScrollBar.getVerticalScrollBar().setUnitIncrement(16);
    activeCodesPaneScrollBar.setVisible(false);
    betasPanel.add(activeCodesPaneScrollBar);
    betasPanel.setComponentZOrder(activeCodesPaneScrollBar, 0);

    JLabel betaCodeSpecialResultLabel = new JLabel("");
    betaCodeSpecialResultLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeSpecialResultLabel.setBounds(325, 390, 450, 50);
    betaCodeSpecialResultLabel.setFont(Fonts.fontReg);
    betaCodeSpecialResultLabel.setVisible(false);
    betasPanel.add(betaCodeSpecialResultLabel);

    betaCodeRevalidateButton = new JButton(Locale.getValue("b.beta_code_revalidate"));
    betaCodeRevalidateButton.setFont(Fonts.fontMed);
    betaCodeRevalidateButton.setFocusPainted(false);
    betaCodeRevalidateButton.setFocusable(false);
    betaCodeRevalidateButton.setVisible(false);
    betaCodeRevalidateButton.setToolTipText(Locale.getValue("b.beta_code_revalidate"));
    betaCodeRevalidateButton.setBounds(25, 388, 250, 25);
    betasPanel.add(betaCodeRevalidateButton);
    betaCodeRevalidateButton.addActionListener(action -> {
      SettingsEventHandler.revalidateBetaCodes();
      betaCodeSpecialResultLabel.setVisible(true);
      betaCodeSpecialResultLabel.setText(Locale.getValue("m.beta_code_revalidated"));

      // Hide the result label after some time.
      Thread hideResultThread = new Thread(() -> {
        betaCodeSpecialResultLabel.setVisible(false);
      });
      ThreadingUtil.executeWithDelay(hideResultThread, 5000);
    });

    betaCodeClearLocalButton = new JButton(Locale.getValue("b.beta_code_clear_local"));
    betaCodeClearLocalButton.setFont(Fonts.fontMed);
    betaCodeClearLocalButton.setFocusPainted(false);
    betaCodeClearLocalButton.setFocusable(false);
    betaCodeClearLocalButton.setVisible(false);
    betaCodeClearLocalButton.setForeground(CustomColors.BUTTON_FOREGROUND_DANGER);
    betaCodeClearLocalButton.setToolTipText(Locale.getValue("b.beta_code_clear_local"));
    betaCodeClearLocalButton.setBounds(25, 423, 250, 25);
    betasPanel.add(betaCodeClearLocalButton);
    betaCodeClearLocalButton.addActionListener(action -> {
      boolean confirm = Dialog.pushWithConfirm(Locale.getValue("m.destructive_action"), Locale.getValue("b.beta_code_clear_local"), JOptionPane.WARNING_MESSAGE);
      if(confirm) {
        SettingsEventHandler.clearLocalBetaCodes();
        betaCodeSpecialResultLabel.setVisible(true);
        betaCodeSpecialResultLabel.setText(Locale.getValue("m.beta_code_cleared"));

        // Hide the result label after some time.
        Thread hideResultThread = new Thread(() -> {
          betaCodeSpecialResultLabel.setVisible(false);
        });
        ThreadingUtil.executeWithDelay(hideResultThread, 5000);
      }
    });

    return betasPanel;
  }

  protected JPanel createAdvancedPanel() {
    JPanel advancedPanel = new JPanel();
    advancedPanel.setLayout(null);
    advancedPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.advanced"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    advancedPanel.add(headerLabel);

    JLabel labelArguments = new JLabel(Locale.getValue("m.extratxt_write_arguments") + " (Extra.txt)");
    labelArguments.setBounds(25, 90, 600, 20);
    labelArguments.setFont(Fonts.fontRegBig);
    advancedPanel.add(labelArguments);

    argumentsPane = new JEditorPane();
    argumentsPane.setFont(Fonts.fontCodeReg);
    argumentsPane.setBounds(25, 117, 615, 100);
    advancedPanel.add(argumentsPane);
    argumentsPane.setText(Settings.gameAdditionalArgs);
    argumentsPane.addFocusListener(new FocusListener() {
      @Override public void focusLost(FocusEvent e) {
        SettingsEventHandler.saveAdditionalArgs();
      }
      @Override public void focusGained(FocusEvent e) {}
    });

    JScrollPane scrollBar = new JScrollPane(argumentsPane);
    scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollBar.setBounds(25, 117, 590, 100);
    advancedPanel.add(scrollBar);

    argumentsPane.setCaretPosition(0);

    checkExistingArguments();

    JSeparator sep = new JSeparator();
    sep.setBounds(25, 237, 600, 16);
    advancedPanel.add(sep);

    JLabel labelConnectionSettings = new JLabel(Locale.getValue("m.connection_settings"));
    labelConnectionSettings.setBounds(25, 254, 600, 20);
    labelConnectionSettings.setFont(Fonts.fontRegBig);
    advancedPanel.add(labelConnectionSettings);

    labelDisclaimer = new JLabel(Locale.getValue("m.connection_settings_thirdparty_disclaimer"));
    labelDisclaimer.setBounds(195, 255, 300, 18);
    labelDisclaimer.setFont(Fonts.fontReg);
    labelDisclaimer.setHorizontalAlignment(SwingConstants.LEFT);
    labelDisclaimer.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    advancedPanel.add(labelDisclaimer);

    JLabel serverAddressLabel = new JLabel(Locale.getValue("m.server_address"));
    serverAddressLabel.setHorizontalAlignment(SwingConstants.LEFT);
    serverAddressLabel.setBounds(25, 269, 450, 50);
    serverAddressLabel.setFont(Fonts.fontReg);
    advancedPanel.add(serverAddressLabel);

    serverAddressTextField = new JTextField();
    serverAddressTextField.setFont(Fonts.fontCodeReg);
    serverAddressTextField.setBounds(25, 304, 250, 25);
    serverAddressTextField.addActionListener(e -> {
      SettingsEventHandler.saveConnectionSettings();
    });
    advancedPanel.add(serverAddressTextField);
    serverAddressTextField.setText(Settings.gameEndpoint);

    JLabel portLabel = new JLabel(Locale.getValue("m.port"));
    portLabel.setHorizontalAlignment(SwingConstants.LEFT);
    portLabel.setBounds(280, 269, 450, 50);
    portLabel.setFont(Fonts.fontReg);
    advancedPanel.add(portLabel);

    portTextField = new JTextField();
    portTextField.setFont(Fonts.fontCodeReg);
    portTextField.setBounds(280, 304, 55, 25);
    advancedPanel.add(portTextField);
    portTextField.setText(String.valueOf(Settings.gamePort));

    JLabel publicKeyLabel = new JLabel(Locale.getValue("m.public_key"));
    publicKeyLabel.setHorizontalAlignment(SwingConstants.LEFT);
    publicKeyLabel.setBounds(25, 328, 450, 50);
    publicKeyLabel.setFont(Fonts.fontReg);
    advancedPanel.add(publicKeyLabel);

    publicKeyTextField = new JTextField();
    publicKeyTextField.setFont(Fonts.fontCodeReg);
    publicKeyTextField.setBounds(25, 363, 355, 30);

    JScrollBar publicKeyScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel publicKeyPanel = new JPanel();
    publicKeyPanel.setLayout(new BoxLayout(publicKeyPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel publicKeyBRM = publicKeyTextField.getHorizontalVisibility();
    publicKeyScrollBar.setModel(publicKeyBRM);
    publicKeyPanel.add(publicKeyTextField);
    publicKeyPanel.add(publicKeyScrollBar);
    publicKeyPanel.setBounds(25, 363, 355, 38);

    advancedPanel.add(publicKeyPanel);
    publicKeyTextField.setText(Settings.gamePublicKey);

    JLabel getdownURLLabel = new JLabel(Locale.getValue("m.getdown_url"));
    getdownURLLabel.setHorizontalAlignment(SwingConstants.LEFT);
    getdownURLLabel.setBounds(25, 387, 450, 50);
    getdownURLLabel.setFont(Fonts.fontReg);
    advancedPanel.add(getdownURLLabel);

    getdownURLTextField = new JTextField();
    getdownURLTextField.setFont(Fonts.fontCodeReg);
    getdownURLTextField.setBounds(25, 422, 355, 30);

    JScrollBar getdownURLScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel getdownURLPanel = new JPanel();
    getdownURLPanel.setLayout(new BoxLayout(getdownURLPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel getdownURLBRM = getdownURLTextField.getHorizontalVisibility();
    getdownURLScrollBar.setModel(getdownURLBRM);
    getdownURLPanel.add(getdownURLTextField);
    getdownURLPanel.add(getdownURLScrollBar);
    getdownURLPanel.setBounds(25, 422, 355, 38);

    advancedPanel.add(getdownURLPanel);
    getdownURLTextField.setText(Settings.gameGetdownFullURL);

    resetConnectionSettingsButton = new JButton(Locale.getValue("b.reset_default"));
    resetConnectionSettingsButton.setFont(Fonts.fontMed);
    resetConnectionSettingsButton.setBounds(435, 423, 180, 23);
    resetConnectionSettingsButton.setFocusPainted(false);
    resetConnectionSettingsButton.setFocusable(false);
    resetConnectionSettingsButton.setForeground(CustomColors.BUTTON_FOREGROUND_DANGER);
    resetConnectionSettingsButton.setToolTipText(Locale.getValue("b.reset_default"));
    advancedPanel.add(resetConnectionSettingsButton);
    resetConnectionSettingsButton.addActionListener(SettingsEventHandler::resetConnectionSettingsButtonEvent);

    publicKeyTextField.setCaretPosition(0);
    getdownURLTextField.setCaretPosition(0);

    return advancedPanel;
  }

  protected JPanel createAboutPanel() {
    JPanel aboutPanel = new JPanel();
    aboutPanel.setLayout(null);
    aboutPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.about"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    aboutPanel.add(headerLabel);

    JLabel creditsLabel = new JLabel(Locale.getValue("m.credits"));
    creditsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    creditsLabel.setBounds(25, 90, 200, 30);
    creditsLabel.setFont(Fonts.fontRegBig);
    aboutPanel.add(creditsLabel);

    JPanel creditsPane = new JPanel();
    creditsPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JScrollPane creditsPaneScrollBar = new JScrollPane(creditsPane);
    creditsPaneScrollBar.setBounds(-10, 120, 650, 175);
    creditsPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    creditsPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    creditsPaneScrollBar.setBorder(null);
    creditsPaneScrollBar.getVerticalScrollBar().setUnitIncrement(16);
    aboutPanel.add(creditsPaneScrollBar);

    JTextArea credits = new JTextArea();
    credits.setBounds(0, 0, 650, 250);
    credits.setFont(Fonts.fontReg);
    credits.setForeground(CustomColors.INTERFACE_DEFAULT);
    credits.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    credits.setEditable(false);
    credits.setHighlighter(null);
    credits.setText("GitHub Contributors:\n- Luqui (maintainer), Crowfunder, CafuneAndChill, yihleego.\n\nLocalization:\n- asan_ploto (Arabic), yihleego (Chinese), Biral (Deutsch), Airbee (Deutsch), Thyrux (Eesti),\nLuqui (English), Luqui (Español), PtitKrugger (Français), Lawn (Italiano), Kaus (Italiano),\noctopews (Japanese), Crowfunder (Polski), Stret (Português Brasil), Gugaarleo (Português Brasil),\nmilliath (Russian), Puzovoz (Russian).\n\nTesting/QA:\n- Nurr, yihleego, Bidoknight, Carpvindra, Mushspore, CafuneAndChill, Xan, analarmingalarm,\nparma, loonadra, ultrongr, milliath, Puzovoz, Stret, 3xample.\n\nThird Party Libraries:\n- Apache Commons IO, Image4J, Zip4J, flatlaf, discord-rpc, mslinks, org.json, jIconFont,\nsamskivert, JHLabs Filters.");
    creditsPane.add(credits);
    credits.setCaretPosition(0);

    labelFlamingoStatus = new JLabel(Locale.getValue("m.kl_version", LauncherGlobals.LAUNCHER_VERSION));
    labelFlamingoStatus.setBounds(25, 320, 600, 20);
    labelFlamingoStatus.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoStatus);

    labelFlamingoStatus = new JLabel(Locale.getValue("m.flamingo_status", Locale.getValue("m.offline")));
    labelFlamingoStatus.setBounds(25, 340, 600, 20);
    labelFlamingoStatus.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoStatus);

    labelFlamingoVersion = new JLabel(Locale.getValue("m.flamingo_version", "N/A"));
    labelFlamingoVersion.setBounds(25, 360, 600, 20);
    labelFlamingoVersion.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoVersion);

    labelFlamingoUptime = new JLabel(Locale.getValue("m.flamingo_uptime", "N/A"));
    labelFlamingoUptime.setBounds(25, 380, 600, 20);
    labelFlamingoUptime.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoUptime);

    JButton copyLogsButton = new JButton(Locale.getValue("b.copy_logs"));
    copyLogsButton.setFont(Fonts.fontMed);
    copyLogsButton.setBounds(25, 423, 200, 23);
    copyLogsButton.setFocusPainted(false);
    copyLogsButton.setFocusable(false);
    copyLogsButton.setToolTipText(Locale.getValue("b.copy_logs"));
    aboutPanel.add(copyLogsButton);
    copyLogsButton.addActionListener(l -> {
      SettingsEventHandler.copyLogsEvent(l);
      Dialog.push(Locale.getValue("m.logs_copied"), JOptionPane.INFORMATION_MESSAGE);
    });

    return aboutPanel;
  }

  private int getMaxAllowedMemoryAlloc() {
    int MAX_ALLOWED_MEMORY_64_BIT = 4096;
    int MAX_ALLOWED_MEMORY_32_BIT = 1024;

    if (JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64) {
      return MAX_ALLOWED_MEMORY_64_BIT;
    } else {
      return MAX_ALLOWED_MEMORY_32_BIT;
    }
  }

  private static void checkBetaCodes() {
    if(!SettingsProperties.getValue("launcher.betaCodes").trim().isEmpty()) {
      betaCodeRevalidateButton.setVisible(true);
      betaCodeClearLocalButton.setVisible(true);
    }
  }

  private static void checkExistingArguments() {
    // Port all the contents of their existing extra.txt into
    // Knight Launcher's gameAdditionalArgs setting so that it's also preserved in-launcher.
    if(!FileUtil.fileExists("old-extra.txt")) {
      try {
        argumentsPane.setText(FileUtil.readFile("extra.txt").trim());
        SettingsEventHandler.saveAdditionalArgs();
      } catch (IOException e) {
        log.error(e);
      }
    }
  }

  public static void updateActiveBetaCodes() {
    java.util.List<Server> entitledServers = new ArrayList<>();

    for(Server server : LauncherApp.serverList) {
      if(server.fromCode == null) continue;
      if(!server.fromCode.equalsIgnoreCase("null")) {
        entitledServers.add(server);
      }
    }

    if(!entitledServers.isEmpty()) {
      int count = 0;
      for(Server server : entitledServers) {
        JPanel activeCodePane = new JPanel();
        activeCodePane.setLayout(null);
        activeCodePane.setBackground(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
        activeCodePane.setBounds(0, count * 35, 449, 35);

        JLabel activeCodeBadge = new JLabel(server.fromCode);
        activeCodeBadge.setBounds(5, 5, 150, 18);
        activeCodeBadge.setHorizontalAlignment(SwingConstants.CENTER);
        activeCodeBadge.setFont(Fonts.fontRegSmall);
        activeCodeBadge.putClientProperty(FlatClientProperties.STYLE,
          "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_CODE_BACKGROUND)
            + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_CODE_FOREGROUND)
            + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_CODE_BACKGROUND));
        activeCodePane.add(activeCodeBadge);

        JLabel activeCodeText = new JLabel();
        activeCodeText.setBounds(165, 5, 265, 18);
        activeCodeText.setText(Locale.getValue("m.beta_code_entitling", server.name));
        activeCodeText.setHorizontalAlignment(SwingConstants.LEFT);
        activeCodeText.setFont(Fonts.fontRegSmall);
        activeCodePane.add(activeCodeText);

        activeCodesPane.add(activeCodePane);
        count++;
      }
    }

    activeCodesLabel.setVisible(!entitledServers.isEmpty());
    activeCodesBackground.setVisible(!entitledServers.isEmpty());
    activeCodesPane.setVisible(!entitledServers.isEmpty());
    activeCodesPaneScrollBar.setVisible(!entitledServers.isEmpty());

    activeCodesPane.setLayout(null);

    activeCodesPane.setPreferredSize(new Dimension(449, entitledServers.size() * 35));

    activeCodesPaneScrollBar.setBounds(
      activeCodesPaneScrollBar.getX(),
      activeCodesPaneScrollBar.getY(),
      activeCodesPaneScrollBar.getWidth(),
      115
    );

    activeCodesPane.updateUI();
    activeCodesPaneScrollBar.updateUI();
  }
}
