package com.luuqui.launcher.setting;

import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.*;
import com.luuqui.launcher.flamingo.Flamingo;
import com.luuqui.launcher.flamingo.data.Server;
import com.luuqui.launcher.flamingo.data.Status;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.util.FileUtil;
import com.luuqui.util.JavaUtil;
import com.luuqui.util.ProcessUtil;
import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.luuqui.launcher.setting.Log.log;

public class SettingsEventHandler {

  public static void platformChangeEvent(ItemEvent event) {
    Settings.gamePlatform = (String) SettingsGUI.choicePlatform.getSelectedItem();
    SettingsProperties.setValue("game.platform", (String) SettingsGUI.choicePlatform.getSelectedItem());
  }

  public static void rebuildsChangeEvent(ActionEvent event) {
    Settings.doRebuilds = SettingsGUI.switchCleaning.isSelected();
    SettingsProperties.setValue("launcher.rebuilds", SettingsGUI.switchCleaning.isSelected() ? "true" : "false");
  }

  public static void keepOpenChangeEvent(ActionEvent event) {
    Settings.keepOpen = SettingsGUI.switchKeepOpen.isSelected();
    SettingsProperties.setValue("launcher.keepOpen", SettingsGUI.switchKeepOpen.isSelected() ? "true" : "false");
  }

  public static void forceRebuildEvent() {
    ModLoader.mountRequired = true;
    new Thread(ModLoader::startFileRebuild).start();
  }

  public static void createShortcutChangeEvent(ActionEvent event) {
    Settings.createShortcut = SettingsGUI.switchShortcut.isSelected();
    SettingsProperties.setValue("launcher.createShortcut", SettingsGUI.switchShortcut.isSelected() ? "true" : "false");
  }

  public static void languageChangeEvent(ItemEvent event) {
    if(event.getStateChange() == ItemEvent.SELECTED)
      return; // Prevent triggering 2 times
    Settings.lang = Locale.getLangCode((String) SettingsGUI.choiceLanguage.getSelectedItem());
    SettingsProperties.setValue("launcher.lang", Locale.getLangCode((String) SettingsGUI.choiceLanguage.getSelectedItem()));
    Dialog.push(Locale.getValue("m.prompt_restart_required"), JOptionPane.INFORMATION_MESSAGE);
  }

  public static void customGCChangeEvent(ActionEvent action) {
    Settings.gameUseCustomGC = SettingsGUI.switchUseCustomGC.isSelected();
    String key = "game.useCustomGC";
    if(LauncherApp.selectedServer != null) {
      key += LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }

    SettingsProperties.setValue(key, SettingsGUI.switchUseCustomGC.isSelected() ? "true" : "false");
  }

  public static void choiceGCChangeEvent(ItemEvent event) {
    String key = "game.garbageCollector";
    if(LauncherApp.selectedServer != null) {
      key += LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }

    switch (SettingsGUI.choiceGC.getSelectedIndex()) {
      case 0:
        Settings.gameGarbageCollector = "ParallelOld";
        SettingsProperties.setValue(key, "ParallelOld");
        break;
      case 1:
        Settings.gameGarbageCollector = "Serial";
        SettingsProperties.setValue(key, "Serial");
        break;
      case 2:
        Settings.gameGarbageCollector = "G1";
        SettingsProperties.setValue(key, "G1");
        break;
    }
  }

  public static void disableExplicitGCChangeEvent(ActionEvent action) {
    Settings.gameDisableExplicitGC = SettingsGUI.switchExplicitGC.isSelected();
    String key = "game.disableExplicitGC";
    if(LauncherApp.selectedServer != null) {
      key += LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }

    SettingsProperties.setValue(key, SettingsGUI.switchExplicitGC.isSelected() ? "true" : "false");
  }

  public static void saveAdditionalArgs() {
    Settings.gameAdditionalArgs = SettingsGUI.argumentsPane.getText();
    String key = "game.additionalArgs";
    if(LauncherApp.selectedServer != null) {
      key += LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }

    SettingsProperties.setValue(key, SettingsGUI.argumentsPane.getText());
  }

  public static void memoryChangeEvent(int memory) {
    Settings.gameMemory = memory;
    String key = "game.memory";
    if(LauncherApp.selectedServer != null) {
      key += LauncherApp.selectedServer.isOfficial() ? "" : "_" + LauncherApp.selectedServer.getSanitizedName();
    }

    SettingsProperties.setValue(key, String.valueOf(memory));
  }

  public static void ingameRPCChangeEvent(ActionEvent action) {
    Settings.useIngameRPC = SettingsGUI.switchUseIngameRPC.isSelected();
    SettingsProperties.setValue("launcher.useIngameRPC", SettingsGUI.switchUseIngameRPC.isSelected() ? "true" : "false");
  }

  public static void autoUpdateChangeEvent(ActionEvent action) {
    Settings.autoUpdate = SettingsGUI.switchAutoUpdate.isSelected();
    SettingsProperties.setValue("launcher.autoUpdate", SettingsGUI.switchAutoUpdate.isSelected() ? "true" : "false");
  }

  public static void jvmPatchEvent(ActionEvent action) {
    String javaVMPatchDir = LauncherGlobals.USER_DIR;
    if(!LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
      javaVMPatchDir += File.separator + "thirdparty" + File.separator + LauncherApp.selectedServer.getSanitizedName();
    }
    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar", "forceJVMPatch", javaVMPatchDir}, true);
    SettingsGUI.settingsGUIFrame.dispose();
    System.exit(1);
  }

  public static void saveConnectionSettings() {
    Settings.gameEndpoint = SettingsGUI.serverAddressTextField.getText();
    Settings.gamePort = Integer.parseInt(SettingsGUI.portTextField.getText());
    Settings.gamePublicKey = SettingsGUI.publicKeyTextField.getText();
    Settings.gameGetdownFullURL = SettingsGUI.getdownURLTextField.getText();
    Settings.gameGetdownURL = "http://" + SettingsGUI.getdownURLTextField.getText().split("://")[1].split("/")[0];
    SettingsProperties.setValue("game.endpoint", Settings.gameEndpoint);
    SettingsProperties.setValue("game.port", String.valueOf(Settings.gamePort));
    SettingsProperties.setValue("game.publicKey", Settings.gamePublicKey);
    SettingsProperties.setValue("game.getdownURL", Settings.gameGetdownURL);
    SettingsProperties.setValue("game.getdownFullURL", Settings.gameGetdownFullURL);
  }

  public static void resetConnectionSettingsButtonEvent(ActionEvent action) {
    final String DEFAULT_SERVER_ADDRESS = "game.spiralknights.com";
    final String DEFAULT_PORT = "47624";
    final String DEFAULT_PUBLIC_KEY = "a5ed0dc3892b9472cfb668e236064e989e95945dad18f3d7e7d8e474d6e03de38bc044c3429b9ca649d0881d601c0eb8ffebc3756f0503f73a8ca1760943ea0e8921ad6f8102026586db3133844bbadbcfcfc666d23982d7684511fbf6cd8bb1d02a14270d0854098d16fe88f99c05825b0fe1b6fd497709106f2c418796aaf7aab7c92f26fcd9fbb3c43df48075fed8dd931273a7b0a333c8de5967797874c1944aed65b47f0792b273a529ac22a2dce08dad04eeebeeff67c7bc99b97682bff488038b28e24f4b5eea77ed966caede52f2c1ecf2b403110a9765daa81ddf718129a040823bead3a0bdca70ef6d08f483757a6d3b6e01fbbcb32006b7872bcd#10001";
    final String DEFAULT_GETDOWN_URL = "http://gamemedia2.spiralknights.com/spiral/client/";

    SettingsGUI.serverAddressTextField.setText(DEFAULT_SERVER_ADDRESS);
    SettingsGUI.portTextField.setText(DEFAULT_PORT);
    SettingsGUI.publicKeyTextField.setText(DEFAULT_PUBLIC_KEY);
    SettingsGUI.getdownURLTextField.setText(DEFAULT_GETDOWN_URL);

    saveConnectionSettings();
  }

  public static void resetGameSettingsButtonEvent(ActionEvent action) {
    final int DEFAULT_MEMORY = 1024;
    final boolean DEFAULT_USE_CUSTOM_GC = false;
    final String DEFAULT_GC = "ParallelOld";
    final boolean DEFAULT_DISABLE_EXPLICIT_GC = false;
    final String DEFAULT_ADDITIONAL_ARGS = "";

    SettingsGUI.memorySlider.setValue(DEFAULT_MEMORY);
    SettingsGUI.switchUseCustomGC.setSelected(DEFAULT_USE_CUSTOM_GC);
    SettingsGUI.choiceGC.setSelectedItem(DEFAULT_GC);
    SettingsGUI.switchExplicitGC.setSelected(DEFAULT_DISABLE_EXPLICIT_GC);
    SettingsGUI.argumentsPane.setText(DEFAULT_ADDITIONAL_ARGS);


    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
    saveAdditionalArgs();
  }

  public static void loadRecommendedSettingsButtonEvent(ActionEvent action) {
    final boolean RECOMMENDED_USE_CUSTOM_GC = true;
    final String RECOMMENDED_GC = "ParallelOld";
    final boolean RECOMMENDED_DISABLE_EXPLICIT_GC = true;

    long maximumMemory = ((OperatingSystemMXBean) ManagementFactory
      .getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1048576;

    int recommendedMemory = (int) Math.min(JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 ? 3072 : 1024, maximumMemory / 0.25);

    log.info("Recommended settings: Maximum physical memory is " + maximumMemory
      + ", setting allocated memory to " + recommendedMemory);

    SettingsGUI.memorySlider.setValue(recommendedMemory);
    SettingsGUI.switchUseCustomGC.setSelected(RECOMMENDED_USE_CUSTOM_GC);
    SettingsGUI.choiceGC.setSelectedItem(RECOMMENDED_GC);
    SettingsGUI.switchExplicitGC.setSelected(RECOMMENDED_DISABLE_EXPLICIT_GC);


    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
  }

  public static void copyLauncherLogEvent(ActionEvent action) {
    List<File> files = new ArrayList<>();

    // Add Knight Launcher logs to clipboard.
    files.add(new File(LauncherGlobals.USER_DIR + "\\knightlauncher.log"));

    FileUtil.copyFileToClipboard(files);
  }

  public static void copyGameLogEvent(ActionEvent action) {
    List<File> files = new ArrayList<>();

    // Initial path where we'll pull game logs from.
    String path = LauncherGlobals.USER_DIR;

    // Check if a third party server is selected, in that case, modify the path to copy their logs instead.
    if(!LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
      path += "\\thirdparty\\" + LauncherApp.selectedServer.getSanitizedName();
    }

    // Copy all game logs.
    File getdownLog = new File(path + "\\launcher.log");
    files.add(getdownLog);

    File gameLog = new File(path + "\\projectx.log");
    files.add(gameLog);

    File oldGameLog = new File(path + "\\old-projectx.log");
    files.add(oldGameLog);

    files.removeIf(file -> !FileUtil.fileExists(file.getAbsolutePath()));

    FileUtil.copyFileToClipboard(files);
  }

  public static void copyLogsEvent(ActionEvent action) {
    List<File> files = new ArrayList<>();

    // Add Knight Launcher logs to clipboard.
    files.add(new File(LauncherGlobals.USER_DIR + "\\knightlauncher.log"));

    // Initial path where we'll pull game logs from.
    String path = LauncherGlobals.USER_DIR;

    // Check if a third party server is selected, in that case, modify the path to copy their logs instead.
    if(!LauncherApp.selectedServer.name.equalsIgnoreCase("Official")) {
      path += "\\thirdparty\\" + LauncherApp.selectedServer.getSanitizedName();
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
    FileUtil.copyFileToClipboard(files);
  }

  public static void updateAboutTab(Status status) {
    if(status.version != null) {
      long uptime = System.currentTimeMillis() - status.uptime;
      String uptimeString = Duration.ofMillis(uptime)
        .toString()
        .replace( "PT" , "" )
        .replace( "H" , " " + Locale.getValue("m.hours").toLowerCase() + " " )
        .replace( "M" , " " + Locale.getValue("m.minutes").toLowerCase() + " " );
      uptimeString = uptimeString.substring(0, uptimeString.length() - 7);

      SettingsGUI.labelFlamingoStatus.setText(Locale.getValue("m.flamingo_status", Locale.getValue("m.online")));
      SettingsGUI.labelFlamingoVersion.setText(Locale.getValue("m.flamingo_version", status.version));

      if(uptimeString.isEmpty()) {
        SettingsGUI.labelFlamingoUptime.setText(Locale.getValue("m.flamingo_uptime", Locale.getValue("m.recently_restarted")));
      } else {
        SettingsGUI.labelFlamingoUptime.setText(Locale.getValue("m.flamingo_uptime", uptimeString));
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
  public static int activateBetaCode(String code, boolean force) {

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
    String codes = SettingsProperties.getValue("launcher.betaCodes");

    // This beta code is already present in local properties. We return '2' indicating duplicate.
    if(codes != null && codes.contains(code) && !force) return 2;

    String response = Flamingo.activateBetaCode(code);

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
      LauncherEventHandler.updateServerList(Flamingo.getServerList());
      updateActiveBetaCodes();
      return 1;
    }

    // Return 0 indicating some sort of failure.
    return 0;
  }

  public static void revalidateBetaCodes() {
    if (SettingsProperties.getValue("launcher.betaCodes").equalsIgnoreCase("")) return;

    String[] betaCodes;
    if(SettingsProperties.getValue("launcher.betaCodes").contains(",")) {
      betaCodes = SettingsProperties.getValue("launcher.betaCodes").split(",");
    } else {
      betaCodes = new String[] { SettingsProperties.getValue("launcher.betaCodes") };
    }

    for(String betaCode : betaCodes) {
      activateBetaCode(betaCode, true);
    }
  }

  public static void clearLocalBetaCodes() {
    SettingsProperties.setValue("launcher.betaCodes", "");
  }

  public static void updateActiveBetaCodes() {
    SettingsGUI.updateActiveBetaCodes();
  }

  private static void addBetaCode(String code) {
    // Get the currently loaded codes
    String codes = SettingsProperties.getValue("launcher.betaCodes");

    // Check if there's any codes to properly format the codes string
    if(codes.equalsIgnoreCase("")) {
      codes += code;
    } else {
      // don't add duplicates
      if(codes.contains(code)) return;

      codes += "," + code;
    }

    // Successfully added a new beta code, update the properties file.
    SettingsProperties.setValue("launcher.betaCodes", codes);
  }

  public static void selectedServerChanged() {
    Server selectedServer = LauncherApp.selectedServer;

    if(selectedServer != null) {
      if(selectedServer.name.equalsIgnoreCase("Official")) {
        SettingsGUI.switchUseIngameRPC.setEnabled(true);
        SettingsGUI.choicePlatform.setEnabled(true);
        SettingsGUI.forceRebuildButton.setEnabled(true);
        SettingsGUI.labelDisclaimer.setVisible(false);
        SettingsGUI.serverAddressTextField.setEnabled(true);
        SettingsGUI.portTextField.setEnabled(true);
        SettingsGUI.publicKeyTextField.setEnabled(true);
        SettingsGUI.getdownURLTextField.setEnabled(true);
        SettingsGUI.resetConnectionSettingsButton.setEnabled(true);
      } else {
        SettingsGUI.switchUseIngameRPC.setEnabled(false);
        SettingsGUI.choicePlatform.setEnabled(false);
        SettingsGUI.forceRebuildButton.setEnabled(false);
        SettingsGUI.labelDisclaimer.setVisible(true);
        SettingsGUI.serverAddressTextField.setEnabled(false);
        SettingsGUI.portTextField.setEnabled(false);
        SettingsGUI.publicKeyTextField.setEnabled(false);
        SettingsGUI.getdownURLTextField.setEnabled(false);
        SettingsGUI.resetConnectionSettingsButton.setEnabled(false);
      }

      updateGameJavaVMData();
      updateServerSettings(selectedServer);
    }

  }

  public static void updateGameJavaVMData() {
    Thread thread = new Thread(() -> {
      SettingsGUI.javaVMBadge.setText(Locale.getValue("m.game_java_vm_data", JavaUtil.getReadableGameJVMData()));

      boolean is64Bit = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64;
      try {
        SettingsGUI.memorySlider.setMaximum(is64Bit ? 4096 : 1024);
        if(SettingsGUI.memorySlider.getValue() >= 256) {
          SettingsEventHandler.memoryChangeEvent(SettingsGUI.memorySlider.getValue());
        }
      } catch (Exception ignored) {}
    });
    thread.start();
  }

  public static void updateServerSettings(Server server) {
    String keySuffix = "";
    if(!server.getSanitizedName().equalsIgnoreCase("")) keySuffix = "_" + server.getSanitizedName();

    SettingsGUI.memorySlider.setValue(Integer.parseInt(SettingsProperties.getValue("game.memory" + keySuffix)));
    SettingsGUI.switchUseCustomGC.setSelected(Boolean.parseBoolean(SettingsProperties.getValue("game.useCustomGC" + keySuffix)));
    SettingsGUI.choiceGC.setSelectedItem(SettingsProperties.getValue("game.garbageCollector" + keySuffix));
    SettingsGUI.switchExplicitGC.setSelected(Boolean.parseBoolean("game.disableExplicitGC" + keySuffix));
    SettingsGUI.argumentsPane.setText(SettingsProperties.getValue("game.additionalArgs" + keySuffix));

    customGCChangeEvent(null);
    choiceGCChangeEvent(null);
    disableExplicitGCChangeEvent(null);
    saveAdditionalArgs();
  }

  public static void checkServerSettingsKeys(String serverName) {
    SettingsProperties.createKeyIfNotExists("game.memory_" + serverName, "1024");
    SettingsProperties.createKeyIfNotExists("game.useCustomGC_" + serverName, "false");
    SettingsProperties.createKeyIfNotExists("game.garbageCollector_" + serverName, "ParallelOld");
    SettingsProperties.createKeyIfNotExists("game.disableExplicitGC_" + serverName, "false");
    SettingsProperties.createKeyIfNotExists("game.additionalArgs_" + serverName, "");
  }
}
