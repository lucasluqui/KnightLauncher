package com.luuqui.launcher;

import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.discord.DiscordPresenceClient;
import com.luuqui.download.DownloadManager;
import com.luuqui.download.data.URLDownloadQueue;
import com.luuqui.launcher.setting.Settings;
import com.luuqui.launcher.setting.SettingsManager;
import com.luuqui.util.*;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.luuqui.launcher.Log.log;

public class JVMPatcher extends BaseGUI
{
  @Inject protected LauncherContext _launcherCtx;
  @Inject protected LocaleManager _localeManager;
  @Inject protected SettingsManager _settingsManager;
  @Inject protected ModuleManager _moduleManager;
  @Inject protected DownloadManager _downloadManager;
  @Inject protected DiscordPresenceClient _discordPresenceClient;

  private String path;
  private boolean legacy;
  private final Map<String, String> availableJVMs = new HashMap<String, String>();

  public JVMPatcher ()
  {
    super(500, 250, true);
  }

  public void init (String path, boolean legacy)
  {
    this.path = path;
    this.legacy = legacy;
    setAvailableJVMs();
    compose();
  }

  private void setAvailableJVMs ()
  {
    if (legacy) {
      this.availableJVMs.put("Java 8 (8u202)", "8u202");
      this.availableJVMs.put("Java 8 (8u251)", "8u251");
    } else {
      this.availableJVMs.put("Java 11 (OpenJDK 11.0.26)", "ojdk-11.0.26");
    }
  }

  private void compose ()
  {
    guiFrame.setVisible(false);
    guiFrame.setTitle(_localeManager.getValue("t.jvm_patcher"));
    guiFrame.setBounds(100, 100, this.width, this.height);
    guiFrame.setResizable(false);
    guiFrame.setUndecorated(true);
    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    guiFrame.setIconImage(ImageUtil.loadImageWithinJar("/rsrc/img/icon-128.png"));
    guiFrame.getContentPane().setBackground(CustomColors.INTERFACE_MAINPANE_BACKGROUND);
    guiFrame.getContentPane().setLayout(null);

    headerLabel = new JLabel("Patch your game to use a compatible 64-bit Java VM");
    headerLabel.setBounds(0, 50, 500, 37);
    headerLabel.setFont(Fonts.fontMed);
    headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    guiFrame.getContentPane().add(headerLabel);

    subHeaderLabel = new JLabel("You can always restart this patcher from the Settings menu");
    subHeaderLabel.setBounds(0, 75, 500, 37);
    subHeaderLabel.setFont(Fonts.fontReg);
    subHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    guiFrame.getContentPane().add(subHeaderLabel);

    JLabel jvmSelectLabel = new JLabel("Select a Java version to install");
    jvmSelectLabel.setBounds(0, 115, 500, 37);
    jvmSelectLabel.setFont(Fonts.fontMed);
    jvmSelectLabel.setHorizontalAlignment(SwingConstants.CENTER);
    guiFrame.getContentPane().add(jvmSelectLabel);

    jvmComboBox = new JComboBox<>();
    jvmComboBox.setBounds(125, 145, 255, 20);
    jvmComboBox.setFocusable(false);
    jvmComboBox.setFont(Fonts.fontReg);
    guiFrame.add(jvmComboBox);

    for (String key : this.availableJVMs.keySet()) {
      jvmComboBox.addItem(key);
    }
    jvmComboBox.setSelectedIndex(0);

    jvmPatcherState = new JLabel("");
    jvmPatcherState.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    jvmPatcherState.setVisible(false);
    jvmPatcherState.setBounds(26, 180, 450, 15);
    jvmPatcherState.setFont(Fonts.fontReg);
    guiFrame.getContentPane().add(jvmPatcherState);

    jvmPatcherProgressBar = new JProgressBar();
    jvmPatcherProgressBar.setBounds(25, 204, 450, 25);
    jvmPatcherProgressBar.setVisible(false);
    guiFrame.getContentPane().add(jvmPatcherProgressBar);

    buttonAccept = new JButton("Start patching");
    buttonAccept.setFocusPainted(false);
    buttonAccept.setFocusable(false);
    buttonAccept.setFont(Fonts.fontMed);
    buttonAccept.setBounds(150, 200, 200, 25);
    guiFrame.getContentPane().add(buttonAccept);
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

    closeButton.addActionListener(e -> {
      if (Settings.jvmPatched) {
        _launcherCtx.exit(true);
      } else {
        Dialog.push(
            "On your first time launching you are required to patch a 64-bit Java VM\nand thus you cannot close this window.",
            "Java VM Patcher",
            JOptionPane.INFORMATION_MESSAGE
        );
      }
    });
    closeButton.setToolTipText(_localeManager.getValue("b.close"));
    minimizeButton.setToolTipText(_localeManager.getValue("b.minimize"));

    guiFrame.setLocationRelativeTo(null);
    guiFrame.setVisible(true);

  }

  private void initPatcher ()
  {
    Thread patchThread = new Thread(this::patch);
    patchThread.start();
  }

  private void patch ()
  {
    // Don't allow closing the window once patching starts.
    closeButton.setEnabled(false);

    jvmPatcherProgressBar.setMaximum(4);
    jvmPatcherState.setVisible(true);

    jvmPatcherProgressBar.setValue(1);
    jvmPatcherState.setText(_localeManager.getValue("m.jvm_patcher_download"));
    this.downloadJVM();

    jvmPatcherProgressBar.setValue(2);
    jvmPatcherState.setText(_localeManager.getValue("m.jvm_patcher_delete"));
    try {
      if (!FileUtil.fileExists(this.path + File.separator + "java_vm_unpatched")) {
        FileUtils.moveDirectory(new File(this.path, "java_vm"), new File(this.path, "java_vm_unpatched"));
      }
    } catch (IOException e) {
      log.error(e);
    }

    jvmPatcherProgressBar.setValue(3);
    jvmPatcherState.setText(_localeManager.getValue("m.jvm_patcher_extract"));
    Compressor.unzip(this.path + File.separator + "jvm_pack.zip", this.path, false);
    new File(this.path, "jvm_pack.zip").delete();

    jvmPatcherProgressBar.setValue(4);
    jvmPatcherState.setText(_localeManager.getValue("m.jvm_patcher_finish"));

    closeButton.setEnabled(true);
    finish();
  }

  private void downloadJVM ()
  {
    String selectedJVM = this.availableJVMs.get(jvmComboBox.getSelectedItem().toString());

    URL downloadUrl = null;
    try {
      downloadUrl = new URL(LauncherGlobals.URL_JAVA_REDIST.replace("{version}", selectedJVM));
    } catch (MalformedURLException e) {
      log.error(e);
    }

    URLDownloadQueue downloadQueue = new URLDownloadQueue(
        "Java VM Patch", downloadUrl, new File(this.path, "jvm_pack.zip")
    );

    _downloadManager.add(downloadQueue);
    _downloadManager.processQueues();

    if (!_downloadManager.getQueueStatus(downloadQueue)) {
      String downloadErrMsg = "The Java VM download couldn't be initiated after 3 attempts." +
          "\nKnight Launcher will boot without patching but be aware game performance might not be the best." +
          "\nYou can manually restart this patcher heading to the 'Game' tab within launcher's Settings.";
      Dialog.push(downloadErrMsg, JOptionPane.ERROR_MESSAGE);
      log.error(downloadErrMsg);
    }
  }

  private void finish ()
  {
    _settingsManager.setValue("launcher.jvm_patched", "true"); // Delete this when the dreaded day comes.
    _moduleManager.loadJarCommandLine();
    _discordPresenceClient.stop();
    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar" }, true);
    guiFrame.dispose();
    System.exit(0);
  }

  private JLabel headerLabel;
  private JLabel subHeaderLabel;
  private JButton buttonAccept;
  private JProgressBar jvmPatcherProgressBar;
  private JLabel jvmPatcherState;
  private JComboBox<String> jvmComboBox;

}

