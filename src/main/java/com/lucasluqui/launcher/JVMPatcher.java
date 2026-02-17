package com.lucasluqui.launcher;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.discord.DiscordPresenceClient;
import com.lucasluqui.download.DownloadManager;
import com.lucasluqui.download.data.URLDownloadQueue;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.swing.SmoothProgressBar;
import com.lucasluqui.util.FileUtil;
import com.lucasluqui.util.ImageUtil;
import com.lucasluqui.util.ProcessUtil;
import com.lucasluqui.util.ZipUtil;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.lucasluqui.launcher.Log.log;

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
      this.availableJVMs.put("Java 25 (OpenJDK 25.0.2_10)", "ojdk-25.0.2_10");
      this.availableJVMs.put("Java 25 (GraalVM 25.0.2+10.1), ", "graalvm-25.0.2_10.1");
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
    guiFrame.setShape(new RoundRectangle2D.Double(0, 0, this.width, this.height, 15, 15));
    guiFrame.getContentPane().setLayout(null);

    headerLabel = new JLabel("Patch your game to use a compatible 64-bit Java VM");
    headerLabel.setBounds(0, 50, 500, 37);
    headerLabel.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    guiFrame.getContentPane().add(headerLabel);

    subHeaderLabel = new JLabel("You can always restart this patcher from the Settings menu");
    subHeaderLabel.setBounds(0, 75, 500, 37);
    subHeaderLabel.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    subHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    guiFrame.getContentPane().add(subHeaderLabel);

    JLabel jvmSelectLabel = new JLabel("Select a Java version to install");
    jvmSelectLabel.setBounds(0, 115, 500, 37);
    jvmSelectLabel.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    jvmSelectLabel.setHorizontalAlignment(SwingConstants.CENTER);
    guiFrame.getContentPane().add(jvmSelectLabel);

    jvmComboBox = new JComboBox<>();
    jvmComboBox.setBounds(125, 145, 255, 20);
    jvmComboBox.setFocusable(false);
    jvmComboBox.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    guiFrame.add(jvmComboBox);

    for (String key : this.availableJVMs.keySet()) {
      jvmComboBox.addItem(key);
    }
    jvmComboBox.setSelectedIndex(0);

    jvmPatcherState = new JLabel("");
    jvmPatcherState.setIcon(new ImageIcon(this.getClass().getResource("/rsrc/img/loading.gif")));
    jvmPatcherState.setVisible(false);
    jvmPatcherState.setBounds(26, 180, 450, 15);
    jvmPatcherState.setFont(Fonts.getFont("defaultRegular", 11.0f, Font.ITALIC));
    guiFrame.getContentPane().add(jvmPatcherState);

    jvmPatcherProgressBar = new SmoothProgressBar();
    jvmPatcherProgressBar.setBounds(25, 204, 450, 25);
    jvmPatcherProgressBar.setVisible(false);
    guiFrame.getContentPane().add(jvmPatcherProgressBar);

    buttonAccept = new JButton("Start patching");
    buttonAccept.setBounds(150, 200, 200, 25);
    buttonAccept.setFocusPainted(false);
    buttonAccept.setFocusable(false);
    buttonAccept.setFont(Fonts.getFont("defaultMedium", 11.0f, Font.PLAIN));
    buttonAccept.setForeground(Color.WHITE);
    buttonAccept.setBackground(CustomColors.INTERFACE_BUTTON_BACKGROUND);
    buttonAccept.putClientProperty(FlatClientProperties.STYLE,
      "arc: 999; borderWidth: 0");
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
    ZipUtil.unzip(this.path + File.separator + "jvm_pack.zip", this.path);
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
        "\n" + BuildConfig.getName() + " will boot without patching but be aware game performance might not be the best." +
        "\nYou can manually restart this patcher heading to the 'Game' tab within the launcher's Settings menu.";
      Dialog.push(downloadErrMsg, JOptionPane.ERROR_MESSAGE);
      log.error(downloadErrMsg);
    }
  }

  private void finish ()
  {
    _settingsManager.setValue("launcher.jvm_patched", "true");
    _moduleManager.loadJarCommandLine();
    _discordPresenceClient.stop();
    ProcessUtil.run(new String[]{"java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar"}, true);
    guiFrame.dispose();
    System.exit(0);
  }

  private JLabel headerLabel;
  private JLabel subHeaderLabel;
  private JButton buttonAccept;
  private SmoothProgressBar jvmPatcherProgressBar;
  private JLabel jvmPatcherState;
  private JComboBox<String> jvmComboBox;

}

