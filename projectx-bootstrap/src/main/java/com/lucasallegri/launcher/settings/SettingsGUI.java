package com.lucasallegri.launcher.settings;

import com.lucasallegri.launcher.*;
import com.lucasallegri.launcher.mods.ModListEventHandler;
import com.lucasallegri.util.ColorUtil;
import com.lucasallegri.util.JavaUtil;
import com.lucasallegri.util.SteamUtil;
import com.lucasallegri.util.SystemUtil;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mdlaf.utils.MaterialBorders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingsGUI extends BaseGUI {

  private final LauncherApp app;
  public static JFrame settingsGUIFrame;
  public static JComboBox<String> choicePlatform;
  public static JComboBox<String> choiceLanguage;
  public static JComboBox<String> choiceStyle;
  public static JComboBox<String> choiceMemory;
  public static JComboBox<String> choiceGC;
  public static JToggleButton switchCleaning;
  public static JToggleButton switchKeepOpen;
  public static JToggleButton switchShortcut;
  public static JButton forceRebuildButton;
  public static JToggleButton switchStringDedup;
  public static JToggleButton switchExplicitGC;
  public static JToggleButton switchUseCustomGC;
  public static JToggleButton switchUseIngameRPC;
  public static JEditorPane argumentsPane;
  public static JTextField serverAddressTextField;
  public static JTextField portTextField;
  public static JTextField publicKeyTextField;
  public static JTextField getdownURLTextField;
  public static JCheckBox understoodCheckBox;

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

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.setBounds(-2, 20, 852, 455);
    tabbedPane.setFont(Fonts.fontMedBig);
    tabbedPane.addTab(Locale.getValue("tab.appearance"), createAppearancePanel());
    tabbedPane.addTab(Locale.getValue("tab.behavior"), createBehaviorPanel());
    tabbedPane.addTab(Locale.getValue("tab.game"), createGamePanel());
    tabbedPane.addTab(Locale.getValue("tab.files"), createFilesPanel());
    tabbedPane.addTab(Locale.getValue("tab.connection"), createConnectionPanel());
    if (SystemUtil.isWindows() && SystemUtil.is64Bit())
      tabbedPane.addTab(Locale.getValue("Discord"), createDiscordPanel());
    tabbedPane.addTab(Locale.getValue("tab.extratxt"), createExtraPanel());
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
    closeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        settingsGUIFrame.setVisible(false);
        SettingsEventHandler.saveAdditionalArgs();
        SettingsEventHandler.saveConnectionSettings();
      }
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.CHEVRON_DOWN, 14, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(settingsGUIFrame.getWidth() - 38, 1, 20, 21);
    minimizeButton.setToolTipText(Locale.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBorder(MaterialBorders.roundedLineColorBorder(ColorUtil.getTitleBarColor(), 0));
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);

    settingsGUIFrame.setLocationRelativeTo(null);
  }

  protected JPanel createAppearancePanel() {
    JPanel appearancePanel = new JPanel();
    appearancePanel.setLayout(null);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.appearance"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
    headerLabel.setFont(Fonts.fontMedGiant);
    appearancePanel.add(headerLabel);

    JLabel labelStyle = new JLabel(Locale.getValue("m.launcher_style"));
    labelStyle.setBounds(25, 90, 175, 18);
    labelStyle.setFont(Fonts.fontRegBig);
    appearancePanel.add(labelStyle);

    choiceStyle = new JComboBox<String>();
    choiceStyle.setBounds(25, 115, 150, 20);
    choiceStyle.setFocusable(false);
    choiceStyle.setFont(Fonts.fontReg);
    choiceStyle.addItem(Locale.getValue("o.dark"));
    choiceStyle.addItem(Locale.getValue("o.light"));
    appearancePanel.add(choiceStyle);
    choiceStyle.setSelectedIndex(Settings.launcherStyle.equals("dark") ? 0 : 1);
    choiceStyle.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        SettingsEventHandler.styleChangeEvent(event);
      }
    });

    JLabel labelLanguage = new JLabel(Locale.getValue("m.language"));
    labelLanguage.setBounds(225, 90, 175, 18);
    labelLanguage.setFont(Fonts.fontRegBig);
    appearancePanel.add(labelLanguage);

    choiceLanguage = new JComboBox<String>();
    choiceLanguage.setBounds(225, 115, 150, 20);
    choiceLanguage.setFocusable(false);
    choiceLanguage.setFont(Fonts.fontReg);
    for (String lang : Locale.AVAILABLE_LANGUAGES) {
      choiceLanguage.addItem(lang);
    }
    appearancePanel.add(choiceLanguage);
    choiceLanguage.setSelectedItem(Locale.getLangName(Settings.lang));
    choiceLanguage.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        SettingsEventHandler.languageChangeEvent(event);
      }
    });

    return appearancePanel;
  }

  protected JPanel createBehaviorPanel() {
    JPanel behaviorPanel = new JPanel();
    behaviorPanel.setLayout(null);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.behavior"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
    headerLabel.setFont(Fonts.fontMedGiant);
    behaviorPanel.add(headerLabel);

    JLabel labelCleaning = new JLabel(Locale.getValue("m.rebuilds"));
    labelCleaning.setBounds(25, 90, 350, 18);
    labelCleaning.setFont(Fonts.fontRegBig);
    behaviorPanel.add(labelCleaning);

    JLabel labelCleaningExplained = new JLabel(Locale.getValue("m.file_cleaning_explained"));
    labelCleaningExplained.setBounds(25, 110, 600, 16);
    labelCleaningExplained.setFont(Fonts.fontReg);
    behaviorPanel.add(labelCleaningExplained);

    switchCleaning = new JToggleButton("");
    switchCleaning.setBounds(790, 95, 30, 23);
    switchCleaning.setFocusPainted(false);
    behaviorPanel.add(switchCleaning);
    switchCleaning.setSelected(Settings.doRebuilds);
    switchCleaning.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsEventHandler.rebuildsChangeEvent(_action);
      }
    });

    JSeparator sep = new JSeparator();
    sep.setBounds(25, 140, 800, 16);
    behaviorPanel.add(sep);

    JLabel labelKeepOpen = new JLabel(Locale.getValue("m.keep_open"));
    labelKeepOpen.setBounds(25, 155, 350, 18);
    labelKeepOpen.setFont(Fonts.fontRegBig);
    behaviorPanel.add(labelKeepOpen);

    JLabel labelKeepOpenExplained = new JLabel(Locale.getValue("m.keep_open_explained"));
    labelKeepOpenExplained.setBounds(25, 175, 600, 16);
    labelKeepOpenExplained.setFont(Fonts.fontReg);
    behaviorPanel.add(labelKeepOpenExplained);

    switchKeepOpen = new JToggleButton("");
    switchKeepOpen.setBounds(790, 160, 30, 23);
    switchKeepOpen.setFocusPainted(false);
    behaviorPanel.add(switchKeepOpen);
    switchKeepOpen.setSelected(Settings.keepOpen);
    switchKeepOpen.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsEventHandler.keepOpenChangeEvent(_action);
      }
    });

    JSeparator sep2 = new JSeparator();
    sep2.setBounds(25, 205, 800, 16);
    behaviorPanel.add(sep2);

    JLabel labelShortcut = new JLabel(Locale.getValue("m.create_shortcut"));
    labelShortcut.setBounds(25, 220, 225, 18);
    labelShortcut.setFont(Fonts.fontRegBig);
    behaviorPanel.add(labelShortcut);

    JLabel labelShortcutExplained = new JLabel(Locale.getValue("m.create_shortcut_explained"));
    labelShortcutExplained.setBounds(25, 240, 600, 16);
    labelShortcutExplained.setFont(Fonts.fontReg);
    behaviorPanel.add(labelShortcutExplained);

    switchShortcut = new JToggleButton("");
    switchShortcut.setBounds(790, 225, 30, 23);
    switchShortcut.setFocusPainted(false);
    behaviorPanel.add(switchShortcut);
    switchShortcut.setSelected(Settings.createShortcut);
    switchShortcut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsEventHandler.createShortcutChangeEvent(_action);
      }
    });

    return behaviorPanel;
  }

  protected JPanel createGamePanel() {
    JPanel gamePanel = new JPanel();
    gamePanel.setLayout(null);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.game"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
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

    choiceMemory = new JComboBox<String>();
    choiceMemory.setBounds(225, 115, 150, 20);
    choiceMemory.setFocusable(false);
    choiceMemory.setFont(Fonts.fontReg);
    gamePanel.add(choiceMemory);
    choiceMemory.addItem(Locale.getValue("o.memory_256"));
    choiceMemory.addItem(Locale.getValue("o.memory_512"));
    choiceMemory.addItem(Locale.getValue("o.memory_768"));
    choiceMemory.addItem(Locale.getValue("o.memory_1024"));
    choiceMemory.addItem(Locale.getValue("o.memory_1536"));
    choiceMemory.addItem(Locale.getValue("o.memory_2048"));
    choiceMemory.addItem(Locale.getValue("o.memory_2560"));
    choiceMemory.addItem(Locale.getValue("o.memory_3072"));
    if (SystemUtil.is64Bit()) {
      choiceMemory.addItem(Locale.getValue("o.memory_4096"));
      choiceMemory.addItem(Locale.getValue("o.memory_5120"));
      choiceMemory.addItem(Locale.getValue("o.memory_6144"));
      choiceMemory.addItem(Locale.getValue("o.memory_8192"));
      choiceMemory.addItem(Locale.getValue("o.memory_16384"));
    }
    choiceMemory.setSelectedIndex(parseSelectedMemoryAsIndex());
    choiceMemory.setToolTipText((String) choiceMemory.getSelectedItem());
    choiceMemory.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        SettingsEventHandler.memoryChangeEvent(event);
      }
    });

    //JLabel labelStringDedup = new JLabel(Locale.getValue("m.use_string_deduplication"));
    //labelStringDedup.setBounds(25, 175, 375, 18);
    //labelStringDedup.setFont(Fonts.fontRegBig);
    //gamePanel.add(labelStringDedup);

    //JLabel labelStringDedupExplained = new JLabel(Locale.getValue("m.string_deduplication_explained"));
    //labelStringDedupExplained.setBounds(25, 195, 600, 16);
    //labelStringDedupExplained.setFont(Fonts.fontReg);
    //gamePanel.add(labelStringDedupExplained);

    //switchStringDedup = new JToggleButton("");
    //switchStringDedup.setBounds(790, 180, 30, 23);
    //switchStringDedup.setFocusPainted(false);
    //gamePanel.add(switchStringDedup);
    //switchStringDedup.setSelected(Settings.gameUseStringDeduplication);
    //switchStringDedup.addActionListener(new ActionListener() {
    //  public void actionPerformed(ActionEvent _action) {
    //    SettingsEventHandler.useStringDeduplicationChangeEvent(_action);
    //  }
    //});

    JLabel labelUseCustomGC = new JLabel("Use a different GC behavior");
    labelUseCustomGC.setBounds(25, 175, 375, 18);
    labelUseCustomGC.setFont(Fonts.fontRegBig);
    gamePanel.add(labelUseCustomGC);

    JLabel labelUseCustomGCExplained = new JLabel("Change how Garbage Collection will be done on the game's Java VM");
    labelUseCustomGCExplained.setBounds(25, 195, 600, 16);
    labelUseCustomGCExplained.setFont(Fonts.fontReg);
    gamePanel.add(labelUseCustomGCExplained);

    switchUseCustomGC = new JToggleButton("");
    switchUseCustomGC.setBounds(790, 180, 30, 23);
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
    choiceGC.setBounds(670, 180, 100, 20);
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
    sep2.setBounds(25, 225, 800, 16);
    gamePanel.add(sep2);

    JLabel labelExplicitGC = new JLabel(Locale.getValue("m.disable_explicit_gc"));
    labelExplicitGC.setBounds(25, 240, 275, 18);
    labelExplicitGC.setFont(Fonts.fontRegBig);
    gamePanel.add(labelExplicitGC);

    JLabel labelExplicitGCExplained = new JLabel(Locale.getValue("m.explicit_gc_explained"));
    labelExplicitGCExplained.setBounds(25, 260, 600, 16);
    labelExplicitGCExplained.setFont(Fonts.fontReg);
    gamePanel.add(labelExplicitGCExplained);

    switchExplicitGC = new JToggleButton("");
    switchExplicitGC.setBounds(790, 245, 30, 23);
    switchExplicitGC.setFocusPainted(false);
    gamePanel.add(switchExplicitGC);
    switchExplicitGC.setSelected(Settings.gameDisableExplicitGC);
    switchExplicitGC.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsEventHandler.disableExplicitGCChangeEvent(_action);
      }
    });
    switchExplicitGC.setEnabled(SystemUtil.is64Bit());

    return gamePanel;
  }

  protected JPanel createFilesPanel() {
    JPanel filesPanel = new JPanel();
    filesPanel.setLayout(null);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.files"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
    headerLabel.setFont(Fonts.fontMedGiant);
    filesPanel.add(headerLabel);

    JLabel labelFileClean = new JLabel(Locale.getValue("b.force_rebuild"));
    labelFileClean.setBounds(25, 90, 275, 18);
    labelFileClean.setFont(Fonts.fontRegBig);
    filesPanel.add(labelFileClean);

    JLabel labelFileCleanExplained = new JLabel(Locale.getValue("m.clean_files_explained"));
    labelFileCleanExplained.setBounds(25, 110, 600, 16);
    labelFileCleanExplained.setFont(Fonts.fontReg);
    filesPanel.add(labelFileCleanExplained);

    Icon startIcon = IconFontSwing.buildIcon(FontAwesome.SHARE, 16, ColorUtil.getForegroundColor());
    JButton forceRebuildButton = new JButton(startIcon);
    forceRebuildButton.setBounds(790, 95, 30, 23);
    forceRebuildButton.setFocusPainted(false);
    forceRebuildButton.setFocusable(false);
    filesPanel.add(forceRebuildButton);
    forceRebuildButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsGUI.settingsGUIFrame.setVisible(false);
        LauncherGUI.launcherGUIFrame.setVisible(true);
        SettingsEventHandler.forceRebuildEvent();
      }
    });

    if (SystemUtil.isWindows() && SystemUtil.is64Bit()) {
      JSeparator sep = new JSeparator();
      sep.setBounds(25, 140, 800, 16);
      filesPanel.add(sep);

      JLabel labelJVMPatch = new JLabel(Locale.getValue("m.force_jvm_patch"));
      labelJVMPatch.setBounds(25, 155, 350, 18);
      labelJVMPatch.setFont(Fonts.fontRegBig);
      filesPanel.add(labelJVMPatch);

      JLabel labelJVMPatchExplained = new JLabel(Locale.getValue("m.force_jvm_patch_explained"));
      labelJVMPatchExplained.setBounds(25, 175, 600, 16);
      labelJVMPatchExplained.setFont(Fonts.fontReg);
      filesPanel.add(labelJVMPatchExplained);

      JLabel labelJVMData = new JLabel("Installed Java VM: " + JavaUtil.getGameJVMData());
      labelJVMData.setBounds(25, 210, 600, 16);
      labelJVMData.setFont(Fonts.fontMedIta);
      filesPanel.add(labelJVMData);

      JButton jvmPatchButton = new JButton(startIcon);
      jvmPatchButton.setBounds(790, 160, 30, 23);
      jvmPatchButton.setFocusPainted(false);
      jvmPatchButton.setFocusable(false);
      filesPanel.add(jvmPatchButton);
      jvmPatchButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent _action) {
          SettingsEventHandler.jvmPatchEvent(_action);
        }
      });
    }

    return filesPanel;
  }

  protected JPanel createExtraPanel() {
    JPanel extraPanel = new JPanel();
    extraPanel.setLayout(null);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.extratxt"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
    headerLabel.setFont(Fonts.fontMedGiant);
    extraPanel.add(headerLabel);

    JLabel labelArguments = new JLabel(Locale.getValue("m.extratxt_write_arguments"));
    labelArguments.setBounds(25, 90, 600, 18);
    labelArguments.setFont(Fonts.fontRegBig);
    extraPanel.add(labelArguments);

    argumentsPane = new JEditorPane();
    argumentsPane.setFont(Fonts.fontMed);
    argumentsPane.setBounds(25, 125, 323, 175);
    extraPanel.add(argumentsPane);
    argumentsPane.setText(Settings.gameAdditionalArgs);

    JScrollPane scrollBar = new JScrollPane(argumentsPane);
    scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollBar.setBounds(25, 125, 340, 175);
    extraPanel.add(scrollBar);

    return extraPanel;
  }

  protected JPanel createDiscordPanel() {
    JPanel ingameRPCPanel = new JPanel();
    ingameRPCPanel.setLayout(null);

    JLabel headerLabel = new JLabel(Locale.getValue("Discord"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
    headerLabel.setFont(Fonts.fontMedGiant);
    ingameRPCPanel.add(headerLabel);

    JLabel labelUseIngameRPC = new JLabel(Locale.getValue("m.use_ingame_rpc"));
    labelUseIngameRPC.setBounds(25, 90, 350, 18);
    labelUseIngameRPC.setFont(Fonts.fontRegBig);
    ingameRPCPanel.add(labelUseIngameRPC);

    JLabel labelUseIngameRPCExplained = new JLabel(Locale.getValue("m.use_ingame_rpc_explained"));
    labelUseIngameRPCExplained.setBounds(25, 110, 600, 16);
    labelUseIngameRPCExplained.setFont(Fonts.fontReg);
    ingameRPCPanel.add(labelUseIngameRPCExplained);

    switchUseIngameRPC = new JToggleButton("");
    switchUseIngameRPC.setBounds(790, 95, 30, 23);
    switchUseIngameRPC.setFocusPainted(false);
    ingameRPCPanel.add(switchUseIngameRPC);
    switchUseIngameRPC.setSelected(Settings.useIngameRPC);
    switchUseIngameRPC.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        SettingsEventHandler.ingameRPCChangeEvent(_action);
      }
    });

    return ingameRPCPanel;
  }

  protected JPanel createConnectionPanel() {
    JPanel connectionPanel = new JPanel();
    connectionPanel.setLayout(null);

    JLabel headerLabel = new JLabel(Locale.getValue("tab.connection"));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headerLabel.setBounds(25, 11, 450, 50);
    headerLabel.setFont(Fonts.fontMedGiant);
    connectionPanel.add(headerLabel);

    JLabel serverAddressLabel = new JLabel("Server Address");
    serverAddressLabel.setHorizontalAlignment(SwingConstants.LEFT);
    serverAddressLabel.setBounds(25, 70, 450, 50);
    serverAddressLabel.setFont(Fonts.fontReg);
    connectionPanel.add(serverAddressLabel);

    serverAddressTextField = new JTextField();
    serverAddressTextField.setFont(Fonts.fontMed);
    serverAddressTextField.setBounds(25, 105, 250, 25);
    connectionPanel.add(serverAddressTextField);
    serverAddressTextField.setText(Settings.gameEndpoint);

    JLabel portLabel = new JLabel("Port");
    portLabel.setHorizontalAlignment(SwingConstants.LEFT);
    portLabel.setBounds(280, 70, 450, 50);
    portLabel.setFont(Fonts.fontReg);
    connectionPanel.add(portLabel);

    portTextField = new JTextField();
    portTextField.setFont(Fonts.fontMed);
    portTextField.setBounds(280, 105, 55, 25);
    connectionPanel.add(portTextField);
    portTextField.setText(String.valueOf(Settings.gamePort));

    JLabel publicKeyLabel = new JLabel("Public Key");
    publicKeyLabel.setHorizontalAlignment(SwingConstants.LEFT);
    publicKeyLabel.setBounds(25, 125, 450, 50);
    publicKeyLabel.setFont(Fonts.fontReg);
    connectionPanel.add(publicKeyLabel);

    publicKeyTextField = new JTextField();
    publicKeyTextField.setFont(Fonts.fontMed);
    publicKeyTextField.setBounds(25, 160, 355, 25);

    JScrollBar publicKeyScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel publicKeyPanel = new JPanel();
    publicKeyPanel.setLayout(new BoxLayout(publicKeyPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel publicKeyBRM = publicKeyTextField.getHorizontalVisibility();
    publicKeyScrollBar.setModel(publicKeyBRM);
    publicKeyPanel.add(publicKeyTextField);
    publicKeyPanel.add(publicKeyScrollBar);
    publicKeyPanel.setBounds(25, 160, 355, 25);

    connectionPanel.add(publicKeyPanel);
    publicKeyTextField.setText(Settings.gamePublicKey);

    JLabel getdownURLLabel = new JLabel("Getdown URL");
    getdownURLLabel.setHorizontalAlignment(SwingConstants.LEFT);
    getdownURLLabel.setBounds(25, 180, 450, 50);
    getdownURLLabel.setFont(Fonts.fontReg);
    connectionPanel.add(getdownURLLabel);

    getdownURLTextField = new JTextField();
    getdownURLTextField.setFont(Fonts.fontMed);
    getdownURLTextField.setBounds(25, 215, 355, 25);

    JScrollBar getdownURLScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    JPanel getdownURLPanel = new JPanel();
    getdownURLPanel.setLayout(new BoxLayout(getdownURLPanel, BoxLayout.Y_AXIS));
    BoundedRangeModel getdownURLBRM = getdownURLTextField.getHorizontalVisibility();
    getdownURLScrollBar.setModel(getdownURLBRM);
    getdownURLPanel.add(getdownURLTextField);
    getdownURLPanel.add(getdownURLScrollBar);
    getdownURLPanel.setBounds(25, 215, 355, 25);

    connectionPanel.add(getdownURLPanel);
    getdownURLTextField.setText(Settings.gameGetdownFullURL);

    JButton resetButton = new JButton("Reset values to default");
    resetButton.setFont(Fonts.fontMed);
    resetButton.setBounds(400, 215, 180, 23);
    resetButton.setFocusPainted(false);
    resetButton.setFocusable(false);
    resetButton.setToolTipText("Reset values to default");
    connectionPanel.add(resetButton);
    resetButton.addActionListener(action -> SettingsEventHandler.resetButtonEvent(action));

    JLabel noticeLabel = new JLabel("<html>* Note: The modifiable values presented above are merely future proofing for when, inevitably, the official game servers shut down." +
        "<br>This, provided the values given are valid, will allow you to route your client's traffic through another server and continue playing.<br>" +
        "Private servers are against Spiral Knights' Terms of Service and Knight Launcher will not grant any type of support while official servers<br>" +
        "remain active.</html>");
    noticeLabel.setHorizontalAlignment(SwingConstants.LEFT);
    noticeLabel.setBounds(25, 255, 800, 100);
    noticeLabel.setFont(Fonts.fontReg);
    connectionPanel.add(noticeLabel);

    understoodCheckBox = new JCheckBox("I understand");
    understoodCheckBox.setBounds(25, 350, 125, 25);
    understoodCheckBox.setFont(Fonts.fontMed);
    understoodCheckBox.addActionListener(action -> SettingsEventHandler.understoodCheckBoxChangeEvent(action));
    connectionPanel.add(understoodCheckBox);

    if(Settings.connectionOverwriteAgreed) {
      serverAddressTextField.setEnabled(true);
      portTextField.setEnabled(true);
      publicKeyTextField.setEnabled(true);
      getdownURLTextField.setEnabled(true);
      understoodCheckBox.setEnabled(false);
      understoodCheckBox.setSelected(true);
      understoodCheckBox.setVisible(false);
    } else {
      serverAddressTextField.setEnabled(false);
      portTextField.setEnabled(false);
      publicKeyTextField.setEnabled(false);
      getdownURLTextField.setEnabled(false);
    }

    return connectionPanel;
  }

  protected static int parseSelectedMemoryAsInt() {
    switch (choiceMemory.getSelectedIndex()) {
      case 0:
        return 256;
      case 1:
        return 512;
      case 2:
        return 768;
      case 3:
        return 1024;
      case 4:
        return 1536;
      case 5:
        return 2048;
      case 6:
        return 2560;
      case 7:
        return 3072;
      case 8:
        return 4096;
      case 9:
        return 5120;
      case 10:
        return 6144;
      case 11:
        return 8192;
      case 12:
        return 16384;
    }
    return 512;
  }

  protected static int parseSelectedMemoryAsIndex() {
    switch (Settings.gameMemory) {
      case 256:
        return 0;
      case 512:
        return 1;
      case 768:
        return 2;
      case 1024:
        return 3;
      case 1536:
        return 4;
      case 2048:
        return 5;
      case 2560:
        return 6;
      case 3072:
        return 7;
      case 4096:
        return 8;
      case 5120:
        return 9;
      case 6144:
        return 10;
      case 8192:
        return 11;
      case 16384:
        return 12;
    }
    return 0;
  }

}
