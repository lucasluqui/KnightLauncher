package com.luuqui.launcher.setting;

import com.formdev.flatlaf.FlatClientProperties;
import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.*;
import com.luuqui.launcher.mod.data.ZipMod;
import com.luuqui.util.ColorUtil;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.SteamUtil;
import com.luuqui.util.SystemUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.event.*;

import static com.luuqui.launcher.setting.Log.log;

public class SettingsGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame settingsGUIFrame;
  public static JTabbedPane tabbedPane;
  public static JComboBox<String> choicePlatform;
  public static JComboBox<String> choiceLanguage;
  public static JComboBox<String> choiceStyle;
  public static JComboBox<String> choiceGC;
  public static JCheckBox switchCleaning;
  public static JCheckBox switchKeepOpen;
  public static JCheckBox switchShortcut;
  public static JButton forceRebuildButton;
  public static JCheckBox switchStringDedup;
  public static JCheckBox switchExplicitGC;
  public static JCheckBox switchUseCustomGC;
  public static JCheckBox switchUseIngameRPC;
  public static JCheckBox switchAutoUpdate;
  public static JEditorPane argumentsPane;
  public static JTextField serverAddressTextField;
  public static JTextField portTextField;
  public static JTextField publicKeyTextField;
  public static JTextField getdownURLTextField;
  public static JTextField betaCodeTextField;
  public static JLabel labelFlamingoStatus;
  public static JLabel labelFlamingoVersion;
  public static JLabel labelFlamingoUptime;
  public static JButton betaCodeRevalidateButton;
  public static JButton betaCodeClearLocalButton;

  @SuppressWarnings("static-access")
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

    JLabel labelAutoUpdate = new JLabel("Auto-update");
    labelAutoUpdate.setBounds(25, 335, 350, 20);
    labelAutoUpdate.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelAutoUpdate);

    JLabel labelAutoUpdateExplained = new JLabel("Automatically download and apply launcher updates when they're available.");
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
    labelMemory.setBounds(225, 90, 275, 18);
    labelMemory.setFont(Fonts.fontRegBig);
    gamePanel.add(labelMemory);

    // Make sure the currently selected game memory does not exceed the max.
    Settings.gameMemory = Math.min(Settings.gameMemory, getMaxAllowedMemoryAlloc());
    SettingsEventHandler.memoryChangeEvent(Settings.gameMemory);

    JSlider memorySlider = new JSlider(JSlider.HORIZONTAL, 256, getMaxAllowedMemoryAlloc(), Settings.gameMemory);
    memorySlider.setBounds(215, 105, 350, 40);
    memorySlider.setFocusable(false);
    memorySlider.setFont(Fonts.fontReg);
    memorySlider.setPaintTicks(true);
    memorySlider.setMinorTickSpacing(256);
    memorySlider.setSnapToTicks(true);
    gamePanel.add(memorySlider);

    JLabel memoryValue = new JLabel();
    memoryValue.setBounds(220, 135, 350, 25);
    memoryValue.setFont(Fonts.fontReg);
    memoryValue.setText(Locale.getValue("o.memory_" + Settings.gameMemory));
    gamePanel.add(memoryValue);

    memorySlider.addChangeListener(l -> {
      memoryValue.setText(Locale.getValue("o.memory_" + memorySlider.getValue()));
      SettingsEventHandler.memoryChangeEvent(memorySlider.getValue());
    });

    JLabel labelUseCustomGC = new JLabel("Use a different GC behavior");
    labelUseCustomGC.setBounds(25, 175, 375, 18);
    labelUseCustomGC.setFont(Fonts.fontRegBig);
    gamePanel.add(labelUseCustomGC);

    JLabel labelUseCustomGCExplained = new JLabel("Change how Garbage Collection will be done on the game's Java VM.");
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
    choiceGC.setBounds(475, 180, 100, 20);
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
    JButton forceRebuildButton = new JButton(startIcon);
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

    JLabel javaVMBadge = new JLabel("Your Java VM: " + JavaUtil.getReadableGameJVMData());
    javaVMBadge.setBounds(25, 425, 210, 18);
    javaVMBadge.setHorizontalAlignment(SwingConstants.CENTER);
    javaVMBadge.setFont(Fonts.fontRegSmall);
    javaVMBadge.putClientProperty(FlatClientProperties.STYLE,
      "background:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND) + "1A; foreground:" + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_FOREGROUND) + "; arc:999; border:2,8,2,8," + ColorUtil.colorToHexString(CustomColors.INTERFACE_SETTINGS_BADGE_JVM_BACKGROUND));
    gamePanel.add(javaVMBadge);

    JButton jvmPatchButton = new JButton(startIcon);
    jvmPatchButton.setBounds(585, 375, 30, 23);
    jvmPatchButton.setFocusPainted(false);
    jvmPatchButton.setFocusable(false);
    jvmPatchButton.setEnabled(false);
    jvmPatchButton.setToolTipText("Your system does not support a 64-bit Java VM.");
    gamePanel.add(jvmPatchButton);
    jvmPatchButton.addActionListener(action -> SettingsEventHandler.jvmPatchEvent(action));

    if(((SystemUtil.isWindows() && SystemUtil.is64Bit() ) || ( SystemUtil.isUnix() && Settings.gamePlatform.startsWith("Steam")))) {
      jvmPatchButton.setEnabled(true);
      jvmPatchButton.setToolTipText(null);
    }

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

    JLabel betaCodeLabel = new JLabel("Activate a Beta code");
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

    JButton betaCodeButton = new JButton("Activate");
    betaCodeButton.setFont(Fonts.fontMed);
    betaCodeButton.setFocusPainted(false);
    betaCodeButton.setFocusable(false);
    betaCodeButton.setToolTipText("Activate");
    betaCodeButton.setBounds(290, 112, 100, 25);
    betasPanel.add(betaCodeButton);
    betaCodeButton.addActionListener(action -> {
      int result = SettingsEventHandler.activateBetaCode(betaCodeTextField.getText(), false);

      switch(result) {
        case 0: betaCodeResultLabel.setText("An unexpected error has occurred."); break;
        case 1: betaCodeResultLabel.setText("Successfully activated Beta code. (Check server list)"); break;
        case 2: betaCodeResultLabel.setText("You already activated this Beta code."); break;
        case 3: betaCodeResultLabel.setText("This Beta code was already activated."); break;
        case 4: betaCodeResultLabel.setText("You already activated this Beta code."); break;
        case 5: betaCodeResultLabel.setText("This Beta code does not exist."); break;
      }

      betaCodeResultLabel.setVisible(true);
    });

    JLabel betaCodeSpecialResultLabel = new JLabel("");
    betaCodeSpecialResultLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeSpecialResultLabel.setBounds(25, 350, 450, 50);
    betaCodeSpecialResultLabel.setFont(Fonts.fontReg);
    betaCodeSpecialResultLabel.setVisible(false);
    betasPanel.add(betaCodeSpecialResultLabel);

    betaCodeRevalidateButton = new JButton("Revalidate my Beta codes");
    betaCodeRevalidateButton.setFont(Fonts.fontMed);
    betaCodeRevalidateButton.setFocusPainted(false);
    betaCodeRevalidateButton.setFocusable(false);
    betaCodeRevalidateButton.setVisible(false);
    betaCodeRevalidateButton.setToolTipText("Revalidate my Beta codes");
    betaCodeRevalidateButton.setBounds(25, 300, 250, 25);
    betasPanel.add(betaCodeRevalidateButton);
    betaCodeRevalidateButton.addActionListener(action -> {
      SettingsEventHandler.revalidateBetaCodes();
      betaCodeSpecialResultLabel.setVisible(true);
      betaCodeSpecialResultLabel.setText("Beta codes revalidated.");
    });

    betaCodeClearLocalButton = new JButton("Clear locally stored Beta codes");
    betaCodeClearLocalButton.setFont(Fonts.fontMed);
    betaCodeClearLocalButton.setFocusPainted(false);
    betaCodeClearLocalButton.setFocusable(false);
    betaCodeClearLocalButton.setVisible(false);
    betaCodeClearLocalButton.setForeground(CustomColors.BUTTON_FOREGROUND_DANGER);
    betaCodeClearLocalButton.setToolTipText("Clear locally stored Beta codes");
    betaCodeClearLocalButton.setBounds(25, 335, 250, 25);
    betasPanel.add(betaCodeClearLocalButton);
    betaCodeClearLocalButton.addActionListener(action -> {
      SettingsEventHandler.clearLocalBetaCodes();
      betaCodeSpecialResultLabel.setVisible(true);
      betaCodeSpecialResultLabel.setText("Beta codes cleared.");
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

    JScrollPane scrollBar = new JScrollPane(argumentsPane);
    scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollBar.setBounds(25, 117, 632, 100);
    advancedPanel.add(scrollBar);

    JSeparator sep = new JSeparator();
    sep.setBounds(25, 237, 600, 16);
    advancedPanel.add(sep);

    JLabel labelConnectionSettings = new JLabel("Connection Settings");
    labelConnectionSettings.setBounds(25, 254, 600, 18);
    labelConnectionSettings.setFont(Fonts.fontRegBig);
    advancedPanel.add(labelConnectionSettings);

    JLabel serverAddressLabel = new JLabel("Server Address");
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

    JLabel portLabel = new JLabel("Port");
    portLabel.setHorizontalAlignment(SwingConstants.LEFT);
    portLabel.setBounds(280, 269, 450, 50);
    portLabel.setFont(Fonts.fontReg);
    advancedPanel.add(portLabel);

    portTextField = new JTextField();
    portTextField.setFont(Fonts.fontCodeReg);
    portTextField.setBounds(280, 304, 55, 25);
    advancedPanel.add(portTextField);
    portTextField.setText(String.valueOf(Settings.gamePort));

    JLabel publicKeyLabel = new JLabel("Public Key");
    publicKeyLabel.setHorizontalAlignment(SwingConstants.LEFT);
    publicKeyLabel.setBounds(25, 324, 450, 50);
    publicKeyLabel.setFont(Fonts.fontReg);
    advancedPanel.add(publicKeyLabel);

    publicKeyTextField = new JTextField();
    publicKeyTextField.setFont(Fonts.fontCodeReg);
    publicKeyTextField.setBounds(25, 359, 355, 30);

    JScrollBar publicKeyScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel publicKeyPanel = new JPanel();
    publicKeyPanel.setLayout(new BoxLayout(publicKeyPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel publicKeyBRM = publicKeyTextField.getHorizontalVisibility();
    publicKeyScrollBar.setModel(publicKeyBRM);
    publicKeyPanel.add(publicKeyTextField);
    publicKeyPanel.add(publicKeyScrollBar);
    publicKeyPanel.setBounds(25, 359, 355, 30);

    advancedPanel.add(publicKeyPanel);
    publicKeyTextField.setText(Settings.gamePublicKey);

    JLabel getdownURLLabel = new JLabel("Getdown URL");
    getdownURLLabel.setHorizontalAlignment(SwingConstants.LEFT);
    getdownURLLabel.setBounds(25, 379, 450, 50);
    getdownURLLabel.setFont(Fonts.fontReg);
    advancedPanel.add(getdownURLLabel);

    getdownURLTextField = new JTextField();
    getdownURLTextField.setFont(Fonts.fontCodeReg);
    getdownURLTextField.setBounds(25, 414, 355, 30);

    JScrollBar getdownURLScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel getdownURLPanel = new JPanel();
    getdownURLPanel.setLayout(new BoxLayout(getdownURLPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel getdownURLBRM = getdownURLTextField.getHorizontalVisibility();
    getdownURLScrollBar.setModel(getdownURLBRM);
    getdownURLPanel.add(getdownURLTextField);
    getdownURLPanel.add(getdownURLScrollBar);
    getdownURLPanel.setBounds(25, 414, 355, 30);

    advancedPanel.add(getdownURLPanel);
    getdownURLTextField.setText(Settings.gameGetdownFullURL);

    JButton resetButton = new JButton("Reset values to default");
    resetButton.setFont(Fonts.fontMed);
    resetButton.setBounds(400, 414, 180, 23);
    resetButton.setFocusPainted(false);
    resetButton.setFocusable(false);
    resetButton.setToolTipText("Reset values to default");
    advancedPanel.add(resetButton);
    resetButton.addActionListener(action -> SettingsEventHandler.resetButtonEvent(action));

    serverAddressTextField.setEnabled(true);
    portTextField.setEnabled(true);
    publicKeyTextField.setEnabled(true);
    getdownURLTextField.setEnabled(true);

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

    labelFlamingoStatus = new JLabel("Knight Launcher version: " + LauncherGlobals.LAUNCHER_VERSION);
    labelFlamingoStatus.setBounds(25, 90, 600, 20);
    labelFlamingoStatus.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoStatus);

    labelFlamingoStatus = new JLabel("Flamingo status: Offline");
    labelFlamingoStatus.setBounds(25, 110, 600, 20);
    labelFlamingoStatus.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoStatus);

    labelFlamingoVersion = new JLabel("Flamingo version: N/A");
    labelFlamingoVersion.setBounds(25, 130, 600, 20);
    labelFlamingoVersion.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoVersion);

    labelFlamingoUptime = new JLabel("Flamingo uptime: N/A");
    labelFlamingoUptime.setBounds(25, 150, 600, 20);
    labelFlamingoUptime.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoUptime);

    JButton copyLogsButton = new JButton("Copy logs to clipboard");
    copyLogsButton.setFont(Fonts.fontMed);
    copyLogsButton.setBounds(25, 400, 200, 23);
    copyLogsButton.setFocusPainted(false);
    copyLogsButton.setFocusable(false);
    copyLogsButton.setToolTipText("Copy logs to clipboard");
    aboutPanel.add(copyLogsButton);
    copyLogsButton.addActionListener(l -> {
      SettingsEventHandler.copyLogsEvent(l);
      Dialog.push("Logs copied to clipboard.", JOptionPane.INFORMATION_MESSAGE);
    });

    return aboutPanel;
  }

  private int getMaxAllowedMemoryAlloc() {
    int MAX_ALLOWED_MEMORY_64_BIT = 8196;
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
}
