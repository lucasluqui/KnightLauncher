package com.luuqui.launcher.settings;

import com.luuqui.dialog.DialogInfo;
import com.luuqui.dialog.DialogWarning;
import com.luuqui.launcher.*;
import com.luuqui.util.ColorUtil;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.SteamUtil;
import com.luuqui.util.SystemUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
  public static JEditorPane argumentsPane;
  public static JTextField serverAddressTextField;
  public static JTextField portTextField;
  public static JTextField publicKeyTextField;
  public static JTextField getdownURLTextField;
  public static JTextField betaCodeTextField;
  public static JLabel labelFlamingoStatus;
  public static JLabel labelFlamingoVersion;
  public static JLabel labelFlamingoUptime;

  @SuppressWarnings("static-access")
  public SettingsGUI(LauncherApp app) {
    super();
    this.app = app;
    initialize();
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
    tabbedPane.addTab(Locale.getValue("tab.launcher"), createLauncherPanel());
    tabbedPane.addTab(Locale.getValue("tab.game"), createGamePanel());
    tabbedPane.addTab(Locale.getValue("tab.betas"), createBetasPanel());
    //tabbedPane.addTab(Locale.getValue("tab.spiralview"), createSpiralviewPanel());
    tabbedPane.addTab(Locale.getValue("tab.advanced"), createAdvancedPanel());
    tabbedPane.addTab(Locale.getValue("tab.about"), createAboutPanel());
    tabbedPane.setBackground(new Color(56, 60, 71));
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
    launcherPanel.setBackground(new Color(56, 60, 71));

    JLabel headerLabel = new JLabel(Locale.getValue("tab.launcher"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    launcherPanel.add(headerLabel);

    JLabel labelLanguage = new JLabel(Locale.getValue("m.language"));
    labelLanguage.setBounds(25, 90, 175, 18);
    labelLanguage.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelLanguage);

    choiceLanguage = new JComboBox<String>();
    choiceLanguage.setBounds(25, 115, 150, 20);
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
    labelCleaning.setBounds(25, 175, 350, 18);
    labelCleaning.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelCleaning);

    JLabel labelCleaningExplained = new JLabel(Locale.getValue("m.file_cleaning_explained"));
    labelCleaningExplained.setBounds(25, 195, 600, 16);
    labelCleaningExplained.setFont(Fonts.fontReg);
    launcherPanel.add(labelCleaningExplained);

    switchCleaning = new JCheckBox("");
    switchCleaning.setBounds(590, 180, 30, 23);
    switchCleaning.setFocusPainted(false);
    launcherPanel.add(switchCleaning);
    switchCleaning.setSelected(Settings.doRebuilds);
    switchCleaning.addActionListener(action -> SettingsEventHandler.rebuildsChangeEvent(action));

    JSeparator sep = new JSeparator();
    sep.setBounds(25, 225, 600, 16);
    launcherPanel.add(sep);

    JLabel labelKeepOpen = new JLabel(Locale.getValue("m.keep_open"));
    labelKeepOpen.setBounds(25, 240, 350, 18);
    labelKeepOpen.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelKeepOpen);

    JLabel labelKeepOpenExplained = new JLabel(Locale.getValue("m.keep_open_explained"));
    labelKeepOpenExplained.setBounds(25, 260, 600, 16);
    labelKeepOpenExplained.setFont(Fonts.fontReg);
    launcherPanel.add(labelKeepOpenExplained);

    switchKeepOpen = new JCheckBox("");
    switchKeepOpen.setBounds(590, 245, 30, 23);
    switchKeepOpen.setFocusPainted(false);
    launcherPanel.add(switchKeepOpen);
    switchKeepOpen.setSelected(Settings.keepOpen);
    switchKeepOpen.addActionListener(action -> SettingsEventHandler.keepOpenChangeEvent(action));

    JSeparator sep2 = new JSeparator();
    sep2.setBounds(25, 290, 600, 16);
    launcherPanel.add(sep2);

    JLabel labelShortcut = new JLabel(Locale.getValue("m.create_shortcut"));
    labelShortcut.setBounds(25, 305, 225, 18);
    labelShortcut.setFont(Fonts.fontRegBig);
    launcherPanel.add(labelShortcut);

    JLabel labelShortcutExplained = new JLabel(Locale.getValue("m.create_shortcut_explained"));
    labelShortcutExplained.setBounds(25, 325, 600, 16);
    labelShortcutExplained.setFont(Fonts.fontReg);
    launcherPanel.add(labelShortcutExplained);

    switchShortcut = new JCheckBox("");
    switchShortcut.setBounds(590, 310, 30, 23);
    switchShortcut.setFocusPainted(false);
    launcherPanel.add(switchShortcut);
    switchShortcut.setSelected(Settings.createShortcut);
    switchShortcut.addActionListener(action -> SettingsEventHandler.createShortcutChangeEvent(action));

    if(SystemUtil.isWindows() && SystemUtil.is64Bit()) {
      JSeparator sepDiscord = new JSeparator();
      sepDiscord.setBounds(25, 355, 600, 16);
      launcherPanel.add(sepDiscord);

      JLabel labelUseIngameRPC = new JLabel(Locale.getValue("m.use_ingame_rpc"));
      labelUseIngameRPC.setBounds(25, 370, 350, 18);
      labelUseIngameRPC.setFont(Fonts.fontRegBig);
      launcherPanel.add(labelUseIngameRPC);

      JLabel labelUseIngameRPCExplained = new JLabel(Locale.getValue("m.use_ingame_rpc_explained"));
      labelUseIngameRPCExplained.setBounds(25, 390, 600, 16);
      labelUseIngameRPCExplained.setFont(Fonts.fontReg);
      launcherPanel.add(labelUseIngameRPCExplained);

      switchUseIngameRPC = new JCheckBox("");
      switchUseIngameRPC.setBounds(590, 375, 30, 23);
      switchUseIngameRPC.setFocusPainted(false);
      launcherPanel.add(switchUseIngameRPC);
      switchUseIngameRPC.setSelected(Settings.useIngameRPC);
      switchUseIngameRPC.addActionListener(action -> SettingsEventHandler.ingameRPCChangeEvent(action));
    }

    return launcherPanel;
  }

  protected JPanel createGamePanel() {
    JPanel gamePanel = new JPanel();
    gamePanel.setLayout(null);
    gamePanel.setBackground(new Color(56, 60, 71));

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

    JLabel labelJVMData = new JLabel("Installed Java VM: " + JavaUtil.getGameJVMData());
    labelJVMData.setBounds(25, 425, 600, 16);
    labelJVMData.setFont(Fonts.fontMedIta);
    gamePanel.add(labelJVMData);

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
    betasPanel.setBackground(new Color(56, 60, 71));

    JLabel headerLabel = new JLabel(Locale.getValue("tab.betas"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    betasPanel.add(headerLabel);

    JLabel betaCodeLabel = new JLabel("Activate a Beta code");
    betaCodeLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeLabel.setBounds(25, 70, 450, 50);
    betaCodeLabel.setFont(Fonts.fontReg);
    betasPanel.add(betaCodeLabel);

    betaCodeTextField = new JTextField();
    betaCodeTextField.setFont(Fonts.fontCodeReg);
    betaCodeTextField.setBounds(25, 105, 250, 25);
    betasPanel.add(betaCodeTextField);

    JLabel betaCodeResultLabel = new JLabel("");
    betaCodeResultLabel.setHorizontalAlignment(SwingConstants.LEFT);
    betaCodeResultLabel.setBounds(25, 125, 450, 50);
    betaCodeResultLabel.setFont(Fonts.fontReg);
    betaCodeResultLabel.setVisible(false);
    betasPanel.add(betaCodeResultLabel);

    JButton betaCodeButton = new JButton("Activate");
    betaCodeButton.setFont(Fonts.fontMed);
    betaCodeButton.setFocusPainted(false);
    betaCodeButton.setFocusable(false);
    betaCodeButton.setToolTipText("Activate");
    betaCodeButton.setBounds(295, 105, 100, 25);
    betasPanel.add(betaCodeButton);
    betaCodeButton.addActionListener(action -> {
      int result = SettingsEventHandler.activateBetaCode(betaCodeTextField.getText());

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

    return betasPanel;
  }

  protected JPanel createSpiralviewPanel() {
    JPanel spiralviewPanel = new JPanel();
    spiralviewPanel.setLayout(null);
    spiralviewPanel.setBackground(new Color(56, 60, 71));

    JLabel headerLabel = new JLabel(Locale.getValue("tab.spiralview"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
    headerLabel.setFont(Fonts.fontMedGiant);
    spiralviewPanel.add(headerLabel);

    JButton modelEditorButton = new JButton("Start Model Viewer");
    modelEditorButton.setFont(Fonts.fontMed);
    modelEditorButton.setBounds(400, 215, 180, 23);
    modelEditorButton.setFocusPainted(false);
    modelEditorButton.setFocusable(false);
    spiralviewPanel.add(modelEditorButton);
    //modelEditorButton.addActionListener(action -> SettingsEventHandler.startModelViewer(action));

    return spiralviewPanel;
  }

  protected JPanel createAdvancedPanel() {
    JPanel advancedPanel = new JPanel();
    advancedPanel.setLayout(null);
    advancedPanel.setBackground(new Color(56, 60, 71));

    JLabel headerLabel = new JLabel(Locale.getValue("tab.advanced"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    advancedPanel.add(headerLabel);

    JLabel labelArguments = new JLabel(Locale.getValue("m.extratxt_write_arguments") + " (Extra.txt)");
    labelArguments.setBounds(25, 90, 600, 18);
    labelArguments.setFont(Fonts.fontRegBig);
    advancedPanel.add(labelArguments);

    argumentsPane = new JEditorPane();
    argumentsPane.setFont(Fonts.fontCodeReg);
    argumentsPane.setBounds(25, 125, 615, 100);
    advancedPanel.add(argumentsPane);
    argumentsPane.setText(Settings.gameAdditionalArgs);

    JScrollPane scrollBar = new JScrollPane(argumentsPane);
    scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollBar.setBounds(25, 125, 632, 100);
    advancedPanel.add(scrollBar);

    JSeparator sep = new JSeparator();
    sep.setBounds(25, 245, 600, 16);
    advancedPanel.add(sep);

    JLabel labelConnectionSettings = new JLabel("Connection Settings");
    labelConnectionSettings.setBounds(25, 260, 600, 18);
    labelConnectionSettings.setFont(Fonts.fontRegBig);
    advancedPanel.add(labelConnectionSettings);

    JLabel serverAddressLabel = new JLabel("Server Address");
    serverAddressLabel.setHorizontalAlignment(SwingConstants.LEFT);
    serverAddressLabel.setBounds(25, 275, 450, 50);
    serverAddressLabel.setFont(Fonts.fontReg);
    advancedPanel.add(serverAddressLabel);

    serverAddressTextField = new JTextField();
    serverAddressTextField.setFont(Fonts.fontCodeReg);
    serverAddressTextField.setBounds(25, 310, 250, 25);
    serverAddressTextField.addActionListener(e -> {
      SettingsEventHandler.saveConnectionSettings();
    });
    advancedPanel.add(serverAddressTextField);
    serverAddressTextField.setText(Settings.gameEndpoint);

    JLabel portLabel = new JLabel("Port");
    portLabel.setHorizontalAlignment(SwingConstants.LEFT);
    portLabel.setBounds(280, 275, 450, 50);
    portLabel.setFont(Fonts.fontReg);
    advancedPanel.add(portLabel);

    portTextField = new JTextField();
    portTextField.setFont(Fonts.fontCodeReg);
    portTextField.setBounds(280, 310, 55, 25);
    advancedPanel.add(portTextField);
    portTextField.setText(String.valueOf(Settings.gamePort));

    JLabel publicKeyLabel = new JLabel("Public Key");
    publicKeyLabel.setHorizontalAlignment(SwingConstants.LEFT);
    publicKeyLabel.setBounds(25, 330, 450, 50);
    publicKeyLabel.setFont(Fonts.fontReg);
    advancedPanel.add(publicKeyLabel);

    publicKeyTextField = new JTextField();
    publicKeyTextField.setFont(Fonts.fontCodeReg);
    publicKeyTextField.setBounds(25, 365, 355, 30);

    JScrollBar publicKeyScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel publicKeyPanel = new JPanel();
    publicKeyPanel.setLayout(new BoxLayout(publicKeyPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel publicKeyBRM = publicKeyTextField.getHorizontalVisibility();
    publicKeyScrollBar.setModel(publicKeyBRM);
    publicKeyPanel.add(publicKeyTextField);
    publicKeyPanel.add(publicKeyScrollBar);
    publicKeyPanel.setBounds(25, 365, 355, 30);

    advancedPanel.add(publicKeyPanel);
    publicKeyTextField.setText(Settings.gamePublicKey);

    JLabel getdownURLLabel = new JLabel("Getdown URL");
    getdownURLLabel.setHorizontalAlignment(SwingConstants.LEFT);
    getdownURLLabel.setBounds(25, 385, 450, 50);
    getdownURLLabel.setFont(Fonts.fontReg);
    advancedPanel.add(getdownURLLabel);

    getdownURLTextField = new JTextField();
    getdownURLTextField.setFont(Fonts.fontCodeReg);
    getdownURLTextField.setBounds(25, 420, 355, 30);

    JScrollBar getdownURLScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel getdownURLPanel = new JPanel();
    getdownURLPanel.setLayout(new BoxLayout(getdownURLPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel getdownURLBRM = getdownURLTextField.getHorizontalVisibility();
    getdownURLScrollBar.setModel(getdownURLBRM);
    getdownURLPanel.add(getdownURLTextField);
    getdownURLPanel.add(getdownURLScrollBar);
    getdownURLPanel.setBounds(25, 420, 355, 30);

    advancedPanel.add(getdownURLPanel);
    getdownURLTextField.setText(Settings.gameGetdownFullURL);

    JButton resetButton = new JButton("Reset values to default");
    resetButton.setFont(Fonts.fontMed);
    resetButton.setBounds(400, 420, 180, 23);
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
    aboutPanel.setBackground(new Color(56, 60, 71));

    JLabel headerLabel = new JLabel(Locale.getValue("tab.about"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 60);
    headerLabel.setFont(Fonts.fontMedGiant);
    aboutPanel.add(headerLabel);

    labelFlamingoStatus = new JLabel("Flamingo status: Offline");
    labelFlamingoStatus.setBounds(25, 90, 600, 18);
    labelFlamingoStatus.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoStatus);

    labelFlamingoVersion = new JLabel("Flamingo version: N/A");
    labelFlamingoVersion.setBounds(25, 110, 600, 18);
    labelFlamingoVersion.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoVersion);

    labelFlamingoUptime = new JLabel("Flamingo uptime: N/A");
    labelFlamingoUptime.setBounds(25, 130, 600, 18);
    labelFlamingoUptime.setFont(Fonts.fontRegBig);
    aboutPanel.add(labelFlamingoUptime);

    JButton copyLogsButton = new JButton("Copy logs to clipboard");
    copyLogsButton.setFont(Fonts.fontMed);
    copyLogsButton.setBounds(25, 160, 200, 23);
    copyLogsButton.setFocusPainted(false);
    copyLogsButton.setFocusable(false);
    copyLogsButton.setToolTipText("Copy logs to clipboard");
    aboutPanel.add(copyLogsButton);
    copyLogsButton.addActionListener(l -> {
      SettingsEventHandler.copyLogsEvent(l);
      DialogInfo.push("Logs copied to clipboard.");
    });

    /*
    JButton copyLauncherLogButton = new JButton("Copy knightlauncher.log to clipboard");
    copyLauncherLogButton.setFont(Fonts.fontMed);
    copyLauncherLogButton.setBounds(25, 160, 270, 23);
    copyLauncherLogButton.setFocusPainted(false);
    copyLauncherLogButton.setFocusable(false);
    copyLauncherLogButton.setToolTipText("Copy knightlauncher.log to clipboard");
    aboutPanel.add(copyLauncherLogButton);
    copyLauncherLogButton.addActionListener(SettingsEventHandler::copyLauncherLogEvent);

    JButton copyGameLogButton = new JButton("Copy game logs to clipboard");
    copyGameLogButton.setFont(Fonts.fontMed);
    copyGameLogButton.setBounds(25, 190, 270, 23);
    copyGameLogButton.setFocusPainted(false);
    copyGameLogButton.setFocusable(false);
    copyGameLogButton.setToolTipText("Copy game logs to clipboard");
    aboutPanel.add(copyGameLogButton);
    copyGameLogButton.addActionListener(SettingsEventHandler::copyGameLogEvent);
    */

    return aboutPanel;
  }

  private int getMaxAllowedMemoryAlloc() {
    int MAX_ALLOWED_MEMORY_64_BIT = 8196;
    int MAX_ALLOWED_MEMORY_32_BIT = 1024;

    if (JavaUtil.getJVMArch(LauncherGlobals.USER_DIR + "\\java_vm\\bin\\java.exe") == 64) {
      return MAX_ALLOWED_MEMORY_64_BIT;
    } else {
      return MAX_ALLOWED_MEMORY_32_BIT;
    }
  }
}
