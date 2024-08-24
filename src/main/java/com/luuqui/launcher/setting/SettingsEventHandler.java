package com.luuqui.launcher.setting;

import com.luuqui.dialog.Dialog;
import com.luuqui.launcher.LauncherApp;
import com.luuqui.launcher.LauncherEventHandler;
import com.luuqui.launcher.Locale;
import com.luuqui.launcher.LauncherGlobals;
import com.luuqui.launcher.flamingo.Flamingo;
import com.luuqui.launcher.flamingo.data.Status;
import com.luuqui.launcher.mod.ModLoader;
import com.luuqui.util.FileUtil;
import com.luuqui.util.ProcessUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
    ModLoader.startFileRebuild();
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
    Dialog.pushTranslated(Locale.getValue("m.prompt_restart_required"), JOptionPane.WARNING_MESSAGE);
  }

  public static void useStringDeduplicationChangeEvent(ActionEvent action) {
    Settings.gameUseStringDeduplication = SettingsGUI.switchStringDedup.isSelected();
    SettingsProperties.setValue("game.useStringDeduplication", SettingsGUI.switchStringDedup.isSelected() ? "true" : "false");
  }

  public static void customGCChangeEvent(ActionEvent action) {
    Settings.gameUseCustomGC = SettingsGUI.switchUseCustomGC.isSelected();
    SettingsProperties.setValue("game.useCustomGC", SettingsGUI.switchUseCustomGC.isSelected() ? "true" : "false");
  }

  public static void choiceGCChangeEvent(ItemEvent event) {
    switch (SettingsGUI.choiceGC.getSelectedIndex()) {
      case 0:
        Settings.gameGarbageCollector = "ParallelOld";
        SettingsProperties.setValue("game.garbageCollector", "ParallelOld");
        break;
      case 1:
        Settings.gameGarbageCollector = "Serial";
        SettingsProperties.setValue("game.garbageCollector", "Serial");
        break;
      case 2:
        Settings.gameGarbageCollector = "G1";
        SettingsProperties.setValue("game.garbageCollector", "G1");
        break;
    }
  }

  public static void disableExplicitGCChangeEvent(ActionEvent action) {
    Settings.gameDisableExplicitGC = SettingsGUI.switchExplicitGC.isSelected();
    SettingsProperties.setValue("game.disableExplicitGC", SettingsGUI.switchExplicitGC.isSelected() ? "true" : "false");
  }

  public static void saveAdditionalArgs() {
    Settings.gameAdditionalArgs = SettingsGUI.argumentsPane.getText();
    SettingsProperties.setValue("game.additionalArgs", SettingsGUI.argumentsPane.getText());
  }

  public static void memoryChangeEvent(int memory) {
    Settings.gameMemory = memory;
    SettingsProperties.setValue("game.memory", String.valueOf(memory));
  }


  public static void styleChangeEvent(ItemEvent event) {
    if(event.getStateChange() == ItemEvent.SELECTED)
      return; // Prevent triggering 2 times
    SettingsProperties.setValue("launcher.style", SettingsGUI.choiceStyle.getSelectedIndex() == 0 ? "dark" : "light");
    Dialog.pushTranslated(Locale.getValue("m.prompt_restart_required"), JOptionPane.WARNING_MESSAGE);
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
    SettingsProperties.setValue("launcher.jvm_patched", "false");
    ProcessUtil.run(new String[] { "java", "-jar", LauncherGlobals.USER_DIR + File.separator + "KnightLauncher.jar", "forceJVMPatch"}, true);
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

  public static void resetButtonEvent(ActionEvent action) {
    SettingsGUI.serverAddressTextField.setText("game.spiralknights.com");
    SettingsGUI.portTextField.setText("47624");
    SettingsGUI.publicKeyTextField.setText("a5ed0dc3892b9472cfb668e236064e989e95945dad18f3d7e7d8e474d6e03de38bc044c3429b9ca649d0881d601c0eb8ffebc3756f0503f73a8ca1760943ea0e8921ad6f8102026586db3133844bbadbcfcfc666d23982d7684511fbf6cd8bb1d02a14270d0854098d16fe88f99c05825b0fe1b6fd497709106f2c418796aaf7aab7c92f26fcd9fbb3c43df48075fed8dd931273a7b0a333c8de5967797874c1944aed65b47f0792b273a529ac22a2dce08dad04eeebeeff67c7bc99b97682bff488038b28e24f4b5eea77ed966caede52f2c1ecf2b403110a9765daa81ddf718129a040823bead3a0bdca70ef6d08f483757a6d3b6e01fbbcb32006b7872bcd#10001");
    SettingsGUI.getdownURLTextField.setText("http://gamemedia2.spiralknights.com/spiral/client/");
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
      path += "\\thirdparty\\" + LauncherApp.getSanitizedServerName(LauncherApp.selectedServer.name);
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
      path += "\\thirdparty\\" + LauncherApp.getSanitizedServerName(LauncherApp.selectedServer.name);
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
        .replace( "H" , " hours " )
        .replace( "M" , " minutes " );
      uptimeString = uptimeString.substring(0, uptimeString.length() - 7);

      SettingsGUI.labelFlamingoStatus.setText("Flamingo status: Online");
      SettingsGUI.labelFlamingoVersion.setText("Flamingo version: " + status.version);
      SettingsGUI.labelFlamingoUptime.setText("Flamingo uptime: " + uptimeString);
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
      .trim();

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

  private static void addBetaCode(String code) {
    // Get the currently loaded codes
    String codes = SettingsProperties.getValue("launcher.betaCodes");

    // Check if there's any codes to properly format the codes string
    if(codes.equalsIgnoreCase("")) {
      codes += code;
    } else {
      codes += "," + code;
    }

    // Successfully added a new beta code, update the properties file.
    SettingsProperties.setValue("launcher.betaCodes", codes);
  }
}
