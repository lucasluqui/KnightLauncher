package com.lucasallegri.launcher;

import com.lucasallegri.dialog.DialogError;
import com.lucasallegri.launcher.settings.SettingsProperties;
import com.lucasallegri.util.ColorUtil;
import com.lucasallegri.util.Compressor;
import com.lucasallegri.util.FileUtil;
import com.lucasallegri.util.ProcessUtil;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.lucasallegri.launcher.Log.log;

public class JVMPatcher extends BaseGUI {

  private final LauncherApp app;
  public static JFrame jvmPatcherFrame;
  private static JLabel headerLabel;
  private static JLabel subHeaderLabel;
  private static JButton buttonAccept;
  private static JButton buttonDecline;
  private static JProgressBar jvmPatcherProgressBar;
  private static JLabel jvmPatcherState;
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
    jvmPatcherFrame.getContentPane().setLayout(null);

    headerLabel = new JLabel(Locale.getValue("m.jvm_patcher_confirm_header"));
    headerLabel.setBounds(10, 40, 480, 37);
    headerLabel.setFont(Fonts.fontRegBig);
    headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jvmPatcherFrame.getContentPane().add(headerLabel);

    subHeaderLabel = new JLabel(Locale.getValue("m.jvm_patcher_confirm_subheader"));
    subHeaderLabel.setBounds(10, 65, 480, 37);
    subHeaderLabel.setFont(Fonts.fontReg);
    subHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    jvmPatcherFrame.getContentPane().add(subHeaderLabel);

    jvmPatcherState = new JLabel("");
    jvmPatcherState.setBounds(11, 180, 480, 15);
    jvmPatcherState.setFont(Fonts.fontReg);
    jvmPatcherFrame.getContentPane().add(jvmPatcherState);

    jvmPatcherProgressBar = new JProgressBar();
    jvmPatcherProgressBar.setBounds(10, 204, 480, 5);
    jvmPatcherProgressBar.setVisible(false);
    jvmPatcherFrame.getContentPane().add(jvmPatcherProgressBar);

    buttonAccept = new JButton(Locale.getValue("b.jvm_patcher_accept"));
    buttonAccept.setFocusPainted(false);
    buttonAccept.setFocusable(false);
    buttonAccept.setFont(Fonts.fontMed);
    buttonAccept.setBounds(30, 200, 200, 23);
    jvmPatcherFrame.getContentPane().add(buttonAccept);
    buttonAccept.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        buttonAccept.setEnabled(false);
        buttonAccept.setVisible(false);
        buttonDecline.setEnabled(false);
        buttonDecline.setVisible(false);
        headerLabel.setText(Locale.getValue("m.jvm_patcher_header"));
        subHeaderLabel.setText(Locale.getValue("m.jvm_patcher_subheader"));
        jvmPatcherProgressBar.setVisible(true);
        initPatcher();
      }
    });

    buttonDecline = new JButton(Locale.getValue("b.jvm_patcher_decline"));
    buttonDecline.setFocusPainted(false);
    buttonDecline.setFocusable(false);
    buttonDecline.setFont(Fonts.fontMed);
    buttonDecline.setBounds(360, 200, 110, 23);
    jvmPatcherFrame.getContentPane().add(buttonDecline);
    buttonDecline.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _action) {
        finish();
      }
    });

    JPanel titleBar = new JPanel();
    titleBar.setBounds(0, 0, jvmPatcherFrame.getWidth(), 20);
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

    JLabel windowTitle = new JLabel(Locale.getValue("t.jvm_patcher"));
    windowTitle.setFont(Fonts.fontMed);
    windowTitle.setBounds(10, 0, jvmPatcherFrame.getWidth() - 100, 20);
    titleBar.add(windowTitle);

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
              "Knight Launcher will boot without patching but be aware game performance might not be the best." +
              "You can manually restart this patcher heading to the 'Files' tab within launcher's settings.";
      DialogError.push(downloadErrMsg);
      log.error(downloadErrMsg);
      finish();
    }

    jvmPatcherProgressBar.setValue(2);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_delete"));
    try {
      if (!FileUtil.fileExists(LauncherGlobals.USER_DIR + "\\java_vm_unpatched")) {
        FileUtils.moveDirectory(new File(LauncherGlobals.USER_DIR + "\\java_vm"), new File(LauncherGlobals.USER_DIR + "\\java_vm_unpatched"));
      }
    } catch (IOException e) {
      log.error(e);
    }

    jvmPatcherProgressBar.setValue(3);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_extract"));
    Compressor.unzip(LauncherGlobals.USER_DIR + "\\jvm_pack.zip", LauncherGlobals.USER_DIR, false);
    new File(LauncherGlobals.USER_DIR + "\\jvm_pack.zip").delete();

    jvmPatcherProgressBar.setValue(4);
    jvmPatcherState.setText(Locale.getValue("m.jvm_patcher_finish"));
    finish();
  }

  private static void downloadPackagedJVM() {
    String downloadUrl = LauncherGlobals.LARGE_CDN_URL
            + "jvm/windows/jvm_pack.zip";

    boolean downloadCompleted = false;
    while(_downloadAttempts <= 3 && !downloadCompleted) {
      _downloadAttempts++;
      log.info("Downloading Java VM", "url", downloadUrl, "attempts", _downloadAttempts);
      try {
        FileUtils.copyURLToFile(
                new URL(downloadUrl),
                new File(LauncherGlobals.USER_DIR + "\\jvm_pack.zip"),
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
    ProcessUtil.startApplication(new String[]{"java", "-jar", LauncherGlobals.USER_DIR + "\\KnightLauncher.jar"});
    jvmPatcherFrame.dispose();
    System.exit(1);
  }
}

