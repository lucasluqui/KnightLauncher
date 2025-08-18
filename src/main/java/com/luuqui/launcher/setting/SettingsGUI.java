package com.luuqui.launcher.setting;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.*;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import org.apache.commons.text.WordUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SettingsGUI extends BaseGUI
{
  @Inject public SettingsEventHandler eventHandler;

  @Inject protected LocaleManager _localeManager;
  @Inject protected SettingsManager _settingsManager;
  @Inject protected FlamingoManager _flamingoManager;

  @Inject
  public SettingsGUI ()
  {
    super(850, 475, false);
  }

  public void init ()
  {
    compose();
    eventHandler.checkBetaCodes();
  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.settings"));
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.getContentPane().setLayout(null);

    tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.setBounds(-2, 20, 852, 455);
    tabbedPane.setFont(Fonts.getFont("defaultMedium", 14.0f, Font.PLAIN));
    tabbedPane.setTabPlacement(JTabbedPane.LEFT);
    tabbedPane.setFocusable(false);
    tabbedPane.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    tabbedPane.addTab(_localeManager.getValue("tab.launcher"), createLauncherPanel());
    tabbedPane.addTab(_localeManager.getValue("tab.game"), createGamePanel());
    tabbedPane.addTab(_localeManager.getValue("tab.betas"), createBetasPanel());
    tabbedPane.addTab(_localeManager.getValue("tab.advanced"), createAdvancedPanel());
    tabbedPane.addTab(_localeManager.getValue("tab.about"), createAboutPanel());

    guiFrame.getContentPane().add(tabbedPane);

    guiFrame.setLocationRelativeTo(null);
  }

  protected JPanel createLauncherPanel ()
  {
    JPanel launcherPanel = new JPanel();
    launcherPanel.setLayout(null);
    launcherPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(_localeManager.getValue("tab.launcher"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    launcherPanel.add(headerLabel);

    launcherSubPanel = new JPanel();
    launcherSubPanel.setBounds(15, 90, 0, 0);
    launcherSubPanel.setPreferredSize(new Dimension(630, 425));
    launcherSubPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    launcherSubPanel.setLayout(null);
    launcherSubPanel.setBorder(null);
    launcherPanel.setComponentZOrder(launcherSubPanel, 0);

    launcherSubPanelScroll = new JScrollPane(launcherSubPanel);
    launcherSubPanelScroll.setBounds(15, 90, 630, 350);
    launcherSubPanelScroll.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    launcherSubPanelScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    launcherSubPanelScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    launcherSubPanelScroll.setBorder(null);
    launcherSubPanelScroll.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    launcherSubPanelScroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_BACKGROUND));
    launcherSubPanelScroll.getVerticalScrollBar().setUnitIncrement(16);
    launcherPanel.add(launcherSubPanelScroll);
    launcherPanel.setComponentZOrder(launcherSubPanelScroll, 0);

    JLabel labelLanguage = new JLabel(_localeManager.getValue("m.language"));
    labelLanguage.setBounds(10, 0, 175, 20);
    labelLanguage.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launcherSubPanel.add(labelLanguage);

    choiceLanguage = new JComboBox<>();
    choiceLanguage.setBounds(95, 0, 150, 20);
    choiceLanguage.setFocusable(false);
    choiceLanguage.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    for (String lang : _localeManager.AVAILABLE_LANGUAGES) {
      choiceLanguage.addItem(lang);
    }
    launcherSubPanel.add(choiceLanguage);
    choiceLanguage.setSelectedItem(_localeManager.getLangName(Settings.lang));
    choiceLanguage.addItemListener(eventHandler::languageChangeEvent);

    JLabel labelCleaning = new JLabel(_localeManager.getValue("m.rebuilds"));
    labelCleaning.setBounds(10, 50, 350, 20);
    labelCleaning.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launcherSubPanel.add(labelCleaning);

    JLabel labelCleaningExplained = new JLabel(_localeManager.getValue("m.file_cleaning_explained"));
    labelCleaningExplained.setBounds(10, 70, 600, 16);
    labelCleaningExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    launcherSubPanel.add(labelCleaningExplained);

    switchCleaning = new JCheckBox("");
    switchCleaning.setBounds(575, 55, 30, 23);
    switchCleaning.setFocusPainted(false);
    launcherSubPanel.add(switchCleaning);
    switchCleaning.setSelected(Settings.doRebuilds);
    switchCleaning.addActionListener(eventHandler::rebuildsChangeEvent);

    JSeparator sep1 = new JSeparator();
    sep1.setBounds(10, 100, 600, 16);
    launcherSubPanel.add(sep1);

    labelFilePurging = new JLabel(_localeManager.getValue("m.file_purging"));
    labelFilePurging.setBounds(10, 115, 350, 20);
    labelFilePurging.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launcherSubPanel.add(labelFilePurging);

    labelFilePurgingExplained = new JLabel(_localeManager.getValue("m.file_purging_explained"));
    labelFilePurgingExplained.setBounds(10, 135, 600, 16);
    labelFilePurgingExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    launcherSubPanel.add(labelFilePurgingExplained);

    switchFilePurging = new JCheckBox("");
    switchFilePurging.setBounds(575, 120, 30, 23);
    switchFilePurging.setFocusPainted(false);
    launcherSubPanel.add(switchFilePurging);
    switchFilePurging.setSelected(Settings.filePurging);
    switchFilePurging.addActionListener(eventHandler::filePurgingChangeEvent);

    JSeparator sep2 = new JSeparator();
    sep2.setBounds(10, 165, 600, 16);
    launcherSubPanel.add(sep2);

    JLabel labelShortcut = new JLabel(_localeManager.getValue("m.create_shortcut"));
    labelShortcut.setBounds(10, 180, 225, 20);
    labelShortcut.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launcherSubPanel.add(labelShortcut);

    JLabel labelShortcutExplained = new JLabel(_localeManager.getValue("m.create_shortcut_explained"));
    labelShortcutExplained.setBounds(10, 200, 600, 16);
    labelShortcutExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    launcherSubPanel.add(labelShortcutExplained);

    switchShortcut = new JCheckBox("");
    switchShortcut.setBounds(575, 185, 30, 23);
    switchShortcut.setFocusPainted(false);
    launcherSubPanel.add(switchShortcut);
    switchShortcut.setSelected(Settings.createShortcut);
    switchShortcut.addActionListener(eventHandler::createShortcutChangeEvent);

    JSeparator sep3 = new JSeparator();
    sep3.setBounds(10, 230, 600, 16);
    launcherSubPanel.add(sep3);

    JLabel labelKeepOpen = new JLabel(_localeManager.getValue("m.keep_open"));
    labelKeepOpen.setBounds(10, 245, 350, 20);
    labelKeepOpen.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launcherSubPanel.add(labelKeepOpen);

    JLabel labelKeepOpenExplained = new JLabel(_localeManager.getValue("m.keep_open_explained"));
    labelKeepOpenExplained.setBounds(10, 265, 600, 16);
    labelKeepOpenExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    launcherSubPanel.add(labelKeepOpenExplained);

    switchKeepOpen = new JCheckBox("");
    switchKeepOpen.setBounds(575, 250, 30, 23);
    switchKeepOpen.setFocusPainted(false);
    launcherSubPanel.add(switchKeepOpen);
    switchKeepOpen.setSelected(Settings.keepOpen);
    switchKeepOpen.addActionListener(eventHandler::keepOpenChangeEvent);

    JSeparator sep4 = new JSeparator();
    sep4.setBounds(10, 295, 600, 16);
    launcherSubPanel.add(sep4);

    labelDiscordIntegration = new JLabel(_localeManager.getValue("m.use_ingame_rpc"));
    labelDiscordIntegration.setBounds(10, 310, 350, 20);
    labelDiscordIntegration.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launcherSubPanel.add(labelDiscordIntegration);

    labelDiscordIntegrationExplained = new JLabel(_localeManager.getValue("m.use_ingame_rpc_explained"));
    labelDiscordIntegrationExplained.setBounds(10, 330, 600, 16);
    labelDiscordIntegrationExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    launcherSubPanel.add(labelDiscordIntegrationExplained);

    switchDiscordIntegration = new JCheckBox("");
    switchDiscordIntegration.setBounds(575, 315, 30, 23);
    switchDiscordIntegration.setFocusPainted(false);
    switchDiscordIntegration.setEnabled(SystemUtil.isWindows() && SystemUtil.is64Bit());
    launcherSubPanel.add(switchDiscordIntegration);
    switchDiscordIntegration.setSelected(Settings.useIngameRPC);
    switchDiscordIntegration.addActionListener(eventHandler::ingameRPCChangeEvent);

    JSeparator sep5 = new JSeparator();
    sep5.setBounds(10, 360, 600, 16);
    launcherSubPanel.add(sep5);

    JLabel labelAutoUpdate = new JLabel(_localeManager.getValue("m.autoupdate"));
    labelAutoUpdate.setBounds(10, 375, 350, 20);
    labelAutoUpdate.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    launcherSubPanel.add(labelAutoUpdate);

    JLabel labelAutoUpdateExplained = new JLabel(_localeManager.getValue("m.autoupdate_explained"));
    labelAutoUpdateExplained.setBounds(10, 395, 600, 16);
    labelAutoUpdateExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    launcherSubPanel.add(labelAutoUpdateExplained);

    switchAutoUpdate = new JCheckBox("");
    switchAutoUpdate.setBounds(575, 380, 30, 23);
    switchAutoUpdate.setFocusPainted(false);
    launcherSubPanel.add(switchAutoUpdate);
    switchAutoUpdate.setSelected(Settings.autoUpdate);
    switchAutoUpdate.addActionListener(eventHandler::autoUpdateChangeEvent);

    return launcherPanel;
  }

  protected JPanel createGamePanel ()
  {
    JPanel gamePanel = new JPanel();
    gamePanel.setLayout(null);
    gamePanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(_localeManager.getValue("tab.game"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    gamePanel.add(headerLabel);

    javaVMBadge = new JLabel("");
    javaVMBadge.setBounds(330, 24, 275, 18);
    javaVMBadge.setHorizontalAlignment(SwingConstants.CENTER);
    javaVMBadge.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
    javaVMBadge.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND) + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_FOREGROUND) + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND));
    gamePanel.add(javaVMBadge);

    gameTabViewingSettingsLabel = new JLabel(_localeManager.getValue("m.viewing_settings", "Official"));
    gameTabViewingSettingsLabel.setBounds(250, 46, 350, 18);
    gameTabViewingSettingsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    gameTabViewingSettingsLabel.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
    gameTabViewingSettingsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    gamePanel.add(gameTabViewingSettingsLabel);

    labelPlatform = new JLabel(_localeManager.getValue("m.platform"));
    labelPlatform.setBounds(25, 90, 125, 18);
    labelPlatform.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    gamePanel.add(labelPlatform);

    choicePlatform = new JComboBox<String>();
    choicePlatform.setBounds(25, 115, 150, 20);
    choicePlatform.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    choicePlatform.setFocusable(false);
    gamePanel.add(choicePlatform);
    choicePlatform.addItem(_localeManager.getValue("o.steam"));
    choicePlatform.addItem(_localeManager.getValue("o.standalone"));
    if(SystemUtil.isWindows() && !SteamUtil.isRunningInSteamapps()) {
      choicePlatform.removeItem(_localeManager.getValue("o.steam"));
    }
    choicePlatform.setSelectedItem(Settings.gamePlatform);
    choicePlatform.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        eventHandler.platformChangeEvent(event);
      }
    });

    labelMemory = new JLabel(_localeManager.getValue("m.allocated_memory"));
    labelMemory.setBounds(275, 90, 275, 18);
    labelMemory.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    gamePanel.add(labelMemory);

    // Make sure the currently selected game memory does not exceed the max.
    Settings.gameMemory = Math.min(Settings.gameMemory, _settingsManager.getMaxAllowedMemoryAlloc());
    eventHandler.memoryChangeEvent(Settings.gameMemory);

    memorySlider = new JSlider(JSlider.HORIZONTAL, 256, _settingsManager.getMaxAllowedMemoryAlloc(), Settings.gameMemory);
    memorySlider.setBounds(265, 105, 350, 40);
    memorySlider.setFocusable(false);
    memorySlider.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    memorySlider.setPaintTicks(true);
    memorySlider.setMinorTickSpacing(256);
    memorySlider.setSnapToTicks(true);
    gamePanel.add(memorySlider);

    memoryValue = new JLabel();
    memoryValue.setBounds(270, 139, 350, 25);
    memoryValue.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    memoryValue.setText(_localeManager.getValue("o.memory_" + Settings.gameMemory));
    gamePanel.add(memoryValue);

    memorySlider.addChangeListener(l -> {
      memoryValue.setText(_localeManager.getValue("o.memory_" + memorySlider.getValue()));
      eventHandler.memoryChangeEvent(memorySlider.getValue());
    });

    JLabel labelUseCustomGC = new JLabel(_localeManager.getValue("m.use_custom_gc"));
    labelUseCustomGC.setBounds(25, 175, 375, 18);
    labelUseCustomGC.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    gamePanel.add(labelUseCustomGC);

    JLabel labelUseCustomGCExplained = new JLabel(_localeManager.getValue("m.use_custom_gc_explained"));
    labelUseCustomGCExplained.setBounds(25, 195, 600, 16);
    labelUseCustomGCExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    gamePanel.add(labelUseCustomGCExplained);

    switchUseCustomGC = new JCheckBox("");
    switchUseCustomGC.setBounds(590, 180, 30, 23);
    switchUseCustomGC.setFocusPainted(false);
    gamePanel.add(switchUseCustomGC);
    switchUseCustomGC.setSelected(Settings.gameUseCustomGC);
    switchUseCustomGC.addActionListener(eventHandler::customGCChangeEvent);
    switchUseCustomGC.setEnabled(SystemUtil.is64Bit());

    choiceGC = new JComboBox<String>();
    choiceGC.setBounds(465, 180, 110, 20);
    choiceGC.setFocusable(false);
    choiceGC.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    gamePanel.add(choiceGC);
    choiceGC.addItem("ParallelOld");
    choiceGC.addItem("Serial");
    choiceGC.addItem("G1");
    choiceGC.setSelectedItem(Settings.gameGarbageCollector);
    choiceGC.addItemListener(eventHandler::choiceGCChangeEvent);
    choiceGC.setEnabled(SystemUtil.is64Bit());

    JSeparator sep2 = new JSeparator();
    sep2.setBounds(25, 225, 600, 16);
    gamePanel.add(sep2);

    JLabel labelExplicitGC = new JLabel(_localeManager.getValue("m.disable_explicit_gc"));
    labelExplicitGC.setBounds(25, 240, 300, 18);
    labelExplicitGC.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    gamePanel.add(labelExplicitGC);

    JLabel labelExplicitGCExplained = new JLabel(_localeManager.getValue("m.explicit_gc_explained"));
    labelExplicitGCExplained.setBounds(25, 260, 600, 16);
    labelExplicitGCExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    gamePanel.add(labelExplicitGCExplained);

    switchExplicitGC = new JCheckBox("");
    switchExplicitGC.setBounds(590, 245, 30, 23);
    switchExplicitGC.setFocusPainted(false);
    gamePanel.add(switchExplicitGC);
    switchExplicitGC.setSelected(Settings.gameDisableExplicitGC);
    switchExplicitGC.addActionListener(eventHandler::disableExplicitGCChangeEvent);
    switchExplicitGC.setEnabled(SystemUtil.is64Bit());

    JSeparator sep3 = new JSeparator();
    sep3.setBounds(25, 290, 600, 16);
    gamePanel.add(sep3);

    JLabel labelFileClean = new JLabel(_localeManager.getValue("m.rebuild_files"));
    labelFileClean.setBounds(25, 305, 275, 18);
    labelFileClean.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    gamePanel.add(labelFileClean);

    JLabel labelFileCleanExplained = new JLabel(_localeManager.getValue("m.rebuild_files_explained"));
    labelFileCleanExplained.setBounds(25, 325, 600, 16);
    labelFileCleanExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    gamePanel.add(labelFileCleanExplained);

    Icon startIcon = IconFontSwing.buildIcon(FontAwesome.SHARE, 16, ColorUtil.getForegroundColor());
    forceRebuildButton = new JButton(startIcon);
    forceRebuildButton.setBounds(585, 310, 30, 23);
    forceRebuildButton.setFocusPainted(false);
    forceRebuildButton.setFocusable(false);
    gamePanel.add(forceRebuildButton);
    forceRebuildButton.addActionListener(action -> {
      //SettingsGUI.settingsGUIFrame.setVisible(false);
      //LauncherGUI.launcherGUIFrame.setVisible(true);
      eventHandler.forceRebuildEvent();
    });

    JSeparator sep4 = new JSeparator();
    sep4.setBounds(25, 355, 600, 16);
    gamePanel.add(sep4);

    JLabel labelJVMPatch = new JLabel(_localeManager.getValue("m.force_jvm_patch"));
    labelJVMPatch.setBounds(25, 370, 350, 18);
    labelJVMPatch.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    gamePanel.add(labelJVMPatch);

    JLabel labelJVMPatchExplained = new JLabel(_localeManager.getValue("m.force_jvm_patch_explained"));
    labelJVMPatchExplained.setBounds(25, 390, 600, 16);
    labelJVMPatchExplained.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    gamePanel.add(labelJVMPatchExplained);

    jvmPatchButton = new JButton(startIcon);
    jvmPatchButton.setBounds(585, 375, 30, 23);
    jvmPatchButton.setFocusPainted(false);
    jvmPatchButton.setFocusable(false);
    jvmPatchButton.setEnabled(false);
    jvmPatchButton.setToolTipText(_localeManager.getValue("error.unsupported_64bit"));
    gamePanel.add(jvmPatchButton);
    jvmPatchButton.addActionListener(eventHandler::jvmPatchEvent);

    if(((SystemUtil.isWindows() && SystemUtil.is64Bit() ) || ( SystemUtil.isUnix() && Settings.gamePlatform.equalsIgnoreCase("Steam")))) {
      jvmPatchButton.setEnabled(true);
      jvmPatchButton.setToolTipText(null);
    }

    JButton loadRecommendedSettingsButton = new JButton(_localeManager.getValue("b.recommended_settings"));
    loadRecommendedSettingsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    loadRecommendedSettingsButton.setBounds(195, 423, 230, 23);
    loadRecommendedSettingsButton.setFocusPainted(false);
    loadRecommendedSettingsButton.setFocusable(false);
    loadRecommendedSettingsButton.setToolTipText(_localeManager.getValue("b.recommended_settings"));
    gamePanel.add(loadRecommendedSettingsButton);
    loadRecommendedSettingsButton.addActionListener(eventHandler::loadRecommendedSettingsButtonEvent);

    resetGameSettingsButton = new JButton(_localeManager.getValue("b.reset_default"));
    resetGameSettingsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    resetGameSettingsButton.setBounds(435, 423, 180, 23);
    resetGameSettingsButton.setFocusPainted(false);
    resetGameSettingsButton.setFocusable(false);
    resetGameSettingsButton.setForeground(CustomColors.DANGER);
    resetGameSettingsButton.setToolTipText(_localeManager.getValue("b.reset_default"));
    gamePanel.add(resetGameSettingsButton);
    resetGameSettingsButton.addActionListener(eventHandler::resetGameSettingsButtonEvent);

    return gamePanel;
  }

  protected JPanel createBetasPanel ()
  {
    JPanel betasPanel = new JPanel();
    betasPanel.setLayout(null);
    betasPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(_localeManager.getValue("tab.betas"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    betasPanel.add(headerLabel);

    JLabel betaCodeLabel = new JLabel(_localeManager.getValue("m.beta_code_activate"));
    betaCodeLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeLabel.setBounds(25, 72, 450, 50);
    betaCodeLabel.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    betasPanel.add(betaCodeLabel);

    betaCodeTextField = new JTextField();
    betaCodeTextField.setFont(Fonts.getFont("codeRegular", 12.0f, Font.PLAIN));
    betaCodeTextField.setBounds(25, 112, 250, 25);
    betasPanel.add(betaCodeTextField);

    JLabel betaCodeResultLabel = new JLabel("");
    betaCodeResultLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeResultLabel.setBounds(25, 127, 450, 50);
    betaCodeResultLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    betaCodeResultLabel.setVisible(false);
    betasPanel.add(betaCodeResultLabel);

    JButton betaCodeButton = new JButton(_localeManager.getValue("b.activate"));
    betaCodeButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    betaCodeButton.setFocusPainted(false);
    betaCodeButton.setFocusable(false);
    betaCodeButton.setToolTipText(_localeManager.getValue("b.activate"));
    betaCodeButton.setBounds(290, 112, 100, 25);
    betasPanel.add(betaCodeButton);
    betaCodeButton.addActionListener(action -> {
      int result = eventHandler.activateBetaCode(betaCodeTextField.getText(), false);

      // TODO: use enums.
      switch (result) {
        case -1: betaCodeResultLabel.setText(_localeManager.getValue("m.beta_code_invalid")); break;
        case 0: betaCodeResultLabel.setText(_localeManager.getValue("m.beta_code_error")); break;
        case 1: betaCodeResultLabel.setText(_localeManager.getValue("m.beta_code_success")); break;
        case 2: betaCodeResultLabel.setText(_localeManager.getValue("m.beta_code_duplicate")); break;
        case 3: betaCodeResultLabel.setText(_localeManager.getValue("m.beta_code_already_used")); break;
        case 4: betaCodeResultLabel.setText(_localeManager.getValue("m.beta_code_already_used_same")); break;
        case 5: betaCodeResultLabel.setText(_localeManager.getValue("m.beta_code_not_exists")); break;
      }

      betaCodeResultLabel.setVisible(true);

      // Hide the result label after some time.
      Thread hideResultThread = new Thread(() -> {
        betaCodeResultLabel.setVisible(false);
      });
      ThreadingUtil.executeWithDelay(hideResultThread, 5000);
    });

    activeCodesLabel = new JLabel(_localeManager.getValue("m.beta_code_activated"));
    activeCodesLabel.setHorizontalAlignment(SwingConstants.LEFT);
    activeCodesLabel.setBounds(25, 190, 200, 30);
    activeCodesLabel.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
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
    betaCodeSpecialResultLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    betaCodeSpecialResultLabel.setVisible(false);
    betasPanel.add(betaCodeSpecialResultLabel);

    betaCodeRevalidateButton = new JButton(_localeManager.getValue("b.beta_code_revalidate"));
    betaCodeRevalidateButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    betaCodeRevalidateButton.setFocusPainted(false);
    betaCodeRevalidateButton.setFocusable(false);
    betaCodeRevalidateButton.setVisible(false);
    betaCodeRevalidateButton.setToolTipText(_localeManager.getValue("b.beta_code_revalidate"));
    betaCodeRevalidateButton.setBounds(25, 388, 250, 25);
    betasPanel.add(betaCodeRevalidateButton);
    betaCodeRevalidateButton.addActionListener(action -> {
      eventHandler.revalidateBetaCodes();
      betaCodeSpecialResultLabel.setVisible(true);
      betaCodeSpecialResultLabel.setText(_localeManager.getValue("m.beta_code_revalidated"));

      // Hide the result label after some time.
      Thread hideResultThread = new Thread(() -> {
        betaCodeSpecialResultLabel.setVisible(false);
      });
      ThreadingUtil.executeWithDelay(hideResultThread, 5000);
    });

    betaCodeClearLocalButton = new JButton(_localeManager.getValue("b.beta_code_clear_local"));
    betaCodeClearLocalButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    betaCodeClearLocalButton.setFocusPainted(false);
    betaCodeClearLocalButton.setFocusable(false);
    betaCodeClearLocalButton.setVisible(false);
    betaCodeClearLocalButton.setForeground(CustomColors.DANGER);
    betaCodeClearLocalButton.setToolTipText(_localeManager.getValue("b.beta_code_clear_local"));
    betaCodeClearLocalButton.setBounds(25, 423, 250, 25);
    betasPanel.add(betaCodeClearLocalButton);
    betaCodeClearLocalButton.addActionListener(action -> {
      boolean confirm = Dialog.pushWithConfirm(_localeManager.getValue("m.destructive_action"), _localeManager.getValue("b.beta_code_clear_local"), JOptionPane.WARNING_MESSAGE);
      if (confirm) {
        eventHandler.clearLocalBetaCodes();
        betaCodeSpecialResultLabel.setVisible(true);
        betaCodeSpecialResultLabel.setText(_localeManager.getValue("m.beta_code_cleared"));

        // Hide the result label after some time.
        Thread hideResultThread = new Thread(() -> {
          betaCodeSpecialResultLabel.setVisible(false);
        });
        ThreadingUtil.executeWithDelay(hideResultThread, 5000);
      }
    });

    return betasPanel;
  }

  public void updateActiveBetaCodes ()
  {
    List<Server> entitledServers = new ArrayList<>();

    for (Server server : _flamingoManager.getServerList()) {
      if (server.fromCode == null) continue;
      if (!server.fromCode.equalsIgnoreCase("null")) {
        entitledServers.add(server);
      }
    }

    if (!entitledServers.isEmpty()) {
      int count = 0;
      for (Server server : entitledServers) {
        JPanel activeCodePane = new JPanel();
        activeCodePane.setLayout(null);
        activeCodePane.setBackground(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
        activeCodePane.setBounds(0, count * 35, 449, 35);

        JLabel activeCodeBadge = new JLabel(server.fromCode);
        activeCodeBadge.setBounds(5, 5, 150, 18);
        activeCodeBadge.setHorizontalAlignment(SwingConstants.CENTER);
        activeCodeBadge.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
        activeCodeBadge.putClientProperty(FlatClientProperties.STYLE,
            "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_CODE_BACKGROUND)
                + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_CODE_FOREGROUND)
                + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_CODE_BACKGROUND));
        activeCodePane.add(activeCodeBadge);

        JLabel activeCodeText = new JLabel();
        activeCodeText.setBounds(165, 5, 265, 18);
        activeCodeText.setText(_localeManager.getValue("m.beta_code_entitling", server.name));
        activeCodeText.setHorizontalAlignment(SwingConstants.LEFT);
        activeCodeText.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
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

  protected JPanel createAdvancedPanel ()
  {
    JPanel advancedPanel = new JPanel();
    advancedPanel.setLayout(null);
    advancedPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(_localeManager.getValue("tab.advanced"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    advancedPanel.add(headerLabel);

    advancedTabViewingSettingsLabel = new JLabel(_localeManager.getValue("m.viewing_settings", "Official"));
    advancedTabViewingSettingsLabel.setBounds(250, 35, 350, 18);
    advancedTabViewingSettingsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    advancedTabViewingSettingsLabel.setFont(Fonts.getFont("defaultRegular", 9.0f, Font.ITALIC));
    advancedTabViewingSettingsLabel.setForeground(CustomColors.INTERFACE_MAINPANE_FOOTNOTE);
    advancedPanel.add(advancedTabViewingSettingsLabel);

    JLabel labelArguments = new JLabel(_localeManager.getValue("m.extratxt_write_arguments") + " (Extra.txt)");
    labelArguments.setBounds(25, 90, 600, 20);
    labelArguments.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    advancedPanel.add(labelArguments);

    argumentsPane = new JEditorPane();
    argumentsPane.setFont(Fonts.getFont("codeRegular", 12.0f, Font.PLAIN));
    argumentsPane.setBounds(25, 117, 615, 100);
    advancedPanel.add(argumentsPane);
    argumentsPane.setText(Settings.gameAdditionalArgs);
    argumentsPane.addFocusListener(new FocusListener() {
      @Override public void focusLost(FocusEvent e) {
        eventHandler.saveAdditionalArgs();
      }
      @Override public void focusGained(FocusEvent e) {}
    });

    JScrollPane scrollBar = new JScrollPane(argumentsPane);
    scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollBar.setBounds(25, 117, 590, 100);
    advancedPanel.add(scrollBar);

    argumentsPane.setCaretPosition(0);

    eventHandler.checkExistingArguments();

    JSeparator sep = new JSeparator();
    sep.setBounds(25, 237, 600, 16);
    advancedPanel.add(sep);

    JLabel labelConnectionSettings = new JLabel(_localeManager.getValue("m.connection_settings"));
    labelConnectionSettings.setBounds(25, 254, 600, 20);
    labelConnectionSettings.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    advancedPanel.add(labelConnectionSettings);

    labelDisclaimer = new JLabel(_localeManager.getValue("m.connection_settings_thirdparty_disclaimer"));
    labelDisclaimer.setBounds(195, 255, 300, 18);
    labelDisclaimer.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    labelDisclaimer.setHorizontalAlignment(SwingConstants.LEFT);
    labelDisclaimer.setForeground(CustomColors.DANGER);
    advancedPanel.add(labelDisclaimer);

    JLabel serverAddressLabel = new JLabel(_localeManager.getValue("m.server_address"));
    serverAddressLabel.setHorizontalAlignment(SwingConstants.LEFT);
    serverAddressLabel.setBounds(25, 269, 450, 50);
    serverAddressLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    advancedPanel.add(serverAddressLabel);

    serverAddressTextField = new JTextField();
    serverAddressTextField.setFont(Fonts.getFont("codeRegular", 12.0f, Font.PLAIN));
    serverAddressTextField.setBounds(25, 304, 250, 25);
    serverAddressTextField.addActionListener(e -> {
      eventHandler.saveConnectionSettings();
    });
    advancedPanel.add(serverAddressTextField);
    serverAddressTextField.setText(Settings.gameEndpoint);

    JLabel portLabel = new JLabel(_localeManager.getValue("m.port"));
    portLabel.setHorizontalAlignment(SwingConstants.LEFT);
    portLabel.setBounds(280, 269, 450, 50);
    portLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    advancedPanel.add(portLabel);

    portTextField = new JTextField();
    portTextField.setFont(Fonts.getFont("codeRegular", 12.0f, Font.PLAIN));
    portTextField.setBounds(280, 304, 55, 25);
    advancedPanel.add(portTextField);
    portTextField.setText(String.valueOf(Settings.gamePort));

    JLabel publicKeyLabel = new JLabel(_localeManager.getValue("m.public_key"));
    publicKeyLabel.setHorizontalAlignment(SwingConstants.LEFT);
    publicKeyLabel.setBounds(25, 328, 450, 50);
    publicKeyLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    advancedPanel.add(publicKeyLabel);

    publicKeyTextField = new JTextField();
    publicKeyTextField.setFont(Fonts.getFont("codeRegular", 12.0f, Font.PLAIN));
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

    JLabel getdownURLLabel = new JLabel(_localeManager.getValue("m.getdown_url"));
    getdownURLLabel.setHorizontalAlignment(SwingConstants.LEFT);
    getdownURLLabel.setBounds(25, 387, 450, 50);
    getdownURLLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    advancedPanel.add(getdownURLLabel);

    getdownURLTextField = new JTextField();
    getdownURLTextField.setFont(Fonts.getFont("codeRegular", 12.0f, Font.PLAIN));
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

    resetConnectionSettingsButton = new JButton(_localeManager.getValue("b.reset_default"));
    resetConnectionSettingsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    resetConnectionSettingsButton.setBounds(435, 423, 180, 23);
    resetConnectionSettingsButton.setFocusPainted(false);
    resetConnectionSettingsButton.setFocusable(false);
    resetConnectionSettingsButton.setForeground(CustomColors.DANGER);
    resetConnectionSettingsButton.setToolTipText(_localeManager.getValue("b.reset_default"));
    advancedPanel.add(resetConnectionSettingsButton);
    resetConnectionSettingsButton.addActionListener(eventHandler::resetConnectionSettingsButtonEvent);

    publicKeyTextField.setCaretPosition(0);
    getdownURLTextField.setCaretPosition(0);

    return advancedPanel;
  }

  protected JPanel createAboutPanel ()
  {
    JPanel aboutPanel = new JPanel();
    aboutPanel.setLayout(null);
    aboutPanel.setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);

    JLabel headerLabel = new JLabel(_localeManager.getValue("tab.about"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.getFont("defaultMedium", 40.0f, Font.PLAIN));
    aboutPanel.add(headerLabel);

    JLabel creditsLabel = new JLabel(_localeManager.getValue("m.credits"));
    creditsLabel.setHorizontalAlignment(SwingConstants.LEFT);
    creditsLabel.setBounds(25, 90, 200, 30);
    creditsLabel.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    aboutPanel.add(creditsLabel);

    BufferedImage creditsBackgroundImage = ImageUtil.generatePlainColorImage(565, 195, CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);
    creditsBackgroundImage = (BufferedImage) ImageUtil.addRoundedCorners(creditsBackgroundImage, 25);
    JLabel creditsBackground = new JLabel("");
    creditsBackground.setIcon(new ImageIcon(creditsBackgroundImage));
    creditsBackground.setBounds(20, 120, 575, 195);
    aboutPanel.add(creditsBackground);
    aboutPanel.setComponentZOrder(creditsBackground, 1);

    JPanel creditsPane = new JPanel();
    creditsPane.setBackground(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND);

    JScrollPane creditsPaneScrollBar = new JScrollPane(creditsPane);
    creditsPaneScrollBar.setBounds(25, 130, 550, 175);
    creditsPaneScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    creditsPaneScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    creditsPaneScrollBar.setBorder(null);
    creditsPaneScrollBar.putClientProperty(FlatClientProperties.STYLE, "border:0,0,0,0");
    creditsPaneScrollBar.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_MAINPANE_SUBBACKGROUND));
    creditsPaneScrollBar.getVerticalScrollBar().setUnitIncrement(16);
    aboutPanel.add(creditsPaneScrollBar);

    JTextArea credits = new JTextArea();
    credits.setBounds(0, 0, 550, 250);
    credits.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    credits.setForeground(CustomColors.INTERFACE_DEFAULT);
    credits.setBackground(null);
    credits.setEditable(false);
    credits.setHighlighter(null);
    credits.setAlignmentX(0);
    credits.setText(_localeManager.getValue(
        "m.credits_template", new String[] {
            WordUtils.wrap(_localeManager.getValue("m.credits_contributors"), 90),
            _localeManager.getValue("m.credits_translators"),
            WordUtils.wrap(_localeManager.getValue("m.credits_qa"), 90),
            WordUtils.wrap(_localeManager.getValue("m.credits_libs"), 90)
        })
    );
    creditsPane.add(credits);
    credits.setCaretPosition(0);

    aboutPanel.setComponentZOrder(creditsPaneScrollBar, 0);

    labelFlamingoStatus = new JLabel(_localeManager.getValue("m.kl_version", BuildConfig.getVersion()));
    labelFlamingoStatus.setBounds(25, 327, 600, 20);
    labelFlamingoStatus.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    aboutPanel.add(labelFlamingoStatus);

    labelFlamingoStatus = new JLabel(_localeManager.getValue("m.flamingo_status", _localeManager.getValue("m.offline")));
    labelFlamingoStatus.setBounds(25, 347, 600, 20);
    labelFlamingoStatus.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    aboutPanel.add(labelFlamingoStatus);

    labelFlamingoVersion = new JLabel(_localeManager.getValue("m.flamingo_version", "N/A"));
    labelFlamingoVersion.setBounds(25, 367, 600, 20);
    labelFlamingoVersion.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    //aboutPanel.add(labelFlamingoVersion);

    labelFlamingoUptime = new JLabel(_localeManager.getValue("m.flamingo_uptime", "N/A"));
    labelFlamingoUptime.setBounds(25, 387, 600, 20);
    labelFlamingoUptime.setFont(Fonts.getFont("defaultRegular", 14.0f, Font.ITALIC));
    //aboutPanel.add(labelFlamingoUptime);

    JButton copyLogsButton = new JButton(_localeManager.getValue("b.copy_logs"));
    copyLogsButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    copyLogsButton.setBounds(25, 423, 200, 23);
    copyLogsButton.setFocusPainted(false);
    copyLogsButton.setFocusable(false);
    copyLogsButton.setToolTipText(_localeManager.getValue("b.copy_logs"));
    aboutPanel.add(copyLogsButton);
    copyLogsButton.addActionListener(l -> {
      eventHandler.copyLogsEvent(l);
      Dialog.push(_localeManager.getValue("m.logs_copied"), JOptionPane.INFORMATION_MESSAGE);
    });

    JButton openRootFolderButton = new JButton(_localeManager.getValue("b.open_game_dir"));
    openRootFolderButton.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    openRootFolderButton.setBounds(235, 423, 200, 23);
    openRootFolderButton.setFocusPainted(false);
    openRootFolderButton.setFocusable(false);
    openRootFolderButton.setToolTipText(_localeManager.getValue("b.open_game_dir"));
    aboutPanel.add(openRootFolderButton);
    openRootFolderButton.addActionListener(eventHandler::openRootFolderEvent);

    return aboutPanel;
  }

  public JTabbedPane tabbedPane;
  public JComboBox<String> choicePlatform = new JComboBox<String>();
  public JComboBox<String> choiceLanguage;
  public JComboBox<String> choiceGC;
  public JCheckBox switchCleaning;
  public JCheckBox switchKeepOpen;
  public JCheckBox switchShortcut;
  public JButton jvmPatchButton = new JButton();
  public JButton forceRebuildButton = new JButton();
  public JCheckBox switchExplicitGC;
  public JCheckBox switchUseCustomGC;
  public JSlider memorySlider;
  public JLabel memoryValue;
  public JCheckBox switchDiscordIntegration = new JCheckBox();
  public JCheckBox switchAutoUpdate;
  public JEditorPane argumentsPane;
  public JLabel labelDisclaimer = new JLabel();
  public JTextField serverAddressTextField = new JTextField();
  public JTextField portTextField = new JTextField();
  public JTextField publicKeyTextField = new JTextField();
  public JTextField getdownURLTextField = new JTextField();
  public JButton resetConnectionSettingsButton = new JButton();
  public JTextField betaCodeTextField;
  public JLabel labelFlamingoStatus = new JLabel();
  public JLabel labelFlamingoVersion = new JLabel();
  public JLabel labelFlamingoUptime = new JLabel();
  public JButton betaCodeRevalidateButton;
  public JButton betaCodeClearLocalButton;
  public JButton resetGameSettingsButton;
  public JLabel javaVMBadge = new JLabel();
  public JLabel gameTabViewingSettingsLabel = new JLabel();
  public JLabel advancedTabViewingSettingsLabel = new JLabel();
  public JLabel activeCodesLabel = new JLabel();
  public JLabel activeCodesBackground = new JLabel();
  public JLabel labelPlatform = new JLabel();
  public JLabel labelMemory = new JLabel();
  public JLabel labelDiscordIntegration = new JLabel();
  public JLabel labelDiscordIntegrationExplained = new JLabel();
  public JPanel activeCodesPane = new JPanel();
  public JScrollPane activeCodesPaneScrollBar = new JScrollPane();
  public JPanel launcherSubPanel = new JPanel();
  public JScrollPane launcherSubPanelScroll = new JScrollPane();
  public JLabel labelFilePurging;
  public JLabel labelFilePurgingExplained;
  public JCheckBox switchFilePurging;

}
