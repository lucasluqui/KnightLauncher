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

import static com.luuqui.launcher.Log.log;

public class JVMPatcher extends BaseGUI {

  private final LauncherApp app;
  public static JFrame jvmPatcherFrame;
  private static JLabel headerLabel;
  private static JLabel subHeaderLabel;
  private static JButton buttonAccept;
  private static JButton buttonDecline;
  private static JProgressBar jvmPatcherProgressBar;
  private static JLabel jvmPatcherState;
  private static JComboBox<String> javaVersionComboBox;
  private static int _downloadAttempts = 0;

  public JVMPatcher(LauncherApp app) {
    super();
    this.app = app;
    initialize();
  }

  @SuppressWarnings("static-access")
  public void switchVisibility() {
    this.jvmPatcherFrame.setVisible(!this.jvmPatcherFrame.isVisible());
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

    JLabel javaVersionSelectLabel = new JLabel("Select a Java version to install");
    javaVersionSelectLabel.setBounds(0, 115, 500, 37);
    javaVersionSelectLabel.setFont(Fonts.fontMed);
    javaVersionSelectLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jvmPatcherFrame.getContentPane().add(javaVersionSelectLabel);

    javaVersionComboBox = new JComboBox<>();
    javaVersionComboBox.setBounds(125, 145, 255, 20);
    javaVersionComboBox.setFocusable(false);
    javaVersionComboBox.setFont(Fonts.fontReg);
    jvmPatcherFrame.add(javaVersionComboBox);

    javaVersionComboBox.addItem("Java 8 (8u202) (Recommended)");
    javaVersionComboBox.addItem("Java 8 (8u251)");
    javaVersionComboBox.setSelectedIndex(0);

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
      javaVersionSelectLabel.setVisible(false);
      javaVersionComboBox.setVisible(false);
      headerLabel.setText("This will take a few minutes...");
      subHeaderLabel.setText("Please do not close this window.");
      jvmPatcherProgressBar.setVisible(true);
      initPatcher();
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
    JButton closeButton = new JButton(closeIcon);
    closeButton.setBounds(jvmPatcherFrame.getWidth() - BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
    closeButton.setToolTipText(Locale.getValue("b.close"));
    closeButton.setFocusPainted(false);
    closeButton.setFocusable(false);
    closeButton.setBackground(null);
    closeButton.setBorder(null);
    closeButton.setVisible(Settings.jvmPatched);
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

  private static void initPatcher() {
    Thread patchThread = new Thread(new Runnable() {
      public void run() {
        patch();
      }
    });
    patchThread.start();
  }

  private static void patch() {
    jvmPatcherProgressBar.setMaximum(4);
    jvmPatcherProgressBar.setValue(1);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_download", "74"));
    
    downloadPackagedJVM();
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
      if (!FileUtil.fileExists(LauncherApp.javaVMPatchDir + File.separator + "java_vm_unpatched")) {
        FileUtils.moveDirectory(new File(LauncherApp.javaVMPatchDir, "java_vm"), new File(LauncherApp.javaVMPatchDir, "java_vm_unpatched"));
      }
    } catch (IOException e) {
      log.error(e);
    }

    jvmPatcherProgressBar.setValue(3);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_extract"));
    Compressor.unzip(LauncherApp.javaVMPatchDir + File.separator + "jvm_pack.zip", LauncherApp.javaVMPatchDir, false);
    new File(LauncherApp.javaVMPatchDir, "jvm_pack.zip").delete();

    jvmPatcherProgressBar.setValue(4);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_finish"));
    finish();
  }

  private static void downloadPackagedJVM() {
    String downloadUrl = LauncherGlobals.URL_JAVA_REDISTRIBUTABLES.replace("{version}", "7u80");

    switch(javaVersionComboBox.getSelectedIndex()) {
      //case 0: downloadUrl = LauncherGlobals.URL_JAVA_REDISTRIBUTABLES.replace("{version}", "7u80"); break;
      case 0: downloadUrl = LauncherGlobals.URL_JAVA_REDISTRIBUTABLES.replace("{version}", "8u202"); break;
      case 1: downloadUrl = LauncherGlobals.URL_JAVA_REDISTRIBUTABLES.replace("{version}", "8u251"); break;
    }

    boolean downloadCompleted = false;
    while(_downloadAttempts <= 3 && !downloadCompleted) {
      _downloadAttempts++;
      log.info("Downloading Java VM", "url", downloadUrl, "attempts", _downloadAttempts);
      try {
        FileUtils.copyURLToFile(
                new URL(downloadUrl),
                new File(LauncherApp.javaVMPatchDir, "jvm_pack.zip"),
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

  private static void finish() {
    SettingsProperties.setValue("launcher.jvm_patched", "true");
    ModuleLoader.loadJarCommandLine();
    DiscordRPC.getInstance().stop();
    ProcessUtil.run(new String[]{"java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar"}, true);
    jvmPatcherFrame.dispose();
    System.exit(1);
  }
}

