package com.luuqui.launcher.setting;

import com.formdev.flatlaf.FlatClientProperties;
import com.google.inject.Inject;
import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.*;
import com.luuqui.launcher.flamingo.FlamingoManager;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.flamingo.data.Status;
import com.luuqui.launcher.mod.ModManager;
import com.luuqui.util.*;
import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.luuqui.launcher.setting.Log.log;

public class SettingsEventHandler
{
  @Inject private SettingsGUI gui;

  protected LauncherContext _launcherCtx;
  protected ModManager _modManager;
  protected LocaleManager _localeManager;
  protected SettingsManager _settingsManager;
  protected FlamingoManager _flamingoManager;

  @Inject
  public SettingsEventHandler (LauncherContext _launcherCtx,
                               ModManager _modManager,
                               LocaleManager _localeManager,
                               SettingsManager _settingsManager,
                               FlamingoManager _flamingoManager)
  {
    this._launcherCtx = _launcherCtx;
    this._modManager = _modManager;
    this._localeManager = _localeManager;
    this._settingsManager = _settingsManager;
    this._flamingoManager = _flamingoManager;
  }

  public void platformChangeEvent (ItemEvent event)
  {
    Settings.gamePlatform = (String) this.gui.choicePlatform.getSelectedItem();
    _settingsManager.setValue("game.platform", (String) this.gui.choicePlatform.getSelectedItem());
  }

  public void rebuildsChangeEvent (ActionEvent event)
  {
    Settings.doRebuilds = this.gui.switchCleaning.isSelected();
    _settingsManager.setValue("launcher.rebuilds", this.gui.switchCleaning.isSelected() ? "true" : "false");
  }

  public void keepOpenChangeEvent (ActionEvent event)
  {
    Settings.keepOpen = this.gui.switchKeepOpen.isSelected();
    _settingsManager.setValue("launcher.keepOpen", this.gui.switchKeepOpen.isSelected() ? "true" : "false");
  }

  public void forceRebuildEvent ()
  {
    _modManager.mountRequired = true;
    new Thread(_modManager::startStrictFileRebuild).start();
    _launcherCtx.launcherGUI.setOnTop();
  }

  public void createShortcutChangeEvent (ActionEvent event)
  {
    Settings.createShortcut = this.gui.switchShortcut.isSelected();
    _settingsManager.setValue("launcher.createShortcut", this.gui.switchShortcut.isSelected() ? "true" : "false");
  }

  public void languageChangeEvent (ItemEvent event)
  {
    if(event.getStateChange() == ItemEvent.SELECTED) return; // Prevent triggering 2 times
    Settings.lang = _localeManager.getLangCode((String) this.gui.choiceLanguage.getSelectedItem());
    _settingsManager.setValue("launcher.lang", _localeManager.getLangCode((String) this.gui.choiceLanguage.getSelectedItem()));
    Dialog.push(_localeManager.getValue("m.prompt_restart_required"), JOptionPane.INFORMATION_MESSAGE);
  }

  public void customGCChangeEvent (ActionEvent action)
  {
    Settings.gameUseCustomGC = this.gui.switchUseCustomGC.isSelected();
    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.useCustomGC", this.gui.switchUseCustomGC.isSelected() ? "true" : "false", selectedServer);
  }

  public void choiceGCChangeEvent (ItemEvent event)
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    switch (this.gui.choiceGC.getSelectedIndex()) {
      case 0:
        Settings.gameGarbageCollector = "ParallelOld";
        _settingsManager.setValue("game.garbageCollector", "ParallelOld", selectedServer);
        break;
      case 1:
        Settings.gameGarbageCollector = "Serial";
        _settingsManager.setValue("game.garbageCollector", "Serial", selectedServer);
        break;
      case 2:
        Settings.gameGarbageCollector = "G1";
        _settingsManager.setValue("game.garbageCollector", "G1", selectedServer);
        break;
    }
  }

  public void disableExplicitGCChangeEvent (ActionEvent action)
  {
    Settings.gameDisableExplicitGC = this.gui.switchExplicitGC.isSelected();
    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.disableExplicitGC", this.gui.switchExplicitGC.isSelected() ? "true" : "false", selectedServer);
  }

  public void saveAdditionalArgs ()
  {
    Settings.gameAdditionalArgs = this.gui.argumentsPane.getText();
    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.additionalArgs", this.gui.argumentsPane.getText(), selectedServer);
  }

  public void memoryChangeEvent (int memory)
  {
    Settings.gameMemory = memory;
    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.memory", String.valueOf(memory), selectedServer);
  }

  public void ingameRPCChangeEvent (ActionEvent action)
  {
    Settings.useIngameRPC = this.gui.switchDiscordIntegration.isSelected();
    _settingsManager.setValue("launcher.useIngameRPC", this.gui.switchDiscordIntegration.isSelected() ? "true" : "false");
  }

  public void autoUpdateChangeEvent (ActionEvent action)
  {
    Settings.autoUpdate = this.gui.switchAutoUpdate.isSelected();
    _settingsManager.setValue("launcher.autoUpdate", this.gui.switchAutoUpdate.isSelected() ? "true" : "false");
  }

  public void filePurgingChangeEvent (ActionEvent action)
  {
    Settings.filePurging = this.gui.switchFilePurging.isSelected();
    _settingsManager.setValue("launcher.filePurging", this.gui.switchAutoUpdate.isSelected() ? "true" : "false");
  }

  public void jvmPatchEvent (ActionEvent action)
  {
    String javaVMPatchDir = LauncherGlobals.USER_DIR;
    //boolean legacy = false;
    boolean legacy = true; // Temporarily set Official to use legacy JVMs too. TODO: Change when game updates to Java 10+.

    if (!_flamingoManager.getSelectedServer().isOfficial()) {
      javaVMPatchDir += File.separator + "thirdparty" + File.separator + _flamingoManager.getSelectedServer().getSanitizedName();

      // Set legacy to true to indicate the JVM Patcher that this installation only supports Java 8 and lower JVMs.
      // Ideally, all servers should follow Official and support newer Java versions at the same pace, but that's not the case.
      legacy = true;
    }

    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar", "forceJVMPatch", javaVMPatchDir, String.valueOf(legacy)}, true);
    this.gui.guiFrame.dispose();
    System.exit(1);
  }

  public void saveConnectionSettings ()
  {
    Settings.gameEndpoint = this.gui.serverAddressTextField.getText();
    Settings.gamePort = Integer.parseInt(this.gui.portTextField.getText());
    Settings.gamePublicKey = this.gui.publicKeyTextField.getText();
    Settings.gameGetdownFullURL = this.gui.getdownURLTextField.getText();
    Settings.gameGetdownURL = "http://" + this.gui.getdownURLTextField.getText().split("://")[1].split("/")[0];

    _settingsManager.setValue("game.endpoint", Settings.gameEndpoint);
    _settingsManager.setValue("game.port", String.valueOf(Settings.gamePort));
    _settingsManager.setValue("game.publicKey", Settings.gamePublicKey);
    _settingsManager.setValue("game.getdownURL", Settings.gameGetdownURL);
    _settingsManager.setValue("game.getdownFullURL", Settings.gameGetdownFullURL);
  }

  public void resetConnectionSettingsButtonEvent (ActionEvent action)
  {
    this.gui.serverAddressTextField.setText(DEFAULT_SERVER_ADDRESS);
    this.gui.portTextField.setText(DEFAULT_PORT);
    this.gui.publicKeyTextField.setText(DEFAULT_PUBLIC_KEY);
    this.gui.getdownURLTextField.setText(DEFAULT_GETDOWN_URL);

    saveConnectionSettings();
  }

  public void resetGameSettingsButtonEvent (ActionEvent action)
  {
    this.gui.memorySlider.setValue(DEFAULT_MEMORY);
    this.gui.switchUseCustomGC.setSelected(DEFAULT_USE_CUSTOM_GC);
    this.gui.choiceGC.setSelectedItem(DEFAULT_GC);
    this.gui.switchExplicitGC.setSelected(DEFAULT_DISABLE_EXPLICIT_GC);
    this.gui.argumentsPane.setText(DEFAULT_ADDITIONAL_ARGS);


    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
    saveAdditionalArgs();
  }

  public void loadRecommendedSettingsButtonEvent (ActionEvent action)
  {
    long maximumMemory = ((OperatingSystemMXBean) ManagementFactory
      .getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1048576;

    int recommendedMemory = (int) Math.min(JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 ? 3072 : 1024, maximumMemory * 0.25);

    log.info("Recommended settings: Maximum physical memory is " + maximumMemory
      + ", setting allocated memory to " + recommendedMemory);

    this.gui.memorySlider.setValue(recommendedMemory);
    this.gui.switchUseCustomGC.setSelected(RECOMMENDED_USE_CUSTOM_GC);
    this.gui.choiceGC.setSelectedItem(RECOMMENDED_GC);
    this.gui.switchExplicitGC.setSelected(RECOMMENDED_DISABLE_EXPLICIT_GC);


    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
  }

  public void copyLauncherLogEvent (ActionEvent action)
  {
    List<File> files = new ArrayList<>();

    // Add launcher logs to clipboard.
    files.add(new File(LauncherGlobals.USER_DIR + "\\knightlauncher.log"));

    FileUtil.copyFilesToClipboard(files);
  }

  public void copyGameLogEvent(ActionEvent action) {
    List<File> files = new ArrayList<>();

    // Initial path where we'll pull game logs from.
    String path = LauncherGlobals.USER_DIR;

    // Check if a third party server is selected, in that case, modify the path to copy their logs instead.
    if(!_flamingoManager.getSelectedServer().name.equalsIgnoreCase("Official")) {
      path += "\\thirdparty\\" + _flamingoManager.getSelectedServer().getSanitizedName();
    }

    // Copy all game logs.
    File getdownLog = new File(path + "\\launcher.log");
    files.add(getdownLog);

    File gameLog = new File(path + "\\projectx.log");
    files.add(gameLog);

    File oldGameLog = new File(path + "\\old-projectx.log");
    files.add(oldGameLog);

    files.removeIf(file -> !FileUtil.fileExists(file.getAbsolutePath()));

    FileUtil.copyFilesToClipboard(files);
  }

  public void copyLogsEvent (ActionEvent action)
  {
    List<File> files = new ArrayList<>();

    // Add launcher logs to clipboard.
    files.add(new File(LauncherGlobals.USER_DIR + "\\knightlauncher.log"));

    // Initial path where we'll pull game logs from.
    String path = LauncherGlobals.USER_DIR;

    // Check if a third party server is selected, in that case, modify the path to copy their logs instead.
    if(!_flamingoManager.getSelectedServer().name.equalsIgnoreCase("Official")) {
      path += "\\thirdparty\\" + _flamingoManager.getSelectedServer().getSanitizedName();
    }

    // Copy all game logs.
    File getdownLog = new File(path + "\\launcher.log");
    files.add(getdownLog);

    File gameLog = new File(path + "\\projectx.log");
    files.add(gameLog);

    File oldGameLog = new File(path + "\\old-projectx.log");
    files.add(oldGameLog);

    files.removeIf(file -> !FileUtil.fileExists(file.getAbsolutePath()));

    // ...And we add them to the clipboard.
    FileUtil.copyFilesToClipboard(files);
  }

  public void openRootFolderEvent (ActionEvent event)
  {
    String rootDir = LauncherGlobals.USER_DIR;
    if(_flamingoManager.getSelectedServer() != null) {
      rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
    }
    DesktopUtil.openDir(rootDir);
  }

  public void updateAboutTab (Status status)
  {
    if(status.version != null) {
      long uptime = System.currentTimeMillis() - status.uptime;
      String uptimeString = Duration.ofMillis(uptime)
        .toString()
        .replace( "PT" , "" )
        .replace( "H" , " " + _localeManager.getValue("m.hours").toLowerCase() + " " )
        .replace( "M" , " " + _localeManager.getValue("m.minutes").toLowerCase() + " " );
      uptimeString = uptimeString.substring(0, uptimeString.length() - 7);

      this.gui.labelFlamingoStatus.setText(_localeManager.getValue("m.flamingo_status", _localeManager.getValue("m.online")));
      this.gui.labelFlamingoVersion.setText(_localeManager.getValue("m.flamingo_version", status.version));

      if(uptimeString.isEmpty()) {
        this.gui.labelFlamingoUptime.setText(_localeManager.getValue("m.flamingo_uptime", _localeManager.getValue("m.recently_restarted")));
      } else {
        this.gui.labelFlamingoUptime.setText(_localeManager.getValue("m.flamingo_uptime", uptimeString));
      }
    }
  }

  // returns:
  // 0 = some failure
  // 1 = success
  // 2 = duplicate (local)
  // 3 = already used (other)
  // 4 = already used (same machine id)
  // 5 = not exist
  // TODO: use enums.
  public int activateBetaCode (String code, boolean force)
  {

    // Sanitize the code (remove any trailing spaces, white spaces, slashes, etc.)
    code = code.replace("/", "")
      .replace("?", "")
      .replace("&", "")
      .replace("\"", "")
      .trim();

    // If the code is empty, do nothing
    if(code.isEmpty()) return -1;

    // Contains any more invalid characters.
    if(!code.matches("^[0-9A-Za-z\\s-]+$")) return -1;

    // Get the currently loaded codes
    String codes = _settingsManager.getValue("launcher.betaCodes");

    // This beta code is already present in local properties. We return '2' indicating duplicate.
    if(codes != null && codes.contains(code) && !force) return 2;

    String response = _flamingoManager.activateBetaCode(code);

    // someone already activated this code.
    if(response.equalsIgnoreCase("already_used")) return 3;

    // this machine id already activated this code, we add it anyways to avoid further requests from this machine.
    if(response.equalsIgnoreCase("already_used_same")) {
      addBetaCode(code);
      return 4;
    }

    // this code does not exist.
    if(response.equalsIgnoreCase("not_exists")) return 5;

    // the code was successfully activated, we update the server list and return a success code to the GUI.
    if(response.equalsIgnoreCase("success")) {
      addBetaCode(code);
      _launcherCtx.launcherGUI.eventHandler.updateServerList(_flamingoManager.fetchServerList());
      updateActiveBetaCodes();
      return 1;
    }

    // Return 0 indicating some sort of failure.
    return 0;
  }

  public void revalidateBetaCodes ()
  {
    if (_settingsManager.getValue("launcher.betaCodes").equalsIgnoreCase("")) return;

    String[] betaCodes;
    if(_settingsManager.getValue("launcher.betaCodes").contains(",")) {
      betaCodes = _settingsManager.getValue("launcher.betaCodes").split(",");
    } else {
      betaCodes = new String[] { _settingsManager.getValue("launcher.betaCodes") };
    }

    for(String betaCode : betaCodes) {
      activateBetaCode(betaCode, true);
    }
  }

  public void clearLocalBetaCodes ()
  {
    _settingsManager.setValue("launcher.betaCodes", "");
  }

  private void addBetaCode (String code)
  {
    // Get the currently loaded codes
    String codes = _settingsManager.getValue("launcher.betaCodes");

    // Check if there are any codes to properly format the code string
    if(codes.equalsIgnoreCase("")) {
      codes += code;
    } else {
      // don't add duplicates
      if(codes.contains(code)) return;

      codes += "," + code;
    }

    // Successfully added a new beta code, update the properties file.
    _settingsManager.setValue("launcher.betaCodes", codes);
  }

  public void selectedServerChanged ()
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    if (selectedServer != null) {
      if (selectedServer.name.equalsIgnoreCase("Official")) {
        this.gui.switchDiscordIntegration.setEnabled(SystemUtil.isWindows() && SystemUtil.is64Bit());
        this.gui.labelPlatform.setVisible(true);
        this.gui.choicePlatform.setEnabled(true);
        this.gui.choicePlatform.setVisible(true);
        this.gui.labelMemory.setBounds(275, 90, 275, 18);
        this.gui.memorySlider.setBounds(265, 105, 350, 40);
        this.gui.memoryValue.setBounds(270, 139, 350, 25);
        this.gui.labelDisclaimer.setVisible(false);
        this.gui.serverAddressTextField.setEnabled(true);
        this.gui.portTextField.setEnabled(true);
        this.gui.publicKeyTextField.setEnabled(true);
        this.gui.getdownURLTextField.setEnabled(true);
        this.gui.resetConnectionSettingsButton.setEnabled(true);
      } else {
        this.gui.switchDiscordIntegration.setEnabled(false);
        this.gui.labelPlatform.setVisible(false);
        this.gui.choicePlatform.setEnabled(false);
        this.gui.choicePlatform.setVisible(false);
        this.gui.labelMemory.setBounds(30, 90, 275, 18);
        this.gui.memorySlider.setBounds(20, 105, 350, 40);
        this.gui.memoryValue.setBounds(25, 139, 350, 25);
        this.gui.labelDisclaimer.setVisible(true);
        this.gui.serverAddressTextField.setEnabled(false);
        this.gui.portTextField.setEnabled(false);
        this.gui.publicKeyTextField.setEnabled(false);
        this.gui.getdownURLTextField.setEnabled(false);
        this.gui.resetConnectionSettingsButton.setEnabled(false);
      }

      updateGameJavaVMData();
      updateServerSettings(selectedServer);
    }

  }

  public void updateGameJavaVMData ()
  {
    Thread thread = new Thread(() -> {
      this.gui.javaVMBadge.setText(_localeManager.getValue("m.game_java_vm_data", JavaUtil.getReadableGameJVMData()));

      boolean is64Bit = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64;
      try {
        this.gui.memorySlider.setMaximum(is64Bit ? 4096 : 1024);
        if(this.gui.memorySlider.getValue() >= 256) {
          memoryChangeEvent(this.gui.memorySlider.getValue());
        }
      } catch (Exception ignored) {}
    });
    thread.start();
  }

  public void checkBetaCodes ()
  {
    if(!_settingsManager.getValue("launcher.betaCodes").trim().isEmpty()) {
      this.gui.betaCodeRevalidateButton.setVisible(true);
      this.gui.betaCodeClearLocalButton.setVisible(true);
    }
  }

  public void checkExistingArguments ()
  {
    // Port all the contents of their existing extra.txt into
    // the launcher's gameAdditionalArgs setting so that it's also preserved by it.
    if(!FileUtil.fileExists("old-extra.txt")) {
      try {
        this.gui.argumentsPane.setText(FileUtil.readFile("extra.txt").trim());
        this.gui.eventHandler.saveAdditionalArgs();
      } catch (IOException e) {
        log.error(e);
      }
    }
  }

  public void updateActiveBetaCodes ()
  {
    List<Server> entitledServers = new ArrayList<>();

    for(Server server : _flamingoManager.getServerList()) {
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
        activeCodeText.setText(_localeManager.getValue("m.beta_code_entitling", server.name));
        activeCodeText.setHorizontalAlignment(SwingConstants.LEFT);
        activeCodeText.setFont(Fonts.fontRegSmall);
        activeCodePane.add(activeCodeText);

        this.gui.activeCodesPane.add(activeCodePane);
        count++;
      }
    }

    this.gui.activeCodesLabel.setVisible(!entitledServers.isEmpty());
    this.gui.activeCodesBackground.setVisible(!entitledServers.isEmpty());
    this.gui.activeCodesPane.setVisible(!entitledServers.isEmpty());
    this.gui.activeCodesPaneScrollBar.setVisible(!entitledServers.isEmpty());

    this.gui.activeCodesPane.setLayout(null);

    this.gui.activeCodesPane.setPreferredSize(new Dimension(449, entitledServers.size() * 35));

    this.gui.activeCodesPaneScrollBar.setBounds(
        this.gui.activeCodesPaneScrollBar.getX(),
        this.gui.activeCodesPaneScrollBar.getY(),
        this.gui.activeCodesPaneScrollBar.getWidth(),
        115
    );

    this.gui.activeCodesPane.updateUI();
    this.gui.activeCodesPaneScrollBar.updateUI();
  }

  public void updateServerSettings (Server server)
  {
    this.gui.memorySlider.setValue(Integer.parseInt(_settingsManager.getValue("game.memory", server)));
    this.gui.switchUseCustomGC.setSelected(Boolean.parseBoolean(_settingsManager.getValue("game.useCustomGC", server)));
    this.gui.choiceGC.setSelectedItem(_settingsManager.getValue("game.garbageCollector", server));
    this.gui.switchExplicitGC.setSelected(Boolean.parseBoolean(_settingsManager.getValue("game.disableExplicitGC", server)));
    this.gui.argumentsPane.setText(_settingsManager.getValue("game.additionalArgs", server));

    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
    saveAdditionalArgs();

    this.gui.gameTabViewingSettingsLabel.setText(_localeManager.getValue("m.viewing_settings", server.name));
    this.gui.advancedTabViewingSettingsLabel.setText(_localeManager.getValue("m.viewing_settings", server.name));
  }

  public void checkServerSettingsKeys (String serverName)
  {
    _settingsManager.createKeyIfNotExists("game.memory_" + serverName, "1024");
    _settingsManager.createKeyIfNotExists("game.useCustomGC_" + serverName, "false");
    _settingsManager.createKeyIfNotExists("game.garbageCollector_" + serverName, "ParallelOld");
    _settingsManager.createKeyIfNotExists("game.disableExplicitGC_" + serverName, "false");
    _settingsManager.createKeyIfNotExists("game.additionalArgs_" + serverName, "");
  }

  // Default game settings
  private final int DEFAULT_MEMORY = 1024;
  private final boolean DEFAULT_USE_CUSTOM_GC = false;
  private final String DEFAULT_GC = "ParallelOld";
  private final boolean DEFAULT_DISABLE_EXPLICIT_GC = false;
  private final String DEFAULT_ADDITIONAL_ARGS = "";

  // Recommended game settings
  private final boolean RECOMMENDED_USE_CUSTOM_GC = true;
  private final String RECOMMENDED_GC = "ParallelOld";
  private final boolean RECOMMENDED_DISABLE_EXPLICIT_GC = true;

  // Default connection settings
  private final String DEFAULT_SERVER_ADDRESS = "game.spiralknights.com";
  private final String DEFAULT_PORT = "47624";
  private final String DEFAULT_PUBLIC_KEY = "a5ed0dc3892b9472cfb668e236064e989e95945dad18f3d7e7d8e474d6e03de38bc044c3429b9ca649d0881d601c0eb8ffebc3756f0503f73a8ca1760943ea0e8921ad6f8102026586db3133844bbadbcfcfc666d23982d7684511fbf6cd8bb1d02a14270d0854098d16fe88f99c05825b0fe1b6fd497709106f2c418796aaf7aab7c92f26fcd9fbb3c43df48075fed8dd931273a7b0a333c8de5967797874c1944aed65b47f0792b273a529ac22a2dce08dad04eeebeeff67c7bc99b97682bff488038b28e24f4b5eea77ed966caede52f2c1ecf2b403110a9765daa81ddf718129a040823bead3a0bdca70ef6d08f483757a6d3b6e01fbbcb32006b7872bcd#10001";
  private final String DEFAULT_GETDOWN_URL = "http://gamemedia2.spiralknights.com/spiral/client/";
}
