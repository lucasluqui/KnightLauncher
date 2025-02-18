package com.luuqui.launcher;

import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordRPC;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsProperties;
import com.luuqui.util.*;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.luuqui.launcher.Log.log;

public class JVMPatcher extends BaseGUI {

  public JFrame jvmPatcherFrame;

  private final LauncherApp _app;
  private final String _path;
  private final boolean _legacy;
  private int _downloadAttempts = 0;
  private Map<String, String> _availableJVM = new HashMap<String, String>();

  public JVMPatcher(LauncherApp app, String path, boolean legacy) {
    super();
    this._app = app;
    this._path = path;
    this._legacy = legacy;
    setAvailableJVM();
    initialize();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.jvmPatcherFrame.setVisible(!this.jvmPatcherFrame.isVisible());
  }

  private void setAvailableJVM() {
    if(_legacy) {
      _availableJVM.put("Java 8 (8u202)", "8u202");
      _availableJVM.put("Java 8 (8u251)", "8u251");
    } else {
      _availableJVM.put("Java 11 (OpenJDK 11.0.26)", "ojdk-11.0.26");
    }
  }

  private void initialize() {
    jvmPatcherFrame = new JFrame();
    jvmPatcherFrame.setVisible(false);
    jvmPatcherFrame.setTitle(Locale.getValue("t.jvm_patcher"));
    jvmPatcherFrame.setBounds(100, 100, 500, 250);
    jvmPatcherFrame.setResizable(false);
    jvmPatcherFrame.setUndecorated(true);
    jvmPatcherFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    jvmPatcherFrame.setIconImage(ImageUtil.loadImageWithinJar("/img/icon-128.png"));
    jvmPatcherFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    jvmPatcherFrame.getContentPane().setLayout(null);

    headerLabel = new JLabel("Patch your game to use a compatible 64-bit Java VM");
    headerLabel.setBounds(0, 50, 500, 37);
    headerLabel.setFont(Fonts.fontMed);
    headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jvmPatcherFrame.getContentPane().add(headerLabel);

    subHeaderLabel = new JLabel("You can always restart this patcher from the Settings menu");
    subHeaderLabel.setBounds(0, 75, 500, 37);
    subHeaderLabel.setFont(Fonts.fontReg);
    subHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jvmPatcherFrame.getContentPane().add(subHeaderLabel);

    JLabel jvmSelectLabel = new JLabel("Select a Java version to install");
    jvmSelectLabel.setBounds(0, 115, 500, 37);
    jvmSelectLabel.setFont(Fonts.fontMed);
    jvmSelectLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jvmPatcherFrame.getContentPane().add(jvmSelectLabel);

    jvmComboBox = new JComboBox<>();
    jvmComboBox.setBounds(125, 145, 255, 20);
    jvmComboBox.setFocusable(false);
    jvmComboBox.setFont(Fonts.fontReg);
    jvmPatcherFrame.add(jvmComboBox);

    for(String key : _availableJVM.keySet()) {
      jvmComboBox.addItem(key);
    }
    jvmComboBox.setSelectedIndex(0);

    jvmPatcherState = new JLabel("");
    jvmPatcherState.setBounds(26, 180, 450, 15);
    jvmPatcherState.setFont(Fonts.fontReg);
    jvmPatcherFrame.getContentPane().add(jvmPatcherState);

    jvmPatcherProgressBar = new JProgressBar();
    jvmPatcherProgressBar.setBounds(25, 204, 450, 25);
    jvmPatcherProgressBar.setVisible(false);
    jvmPatcherFrame.getContentPane().add(jvmPatcherProgressBar);

    buttonAccept = new JButton("Start patching");
    buttonAccept.setFocusPainted(false);
    buttonAccept.setFocusable(false);
    buttonAccept.setFont(Fonts.fontMed);
    buttonAccept.setBounds(150, 200, 200, 25);
    jvmPatcherFrame.getContentPane().add(buttonAccept);
    buttonAccept.addActionListener(l -> {
      buttonAccept.setEnabled(false);
      buttonAccept.setVisible(false);
      jvmSelectLabel.setVisible(false);
      jvmComboBox.setVisible(false);
      headerLabel.setText("This will take a few minutes...");
      subHeaderLabel.setText("Please do not close this window.");
      jvmPatcherProgressBar.setVisible(true);
      this.initPatcher();
    });

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, jvmPatcherFrame.getWidth(), 35);
    titleBar.setBackground(ColorUtil.getTitleBarColor());
    jvmPatcherFrame.getContentPane().add(titleBar);


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

        jvmPatcherFrame.setLocation(jvmPatcherFrame.getLocation().x + me.getX() - pX,
                jvmPatcherFrame.getLocation().y + me.getY() - pY);
      }
    });
    titleBar.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent me) {

        jvmPatcherFrame.setLocation(jvmPatcherFrame.getLocation().x + me.getX() - pX,
                jvmPatcherFrame.getLocation().y + me.getY() - pY);
      }

      @Override
      public void mouseMoved(MouseEvent arg0) {
        // Auto-generated method stub
      }
    });
    titleBar.setLayout(null);

    final int BUTTON_WIDTH = 35;
    final int BUTTON_HEIGHT = 35;

    Icon closeIcon = IconFontSwing.buildIcon(FontAwesome.TIMES, 17, ColorUtil.getForegroundColor());
    closeButton = new JButton(closeIcon);
    closeButton.setBounds(jvmPatcherFrame.getWidth() - BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    closeButton.setToolTipText(Locale.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBackground(null);
    closeButton.setBorder(null);
    closeButton.setVisible(true);
    closeButton.setFont(Fonts.fontMed);
    titleBar.add(closeButton);
    closeButton.addActionListener(e -> {
      finish();
    });
    closeButton.addMouseListener(new MouseListener() {
      @Override public void mouseClicked(MouseEvent e) {}
      @Override public void mousePressed(MouseEvent e) {}
      @Override public void mouseReleased(MouseEvent e) {}
      @Override public void mouseEntered(MouseEvent e) {
        closeButton.setBackground(CustomColors.MID_RED);
      }
      @Override public void mouseExited(MouseEvent e) {
        closeButton.setBackground(null);
      }
    });

    Icon minimizeIcon = IconFontSwing.buildIcon(FontAwesome.WINDOW_MINIMIZE, 12, ColorUtil.getForegroundColor());
    JButton minimizeButton = new JButton(minimizeIcon);
    minimizeButton.setBounds(jvmPatcherFrame.getWidth() - BUTTON_WIDTH * 2, -7, BUTTON_WIDTH, BUTTON_HEIGHT + 7);
    minimizeButton.setToolTipText(Locale.getValue("b.minimize"));
    minimizeButton.setFocusPainted(false);
    minimizeButton.setFocusable(false);
    minimizeButton.setBackground(null);
    minimizeButton.setBorder(null);
    minimizeButton.setVisible(false);
    minimizeButton.setFont(Fonts.fontMed);
    titleBar.add(minimizeButton);
    minimizeButton.addActionListener(e -> jvmPatcherFrame.setState(Frame.ICONIFIED));

    jvmPatcherFrame.setLocationRelativeTo(null);
    jvmPatcherFrame.setVisible(true);

  }

  private void initPatcher() {
    Thread patchThread = new Thread(this::patch);
    patchThread.start();
  }

  private void patch() {
    // Don't allow closing the window once patching starts.
    closeButton.setEnabled(false);

    jvmPatcherProgressBar.setMaximum(4);
    jvmPatcherProgressBar.setValue(1);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_download"));
    
    this.downloadPackagedJVM();
    if(_downloadAttempts > 3) {
      String downloadErrMsg = "The Java VM download couldn't be initiated after 3 attempts." +
              "\nKnight Launcher will boot without patching but be aware game performance might not be the best." +
              "\nYou can manually restart this patcher heading to the 'Game' tab within launcher's Settings.";
      Dialog.push(downloadErrMsg, JOptionPane.ERROR_MESSAGE);
      log.error(downloadErrMsg);
      finish();
    }

    jvmPatcherProgressBar.setValue(2);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_delete"));
    try {
      if (!FileUtil.fileExists(this._path + File.separator + "java_vm_unpatched")) {
        FileUtils.moveDirectory(new File(this._path, "java_vm"), new File(this._path, "java_vm_unpatched"));
      }
    } catch (IOException e) {
      log.error(e);
    }

    jvmPatcherProgressBar.setValue(3);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_extract"));
    Compressor.unzip(this._path + File.separator + "jvm_pack.zip", this._path, false);
    new File(this._path, "jvm_pack.zip").delete();

    jvmPatcherProgressBar.setValue(4);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_finish"));

    closeButton.setEnabled(true);
    finish();
  }

  private void downloadPackagedJVM() {
    String selectedJVM = _availableJVM.get(jvmComboBox.getSelectedItem().toString());
    String downloadUrl = LauncherGlobals.URL_JAVA_REDIST.replace("{version}", selectedJVM);

    boolean downloadCompleted = false;
    while(_downloadAttempts <= 3 && !downloadCompleted) {
      _downloadAttempts++;
      log.info("Downloading Java VM", "url", downloadUrl, "attempts", _downloadAttempts);
      try {
        FileUtils.copyURLToFile(
                new URL(downloadUrl),
                new File(this._path, "jvm_pack.zip"),
                0,
                0
        );
        downloadCompleted = true;
      } catch (IOException e) {
        // Just keep retrying.
        log.error(e);
      }
    }
  }

  private void finish() {
    ModuleLoader.loadJarCommandLine();
    DiscordRPC.getInstance().stop();
    ProcessUtil.run(new String[]{"java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar"}, true);
    jvmPatcherFrame.dispose();
    System.exit(1);
  }

  private JButton closeButton;

  private JLabel headerLabel;
  private JLabel subHeaderLabel;
  private JButton buttonAccept;
  private JProgressBar jvmPatcherProgressBar;
  private JLabel jvmPatcherState;
  private JComboBox<String> jvmComboBox;

}

