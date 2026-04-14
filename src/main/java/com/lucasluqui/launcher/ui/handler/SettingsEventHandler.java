package com.lucasluqui.launcher.ui.handler;

import com.google.inject.Inject;
import com.lucasluqui.dialog.Dialog;
import com.lucasluqui.launcher.LauncherContext;
import com.lucasluqui.launcher.LauncherGlobals;
import com.lucasluqui.launcher.LocaleManager;
import com.lucasluqui.launcher.flamingo.FlamingoManager;
import com.lucasluqui.launcher.flamingo.data.Server;
import com.lucasluqui.launcher.flamingo.data.Status;
import com.lucasluqui.launcher.mod.ModManager;
import com.lucasluqui.launcher.setting.Settings;
import com.lucasluqui.launcher.setting.SettingsManager;
import com.lucasluqui.launcher.ui.LauncherUI;
import com.lucasluqui.launcher.ui.SettingsUI;
import com.lucasluqui.util.*;
import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.lucasluqui.launcher.ui.handler.Log.log;

public class SettingsEventHandler
{
  @Inject
  public SettingsEventHandler (LauncherContext _ctx,
                               ModManager _modManager,
                               LocaleManager _localeManager,
                               SettingsManager _settingsManager,
                               FlamingoManager _flamingoManager)
  {
    this._ctx = _ctx;
    this._modManager = _modManager;
    this._localeManager = _localeManager;
    this._settingsManager = _settingsManager;
    this._flamingoManager = _flamingoManager;
  }

  public void platformChangeEvent (ItemEvent event)
  {
    Settings.gamePlatform = (String) this.ui.choicePlatform.getSelectedItem();
    _settingsManager.setValue("game.platform", (String) this.ui.choicePlatform.getSelectedItem());
  }

  public void rebuildsChangeEvent (ActionEvent event)
  {
    // Only display a warning if the user is disabling this setting.
    boolean confirm = true;
    if (!this.ui.switchCleaning.isSelected()) {
      confirm = Dialog.pushWithConfirm(_localeManager.getValue("m.settings_warning"), _localeManager.getValue("t.warning"), JOptionPane.WARNING_MESSAGE);
    }

    if (confirm) {
      Settings.doRebuilds = this.ui.switchCleaning.isSelected();
      _settingsManager.setValue("launcher.rebuilds", String.valueOf(this.ui.switchCleaning.isSelected()));
    }

    this.ui.switchCleaning.setSelected(Settings.doRebuilds);
  }

  public void keepOpenChangeEvent (ActionEvent event)
  {
    Settings.keepOpen = this.ui.switchKeepOpen.isSelected();
    _settingsManager.setValue("launcher.keepOpen", String.valueOf(this.ui.switchKeepOpen.isSelected()));
  }

  public void forceRebuildEvent ()
  {
    _modManager.setMountRequired(true);
    new Thread(_modManager::startStrictFileRebuild).start();
    _ctx.getApp().returnToHome();
  }

  public void createShortcutChangeEvent (ActionEvent event)
  {
    Settings.createShortcut = this.ui.switchShortcut.isSelected();
    _settingsManager.setValue("launcher.createShortcut", String.valueOf(this.ui.switchShortcut.isSelected()));
  }

  public void languageChangeEvent (ItemEvent event)
  {
    if (event.getStateChange() == ItemEvent.SELECTED) return; // Prevent triggering 2 times
    Settings.lang = _localeManager.getLangCode((String) this.ui.choiceLanguage.getSelectedItem());
    _settingsManager.setValue("launcher.lang", _localeManager.getLangCode((String) this.ui.choiceLanguage.getSelectedItem()));
    Dialog.push(_localeManager.getValue("m.prompt_restart_required"), JOptionPane.INFORMATION_MESSAGE);
  }

  public void customGCChangeEvent (ActionEvent action)
  {
    Settings.gameUseCustomGC = this.ui.switchUseCustomGC.isSelected();
    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.useCustomGC", String.valueOf(this.ui.switchUseCustomGC.isSelected()), selectedServer);
  }

  public void choiceGCChangeEvent (ItemEvent event)
  {
    Server selectedServer = _flamingoManager.getSelectedServer();

    switch (this.ui.choiceGC.getSelectedIndex()) {
      case 0:
        Settings.gameGarbageCollector = "Parallel";
        _settingsManager.setValue("game.garbageCollector.v2", "Parallel", selectedServer);
        break;
      case 1:
        Settings.gameGarbageCollector = "ZGC";
        _settingsManager.setValue("game.garbageCollector.v2", "ZGC", selectedServer);
        break;
      case 2:
        Settings.gameGarbageCollector = "Serial";
        _settingsManager.setValue("game.garbageCollector.v2", "Serial", selectedServer);
        break;
      case 3:
        Settings.gameGarbageCollector = "G1";
        _settingsManager.setValue("game.garbageCollector.v2", "G1", selectedServer);
        break;
    }
  }

  public void disableExplicitGCChangeEvent (ActionEvent action)
  {
    Settings.gameDisableExplicitGC = this.ui.switchExplicitGC.isSelected();
    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.disableExplicitGC", String.valueOf(this.ui.switchExplicitGC.isSelected()), selectedServer);
  }

  public void saveAdditionalArgs ()
  {
    saveAdditionalArgs(false);
  }

  public void saveAdditionalArgs (boolean force)
  {
    if (!force) {
      if (this.ui.argumentsPane.getText().equalsIgnoreCase("0")) {
        // no clue why this happens, just ignore it I guess.
        return;
      }

      if (_settingsManager.validAdditionalArgs(this.ui.argumentsPane.getText())) {
        Settings.gameAdditionalArgs = this.ui.argumentsPane.getText();
      } else {
        Settings.gameAdditionalArgs = "";
        this.ui.argumentsPane.setText(Settings.gameAdditionalArgs);
        Dialog.push(
          _localeManager.getValue("m.invalid_additional_args_save_warning"),
          _localeManager.getValue("t.invalid_additional_args"),
          JOptionPane.WARNING_MESSAGE
        );
      }
    }

    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.additionalArgs.v2", Settings.gameAdditionalArgs, selectedServer);
  }

  public void memoryChangeEvent (int memory)
  {
    Settings.gameMemory = memory;
    Server selectedServer = _flamingoManager.getSelectedServer();
    _settingsManager.setValue("game.memory", String.valueOf(memory), selectedServer);
  }

  public void ingameRPCChangeEvent (ActionEvent action)
  {
    Settings.useIngameRPC = this.ui.switchDiscordIntegration.isSelected();
    _settingsManager.setValue("launcher.useIngameRPC", String.valueOf(this.ui.switchDiscordIntegration.isSelected()));
  }

  public void autoUpdateChangeEvent (ActionEvent action)
  {
    Settings.autoUpdate = this.ui.switchAutoUpdate.isSelected();
    _settingsManager.setValue("launcher.autoUpdate", String.valueOf(this.ui.switchAutoUpdate.isSelected()));
  }

  public void filePurgingChangeEvent (ActionEvent action)
  {
    // Only display a warning if the user is disabling this setting.
    boolean confirm = true;
    if (!this.ui.switchFilePurging.isSelected()) {
      confirm = Dialog.pushWithConfirm(_localeManager.getValue("m.settings_warning"), _localeManager.getValue("t.warning"), JOptionPane.WARNING_MESSAGE);
    }

    if (confirm) {
      Settings.filePurging = this.ui.switchFilePurging.isSelected();
      _settingsManager.setValue("launcher.filePurging", String.valueOf(this.ui.switchFilePurging.isSelected()));
    }

    this.ui.switchFilePurging.setSelected(Settings.filePurging);
  }

  public void jvmPatchEvent (ActionEvent action)
  {
    String javaVMPatchDir = LauncherGlobals.USER_DIR;

    // If the game's Java VM version is 8 or lower, mark it as legacy so that only
    // legacy Java VMs are offered to patch.
    final boolean legacy = JavaUtil.isLegacy();

    if (!_flamingoManager.getSelectedServer().isOfficial()) {
      javaVMPatchDir += File.separator + "thirdparty" + File.separator + _flamingoManager.getSelectedServer().getSanitizedName();
    }

    ProcessUtil.run(new String[]{"java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar", "forceJVMPatch", javaVMPatchDir, String.valueOf(legacy)}, true);
    this.ui.guiFrame.dispose();
    System.exit(1);
  }

  public void saveConnectionSettings ()
  {
    Settings.gameEndpoint = this.ui.serverAddressTextField.getText();
    Settings.gamePort = Integer.parseInt(this.ui.portTextField.getText());
    Settings.gamePublicKey = this.ui.publicKeyTextField.getText();
    Settings.gameGetdownFullURL = this.ui.getdownURLTextField.getText();
    Settings.gameGetdownURL = "http://" + this.ui.getdownURLTextField.getText().split("://")[1].split("/")[0];

    _settingsManager.setValue("game.endpoint", Settings.gameEndpoint);
    _settingsManager.setValue("game.port", String.valueOf(Settings.gamePort));
    _settingsManager.setValue("game.publicKey", Settings.gamePublicKey);
    _settingsManager.setValue("game.getdownURL", Settings.gameGetdownURL);
    _settingsManager.setValue("game.getdownFullURL", Settings.gameGetdownFullURL);
  }

  public void resetConnectionSettingsButtonEvent (ActionEvent action)
  {
    this.ui.serverAddressTextField.setText(DEFAULT_SERVER_ADDRESS);
    this.ui.portTextField.setText(DEFAULT_PORT);
    this.ui.publicKeyTextField.setText(DEFAULT_PUBLIC_KEY);
    this.ui.getdownURLTextField.setText(DEFAULT_GETDOWN_URL);

    saveConnectionSettings();
  }

  public void resetGameSettingsButtonEvent (ActionEvent action)
  {
    this.ui.memorySlider.setValue(DEFAULT_MEMORY);
    this.ui.switchUseCustomGC.setSelected(DEFAULT_USE_CUSTOM_GC);
    this.ui.choiceGC.setSelectedItem(DEFAULT_GC);
    this.ui.switchExplicitGC.setSelected(DEFAULT_DISABLE_EXPLICIT_GC);
    this.ui.argumentsPane.setText(DEFAULT_ADDITIONAL_ARGS);

    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
    saveAdditionalArgs();
  }

  public void loadRecommendedSettingsButtonEvent (ActionEvent action)
  {
    long maximumMemory = ((OperatingSystemMXBean) ManagementFactory
      .getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1048576;

    int recommendedMemory = (int) Math.min(JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64
      ? RECOMMENDED_MAX_MEMORY : 1024, maximumMemory * 0.25);

    log.info("Recommended settings: Maximum physical memory is " + maximumMemory
      + ", setting allocated memory to " + recommendedMemory);

    this.ui.memorySlider.setValue(recommendedMemory);
    this.ui.switchUseCustomGC.setSelected(RECOMMENDED_USE_CUSTOM_GC);
    this.ui.choiceGC.setSelectedItem(RECOMMENDED_GC);
    this.ui.switchExplicitGC.setSelected(RECOMMENDED_DISABLE_EXPLICIT_GC);

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

  public void copyGameLogEvent (ActionEvent action)
  {
    List<File> files = new ArrayList<>();

    // Initial path where we'll pull game logs from.
    String path = LauncherGlobals.USER_DIR;

    // Check if a third party server is selected, in that case, modify the path to copy their logs instead.
    if (!_flamingoManager.getSelectedServer().name.equalsIgnoreCase("Official")) {
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
    if (!_flamingoManager.getSelectedServer().name.equalsIgnoreCase("Official")) {
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
    if (_flamingoManager.getSelectedServer() != null) {
      rootDir = _flamingoManager.getSelectedServer().getRootDirectory();
    }
    DesktopUtil.openDir(rootDir);
  }

  public void updateAboutTab (Status status)
  {
    if (status.version != null) {
      long uptime = System.currentTimeMillis() - status.uptime;
      String uptimeString = Duration.ofMillis(uptime)
        .toString()
        .replace("PT", "")
        .replace("H", " " + _localeManager.getValue("m.hours").toLowerCase() + " ")
        .replace("M", " " + _localeManager.getValue("m.minutes").toLowerCase() + " ");
      uptimeString = uptimeString.substring(0, uptimeString.length() - 7);

      this.ui.labelFlamingoStatus.setText(_localeManager.getValue("m.flamingo_status", _localeManager.getValue("m.online")));
      this.ui.labelFlamingoVersion.setText(_localeManager.getValue("m.flamingo_version", status.version));

      if (uptimeString.isEmpty()) {
        this.ui.labelFlamingoUptime.setText(_localeManager.getValue("m.flamingo_uptime", _localeManager.getValue("m.recently_restarted")));
      } else {
        this.ui.labelFlamingoUptime.setText(_localeManager.getValue("m.flamingo_uptime", uptimeString));
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
    if (code.isEmpty()) return -1;

    // Contains any more invalid characters.
    if (!code.matches("^[0-9A-Za-z\\s-]+$")) return -1;

    // Get the currently loaded codes
    String codes = _settingsManager.getValue("launcher.betaCodes");

    // This beta code is already present in local properties. We return '2' indicating duplicate.
    if (codes != null && codes.contains(code) && !force) return 2;

    String response = _flamingoManager.activateBetaCode(code);

    // someone already activated this code.
    if (response.equalsIgnoreCase("already_used")) return 3;

    // this machine id already activated this code, we add it anyways to avoid further requests from this machine.
    if (response.equalsIgnoreCase("already_used_same")) {
      addBetaCode(code);
      return 4;
    }

    // this code does not exist.
    if (response.equalsIgnoreCase("not_exists")) return 5;

    // the code was successfully activated, we update the server list and return a success code to the GUI.
    if (response.equalsIgnoreCase("success")) {
      addBetaCode(code);
      _flamingoManager.updateServerList();
      return 1;
    }

    // Return 0 indicating some sort of failure.
    return 0;
  }

  public void revalidateBetaCodes ()
  {
    if (_settingsManager.getValue("launcher.betaCodes").equalsIgnoreCase("")) return;

    String[] betaCodes;
    if (_settingsManager.getValue("launcher.betaCodes").contains(",")) {
      betaCodes = _settingsManager.getValue("launcher.betaCodes").split(",");
    } else {
      betaCodes = new String[]{_settingsManager.getValue("launcher.betaCodes")};
    }

    for (String betaCode : betaCodes) {
      activateBetaCode(betaCode, true);
    }
    this.ui.updateActiveBetaCodes();
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
    if (codes.equalsIgnoreCase("")) {
      codes += code;
    } else {
      // don't add duplicates
      if (codes.contains(code)) return;

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
        this.ui.switchDiscordIntegration.setEnabled(SystemUtil.isWindows() && SystemUtil.is64Bit());
        this.ui.labelPlatform.setVisible(true);
        this.ui.choicePlatform.setEnabled(true);
        this.ui.choicePlatform.setVisible(true);
        this.ui.labelMemory.setBounds(275, 90, 275, 18);
        this.ui.memorySlider.setBounds(265, 105, 350, 40);
        this.ui.memoryValue.setBounds(270, 139, 350, 25);
        this.ui.labelDisclaimer.setVisible(false);
        this.ui.serverAddressTextField.setEnabled(true);
        this.ui.portTextField.setEnabled(true);
        this.ui.publicKeyTextField.setEnabled(true);
        this.ui.getdownURLTextField.setEnabled(true);
        this.ui.resetConnectionSettingsButton.setEnabled(true);
      } else {
        this.ui.switchDiscordIntegration.setEnabled(false);
        this.ui.labelPlatform.setVisible(false);
        this.ui.choicePlatform.setEnabled(false);
        this.ui.choicePlatform.setVisible(false);
        this.ui.labelMemory.setBounds(30, 90, 275, 18);
        this.ui.memorySlider.setBounds(20, 105, 350, 40);
        this.ui.memoryValue.setBounds(25, 139, 350, 25);
        this.ui.labelDisclaimer.setVisible(true);
        this.ui.serverAddressTextField.setEnabled(false);
        this.ui.portTextField.setEnabled(false);
        this.ui.publicKeyTextField.setEnabled(false);
        this.ui.getdownURLTextField.setEnabled(false);
        this.ui.resetConnectionSettingsButton.setEnabled(false);
      }

      updateGameJavaVMData();
      updateServerSettings(selectedServer);
    }

  }

  public void updateGameJavaVMData ()
  {
    Thread thread = new Thread(() -> {
      this.ui.javaVMBadge.setText(_localeManager.getValue("m.game_java_vm_data", JavaUtil.getReadableGameJVMData()));

      boolean is64Bit = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64;
      try {
        this.ui.memorySlider.setMaximum(is64Bit ? 4096 : 1024);
        if (this.ui.memorySlider.getValue() >= 256) {
          memoryChangeEvent(this.ui.memorySlider.getValue());
        }
      } catch (Exception ignored) {}
    });
    thread.start();
  }

  public void checkBetaCodes ()
  {
    if (!_settingsManager.getValue("launcher.betaCodes").trim().isEmpty()) {
      this.ui.betaCodeRevalidateButton.setVisible(true);
      this.ui.betaCodeClearLocalButton.setVisible(true);
    }
  }

  public void checkExistingArguments ()
  {
    // Port all the contents of their existing extra.txt into
    // the launcher's gameAdditionalArgs setting so that it's also preserved by it.
    if (FileUtil.fileExists("extra.txt") && !FileUtil.fileExists("old-extra.txt")) {
      try {
        String extraTxtContents = FileUtil.readFile("extra.txt").trim();

        // Avoid porting ParallelOld, causes issues post Java update.
        if (!extraTxtContents.contains("ParallelOld")) {
          this.ui.argumentsPane.setText(extraTxtContents);
        }

        this.ui.eventHandler.saveAdditionalArgs();
      } catch (Exception e) {
        log.error(e);
      }
    }
  }

  public void updateActiveBetaCodes ()
  {
    this.ui.updateActiveBetaCodes();
  }

  public void updateServerSettings (Server server)
  {
    this.ui.memorySlider.setValue(Integer.parseInt(_settingsManager.getValue("game.memory", server)));
    this.ui.switchUseCustomGC.setSelected(Boolean.parseBoolean(_settingsManager.getValue("game.useCustomGC", server)));
    this.ui.choiceGC.setSelectedItem(_settingsManager.getValue("game.garbageCollector", server));
    this.ui.switchExplicitGC.setSelected(Boolean.parseBoolean(_settingsManager.getValue("game.disableExplicitGC", server)));
    this.ui.argumentsPane.setText(_settingsManager.getValue("game.additionalArgs.v2", server));

    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
    saveAdditionalArgs();

    if (server.isOfficial()) {
      this.ui.gameTabViewingSettingsLabel.setVisible(false);
      this.ui.advancedTabViewingSettingsLabel.setVisible(false);
    } else {
      this.ui.gameTabViewingSettingsLabel.setVisible(true);
      this.ui.advancedTabViewingSettingsLabel.setVisible(true);
      this.ui.gameTabViewingSettingsLabel.setText(_localeManager.getValue("m.viewing_settings", server.name));
      this.ui.advancedTabViewingSettingsLabel.setText(_localeManager.getValue("m.viewing_settings", server.name));
    }
  }

  public void checkServerSettingsKeys (String serverName)
  {
    _settingsManager.createKeyIfNotExists("game.memory_" + serverName, "1024");
    _settingsManager.createKeyIfNotExists("game.useCustomGC_" + serverName, "false");
    _settingsManager.createKeyIfNotExists("game.garbageCollector_" + serverName, "ParallelOld");
    _settingsManager.createKeyIfNotExists("game.disableExplicitGC_" + serverName, "false");
    _settingsManager.createKeyIfNotExists("game.additionalArgs.v2_" + serverName, "");
  }

  @Inject private SettingsUI ui;

  protected LauncherContext _ctx;
  protected ModManager _modManager;
  protected LocaleManager _localeManager;
  protected SettingsManager _settingsManager;
  protected FlamingoManager _flamingoManager;

  // Default game settings
  private final int DEFAULT_MEMORY = 1024;
  private final boolean DEFAULT_USE_CUSTOM_GC = false;
  private final String DEFAULT_GC = "Parallel";
  private final boolean DEFAULT_DISABLE_EXPLICIT_GC = false;
  private final String DEFAULT_ADDITIONAL_ARGS = "";

  // Recommended game settings
  private final int RECOMMENDED_MAX_MEMORY = 3072;
  private final boolean RECOMMENDED_USE_CUSTOM_GC = true;
  private final String RECOMMENDED_GC = "Parallel";
  private final boolean RECOMMENDED_DISABLE_EXPLICIT_GC = true;

  // Default connection settings
  private final String DEFAULT_SERVER_ADDRESS = "game.spiralknights.com";
  private final String DEFAULT_PORT = "47624";
  private final String DEFAULT_PUBLIC_KEY = "a5ed0dc3892b9472cfb668e236064e989e95945dad18f3d7e7d8e474d6e03de38bc044c3429b9ca649d0881d601c0eb8ffebc3756f0503f73a8ca1760943ea0e8921ad6f8102026586db3133844bbadbcfcfc666d23982d7684511fbf6cd8bb1d02a14270d0854098d16fe88f99c05825b0fe1b6fd497709106f2c418796aaf7aab7c92f26fcd9fbb3c43df48075fed8dd931273a7b0a333c8de5967797874c1944aed65b47f0792b273a529ac22a2dce08dad04eeebeeff67c7bc99b97682bff488038b28e24f4b5eea77ed966caede52f2c1ecf2b403110a9765daa81ddf718129a040823bead3a0bdca70ef6d08f483757a6d3b6e01fbbcb32006b7872bcd#10001";
  private final String DEFAULT_GETDOWN_URL = "http://gamemedia2.spiralknights.com/spiral/client/";
}
